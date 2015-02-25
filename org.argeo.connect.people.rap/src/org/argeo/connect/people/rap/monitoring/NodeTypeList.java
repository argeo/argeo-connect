package org.argeo.connect.people.rap.monitoring;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Query;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.rap.utils.Refreshable;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.eclipse.ui.utils.ViewerUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

/**
 * Quick and dirty editor to monitor the number of instances for each known node
 * type
 * 
 * TODO Work on this to enrich the generic monitoring perspective.
 */
public class NodeTypeList extends EditorPart implements Refreshable {

	/* DEPENDENCY INJECTION */
	private Session session;

	protected NumberFormat numberFormat;
	protected DateFormat dateFormat = new SimpleDateFormat(
			"dd/MM/yyyy 'at' HH:mm");

	// Business Object
	private String[][] elements;

	// This page widget
	private TableViewer tableViewer;
	private NameLP nameLP = new NameLP();
	private CountLP countLP = new CountLP();

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);

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

		MyLazyContentProvider lazyContentProvider = new MyLazyContentProvider(
				viewer);
		viewer.setContentProvider(lazyContentProvider);
		return viewer;
	}

	private final static String NODE_DEF_PARENT_PATH = "/jcr:system/jcr:nodeTypes";

	@Override
	public void forceRefresh(Object object) {
		// TODO put this in a job
		try {
			if (session.nodeExists(NODE_DEF_PARENT_PATH)) {
				Node parent = session.getNode(NODE_DEF_PARENT_PATH);
				NodeIterator nit = parent.getNodes();

				List<String[]> infos = new ArrayList<String[]>();
				while (nit.hasNext()) {
					Node currDef = nit.nextNode();
					if (currDef.isNodeType(NodeType.NT_NODE_TYPE)) {
						String[] vals = new String[2];
						vals[0] = nameLP.getText(currDef);
						vals[1] = countLP.getText(currDef);
						infos.add(vals);
					}
				}

				elements = infos.toArray(new String[1][2]);
				tableViewer.setInput(elements);
				// we must explicitly set the items count
				tableViewer.setItemCount(elements.length);
				tableViewer.refresh();
			}
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to refresh node type list table",
					e);
		}
	}

	@Override
	public void dispose() {
		JcrUtils.logoutQuietly(session);
		super.dispose();
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

	private class MyLazyContentProvider implements ILazyContentProvider {
		private static final long serialVersionUID = 1L;
		private TableViewer viewer;

		public MyLazyContentProvider(TableViewer viewer) {
			this.viewer = viewer;
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// IMPORTANT: don't forget this: an exception will be thrown if a
			// selected object is not part of the results anymore.
			viewer.setSelection(null);
			elements = (String[][]) newInput;
		}

		public void updateElement(int index) {
			viewer.replace(elements[index], index);
		}
	}

	// PROVIDERS
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
				Query query = session
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

	protected Session getSession() {
		return session;
	}

	protected TableViewer getTableViewer() {
		return tableViewer;
	}

	// COMPULSORY UNUSED METHODS
	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public void setFocus() {
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}

	/* DEPENDENCY INJECTION */
	public void setRepository(Repository repository) {
		session = CommonsJcrUtils.login(repository);
	}
}