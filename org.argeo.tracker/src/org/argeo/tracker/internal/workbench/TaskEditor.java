package org.argeo.tracker.internal.workbench;

import static org.argeo.activities.ActivitiesNames.ACTIVITIES_RELATED_TO;
import static org.argeo.eclipse.ui.EclipseUiUtils.notEmpty;
import static org.eclipse.ui.forms.widgets.TableWrapData.BOTTOM;
import static org.eclipse.ui.forms.widgets.TableWrapData.FILL;
import static org.eclipse.ui.forms.widgets.TableWrapData.FILL_GRAB;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.argeo.activities.ActivitiesNames;
import org.argeo.activities.ActivitiesTypes;
import org.argeo.cms.ArgeoNames;
import org.argeo.cms.auth.CurrentUser;
import org.argeo.cms.ui.CmsEditable;
import org.argeo.cms.ui.workbench.util.CommandUtils;
import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.ConnectConstants;
import org.argeo.connect.ui.ConnectImages;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.workbench.ConnectWorkbenchUtils;
import org.argeo.connect.workbench.commands.OpenEntityEditor;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.node.NodeConstants;
import org.argeo.tracker.TrackerException;
import org.argeo.tracker.TrackerNames;
import org.argeo.tracker.TrackerTypes;
import org.argeo.tracker.core.TrackerUtils;
import org.argeo.tracker.internal.ui.TrackerUiUtils;
import org.argeo.tracker.internal.ui.dialogs.ConfigureTaskWizard;
import org.argeo.tracker.workbench.TechnicalInfoPage;
import org.argeo.tracker.workbench.TrackerUiPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
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

/** Default editor to display and edit a Tracker's task */
public class TaskEditor extends AbstractTrackerEditor implements CmsEditable {
	// private final static Log log = LogFactory.getLog(IssueEditor.class);
	private static final long serialVersionUID = 1027157523552831925L;

	public static final String ID = TrackerUiPlugin.PLUGIN_ID + ".taskEditor";

