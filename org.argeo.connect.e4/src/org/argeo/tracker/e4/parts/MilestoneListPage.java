package org.argeo.tracker.e4.parts;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.cms.ArgeoNames;
import org.argeo.cms.auth.CurrentUser;
import org.argeo.cms.ui.eclipse.forms.AbstractFormPart;
import org.argeo.cms.ui.eclipse.forms.IFormPart;
import org.argeo.cms.ui.eclipse.forms.IManagedForm;
import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.ConnectNames;
import org.argeo.connect.UserAdminService;
import org.argeo.connect.ui.AppWorkbenchService;
import org.argeo.connect.ui.ConnectImages;
import org.argeo.connect.ui.ConnectUiConstants;
import org.argeo.connect.ui.ConnectUiSnippets;
import org.argeo.connect.ui.ConnectWorkbenchUtils;
import org.argeo.connect.ui.parts.AskTitleDescriptionDialog;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.ColumnDefinition;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.eclipse.ui.jcr.lists.SimpleJcrNodeLabelProvider;
import org.argeo.jcr.JcrUtils;
import org.argeo.node.NodeConstants;
import org.argeo.tracker.TrackerException;
import org.argeo.tracker.TrackerNames;
import org.argeo.tracker.TrackerService;
import org.argeo.tracker.TrackerTypes;
import org.argeo.tracker.core.TrackerUtils;
import org.argeo.tracker.ui.TrackerLps;
import org.argeo.tracker.ui.TrackerUiConstants;
import org.argeo.tracker.ui.VersionComparator;
import org.argeo.tracker.ui.dialogs.ConfigureVersionWizard;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

public class MilestoneListPage extends AbstractEditorPage implements ArgeoNames {

	private final UserAdminService userAdminService;
	private final TrackerService trackerService;
	private final AppWorkbenchService appWorkbenchService;
	private final Node project;

	private TableViewer tableViewer;
	private Text filterTxt;

	public MilestoneListPage(AbstractTrackerEditor editor, String id, Node project, UserAdminService userAdminService,
			TrackerService trackerService, AppWorkbenchService appWorkbenchService) {
		super(editor, id, "Milestones");
		this.userAdminService = userAdminService;
		this.trackerService = trackerService;
		this.appWorkbenchService = appWorkbenchService;
		this.project = project;
	}

	private boolean canEdit() {
		boolean isDataAdmin = CurrentUser.isInRole(NodeConstants.ROLE_DATA_ADMIN);
		String managerId = ConnectJcrUtils.get(project, TrackerNames.TRACKER_MANAGER);
		// FIXME known issue: manager ID is uid=... where as CurrentUser own
		// role is UID=...
		boolean isManager = (EclipseUiUtils.notEmpty(managerId)
				&& (CurrentUser.getUsername().equalsIgnoreCase(managerId)) || CurrentUser.isInRole(managerId));
		return isDataAdmin || isManager;
		// return CurrentUser.isInRole(NodeConstants.ROLE_DATA_ADMIN)
		// || (EclipseUiUtils.notEmpty(managerId) &&
		// CurrentUser.isInRole(managerId));
	}

	protected void createFormContent(IManagedForm mf) {
//		ScrolledForm form = mf.getForm();
//		Composite body = form.getBody();
		ScrolledComposite form = mf.getForm();
		Composite body = new Composite(form, SWT.NONE);
		GridLayout mainLayout = new GridLayout();

		body.setLayout(mainLayout);
		Composite filterCmp = new Composite(body, SWT.NO_FOCUS);
		createFilterPart(filterCmp);
		filterCmp.setLayoutData(EclipseUiUtils.fillWidth());
		Composite tableCmp = new Composite(body, SWT.NO_FOCUS);
		appendMilestonesPart(mf, tableCmp);
		tableCmp.setLayoutData(EclipseUiUtils.fillAll());

//		form.reflow(true);
	}

