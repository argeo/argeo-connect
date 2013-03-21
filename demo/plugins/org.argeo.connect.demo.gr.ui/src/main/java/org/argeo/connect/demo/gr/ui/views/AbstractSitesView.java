package org.argeo.connect.demo.gr.ui.views;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.argeo.connect.demo.gr.GrBackend;
import org.argeo.connect.demo.gr.GrException;
import org.argeo.connect.demo.gr.GrNames;
import org.argeo.connect.demo.gr.GrTypes;
import org.argeo.connect.demo.gr.ui.GrMessages;
import org.argeo.connect.demo.gr.ui.exports.CalcExtractHeaderLabelProvider;
import org.argeo.connect.demo.gr.ui.exports.ICalcExtractProvider;
import org.argeo.connect.demo.gr.ui.providers.GrJcrPropertyValueProvider;
import org.argeo.connect.demo.gr.ui.providers.IJcrPropertyLabelProvider;
import org.argeo.connect.demo.gr.ui.utils.GrDoubleClickListener;
import org.argeo.connect.demo.gr.ui.utils.NodeViewerComparator;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.ViewPart;

/** Filterable list of all sites */
public abstract class AbstractSitesView extends ViewPart implements
		ICalcExtractProvider {
	private final static Log log = LogFactory.getLog(AbstractSitesView.class);

	public final String ID;

	/* DEPENDENCY INJECTION */
	protected GrBackend grBackend;
	// Relevant attribute for exports
	// protected List<String> calcExtractAttributes;

	// This page widgets
	protected TableViewer tableViewer;
	protected NodeViewerComparator comparator;

	// To keep mapping between column index and corresponding JCR properties
	protected List<String> propertiesList;
	protected List<Integer> propertyTypesList;

	// Business objects
	private Session session;

	protected AbstractSitesView(String ID) {
		super();
		this.ID = ID;
	}

	// LIFE CYCLE
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		Repository repository = grBackend.getRepository();
		try {
			session = repository.login();
		} catch (RepositoryException e) {
			throw new GrException("Unable to create new session"
					+ " to use with current view", e);
		}
	}

	@Override
	public void dispose() {
		JcrUtils.logoutQuietly(session);
		super.dispose();
	}

	public void createPartControl(Composite parent) {
		GridData gd;

		// MainLayout
		parent.setLayout(new GridLayout(1, false));
		// Header
		Section headerSection = new Section(parent, Section.TITLE_BAR
				| Section.TWISTIE); // Section.DESCRIPTION
		// headerSection.setDescription("une description");
		headerSection.setText(GrMessages.get().siteListView_displayFilterLbl);
		headerSection.setExpanded(false);
		Composite body = new Composite(headerSection, SWT.NONE);
		createHeaderPart(body);
		headerSection.setClient(body);

		// Table
		Composite table = new Composite(parent, SWT.NONE);
		gd = new GridData(GridData.FILL_BOTH);
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		table.setLayoutData(gd);
		table.setLayout(new FillLayout());
		createTablePart(table);

		// set data
		refreshFilteredList();
	}

	public abstract void createHeaderPart(Composite body);

	protected abstract void refreshFilteredList();

	protected List<Node> getSitesWithWhereClause(String whereClause) {
		StringBuffer strBuf = new StringBuffer();
		strBuf.append("Select * FROM [" + GrTypes.GR_WATER_SITE + "] AS sites ");
		if (whereClause != null && !"".equals(whereClause.trim())) {

			strBuf.append(" WHERE ");
			strBuf.append(whereClause);
		}
		strBuf.append(" ORDER BY ");
		strBuf.append("sites.[" + GrNames.GR_UUID + "] ");
		strBuf.append("DESC");
		try {
			if (log.isTraceEnabled())
				log.trace("Get sites query : " + strBuf.toString());
			Query query = session.getWorkspace().getQueryManager()
					.createQuery(strBuf.toString(), Query.JCR_SQL2);
			NodeIterator ni = query.execute().getNodes();
			List<Node> sites = new ArrayList<Node>();
			if (ni.hasNext()) {
				while (ni.hasNext())
					sites.add(ni.nextNode());
			}
			return sites;
		} catch (RepositoryException e) {
			throw new ArgeoException(
					"unexpected error while retrieving list of sites", e);
		}
	}

	public void createTablePart(Composite parent) {
		TableViewer tableViewer = createTableViewer(parent);
		tableViewer.setContentProvider(new ViewContentProvider());
		tableViewer.addDoubleClickListener(new GrDoubleClickListener());
		// fill the table
	}

	protected abstract TableViewer createTableViewer(Composite parent);

	// ADD SPREADSHEET GENERATION ABILITY
	@SuppressWarnings("unchecked")
	@Override
	public List<Node> getNodeList(String extractId) {
		return (List<Node>) tableViewer.getInput();
	}

	@Override
	public List<String> getHeaderList(String extractId) {
		return propertiesList;
	}

	@Override
	public List<String> getHeaderLblList(String extractId) {
		List<String> headerLbls = new ArrayList<String>();
		// TODO : add the ability to have different fields in the result table
		// and in the calc export
		for (String header : propertiesList) {
			headerLbls.add(CalcExtractHeaderLabelProvider
					.getLabelFromPropertyName(header));
		}
		return headerLbls;
	}

	// // Helper to simplify mapping
	// protected String getLabelWithPropName(String propertyName) {
	// String prop;
	// if (propertyName.startsWith("gr:"))
	// prop = propertyName.substring("gr:".length());
	// else
	// prop = propertyName;
	// return prop;
	// }

	protected final static GrJcrPropertyValueProvider defaultJcrPropLblProvider = new GrJcrPropertyValueProvider();

	@Override
	public IJcrPropertyLabelProvider getLabelProvider(String extractId) {
		return defaultJcrPropLblProvider;
	}

	// View Specific inner class
	// Providers
	protected class ViewContentProvider implements IStructuredContentProvider {

		public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
		}

		public void dispose() {
		}

		@SuppressWarnings("unchecked")
		public Object[] getElements(Object obj) {
			return ((List<Node>) obj).toArray();
		}
	}

	public void setFocus() {
	}

	/** Reset the PIC list and refresh the view */
	public void refresh() {
		refreshFilteredList();
		// if (tableViewer != null)
		// refresh(getSitesWithWhereClause(null));
	}

	/** Refresh the view with the given nodes list */
	public void refresh(List<Node> nodes) {
		if (tableViewer != null)
			tableViewer.setInput(nodes);
	}

	protected SelectionAdapter getSelectionAdapter(final TableColumn column,
			final int index) {
		SelectionAdapter selectionAdapter = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				comparator.setColumn(index);
				int dir = tableViewer.getTable().getSortDirection();
				if (tableViewer.getTable().getSortColumn() == column) {
					dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
				} else {
					dir = SWT.DOWN;
				}
				tableViewer.getTable().setSortDirection(dir);
				tableViewer.getTable().setSortColumn(column);
				tableViewer.refresh();
			}
		};
		return selectionAdapter;
	}

	/* DEPENDENCY INJECTION */
	public void setGrBackend(GrBackend grBackend) {
		this.grBackend = grBackend;
	}

	// public void setExtractAttributes(List<String> calcExtractAttributes) {
	// this.calcExtractAttributes = calcExtractAttributes;
	// }
}