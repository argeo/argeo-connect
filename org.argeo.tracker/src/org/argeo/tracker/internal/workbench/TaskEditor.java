package org.argeo.tracker.internal.workbench;

import static org.argeo.eclipse.ui.EclipseUiUtils.notEmpty;
import static org.eclipse.ui.forms.widgets.TableWrapData.FILL_GRAB;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.activities.ActivitiesNames;
import org.argeo.activities.ActivitiesTypes;
import org.argeo.cms.ArgeoNames;
import org.argeo.cms.auth.CurrentUser;
import org.argeo.cms.ui.CmsEditable;
import org.argeo.cms.ui.workbench.useradmin.PickUpUserDialog;
import org.argeo.cms.ui.workbench.util.CommandUtils;
import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.ConnectNames;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.workbench.ConnectWorkbenchUtils;
import org.argeo.connect.workbench.TechnicalInfoPage;
import org.argeo.connect.workbench.commands.OpenEntityEditor;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;
import org.argeo.node.NodeConstants;
import org.argeo.tracker.TrackerException;
import org.argeo.tracker.TrackerNames;
import org.argeo.tracker.TrackerTypes;
import org.argeo.tracker.core.TrackerUtils;
import org.argeo.tracker.internal.ui.TrackerLps;
import org.argeo.tracker.internal.ui.TrackerUiUtils;
import org.argeo.tracker.internal.ui.controls.MilestoneDropDown;
import org.argeo.tracker.internal.ui.dialogs.EditFreeTextDialog;
import org.argeo.tracker.workbench.TrackerUiPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.osgi.service.useradmin.User;

/** Default editor to display and edit an issue */
public class TaskEditor extends AbstractTrackerEditor implements CmsEditable {
	private static final long serialVersionUID = -5501994143125392009L;
	// private final static Log log = LogFactory.getLog(IssueEditor.class);
	public static final String ID = TrackerUiPlugin.PLUGIN_ID + ".taskEditor";

	// Context
	private Session session;
	private Node project;
	private Node task;

