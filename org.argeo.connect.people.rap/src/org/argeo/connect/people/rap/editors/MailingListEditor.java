package org.argeo.connect.people.rap.editors;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.DynamicOperand;
import javax.jcr.query.qom.Ordering;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelConstants;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;
import javax.jcr.query.qom.StaticOperand;

import org.argeo.ArgeoException;
import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.UserManagementService;
import org.argeo.connect.people.rap.PeopleRapConstants;
import org.argeo.connect.people.rap.PeopleRapPlugin;
import org.argeo.connect.people.rap.PeopleWorkbenchService;
import org.argeo.connect.people.rap.composites.PeopleVirtualTableViewer;
import org.argeo.connect.people.rap.editors.utils.EntityEditorInput;
import org.argeo.connect.people.rap.exports.PeopleColumnDefinition;
import org.argeo.connect.people.rap.listeners.PeopleJcrViewerDClickListener;
import org.argeo.connect.people.rap.providers.JcrRowHtmlLabelProvider;
import org.argeo.connect.people.rap.providers.TagLabelProvider;
import org.argeo.connect.people.rap.providers.TitleWithIconLP;
import org.argeo.connect.people.rap.utils.Refreshable;
import org.argeo.connect.people.rap.wizards.EditTagWizard;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.PeopleJcrUtils;
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

/**
 * Editor page that display a mailing list
 */
