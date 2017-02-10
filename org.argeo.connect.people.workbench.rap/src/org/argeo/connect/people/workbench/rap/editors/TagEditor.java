package org.argeo.connect.people.workbench.rap.editors;

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
import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.exports.PrimAddressLP;
import org.argeo.connect.people.ui.exports.PrimContactValueLP;
import org.argeo.connect.people.ui.exports.PrimOrgNameLP;
import org.argeo.connect.people.ui.exports.PrimaryTypeLP;
import org.argeo.connect.people.workbench.PeopleWorkbenchService;
import org.argeo.connect.people.workbench.rap.PeopleRapConstants;
import org.argeo.connect.people.workbench.rap.PeopleRapPlugin;
import org.argeo.connect.people.workbench.rap.composites.VirtualJcrTableViewer;
import org.argeo.connect.people.workbench.rap.dialogs.NoProgressBarWizardDialog;
import org.argeo.connect.people.workbench.rap.editors.util.EntityEditorInput;
import org.argeo.connect.people.workbench.rap.listeners.PeopleJcrViewerDClickListener;
import org.argeo.connect.people.workbench.rap.providers.JcrHtmlLabelProvider;
import org.argeo.connect.people.workbench.rap.providers.TagLabelProvider;
import org.argeo.connect.people.workbench.rap.providers.TitleIconRowLP;
import org.argeo.connect.people.workbench.rap.wizards.EditTagWizard;
import org.argeo.connect.ui.ConnectColumnDefinition;
import org.argeo.connect.ui.IJcrTableViewer;
import org.argeo.connect.ui.JcrRowLabelProvider;
import org.argeo.connect.ui.workbench.Refreshable;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.util.XPathUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
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

/** display a filtered list of entities for a given Tag */
public class TagEditor extends EditorPart implements PeopleNames, Refreshable, IJcrTableViewer {
	private final static Log log = LogFactory.getLog(TagEditor.class);
	public final static String ID = PeopleRapPlugin.PLUGIN_ID + ".tagEditor";

	/* DEPENDENCY INJECTION */
	private Repository repository;
	private PeopleService peopleService;
	private PeopleWorkbenchService peopleWorkbenchService;

	// UI Objects
	private Row[] rows;
	protected FormToolkit toolkit;
	private List<ConnectColumnDefinition> colDefs;
	private TableViewer membersViewer;
	private Text filterTxt;
	private Label titleROLbl;
	private ColumnLabelProvider groupTitleLP;

	// Business Objects
	private Session session;
	private Node node;

	// LIFE CYCLE
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
		EntityEditorInput sei = (EntityEditorInput) getEditorInput();

		session = ConnectJcrUtils.login(repository);
		node = ConnectJcrUtils.getNodeByIdentifier(session, sei.getUid());

