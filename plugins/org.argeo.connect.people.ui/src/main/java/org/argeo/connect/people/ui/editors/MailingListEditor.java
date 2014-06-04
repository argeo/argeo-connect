package org.argeo.connect.people.ui.editors;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
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

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.composites.PeopleVirtualTableViewer;
import org.argeo.connect.people.ui.editors.utils.AbstractEntityCTabEditor;
import org.argeo.connect.people.ui.exports.PeopleColumnDefinition;
import org.argeo.connect.people.ui.listeners.PeopleJcrViewerDClickListener;
import org.argeo.connect.people.ui.providers.TagLabelProvider;
import org.argeo.connect.people.ui.providers.TitleWithIconLP;
import org.argeo.connect.people.ui.utils.PeopleHtmlUtils;
import org.argeo.connect.people.ui.utils.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.PeopleJcrUtils;
import org.argeo.eclipse.ui.jcr.lists.SimpleJcrRowLabelProvider;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.UrlLauncher;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.AbstractFormPart;

/**
 * Editor page that display a mailing list
 */
public class MailingListEditor extends AbstractEntityCTabEditor implements
		PeopleNames {

	public final static String ID = PeopleUiPlugin.PLUGIN_ID
			+ ".mailingListEditor";

	// Business objects
	private Node mailingList;

	// This page objects
	private List<PeopleColumnDefinition> colDefs; // Default column
	private TableViewer membersViewer;
	private Text filterTxt;
	private Row[] rows;

	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		mailingList = getNode();

		// Initialize column definition
		// Cannot be statically done to have a valid reference to the injected
		// peopleUiService
		colDefs = new ArrayList<PeopleColumnDefinition>();
		colDefs.add(new PeopleColumnDefinition(PeopleTypes.PEOPLE_ENTITY,
				Property.JCR_TITLE, PropertyType.STRING, "Display Name",
				new TitleWithIconLP(getPeopleUiService(),
						PeopleTypes.PEOPLE_ENTITY, Property.JCR_TITLE), 300));
		colDefs.add(new PeopleColumnDefinition(PeopleTypes.PEOPLE_ENTITY,
				PEOPLE_ML_INSTANCES, PropertyType.STRING, "Mailing lists",
				new SimpleJcrRowLabelProvider(PeopleTypes.PEOPLE_ENTITY,
						PEOPLE_TAGS), 300));
	}

	@Override
	protected void populateTabFolder(CTabFolder folder) {
		// The member list
		String tooltip = "Display and search members of "
				+ JcrUtils.get(mailingList, Property.JCR_TITLE);
		Composite innerPannel = addTabToFolder(folder, CTAB_COMP_STYLE,
				"Members", PeopleUiConstants.PANEL_MEMBERS, tooltip);
		createMembersList(innerPannel, mailingList);
	}

	@Override
	// TODO refactor this.
	protected void populateHeader(Composite parent) {
		try {
			parent.setLayout(new FormLayout());

			// READ ONLY PANEL
			final Composite roPanelCmp = toolkit.createComposite(parent,
					SWT.NO_FOCUS);
			PeopleUiUtils.setSwitchingFormData(roPanelCmp);
			roPanelCmp.setLayout(new GridLayout());

			// Add a label with info provided by the FilmOverviewLabelProvider
			final Label titleROLbl = toolkit.createLabel(roPanelCmp, "",
					SWT.WRAP);
			titleROLbl.setData(PeopleUiConstants.MARKUP_ENABLED, Boolean.TRUE);
			
			final ColumnLabelProvider groupTitleLP = new TagLabelProvider(
					PeopleUiConstants.LIST_TYPE_OVERVIEW_TITLE,
					getPeopleService().getBasePath(null), PeopleTypes.PEOPLE_ENTITY,
					PEOPLE_ML_INSTANCES);

			

			// EDIT PANEL
			final Composite editPanel = toolkit.createComposite(parent,
					SWT.NO_FOCUS);
			PeopleUiUtils.setSwitchingFormData(editPanel);

			// intern layout
			editPanel.setLayout(new GridLayout(1, false));
			final Text titleTxt = PeopleUiUtils.createGDText(toolkit,
					editPanel, "A title", "The title of this group", 200, 1);
			final Text descTxt = PeopleUiUtils.createGDText(toolkit, editPanel,
					"A Description", "", 400, 1);

			AbstractFormPart editPart = new AbstractFormPart() {
				public void refresh() {
					super.refresh();
					// EDIT PART
					PeopleUiUtils.refreshTextWidgetValue(titleTxt, mailingList,
							Property.JCR_TITLE);
					PeopleUiUtils.refreshTextWidgetValue(descTxt, mailingList,
							Property.JCR_DESCRIPTION);

					// READ ONLY PART
					titleROLbl.setText(groupTitleLP.getText(mailingList));
					// Manage switch
					if (CommonsJcrUtils.isNodeCheckedOutByMe(mailingList))
						editPanel.moveAbove(roPanelCmp);
					else
						editPanel.moveBelow(roPanelCmp);
					editPanel.getParent().layout();
				}
			};

			// Listeners
			PeopleUiUtils.addTxtModifyListener(editPart, titleTxt, mailingList,
					Property.JCR_TITLE, PropertyType.STRING);
			PeopleUiUtils.addTxtModifyListener(editPart, descTxt, mailingList,
					Property.JCR_DESCRIPTION, PropertyType.STRING);

			// compulsory because we broke normal life cycle while implementing
			// IManageForm
			editPart.initialize(getManagedForm());
			getManagedForm().addPart(editPart);
		} catch (Exception e) {
			// } catch (RepositoryException e) {
			throw new PeopleException("Cannot create main info section", e);
		}
	}

	public void createMembersList(Composite parent, final Node entity) {
		parent.setLayout(PeopleUiUtils.gridLayoutNoBorder());

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

		// Add life cycle management
		AbstractFormPart sPart = new AbstractFormPart() {
			public void refresh() {
				super.refresh();
				refreshFilteredList();
			}
		};
		sPart.initialize(getManagedForm());
		getManagedForm().addPart(sPart);
		addFilterListener(filterTxt, membersViewer);
		// configureAddMemberButton(sPart, addBtn, entity,
		// "Add new members to this mailing list",
		// PeopleTypes.PEOPLE_CONTACTABLE);

		refreshFilteredList();

		// Double click
		PeopleJcrViewerDClickListener ndcl = new PeopleJcrViewerDClickListener(
				PeopleTypes.PEOPLE_ENTITY, getPeopleUiService());
		membersViewer.addDoubleClickListener(ndcl);
	}

	/** Refresh the table viewer based on the free text search field */
	protected void refreshFilteredList() {
		try {
			Session session = getSession();
			QueryManager queryManager = session.getWorkspace()
					.getQueryManager();
			QueryObjectModelFactory factory = queryManager.getQOMFactory();
			Selector source = factory.selector(PeopleTypes.PEOPLE_ENTITY,
					PeopleTypes.PEOPLE_ENTITY);

			String filter = filterTxt.getText();
			String currVal = CommonsJcrUtils.get(getNode(), Property.JCR_TITLE);
			StaticOperand so = factory.literal(session.getValueFactory()
					.createValue(currVal));
			DynamicOperand dyo = factory.propertyValue(
					source.getSelectorName(), PEOPLE_ML_INSTANCES);
			Constraint constraint = factory.comparison(dyo,
					QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO, so);

			Constraint subTree = factory.descendantNode(
					source.getSelectorName(),
					getPeopleService().getBasePath(null));
			constraint = localAnd(factory, constraint, subTree);

			if (CommonsJcrUtils.checkNotEmptyString(filter)) {
				String[] strs = filter.trim().split(" ");
				for (String token : strs) {
					StaticOperand soTmp = factory.literal(session
							.getValueFactory().createValue("*" + token + "*"));
					Constraint currC = factory.fullTextSearch(
							source.getSelectorName(), null, soTmp);
					constraint = PeopleUiUtils.localAnd(factory, constraint,
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
					"Unable to list entities with static filter for tag "
							+ getNode(), e);
		}
	}

	/* Provide extraction ability */
	public Row[] getRows(String extractId) {
		return rows;
	}

	private class PrimaryMailLabelProvider extends ColumnLabelProvider {
		private static final long serialVersionUID = 1L;

		@Override
		public String getText(Object element) {
			String text = null;
			try {
				Row currRow = (Row) element;
				Node currNode = currRow.getNode(PeopleTypes.PEOPLE_ENTITY);
				text = PeopleJcrUtils.getPrimaryContactValue(currNode,
						PeopleTypes.PEOPLE_EMAIL);
			} catch (RepositoryException re) {
				throw new PeopleException(
						"unable to retrieve primary mail value for row "
								+ element, re);
			}
			return text == null ? "" : PeopleHtmlUtils.cleanHtmlString(text);
		}

		@Override
		public Image getImage(Object element) {
			return null;
		}

	}

	private TableViewer createTableViewer(Composite parent) {
		parent.setLayout(new GridLayout());
		PeopleVirtualTableViewer tableCmp = new PeopleVirtualTableViewer(
				parent, SWT.MULTI, colDefs);
		TableViewer tableViewer = tableCmp.getTableViewer();
		tableCmp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tableViewer.addDoubleClickListener(new PeopleJcrViewerDClickListener(
				PeopleTypes.PEOPLE_ENTITY, getPeopleUiService()));
		return tableViewer;
	}

	// private TableViewer createTableViewer(Composite parent) {
	// parent.setLayout(new FillLayout());
	// // Define the TableViewer
	// final Table table = toolkit.createTable(parent, SWT.VIRTUAL);
	// TableViewer viewer = new TableViewer(table);
	//
	// table.setHeaderVisible(true);
	// table.setLinesVisible(true);
	// // table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	// table.setData(PeopleUiConstants.MARKUP_ENABLED, Boolean.TRUE);
	//
	// // Entity Type Icon
	// TableViewerColumn col = ViewerUtils.createTableViewerColumn(viewer, "",
	// SWT.NONE, 25);
	// col.setLabelProvider(new TypeLabelProvider());
	//
	// // Display Name
	// col = ViewerUtils.createTableViewerColumn(viewer, "Display Name",
	// SWT.NONE, 180);
	// col.setLabelProvider(new HtmlJcrRowLabelProvider(
	// PeopleTypes.PEOPLE_ENTITY, Property.JCR_TITLE));
	//
	// // Primary mail address
	// col = ViewerUtils.createTableViewerColumn(viewer, "Primary mail",
	// SWT.NONE, 300);
	// col.setLabelProvider(new PrimaryMailLabelProvider());
	//
	// // Remove links
	// table.addSelectionListener(new HtmlListRwtAdapter());
	// col = ViewerUtils.createTableViewerColumn(viewer, "", SWT.NONE, 50);
	// col.setLabelProvider(new ColumnLabelProvider() {
	// private static final long serialVersionUID = 1L;
	//
	// @Override
	// public String getText(Object element) {
	// try {
	// Node link = ((Row) element)
	// .getNode(PeopleTypes.PEOPLE_MAILING_LIST_ITEM);
	// // get the corresponding group
	// Node person = getParentMailingList(link);
	// return PeopleHtmlUtils.getRemoveReferenceSnippetForLists(
	// link, person);
	// } catch (RepositoryException e) {
	// throw new PeopleException(
	// "Error while getting versionable parent", e);
	// }
	// }
	// });
	// return viewer;
	// }

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
		filterTxt.setMessage(PeopleUiConstants.FILTER_HELP_MSG);
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
}