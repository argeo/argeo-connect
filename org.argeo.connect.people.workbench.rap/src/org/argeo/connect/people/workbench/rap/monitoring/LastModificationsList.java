package org.argeo.connect.people.workbench.rap.monitoring;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Query;

import org.argeo.cms.ui.workbench.util.PrivilegedJob;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.workbench.PeopleWorkbenchService;
import org.argeo.connect.people.workbench.rap.PeopleRapPlugin;
import org.argeo.connect.people.workbench.rap.editors.util.AbstractPeopleBasicEditor;
import org.argeo.connect.people.workbench.rap.providers.SimpleLazyContentProvider;
import org.argeo.connect.ui.workbench.Refreshable;
import org.argeo.connect.ui.workbench.util.JcrViewerDClickListener;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.util.XPathUtils;
import org.argeo.eclipse.ui.EclipseJcrMonitor;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.eclipse.ui.jcr.lists.SimpleJcrNodeLabelProvider;
import org.argeo.eclipse.ui.utils.ViewerUtils;
import org.argeo.jcr.JcrMonitor;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

/**
 * Quick and dirty editor to monitor the last 100 modifications on a repo
 * 
 * TODO work on this to enrich the generic monitoring perspective.
 */
public class LastModificationsList extends AbstractPeopleBasicEditor implements Refreshable {

	/* DEPENDENCY INJECTION */
	private PeopleWorkbenchService peopleWorkbenchService;

	// This page widget
	private TableViewer tableViewer;
	private SimpleLazyContentProvider lazyCp;

	// Utils
	final static int QUERY_LIMIT = 100;
	protected DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy 'at' HH:mm");

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		setPartName("Last modif. list");
		setTitleToolTip("Display the list of the most recent " + "modifications done on the system");
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());
		// The table itself
		tableViewer = createTableViewer(parent, SWT.READ_ONLY | SWT.VIRTUAL);
		tableViewer.addDoubleClickListener(new JcrViewerDClickListener(peopleWorkbenchService));
		forceRefresh(null);
	}

	private TableViewer createTableViewer(final Composite parent, int tableStyle) {
		parent.setLayout(new GridLayout());
		Table table = new Table(parent, tableStyle);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.setLayoutData(EclipseUiUtils.fillAll());

		TableViewer viewer = new TableViewer(table);
		TableViewerColumn column;
		column = ViewerUtils.createTableViewerColumn(viewer, "Title", SWT.NONE, 250);
		column.setLabelProvider(new SimpleJcrNodeLabelProvider(Property.JCR_TITLE));

		column = ViewerUtils.createTableViewerColumn(viewer, "Last modif by ... on ...", SWT.NONE, 200);
		column.setLabelProvider(new ModifyLP());

		column = ViewerUtils.createTableViewerColumn(viewer, "Path", SWT.NONE, 350);
		column.setLabelProvider(new PathLP());

		lazyCp = new SimpleLazyContentProvider(viewer);
		viewer.setContentProvider(lazyCp);
		return viewer;
	}

	@Override
	public void forceRefresh(Object object) {
		new QueryJob(tableViewer).schedule();
	}

	/** Privileged job that performs the query asynchronously */
	private class QueryJob extends PrivilegedJob {

		// Enable sanity check before updating the UI
		private Table table;

		public QueryJob(TableViewer tableViewer) {
			super("AsynchronousQuery");
			table = tableViewer.getTable();
		}

		protected IStatus doRun(IProgressMonitor progressMonitor) {
			Session session = getSession();
			final List<Node> elements = new ArrayList<Node>();
			try {
				JcrMonitor monitor = new EclipseJcrMonitor(progressMonitor);
				if (monitor != null && !monitor.isCanceled()) {
					monitor.beginTask("Retrieving last modified nodes", -1);

					// XPath
					StringBuilder builder = new StringBuilder();
					builder.append("//element(*, ").append(NodeType.MIX_LAST_MODIFIED).append(")");
					builder.append(" order by @");
					builder.append(Property.JCR_LAST_MODIFIED);
					builder.append(" descending ");
					Query query = XPathUtils.createQuery(session, builder.toString());

					// SQL2
					// String queryStr = "SELECT * FROM ["
					// + NodeType.MIX_LAST_MODIFIED + "] ORDER BY ["
					// + Property.JCR_LAST_MODIFIED + "] DESC ";
					// Query query = session.getWorkspace().getQueryManager()
					// .createQuery(queryStr, Query.JCR_SQL2);

					query.setLimit(QUERY_LIMIT);
					NodeIterator nit = query.execute().getNodes();
					if (nit.hasNext())
						elements.addAll(JcrUtils.nodeIteratorToList(nit));
				}

				if (table.isDisposed())
					return Status.OK_STATUS;

				table.getDisplay().asyncExec(new Runnable() {
					public void run() {
						Node[] input = null;
						if (!elements.isEmpty())
							input = elements.toArray(new Node[0]);
						lazyCp.setElements(input);
					}
				});
			} catch (Exception e) {
				return new Status(IStatus.ERROR, PeopleRapPlugin.PLUGIN_ID, "Unable to refresh last modification list",
						e);
			}
			return Status.OK_STATUS;
		}
	}

	// PROVIDERS
	protected class PathLP extends ColumnLabelProvider {
		private static final long serialVersionUID = -8179051587196328000L;

		@Override
		public String getText(Object element) {
			try {
				Node node = (Node) element;
				return node.getPath();
			} catch (RepositoryException e) {
				throw new PeopleException("Unable to retrieve name " + "for " + element, e);
			}
		}
	}

	protected class ModifyLP extends ColumnLabelProvider {
		private static final long serialVersionUID = -4022534826825314784L;

		@Override
		public String getText(Object element) {
			try {
				Node currNode = (Node) element;
				String modifBy = ConnectJcrUtils.get(currNode, Property.JCR_LAST_MODIFIED_BY);
				Calendar modifOn = currNode.getProperty(Property.JCR_LAST_MODIFIED).getDate();
				return modifBy + " - " + dateFormat.format(modifOn.getTime());
			} catch (Exception e) {
				throw new PeopleException("Unable to retrieve and " + "format last modif info for " + element, e);
			}
		}
	}

	public void setPeopleWorkbenchService(PeopleWorkbenchService peopleWorkbenchService) {
		this.peopleWorkbenchService = peopleWorkbenchService;
	}
}