	// UI Objects
	private String assignedToGroupDn;

	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
	}

	@Override
	protected void addPages() {
		// Initialise local cache to ease implementation
		task = getNode();
		session = ConnectJcrUtils.getSession(task);
		project = TrackerUtils.getRelatedProject(getAppService(), task);
		try {
			addPage(new TaskMainPage(this));

			String techPageId = TrackerUiPlugin.PLUGIN_ID + ".issueEditor.techInfoPage";
			if (CurrentUser.isInRole(NodeConstants.ROLE_ADMIN))
				addPage(new TechnicalInfoPage(this, techPageId, getNode()));
		} catch (PartInitException e) {
			throw new TrackerException("Cannot add pages for editor of " + getNode(), e);
		}
	}

	/** Overwrite to provide a specific part Name */
	protected void updatePartName() {
		String name = getIssueTitle();
		if (notEmpty(name))
			setPartName(name);
		else
			super.updatePartName();
	}

	@Override
	public Boolean canEdit() {
		return true;
	}

	@Override
	public Boolean isEditing() {
		return true;
	}

	@Override
	public void startEditing() {
	}

	@Override
	public void stopEditing() {
	}

	// Specific pages
	private class TaskMainPage extends FormPage implements ArgeoNames {
		public final static String ID = TrackerUiPlugin.PLUGIN_ID + ".issueEditor.issueMainPage";

		private Combo statusCmb;
		private Combo importanceCmb;
		private Combo priorityCmb;
		// private VersionDropDown versionDD;
		private MilestoneDropDown targetDD;
		private Link changeAssignationLk;
		private Link reporterLk;
		private Link projectLk;
		private Text descTxt;

		public TaskMainPage(FormEditor editor) {
			super(editor, ID, "Main");
		}

		protected void createFormContent(final IManagedForm mf) {
			ScrolledForm form = mf.getForm();
			Composite body = form.getBody();
			TableWrapLayout layout = new TableWrapLayout();
			body.setLayout(layout);
			appendOverviewPart(body);
			appendCommentListPart(body);
		}

		/** Creates the general section */
		private void appendOverviewPart(Composite parent) {
			FormToolkit tk = getManagedForm().getToolkit();

			final Section section = TrackerUiUtils.addFormSection(tk, parent, getIssueTitle());
			section.setLayoutData(new TableWrapData(FILL_GRAB));

			Composite body = (Composite) section.getClient();
			TableWrapLayout layout = new TableWrapLayout();
			layout.numColumns = 6;
			body.setLayout(layout);

			// 1st line: Status, Importance and Priority
			TrackerUiUtils.createFormBoldLabel(tk, body, "Status");
			statusCmb = new Combo(body, SWT.READ_ONLY);
			statusCmb.setLayoutData(new TableWrapData(FILL_GRAB));
			TrackerUiUtils.createFormBoldLabel(tk, body, "Importance");
			importanceCmb = new Combo(body, SWT.READ_ONLY);
			importanceCmb.setLayoutData(new TableWrapData(FILL_GRAB));
			importanceCmb.setItems(TrackerUtils.MAPS_ISSUE_IMPORTANCES.values().toArray(new String[0]));
			TrackerUiUtils.createFormBoldLabel(tk, body, "Priority");
			priorityCmb = new Combo(body, SWT.READ_ONLY);
			priorityCmb.setLayoutData(new TableWrapData(FILL_GRAB));
			priorityCmb.setItems(TrackerUtils.MAPS_ISSUE_PRIORITIES.values().toArray(new String[0]));

			// Assigned to
			TrackerUiUtils.createFormBoldLabel(tk, body, "Assigned to");
			changeAssignationLk = new Link(body, SWT.NONE);
			changeAssignationLk.setLayoutData(new TableWrapData(FILL_GRAB));

			// Text assignedToTxt = createBoldLT(parent, "Assigned to", "",
			// "Choose a group or person to manage this issue", 3);
			// assignedToDD = new ExistingGroupsDropDown(assignedToTxt,
			// userAdminService, true, false);

			// Reported by
			TrackerUiUtils.createFormBoldLabel(tk, body, "Reported by");
			reporterLk = new Link(body, SWT.NONE);
			TableWrapData twd = new TableWrapData(FILL_GRAB);
			twd.colspan = 3;
			reporterLk.setLayoutData(twd);

			// Project
			TrackerUiUtils.createFormBoldLabel(tk, body, "Project");
			projectLk = new Link(body, SWT.NONE);
			projectLk.setLayoutData(new TableWrapData(FILL_GRAB));
			projectLk.setText("<a>" + ConnectJcrUtils.get(project, Property.JCR_TITLE) + "</a>");
			projectLk.addSelectionListener(new SelectionAdapter() {
				private static final long serialVersionUID = 1L;

				@Override
				public void widgetSelected(SelectionEvent e) {
					CommandUtils.callCommand(getAppWorkbenchService().getOpenEntityEditorCmdId(),
							OpenEntityEditor.PARAM_JCR_ID, ConnectJcrUtils.getIdentifier(project));
				}
			});

			// Components
			// TrackerUiUtils.createFormBoldLabel(tk, body, "Components");
			// ComponentListFormPart clfp = new
			// ComponentListFormPart(getManagedForm(), body, SWT.NO_FOCUS);
			// twd = new TableWrapData(FILL_GRAB);
			// twd.colspan = 3;
			// clfp.setLayoutData(twd);

			// Target milestone
			TrackerUiUtils.createFormBoldLabel(tk, body, "Target");
			Text targetTxt = tk.createText(body, "", SWT.BORDER);
			twd = new TableWrapData(FILL_GRAB);
			twd.colspan = 3;
			targetTxt.setLayoutData(twd);
			targetDD = new MilestoneDropDown(project, targetTxt, false);

			String muid = ConnectJcrUtils.get(task, TrackerNames.TRACKER_MILESTONE_UID);
			if (EclipseUiUtils.notEmpty(muid))
				targetDD.resetMilestone(
						getTrackerService().getEntityByUid(ConnectJcrUtils.getSession(task), null, muid));

			// TODO add linked documents

			// Description
			twd = (TableWrapData) TrackerUiUtils.createFormBoldLabel(tk, body, "Description").getLayoutData();
			twd.valign = TableWrapData.TOP;
			descTxt = new Text(body, SWT.MULTI | SWT.WRAP | SWT.BORDER);
			twd = new TableWrapData(FILL_GRAB);
			twd.colspan = 5;
			twd.heightHint = 160;
			descTxt.setLayoutData(twd);

			SectionPart part = new SectionPart((Section) body.getParent()) {

				@Override
				public void refresh() {
					// TODO Prevent edition for user without sufficient rights
					String manager = getActivitiesService().getAssignedToDisplayName(task);
					refreshStatusCombo(statusCmb, task);
					if (isEditing()) {
						manager += " ~ <a>Change</a>";
					} else {

					}
					changeAssignationLk.setText(manager);
					reporterLk.setText(TrackerUtils.getCreationLabel(getUserAdminService(), task));
					statusCmb.setText(ConnectJcrUtils.get(task, ActivitiesNames.ACTIVITIES_TASK_STATUS));
					Long importance = ConnectJcrUtils.getLongValue(task, TrackerNames.TRACKER_IMPORTANCE);
					if (importance != null) {
						String strVal = importance + "";
						String iv = TrackerUtils.MAPS_ISSUE_IMPORTANCES.get(strVal);
						importanceCmb.setText(iv);
					}
					Long priority = ConnectJcrUtils.getLongValue(task, TrackerNames.TRACKER_PRIORITY);
					if (priority != null) {
						String strVal = priority + "";
						String iv = TrackerUtils.MAPS_ISSUE_PRIORITIES.get(strVal);
						priorityCmb.setText(iv);
					}
					String mileStoneUid = ConnectJcrUtils.get(task, TrackerNames.TRACKER_MILESTONE_UID);
					if (EclipseUiUtils.notEmpty(mileStoneUid))
						targetDD.resetMilestone(getAppService().getEntityByUid(session, "/", mileStoneUid));

					String desc = ConnectJcrUtils.get(task, Property.JCR_DESCRIPTION);
					descTxt.setText(desc);
					statusCmb.getParent().layout();

					super.refresh();
				}
			};
			addStatusCmbSelListener(part, statusCmb, task, ActivitiesNames.ACTIVITIES_TASK_STATUS, PropertyType.STRING);
			addLongCmbSelListener(part, importanceCmb, task, TrackerNames.TRACKER_IMPORTANCE,
					TrackerUtils.MAPS_ISSUE_IMPORTANCES);
			addLongCmbSelListener(part, priorityCmb, task, TrackerNames.TRACKER_PRIORITY,
					TrackerUtils.MAPS_ISSUE_PRIORITIES);

			addMilestoneDDFOListener(part, targetTxt, task);
			addFocusOutListener(part, descTxt, task, Property.JCR_DESCRIPTION);
			addChangeAssignListener(part, changeAssignationLk);
			getManagedForm().addPart(part);
			// addMainSectionMenu(part);

			parent.layout(true, true);
		}

		private void addChangeAssignListener(final AbstractFormPart myFormPart, final Link changeAssignationLk) {
			changeAssignationLk.addSelectionListener(new SelectionAdapter() {
				private static final long serialVersionUID = 1L;

				@Override
				public void widgetSelected(final SelectionEvent event) {
					try {
						PickUpUserDialog diag = new PickUpUserDialog(changeAssignationLk.getShell(), "Choose a group",
								getUserAdminService().getUserAdmin());
						int result = diag.open();
						if (Window.OK == result) {
							User newGroup = diag.getSelected();
							String newGroupDn = newGroup.getName();
							if (newGroupDn == null || newGroupDn.equals(assignedToGroupDn))
								return; // nothing has changed
							else {
								// Update value
								task.setProperty(ActivitiesNames.ACTIVITIES_ASSIGNED_TO, newGroupDn);
								// update cache and display.
								assignedToGroupDn = newGroupDn;
								changeAssignationLk.setText(
										getUserAdminService().getUserDisplayName(newGroupDn) + "  ~ <a>Change</a>");
								myFormPart.markDirty();
							}
						}
					} catch (RepositoryException re) {
						throw new TrackerException("Unable to change assignation for node " + task, re);
					}
				}
			});
		}

		// THE COMMENT LIST
		private Section appendCommentListPart(Composite parent) {
			FormToolkit tk = getManagedForm().getToolkit();
			Section section = TrackerUiUtils.addFormSection(tk, parent, "Comments");
			section.setLayoutData(new TableWrapData(FILL_GRAB));

			Composite body = ((Composite) section.getClient());
			body.setLayout(new TableWrapLayout());

			Composite newCommentCmp = new Composite(body, SWT.NO_FOCUS);
			TableWrapLayout layout = new TableWrapLayout();
			layout.numColumns = 2;
			newCommentCmp.setLayout(layout);

			// Add a new comment fields
			final Text newCommentTxt = new Text(newCommentCmp, SWT.MULTI | SWT.WRAP | SWT.BORDER);
			TableWrapData twd = new TableWrapData(FILL_GRAB);
			newCommentTxt.setLayoutData(twd);

			newCommentTxt.setMessage("Enter a new comment...");
			newCommentTxt.addFocusListener(new FocusListener() {
				private static final long serialVersionUID = 1L;

				@Override
				public void focusLost(FocusEvent event) {
					String currText = newCommentTxt.getText();
					if (EclipseUiUtils.isEmpty(currText)) {
						TableWrapData twd = ((TableWrapData) newCommentTxt.getLayoutData());
						twd.heightHint = SWT.DEFAULT;
						newCommentTxt.getParent().layout(true, true);
						getManagedForm().reflow(true);
					}
				}

				@Override
				public void focusGained(FocusEvent event) {
					TableWrapData twd = ((TableWrapData) newCommentTxt.getLayoutData());
					twd.heightHint = 200;
					newCommentTxt.getParent().layout(true, true);
					getManagedForm().reflow(true);
				}
			});
			Button okBtn = new Button(newCommentCmp, SWT.BORDER | SWT.PUSH | SWT.BOTTOM);
			okBtn.setLayoutData(new TableWrapData(TableWrapData.CENTER, TableWrapData.TOP));
			okBtn.setText("OK");

			// Existing comment list
			final Composite commentsCmp = new Composite(body, SWT.NO_FOCUS);
			commentsCmp.setLayout(new TableWrapLayout());
			commentsCmp.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

			SectionPart part = new SectionPart(section) {

				private ColumnLabelProvider lp = new TrackerLps().new IssueCommentOverviewLabelProvider();

				@Override
				public void refresh() {
					if (commentsCmp.isDisposed())
						return;
					CmsUtils.clear(commentsCmp);

					List<Node> comments = getComments();
					for (Node comment : comments)
						addCommentCmp(commentsCmp, lp, null, comment);

					parent.layout(true, true);
					super.refresh();
				}
			};
			part.initialize(getManagedForm());
			getManagedForm().addPart(part);
			// addListSectionMenu(part);

			okBtn.addSelectionListener(new SelectionAdapter() {
				private static final long serialVersionUID = -5295361445564398576L;

				@Override
				public void widgetSelected(SelectionEvent e) {
					String newTag = newCommentTxt.getText();
					if (EclipseUiUtils.notEmpty(newTag)) {
						Session tmpSession = null;
						try {
							// We use a new session that is saved
							String issuePath = task.getPath();
							tmpSession = task.getSession().getRepository().login();
							Node issueExt = tmpSession.getNode(issuePath);
							getTrackerService().addComment(issueExt, newTag);
							tmpSession.save();
							session.refresh(true);
						} catch (RepositoryException re) {
							throw new TrackerException("Unable to add comment " + newTag + " on " + task, re);
						} finally {
							JcrUtils.logoutQuietly(tmpSession);
						}
						part.refresh();
						// part.markDirty();
					}
					// Reset the "new comment" field
					newCommentTxt.setText("");
					// okBtn.setFocus();
					TableWrapData twd = ((TableWrapData) newCommentTxt.getLayoutData());
					twd.heightHint = SWT.DEFAULT;
					newCommentTxt.getParent().layout(true, true);
					getManagedForm().reflow(true);
				}
			});
			return section;
		}

		private void addMilestoneDDFOListener(final AbstractFormPart part, Text text, final Node task) {
			text.addFocusListener(new FocusAdapter() {
				private static final long serialVersionUID = 3699937056116569441L;

				@Override
				public void focusLost(FocusEvent event) {
					Node chosenMilestone = targetDD.getChosenMilestone();
					String muid = null;
					if (chosenMilestone != null)
						muid = ConnectJcrUtils.get(chosenMilestone, ConnectNames.CONNECT_UID);
					if (ConnectJcrUtils.setJcrProperty(task, TrackerNames.TRACKER_MILESTONE_UID, PropertyType.STRING,
							muid))
						part.markDirty();
				}
			});
		}

	}

	private List<Node> getComments() {
		List<Node> comments = new ArrayList<Node>();
		try {
			if (task.hasNode(TrackerNames.TRACKER_COMMENTS)) {
				NodeIterator nit = task.getNode(TrackerNames.TRACKER_COMMENTS).getNodes();
				// We want to have last created node first
				// TODO not reliable, enhance
				while (nit.hasNext())
					comments.add(0, nit.nextNode());
			}
		} catch (RepositoryException re) {
			throw new TrackerException("Unable retrieve comments for " + task, re);
		}
		return comments;
	}

	private void addCommentCmp(Composite parent, ColumnLabelProvider lp, AbstractFormPart formPart, Node comment) {
		// retrieve properties
		String description = ConnectJcrUtils.get(comment, Property.JCR_DESCRIPTION);

		Composite commentCmp = new Composite(parent, SWT.NO_FOCUS);
		commentCmp.setLayoutData(new TableWrapData(FILL_GRAB));
		commentCmp.setLayout(new TableWrapLayout());

		// First line
		Label overviewLabel = new Label(commentCmp, SWT.WRAP);
		overviewLabel.setLayoutData(new TableWrapData(FILL_GRAB));
		overviewLabel.setText(lp.getText(comment));
		overviewLabel.setFont(EclipseUiUtils.getBoldFont(parent));

		// Second line: description
		Label descLabel = new Label(commentCmp, SWT.WRAP);
		descLabel.setLayoutData(new TableWrapData(FILL_GRAB));
		descLabel.setText(description);

		// third line: separator
		Label sepLbl = new Label(commentCmp, SWT.HORIZONTAL | SWT.SEPARATOR);
		sepLbl.setLayoutData(new TableWrapData(FILL_GRAB));
	}

	// SECTION MENU
	private void addMainSectionMenu(SectionPart sectionPart) {
		ToolBarManager toolBarManager = TrackerUiUtils.addMenu(sectionPart.getSection());
		String tooltip = "Edit the title of this issue";
		Action action = new EditTitle(tooltip, TrackerImages.IMG_DESC_EDIT, sectionPart);
		toolBarManager.add(action);
		toolBarManager.update(true);
	}

	// MENU ACTIONS
	private class EditTitle extends Action {
		private static final long serialVersionUID = -6798429720348536525L;
		private final SectionPart sectionPart;

		private EditTitle(String name, ImageDescriptor img, SectionPart sectionPart) {
			super(name, img);
			this.sectionPart = sectionPart;
		}

		@Override
		public void run() {
			Shell currShell = sectionPart.getSection().getShell();
			EditFreeTextDialog dialog = new EditFreeTextDialog(currShell, "Update task title", task,
					Property.JCR_TITLE);
			if (dialog.open() == Window.OK) {
				String newTitle = dialog.getEditedText();
				if (EclipseUiUtils.isEmpty(newTitle)) {
					MessageDialog.openError(currShell, "Title cannot be null or empty", "Please provide a valid title");
					return;
				}
				if (ConnectJcrUtils.setJcrProperty(task, Property.JCR_TITLE, PropertyType.STRING, newTitle)) {
					sectionPart.getSection().setText(getIssueTitle());
					updatePartName();
					sectionPart.markDirty();
				}
			}
		}
	}

	// LISTENERS
	private void addStatusCmbSelListener(final AbstractFormPart part, final Combo combo, final Node entity,
			final String propName, final int propType) {
		combo.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = combo.getSelectionIndex();
				if (index != -1) {
					String selectedStatus = combo.getItem(index);
					try {
						if (getActivitiesService().updateStatus(TrackerTypes.TRACKER_TASK, task, selectedStatus,
								new ArrayList<String>()))
							part.markDirty();
					} catch (RepositoryException e1) {
						throw new TrackerException("Cannot update status to " + selectedStatus + " for " + task, e1);
					}
				}
			}
		});
	}

	private void addLongCmbSelListener(final AbstractFormPart part, final Combo combo, final Node entity,
			final String propName, final Map<String, String> map) {
		combo.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = combo.getSelectionIndex();
				if (index != -1) {
					String selectedValue = combo.getItem(index);
					String longStr = TrackerUtils.getKeyByValue(map, selectedValue);
					long value = new Long(longStr).longValue();
					if (ConnectJcrUtils.setJcrProperty(entity, propName, PropertyType.LONG, value))
						part.markDirty();
				}
			}
		});
	}

	private void addFocusOutListener(final AbstractFormPart part, final Text text, final Node entity,
			final String propName) {
		text.addFocusListener(new FocusAdapter() {
			private static final long serialVersionUID = 3699937056116569441L;

			@Override
			public void focusLost(FocusEvent event) {
				// TODO check if the value is correct
				String newValue = text.getText();
				if (ConnectJcrUtils.setJcrProperty(entity, propName, PropertyType.STRING, newValue))
					part.markDirty();
			}
		});
	}

	/** Override this to add specific rights for status change */
	protected void refreshStatusCombo(Combo combo, Node currTask) {
		List<String> values = getResourcesService().getTemplateCatalogue(session, ActivitiesTypes.ACTIVITIES_TASK,
				ActivitiesNames.ACTIVITIES_TASK_STATUS, null);
		combo.setItems(values.toArray(new String[values.size()]));
		ConnectWorkbenchUtils.refreshFormCombo(TaskEditor.this, combo, currTask,
				ActivitiesNames.ACTIVITIES_TASK_STATUS);
		combo.setEnabled(TaskEditor.this.isEditing());
	}

	private String getIssueTitle() {
		String id = ConnectJcrUtils.get(getNode(), TrackerNames.TRACKER_ID);
		String name = ConnectJcrUtils.get(getNode(), Property.JCR_TITLE);
		if (notEmpty(name)) {
			Node project = TrackerUtils.getProjectFromChild(getNode());
			String pname = ConnectJcrUtils.get(project, Property.JCR_TITLE);
			name = name + (notEmpty(pname) ? " (" + pname + ")" : "");
		}
		return "#" + id + " " + name;
	}
}
