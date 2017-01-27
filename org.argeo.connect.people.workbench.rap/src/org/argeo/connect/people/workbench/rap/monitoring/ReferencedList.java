package org.argeo.connect.people.workbench.rap.monitoring;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;

import org.apache.commons.io.IOUtils;
import org.argeo.cms.ui.workbench.util.PrivilegedJob;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.workbench.rap.providers.SimpleLazyContentProvider;
import org.argeo.connect.people.workbench.rap.util.Refreshable;
import org.argeo.eclipse.ui.EclipseJcrMonitor;
import org.argeo.jcr.JcrMonitor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

/**
 * Quick and dirty editor to monitor number of references that point to a given
 * node: we have noticed issues with PostgreSQL cache management on big data
 * import
 * 
 * TODO work on this to enrich the generic monitoring perspective.
 */
public class ReferencedList extends NodeTypeList implements Refreshable {

	private final int MIN_REF_NB = 5;
	private final String relPath = "/references";
	private final String[] headers = { "Path", "Nb of occurences" };
	private final String prefix = "refList";

	// This page widget
	private NameLP nameLP = new NameLP();
	private CountLP countLP = new CountLP();

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		setPartName("Referenced list");
		setTitleToolTip("Display the list of referenceable nodes that "
				+ "have more than 10 references");
	}

	@Override
	public void forceRefresh(Object object) {
		new QueryJob().schedule();
	}

	/** Privileged job that performs the query asynchronously */
	private class QueryJob extends PrivilegedJob {
		private TableViewer tableViewer;
		private Table table;

		public QueryJob() {
			super("AsynchronousQuery");
			tableViewer = getTableViewer();
			table = getTableViewer().getTable();
		}

		protected IStatus doRun(IProgressMonitor progressMonitor) {
			JcrMonitor monitor = new EclipseJcrMonitor(progressMonitor);
			if (monitor != null && !monitor.isCanceled())
				monitor.beginTask("Querying the repository", -1);
			OutputStream outputStream = null;
			try {
				Query query = getSession()
						.getWorkspace()
						.getQueryManager()
						.createQuery(
								"select * from [nt:unstructured] as instances",
								Query.JCR_SQL2);
				NodeIterator nit = query.execute().getNodes();

				final List<String[]> infos = new ArrayList<String[]>();

				if (nit.hasNext()) {
					outputStream = new FileOutputStream(createLogFile(relPath,
							prefix));
					writeLine(outputStream, headers);
				}

				while (nit.hasNext()) {
					Node currNode = nit.nextNode();
					PropertyIterator pit = currNode.getReferences();
					if (pit.getSize() > MIN_REF_NB) {
						String[] vals = new String[2];
						vals[0] = nameLP.getText(currNode);
						vals[1] = countLP.getText(pit.getSize());
						infos.add(vals);
						writeLine(outputStream, vals);
					}
				}

				outputStream.flush();
				outputStream.close();

				if (table.isDisposed())
					return Status.OK_STATUS;

				if (table.isDisposed())
					return Status.OK_STATUS;

				table.getDisplay().asyncExec(new Runnable() {
					public void run() {
						SimpleLazyContentProvider lazyCp = (SimpleLazyContentProvider) tableViewer
								.getContentProvider();
						Object[] input = null;
						if (!infos.isEmpty())
							input = infos.toArray(new String[1][2]);
						lazyCp.setElements(input);
					}
				});
			} catch (RepositoryException e) {
				throw new PeopleException(
						"Unable to refresh Referenced node table", e);
			} catch (IOException e) {
				throw new PeopleException(
						"Unable write to log file with prefix " + prefix, e);
			} finally {
				IOUtils.closeQuietly(outputStream);
			}
			return Status.OK_STATUS;
		}
	}

	// PROVIDERS
	protected class NameLP extends ColumnLabelProvider {
		private static final long serialVersionUID = -8179051587196328000L;

		@Override
		public String getText(Object element) {
			try {
				Node node = (Node) element;
				return node.getPath();
			} catch (RepositoryException e) {
				throw new PeopleException("Unable to retrieve name " + "for "
						+ element, e);
			}
		}
	}

	protected class CountLP extends ColumnLabelProvider {
		private static final long serialVersionUID = -4022534826825314784L;

		@Override
		public String getText(Object element) {
			// return numberFormat.format((Long) element);
			return "" + (Long) element;
		}
	}
}