	private void appendMilestonesPart(IManagedForm mf, Composite parent) {
		List<ColumnDefinition> columnDefs = new ArrayList<ColumnDefinition>();
		columnDefs.add(new ColumnDefinition(getJcrLP(Property.JCR_TITLE), "Name", 120));
		columnDefs.add(new ColumnDefinition(new TrackerLps().new DateLabelProvider(TrackerNames.TRACKER_TARGET_DATE),
				"Target Date", 120));
		columnDefs.add(new ColumnDefinition(
				new TrackerLps().new DnLabelProvider(userAdminService, TrackerNames.TRACKER_MANAGER), "Manager", 160));
		columnDefs.add(new ColumnDefinition(getJcrLP(Property.JCR_DESCRIPTION), "Description", 300));
		columnDefs.add(new ColumnDefinition(getMSIssuesLP(), "Open / All Tasks", 120));
		// columnDefs.add(new ColumnDefinition(getMSIssuesLP(false), "All
		// tasks", 120));
		columnDefs.add(new ColumnDefinition(new TrackerLps().new DateLabelProvider(ConnectNames.CONNECT_CLOSE_DATE),
				"Close Date", 120));
		if (canEdit())
			columnDefs.add(new ColumnDefinition(getEditionLP(), "", 120));

		tableViewer = TrackerUiUtils.createTableViewer(parent, SWT.SINGLE, columnDefs);
		tableViewer.setComparator(new ViewerComparator() {
			private static final long serialVersionUID = 1L;
			VersionComparator comp = new VersionComparator();

			public int compare(Viewer viewer, Object e1, Object e2) {
				Node n1 = (Node) e1;
				Node n2 = (Node) e2;
				// Last must be first: we skip 1 & 2
				return comp.compare(viewer, ConnectJcrUtils.getName(n2), ConnectJcrUtils.getName(n1));
			};
		});
		addDClickListener(tableViewer);

		AbstractFormPart part = new AbstractFormPart() {
			@Override
			public void refresh() {
				refreshViewer(filterTxt.getText());
				super.refresh();
			}
		};
		mf.addPart(part);

		if (canEdit()) {
			Table table = tableViewer.getTable();
			CmsUtils.setItemHeight(table, TrackerUiConstants.DEFAULT_ROW_HEIGHT);
			CmsUtils.markup(table);
			table.addSelectionListener(new EditionRwtAdapter(part));
		}
		refreshViewer(null);
	}

	private void refreshViewer(String filter) {
		NodeIterator nit = TrackerUtils.getOpenMilestones(project, filter);
		tableViewer.setInput(JcrUtils.nodeIteratorToList(nit).toArray(new Node[0]));
		tableViewer.refresh();
	}