		colDefs = new ArrayList<ConnectColumnDefinition>();
		colDefs.add(new ConnectColumnDefinition("Display Name",
				new TitleIconRowLP(peopleWorkbenchService, null, Property.JCR_TITLE), 300));
		colDefs.add(new ConnectColumnDefinition("Tags", new JcrHtmlLabelProvider(PEOPLE_TAGS), 300));
	}

	protected void afterNameUpdate(String name) {
		if (EclipseUiUtils.isEmpty(name))
			name = ConnectJcrUtils.get(node, Property.JCR_TITLE);
		if (EclipseUiUtils.notEmpty(name)) {
			setPartName(name);
			((EntityEditorInput) getEditorInput()).setTooltipText("List contacts tagged as " + name);
		}
		if (titleROLbl != null && !titleROLbl.isDisposed())
			titleROLbl.setText(groupTitleLP.getText(node));
	}

	/* SPECIFIC CONFIGURATION */
	/** Overwrite to add the batch update features */
	protected boolean enableBatchUpdate() {
		return false;
	}

	/* CONTENT CREATION */
	@Override
	public void createPartControl(Composite parent) {
		// Initialize main UI objects
		toolkit = new FormToolkit(parent.getDisplay());
		Form form = toolkit.createForm(parent);
		Composite main = form.getBody();
		createMainLayout(main);
		afterNameUpdate(null);
	}

	protected void createMainLayout(Composite parent) {
		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());
		// The header
		Composite header = toolkit.createComposite(parent, SWT.NO_FOCUS);
		header.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		populateHeader(header);
		// the body
		Composite body = toolkit.createComposite(parent, SWT.NO_FOCUS);
		body.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createMembersList(body, getNode());
	}

	protected void populateHeader(final Composite parent) {
		parent.setLayout(new GridLayout(2, false));
		titleROLbl = toolkit.createLabel(parent, "", SWT.WRAP);
		CmsUtils.markup(titleROLbl);
		groupTitleLP = new TagLabelProvider(peopleService.getResourceService(),
				PeopleRapConstants.LIST_TYPE_OVERVIEW_TITLE);
		titleROLbl.setText(groupTitleLP.getText(getNode()));
		titleROLbl.setLayoutData(EclipseUiUtils.fillWidth());

		Link editTitleLink = null;
		if (CurrentUser.isInRole(PeopleConstants.ROLE_BUSINESS_ADMIN)) {
			editTitleLink = new Link(parent, SWT.NONE);
			editTitleLink.setText("<a>Edit Tag</a>");
		} else
			toolkit.createLabel(parent, "");

		if (editTitleLink != null) {
			editTitleLink.addSelectionListener(new SelectionAdapter() {
				private static final long serialVersionUID = 1L;

				@Override
				public void widgetSelected(final SelectionEvent event) {
					EditTagWizard wizard = new EditTagWizard(peopleService, peopleWorkbenchService, getNode(),
							PeopleConstants.RESOURCE_TAG, PeopleNames.PEOPLE_TAGS);
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

		columns.add(
				new ConnectColumnDefinition("Type", new PrimaryTypeLP(getPeopleService().getResourceService(), null)));

		columns.add(new ConnectColumnDefinition("Name", new JcrRowLabelProvider(Property.JCR_TITLE)));

		columns.add(
				new ConnectColumnDefinition("Primary Email", new PrimContactValueLP(null, PeopleTypes.PEOPLE_EMAIL)));
		columns.add(
				new ConnectColumnDefinition("Primary Phone", new PrimContactValueLP(null, PeopleTypes.PEOPLE_PHONE)));

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
		columns.add(new ConnectColumnDefinition("Tags", new JcrRowLabelProvider(PEOPLE_TAGS)));
		columns.add(new ConnectColumnDefinition("Mailing Lists", new JcrRowLabelProvider(PEOPLE_MAILING_LISTS)));

		return columns;
	}

	public void createMembersList(Composite parent, final Node entity) {
		parent.setLayout(new GridLayout());

		// First line: search Text and buttons
		filterTxt = createFilterText(parent);
		filterTxt.setLayoutData(EclipseUiUtils.fillWidth());

		// Corresponding list
		Composite tableComp = toolkit.createComposite(parent);
		tableComp.setLayoutData(EclipseUiUtils.fillAll());

		membersViewer = createTableViewer(tableComp);
		membersViewer.setContentProvider(new MyLazyContentProvider(membersViewer));
		refreshFilteredList();

		// Double click
		PeopleJcrViewerDClickListener ndcl = new PeopleJcrViewerDClickListener(null, peopleWorkbenchService);
		membersViewer.addDoubleClickListener(ndcl);
	}

	private Text createFilterText(Composite parent) {
		Text filterTxt = toolkit.createText(parent, "", SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH | SWT.ICON_CANCEL);
		filterTxt.setMessage(PeopleRapConstants.FILTER_HELP_MSG);
		filterTxt.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		filterTxt.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 5003010530960334977L;

			public void modifyText(ModifyEvent event) {
				refreshFilteredList();
			}
		});
		return filterTxt;
	}

	/** Refresh the table viewer based on the free text search field */
	protected void refreshFilteredList() {
		long begin = System.currentTimeMillis();

		try {
			QueryManager queryManager = session.getWorkspace().getQueryManager();

			String xpathQueryStr = XPathUtils.descendantFrom(peopleService.getBasePath(null)) + "//element(*, "
					+ PeopleTypes.PEOPLE_ENTITY + ")";

			String filter = filterTxt.getText();
			String currVal = ConnectJcrUtils.get(getNode(), Property.JCR_TITLE);

			String freeTxtCond = XPathUtils.getFreeTextConstraint(filter);
			String mlNamecond = XPathUtils.getPropertyEquals(PEOPLE_TAGS, currVal);
			String conditions = XPathUtils.localAnd(freeTxtCond, mlNamecond);

			if (EclipseUiUtils.notEmpty(conditions))
				xpathQueryStr += "[" + conditions + "]";
			Query xpathQuery = queryManager.createQuery(xpathQueryStr, ConnectConstants.QUERY_XPATH);

			RowIterator xPathRit = xpathQuery.execute().getRows();
			Row[] rows = ConnectJcrUtils.rowIteratorToArray(xPathRit);
			setViewerInput(rows);

			if (log.isDebugEnabled()) {
				long end = System.currentTimeMillis();
				log.debug("Found: " + xPathRit.getSize() + " members for tag " + getNode() + " in " + (end - begin)
						+ " ms by executing XPath query (" + xpathQueryStr + ").");
			}

		} catch (RepositoryException e) {
			throw new PeopleException("Unable to list entities with static filter for tag " + getNode(), e);
		}
	}

	private TableViewer createTableViewer(Composite parent) {
		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());
		VirtualJcrTableViewer tableCmp = new VirtualJcrTableViewer(parent, SWT.MULTI, colDefs, enableBatchUpdate());
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

	@Override
	public void forceRefresh(Object object) {
		afterNameUpdate(null);
		refreshFilteredList();
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	/* EXPOSES TO CHILDREN CLASSES */
	/** Returns the entity Node that is bound to this editor */
	protected Node getNode() {
		return node;
	}

	protected PeopleService getPeopleService() {
		return peopleService;
	}

	protected PeopleWorkbenchService getPeopleWorkbenchService() {
		return peopleWorkbenchService;
	}

	/* UTILITES */
	protected boolean canSave() {
		return false;
	}

	@Override
	public void dispose() {
		JcrUtils.logoutQuietly(session);
		super.dispose();
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void setFocus() {
	}

	/* DEPENDENCY INJECTION */
	public void setPeopleService(PeopleService peopleService) {
		this.peopleService = peopleService;
	}

	public void setPeopleWorkbenchService(PeopleWorkbenchService peopleWorkbenchService) {
		this.peopleWorkbenchService = peopleWorkbenchService;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

}