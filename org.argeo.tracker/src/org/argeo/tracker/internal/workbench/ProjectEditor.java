package org.argeo.tracker.internal.workbench;

import static org.eclipse.ui.forms.widgets.TableWrapData.BOTTOM;
import static org.eclipse.ui.forms.widgets.TableWrapData.FILL_GRAB;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.activities.ActivitiesNames;
import org.argeo.cms.ArgeoNames;
import org.argeo.cms.auth.CurrentUser;
import org.argeo.cms.ui.workbench.util.CommandUtils;
import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.AppService;
import org.argeo.connect.ConnectNames;
import org.argeo.connect.ui.ConnectUiUtils;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.workbench.ConnectWorkbenchUtils;
import org.argeo.connect.workbench.TechnicalInfoPage;
import org.argeo.connect.workbench.commands.OpenEntityEditor;
import org.argeo.eclipse.ui.ColumnDefinition;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.eclipse.ui.jcr.lists.SimpleJcrNodeLabelProvider;
import org.argeo.jcr.JcrUtils;
import org.argeo.node.NodeConstants;
import org.argeo.tracker.TrackerException;
import org.argeo.tracker.TrackerNames;
import org.argeo.tracker.TrackerTypes;
import org.argeo.tracker.core.TrackerUtils;
import org.argeo.tracker.internal.ui.TrackerLps;
import org.argeo.tracker.internal.ui.TrackerUiUtils;
import org.argeo.tracker.internal.ui.controls.RepartitionChart;
import org.argeo.tracker.internal.ui.dialogs.ConfigureProjectWizard;
import org.argeo.tracker.ui.MilestoneListComposite;
import org.argeo.tracker.workbench.TrackerUiPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
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

/** Default editor to display and edit a project */
public class ProjectEditor extends AbstractTrackerEditor {
	private static final long serialVersionUID = -2589214457345896922L;
	public static final String ID = TrackerUiPlugin.PLUGIN_ID + ".projectEditor";

	// Ease implementation
	private Node project;

	// local parameters
	private final static int CHART_DATA_LIMIT = 8;
	private final static int CHART_WIDTH = 300;
	private final static int CHART_HEIGHT = 200;

	@Override
	protected void addPages() {
		project = getNode();
		try {
			addPage(new MainPage(this));
			addPage(new TasksPage(this));
			addPage(new MilestoneListPage(this, ID + ".milestoneList", project, getUserAdminService(),
					getTrackerService(), getAppWorkbenchService()));

			if (CurrentUser.isInRole(NodeConstants.ROLE_ADMIN))
				addPage(new TechnicalInfoPage(this, ID + ".techInfoPage", getNode()));
		} catch (PartInitException e) {
			throw new TrackerException("Cannot add pages for editor of " + getNode(), e);
		}
	}

	// Specific pages
	private class MainPage extends FormPage implements ArgeoNames {
		public final static String PAGE_ID = ID + ".mainPage";

		private Link managerLk;
		private Link overdueTasksLk;
		private Composite chartCmp;
		private Label descLbl;

		public MainPage(FormEditor editor) {
			super(editor, PAGE_ID, "Overview");
		}

		protected void createFormContent(final IManagedForm mf) {
			ScrolledForm form = mf.getForm();
			Composite body = form.getBody();
			TableWrapLayout layout = new TableWrapLayout();
			body.setLayout(layout);
			appendOverviewPart(body);
			appendOpenMilestonePart(body);
		}

