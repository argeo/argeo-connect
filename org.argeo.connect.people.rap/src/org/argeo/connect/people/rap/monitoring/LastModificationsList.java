package org.argeo.connect.people.rap.monitoring;

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

import org.argeo.ArgeoMonitor;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.rap.PeopleRapPlugin;
import org.argeo.connect.people.rap.PeopleWorkbenchService;
import org.argeo.connect.people.rap.editors.utils.AbstractPeopleBasicEditor;
import org.argeo.connect.people.rap.listeners.PeopleJcrViewerDClickListener;
import org.argeo.connect.people.rap.providers.SimpleLazyContentProvider;
import org.argeo.connect.people.rap.utils.Refreshable;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.eclipse.ui.EclipseArgeoMonitor;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.eclipse.ui.jcr.lists.SimpleJcrNodeLabelProvider;
import org.argeo.eclipse.ui.utils.ViewerUtils;
import org.argeo.jcr.JcrUtils;
import org.argeo.security.ui.PrivilegedJob;
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
public class LastModificationsList extends AbstractPeopleBasicEditor implements
		Refreshable {

	/* DEPENDENCY INJECTION */
	private PeopleWorkbenchService peopleWorkbenchService;

	// This page widget
	private TableViewer tableViewer;
	private SimpleLazyContentProvider lazyCp;

	// Utils
	final static int QUERY_LIMIT = 100;
	protected DateFormat dateFormat = new SimpleDateFormat(
			"dd/MM/yyyy 'at' HH:mm");

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		setPartName("Last modif. list");
		setTitleToolTip("Display the list of the most recent "
				+ "modifications done on the system");
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(PeopleUiUtils.noSpaceGridLayout());
		// The table itself
		tableViewer = createTableViewer(parent, SWT.READ_ONLY | SWT.VIRTUAL);
		tableViewer.addDoubleClickListener(new PeopleJcrViewerDClickListener(
				peopleWorkbenchService));
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
		column = ViewerUtils.createTableViewerColumn(viewer, "Title", SWT.NONE,
				250);
		column.setLabelProvider(new SimpleJcrNodeLabelProvider(
				Property.JCR_TITLE));

		column = ViewerUtils.createTableViewerColumn(viewer,
				"Last modif by ... on ...", SWT.NONE, 200);
		column.setLabelProvider(new ModifyLP());

		column = ViewerUtils.createTableViewerColumn(viewer, "Path", SWT.NONE,
				350);
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
				ArgeoMonitor monitor = new EclipseArgeoMonitor(progressMonitor);
				if (monitor != null && !monitor.isCanceled()) {
					monitor.beginTask("Retrieving last modified nodes", -1);
					Query query = session
							.getWorkspace()
							.getQueryManager()
							.createQuery(
									"SELECT * FROM ["
											+ NodeType.MIX_LAST_MODIFIED
											+ "] ORDER BY ["
											+ Property.JCR_LAST_MODIFIED
											+ "] DESC ", Query.JCR_SQL2);
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
				return new Status(IStatus.ERROR, PeopleRapPlugin.PLUGIN_ID,
						"Unable to refresh last modification list", e);
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
				throw new PeopleException("Unable to retrieve name " + "for "
						+ element, e);
			}
		}
	}

	protected class ModifyLP extends ColumnLabelProvider {
		private static final long serialVersionUID = -4022534826825314784L;

		@Override
		public String getText(Object element) {
			try {
				Node currNode = (Node) element;
				String modifBy = CommonsJcrUtils.get(currNode,
						Property.JCR_LAST_MODIFIED_BY);
				Calendar modifOn = currNode.getProperty(
						Property.JCR_LAST_MODIFIED).getDate();
				return modifBy + " - " + dateFormat.format(modifOn.getTime());
			} catch (Exception e) {
				throw new PeopleException("Unable to retrieve and "
						+ "format last modif info for " + element, e);
			}
		}
	}

	public void setPeopleWorkbenchService(
			PeopleWorkbenchService peopleWorkbenchService) {
		this.peopleWorkbenchService = peopleWorkbenchService;
	}
}