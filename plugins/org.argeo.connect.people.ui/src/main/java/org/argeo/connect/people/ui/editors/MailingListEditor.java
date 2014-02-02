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
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.commands.AddEntityReference;
import org.argeo.connect.people.ui.commands.GetCalcExtract;
import org.argeo.connect.people.ui.extracts.ExtractDefinition;
import org.argeo.connect.people.ui.extracts.ITableProvider;
import org.argeo.connect.people.ui.listeners.HtmlListRwtAdapter;
import org.argeo.connect.people.ui.listeners.PeopleJcrViewerDClickListener;
import org.argeo.connect.people.ui.providers.PeopleImageProvider;
import org.argeo.connect.people.ui.utils.PeopleHtmlUtils;
import org.argeo.connect.people.ui.utils.PeopleUiUtils;
import org.argeo.connect.people.ui.utils.RowViewerComparator;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.PeopleJcrUtils;
import org.argeo.eclipse.ui.jcr.lists.ColumnDefinition;
import org.argeo.eclipse.ui.jcr.lists.SimpleJcrRowLabelProvider;
import org.argeo.eclipse.ui.utils.CommandUtils;
import org.argeo.eclipse.ui.utils.ViewerUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ILazyContentProvider;
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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.AbstractFormPart;

/**
 * Editor page that display a mailing list, roughly based on the group editor
 * TODO what specific should be added
 */
public class MailingListEditor extends GroupEditor implements ITableProvider {

	public final static String ID = PeopleUiPlugin.PLUGIN_ID
			+ ".mailingListEditor";

	private TableViewer membersViewer;

	private Text filterTxt;

	@Override
	protected void createToolkits() {
	}

