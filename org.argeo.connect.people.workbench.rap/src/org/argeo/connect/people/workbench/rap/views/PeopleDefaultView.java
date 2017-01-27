package org.argeo.connect.people.workbench.rap.views;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.argeo.cms.ui.workbench.util.CommandUtils;
import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.ConnectConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.workbench.rap.PeopleRapImages;
import org.argeo.connect.people.workbench.rap.PeopleRapPlugin;
import org.argeo.connect.people.workbench.rap.PeopleRapUtils;
import org.argeo.connect.people.workbench.rap.PeopleStyles;
import org.argeo.connect.people.workbench.rap.PeopleWorkbenchService;
import org.argeo.connect.people.workbench.rap.commands.OpenSearchEntityEditor;
import org.argeo.connect.people.workbench.rap.listeners.PeopleJcrViewerDClickListener;
import org.argeo.connect.people.workbench.rap.providers.BasicNodeListContentProvider;
import org.argeo.connect.people.workbench.rap.providers.EntitySingleColumnLabelProvider;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.util.XPathUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TableViewer;
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
	private Repository repository;
	private Session session;
	private PeopleService peopleService;
	private PeopleWorkbenchService peopleWorkbenchService;

	// This page widgets
	private TableViewer contactableViewer;
	private Text filterTxt;
	private final static String FILTER_HELP_MSG = "Search";

	private final static String CMD_OPEN_SEARCH_EDITOR = "openSearchEditor";
	private final static String CMD_LOGOUT = "org.eclipse.ui.file.exit";

	@Override
	public void createPartControl(Composite parent) {
		this.session = ConnectJcrUtils.login(repository);

		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());

		// Header
		Composite cmp = new Composite(parent, SWT.NO_FOCUS);
		cmp.setLayoutData(EclipseUiUtils.fillWidth());
		createHeaderPart(cmp);

		// Filter
		cmp = new Composite(parent, SWT.NO_FOCUS);
		cmp.setLayoutData(EclipseUiUtils.fillWidth());
		createFilterPart(cmp);

		// Table
		cmp = new Composite(parent, SWT.NO_FOCUS);
		cmp.setLayoutData(EclipseUiUtils.fillAll());
		contactableViewer = createListPart(cmp,
				new EntitySingleColumnLabelProvider(peopleService,
						peopleWorkbenchService));

		// refreshFilteredList();
	}

	private void createHeaderPart(Composite parent) {
		parent.setLayout(new FormLayout());

		// Background
		Composite logoCmp = new Composite(parent, SWT.NO_FOCUS);
		FormData fdBg = PeopleRapUtils.createformData(0, 20, 100, 65);
		logoCmp.setLayoutData(fdBg);
		CmsUtils.style(logoCmp, PeopleStyles.LOGO_BOX);

		// Logo
		Label logoLbl = new Label(parent, SWT.NO_FOCUS);
		logoLbl.setImage(PeopleRapImages.LOGO_SMALL);
		CmsUtils.style(logoLbl, PeopleStyles.LOGO);
		logoLbl.setSize(130, 131);
		FormData fdImg = new FormData();
		fdImg.top = new FormAttachment(0, 0);
		fdImg.bottom = new FormAttachment(100, 0);
		fdImg.left = new FormAttachment(2, 0);
		logoLbl.setLayoutData(fdImg);

		// Links
		Composite linksCmp = new Composite(parent, SWT.NO_FOCUS);
		linksCmp.setLayoutData(PeopleRapUtils.createformData(65, 25, 98, 73));
		CmsUtils.style(linksCmp, PeopleStyles.LOGO_TABLE);
		linksCmp.setLayout(EclipseUiUtils.noSpaceGridLayout());
		addLink(linksCmp, "Search contact",
				"Open an editor to precise you search", CMD_OPEN_SEARCH_EDITOR);
		addLink(linksCmp, "Logout", "Directly log out from the application", CMD_LOGOUT);

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
					peopleWorkbenchService.getOpenSearchEntityEditorCmdId(),
					params);
		} else if (CMD_LOGOUT.equals(commandId))
			CommandUtils.callCommand(CMD_LOGOUT);
	}

	private Link addLink(Composite parent, String label, String tooltip,
			final String commandId) {
		Link link = new Link(parent, SWT.NONE);
		link.setText(" <a>" + label + "</a> ");
		if (tooltip != null)
			link.setToolTipText(tooltip);
		CmsUtils.markup(link);
		CmsUtils.style(link, PeopleStyles.LOGO_TABLE);

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
		CmsUtils.markup(table);
		CmsUtils.setItemHeight(table, 24);

		v.setContentProvider(new BasicNodeListContentProvider());
		v.addDoubleClickListener(new PeopleJcrViewerDClickListener(
				peopleWorkbenchService));
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
					filterTxt.getText(), PeopleTypes.PEOPLE_CONTACTABLE));
			// ,Property.JCR_TITLE, PeopleNames.PEOPLE_PRIMARY_EMAIL
			contactableViewer.setInput(persons);
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to list persons", e);
		}
	}

	/** Build repository request */
	private NodeIterator doSearch(Session session, String filter,
			String typeName) throws RepositoryException {
		QueryManager queryManager = session.getWorkspace().getQueryManager();
		String xpathQueryStr = "//element(*, " + typeName + ")";
		String xpathFilter = XPathUtils.getFreeTextConstraint(filter);
		if (EclipseUiUtils.notEmpty(xpathFilter))
			xpathQueryStr += "[" + xpathFilter + "]";

		Query xpathQuery = queryManager.createQuery(xpathQueryStr,
				ConnectConstants.QUERY_XPATH);
		xpathQuery.setLimit(ROW_LIMIT);
		QueryResult result = xpathQuery.execute();
		return result.getNodes();
	}

	/* DEPENDENCY INJECTION */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setPeopleService(PeopleService peopleService) {
		this.peopleService = peopleService;
	}

	public void setPeopleWorkbenchService(
			PeopleWorkbenchService peopleWorkbenchService) {
		this.peopleWorkbenchService = peopleWorkbenchService;
	}
}