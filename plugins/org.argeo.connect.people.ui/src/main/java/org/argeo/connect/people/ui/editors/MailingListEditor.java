package org.argeo.connect.people.ui.editors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.EquiJoinCondition;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelConstants;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;
import javax.jcr.query.qom.Source;
import javax.jcr.query.qom.StaticOperand;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.PeopleHtmlUtils;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.ui.commands.AddEntityReferenceWithPosition;
import org.argeo.connect.people.ui.commands.GetCalcExtract;
import org.argeo.connect.people.ui.extracts.ColumnDefinition;
import org.argeo.connect.people.ui.extracts.ExtractDefinition;
import org.argeo.connect.people.ui.extracts.ITableProvider;
import org.argeo.connect.people.ui.listeners.HtmlListRwtAdapter;
import org.argeo.connect.people.ui.listeners.RowViewerDoubleClickListener;
import org.argeo.connect.people.ui.providers.SimpleJcrRowLabelProvider;
import org.argeo.connect.people.ui.utils.MailListComparator;
import org.argeo.eclipse.ui.utils.CommandUtils;
import org.argeo.eclipse.ui.utils.ViewerUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

/**
 * Editor page that display a mailing list, roughly based on the group editor
 * TODO what specific should be added
 */
public class MailingListEditor extends GroupEditor implements
		ITableProvider {
	// final static Log log = LogFactory.getLog(MailingListEditor.class);

	public final static String ID = PeopleUiPlugin.PLUGIN_ID
			+ ".mailingListEditor";

	private TableViewer membersViewer;

	// private GroupToolkit groupToolkit;

	@Override
	protected void createToolkits() {
		// groupToolkit = new GroupToolkit(toolkit, getManagedForm(),
		// getPeopleServices(), getPeopleUiServices());
	}

	// protected void populateMainInfoDetails(Composite parent) {
	// super.populateMainInfoDetails(parent);
	// }

	private String getCurrentMail() {
		StringBuilder builder = new StringBuilder();
		try {
			RowIterator ri = refreshFilteredList((String) membersViewer
					.getInput());
			while (ri.hasNext()) {
				Row row = ri.nextRow();
				Node node;
				node = row.getNode(PeopleTypes.PEOPLE_MAILING_LIST_ITEM);
				builder.append(
						node.getProperty(PeopleNames.PEOPLE_ROLE).getString())
						.append(",");
			}
			return builder.toString();
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to retrieved current mails ", e);
		}
	}

	@Override
	protected void populateTabFolder(CTabFolder folder) {
		// The member list
		String tooltip = "Members of mailing list"
				+ JcrUtils.get(getEntity(), Property.JCR_TITLE);
		Composite innerPannel = addTabToFolder(folder, CTAB_COMP_STYLE,
				"Members", PeopleUiConstants.PANEL_MEMBERS, tooltip);
		membersViewer = createMembersList(innerPannel, getEntity());
	}

	@Override
	public RowIterator getRowIterator(String extractId) {
		return refreshFilteredList((String) membersViewer.getInput());
	}

	@Override
	public List<ColumnDefinition> getColumnDefinition(String extractId) {
		return ExtractDefinition.EXTRACT_SIMPLE_MAILING_LIST;
	}

	public TableViewer createMembersList(Composite parent, final Node entity) {
		parent.setLayout(PeopleUiUtils.gridLayoutNoBorder());
		// The header buttons
		Composite buttonsCmp = toolkit.createComposite(parent);
		buttonsCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		buttonsCmp.setLayout(new GridLayout(4, false));

		Text text = createFilterText(buttonsCmp);

		Button addBtn = toolkit
				.createButton(buttonsCmp, "Add member", SWT.PUSH);
		configureAddMemberButton(addBtn, entity,
				"Add new members to this mailing list",
				PeopleTypes.PEOPLE_PERSON);

		Button exportBtn = toolkit.createButton(buttonsCmp, "Export", SWT.PUSH);
		configureCallExtractButton(exportBtn,
				"Export current results as a tabular file.");

		// Add a button that triggers a "mailto action" with the mail of all
		// items that are currently displayed in the bottom table.
		final Button button = toolkit.createButton(buttonsCmp, "Mail to",
				SWT.PUSH);
		button.setToolTipText("Open a mail client with the mails of all "
				+ "members of this list that fit current search already set as BCC"
				+ " target");
		button.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));

		button.addSelectionListener(new SelectionListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				UrlLauncher launcher = RWT.getClient().getService(
						UrlLauncher.class);
				launcher.openURL("mailto:?bcc=" + getCurrentMail());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// Corresponding list
		Composite tableComp = toolkit.createComposite(parent);
		tableComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		final TableViewer tableViewer = createTableViewer(tableComp);
		tableViewer.setContentProvider(new MyContentProvider());

		// Add life cycle management
		final EntityAbstractFormPart sPart = new EntityAbstractFormPart() {
			public void refresh() {
				super.refresh();
				// refreshFilteredList((String) membersViewer.getInput());
				tableViewer.refresh();
			}
		};
		sPart.initialize(getManagedForm());
		getManagedForm().addPart(sPart);
		addFilterListener(text, tableViewer);
		tableViewer.setInput("");
		return tableViewer;
	}

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
				viewer.setInput(filterTxt.getText());
			}
		});

	}

	protected RowIterator refreshFilteredList(String filter) {
		try {
			// TODO manage duplicates
			QueryManager queryManager = getSession().getWorkspace()
					.getQueryManager();
			QueryObjectModelFactory factory = queryManager.getQOMFactory();

			Selector mainSlct = factory.selector(PeopleTypes.PEOPLE_PERSON,
					PeopleTypes.PEOPLE_PERSON);
			Selector refSlct = factory.selector(
					PeopleTypes.PEOPLE_MAILING_LIST_ITEM,
					PeopleTypes.PEOPLE_MAILING_LIST_ITEM);

			EquiJoinCondition joinCond = factory.equiJoinCondition(
					mainSlct.getSelectorName(), PeopleNames.PEOPLE_UID,
					refSlct.getSelectorName(), PeopleNames.PEOPLE_REF_UID);
			Source jointSrc = factory.join(mainSlct, refSlct,
					QueryObjectModelConstants.JCR_JOIN_TYPE_LEFT_OUTER,
					joinCond);

			// Only show items for this list
			Constraint defaultC = factory.descendantNode(
					refSlct.getSelectorName(),
					getEntity().getNode(PeopleNames.PEOPLE_MEMBERS).getPath());

			// TODO clean this
			if (filter == null)
				filter = "";

			// Parse the String
			String[] strs = filter.trim().split(" ");
			if (strs.length == 0) {
				StaticOperand so = factory.literal(getSession()
						.getValueFactory().createValue("*"));
				Constraint currC = factory.fullTextSearch(
						refSlct.getSelectorName(), null, so);
				defaultC = factory.and(defaultC, currC);
			} else {
				for (String token : strs) {
					StaticOperand so = factory.literal(getSession()
							.getValueFactory().createValue("*" + token + "*"));
					Constraint currC = factory.fullTextSearch(
							refSlct.getSelectorName(), null, so);
					if (defaultC == null)
						defaultC = currC;
					else
						defaultC = factory.and(defaultC, currC);
				}
			}
			QueryObjectModel query;
			query = factory.createQuery(jointSrc, defaultC, null, null);
			// query.setLimit(PeopleConstants.QUERY_DEFAULT_LIMIT);
			QueryResult result = query.execute();
			return result.getRows();
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to list entity", e);
		}
	}

	private TableViewer createTableViewer(Composite parent) {
		parent.setLayout(new FillLayout());
		MailListComparator comparator = new MailListComparator();
		// Define the TableViewer
		TableViewer viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);

		final Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);

		// Last Name
		TableViewerColumn col = ViewerUtils.createTableViewerColumn(viewer,
				"Last Name", SWT.NONE, 120);
		col.setLabelProvider(new SimpleJcrRowLabelProvider(
				PeopleTypes.PEOPLE_PERSON, PeopleNames.PEOPLE_LAST_NAME));
		col.getColumn().addSelectionListener(
				getSelectionAdapter(0, PropertyType.STRING,
						PeopleTypes.PEOPLE_PERSON,
						PeopleNames.PEOPLE_LAST_NAME, comparator, viewer));

		// First Name
		col = ViewerUtils.createTableViewerColumn(viewer, "First Name",
				SWT.NONE, 120);
		col.setLabelProvider(new SimpleJcrRowLabelProvider(
				PeopleTypes.PEOPLE_PERSON, PeopleNames.PEOPLE_FIRST_NAME));
		col.getColumn().addSelectionListener(
				getSelectionAdapter(1, PropertyType.STRING,
						PeopleTypes.PEOPLE_PERSON,
						PeopleNames.PEOPLE_FIRST_NAME, comparator, viewer));

		// default mail
		col = ViewerUtils.createTableViewerColumn(viewer, "Address", SWT.NONE,
				200);
		col.setLabelProvider(new SimpleJcrRowLabelProvider(
				PeopleTypes.PEOPLE_MAILING_LIST_ITEM, PeopleNames.PEOPLE_ROLE));
		col.getColumn().addSelectionListener(
				getSelectionAdapter(2, PropertyType.STRING,
						PeopleTypes.PEOPLE_MAILING_LIST_ITEM,
						PeopleNames.PEOPLE_ROLE, comparator, viewer));

		// Remove links
		table.addSelectionListener(new HtmlListRwtAdapter());
		col = ViewerUtils.createTableViewerColumn(viewer, "", SWT.NONE, 50);
		col.setLabelProvider(new ColumnLabelProvider() {
			private static final long serialVersionUID = 1L;

			@Override
			public String getText(Object element) {
				try {
					Node link = ((Row) element)
							.getNode(PeopleTypes.PEOPLE_MAILING_LIST_ITEM);
					// get the corresponding group
					Node person = link.getParent().getParent();
					return PeopleHtmlUtils.getRemoveReferenceSnippetForLists(
							link, person);
				} catch (RepositoryException e) {
					throw new PeopleException(
							"Error while getting versionable parent", e);
				}
			}
		});

		// IMPORTANT: initialize comparator before setting it
		comparator.setColumn(PropertyType.STRING, PeopleTypes.PEOPLE_PERSON,
				PeopleNames.PEOPLE_LAST_NAME);
		viewer.setComparator(comparator);

		// Double click

		RowViewerDoubleClickListener ndcl = new RowViewerDoubleClickListener(
				getPeopleUiService(), PeopleTypes.PEOPLE_PERSON);
		viewer.addDoubleClickListener(ndcl);
		return viewer;
	}

	/* LOCAL CLASSES */
	private SelectionAdapter getSelectionAdapter(final int index,
			final int propertyType, final String selectorName,
			final String propertyName, final MailListComparator comparator,
			final TableViewer viewer) {
		SelectionAdapter selectionAdapter = new SelectionAdapter() {
			private static final long serialVersionUID = -3452356616673385039L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				Table table = viewer.getTable();
				comparator.setColumn(propertyType, selectorName, propertyName);
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

	/**
	 * Specific content provider for this Part
	 */
	private class MyContentProvider implements IStructuredContentProvider {
		private static final long serialVersionUID = 1L;
		private String filter;

		public void dispose() {
		}

		/** Expects a filter text as a new input */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			filter = (String) newInput;
			if (newInput != null)
				viewer.refresh();
		}

		public Object[] getElements(Object arg0) {
			// TODO support multiple node types.
			RowIterator ri = refreshFilteredList(filter);
			// FIXME will not work for big resultset
			Object[] result = new Object[(int) ri.getSize()];
			int i = 0;
			while (ri.hasNext()) {
				result[i] = ri.nextRow();
				i++;
			}
			return result;
		}
	}

	// private void setLabelProvider(TableViewerColumn col, final String
	// propName,
	// final boolean getFromRef) {
	// col.setLabelProvider( {
	// private static final long serialVersionUID = -1600422863655068949L;
	//
	// @Override
	// public String getText(Object element) {
	// try {
	// Node node = (Node) element;
	// if (getFromRef) {
	// node = getPeopleServices().getEntityByUid(
	// node.getSession(),
	// node.getProperty(PeopleNames.PEOPLE_REF_UID)
	// .getString());
	// }
	// return CommonsJcrUtils.get(node, propName);
	// } catch (RepositoryException re) {
	// throw new PeopleException("unable to get text for node", re);
	// }
	// }
	// });
	// }

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
					params.put(AddEntityReferenceWithPosition.PARAM_DIALOG_ID,
							PeopleUiConstants.DIALOG_ADD_ML_MEMBERS);
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

	private void configureCallExtractButton(Button button, String tooltip) {
		button.setToolTipText(tooltip);
		button.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
		button.addSelectionListener(new SelectionListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				Map<String, String> params = new HashMap<String, String>();
				// params.put(
				// GetCalcExtract.PARAM_EXTACT_ID,
				// "");
				CommandUtils.callCommand(GetCalcExtract.ID, params);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

	}
}