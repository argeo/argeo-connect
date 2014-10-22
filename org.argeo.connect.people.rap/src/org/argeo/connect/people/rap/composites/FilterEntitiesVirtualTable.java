package org.argeo.connect.people.rap.composites;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.Ordering;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;
import javax.jcr.query.qom.StaticOperand;

import org.argeo.ArgeoException;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.rap.PeopleRapConstants;
import org.argeo.connect.people.rap.PeopleWorkbenchService;
import org.argeo.connect.people.rap.providers.TitleIconLP;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.eclipse.ui.jcr.lists.ColumnDefinition;
import org.argeo.eclipse.ui.specific.EclipseUiSpecificUtils;
import org.argeo.eclipse.ui.utils.ViewerUtils;
import org.argeo.jcr.ArgeoNames;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

/** Almost canonical implementation of a table that display entities */
public class FilterEntitiesVirtualTable extends Composite implements ArgeoNames {
	private static final long serialVersionUID = 1262369448445021926L;

	// Context
	private Session session;
	private PeopleWorkbenchService peopleWorkbenchService;

	// UI Objects
	private TableViewer entityViewer;
	private Text filterTxt;
	private int tableStyle;

	// Defaults
	private String nodeType = PeopleTypes.PEOPLE_ENTITY;
	private List<ColumnDefinition> colDefs = new ArrayList<ColumnDefinition>();
	{
		colDefs.add(new ColumnDefinition(null, Property.JCR_TITLE,
				PropertyType.STRING, "Name", 300));
	};

	public FilterEntitiesVirtualTable(Composite parent, int style,
			Session session, PeopleWorkbenchService peopleWorkbenchService,
			String nodeType) {
		super(parent, SWT.NONE);
		this.session = session;
		this.peopleWorkbenchService = peopleWorkbenchService;
		this.tableStyle = style;
		if (CommonsJcrUtils.checkNotEmptyString(nodeType))
			this.nodeType = nodeType;
		populate();
	}

	protected void populate() {
		Composite parent = this;
		GridLayout layout = PeopleUiUtils.noSpaceGridLayout();
		layout.verticalSpacing = 5;
		this.setLayout(layout);
		createFilterPart(parent);
		createTableViewer(parent);
		EclipseUiSpecificUtils.enableToolTipSupport(entityViewer);
		refreshFilteredList();
	}

	private void createTableViewer(final Composite parent) {
		// the list
		// We must limit the size of the table otherwise the full list is loaded
		// before the layout happens
		Composite listCmp = new Composite(parent, SWT.NO_FOCUS);
		GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false);
		gd.heightHint = 460;
		listCmp.setLayoutData(gd);
		listCmp.setLayout(PeopleUiUtils.noSpaceGridLayout());

		entityViewer = new TableViewer(listCmp, SWT.VIRTUAL | tableStyle);
		Table table = entityViewer.getTable();

		table.setLayoutData(PeopleUiUtils.fillGridData());
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);

		TableViewerColumn column;
		for (ColumnDefinition colDef : colDefs) {
			column = ViewerUtils.createTableViewerColumn(entityViewer,
					colDef.getHeaderLabel(), SWT.NONE, colDef.getColumnSize());
			column.setLabelProvider(new TitleIconLP(peopleWorkbenchService,
					colDef.getPropertyName()));
		}

		entityViewer.setContentProvider(new MyLazyCP(entityViewer));
	}

	private class MyLazyCP implements ILazyContentProvider {
		private static final long serialVersionUID = 1L;
		private TableViewer viewer;
		private Object[] elements;

		public MyLazyCP(TableViewer viewer) {
			this.viewer = viewer;
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// IMPORTANT: don't forget this: an exception will be thrown if a
			// selected object is not part of the results anymore.
			viewer.setSelection(null);
			this.elements = (Object[]) newInput;
		}

		public void updateElement(int index) {
			viewer.replace(elements[index], index);
		}
	}

	public List<ColumnDefinition> getColumnsDef() {
		return colDefs;
	}

	/** Returns the User table viewer, typically to add doubleclick listener */
	public TableViewer getTableViewer() {
		return entityViewer;
	}

	/* MANAGE FILTER */
	private void createFilterPart(Composite parent) {
		// Text Area for the filter
		filterTxt = new Text(parent, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH
				| SWT.ICON_CANCEL);
		filterTxt.setMessage(PeopleRapConstants.FILTER_HELP_MSG);
		filterTxt.setLayoutData(PeopleUiUtils.horizontalFillData());
		filterTxt.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 1L;

			public void modifyText(ModifyEvent event) {
				refreshFilteredList();
			}
		});
	}

	/**
	 * Refresh the list: caller might overwrite in order to display a subset of
	 * all nodes
	 */
	protected void refreshFilteredList() {
		List<Node> nodes;
		try {
			nodes = JcrUtils.nodeIteratorToList(listFilteredElements(session,
					filterTxt.getText()));
			entityViewer.setInput(nodes.toArray());
			entityViewer.setItemCount(nodes.size());
			entityViewer.refresh();
		} catch (RepositoryException e) {
			throw new ArgeoException("Unable to list users", e);
		}
	}

	// protected void setViewerInput(Object[] elements) {
	// entityViewer.setInput(elements);
	// // we must explicitly set the items count
	// entityViewer.setItemCount(elements.length);
	// entityViewer.refresh();
	// }

	/**
	 * Build repository request : caller might overwrite in order to display a
	 * subset
	 */
	protected NodeIterator listFilteredElements(Session session, String filter)
			throws RepositoryException {
		QueryManager queryManager = session.getWorkspace().getQueryManager();
		QueryObjectModelFactory factory = queryManager.getQOMFactory();

		Selector source = factory.selector(nodeType, nodeType);

		Constraint defaultC = null;

		// Build constraints based the textArea filter content
		if (filter != null && !"".equals(filter.trim())) {
			// Parse the String
			String[] strs = filter.trim().split(" ");
			for (String token : strs) {
				StaticOperand so = factory.literal(session.getValueFactory()
						.createValue("*" + token + "*"));
				Constraint currC = factory.fullTextSearch(
						source.getSelectorName(), null, so);
				if (defaultC == null)
					defaultC = currC;
				else
					defaultC = factory.and(defaultC, currC);
			}
		}
		// Entity should normally always be a mix:title
		Ordering order = factory.ascending(factory.propertyValue(
				source.getSelectorName(), Property.JCR_TITLE));
		Ordering[] orderings = { order };
		QueryObjectModel query = factory.createQuery(source, defaultC,
				orderings, null);
		// TODO rather implement a virtual viewer
		// query.setLimit(100);
		QueryResult result = query.execute();
		return result.getNodes();
	}
}