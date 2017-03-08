package org.argeo.people.workbench.rap.parts;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.cms.auth.CurrentUser;
import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.ConnectConstants;
import org.argeo.connect.resources.ResourcesNames;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.resources.workbench.OtherTagsLabelProvider;
import org.argeo.connect.ui.ConnectColumnDefinition;
import org.argeo.connect.ui.ConnectUiConstants;
import org.argeo.connect.ui.IJcrTableViewer;
import org.argeo.connect.ui.util.JcrRowLabelProvider;
import org.argeo.connect.ui.util.MainNodeTypeLabelProvider;
import org.argeo.connect.ui.util.TagLabelProvider;
import org.argeo.connect.ui.util.VirtualJcrTableViewer;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.util.XPathUtils;
import org.argeo.connect.workbench.AppWorkbenchService;
import org.argeo.connect.workbench.Refreshable;
import org.argeo.connect.workbench.commands.EditTagWizard;
import org.argeo.connect.workbench.util.EntityEditorInput;
import org.argeo.connect.workbench.util.JcrHtmlLabelProvider;
import org.argeo.connect.workbench.util.JcrViewerDClickListener;
import org.argeo.connect.workbench.util.TitleIconRowLP;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;
import org.argeo.people.PeopleException;
import org.argeo.people.PeopleNames;
import org.argeo.people.PeopleRole;
import org.argeo.people.PeopleService;
import org.argeo.people.PeopleTypes;
import org.argeo.people.ui.exports.PrimAddressLP;
import org.argeo.people.ui.exports.PrimContactValueLP;
import org.argeo.people.ui.exports.PrimOrgNameLP;
import org.argeo.people.util.PeopleJcrUtils;
import org.argeo.people.workbench.rap.PeopleRapPlugin;
import org.argeo.people.workbench.rap.dialogs.NoProgressBarWizardDialog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.UrlLauncher;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.EditorPart;

/** Editor page that displays a mailing list and its members */
public class MailingListEditor extends EditorPart implements PeopleNames, Refreshable, IJcrTableViewer {
	private final static Log log = LogFactory.getLog(MailingListEditor.class);

	public final static String ID = PeopleRapPlugin.PLUGIN_ID + ".mailingListEditor";

	/* DEPENDENCY INJECTION */
	private Repository repository;
	private ResourcesService resourcesService;
	private PeopleService peopleService;
	private AppWorkbenchService appWorkbenchService;

	// Business objects
	private Node mailingList;

	// Context
	private Session session;

	// UI objects
	protected FormToolkit toolkit;
	private List<ConnectColumnDefinition> colDefs; // Default columns
	private TableViewer membersViewer;
	private Text filterTxt;

	private Label titleROLbl;
	private ColumnLabelProvider mlTitleLP;

	private Row[] rows;
	private Form form;

	// LIFE CYCLE
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
		EntityEditorInput sei = (EntityEditorInput) getEditorInput();

		// Initialise context
		session = ConnectJcrUtils.login(repository);
		mailingList = ConnectJcrUtils.getNodeByIdentifier(session, sei.getUid());

