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

import org.argeo.cms.ui.CmsEditable;
import org.argeo.cms.ui.workbench.useradmin.PickUpUserDialog;
import org.argeo.cms.ui.workbench.util.CommandUtils;
import org.argeo.cms.util.CmsUtils;
import org.argeo.cms.util.UserAdminUtils;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.workbench.ConnectWorkbenchUtils;
import org.argeo.connect.workbench.TechnicalInfoPage;
import org.argeo.connect.workbench.commands.OpenEntityEditor;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.tracker.TrackerException;
import org.argeo.tracker.TrackerNames;
import org.argeo.tracker.TrackerTypes;
import org.argeo.tracker.core.TrackerUtils;
import org.argeo.tracker.internal.ui.TrackerImages;
import org.argeo.tracker.internal.ui.TrackerUiConstants;
import org.argeo.tracker.internal.ui.TrackerUiUtils;
import org.argeo.tracker.internal.ui.controls.MilestoneDropDown;
import org.argeo.tracker.internal.ui.controls.TagListFormPart;
import org.argeo.tracker.internal.ui.dialogs.EditFreeTextDialog;
import org.argeo.tracker.workbench.TrackerUiPlugin;
import org.argeo.activities.ActivitiesNames;
import org.argeo.cms.ArgeoNames;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
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
public class IssueEditor extends AbstractTrackerEditor implements CmsEditable {
	private static final long serialVersionUID = -5501994143125392009L;
	// private final static Log log = LogFactory.getLog(IssueEditor.class);
	public static final String ID = TrackerUiPlugin.PLUGIN_ID + ".issueEditor";

	// Context
	private Session session;
	private Node issue;
	private Node project;