		/** Creates the general section */
		private void appendOverviewPart(Composite parent) {
			FormToolkit tk = getManagedForm().getToolkit();
			TableWrapData twd;

			Section section = TrackerUiUtils.addFormSection(tk, parent,
					ConnectJcrUtils.get(project, Property.JCR_TITLE));
			section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

			Composite body = (Composite) section.getClient();
			TableWrapLayout layout = new TableWrapLayout();
			layout.numColumns = 5;
			body.setLayout(layout);

			// Manager
			createFormBoldLabel(tk, body, "Manager");
			managerLk = new Link(body, SWT.NONE);
			managerLk.setLayoutData(new TableWrapData(FILL_GRAB, BOTTOM));

			// Overdue tasks
			createFormBoldLabel(tk, body, "Overdue Tasks");
			overdueTasksLk = new Link(body, SWT.NONE);
			overdueTasksLk.setLayoutData(new TableWrapData(FILL_GRAB, BOTTOM));

			// Chart
			chartCmp = new Composite(body, SWT.NO_FOCUS);
			chartCmp.setLayout(EclipseUiUtils.noSpaceGridLayout());
			twd = new TableWrapData(TableWrapData.CENTER, TableWrapData.MIDDLE);
			twd.maxWidth = CHART_WIDTH;
			twd.rowspan = 2;
			chartCmp.setLayoutData(twd);

			// Description
			twd = (TableWrapData) TrackerUiUtils.createFormBoldLabel(tk, body, "Details").getLayoutData();
			twd.valign = TableWrapData.TOP;
			descLbl = new Label(body, SWT.WRAP);
			twd = new TableWrapData(FILL_GRAB, TableWrapData.TOP);
			twd.colspan = 3;
			descLbl.setLayoutData(twd);

			SectionPart part = new SectionPart((Section) body.getParent()) {

				@Override
				public void refresh() {
					String managerId = ConnectJcrUtils.get(project, TrackerNames.TRACKER_MANAGER);
					if (EclipseUiUtils.notEmpty(managerId))
						managerLk.setText(getUserAdminService().getUserDisplayName(managerId));
					else
						managerLk.setText("");

					String desc = ConnectJcrUtils.get(project, Property.JCR_DESCRIPTION);
					descLbl.setText(desc);

					// The chart
					CmsUtils.clear(chartCmp);
					TableWrapData twd = (TableWrapData) chartCmp.getLayoutData();
					Map<String, String> ot = TrackerUtils.getOpenTasksByAssignee(getUserAdminService(), project, null,
							CHART_DATA_LIMIT);
					if (ot == null || ot.isEmpty()) {
						Label lbl = new Label(chartCmp, SWT.CENTER);
						lbl.setFont(EclipseUiUtils.getItalicFont(body));
						lbl.setText("No open task has been found for this project.");
						twd.heightHint = SWT.DEFAULT;
					} else {
						RepartitionChart coc = new RepartitionChart(chartCmp, SWT.NO_FOCUS);
						twd.heightHint = CHART_HEIGHT;
						coc.setLayoutData(EclipseUiUtils.fillAll());
						coc.setInput("Open tasks by assignee", ot, CHART_WIDTH, CHART_HEIGHT);
					}

					// Overdue tasks
					Long nb = TrackerUtils.getProjectOverdueTasksNumber(project);
					overdueTasksLk.setText(nb < 0 ? "-" : nb.toString());

					parent.layout(true, true);
					section.setFocus();
					super.refresh();
				}
			};
			getManagedForm().addPart(part);
			addMainSectionMenu(part);
		}

		private Section appendOpenMilestonePart(Composite parent) {
			FormToolkit tk = getManagedForm().getToolkit();
			Section section = TrackerUiUtils.addFormSection(tk, parent, "Open Milestones");
			section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

			Composite body = (Composite) section.getClient();
			body.setLayout(new TableWrapLayout());
			final MilestoneListComposite msBoxCmp = new MilestoneListComposite(body, SWT.NO_FOCUS,
					getAppWorkbenchService(), project);
			msBoxCmp.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

			SectionPart part = new SectionPart(section) {

				@Override
				public void refresh() {
					NodeIterator nit = TrackerUtils.getOpenMilestones(project, null);
					msBoxCmp.setInput(JcrUtils.nodeIteratorToList(nit).toArray(new Node[0]));
					super.refresh();
				}
			};
			getManagedForm().addPart(part);
			return section;
		}

		// SECTION MENU
		private void addMainSectionMenu(SectionPart sectionPart) {
			ToolBarManager toolBarManager = TrackerUiUtils.addMenu(sectionPart.getSection());

			String tooltip = "Edit the project main information";
			Action action = new OpenConfigureDialog(tooltip, TrackerImages.IMG_DESC_EDIT, sectionPart);
			toolBarManager.add(action);

			tooltip = "Add a milestone to this project";
			action = new AddMilestone(sectionPart.getSection().getShell(), tooltip, TrackerImages.IMG_DESC_ADD);
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
				ConfigureProjectWizard wizard = new ConfigureProjectWizard(getUserAdminService(), getTrackerService(),
						project);
				WizardDialog dialog = new WizardDialog(currShell, wizard);
				try {
					if (dialog.open() == Window.OK && project.getSession().hasPendingChanges()) {
						updatePartName();
						sectionPart.getSection().setText(ConnectJcrUtils.get(project, Property.JCR_TITLE));
						sectionPart.refresh();
						sectionPart.markDirty();
						sectionPart.getSection().setFocus();
					}
				} catch (RepositoryException e) {
					throw new TrackerException("Cannot check session state on " + project, e);
				}
			}
		}

		private class AddMilestone extends Action {
			private static final long serialVersionUID = 5112793747049604434L;
			final Shell shell;

			private AddMilestone(Shell shell, String name, ImageDescriptor img) {
				super(name, img);
				this.shell = shell;
			}