		// Initialize column definition
		// Cannot be done statically: we must have a valid reference to the
		// injected peopleUiService
		colDefs = new ArrayList<ConnectColumnDefinition>();
		colDefs.add(new ConnectColumnDefinition("Display Name",
				new TitleIconRowLP(appWorkbenchService, null, Property.JCR_TITLE), 300));
		colDefs.add(new ConnectColumnDefinition("Primary mail", new JcrHtmlLabelProvider(PEOPLE_PMAIL), 300));
		colDefs.add(
				new ConnectColumnDefinition("Other mailing lists", new OtherTagsLabelProvider(mailingList, null), 300));
	}

	protected void afterNameUpdate(String name) {
		if (EclipseUiUtils.isEmpty(name))
			name = ConnectJcrUtils.get(mailingList, Property.JCR_TITLE);

		if (EclipseUiUtils.notEmpty(name)) {
			setPartName(name);
			((EntityEditorInput) getEditorInput()).setTooltipText("List contacts referenced by Mailing List " + name);
		}
		if (titleROLbl != null && !titleROLbl.isDisposed())
			titleROLbl.setText(mlTitleLP.getText(mailingList));
	}

	/* CONTENT CREATION */
	@Override
	public void createPartControl(Composite parent) {
		// Initialize main UI objects
		toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createForm(parent);
		Composite main = form.getBody();
		createMainLayout(main);
		afterNameUpdate(null);
	}

	protected void createMainLayout(Composite parent) {
		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());
		// The header
		Composite header = toolkit.createComposite(parent, SWT.NO_FOCUS);
		header.setLayoutData(EclipseUiUtils.fillWidth());
		populateHeader(header);
		// the body
		Composite body = toolkit.createComposite(parent, SWT.NO_FOCUS);
		body.setLayoutData(EclipseUiUtils.fillAll());
		createMembersList(body, mailingList);
	}

	protected void populateHeader(final Composite parent) {
		parent.setLayout(new GridLayout(2, false));
		titleROLbl = toolkit.createLabel(parent, "", SWT.WRAP);
		CmsUtils.markup(titleROLbl);

		mlTitleLP = new TagLabelProvider(resourcesService, ConnectUiConstants.LIST_TYPE_OVERVIEW_TITLE);
		titleROLbl.setText(mlTitleLP.getText(mailingList));
		titleROLbl.setLayoutData(EclipseUiUtils.fillWidth());

		Link editTitleLink = null;
		if (CurrentUser.isInRole(PeopleRole.editor.dn())) {
			editTitleLink = new Link(parent, SWT.NONE);
			editTitleLink.setText("<a>Edit Mailing List</a>");
		} else
			toolkit.createLabel(parent, "");

		if (editTitleLink != null) {
			editTitleLink.addSelectionListener(new SelectionAdapter() {
				private static final long serialVersionUID = 1L;

				@Override
				public void widgetSelected(final SelectionEvent event) {

					EditTagWizard wizard = new EditTagWizard(resourcesService, appWorkbenchService, mailingList,
							PeopleTypes.PEOPLE_MAILING_LIST, PeopleNames.PEOPLE_MAILING_LISTS);

					NoProgressBarWizardDialog dialog = new NoProgressBarWizardDialog(titleROLbl.getShell(), wizard);
					dialog.open();
				}
			});
		}
	}

	/* Provide extraction ability */
	@Override
	public Row[] getElements(String extractId) {
		return rows;
	}

	@Override
	public List<ConnectColumnDefinition> getColumnDefinition(String extractId) {
		List<ConnectColumnDefinition> columns = new ArrayList<ConnectColumnDefinition>();

		columns.add(new ConnectColumnDefinition("Type", new MainNodeTypeLabelProvider(resourcesService, null)));
		columns.add(new ConnectColumnDefinition("Name", new JcrRowLabelProvider(Property.JCR_TITLE)));

		columns.add(
				new ConnectColumnDefinition("Primary Email", new PrimContactValueLP(null, PeopleTypes.PEOPLE_MAIL)));
		columns.add(
				new ConnectColumnDefinition("Primary Mobile", new PrimContactValueLP(null, PeopleTypes.PEOPLE_MOBILE)));
		columns.add(new ConnectColumnDefinition("Primary Phone",
				new PrimContactValueLP(null, PeopleTypes.PEOPLE_TELEPHONE_NUMBER)));

		columns.add(new ConnectColumnDefinition("Salutation", new JcrRowLabelProvider(PEOPLE_SALUTATION)));
		columns.add(new ConnectColumnDefinition("Title", new JcrRowLabelProvider(PEOPLE_HONORIFIC_TITLE)));
		columns.add(new ConnectColumnDefinition("First name", new JcrRowLabelProvider(PEOPLE_FIRST_NAME)));
		columns.add(new ConnectColumnDefinition("Middle name", new JcrRowLabelProvider(PEOPLE_MIDDLE_NAME)));
		columns.add(new ConnectColumnDefinition("Last name", new JcrRowLabelProvider(PEOPLE_LAST_NAME)));
		columns.add(new ConnectColumnDefinition("Name Suffix", new JcrRowLabelProvider(PEOPLE_NAME_SUFFIX)));

		columns.add(new ConnectColumnDefinition("Organisation", new PrimOrgNameLP(getPeopleService(), null)));

		columns.add(new ConnectColumnDefinition("Primary Street",
				new PrimAddressLP(getPeopleService(), null, PEOPLE_STREET)));
		columns.add(new ConnectColumnDefinition("Primary Street2",
				new PrimAddressLP(getPeopleService(), null, PEOPLE_STREET_COMPLEMENT), 100));
		columns.add(
				new ConnectColumnDefinition("Primary City", new PrimAddressLP(getPeopleService(), null, PEOPLE_CITY)));
		columns.add(new ConnectColumnDefinition("Primary State",
				new PrimAddressLP(getPeopleService(), null, PEOPLE_STATE)));
		columns.add(new ConnectColumnDefinition("Primary Zip",
				new PrimAddressLP(getPeopleService(), null, PEOPLE_ZIP_CODE)));
		columns.add(new ConnectColumnDefinition("Primary Country",
				new PrimAddressLP(getPeopleService(), null, PEOPLE_COUNTRY)));

		columns.add(
				new ConnectColumnDefinition("Primary Website", new PrimContactValueLP(null, PeopleTypes.PEOPLE_URL)));

		columns.add(new ConnectColumnDefinition("Notes", new JcrRowLabelProvider(Property.JCR_DESCRIPTION)));
		columns.add(new ConnectColumnDefinition("Tags", new JcrRowLabelProvider(ResourcesNames.CONNECT_TAGS)));
		columns.add(new ConnectColumnDefinition("Mailing Lists", new JcrRowLabelProvider(PEOPLE_MAILING_LISTS)));

		return columns;
	}

	public void createMembersList(Composite parent, final Node entity) {
		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());

		// First line: search Text and buttons
		Composite buttonsCmp = toolkit.createComposite(parent);
		buttonsCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		buttonsCmp.setLayout(new GridLayout(3, false));
		filterTxt = createFilterText(buttonsCmp);

		// Button to launch a mail client (depending on the end user brower and
		// system configuration), and create a new mail adding in BCC all
		// addresses that are found from the currently displayed items
		final Button mailToBtn = toolkit.createButton(buttonsCmp, "Mail to", SWT.PUSH);
		mailToBtn.setToolTipText("Open a mail client with the mails of all "
				+ "members of this list that fit current search already set as BCC" + " target");
		mailToBtn.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
		mailToBtn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				UrlLauncher launcher = RWT.getClient().getService(UrlLauncher.class);
				launcher.openURL("mailto:?bcc=" + getCurrentMails());
			}
		});

		// Corresponding list
		Composite tableComp = toolkit.createComposite(parent);
		tableComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		membersViewer = createTableViewer(tableComp);
		membersViewer.setContentProvider(new MyLazyContentProvider(membersViewer));
		membersViewer.addDoubleClickListener(new JcrViewerDClickListener());
		addFilterListener(filterTxt, membersViewer);
		refreshFilteredList();
	}

	/** Refresh the table viewer based on the free text search field */
	protected void refreshFilteredList() {
		long begin = System.currentTimeMillis();
		try {
			String xpathQueryStr = XPathUtils
					.descendantFrom("/" + peopleService.getBaseRelPath(PeopleTypes.PEOPLE_ENTITY)) + "//element(*, "
					+ PeopleTypes.PEOPLE_CONTACTABLE + ")";
			String filter = filterTxt.getText();
			String currVal = ConnectJcrUtils.get(mailingList, Property.JCR_TITLE);

			String freeTxtCond = XPathUtils.getFreeTextConstraint(filter);
			String mlNamecond = XPathUtils.getPropertyEquals(PEOPLE_MAILING_LISTS, currVal);
			String conditions = XPathUtils.localAnd(freeTxtCond, mlNamecond);

			if (EclipseUiUtils.notEmpty(conditions))
				xpathQueryStr += "[" + conditions + "]";

			QueryManager queryManager = session.getWorkspace().getQueryManager();
			Query xpathQuery = queryManager.createQuery(xpathQueryStr, ConnectConstants.QUERY_XPATH);
			RowIterator xPathRit = xpathQuery.execute().getRows();
			Row[] rows = ConnectJcrUtils.rowIteratorToArray(xPathRit);
			setViewerInput(rows);
			if (log.isDebugEnabled()) {
				long end = System.currentTimeMillis();
				log.debug("Found: " + xPathRit.getSize() + " members for mailing list " + mailingList + " in "
						+ (end - begin) + " ms by executing XPath query (" + xpathQueryStr + ").");
			}
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to list contacts " + "for mailing list " + mailingList, e);
		}
	}

	private TableViewer createTableViewer(Composite parent) {
		parent.setLayout(new GridLayout());
		VirtualJcrTableViewer tableCmp = new VirtualJcrTableViewer(parent, SWT.MULTI, colDefs);
		TableViewer tableViewer = tableCmp.getTableViewer();
		tableCmp.setLayoutData(EclipseUiUtils.fillAll());
		return tableViewer;
	}

	/** Use this method to update the result table */
	protected void setViewerInput(Row[] rows) {
		this.rows = rows;
		membersViewer.setInput(rows);
		// we must explicitly set the items count
		membersViewer.setItemCount(rows.length);
		membersViewer.refresh();
	}

	private class MyLazyContentProvider implements ILazyContentProvider {
		private static final long serialVersionUID = 1L;
		private TableViewer viewer;
		private Row[] elements;

		public MyLazyContentProvider(TableViewer viewer) {
			this.viewer = viewer;
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// IMPORTANT: don't forget this: an exception will be thrown if a
			// selected object is not part of the results anymore.
			viewer.setSelection(null);
			this.elements = (Row[]) newInput;
		}

		public void updateElement(int index) {
			viewer.replace(elements[index], index);
		}
	}

	/* LOCAL CLASSES */
	// ///////////////////////
	// HELPERS
	private String getCurrentMails() {
		StringBuilder builder = new StringBuilder();
		try {
			for (Row row : rows) {
				Node node = row.getNode(); // PeopleTypes.PEOPLE_ENTITY
				String mailValue = PeopleJcrUtils.getPrimaryContactValue(node, PeopleTypes.PEOPLE_MAIL);
				if (EclipseUiUtils.notEmpty(mailValue))
					builder.append(mailValue).append(",");
			}
			return builder.toString();
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to retrieved current mails ", e);
		}
	}

	private Text createFilterText(Composite parent) {
		Text filterTxt = toolkit.createText(parent, "", SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH | SWT.ICON_CANCEL);
		filterTxt.setLayoutData(EclipseUiUtils.fillWidth());
		return filterTxt;
	}

	private void addFilterListener(final Text filterTxt, final TableViewer viewer) {
		filterTxt.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 5003010530960334977L;

			public void modifyText(ModifyEvent event) {
				refreshFilteredList();
			}
		});
	}

	@Override
	public void forceRefresh(Object object) {
		afterNameUpdate(null);
		refreshFilteredList();
	}

	@Override
	public void setFocus() {
		filterTxt.setFocus();
	}

	@Override
	public void dispose() {
		JcrUtils.logoutQuietly(session);
		super.dispose();
	}

	// Expose to extending classes
	public PeopleService getPeopleService() {
		return peopleService;
	}

	public AppWorkbenchService getAppWorkbenchService() {
		return appWorkbenchService;
	}

	public Node getNode() {
		return mailingList;
	}

	// Compulsory unused methods.
	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	/* DEPENDENCY INJECTION */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setResourcesService(ResourcesService resourcesService) {
		this.resourcesService = resourcesService;
	}

	public void setPeopleService(PeopleService peopleService) {
		this.peopleService = peopleService;
	}

	public void setAppWorkbenchService(AppWorkbenchService appWorkbenchService) {
		this.appWorkbenchService = appWorkbenchService;
	}
}
