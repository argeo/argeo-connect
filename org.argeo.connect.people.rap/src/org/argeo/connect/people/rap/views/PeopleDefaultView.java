package org.argeo.connect.people.rap.views;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Repository;
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

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.rap.PeopleImages;
import org.argeo.connect.people.rap.PeopleRapConstants;
import org.argeo.connect.people.rap.PeopleRapPlugin;
import org.argeo.connect.people.rap.PeopleWorkbenchService;
import org.argeo.connect.people.rap.commands.OpenSearchEntityEditor;
import org.argeo.connect.people.rap.listeners.PeopleJcrViewerDClickListener;
import org.argeo.connect.people.rap.providers.BasicNodeListContentProvider;
import org.argeo.connect.people.rap.providers.EntitySingleColumnLabelProvider;
import org.argeo.connect.people.rap.utils.PeopleRapUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.eclipse.ui.utils.CommandUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

/** Basic view that display a list of entities with a quick search field. */
public class PeopleDefaultView extends ViewPart {
	// private final static Log log = LogFactory.getLog(QuickSearchView.class);

	public static final String ID = PeopleRapPlugin.PLUGIN_ID
			+ ".peopleDefaultView";

	private final static Integer ROW_LIMIT = 100;

	/* DEPENDENCY INJECTION */
	private Session session;
	private PeopleService peopleService;
	private PeopleWorkbenchService peopleUiService;
	// private Repository repository;

	// This page widgets
	private TableViewer personViewer;
	private Text filterTxt;
	private final static String FILTER_HELP_MSG = "Search...";

	private final static String CMD_OPEN_SEARCH_EDITOR = "openSearchEditor";
	private final static String CMD_LOGOUT = "org.eclipse.ui.file.exit";

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(PeopleRapUtils.noSpaceGridLayout());

		// Header
		Composite cmp = new Composite(parent, SWT.NO_FOCUS);
		cmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		createHeaderPart(cmp);
		// Filter
		cmp = new Composite(parent, SWT.NO_FOCUS);
		cmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		createFilterPart(cmp);

		// Table
		cmp = new Composite(parent, SWT.NO_FOCUS);
		cmp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		personViewer = createListPart(cmp, new EntitySingleColumnLabelProvider(
				peopleService, peopleUiService));