			@Override
			public void run() {
				String mainMixin = TrackerTypes.TRACKER_MILESTONE;
				Session referenceSession = ConnectJcrUtils.getSession(project);
				AppService appService = getAppService();
				String propName1 = TrackerNames.TRACKER_PROJECT_UID;
				String value1 = ConnectJcrUtils.get(project, ConnectNames.CONNECT_UID);
				String pathCreated = ConnectUiUtils.createAndConfigureEntity(shell, referenceSession, appService,
						getAppWorkbenchService(), mainMixin, propName1, value1);
				if (EclipseUiUtils.notEmpty(pathCreated)) {
					Node created = ConnectJcrUtils.getNode(referenceSession, pathCreated);
					ConnectWorkbenchUtils.callCommand(getAppWorkbenchService().getOpenEntityEditorCmdId(),
							OpenEntityEditor.PARAM_JCR_ID, ConnectJcrUtils.getIdentifier(created));
				}
			}
		}

	}

	private class TasksPage extends FormPage implements ArgeoNames {
		public final static String ID = TrackerUiPlugin.PLUGIN_ID + ".projectEditor.issuesPage";

		private TableViewer tableViewer;
		private Text filterTxt;

		public TasksPage(FormEditor editor) {
			super(editor, ID, "All tasks");
		}

		protected void createFormContent(final IManagedForm mf) {
			ScrolledForm form = mf.getForm();
			Composite body = form.getBody();
			GridLayout mainLayout = new GridLayout();
			body.setLayout(mainLayout);

			Composite filterCmp = new Composite(body, SWT.NO_FOCUS);
			createFilterPart(filterCmp);
			filterCmp.setLayoutData(EclipseUiUtils.fillWidth());
			Composite tableCmp = new Composite(body, SWT.NO_FOCUS);
			appendIssuesPart(tableCmp);
			tableCmp.setLayoutData(EclipseUiUtils.fillAll());

			AbstractFormPart part = new AbstractFormPart() {
				@Override
				public void refresh() {
					refreshViewer(filterTxt.getText());
					super.refresh();
				}
			};
			mf.addPart(part);
			// form.reflow(true);
		}

		private void appendIssuesPart(Composite parent) {
			List<ColumnDefinition> columnDefs = new ArrayList<ColumnDefinition>();
			columnDefs.add(new ColumnDefinition(getJcrLP(TrackerNames.TRACKER_ID), "ID", 40));
			columnDefs.add(new ColumnDefinition(getJcrLP(Property.JCR_TITLE), "Title", 300));
			columnDefs.add(new ColumnDefinition(getJcrLP(ActivitiesNames.ACTIVITIES_TASK_STATUS), "Status", 100));
			columnDefs.add(new ColumnDefinition(
					new TrackerLps().new DnLabelProvider(getUserAdminService(), ActivitiesNames.ACTIVITIES_ASSIGNED_TO),
					"Assignee", 160));
			columnDefs.add(new ColumnDefinition(new TrackerLps().new MilestoneLabelProvider(getAppService()),
					"Milestone", 220));
			columnDefs.add(new ColumnDefinition(new TrackerLps().new CommentNbLabelProvider(), "Comments", 120));
			columnDefs.add(new ColumnDefinition(new TrackerLps().new ImportanceLabelProvider(), "Importance", 100));
			columnDefs.add(new ColumnDefinition(new TrackerLps().new PriorityLabelProvider(), "Priority", 100));

			tableViewer = TrackerUiUtils.createTableViewer(parent, SWT.SINGLE, columnDefs);
			addDClickListener(tableViewer);
			refreshViewer(null);
		}

		private void refreshViewer(String filter) {
			NodeIterator nit = TrackerUtils.getTasks(project, filter);
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
			addBtn.setToolTipText("Create a task");
			addBtn.setImage(TrackerImages.ICON_ADD);

			filterTxt.addModifyListener(new ModifyListener() {
				private static final long serialVersionUID = 8130545587125370689L;

				public void modifyText(ModifyEvent event) {
					refreshViewer(filterTxt.getText());
				}
			});

			addBtn.addSelectionListener(new SelectionAdapter() {
				private static final long serialVersionUID = 9141984572082449486L;

				@Override
				public void widgetSelected(SelectionEvent e) {
					String mainMixin = TrackerTypes.TRACKER_TASK;
					Session referenceSession = ConnectJcrUtils.getSession(project);
					Shell shell = addBtn.getShell();
					AppService appService = getAppService();
					String propName1 = TrackerNames.TRACKER_PROJECT_UID;
					String value1 = ConnectJcrUtils.get(project, ConnectNames.CONNECT_UID);
					String pathCreated = ConnectUiUtils.createAndConfigureEntity(shell, referenceSession, appService,
							getAppWorkbenchService(), mainMixin, propName1, value1);
					if (EclipseUiUtils.notEmpty(pathCreated))
						refreshViewer(filterTxt.getText());
				}
			});
		}
	}

	// LOCAL HELPERS

	// Shorten this call
	private static ColumnLabelProvider getJcrLP(String propName) {
		return new SimpleJcrNodeLabelProvider(propName);
	}

	private void addDClickListener(TableViewer tableViewer) {
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				Object element = ((IStructuredSelection) event.getSelection()).getFirstElement();
				String jcrId = ConnectJcrUtils.getIdentifier((Node) element);
				CommandUtils.callCommand(getAppWorkbenchService().getOpenEntityEditorCmdId(),
						OpenEntityEditor.PARAM_JCR_ID, jcrId);
			}
		});
	}

	private Label createFormBoldLabel(FormToolkit toolkit, Composite parent, String value) {
		Label label = toolkit.createLabel(parent, " " + value, SWT.END);
		label.setFont(EclipseUiUtils.getBoldFont(parent));
		TableWrapData twd = new TableWrapData(TableWrapData.RIGHT, TableWrapData.BOTTOM);
		label.setLayoutData(twd);
		return label;
	}
}