	private String getCurrentMails() {
		StringBuilder builder = new StringBuilder();
		try {
			RowIterator ri = refreshFilteredList(filterTxt.getText());
			while (ri.hasNext()) {
				Row row = ri.nextRow();
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

	@Override
	protected void populateTabFolder(CTabFolder folder) {
		// The member list
		String tooltip = "Members of mailing list"
				+ JcrUtils.get(getNode(), Property.JCR_TITLE);
		Composite innerPannel = addTabToFolder(folder, CTAB_COMP_STYLE,
				"Members", PeopleUiConstants.PANEL_MEMBERS, tooltip);
		membersViewer = createMembersList(innerPannel, getNode());

		// Double click
		PeopleJcrViewerDClickListener ndcl = new PeopleJcrViewerDClickListener(
				PeopleTypes.PEOPLE_ENTITY, getOpenEntityEditorCmdId());
		membersViewer.addDoubleClickListener(ndcl);
	}

	@Override
	public RowIterator getRowIterator(String extractId) {
		return refreshFilteredList(filterTxt.getText());
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

		filterTxt = createFilterText(buttonsCmp);

		Button addBtn = toolkit
				.createButton(buttonsCmp, "Add member", SWT.PUSH);

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

		button.addSelectionListener(new SelectionAdapter() {
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

		final TableViewer tableViewer = createTableViewer(tableComp);
		tableViewer.setContentProvider(new LazyJcrContentProvider(tableViewer));

		// Add life cycle management
		AbstractFormPart sPart = new AbstractFormPart() {
			public void refresh() {
				super.refresh();
				// refreshFilteredList((String) membersViewer.getInput());
				tableViewer.setInput(refreshFilteredList(""));
				tableViewer.refresh();
			}
		};
		sPart.initialize(getManagedForm());
		getManagedForm().addPart(sPart);
		addFilterListener(filterTxt, tableViewer);
		configureAddMemberButton(sPart, addBtn, entity,
				"Add new members to this mailing list",
				PeopleTypes.PEOPLE_CONTACTABLE);

		tableViewer.setInput(refreshFilteredList(""));
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
				viewer.setInput(refreshFilteredList(filterTxt.getText()));
			}
		});

	}

	protected RowIterator refreshFilteredList(String filter) {
		try {
			QueryManager queryManager = getSession().getWorkspace()
					.getQueryManager();
			QueryObjectModelFactory factory = queryManager.getQOMFactory();

			Selector mainSlct = factory.selector(PeopleTypes.PEOPLE_ENTITY,
					PeopleTypes.PEOPLE_ENTITY);
			Selector refSlct = factory.selector(
					PeopleTypes.PEOPLE_MAILING_LIST_ITEM,
					PeopleTypes.PEOPLE_MAILING_LIST_ITEM);

			EquiJoinCondition joinCond = factory.equiJoinCondition(
					refSlct.getSelectorName(), PeopleNames.PEOPLE_REF_UID,
					mainSlct.getSelectorName(), PeopleNames.PEOPLE_UID);
			Source jointSrc = factory.join(refSlct, mainSlct,
					QueryObjectModelConstants.JCR_JOIN_TYPE_INNER, joinCond);

			// Only show items for this list
			Constraint defaultC = factory.descendantNode(
					refSlct.getSelectorName(),
					getNode().getNode(PeopleNames.PEOPLE_MEMBERS).getPath());

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
							mainSlct.getSelectorName(), null, so);
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

	/** Recursively retrieve the parent Mailing list **/
	private Node getParentMailingList(Node node) throws RepositoryException {
		if (node.isNodeType(PeopleTypes.PEOPLE_MAILING_LIST))
			return node;
		else
			return getParentMailingList(node.getParent());
	}

	private TableViewer createTableViewer(Composite parent) {
		parent.setLayout(new FillLayout());
		RowViewerComparator comparator = new RowViewerComparator();
		// Define the TableViewer
		final Table table = toolkit.createTable(parent, SWT.VIRTUAL);

		TableViewer viewer = new TableViewer(table);

		// new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
		// | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		// final Table table = viewer.getTable();

		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setData(PeopleUiConstants.MARKUP_ENABLED, Boolean.TRUE);

		// Entity Type Icon
		TableViewerColumn col = ViewerUtils.createTableViewerColumn(viewer, "",
				SWT.NONE, 25);
		// column.setEditingSupport(new SelectedEditingSupport(viewer));
		col.setLabelProvider(new ColumnLabelProvider() {
			private static final long serialVersionUID = 1L;
			private PeopleImageProvider imageProvider = new PeopleImageProvider();

			@Override
			public String getText(Object element) {
				return null;
			}

			@Override
			public Image getImage(Object element) {
				try {
					Row currRow = (Row) element;
					Node currNode = currRow.getNode(PeopleTypes.PEOPLE_ENTITY);
					return imageProvider.getDefaultIconByType(currNode);
				} catch (RepositoryException re) {
					// Error while retrieving image, silent
				}
				return null;
			}

		});

		// Display Name
		col = ViewerUtils.createTableViewerColumn(viewer, "Display Name",
				SWT.NONE, 180);
		col.setLabelProvider(new HtmlJcrRowLabelProvider(
				PeopleTypes.PEOPLE_ENTITY, Property.JCR_TITLE));

		// FIXME: does not work with virtual table
		// col.getColumn().addSelectionListener(
		// getSelectionAdapter(1, PropertyType.STRING,
		// PeopleTypes.PEOPLE_ENTITY, Property.JCR_TITLE,
		// comparator, viewer));

		// Primary mail address
		col = ViewerUtils.createTableViewerColumn(viewer, "Primary mail",
				SWT.NONE, 300);
		col.setLabelProvider(new ColumnLabelProvider() {
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
				return text == null ? "" : PeopleHtmlUtils
						.cleanHtmlString(text);
			}

			@Override
			public Image getImage(Object element) {
				return null;
			}

		});

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
					Node person = getParentMailingList(link);
					return PeopleHtmlUtils.getRemoveReferenceSnippetForLists(
							link, person);
				} catch (RepositoryException e) {
					throw new PeopleException(
							"Error while getting versionable parent", e);
				}
			}
		});

		// IMPORTANT: initialize comparator before setting it
		comparator.setColumn(PropertyType.STRING, PeopleTypes.PEOPLE_ENTITY,
				Property.JCR_TITLE);
		viewer.setComparator(comparator);
		return viewer;
	}

	private class LazyJcrContentProvider implements ILazyContentProvider {

		private static final long serialVersionUID = 2329346740515876042L;
		private TableViewer viewer;
		// private boolean isScrollable;
		private RowIterator ri;

		private List<Object> buffer = new ArrayList<Object>();
		private int pageSize = 50;

		public LazyJcrContentProvider(TableViewer viewer) {
			this.viewer = viewer;
		}

		public void dispose() {
			// try {
			// // resultSet.close();
			// } catch (SQLException e) {
			// // silent
			// }
		}

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
			if (newInput == null)
				return;

			ri = (RowIterator) newInput;
			TableViewer viewer = (TableViewer) v;
			buffer.clear();

			int i = 0;
			while (ri.hasNext() && i < pageSize) {
				buffer.add(ri.nextRow());
				i++;
			}
			viewer.setItemCount(buffer.size());
		}

		public void updateElement(int index) {
			int itemCount = viewer.getTable().getItemCount();
			if (index == (itemCount - 1) && ri.hasNext()) {

				int i = 0;
				// Update
				while (i < pageSize && ri.hasNext()) {
					buffer.add(ri.nextRow());
					i++;
				}
				viewer.setItemCount(itemCount + i);
			}
			viewer.replace(buffer.get(index), index);
		}
	}

	/* LOCAL CLASSES */
	/** Override to remove "&" character */
	private class HtmlJcrRowLabelProvider extends SimpleJcrRowLabelProvider {
		private static final long serialVersionUID = -7758839225650525190L;

		public HtmlJcrRowLabelProvider(String selectorName, String propertyName) {
			super(selectorName, propertyName);
		}

		@Override
		public String getText(Object element) {
			String text = super.getText(element);
			return PeopleHtmlUtils.cleanHtmlString(text);
		}

	}

	// ///////////////////////
	// HELPERS

	private void configureAddMemberButton(final AbstractFormPart part,
			Button button, final Node targetNode, String tooltip,
			final String nodeTypeToSearch) {
		button.setToolTipText(tooltip);
		button.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));

		button.addSelectionListener(new SelectionListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				Map<String, String> params = new HashMap<String, String>();
				try {
					params.put(AddEntityReference.PARAM_REFERENCING_JCR_ID,
							targetNode.getIdentifier());
					params.put(AddEntityReference.PARAM_TO_SEARCH_NODE_TYPE,
							nodeTypeToSearch);
					params.put(AddEntityReference.PARAM_DIALOG_ID,
							PeopleUiConstants.DIALOG_ADD_ML_MEMBERS);
					CommandUtils.callCommand(AddEntityReference.ID, params);
					part.refresh();
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
				CommandUtils.callCommand(GetCalcExtract.ID, params);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}
}