		refreshFilteredList();
	}

	private void createHeaderPart(Composite parent) {
		parent.setLayout(new FormLayout());

		// The BackGround
		Composite logoCmp = new Composite(parent, SWT.NO_FOCUS);
		FormData fdBg = PeopleRapUtils.createformData(0, 20, 100, 75);
		logoCmp.setLayoutData(fdBg);
		logoCmp.setData(RWT.CUSTOM_VARIANT,
				"people-logoComposite");

		// The Image
		Label logoLbl = new Label(parent, SWT.NO_FOCUS);
		logoLbl.setImage(PeopleImages.LOGO_SMALL);
		logoLbl.setData(RWT.CUSTOM_VARIANT, "people-logo");
		logoLbl.setSize(130, 131);
		FormData fdImg = new FormData();
		fdImg.top = new FormAttachment(0, 0);
		fdImg.bottom = new FormAttachment(100, 0);
		fdImg.left = new FormAttachment(2, 0);
		logoLbl.setLayoutData(fdImg);

		// The links
		Composite linksCmp = new Composite(parent, SWT.NO_FOCUS);
		linksCmp.setLayoutData(PeopleRapUtils.createformData(75, 25, 98, 73));
		linksCmp.setData(RWT.CUSTOM_VARIANT, "people-logoTable");
		linksCmp.setLayout(PeopleRapUtils.noSpaceGridLayout());

		addLink(linksCmp, "Search Entities",
				"Open an editor to narrow you search", CMD_OPEN_SEARCH_EDITOR);
		addLink(linksCmp, "Logout", "Log out from connect", CMD_LOGOUT);

		// Order layouts
		logoLbl.moveAbove(logoCmp);
		linksCmp.moveAbove(logoLbl);
	}

	private void callCommand(String commandId) {
		if (CMD_OPEN_SEARCH_EDITOR.equals(commandId)) {
			Map<String, String> params = new HashMap<String, String>();
			params.put(OpenSearchEntityEditor.PARAM_NODE_TYPE,
					PeopleTypes.PEOPLE_ENTITY);
			params.put(OpenSearchEntityEditor.PARAM_EDITOR_NAME, "Search");
			params.put(OpenSearchEntityEditor.PARAM_BASE_PATH, "/");
			CommandUtils.callCommand(
					peopleUiService.getOpenSearchEntityEditorCmdId(), params);
		} else if (CMD_LOGOUT.equals(commandId))
			CommandUtils.callCommand(CMD_LOGOUT);
	}

	private Link addLink(Composite parent, String label, String tooltip,
			final String commandId) {
		Link link = new Link(parent, SWT.NONE);
		link.setText("<a>" + label + "</a>");
		if (tooltip != null)
			link.setToolTipText(tooltip);
		link.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
		link.setData(RWT.CUSTOM_VARIANT, "people-logoTable");

		link.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				callCommand(commandId);
			}
		});
		return link;
	}

	private void createFilterPart(Composite parent) {
		parent.setLayout(new GridLayout());
		// Text Area for the filter
		filterTxt = new Text(parent, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH
				| SWT.ICON_CANCEL);
		filterTxt.setMessage(FILTER_HELP_MSG);
		filterTxt.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		filterTxt.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 5003010530960334977L;

			public void modifyText(ModifyEvent event) {
				refreshFilteredList();
			}
		});
	}

	protected TableViewer createListPart(Composite parent,
			ILabelProvider labelProvider) {
		TableViewer v = new TableViewer(parent);
		v.setLabelProvider(labelProvider);

		TableColumn singleColumn = new TableColumn(v.getTable(), SWT.V_SCROLL);
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		tableColumnLayout.setColumnData(singleColumn, new ColumnWeightData(85));
		parent.setLayout(tableColumnLayout);

		// Corresponding table & style
		Table table = v.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(false);
		// Enable markups
		table.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
		table.setData(RWT.CUSTOM_ITEM_HEIGHT,
				Integer.valueOf(24));

		v.setContentProvider(new BasicNodeListContentProvider());
		v.addDoubleClickListener(new PeopleJcrViewerDClickListener(
				peopleUiService));
		return v;
	}

	@Override
	public void dispose() {
		JcrUtils.logoutQuietly(session);
		super.dispose();
	}

	@Override
	public void setFocus() {
	}

	protected void refreshFilteredList() {
		try {
			List<Node> persons = JcrUtils.nodeIteratorToList(doSearch(session,
					filterTxt.getText(), PeopleTypes.PEOPLE_PERSON,
					PeopleNames.PEOPLE_LAST_NAME,
					PeopleNames.PEOPLE_PRIMARY_EMAIL));
			personViewer.setInput(persons);
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to list persons", e);
		}
	}

	/** Build repository request */
	private NodeIterator doSearch(Session session, String filter,
			String typeName, String orderProperty, String orderProperty2)
			throws RepositoryException {
		QueryManager queryManager = session.getWorkspace().getQueryManager();
		QueryObjectModelFactory factory = queryManager.getQOMFactory();

		Selector source = factory.selector(typeName, typeName);

		// no Default Constraint
		Constraint defaultC = null;

		// Parse the String
		String[] strs = filter.trim().split(" ");
		if (strs.length == 0) {
			// StaticOperand so = factory.literal(session.getValueFactory()
			// .createValue("*"));
			// defaultC = factory.fullTextSearch("selector", null, so);
		} else {
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

		Ordering order = null, order2 = null;

		if (orderProperty != null && !"".equals(orderProperty.trim()))
			order = factory.ascending(factory.lowerCase(factory.propertyValue(
					source.getSelectorName(), orderProperty)));
		if (orderProperty2 != null && !"".equals(orderProperty2.trim()))
			order2 = factory.ascending(factory.propertyValue(
					source.getSelectorName(), orderProperty2));

		QueryObjectModel query;
		if (order == null) {
			query = factory.createQuery(source, defaultC, null, null);
		} else {
			if (order2 == null)
				query = factory.createQuery(source, defaultC,
						new Ordering[] { order }, null);
			else
				query = factory.createQuery(source, defaultC, new Ordering[] {
						order, order2 }, null);
		}
		query.setLimit(ROW_LIMIT.longValue());
		QueryResult result = query.execute();
		return result.getNodes();
	}

	/* DEPENDENCY INJECTION */
	public void setRepository(Repository repository) {
		// this.repository = repository;
		session = CommonsJcrUtils.login(repository);
	}

	public void setPeopleService(PeopleService peopleService) {
		this.peopleService = peopleService;
	}

	public void setPeopleUiService(PeopleWorkbenchService peopleUiService) {
		this.peopleUiService = peopleUiService;
	}

}