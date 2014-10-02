package org.argeo.connect.people.web.pages;

import java.util.List;

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

import org.argeo.ArgeoException;
import org.argeo.cms.CmsSession;
import org.argeo.cms.CmsUiProvider;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.web.providers.SingleColListLabelProvider;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

/** Simple page to manage RSS channels and feeds */
public class PeopleSearchPage implements CmsUiProvider {

	/* DEPENDENCY INJECTION */
	private PeopleService peopleService;

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
		final Text entityFilterTxt = new Text(parent, SWT.BORDER | SWT.SEARCH
				| SWT.ICON_SEARCH | SWT.ICON_CANCEL);
		entityFilterTxt.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true,
				false));
		entityFilterTxt.setMessage("Search entities");

		Composite tableComposite = new Composite(parent, SWT.NONE);
		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true));
		final TableViewer entityViewer = createListPart(tableComposite,
				new SingleColListLabelProvider(peopleService));

		entityFilterTxt.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 1L;

			public void modifyText(ModifyEvent event) {
				// might be better to use an asynchronous Refresh();
				refreshFilteredList(entityViewer, entityFilterTxt.getText(),
						context);
			}
		});
		refreshFilteredList(entityViewer, entityFilterTxt.getText(), context);
	}

	protected TableViewer createListPart(Composite parent,
			ILabelProvider labelProvider) {
		final TableViewer v = new TableViewer(parent);
		v.setLabelProvider(labelProvider);
		TableColumn singleColumn = new TableColumn(v.getTable(), SWT.V_SCROLL);
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		tableColumnLayout.setColumnData(singleColumn, new ColumnWeightData(85));
		parent.setLayout(tableColumnLayout);

		// Corresponding table & style
		Table table = v.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(false);
		table.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
		table.setData(RWT.CUSTOM_ITEM_HEIGHT, Integer.valueOf(25));
		v.setContentProvider(new BasicContentProvider());

		v.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				CmsSession cmsSession = (CmsSession) v.getTable().getDisplay()
						.getData(CmsSession.KEY);
				Node node = (Node) ((IStructuredSelection) event.getSelection())
						.getFirstElement();
				try {
					cmsSession.navigateTo(node.getPath());
				} catch (RepositoryException e) {
					throw new ArgeoException("unable to get path for node "
							+ node + " in the PeopleSearchPage", e);
				}

				// DefaultHomePage.this.

			}
		});

		return v;
	}

	protected void refreshFilteredList(TableViewer entityViewer, String filter,
			Node context) {
		try {
			// Do not load all contacts when no filter is present
			// if (CommonsJcrUtils.isEmptyString(filter)) {
			// entityViewer.setInput(null);
			// return;
			// }

			Session session = context.getSession();
			QueryManager queryManager = session.getWorkspace()
					.getQueryManager();
			QueryObjectModelFactory factory = queryManager.getQOMFactory();
			String path = context.getPath();

			Selector source = factory.selector(PeopleTypes.PEOPLE_ENTITY,
					PeopleTypes.PEOPLE_ENTITY);

			// StaticOperand so =
			// factory.literal(session.getValueFactory()
			// .createValue("*"));
			Constraint defaultC = factory.descendantNode(
					source.getSelectorName(), path);

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

			Ordering order = factory.ascending(factory.propertyValue(
					source.getSelectorName(), Property.JCR_TITLE));
			QueryObjectModel query = factory.createQuery(source, defaultC,
					new Ordering[] { order }, null);
			// query.setLimit(PeopleConstants.QUERY_DEFAULT_LIMIT);
			entityViewer.setInput(JcrUtils.nodeIteratorToList(query.execute()
					.getNodes()));
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to list entities for context "
					+ context + " and filter " + filter, e);
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
}