	private void createFilterPart(Composite parent) {
		GridLayout layout = EclipseUiUtils.noSpaceGridLayout(new GridLayout(2, false));
		layout.horizontalSpacing = 5;
		parent.setLayout(layout);
		parent.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		filterTxt = new Text(parent, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH | SWT.ICON_CANCEL);
		filterTxt.setLayoutData(EclipseUiUtils.fillWidth());

		final Button addBtn = new Button(parent, SWT.PUSH);
		addBtn.setToolTipText("Create a milestone");
		addBtn.setImage(ConnectImages.ADD);

		filterTxt.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 8130545587125370689L;

			public void modifyText(ModifyEvent event) {
				refreshViewer(filterTxt.getText());
			}
		});

		addBtn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = -6057495212496327413L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				Session session = ConnectJcrUtils.getSession(project);
				String mainMixin = TrackerTypes.TRACKER_MILESTONE;
				String propName1 = TrackerNames.TRACKER_PROJECT_UID;
				String value1 = ConnectJcrUtils.get(project, ConnectNames.CONNECT_UID);

				String pathCreated = ConnectWorkbenchUtils.createAndConfigureEntity(addBtn.getShell(), session,
						trackerService, appWorkbenchService, mainMixin, propName1, value1);
				if (EclipseUiUtils.notEmpty(pathCreated))
					refreshViewer(filterTxt.getText());
			}
		});
	}

	// LOCAL HELPERS

	// Shorten this call
	private static ColumnLabelProvider getJcrLP(String propName) {
		return new SimpleJcrNodeLabelProvider(propName);
	}

	private ColumnLabelProvider getMSIssuesLP() {
		return new ColumnLabelProvider() {
			private static final long serialVersionUID = -998161071505982347L;

			@Override
			public String getText(Object element) {
				Node milestone = (Node) element;
				NodeIterator nit = TrackerUtils.getIssues(project, null, TrackerNames.TRACKER_MILESTONE_UID,
						ConnectJcrUtils.get(milestone, ConnectNames.CONNECT_UID), true);
				long openNb = nit.getSize();

				nit = TrackerUtils.getIssues(project, null, TrackerNames.TRACKER_MILESTONE_UID,
						ConnectJcrUtils.get(milestone, ConnectNames.CONNECT_UID), false);
				long allNb = nit.getSize();

				return openNb + " / " + allNb;
			}
		};
	}

	private static ColumnLabelProvider getEditionLP() {
		return new ColumnLabelProvider() {
			private static final long serialVersionUID = 6502008085763687925L;

			@Override
			public String getText(Object element) {
				Node category = (Node) element;
				String jcrId = ConnectJcrUtils.getIdentifier(category);

				String editHref = ConnectUiConstants.CRUD_EDIT + "/" + jcrId;
				String editLinkStr = ConnectUiSnippets.getRWTLink(editHref, ConnectUiConstants.CRUD_EDIT);

				// TODO enable deletion of referenced components
				if (TrackerUtils.getIssueNb(category, false) == 0) {
					String removeHref = ConnectUiConstants.CRUD_DELETE + "/" + jcrId;
					String removeLinkStr = ConnectUiSnippets.getRWTLink(removeHref, ConnectUiConstants.CRUD_DELETE);
					editLinkStr += "  or  " + removeLinkStr;
				}
				return editLinkStr;
			}
		};
	}

	private void addDClickListener(TableViewer tableViewer) {
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				Object element = ((IStructuredSelection) event.getSelection()).getFirstElement();
				// String jcrId = ConnectJcrUtils.getIdentifier((Node) element);
				// CommandUtils.callCommand(appWorkbenchService.getOpenEntityEditorCmdId(),
				// ConnectEditor.PARAM_JCR_ID,
				// jcrId);
				appWorkbenchService.openEntityEditor((Node) element);
			}
		});
	}

	private class EditionRwtAdapter extends SelectionAdapter {
		private static final long serialVersionUID = -7459078949241763141L;

		private IFormPart part;

		public EditionRwtAdapter(IFormPart part) {
			this.part = part;
		}

		public void widgetSelected(SelectionEvent event) {
			if (event.detail == ConnectUiConstants.MARKUP_VIEWER_HYPERLINK) {
				String string = event.text;
				String[] token = string.split("/");
				String cmdId = token[0];
				String jcrId = token[1];
				Shell shell = event.display.getActiveShell();
				boolean hasChanged = false;
				try {
					Node node = project.getSession().getNodeByIdentifier(jcrId);
					if (ConnectUiConstants.CRUD_DELETE.equals(cmdId)) {
						if (MessageDialog.openConfirm(shell, "Confirm deletion",
								"Are you sure you want to delete " + ConnectJcrUtils.get(node, Property.JCR_TITLE))) {
							node.remove();
							project.getSession().save();
							hasChanged = true;
						}
					} else if (ConnectUiConstants.CRUD_EDIT.equals(cmdId)) {
						if (node.isNodeType(TrackerTypes.TRACKER_COMPONENT)) {
							String title = ConnectJcrUtils.get(node, Property.JCR_TITLE);
							String desc = ConnectJcrUtils.get(node, Property.JCR_DESCRIPTION);
							AskTitleDescriptionDialog dialog = new AskTitleDescriptionDialog(shell, "Edit component",
									title, desc);
							if (dialog.open() == Window.OK) {
								hasChanged = ConnectJcrUtils.setJcrProperty(node, Property.JCR_TITLE,
										PropertyType.STRING, dialog.getTitle());
								hasChanged |= ConnectJcrUtils.setJcrProperty(node, Property.JCR_DESCRIPTION,
										PropertyType.STRING, dialog.getDescription());
								if (hasChanged)
									project.getSession().save();
							}
						} else if (node.isNodeType(TrackerTypes.TRACKER_VERSION)) {
							ConfigureVersionWizard wizard = new ConfigureVersionWizard(trackerService, node);
							WizardDialog dialog = new WizardDialog(shell, wizard);
							if (dialog.open() == Window.OK) {
								if (project.getSession().hasPendingChanges()) {
									project.getSession().save();
									hasChanged = true;
								}
							}
						}
					}
				} catch (RepositoryException e) {
					throw new TrackerException("Cannot " + cmdId + " with JcrId " + jcrId, e);
				}
				if (hasChanged)
					part.refresh();
			}
		}
	}
}
