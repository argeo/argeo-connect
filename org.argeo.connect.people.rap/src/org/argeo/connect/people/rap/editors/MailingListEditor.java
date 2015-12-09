package org.argeo.connect.people.rap.editors;

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
import org.argeo.ArgeoException;
import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.UserAdminService;
import org.argeo.connect.people.rap.PeopleRapConstants;
import org.argeo.connect.people.rap.PeopleRapPlugin;
import org.argeo.connect.people.rap.PeopleWorkbenchService;
import org.argeo.connect.people.rap.composites.VirtualJcrTableViewer;
import org.argeo.connect.people.rap.editors.utils.EntityEditorInput;
import org.argeo.connect.people.rap.listeners.PeopleJcrViewerDClickListener;
import org.argeo.connect.people.rap.providers.JcrHtmlLabelProvider;
import org.argeo.connect.people.rap.providers.TagLabelProvider;
import org.argeo.connect.people.rap.providers.TitleIconRowLP;
import org.argeo.connect.people.rap.utils.Refreshable;
import org.argeo.connect.people.rap.wizards.EditTagWizard;
import org.argeo.connect.people.ui.PeopleColumnDefinition;
import org.argeo.connect.people.utils.JcrUiUtils;
import org.argeo.connect.people.utils.PeopleJcrUtils;
import org.argeo.connect.people.utils.XPathUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
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
public class MailingListEditor extends EditorPart implements PeopleNames,
		Refreshable {
	private final static Log log = LogFactory.getLog(MailingListEditor.class);

	public final static String ID = PeopleRapPlugin.PLUGIN_ID
			+ ".mailingListEditor";

	/* DEPENDENCY INJECTION */
	private Repository repository;
	private PeopleService peopleService;
	private PeopleWorkbenchService peopleWorkbenchService;

	// Business objects
	private Node mailingList;

	// Context
	private Session session;
	private UserAdminService userService;

	// This page objects
	protected FormToolkit toolkit;
	private List<PeopleColumnDefinition> colDefs; // Default columns
	private TableViewer membersViewer;
	private Text filterTxt;
	private Row[] rows;
	private Form form;

	// LIFE CYCLE
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
		EntityEditorInput sei = (EntityEditorInput) getEditorInput();

		// Initialise context
		try {
			session = repository.login();
			mailingList = session.getNodeByIdentifier(sei.getUid());
		} catch (RepositoryException e) {
			throw new ArgeoException(
					"Unable to initialise mailing list editor for id "
							+ sei.getUid(), e);
		}
		// Retrieve userService from context
		userService = peopleService.getUserAdminService();

		// Name and tooltip
		String name = JcrUiUtils.get(mailingList, Property.JCR_TITLE);
		if (EclipseUiUtils.notEmpty(name))
			setPartName(name);
		setTitleToolTip("List contacts referenced by Mailing List " + name);

		// Initialize column definition
		// Cannot be done statically: we must have a valid reference to the
		// injected peopleUiService
		colDefs = new ArrayList<PeopleColumnDefinition>();
		colDefs.add(new PeopleColumnDefinition("Display Name",
				new TitleIconRowLP(peopleWorkbenchService, null,
						Property.JCR_TITLE), 300));
		colDefs.add(new PeopleColumnDefinition("Primary mail",
				new JcrHtmlLabelProvider(PEOPLE_CACHE_PMAIL), 300));
		colDefs.add(new PeopleColumnDefinition("Mailing lists",
				new JcrHtmlLabelProvider(PEOPLE_TAGS), 300));
	}

	/* CONTENT CREATION */
	@Override
	public void createPartControl(Composite parent) {
		// Initialize main UI objects
		toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createForm(parent);
		Composite main = form.getBody();
		createMainLayout(main);
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
		createMembersList(body, mailingList);
	}

	protected void populateHeader(final Composite parent) {
		parent.setLayout(new GridLayout(2, false));
		final Label titleROLbl = toolkit.createLabel(parent, "", SWT.WRAP);
		titleROLbl.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);

		final ColumnLabelProvider mlTitleLP = new TagLabelProvider(
				peopleService.getResourceService(),
				PeopleRapConstants.LIST_TYPE_OVERVIEW_TITLE);
		titleROLbl.setText(mlTitleLP.getText(mailingList));
		titleROLbl
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Link editTitleLink = null;
		if (userService.amIInRole(PeopleConstants.ROLE_BUSINESS_ADMIN)) {
			// || userService.amIInRole(PeopleConstants.ROLE_ADMIN)) {
			editTitleLink = new Link(parent, SWT.NONE);
			editTitleLink.setText("<a>Edit Mailing List</a>");
		} else
			toolkit.createLabel(parent, "");

		if (editTitleLink != null) {
			editTitleLink.addSelectionListener(new SelectionAdapter() {
				private static final long serialVersionUID = 1L;

				@Override
				public void widgetSelected(final SelectionEvent event) {

					Wizard wizard = new EditTagWizard(peopleService,
							peopleWorkbenchService, mailingList,
							PeopleTypes.PEOPLE_MAILING_LIST,
							PeopleNames.PEOPLE_MAILING_LISTS);

					WizardDialog dialog = new WizardDialog(titleROLbl
							.getShell(), wizard);
					int result = dialog.open();
					if (result == WizardDialog.OK) {
						titleROLbl.setText(mlTitleLP.getText(mailingList));
					}
				}
			});
		}

	}

	public void createMembersList(Composite parent, final Node entity) {
		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());

		// First line: search Text and buttons
		Composite buttonsCmp = toolkit.createComposite(parent);
		buttonsCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		buttonsCmp.setLayout(new GridLayout(3, false)); // remove add members
														// btn 4, false));

		filterTxt = createFilterText(buttonsCmp);
		// Button addBtn = toolkit
		// .createButton(buttonsCmp, "Add member", SWT.PUSH);

		// Add a button that triggers a "mailto action" with the mail of all
		// items that are currently displayed in the bottom table.
		final Button mailToBtn = toolkit.createButton(buttonsCmp, "Mail to",
				SWT.PUSH);
		mailToBtn
				.setToolTipText("Open a mail client with the mails of all "
						+ "members of this list that fit current search already set as BCC"
						+ " target");
		mailToBtn.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
		mailToBtn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				UrlLauncher launcher = RWT.getClient().getService(
						UrlLauncher.class);
				launcher.openURL("mailto:?bcc=" + getCurrentMails());
			}
		});

		// Corresponding list
		Composite tableComp = toolkit.createComposite(parent);
		tableComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		membersViewer = createTableViewer(tableComp);
		membersViewer.setContentProvider(new MyLazyContentProvider(
				membersViewer));
		// Double click
		PeopleJcrViewerDClickListener ndcl = new PeopleJcrViewerDClickListener(
				null, peopleWorkbenchService);
		membersViewer.addDoubleClickListener(ndcl);

		addFilterListener(filterTxt, membersViewer);
		refreshFilteredList();

	}

	/** Refresh the table viewer based on the free text search field */
	protected void refreshFilteredList() {
		long begin = System.currentTimeMillis();
		try {

			QueryManager queryManager = session.getWorkspace()
					.getQueryManager();

			String xpathQueryStr = XPathUtils.descendantFrom(peopleService
					.getBasePath(null))
					+ "//element(*, "
					+ PeopleTypes.PEOPLE_ENTITY + ")";

			String filter = filterTxt.getText();
			String currVal = JcrUiUtils.get(mailingList,
					Property.JCR_TITLE);

			String freeTxtCond = XPathUtils.getFreeTextConstraint(filter);
			String mlNamecond = XPathUtils.getPropertyEquals(
					PEOPLE_MAILING_LISTS, currVal);
			String conditions = XPathUtils.localAnd(freeTxtCond, mlNamecond);

			if (EclipseUiUtils.notEmpty(conditions))
				xpathQueryStr += "[" + conditions + "]";
			Query xpathQuery = queryManager.createQuery(xpathQueryStr,
					PeopleConstants.QUERY_XPATH);

			RowIterator xPathRit = xpathQuery.execute().getRows();
			Row[] rows = JcrUiUtils.rowIteratorToArray(xPathRit);
			setViewerInput(rows);

			if (log.isDebugEnabled()) {
				long end = System.currentTimeMillis();
				log.debug("Found: " + xPathRit.getSize()
						+ " members for mailing list " + mailingList + " in "
						+ (end - begin) + " ms by executing XPath query ("
						+ xpathQueryStr + ").");
			}

			// QueryObjectModelFactory factory = queryManager.getQOMFactory();
			// Selector source = factory.selector(PeopleTypes.PEOPLE_ENTITY,
			// PeopleTypes.PEOPLE_ENTITY);
			//
			// String filter = filterTxt.getText();
			// String currVal = JcrUiUtils.get(mailingList,
			// Property.JCR_TITLE);
			// StaticOperand so = factory.literal(session.getValueFactory()
			// .createValue(currVal));
			// DynamicOperand dyo = factory.propertyValue(
			// source.getSelectorName(), PEOPLE_MAILING_LISTS);
			// Constraint constraint = factory.comparison(dyo,
			// QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO, so);
			//
			// Constraint subTree = factory.descendantNode(
			// source.getSelectorName(), peopleService.getBasePath(null));
			// constraint = JcrUiUtils.localAnd(factory, constraint,
			// subTree);
			//
			// if (JcrUiUtils.checkNotEmptyString(filter)) {
			// String[] strs = filter.trim().split(" ");
			// for (String token : strs) {
			// StaticOperand soTmp = factory.literal(session
			// .getValueFactory().createValue("*" + token + "*"));
			// Constraint currC = factory.fullTextSearch(
			// source.getSelectorName(), null, soTmp);
			// constraint = JcrUiUtils.localAnd(factory, constraint,
			// currC);
			// }
			// }
			//
			// Ordering order = factory.ascending(factory.propertyValue(
			// source.getSelectorName(), Property.JCR_TITLE));
			// Ordering[] orderings = { order };
			// QueryObjectModel query = factory.createQuery(source, constraint,
			// orderings, null);
			// QueryResult result = query.execute();
			// Row[] rows =
			// JcrUiUtils.rowIteratorToArray(result.getRows());
			// setViewerInput(rows);

		} catch (RepositoryException e) {
			throw new PeopleException(
					"Unable to list contacts for mailing list " + mailingList,
					e);
		}
	}

	/* Provide extraction ability */
	public Row[] getElements(String extractId) {
		return rows;
	}

	private TableViewer createTableViewer(Composite parent) {
		parent.setLayout(new GridLayout());
		VirtualJcrTableViewer tableCmp = new VirtualJcrTableViewer(parent,
				SWT.MULTI, colDefs);
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
				Node node;
				node = row.getNode(PeopleTypes.PEOPLE_ENTITY);
				String mailValue = PeopleJcrUtils.getPrimaryContactValue(node,
						PeopleTypes.PEOPLE_EMAIL);
				if (EclipseUiUtils.notEmpty(mailValue))
					builder.append(mailValue).append(",");
			}
			return builder.toString();
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to retrieved current mails ", e);
		}
	}

	// private void configureAddMemberButton(final AbstractFormPart part,
	// Button button, final Node targetNode, String tooltip,
	// final String nodeTypeToSearch) {
	// button.setToolTipText(tooltip);
	// button.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
	//
	// button.addSelectionListener(new SelectionListener() {
	// private static final long serialVersionUID = 1L;
	//
	// @Override
	// public void widgetSelected(SelectionEvent e) {
	// Map<String, String> params = new HashMap<String, String>();
	// try {
	// params.put(AddEntityReference.PARAM_REFERENCING_JCR_ID,
	// targetNode.getIdentifier());
	// params.put(AddEntityReference.PARAM_TO_SEARCH_NODE_TYPE,
	// nodeTypeToSearch);
	// params.put(AddEntityReference.PARAM_DIALOG_ID,
	// PeopleUiConstants.DIALOG_ADD_ML_MEMBERS);
	// CommandUtils.callCommand(AddEntityReference.ID, params);
	// part.refresh();
	// } catch (RepositoryException e1) {
	// throw new PeopleException(
	// "Unable to get parent Jcr identifier", e1);
	// }
	// }
	//
	// @Override
	// public void widgetDefaultSelected(SelectionEvent e) {
	// }
	// });
	// }

	private Text createFilterText(Composite parent) {
		Text filterTxt = toolkit.createText(parent, "", SWT.BORDER | SWT.SEARCH
				| SWT.ICON_SEARCH | SWT.ICON_CANCEL);
		filterTxt.setMessage(PeopleRapConstants.FILTER_HELP_MSG);
		filterTxt.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		return filterTxt;
	}

	private void addFilterListener(final Text filterTxt,
			final TableViewer viewer) {
		filterTxt.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 5003010530960334977L;

			public void modifyText(ModifyEvent event) {
				refreshFilteredList();
			}
		});

	}

	@Override
	public void forceRefresh(Object object) {
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

	public PeopleWorkbenchService getPeopleWorkbenchService() {
		return peopleWorkbenchService;
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

	public void setPeopleService(PeopleService peopleService) {
		this.peopleService = peopleService;
	}

	public void setPeopleWorkbenchService(
			PeopleWorkbenchService peopleWorkbenchService) {
		this.peopleWorkbenchService = peopleWorkbenchService;
	}
}