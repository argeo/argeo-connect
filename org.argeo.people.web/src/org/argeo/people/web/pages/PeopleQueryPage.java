package org.argeo.people.web.pages;

import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.cms.ui.util.CmsUiUtils;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

/** Generic page to display a list of filtered nodes, base on their type */
public class PeopleQueryPage implements CmsUiProvider {

	private PeopleService peopleService;
	private ResourcesService resourcesService;
	private Map<String, String> iconPathes;

	public PeopleQueryPage(PeopleService peopleService, ResourcesService resourcesService,
			Map<String, String> iconPathes) {
		this.peopleService = peopleService;
		this.resourcesService = resourcesService;
		this.iconPathes = iconPathes;
	}

	@Override
	public Control createUi(Composite parent, Node context) {
		Composite body = new Composite(parent, SWT.NO_FOCUS);
		body.setLayoutData(EclipseUiUtils.fillAll());
		populateBodyPanel(body, context);
		parent.layout();
		return body;
	}

	public void populateBodyPanel(Composite parent, final Node context) {
		parent.setLayout(new GridLayout());
		final Text entityFilterTxt = new Text(parent, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH | SWT.ICON_CANCEL);
		entityFilterTxt.setLayoutData(EclipseUiUtils.fillWidth());
		// entityFilterTxt.setMessage(PeopleMsg.searchEntities.lead());

		Composite tableComposite = new Composite(parent, SWT.NONE);
		tableComposite.setLayoutData(EclipseUiUtils.fillAll());
		final TableViewer entityViewer = createListPart(tableComposite);

		entityFilterTxt.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 1L;

			public void modifyText(ModifyEvent event) {
				// might be better to use an asynchronous Refresh();
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
		CmsUiUtils.markup(table);
		CmsUiUtils.setItemHeight(table, 23);
		v.setContentProvider(new BasicContentProvider());
		ILabelProvider labelProvider = new SearchEntitiesLP(resourcesService, peopleService, table.getDisplay(),
				iconPathes);
		v.setLabelProvider(labelProvider);
		v.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				Object firstObj = ((IStructuredSelection) event.getSelection()).getFirstElement();
				String path = ConnectJcrUtils.getPath((Node) firstObj);
				CmsUiUtils.getCmsView().navigateTo(path);
			}
		});
		return v;
	}

	protected void refreshFilteredList(TableViewer entityViewer, String filter, Node context) {
		try {
			// Do not load all contacts when no filter is present
			// if (ConnectJcrUtils.isEmptyString(filter)) {
			// entityViewer.setInput(null);
			// return;
			// }

			Session session = context.getSession();
			QueryManager queryManager = session.getWorkspace().getQueryManager();

			String statement = context.getProperty(Property.JCR_STATEMENT).getString();
			String selectorName = statement.substring(statement.indexOf('['), statement.indexOf(']') + 1);

			String language = context.getProperty(Property.JCR_LANGUAGE).getString();
			if (!Query.JCR_SQL2.equals(language))
				throw new PeopleException("Unknown language type " + language);
			if (EclipseUiUtils.notEmpty(filter))
				statement += " WHERE CONTAINS(" + selectorName + ".*, '*" + filter + "*')";
			Query query = queryManager.createQuery(statement, language);

			query.getBindVariableNames();
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
}