	// Context
	private Session session;
	private Node project;
	private Node task;

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

//			if (CurrentUser.isInRole(NodeConstants.ROLE_ADMIN))
//				addPage(new TechnicalInfoPage(this, ID + ".techInfoPage", getNode()));
		} catch (PartInitException e) {
			throw new TrackerException("Cannot add pages for editor of " + getNode(), e);
		}
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
		public final static String PAGE_ID = ID + ".taskMainPage";

		private Combo statusCmb;
		private Link projectLk;
		private Link milestoneLk;
		private Link dueDateLk;
		private Link reporterLk;
		private Link assignedToLk;
		private Composite relatedCmp;
		private Label descLbl;

		public TaskMainPage(FormEditor editor) {
			super(editor, PAGE_ID, "Main");
		}

		protected void createFormContent(final IManagedForm mf) {
			ScrolledForm form = mf.getForm();
			Composite body = form.getBody();
			TableWrapLayout layout = new TableWrapLayout();
			body.setLayout(layout);
			appendOverviewPart(body);

			Composite commentFormPart = new CommentListFormPart(getManagedForm(), body, SWT.NO_FOCUS,
					getTrackerService(), getNode());
			commentFormPart.setLayoutData(new TableWrapData(FILL_GRAB));
		}

		/** Creates the general section */
		private void appendOverviewPart(final Composite parent) {
			FormToolkit tk = getManagedForm().getToolkit();

			final Section section = TrackerUiUtils.addFormSection(tk, parent, getIssueTitle());
			section.setLayoutData(new TableWrapData(FILL_GRAB));

			Composite body = (Composite) section.getClient();
			TableWrapLayout layout = new TableWrapLayout();
			layout.numColumns = 6;
			body.setLayout(layout);

			// Status
			createFormBoldLabel(tk, body, "Status");
			statusCmb = new Combo(body, SWT.READ_ONLY);
			statusCmb.setLayoutData(new TableWrapData(FILL, BOTTOM));

			// Project
			createFormBoldLabel(tk, body, "Project");
			projectLk = new Link(body, SWT.NONE);
			projectLk.setLayoutData(new TableWrapData(FILL_GRAB, BOTTOM));
			configureOpenLink(projectLk, project);

			// Target milestone
			createFormBoldLabel(tk, body, "Milestone");
			milestoneLk = new Link(body, SWT.NONE);
			milestoneLk.setLayoutData(new TableWrapData(FILL_GRAB, BOTTOM));

			// Assigned to
			createFormBoldLabel(tk, body, "Assigned to");
			assignedToLk = new Link(body, SWT.NONE);
			assignedToLk.setLayoutData(new TableWrapData(FILL, BOTTOM));

			// Due Date
			TrackerUiUtils.createFormBoldLabel(tk, body, "Due Date");
			dueDateLk = new Link(body, SWT.NONE);
			dueDateLk.setLayoutData(new TableWrapData(FILL_GRAB, BOTTOM));

			// Reported by
			TrackerUiUtils.createFormBoldLabel(tk, body, "Reported by");
			reporterLk = new Link(body, SWT.NONE);
			reporterLk.setLayoutData(new TableWrapData(FILL_GRAB, BOTTOM));

			// Related entities
			TrackerUiUtils.createFormBoldLabel(tk, body, "Related to");
			relatedCmp = new Composite(body, SWT.NO_FOCUS);
			TableWrapData twd = new TableWrapData(FILL_GRAB, BOTTOM);
			twd.colspan = 5;
			relatedCmp.setLayoutData(twd);

			// TODO add linked documents

			// Description
			twd = (TableWrapData) TrackerUiUtils.createFormBoldLabel(tk, body, "Details").getLayoutData();
			twd.valign = TableWrapData.TOP;
			descLbl = new Label(body, SWT.WRAP);
			twd = new TableWrapData(FILL_GRAB, TableWrapData.TOP);
			twd.colspan = 5;
			descLbl.setLayoutData(twd);

			SectionPart part = new SectionPart((Section) body.getParent()) {

				@Override
				public void refresh() {
					// TODO Prevent edition for user without sufficient rights
					refreshStatusCombo(statusCmb, task);
					statusCmb.setText(ConnectJcrUtils.get(task, ActivitiesNames.ACTIVITIES_TASK_STATUS));

					// Project cannot change
					// configureOpenLink(projectLk, project);
					Node milestone = TrackerUtils.getMilestone(getTrackerService(), task);
					if (milestone != null)
						configureOpenLink(milestoneLk, milestone);

					String manager = getActivitiesService().getAssignedToDisplayName(task);
					String importance = TrackerUtils.getImportanceLabel(task);
					String priority = TrackerUtils.getPriorityLabel(task);
					String tmp = ConnectJcrUtils.concatIfNotEmpty(importance, priority, "/");
					if (EclipseUiUtils.notEmpty(tmp))
						manager += " (" + tmp + " )";
					// TODO make it clickable
					assignedToLk.setText(manager);

					String dueDateStr = ConnectJcrUtils.getDateFormattedAsString(task,
							ActivitiesNames.ACTIVITIES_DUE_DATE, ConnectConstants.DEFAULT_DATE_TIME_FORMAT);
					if (EclipseUiUtils.notEmpty(dueDateStr))
						dueDateLk.setText(dueDateStr);

					reporterLk.setText(TrackerUtils.getCreationLabel(getUserAdminService(), task));

					try {
						List<Node> relatedTo = new ArrayList<>();
						if (task.hasProperty(ACTIVITIES_RELATED_TO)) {
							Value[] values = task.getProperty(ACTIVITIES_RELATED_TO).getValues();
							for (Value value : values) {
								String valueStr = value.getString();
								Node targetNode = task.getSession().getNodeByIdentifier(valueStr);
								relatedTo.add(targetNode);
							}
						}
						populateMultiValueClickableList(relatedCmp, relatedTo);
					} catch (RepositoryException re) {
						throw new TrackerException("Unable to refresh related to composite on " + task, re);
					}

					String desc = ConnectJcrUtils.get(task, Property.JCR_DESCRIPTION);
					descLbl.setText(desc);

					parent.layout(true, true);
					section.setFocus();
					super.refresh();
				}
			};
			addStatusCmbSelListener(part, statusCmb, task, ActivitiesNames.ACTIVITIES_TASK_STATUS, PropertyType.STRING);
			// addLongCmbSelListener(part, importanceCmb, task,
			// TrackerNames.TRACKER_IMPORTANCE,
			// TrackerUtils.MAPS_ISSUE_IMPORTANCES);
			// addLongCmbSelListener(part, priorityCmb, task,
			// TrackerNames.TRACKER_PRIORITY,
			// TrackerUtils.MAPS_ISSUE_PRIORITIES);

			// addMilestoneDDFOListener(part, targetTxt, task);
			// addFocusOutListener(part, descTxt, task,
			// Property.JCR_DESCRIPTION);
			// addChangeAssignListener(part, assignedToLk);
			getManagedForm().addPart(part);
			addMainSectionMenu(part);
		}

		private void configureOpenLink(Link link, Node targetNode) {
			link.setText("<a>" + ConnectJcrUtils.get(targetNode, Property.JCR_TITLE) + "</a>");

			// Remove existing if necessary
			Listener[] existings = link.getListeners(SWT.Selection);
			for (Listener l : existings)
				link.removeListener(SWT.Selection, l);

			link.addSelectionListener(new SelectionAdapter() {
				private static final long serialVersionUID = 1L;

				@Override
				public void widgetSelected(SelectionEvent e) {
					CommandUtils.callCommand(getAppWorkbenchService().getOpenEntityEditorCmdId(),
							OpenEntityEditor.PARAM_JCR_ID, ConnectJcrUtils.getIdentifier(targetNode));
				}
			});
		}
	}

	private void populateMultiValueClickableList(Composite parent, List<Node> nodes) {
		CmsUtils.clear(parent);
		// TableWrapData twd = ((TableWrapData) parent.getLayoutData());
		// if (nodes == null || nodes.size() < 1) {
		// twd.heightHint = 0;
		// return;
		// } else
		// twd.heightHint = SWT.DEFAULT;

		RowLayout rl = new RowLayout(SWT.HORIZONTAL);
		rl.wrap = true;
		rl.marginLeft = rl.marginTop = rl.marginBottom = 0;
		rl.marginRight = 8;
		parent.setLayout(rl);

		for (Node node : nodes) {
			String value = ConnectJcrUtils.get(node, Property.JCR_TITLE);
			Link link = new Link(parent, SWT.NONE);
			CmsUtils.markup(link);
			link.setText(" <a>" + value + "</a>");

			link.addSelectionListener(new SelectionAdapter() {
				private static final long serialVersionUID = 1L;

				@Override
				public void widgetSelected(final SelectionEvent event) {
					CommandUtils.callCommand(getAppWorkbenchService().getOpenEntityEditorCmdId(),
							OpenEntityEditor.PARAM_JCR_ID, ConnectJcrUtils.getIdentifier(node));
				}
			});
		}
	}

	// SECTION MENU
	private void addMainSectionMenu(SectionPart sectionPart) {
		ToolBarManager toolBarManager = TrackerUiUtils.addMenu(sectionPart.getSection());
		String tooltip = "Edit the task main information";
		Action action = new OpenConfigureDialog(tooltip, ConnectImages.IMG_DESC_EDIT, sectionPart);
		toolBarManager.add(action);
		toolBarManager.update(true);
	}

	// MENU ACTIONS
	private class OpenConfigureDialog extends Action {
		private static final long serialVersionUID = -6798429720348536525L;
		private final SectionPart sectionPart;

		private OpenConfigureDialog(String name, ImageDescriptor img, SectionPart sectionPart) {
			super(name, img);
			this.sectionPart = sectionPart;
		}

		@Override
		public void run() {
			Shell currShell = sectionPart.getSection().getShell();
			ConfigureTaskWizard wizard = new ConfigureTaskWizard(getUserAdminService(), getActivitiesService(),
					getTrackerService(), getAppWorkbenchService(),task);
			WizardDialog dialog = new WizardDialog(currShell, wizard);
			try {
				if (dialog.open() == Window.OK && task.getSession().hasPendingChanges()) {
					updatePartName();
					sectionPart.getSection().setText(getIssueTitle());
					sectionPart.refresh();
					sectionPart.markDirty();
					sectionPart.getSection().setFocus();
				}
			} catch (RepositoryException e) {
				throw new TrackerException("Cannot check session state on " + task, e);
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

	/** Override this to add specific rights for status change */
	private void refreshStatusCombo(Combo combo, Node currTask) {
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
			Node project = TrackerUtils.getRelatedProject(getTrackerService(), getNode());
			String pname = ConnectJcrUtils.get(project, Property.JCR_TITLE);
			name = name + (notEmpty(pname) ? " (" + pname + ")" : "");
		}
		return "#" + id + " " + name;
	}

	private Label createFormBoldLabel(FormToolkit toolkit, Composite parent, String value) {
		// We add a blank space before to workaround the cropping of the
		// word
		// first letter in some OS/Browsers (typically MAC/Firefox 31 )
		Label label = toolkit.createLabel(parent, " " + value, SWT.END);
		label.setFont(EclipseUiUtils.getBoldFont(parent));
		TableWrapData twd = new TableWrapData(TableWrapData.RIGHT, TableWrapData.BOTTOM);
		label.setLayoutData(twd);
		return label;
	}
}