	// local cache
	private String assignedToGroupDn;
	// private boolean isBeingEdited;
	// private List<String> hiddenItemIds;
	// private List<String> modifiedPaths = new ArrayList<String>();

	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		// issue = getNode();
	}

	@Override
	protected void addPages() {
		// Initialise the nodes
		issue = getNode();
		session = ConnectJcrUtils.getSession(issue);
		project = TrackerUtils.getProjectFromChild(issue);
		try {
			IssueMainPage issueMainPage = new IssueMainPage(this);
			addPage(issueMainPage);

			TechnicalInfoPage techInfoPage = new TechnicalInfoPage(this,
					TrackerUiPlugin.PLUGIN_ID + ".issueEditor.techInfoPage", getNode());
			addPage(techInfoPage);
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
	private class IssueMainPage extends FormPage implements ArgeoNames {
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

		public IssueMainPage(FormEditor editor) {
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
			TrackerUiUtils.createFormBoldLabel(tk, body, "Components");
			ComponentListFormPart clfp = new ComponentListFormPart(getManagedForm(), body, SWT.NO_FOCUS);
			twd = new TableWrapData(FILL_GRAB);
			twd.colspan = 3;
			clfp.setLayoutData(twd);

			// Target milestone
			TrackerUiUtils.createFormBoldLabel(tk, body, "Target");
			Text targetTxt = tk.createText(body, "", SWT.BORDER);
			targetTxt.setLayoutData(new TableWrapData(FILL_GRAB));
			targetDD = new MilestoneDropDown(TrackerUtils.getProjectFromChild(issue), targetTxt);

			// Versions
			TrackerUiUtils.createFormBoldLabel(tk, body, "Versions");
			VersionListFormPart vlfp = new VersionListFormPart(getManagedForm(), body, SWT.NO_FOCUS);
			twd = new TableWrapData(FILL_GRAB);
			twd.colspan = 3;
			vlfp.setLayoutData(twd);

			// TODO add linked documents

			// Description
			twd = (TableWrapData) TrackerUiUtils.createFormBoldLabel(tk, body, "Description").getLayoutData();
			twd.valign = TableWrapData.TOP;
			descTxt = new Text(body, SWT.MULTI | SWT.WRAP | SWT.BORDER);
			twd = new TableWrapData(FILL_GRAB);
			twd.colspan = 5;
			twd.heightHint = 160;
			descTxt.setLayoutData(twd);

			// create form part (controller)
			SectionPart part = new SectionPart((Section) body.getParent()) {

				@Override
				public void refresh() {
					// TODO Prevent edition for user that do not have sufficient
					// rights

					String manager = getActivitiesService().getAssignedToDisplayName(issue);
					refreshStatusCombo(statusCmb, issue);
					if (isEditing()) {
						manager += " ~ <a>Change</a>";
					} else {

					}
					changeAssignationLk.setText(manager);
					reporterLk.setText(TrackerUtils.getCreationLabel(getUserAdminService(), issue));
					statusCmb.setText(ConnectJcrUtils.get(issue, ActivitiesNames.ACTIVITIES_TASK_STATUS));
					Long importance = ConnectJcrUtils.getLongValue(issue, TrackerNames.TRACKER_IMPORTANCE);
					if (importance != null) {
						String strVal = importance + "";
						String iv = TrackerUtils.MAPS_ISSUE_IMPORTANCES.get(strVal);
						importanceCmb.setText(iv);
					}
					Long priority = ConnectJcrUtils.getLongValue(issue, TrackerNames.TRACKER_PRIORITY);
					if (priority != null) {
						String strVal = priority + "";
						String iv = TrackerUtils.MAPS_ISSUE_PRIORITIES.get(strVal);
						priorityCmb.setText(iv);
					}
					String target = ConnectJcrUtils.get(issue, TrackerNames.TRACKER_TARGET_ID);
					targetDD.reset(target);

					String desc = ConnectJcrUtils.get(issue, Property.JCR_DESCRIPTION);
					descTxt.setText(desc);
					statusCmb.getParent().layout();

					super.refresh();
				}
			};
			addStatusCmbSelListener(part, statusCmb, issue, ActivitiesNames.ACTIVITIES_TASK_STATUS,
					PropertyType.STRING);
			addLongCmbSelListener(part, importanceCmb, issue, TrackerNames.TRACKER_IMPORTANCE,
					TrackerUtils.MAPS_ISSUE_IMPORTANCES);
			addLongCmbSelListener(part, priorityCmb, issue, TrackerNames.TRACKER_PRIORITY,
					TrackerUtils.MAPS_ISSUE_PRIORITIES);
			addFocusOutListener(part, targetTxt, issue, TrackerNames.TRACKER_TARGET_ID);
			addFocusOutListener(part, descTxt, issue, Property.JCR_DESCRIPTION);
			addChangeAssignListener(part, changeAssignationLk);
			getManagedForm().addPart(part);
			addMainSectionMenu(part);
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
								issue.setProperty(ActivitiesNames.ACTIVITIES_ASSIGNED_TO, newGroupDn);
								// update cache and display.
								assignedToGroupDn = newGroupDn;
								changeAssignationLk.setText(
										getUserAdminService().getUserDisplayName(newGroupDn) + "  ~ <a>Change</a>");
								myFormPart.markDirty();
							}
						}
					} catch (RepositoryException re) {
						throw new TrackerException("Unable to change assignation for node " + issue, re);
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
				@Override
				public void refresh() {
					if (commentsCmp.isDisposed())
						return;
					CmsUtils.clear(commentsCmp);

					List<Node> comments = getComments();
					for (Node comment : comments)
						addCommentCmp(commentsCmp, null, comment);

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
						try {
							getTrackerService().addComment(issue, newTag);
						} catch (RepositoryException re) {
							throw new TrackerException("Unable to add comment " + newTag + " on " + issue, re);
						}
						part.refresh();
						part.markDirty();
					}
					// Reset the "new comment" field
					newCommentTxt.setText("");
					GridData gd = ((GridData) newCommentTxt.getLayoutData());
					gd.heightHint = SWT.DEFAULT;
					newCommentTxt.getParent().layout(true, true);
					getManagedForm().reflow(true);
				}
			});
			return section;
		}
	}

	private List<Node> getComments() {
		List<Node> comments = new ArrayList<Node>();
		try {
			if (issue.hasNode(TrackerNames.TRACKER_COMMENTS)) {
				NodeIterator nit = issue.getNode(TrackerNames.TRACKER_COMMENTS).getNodes();
				// We want to have last created node first
				// TODO not reliable, enhance
				while (nit.hasNext())
					comments.add(0, nit.nextNode());
			}
		} catch (RepositoryException re) {
			throw new TrackerException("Unable retrieve comments for " + issue, re);
		}
		return comments;
	}

	private void addCommentCmp(Composite parent, AbstractFormPart formPart, Node comment) {
		// retrieve properties
		String createdBy = ConnectJcrUtils.get(comment, Property.JCR_CREATED_BY);
		String createdOn = ConnectJcrUtils.getDateFormattedAsString(comment, Property.JCR_CREATED,
				TrackerUiConstants.simpleDateTimeFormat);
		String lastUpdatedBy = ConnectJcrUtils.get(comment, Property.JCR_LAST_MODIFIED_BY);
		String lastUpdatedOn = ConnectJcrUtils.getDateFormattedAsString(comment, Property.JCR_LAST_MODIFIED,
				TrackerUiConstants.simpleDateTimeFormat);
		String description = ConnectJcrUtils.get(comment, Property.JCR_DESCRIPTION);

		Composite commentCmp = new Composite(parent, SWT.NO_FOCUS);
		commentCmp.setLayoutData(new TableWrapData(FILL_GRAB));
		commentCmp.setLayout(new TableWrapLayout());

		// First line
		Label overviewLabel = new Label(commentCmp, SWT.WRAP);
		overviewLabel.setLayoutData(new TableWrapData(FILL_GRAB));
		StringBuilder builder = new StringBuilder();
		builder.append(UserAdminUtils.getUserLocalId(createdBy)).append(" on ").append(createdOn);
		if (EclipseUiUtils.notEmpty(lastUpdatedBy))
			builder.append(" (last edited by ").append(UserAdminUtils.getUserLocalId(lastUpdatedBy)).append(" on ")
					.append(lastUpdatedOn).append(")");
		overviewLabel.setText(builder.toString());

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
			EditFreeTextDialog dialog = new EditFreeTextDialog(currShell, "Update issue title", issue,
					Property.JCR_TITLE);
			if (dialog.open() == Window.OK) {
				String newTitle = dialog.getEditedText();
				if (EclipseUiUtils.isEmpty(newTitle)) {
					MessageDialog.openError(currShell, "Title cannot be null or empty", "Please provide a valid title");
					return;
				}
				if (ConnectJcrUtils.setJcrProperty(issue, Property.JCR_TITLE, PropertyType.STRING, newTitle)) {
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
						if (getActivitiesService().updateStatus(TrackerTypes.TRACKER_ISSUE, issue, selectedStatus,
								new ArrayList<String>()))
							part.markDirty();
					} catch (RepositoryException e1) {
						throw new TrackerException("Cannot update status to " + selectedStatus + " for " + issue, e1);
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
		text.addFocusListener(new FocusListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void focusLost(FocusEvent event) {
				// TODO check if the value is correct
				String newValue = text.getText();
				if (ConnectJcrUtils.setJcrProperty(entity, propName, PropertyType.STRING, newValue))
					part.markDirty();
			}

			@Override
			public void focusGained(FocusEvent event) {
			}
		});
	}

	/** Override this to add specific rights for status change */
	protected void refreshStatusCombo(Combo combo, Node currTask) {
		List<String> values = getResourcesService().getTemplateCatalogue(session, TrackerTypes.TRACKER_ISSUE,
				ActivitiesNames.ACTIVITIES_TASK_STATUS, null);
		combo.setItems(values.toArray(new String[values.size()]));
		ConnectWorkbenchUtils.refreshFormCombo(IssueEditor.this, combo, currTask,
				ActivitiesNames.ACTIVITIES_TASK_STATUS);
		combo.setEnabled(IssueEditor.this.isEditing());
	}

	private String getIssueTitle() {
		String name = ConnectJcrUtils.get(getNode(), Property.JCR_TITLE);
		if (notEmpty(name)) {
			Node project = TrackerUtils.getProjectFromChild(getNode());
			String pname = ConnectJcrUtils.get(project, Property.JCR_TITLE);
			name = name + (notEmpty(pname) ? " (" + pname + ")" : "");
		}
		return name;
	}

	private class VersionListFormPart extends TagListFormPart {
		private static final long serialVersionUID = -6810842097242400473L;

		public VersionListFormPart(IManagedForm form, Composite parent, int style) {
			super(IssueEditor.this, form, parent, style, getNode(), TrackerNames.TRACKER_VERSION_IDS);
		}

		@Override
		protected List<String> getFilteredValues(String filter) {
			NodeIterator nit = TrackerUtils.getAllVersions(project, filter);
			List<String> values = new ArrayList<String>();
			while (nit.hasNext()) {
				values.add(getTagKey(nit.nextNode()));
			}
			return values;
		}

		@Override
		protected Node createTag(String tagKey) throws RepositoryException {
			return getTrackerService().createVersion(project, tagKey, null, null, null);
		}

		protected String getTagKey(Node tagDefinition) {
			return ConnectJcrUtils.get(tagDefinition, TrackerNames.TRACKER_ID);
		}

		protected void callOpenEditor(String tagKey) {
			Node node = TrackerUtils.getVersionById(project, tagKey);
			if (node != null)
				CommandUtils.callCommand(getAppWorkbenchService().getOpenEntityEditorCmdId(),
						OpenEntityEditor.PARAM_JCR_ID, ConnectJcrUtils.getIdentifier(node));
		}

	}

	private class ComponentListFormPart extends TagListFormPart {
		private static final long serialVersionUID = -5392419310704426378L;

		public ComponentListFormPart(IManagedForm form, Composite parent, int style) {
			super(IssueEditor.this, form, parent, style, getNode(), TrackerNames.TRACKER_COMPONENT_IDS);
		}

		@Override
		protected List<String> getFilteredValues(String filter) {
			List<String> values = new ArrayList<String>();
			NodeIterator nit = TrackerUtils.getComponents(project, filter);
			while (nit != null && nit.hasNext()) {
				values.add(getTagKey(nit.nextNode()));
			}
			return values;
		}

		@Override
		protected Node createTag(String tagKey) throws RepositoryException {
			return getTrackerService().createComponent(project, tagKey, tagKey, null);
		}

		protected String getTagKey(Node tagDefinition) {
			return ConnectJcrUtils.get(tagDefinition, TrackerNames.TRACKER_ID);
		}

		protected void callOpenEditor(String tagKey) {
			Node node = TrackerUtils.getComponentById(project, tagKey);
			if (node != null)
				CommandUtils.callCommand(getAppWorkbenchService().getOpenEntityEditorCmdId(),
						OpenEntityEditor.PARAM_JCR_ID, ConnectJcrUtils.getIdentifier(node));
		}
	}
}
