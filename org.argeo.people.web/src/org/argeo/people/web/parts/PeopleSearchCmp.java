package org.argeo.people.web.parts;

import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryManager;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.Ordering;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;
import javax.jcr.query.qom.StaticOperand;

import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.ConnectTypes;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;
import org.argeo.people.PeopleException;
import org.argeo.people.PeopleService;
import org.argeo.people.web.providers.SearchEntitiesLP;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

/** Generic composite to display a filtered list of entities */
public class PeopleSearchCmp extends Composite {
	private static final long serialVersionUID = 8520892609661053149L;

	private final PeopleService peopleService;
	private final ResourcesService resourceService;
	private final Map<String, String> iconPaths;

	// We search only in this node subtree.
	private Node context;

	// State full embedded controls
	private TableViewer viewer;
	private Text entityFilterTxt;

	public PeopleSearchCmp(Composite parent, int style, ResourcesService resourceService, PeopleService peopleService,
			Map<String, String> iconPaths) {
		super(parent, style);
		this.peopleService = peopleService;
		this.resourceService = resourceService;
		this.iconPaths = iconPaths;
	}

	public TableViewer getViewer() {
		return viewer;
	}

	public void populate(Node context, boolean doDefaultQuery) {
		this.context = context;
		Composite parent = this;
		parent.setLayout(new GridLayout());
		entityFilterTxt = new Text(parent, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH | SWT.ICON_CANCEL);
		entityFilterTxt.setLayoutData(EclipseUiUtils.fillWidth());
		Composite tableComposite = new Composite(parent, SWT.NONE);
		tableComposite.setLayoutData(EclipseUiUtils.fillAll());
		viewer = createListPart(tableComposite);

		entityFilterTxt.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 1L;

			public void modifyText(ModifyEvent event) {
				refresh();
			}
		});

		if (doDefaultQuery)
			refresh();
	}

	protected TableViewer createListPart(Composite parent) {
		final TableViewer v = new TableViewer(parent);
		TableColumn singleColumn = new TableColumn(v.getTable(), SWT.V_SCROLL);
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		tableColumnLayout.setColumnData(singleColumn, new ColumnWeightData(85));
		parent.setLayout(tableColumnLayout);

		// Corresponding table & style
		Table table = v.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(false);
		CmsUtils.markup(table);
		CmsUtils.setItemHeight(table, 23);
		v.setContentProvider(new BasicContentProvider());
		ILabelProvider labelProvider = new SearchEntitiesLP(resourceService, peopleService, table.getDisplay(),
				iconPaths);
		v.setLabelProvider(labelProvider);
		return v;
	}

	protected void refresh() {
		String filter = entityFilterTxt.getText();
		try {
			Session session = context.getSession();
			QueryManager queryManager = session.getWorkspace().getQueryManager();
			QueryObjectModelFactory factory = queryManager.getQOMFactory();
			String path = context.getPath();

			Selector source = factory.selector(ConnectTypes.CONNECT_ENTITY, ConnectTypes.CONNECT_ENTITY);

			Constraint defaultC = factory.descendantNode(source.getSelectorName(), path);

			if (EclipseUiUtils.notEmpty(filter)) {
				String[] strs = filter.trim().split(" ");
				for (String token : strs) {
					StaticOperand so = factory.literal(session.getValueFactory().createValue("*" + token + "*"));
					Constraint currC = factory.fullTextSearch(source.getSelectorName(), null, so);
					if (defaultC == null)
						defaultC = currC;
					else
						defaultC = factory.and(defaultC, currC);
				}
			}

			Ordering order = factory.ascending(factory.propertyValue(source.getSelectorName(), Property.JCR_TITLE));
			QueryObjectModel query = factory.createQuery(source, defaultC, new Ordering[] { order }, null);

			// TODO use a virtual table
			viewer.setInput(JcrUtils.nodeIteratorToList(query.execute().getNodes()));

		} catch (RepositoryException e) {
			throw new PeopleException("Unable to list entities under " + context + " with filter: " + filter, e);
		}
	}

	private class BasicContentProvider implements IStructuredContentProvider {
		private static final long serialVersionUID = 1L;

		private List<Node> nodes;

		// Expects a List<Node> as a new input
		@SuppressWarnings("unchecked")
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			nodes = (List<Node>) newInput;
		}

		public Object[] getElements(Object arg0) {
			return nodes.toArray();
		}

		public void dispose() {
		}
	}
}
