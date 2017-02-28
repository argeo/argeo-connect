package org.argeo.people.workbench.rap.monitoring;

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

import org.argeo.connect.workbench.Refreshable;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.eclipse.ui.utils.ViewerUtils;
import org.argeo.jcr.JcrUtils;
import org.argeo.people.PeopleException;
import org.argeo.people.workbench.rap.editors.util.AbstractPeopleBasicEditor;
import org.argeo.people.workbench.rap.providers.SimpleLazyContentProvider;
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
 * WORK IN PROGRESS: Generic table to manage display of CSV log file of a given
 * type. Also enable the launch of a new long running job to generate an updated
 * file.
 */
public class AbstractCsvLogTableViewer extends AbstractPeopleBasicEditor
		implements Refreshable {

	/* DEPENDENCY INJECTION */
	private Session session;

	protected NumberFormat numberFormat;
	protected DateFormat dateFormat = new SimpleDateFormat(
			"dd/MM/yyyy 'at' HH:mm");

	// Business Object
	// private String[][] elements;

	// This page widget
	private TableViewer tableViewer;
	private SimpleLazyContentProvider lazyCp;
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
		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());
		// The table itself
		Composite tableCmp = new Composite(parent, SWT.NO_FOCUS);
		tableViewer = createTableViewer(tableCmp, SWT.READ_ONLY);
		tableCmp.setLayoutData(EclipseUiUtils.fillAll());
		// tableViewer.addDoubleClickListener(new PeopleJcrViewerDClickListener(
		// tagInstanceType, peopleWorkbenchService));
	}

	private TableViewer createTableViewer(final Composite parent, int tableStyle) {
		parent.setLayout(new GridLayout());

		int swtStyle = tableStyle | SWT.VIRTUAL;
		Table table = new Table(parent, swtStyle);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.setLayoutData(EclipseUiUtils.fillAll());

		TableViewer viewer = new TableViewer(table);
		TableViewerColumn column;
		column = ViewerUtils.createTableViewerColumn(viewer, "Name", SWT.NONE,
				300);
		column.setLabelProvider(new SimpleLP(0));
		column = ViewerUtils.createTableViewerColumn(viewer, "Number",
				SWT.RIGHT, 150);
		column.setLabelProvider(new SimpleLP(1));

		lazyCp = new SimpleLazyContentProvider(viewer);
		viewer.setContentProvider(lazyCp);
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
				if (!nit.hasNext()) {
					// elements = null;
					lazyCp.setElements(null);
				} else {
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
					lazyCp.setElements(infos.toArray(new String[1][2]));
				}
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

	protected TableViewer getTableViewer() {
		return tableViewer;
	}
}