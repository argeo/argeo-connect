package org.argeo.people.web.pages;

import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryManager;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.DynamicOperand;
import javax.jcr.query.qom.Ordering;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelConstants;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;
import javax.jcr.query.qom.StaticOperand;

import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.ConnectTypes;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.jcr.JcrUtils;
import org.argeo.people.PeopleException;
import org.argeo.people.PeopleService;
import org.argeo.people.web.providers.SearchEntitiesLP;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

/** Show all entities that have a given tag-like property */
public class TagLikeInstancePage implements CmsUiProvider {

	/* DEPENDENCY INJECTION */
	private ResourcesService resourcesService;
	private PeopleService peopleService;
	private Map<String, String> iconPathes;
	private String propertyName;

	@Override
	public Control createUi(Composite parent, Node context) {
		Composite body = new Composite(parent, SWT.NO_FOCUS);
		body.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		populateBodyPanel(body, context);
		parent.layout();
		return body;
	}

	public void populateBodyPanel(Composite parent, final Node context) {
		parent.setLayout(new GridLayout());
		Label title = new Label(parent, SWT.WRAP);
		CmsUtils.markup(title);
		StringBuilder builder = new StringBuilder();
		builder.append("<b><big> ");
		builder.append(ConnectJcrUtils.get(context, Property.JCR_TITLE));
		builder.append("</big></b>");
		title.setText(builder.toString());

		final Text entityFilterTxt = new Text(parent, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH | SWT.ICON_CANCEL);
		entityFilterTxt.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		// entityFilterTxt.setMessage(PeopleMsg.searchEntities.lead());

		Composite tableComposite = new Composite(parent, SWT.NONE);
		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		final TableViewer entityViewer = createListPart(tableComposite);

		entityFilterTxt.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 1L;

			public void modifyText(ModifyEvent event) {
				refreshFilteredList(entityViewer, entityFilterTxt.getText(), context);
			}
		});
		refreshFilteredList(entityViewer, entityFilterTxt.getText(), context);
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
		ILabelProvider labelProvider = new SearchEntitiesLP(resourcesService, peopleService, table.getDisplay(),
				iconPathes);
		v.setLabelProvider(labelProvider);
		v.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				Object firstObj = ((IStructuredSelection) event.getSelection()).getFirstElement();
				String path = ConnectJcrUtils.getPath((Node) firstObj);
				CmsUtils.getCmsView().navigateTo(path);
			}
		});
		return v;
	}

	protected void refreshFilteredList(TableViewer entityViewer, String filter, Node context) {
		try {
			Session session = context.getSession();
			QueryManager queryManager = session.getWorkspace().getQueryManager();
			QueryObjectModelFactory factory = queryManager.getQOMFactory();
			String path = "/"; // peopleService.getDefaultBasePath();
			String value = context.getProperty(Property.JCR_TITLE).getString();

			Selector source = factory.selector(ConnectTypes.CONNECT_ENTITY, ConnectTypes.CONNECT_ENTITY);

			// Reduce to the business sub tree
			Constraint descCst = factory.descendantNode(source.getSelectorName(), path);

			// only entities with correct tag like value
			StaticOperand so = factory.literal(session.getValueFactory().createValue(value));
			DynamicOperand dyo = factory.propertyValue(source.getSelectorName(), propertyName);
			Constraint tagCst = factory.comparison(dyo, QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO, so);

			Constraint defaultC = factory.and(tagCst, descCst);

			// Apply filtering
			for (String token : filter.trim().split(" ")) {
				StaticOperand so2 = factory.literal(session.getValueFactory().createValue("*" + token + "*"));
				Constraint currC = factory.fullTextSearch(source.getSelectorName(), null, so2);
				defaultC = factory.and(defaultC, currC);
			}

			Ordering order = factory.ascending(factory.propertyValue(source.getSelectorName(), Property.JCR_TITLE));
			QueryObjectModel query = factory.createQuery(source, defaultC, new Ordering[] { order }, null);
			entityViewer.setInput(JcrUtils.nodeIteratorToList(query.execute().getNodes()));
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to list entities for context " + context + " and filter " + filter, e);
		}
	}

	private class BasicContentProvider implements IStructuredContentProvider {
		private static final long serialVersionUID = 1L;
		// we keep a cache of the Nodes in the content provider to be able to
		// manage long request
		private List<Node> nodes;

		public void dispose() {
		}

		// We expect a list of nodes as a new input
		@SuppressWarnings("unchecked")
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			nodes = (List<Node>) newInput;
		}

		public Object[] getElements(Object arg0) {
			return nodes.toArray();
		}
	}

	/* DEPENDENCY INJECTION */
	public void setPeopleService(PeopleService peopleService) {
		this.peopleService = peopleService;
	}

	public void setResourcesService(ResourcesService resourcesService) {
		this.resourcesService = resourcesService;
	}

	public void setIconPathes(Map<String, String> iconPathes) {
		this.iconPathes = iconPathes;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}
}