public class MailingListEditor extends EditorPart implements PeopleNames,
		Refreshable {
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
	private UserManagementService userService;

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
		userService = peopleService.getUserManagementService();

		// Name and tooltip
		String name = CommonsJcrUtils.get(mailingList, Property.JCR_TITLE);
		if (CommonsJcrUtils.checkNotEmptyString(name))
			setPartName(name);
		setTitleToolTip("List contacts referenced by Mailing List " + name);

		// Initialize column definition
		// Cannot be done statically: we must have a valid reference to the
		// injected peopleUiService
		colDefs = new ArrayList<PeopleColumnDefinition>();
		colDefs.add(new PeopleColumnDefinition(PeopleTypes.PEOPLE_ENTITY,
				Property.JCR_TITLE, PropertyType.STRING, "Display Name",
				new TitleWithIconLP(peopleWorkbenchService,
						PeopleTypes.PEOPLE_ENTITY, Property.JCR_TITLE), 300));
		colDefs.add(new PeopleColumnDefinition(PeopleTypes.PEOPLE_ENTITY,
				PEOPLE_CACHE_PMAIL, PropertyType.STRING, "Primary mail",
				new JcrRowHtmlLabelProvider(PeopleTypes.PEOPLE_ENTITY,
						PEOPLE_CACHE_PMAIL), 300));
		colDefs.add(new PeopleColumnDefinition(PeopleTypes.PEOPLE_ENTITY,
				PEOPLE_MAILING_LISTS, PropertyType.STRING, "Mailing lists",
				new JcrRowHtmlLabelProvider(PeopleTypes.PEOPLE_ENTITY,
						PEOPLE_TAGS), 300));
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
		parent.setLayout(PeopleUiUtils.noSpaceGridLayout());
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
				peopleService.getTagService(),
				PeopleRapConstants.LIST_TYPE_OVERVIEW_TITLE,
				peopleService.getBasePath(null), PeopleTypes.PEOPLE_ENTITY,
				PEOPLE_MAILING_LISTS);
		titleROLbl.setText(mlTitleLP.getText(mailingList));
		titleROLbl
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Link editTitleLink = null;
		if (userService.isUserInRole(PeopleConstants.ROLE_BUSINESS_ADMIN)
				|| userService.isUserInRole(PeopleConstants.ROLE_ADMIN)) {
			editTitleLink = new Link(parent, SWT.NONE);
			editTitleLink.setText("<a>Edit Mailing List</a>");
		} else
			toolkit.createLabel(parent, "");

		if (editTitleLink != null) {
			editTitleLink.addSelectionListener(new SelectionAdapter() {
				private static final long serialVersionUID = 1L;

				@Override
				public void widgetSelected(final SelectionEvent event) {

					Wizard wizard = new EditTagWizard(
							peopleService,
							peopleWorkbenchService,
							mailingList,
							PeopleTypes.PEOPLE_MAILING_LIST,
							peopleService
									.getResourceBasePath(PeopleTypes.PEOPLE_MAILING_LIST),
							PeopleTypes.PEOPLE_ENTITY,
							PeopleNames.PEOPLE_MAILING_LISTS, peopleService
									.getBasePath(null));

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

	// protected void populateHeader(Composite parent) {
	// parent.setLayout(new FormLayout());
	//
	// // READ ONLY PANEL
	// final Composite roPanelCmp = toolkit.createComposite(parent,
	// SWT.NO_FOCUS);
	// PeopleUiUtils.setSwitchingFormData(roPanelCmp);
	// roPanelCmp.setLayout(new GridLayout());
	//
	// // Add a label with info provided by the FilmOverviewLabelProvider
	// final Label titleROLbl = toolkit.createLabel(roPanelCmp, "", SWT.WRAP);
	// titleROLbl.setData(PeopleUiConstants.MARKUP_ENABLED, Boolean.TRUE);
	//
	// final ColumnLabelProvider groupTitleLP = new TagLabelProvider(
	// PeopleUiConstants.LIST_TYPE_OVERVIEW_TITLE, getPeopleService()
	// .getBasePath(null), PeopleTypes.PEOPLE_ENTITY,
	// PEOPLE_MAILING_LISTS);
	//
	// // EDIT PANEL
	// final Composite editPanel = toolkit.createComposite(parent,
	// SWT.NO_FOCUS);
	// PeopleUiUtils.setSwitchingFormData(editPanel);
	//
	// // intern layout
	// editPanel.setLayout(new GridLayout(2, false));
	// final Label editTitle = toolkit.createLabel(editPanel, "");
	// editTitle.setData(PeopleUiConstants.MARKUP_ENABLED, Boolean.TRUE);
	// editTitle.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
	//
	// Link editTitleLink = null;
	// if (userService.isUserInRole(PeopleConstants.ROLE_BUSINESS_ADMIN)
	// || userService.isUserInRole(PeopleConstants.ROLE_ADMIN)) {
	// editTitleLink = new Link(editPanel, SWT.NONE);
	// editTitleLink.setText("<a>Edit Mailing List Title...</a>");
	// } else
	// toolkit.createLabel(editPanel, "");
	//
	// final Text descTxt = PeopleUiUtils.createGDText(toolkit, editPanel,
	// "A Description", "", 400, 2);
	//
	// final AbstractFormPart editPart = new AbstractFormPart() {
	// public void refresh() {
	// super.refresh();
	// // EDIT PART
	// String title = "<b><big> "
	// + CommonsJcrUtils.get(mailingList, Property.JCR_TITLE)
	// + "</big></b>";
	// editTitle.setText(title);
	// PeopleUiUtils.refreshTextWidgetValue(descTxt, mailingList,
	// Property.JCR_DESCRIPTION);
	//
	// // READ ONLY PART
	// titleROLbl.setText(groupTitleLP.getText(mailingList));
	// // Manage switch
	// if (CommonsJcrUtils.isNodeCheckedOutByMe(mailingList))
	// editPanel.moveAbove(roPanelCmp);
	// else
	// editPanel.moveBelow(roPanelCmp);
	// editPanel.getParent().layout();
	// }
	// };
	//
	// PeopleUiUtils.addTxtModifyListener(editPart, descTxt, mailingList,
	// Property.JCR_DESCRIPTION, PropertyType.STRING);
	//
	// if (editTitleLink != null) {
	// editTitleLink.addSelectionListener(new SelectionAdapter() {
	// private static final long serialVersionUID = 1L;
	//
	// @Override
	// public void widgetSelected(final SelectionEvent event) {
	//
	// Wizard wizard = new EditTagWizard(getPeopleService(),
	// getPeopleUiService(), mailingList,
	// PeopleTypes.PEOPLE_MAILING_LIST, getPeopleService()
	// .getResourceBasePath(
	// PeopleTypes.PEOPLE_MAILING_LIST),
	// PeopleTypes.PEOPLE_ENTITY,
	// PeopleNames.PEOPLE_MAILING_LISTS,
	// getPeopleService().getBasePath(null));
	// WizardDialog dialog = new WizardDialog(descTxt.getShell(),
	// wizard);
	// // dialog.setText();
	// int result = dialog.open();
	// if (result == WizardDialog.OK) {
	// editPart.markDirty();
	// editPart.refresh();
	// }
	// }
	// });
	// }
	// editPart.initialize(getManagedForm());
	// getManagedForm().addPart(editPart);
	// }

	public void createMembersList(Composite parent, final Node entity) {
		parent.setLayout(PeopleUiUtils.noSpaceGridLayout());

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

		addFilterListener(filterTxt, membersViewer);
		refreshFilteredList();

		// Double click
		PeopleJcrViewerDClickListener ndcl = new PeopleJcrViewerDClickListener(
				PeopleTypes.PEOPLE_ENTITY, peopleWorkbenchService);
		membersViewer.addDoubleClickListener(ndcl);
	}

	/** Refresh the table viewer based on the free text search field */
	protected void refreshFilteredList() {
		try {
			QueryManager queryManager = session.getWorkspace()
					.getQueryManager();
			QueryObjectModelFactory factory = queryManager.getQOMFactory();
			Selector source = factory.selector(PeopleTypes.PEOPLE_ENTITY,
					PeopleTypes.PEOPLE_ENTITY);

			String filter = filterTxt.getText();
			String currVal = CommonsJcrUtils.get(mailingList,
					Property.JCR_TITLE);
			StaticOperand so = factory.literal(session.getValueFactory()
					.createValue(currVal));
			DynamicOperand dyo = factory.propertyValue(
					source.getSelectorName(), PEOPLE_MAILING_LISTS);
			Constraint constraint = factory.comparison(dyo,
					QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO, so);

			Constraint subTree = factory.descendantNode(
					source.getSelectorName(), peopleService.getBasePath(null));
			constraint = CommonsJcrUtils.localAnd(factory, constraint, subTree);

			if (CommonsJcrUtils.checkNotEmptyString(filter)) {
				String[] strs = filter.trim().split(" ");
				for (String token : strs) {
					StaticOperand soTmp = factory.literal(session
							.getValueFactory().createValue("*" + token + "*"));
					Constraint currC = factory.fullTextSearch(
							source.getSelectorName(), null, soTmp);
					constraint = CommonsJcrUtils.localAnd(factory, constraint,
							currC);
				}
			}

			Ordering order = factory.ascending(factory.propertyValue(
					source.getSelectorName(), Property.JCR_TITLE));
			Ordering[] orderings = { order };
			QueryObjectModel query = factory.createQuery(source, constraint,
					orderings, null);
			QueryResult result = query.execute();
			Row[] rows = CommonsJcrUtils.rowIteratorToArray(result.getRows());
			setViewerInput(rows);

		} catch (RepositoryException e) {
			throw new PeopleException(
					"Unable to list contacts for mailing list " + mailingList,
					e);
		}
	}

	/* Provide extraction ability */
	public Row[] getRows(String extractId) {
		return rows;
	}

	private TableViewer createTableViewer(Composite parent) {
		parent.setLayout(new GridLayout());
		PeopleVirtualTableViewer tableCmp = new PeopleVirtualTableViewer(
				parent, SWT.MULTI, colDefs);
		TableViewer tableViewer = tableCmp.getTableViewer();
		tableCmp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tableViewer.addDoubleClickListener(new PeopleJcrViewerDClickListener(
				PeopleTypes.PEOPLE_ENTITY, peopleWorkbenchService));
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
				if (CommonsJcrUtils.checkNotEmptyString(mailValue))
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

	public PeopleService getPeopleService() {
		return peopleService;
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