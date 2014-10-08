package org.argeo.connect.people.rap.toolkits;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.rap.PeopleUiConstants;
import org.argeo.connect.people.rap.commands.AddEntityReferenceWithPosition;
import org.argeo.connect.people.rap.listeners.HtmlListRwtAdapter;
import org.argeo.connect.people.rap.providers.BasicNodeListContentProvider;
import org.argeo.connect.people.rap.providers.PersonOverviewLabelProvider;
import org.argeo.connect.people.rap.providers.RoleListLabelProvider;
import org.argeo.connect.people.rap.utils.PeopleHtmlUtils;
import org.argeo.connect.people.rap.utils.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.eclipse.ui.utils.CommandUtils;
import org.argeo.eclipse.ui.utils.ViewerUtils;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Centralizes the creation of the different form composites and controls to
 * manage people groups.
 */
public class GroupToolkit {
	// private final static Log log = LogFactory.getLog(ListToolkit.class);

	private final FormToolkit toolkit;
	private final IManagedForm form;
	private final PeopleService peopleService;

	public GroupToolkit(FormToolkit toolkit, IManagedForm form,
			PeopleService peopleService) {
		this.toolkit = toolkit;
		this.form = form;
		this.peopleService = peopleService;
	}

	/**
	 * The jobs for a person
	 */
	public void createMemberList(Composite parent, final Node entity) {
		parent.setLayout(new GridLayout());

		// Maybe add more functionalities here
		// Create new button
		final Button addBtn = toolkit.createButton(parent, "Add member",
				SWT.PUSH);
		configureAddMemberButton(addBtn, entity,
				"Add a new member to this group", PeopleTypes.PEOPLE_PERSON);

		// Corresponding list
		Composite tableComp = toolkit.createComposite(parent);
		tableComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		final TableViewer viewer = new TableViewer(tableComp);
		TableColumnLayout tableColumnLayout = createMembersTableColumns(
				tableComp, viewer);
		tableComp.setLayout(tableColumnLayout);
		PeopleUiUtils.setTableDefaultStyle(viewer, 60);

		// compulsory content provider
		viewer.setContentProvider(new BasicNodeListContentProvider());
		// try {
		// viewer.addDoubleClickListener(peopleUiService
		// .getNewNodeListDoubleClickListener(peopleService, entity
		// .getPrimaryNodeType().getName()));
		// } catch (RepositoryException re) {
		// throw new PeopleException("Error adding double click on job list",
		// re);
		// }

		// Add life cycle management
		AbstractFormPart sPart = new AbstractFormPart() {
			public void refresh() {
				super.refresh();
				try {
					addBtn.setEnabled(CommonsJcrUtils
							.isNodeCheckedOutByMe(entity));
					List<Node> members = new ArrayList<Node>();
					if (!entity.hasNode(PeopleNames.PEOPLE_MEMBERS))
						return;
					NodeIterator ni = entity
							.getNode(PeopleNames.PEOPLE_MEMBERS).getNodes();
					while (ni.hasNext()) {
						// Check if have the right type of node
						Node currMember = ni.nextNode();
						if (currMember.isNodeType(PeopleTypes.PEOPLE_MEMBER)) {
							members.add(currMember);
						}
					}
					viewer.setInput(members);
				} catch (RepositoryException re) {
					throw new PeopleException("Cannot refresh jobs list", re);
				}
			}
		};
		sPart.initialize(form);
		form.addPart(sPart);
	}

	private TableColumnLayout createMembersTableColumns(Composite parent,
			TableViewer viewer) {
		int[] bounds = { 150, 300 };
		TableColumnLayout tableColumnLayout = new TableColumnLayout();

		// Role
		TableViewerColumn col = ViewerUtils.createTableViewerColumn(viewer, "",
				SWT.LEFT, bounds[0]);
		col.setLabelProvider(new RoleListLabelProvider());
		tableColumnLayout.setColumnData(col.getColumn(), new ColumnWeightData(
				80, 20, true));

		// Person
		col = ViewerUtils.createTableViewerColumn(viewer, "", SWT.LEFT,
				bounds[1]);
		col.setLabelProvider(new PersonOverviewLabelProvider(
				PeopleUiConstants.LIST_TYPE_MEDIUM, peopleService));
		tableColumnLayout.setColumnData(col.getColumn(), new ColumnWeightData(
				200, 80, true));

		// Edit & Remove links
		viewer.getTable().addSelectionListener(new HtmlListRwtAdapter());
		col = ViewerUtils.createTableViewerColumn(viewer, "Edit/Remove links",
				SWT.NONE, 60);
		tableColumnLayout.setColumnData(col.getColumn(), new ColumnWeightData(
				40, 40, true));
		col.setLabelProvider(new ColumnLabelProvider() {
			private static final long serialVersionUID = 1L;

			@Override
			public String getText(Object element) {
				try {
					// get the corresponding person
					Node link = (Node) element;
					Node person = link.getParent().getParent();

					return PeopleHtmlUtils.getEditWithPosSnippetForLists(link,
							false, PeopleTypes.PEOPLE_PERSON)
							+ " <br />"
							+ PeopleHtmlUtils
									.getRemoveReferenceSnippetForLists(link,
											person);
				} catch (RepositoryException e) {
					throw new PeopleException(
							"Error while getting versionable parent", e);
				}

			}
		});

		return tableColumnLayout;
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