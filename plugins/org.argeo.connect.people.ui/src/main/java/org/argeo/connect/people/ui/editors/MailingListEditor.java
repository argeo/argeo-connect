package org.argeo.connect.people.ui.editors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;
import javax.jcr.query.qom.StaticOperand;

import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.ui.commands.AddEntityReferenceWithPosition;
import org.argeo.connect.people.ui.providers.BasicNodeListContentProvider;
import org.argeo.connect.people.ui.utils.MailListComparator;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.eclipse.ui.utils.CommandUtils;
import org.argeo.eclipse.ui.utils.ViewerUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.UrlLauncher;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

/**
 * Editor page that display a mailing list, roughly based on the group editor
 * TODO what specific should be added
 */
public class MailingListEditor extends GroupEditor {
	// final static Log log = LogFactory.getLog(MailingListEditor.class);

	public final static String ID = PeopleUiPlugin.PLUGIN_ID
			+ ".mailingListEditor";

	private TableViewer membersViewer;
	private Text filterTxt;

	// private GroupToolkit groupToolkit;

	@Override
	protected void createToolkits() {
		// groupToolkit = new GroupToolkit(toolkit, getManagedForm(),
		// getPeopleServices(), getPeopleUiServices());
	}

	protected void populateMainInfoDetails(Composite parent) {

		// Add a button that triggers a "mailto action" with the mail of all
		// items that are currently displayed
		// in the bottom table.

		// launcher.openURL("tel:555-123456");

		final Button button = toolkit.createButton(parent, "Send grouped mail",
				SWT.PUSH);
		// button.setToolTipText(tooltip);
		button.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));

		button.addSelectionListener(new SelectionListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				UrlLauncher launcher = RWT.getClient().getService(
						UrlLauncher.class);
				launcher.openURL("mailto:someone@nowhere.org?bcc=bruno@mostar-style.net,brunosinou@gmail.com"
						+ "&subject=Hello%3F&body=RAP%20is%20awesome!");
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		super.populateMainInfoDetails(parent);

	}

	@Override
	protected void populateTabFolder(CTabFolder folder) {
		// The member list
		String tooltip = "Members of mailing list"
				+ JcrUtils.get(getEntity(), Property.JCR_TITLE);
		Composite innerPannel = addTabToFolder(folder, CTAB_COMP_STYLE,
				"Members", PeopleUiConstants.PANEL_MEMBERS, tooltip);
		createMembersPanel(innerPannel, getEntity());
	}

	public void createMembersPanel(Composite parent, final Node entity) {
		parent.setLayout(PeopleUiUtils.gridLayoutNoBorder());
		createFilterPanel(parent);
		createMembersList(parent, entity);
		parent.layout();
	}

	public void createMembersList(Composite parent, final Node entity) {
		// Maybe add more functionalities here
		// Create new button
		final Button addBtn = toolkit.createButton(parent, "Add member",
				SWT.PUSH);
		configureAddMemberButton(addBtn, entity,
				"Add a new member to this group", PeopleTypes.PEOPLE_PERSON);

		// Corresponding list
		Composite tableComp = toolkit.createComposite(parent);
		tableComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		membersViewer = createTableViewer(tableComp);

		// Add life cycle management
		final EntityAbstractFormPart sPart = new EntityAbstractFormPart() {
			public void refresh() {
				super.refresh();
				refreshFilteredList();
			}
		};
		sPart.initialize(getManagedForm());
		getManagedForm().addPart(sPart);
	}

	public void createFilterPanel(Composite parent) {
		// Text Area for the filter
		filterTxt = new Text(parent, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH
				| SWT.ICON_CANCEL);
		filterTxt.setMessage(PeopleUiConstants.FILTER_HELP_MSG);
		filterTxt.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		filterTxt.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 5003010530960334977L;

			public void modifyText(ModifyEvent event) {
				// might be better to use an asynchronous Refresh();
				refreshFilteredList();
			}
		});
	}

	protected void refreshFilteredList() {
		try {
			String filter = filterTxt.getText();
			QueryManager queryManager = getSession().getWorkspace()
					.getQueryManager();
			QueryObjectModelFactory factory = queryManager.getQOMFactory();

			Selector source = factory.selector(
					PeopleTypes.PEOPLE_MAILING_LIST_ITEM, "selector");

			// no Default Constraint
			Constraint defaultC = null;

			// Parse the String
			String[] strs = filter.trim().split(" ");
			if (strs.length == 0) {
				StaticOperand so = factory.literal(getSession()
						.getValueFactory().createValue("*"));
				defaultC = factory.fullTextSearch("selector", null, so);
			} else {
				for (String token : strs) {
					StaticOperand so = factory.literal(getSession()
							.getValueFactory().createValue("*" + token + "*"));
					Constraint currC = factory.fullTextSearch(
							source.getSelectorName(), null, so);
					if (defaultC == null)
						defaultC = currC;
					else
						defaultC = factory.and(defaultC, currC);
				}
			}
			defaultC = factory.and(defaultC, factory.descendantNode(
					source.getSelectorName(), getEntity().getPath()));
			QueryObjectModel query;
			query = factory.createQuery(source, defaultC, null, null);
			query.setLimit(PeopleConstants.QUERY_DEFAULT_LIMIT);
			QueryResult result = query.execute();
			membersViewer.setInput(JcrUtils.nodeIteratorToList(result
					.getNodes()));
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to list entity", e);
		}
	}

	private TableViewer createTableViewer(Composite parent) {
		// Composite tableCmp = toolkit.createComposite(parent, SWT.NO_FOCUS);
		// tableCmp.setLayout(PeopleUiUtils.gridLayoutNoBorder());
		parent.setLayout(new FillLayout());
		// tableCmp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// helpers to enable sorting by column
		List<String> propertiesList = new ArrayList<String>();
		List<Integer> propertyTypesList = new ArrayList<Integer>();
		MailListComparator comparator = new MailListComparator(0,
				MailListComparator.ASCENDING, getPeopleServices());

		// Define the TableViewer
		TableViewer viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);

		// Last Name
		TableViewerColumn col = ViewerUtils.createTableViewerColumn(viewer,
				"Last Name", SWT.NONE, 120);
		setLabelProvider(col, PeopleNames.PEOPLE_LAST_NAME, true);
		col.getColumn().addSelectionListener(
				getSelectionAdapter(0, comparator, viewer));
		propertiesList.add(PeopleNames.PEOPLE_LAST_NAME);
		propertyTypesList.add(PropertyType.STRING);

		// First Name
		col = ViewerUtils.createTableViewerColumn(viewer, "First Name",
				SWT.NONE, 120);
		setLabelProvider(col, PeopleNames.PEOPLE_FIRST_NAME, true);
		col.getColumn().addSelectionListener(
				getSelectionAdapter(1, comparator, viewer));
		propertiesList.add(PeopleNames.PEOPLE_FIRST_NAME);
		propertyTypesList.add(PropertyType.STRING);

		// default mail
		col = ViewerUtils.createTableViewerColumn(viewer, "Address", SWT.NONE,
				200);
		setLabelProvider(col, PeopleNames.PEOPLE_ROLE, false);
		col.getColumn().addSelectionListener(
				getSelectionAdapter(2, comparator, viewer));
		propertiesList.add(PeopleNames.PEOPLE_ROLE);
		propertyTypesList.add(PropertyType.STRING);

		final Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		viewer.setContentProvider(new BasicNodeListContentProvider());
		comparator.setPropertyList(propertiesList);
		comparator.setPropertyTypeList(propertyTypesList);
		comparator.setColumn(0);

		// getSite().setSelectionProvider(viewer);
		viewer.setComparator(comparator);

		// Context Menu
		// MenuManager menuManager = new MenuManager();
		// Menu menu = menuManager.createContextMenu(viewer.getTable());
		// menuManager.addMenuListener(new IMenuListener() {
		// public void menuAboutToShow(IMenuManager manager) {
		// contextMenuAboutToShow(manager);
		// }
		// });
		// viewer.getTable().setMenu(menu);
		// getSite().registerContextMenu(menuManager, viewer);

		// Double click
		// viewer.addDoubleClickListener(new DoubleClickListener());
		return viewer;
	}

	/* LOCAL CLASSES */
	private SelectionAdapter getSelectionAdapter(final int index,
			final MailListComparator comparator, final TableViewer viewer) {
		SelectionAdapter selectionAdapter = new SelectionAdapter() {
			private static final long serialVersionUID = -3452356616673385039L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				Table table = viewer.getTable();
				comparator.setColumn(index);
				int dir = table.getSortDirection();
				if (table.getSortColumn() == table.getColumn(index)) {
					dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
				} else {
					dir = SWT.DOWN;
				}
				table.setSortDirection(dir);
				table.setSortColumn(table.getColumn(index));
				viewer.refresh();
			}
		};
		return selectionAdapter;
	}

	// private void setLabelProvider(TableViewerColumn col, final String
	// propName) {
	// col.setLabelProvider(new ColumnLabelProvider() {
	// private static final long serialVersionUID = 6860294095146880630L;
	//
	// @Override
	// public String getText(Object element) {
	// return CommonsJcrUtils.get((Node) element, propName);
	// }
	// });
	// }

	private void setLabelProvider(TableViewerColumn col, final String propName,
			final boolean getFromRef) {
		col.setLabelProvider(new ColumnLabelProvider() {
			private static final long serialVersionUID = -1600422863655068949L;

			@Override
			public String getText(Object element) {
				try {
					Node node = (Node) element;
					if (getFromRef) {
						node = getPeopleServices().getEntityByUid(
								node.getSession(),
								node.getProperty(PeopleNames.PEOPLE_REF_UID)
										.getString());
					}
					return CommonsJcrUtils.get(node, propName);
				} catch (RepositoryException re) {
					throw new PeopleException("unable to get text for node", re);
				}
			}
		});
	}

	// ///////////////////////
	// HELPERS

	private void configureAddMemberButton(Button button, final Node targetNode,
			String tooltip, final String nodeTypeToSearch) {
		button.setToolTipText(tooltip);
		button.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));

		button.addSelectionListener(new SelectionListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				Map<String, String> params = new HashMap<String, String>();
				try {
					params.put(
							AddEntityReferenceWithPosition.PARAM_REFERENCING_JCR_ID,
							targetNode.getIdentifier());
					params.put(
							AddEntityReferenceWithPosition.PARAM_TO_SEARCH_NODE_TYPE,
							nodeTypeToSearch);

					CommandUtils.callCommand(AddEntityReferenceWithPosition.ID,
							params);
				} catch (RepositoryException e1) {
					throw new PeopleException(
							"Unable to get parent Jcr identifier", e1);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

	}
}