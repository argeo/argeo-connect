package org.argeo.connect.people.rap.monitoring;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Query;

import org.argeo.ArgeoMonitor;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.rap.editors.utils.AbstractPeopleBasicEditor;
import org.argeo.connect.people.rap.providers.MyLazyContentProvider;
import org.argeo.connect.people.rap.utils.Refreshable;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.eclipse.ui.EclipseArgeoMonitor;
import org.argeo.eclipse.ui.utils.ViewerUtils;
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
 * Quick and dirty editor to monitor the number of instances for each known node
 * type
 * 
 * TODO Work on this to enrich the generic monitoring perspective.
 */
public class NodeTypeList extends AbstractPeopleBasicEditor implements
		Refreshable {

	/* DEPENDENCY INJECTION */

	protected NumberFormat numberFormat;
	protected DateFormat dateFormat = new SimpleDateFormat(
			"dd/MM/yyyy 'at' HH:mm");

	// Business Object
	// private String[][] elements;

	// This page widget
	private TableViewer tableViewer;
	private MyLazyContentProvider lazyCp;
	private NameLP nameLP = new NameLP();
	private CountLP countLP = new CountLP();

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);

		setPartName("Node types");
		setTitleToolTip("Display the list of used node types and corresponding occurence number");

		numberFormat = DecimalFormat.getInstance();
		((DecimalFormat) numberFormat).applyPattern("#,##0");
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(PeopleUiUtils.noSpaceGridLayout());
		// The table itself
		Composite tableCmp = new Composite(parent, SWT.NO_FOCUS);
		tableViewer = createTableViewer(tableCmp, SWT.READ_ONLY);
		tableCmp.setLayoutData(PeopleUiUtils.fillGridData());
		// tableViewer.addDoubleClickListener(new PeopleJcrViewerDClickListener(
		// tagInstanceType, peopleWorkbenchService));
	}

	private TableViewer createTableViewer(final Composite parent, int tableStyle) {
		parent.setLayout(new GridLayout());

		int swtStyle = tableStyle | SWT.VIRTUAL;
		Table table = new Table(parent, swtStyle);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.setLayoutData(PeopleUiUtils.fillGridData());

		TableViewer viewer = new TableViewer(table);
		TableViewerColumn column;
		column = ViewerUtils.createTableViewerColumn(viewer, "Name", SWT.NONE,
				300);
		column.setLabelProvider(new SimpleLP(0));
		column = ViewerUtils.createTableViewerColumn(viewer, "Number",
				SWT.RIGHT, 150);
		column.setLabelProvider(new SimpleLP(1));

		lazyCp = new MyLazyContentProvider(viewer);
		viewer.setContentProvider(lazyCp);
		return viewer;
	}

	private final static String NODE_DEF_PARENT_PATH = "/jcr:system/jcr:nodeTypes";

	@Override
	public void forceRefresh(Object object) {
		new QueryJob().schedule();
	}

	/** Privileged job that performs the query asynchronously */
	private class QueryJob extends PrivilegedJob {
		private Table table;

		public QueryJob() {
			super("AsynchronousQuery");
			tableViewer = getTableViewer();
			table = getTableViewer().getTable();
		}

		protected IStatus doRun(IProgressMonitor progressMonitor) {
			ArgeoMonitor monitor = new EclipseArgeoMonitor(progressMonitor);
			if (monitor != null && !monitor.isCanceled())
				monitor.beginTask("Querying the repository", -1);

			Session session = getSession();
			try {
				if (session.nodeExists(NODE_DEF_PARENT_PATH)) {
					Node parent = session.getNode(NODE_DEF_PARENT_PATH);
					NodeIterator nit = parent.getNodes();
					final List<String[]> infos = new ArrayList<String[]>();
					while (nit.hasNext()) {
						Node currDef = nit.nextNode();
						if (currDef.isNodeType(NodeType.NT_NODE_TYPE)) {
							String[] vals = new String[2];
							vals[0] = nameLP.getText(currDef);
							vals[1] = countLP.getText(currDef);
							infos.add(vals);
						}
					}

					if (table.isDisposed())
						return Status.OK_STATUS;

					table.getDisplay().asyncExec(new Runnable() {
						public void run() {
							Object[] input = null;
							if (!infos.isEmpty())
								input = infos.toArray(new String[1][2]);
							lazyCp.setElements(input);
						}
					});
				}
			} catch (RepositoryException e) {
				throw new PeopleException(
						"Unable to refresh node type list table", e);
			}
			return Status.OK_STATUS;
		}
	}

	// PROVIDERS
	private class SimpleLP extends ColumnLabelProvider {
		private static final long serialVersionUID = -4896540744722267118L;
		private final int colIndex;

		public SimpleLP(int colIndex) {
			this.colIndex = colIndex;
		}

		@Override
		public String getText(Object element) {
			return ((String[]) element)[colIndex];
		}
	}

	protected class NameLP extends ColumnLabelProvider {
		private static final long serialVersionUID = -8179051587196328000L;

		@Override
		public String getText(Object element) {
			try {
				return ((Node) element).getName();
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
			try {
				String currType = ((Node) element).getName();
				Query query = getSession()
						.getWorkspace()
						.getQueryManager()
						.createQuery(
								"select * from [" + currType + "] as instances",
								Query.JCR_SQL2);
				NodeIterator nit = query.execute().getNodes();
				return numberFormat.format(nit.getSize());
			} catch (RepositoryException re) {
				throw new PeopleException("Unable to retrieve number of "
						+ "instances for type " + element, re);
			}
		}
	}

	protected TableViewer getTableViewer() {
		return tableViewer;
	}
}