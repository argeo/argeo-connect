package org.argeo.tracker.e4.parts;

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
import org.argeo.cms.ui.eclipse.forms.AbstractFormPart;
import org.argeo.cms.ui.eclipse.forms.FormToolkit;
import org.argeo.cms.ui.eclipse.forms.IFormPart;
import org.argeo.cms.ui.eclipse.forms.IManagedForm;
import org.argeo.cms.ui.util.CmsUiUtils;
import org.argeo.connect.AppService;
import org.argeo.connect.ConnectException;
import org.argeo.connect.ConnectNames;
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
import org.argeo.tracker.TrackerException;
import org.argeo.tracker.TrackerNames;
import org.argeo.tracker.TrackerService;
import org.argeo.tracker.TrackerTypes;
import org.argeo.tracker.core.TrackerUtils;
import org.argeo.tracker.ui.MilestoneListComposite;
import org.argeo.tracker.ui.TrackerLps;
import org.argeo.tracker.ui.TrackerUiConstants;
import org.argeo.tracker.ui.VersionComparator;
import org.argeo.tracker.ui.controls.RepartitionChart;
import org.argeo.tracker.ui.dialogs.ConfigureProjectWizard;
import org.argeo.tracker.ui.dialogs.ConfigureVersionWizard;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

/** Default editor to display and edit an IT project */
public class ItProjectEditor extends AbstractTrackerEditor {
	// private static final long serialVersionUID = 1787310296792025442L;

	// public static final String ID = TrackerUiPlugin.PLUGIN_ID +
	// ".itProjectEditor";

	// Ease implementation
	private Node project;

	// local parameters
	private final static int CHART_DATA_LIMIT = 8;
	private final static int CHART_WIDTH = 300;
	private final static int CHART_HEIGHT = 200;

	// @Override
	// public void init(IEditorSite site, IEditorInput input) throws
	// PartInitException {
	// super.init(site, input);
	// }

	@Override
	protected void addPages() {
		// Initialise the nodes
		project = getNode();
		try {
			addPage(new MainPage(this));
			addPage(new IssuesPage(this));
			addPage(new MilestoneListPage(this, ".milestoneList", project, getUserAdminService(), getTrackerService(),
					getAppWorkbenchService()));
			addPage(new VersionsPage(this));
			addPage(new ComponentsPage(this));

			// if (CurrentUser.isInRole(NodeConstants.ROLE_ADMIN))
			// addPage(new TechnicalInfoPage(this, ID + ".techInfoPage", getNode()));
		} catch (Exception e) {
			throw new TrackerException("Cannot add pages for editor of " + getNode(), e);
		}
	}

	// Specific pages
	private class MainPage extends AbstractEditorPage {
		public final static String PAGE_ID = "mainPage";

		private Link managerLk;
		private Link overdueTasksLk;
		private Composite chartCmp;
		private Label descLbl;

		public MainPage(AbstractTrackerEditor editor) {
			super(editor, PAGE_ID, "Overview");
		}

		protected void createFormContent(final IManagedForm mf) {
			// ScrolledForm form = mf.getForm();
			// Composite body = form.getBody();
			ScrolledComposite form = mf.getForm();
			Composite body = new Composite(form, SWT.NONE);

			GridLayout layout = new GridLayout();
			body.setLayout(layout);
			appendOverviewPart(body);
			appendOpenMilestonePart(body);
		}

		/** Creates the general section */
		private void appendOverviewPart(Composite parent) {
			FormToolkit tk = getPageManagedForm().getToolkit();
			GridData twd;

			Section section = TrackerUiUtils.addFormSection(tk, parent,
					ConnectJcrUtils.get(project, Property.JCR_TITLE));
			section.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

			Composite body = (Composite) section.getClient();
			GridLayout layout = new GridLayout();
			layout.numColumns = 5;
			body.setLayout(layout);

			// Manager
			createFormBoldLabel(tk, body, "Manager");
			managerLk = new Link(body, SWT.NONE);
			managerLk.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));

			// Overdue tasks
			createFormBoldLabel(tk, body, "Overdue Tasks");
			overdueTasksLk = new Link(body, SWT.NONE);
			overdueTasksLk.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));

			// Chart
			chartCmp = new Composite(body, SWT.NO_FOCUS);
			chartCmp.setLayout(EclipseUiUtils.noSpaceGridLayout());
			twd = new GridData(SWT.CENTER, SWT.CENTER);
			// twd.maxWidth = CHART_WIDTH;
			// twd.rowspan = 2;
			chartCmp.setLayoutData(twd);

			// Description
			twd = (GridData) TrackerUiUtils.createFormBoldLabel(tk, body, "Details").getLayoutData();
			// twd.valign = GridData.TOP;
			descLbl = new Label(body, SWT.WRAP);
			twd = new GridData(SWT.FILL, SWT.TOP, true, false);
			// twd.colspan = 3;
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
					CmsUiUtils.clear(chartCmp);
					GridData twd = (GridData) chartCmp.getLayoutData();
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
			getPageManagedForm().addPart(part);
			addMainSectionMenu(part);
		}

		private Section appendOpenMilestonePart(Composite parent) {
			FormToolkit tk = getPageManagedForm().getToolkit();
			Section section = TrackerUiUtils.addFormSection(tk, parent, "Open Milestones");
			section.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

			Composite body = (Composite) section.getClient();
			body.setLayout(new GridLayout());
			final MilestoneListComposite msBoxCmp = new MilestoneListComposite(body, SWT.NO_FOCUS,
					getAppWorkbenchService(), project);
			msBoxCmp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

			SectionPart part = new SectionPart(section) {

				@Override
				public void refresh() {
					NodeIterator nit = TrackerUtils.getOpenMilestones(project, null);
					msBoxCmp.setInput(JcrUtils.nodeIteratorToList(nit).toArray(new Node[0]));
					super.refresh();
				}
			};
			getPageManagedForm().addPart(part);
			return section;
		}
	}

	// SECTION MENU
	private void addMainSectionMenu(SectionPart sectionPart) {
		ToolBarManager toolBarManager = TrackerUiUtils.addMenu(sectionPart.getSection());

		String tooltip = "Edit the project main information";
		Action action = new OpenConfigureDialog(tooltip, ConnectImages.IMG_DESC_EDIT, sectionPart);
		toolBarManager.add(action);

		tooltip = "Add a milestone to this project";
		action = new AddMilestone(sectionPart.getSection().getShell(), tooltip, ConnectImages.IMG_DESC_ADD);
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
			String pathCreated = ConnectWorkbenchUtils.createAndConfigureEntity(shell, referenceSession, appService,
					getAppWorkbenchService(), mainMixin, propName1, value1);
			if (EclipseUiUtils.notEmpty(pathCreated)) {
				Node created = ConnectJcrUtils.getNode(referenceSession, pathCreated);
				// ConnectWorkbenchUtils.callCommand(getAppWorkbenchService().getOpenEntityEditorCmdId(),
				// ConnectEditor.PARAM_JCR_ID, ConnectJcrUtils.getIdentifier(created));
				getAppWorkbenchService().openEntityEditor(created);
			}
		}
	}

	// private void appendMilestoneCmp(Composite parent, Node milestone) {
	// String currTitle = ConnectJcrUtils.get(milestone, Property.JCR_TITLE);
	// String currId = ConnectJcrUtils.get(milestone, TrackerNames.TRACKER_ID);
	// int totalNb = (int) TrackerUtils.getIssues(project, null,
	// TrackerNames.TRACKER_MILESTONE_ID, currId).getSize();
	// int openNb = (int) TrackerUtils.getIssues(project, null,
	// TrackerNames.TRACKER_MILESTONE_ID, currId, true)
	// .getSize();
	// int closeNb = totalNb - openNb;
	//
	// if (totalNb <= 0)
	// return;
	//
	// Composite boxCmp = new Composite(parent, SWT.NO_FOCUS | SWT.BORDER); //
	// boxCmp.setLayoutData(new GridData(FILL_GRAB));
	//
	// GridLayout layout = new GridLayout();
	// layout.numColumns = 2;
	// boxCmp.setLayout(layout);
	//
	// Link titleLk = new Link(boxCmp, SWT.WRAP);
	// titleLk.setLayoutData(new GridData(FILL_GRAB));
	// titleLk.setFont(EclipseUiUtils.getBoldFont(boxCmp));
	// titleLk.setText("<a>" + currId + "</a>");
	// titleLk.addSelectionListener(new SelectionAdapter() {
	// private static final long serialVersionUID = 5342086098924045174L;
	//
	// @Override
	// public void widgetSelected(SelectionEvent e) {
	// String jcrId = ConnectJcrUtils.getIdentifier(milestone);
	// CommandUtils.callCommand(getAppWorkbenchService().getOpenEntityEditorCmdId(),
	// OpenEntityEditor.PARAM_JCR_ID, jcrId);
	// }
	// });
	//
	// Composite chartCmp = new Composite(boxCmp, SWT.NO_FOCUS);
	// GridData twd = new GridData();
	// twd.rowspan = 3;
	// twd.heightHint = 40;
	// twd.valign = GridData.CENTER;
	// chartCmp.setLayoutData(twd);
	// chartCmp.setLayout(EclipseUiUtils.noSpaceGridLayout());
	//
	// CategoryOverviewChart coc = new CategoryOverviewChart(chartCmp,
	// SWT.NO_FOCUS);
	// coc.setInput(currTitle, closeNb, totalNb);
	// coc.setLayoutData(new GridData(310, 40));
	// coc.layout(true, true);
	//
	// Label datesLbl = new Label(boxCmp, SWT.WRAP);
	// String ddVal = ConnectJcrUtils.getDateFormattedAsString(milestone,
	// TrackerNames.TRACKER_TARGET_DATE,
	// TrackerUiConstants.defaultDateFormat);
	// if (EclipseUiUtils.isEmpty(ddVal)) {
	// datesLbl.setText("No due date defined");
	// datesLbl.setFont(EclipseUiUtils.getItalicFont(boxCmp));
	// } else
	// datesLbl.setText("Due date: " + ddVal);
	//
	// Label descLbl = new Label(boxCmp, SWT.WRAP);
	// String desc = ConnectJcrUtils.get(milestone, Property.JCR_DESCRIPTION);
	// if (EclipseUiUtils.isEmpty(desc))
	// descLbl.setText("-");
	// else
	// descLbl.setText(desc);
	// }

	private class IssuesPage extends AbstractEditorPage {
		public final static String PAGE_ID = ".issueListPage";

		private TableViewer tableViewer;
		private Text filterTxt;

		public IssuesPage(AbstractTrackerEditor editor) {
			super(editor, PAGE_ID, "Issues");
		}

		protected void createFormContent(final IManagedForm mf) {
			// ScrolledForm form = mf.getForm();
			// Composite body = form.getBody();
			ScrolledComposite form = mf.getForm();
			Composite body = new Composite(form, SWT.NONE);

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
			columnDefs.add(new ColumnDefinition(new TrackerLps().new ImportanceLabelProvider(), "Importance", 100));
			columnDefs.add(new ColumnDefinition(new TrackerLps().new PriorityLabelProvider(), "Priority", 100));
			columnDefs.add(new ColumnDefinition(new TrackerLps().new CommentNbLabelProvider(), "Comments", 120));

			tableViewer = TrackerUiUtils.createTableViewer(parent, SWT.SINGLE, columnDefs);
			addDClickListener(tableViewer);
			refreshViewer(null);
		}

		private void refreshViewer(String filter) {
			NodeIterator nit = TrackerUtils.getIssues(project, filter);
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
			addBtn.setToolTipText("Create an issue");
			addBtn.setImage(ConnectImages.ADD);

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

					String mainMixin = TrackerTypes.TRACKER_ISSUE;
					Session referenceSession = ConnectJcrUtils.getSession(project);
					Shell shell = addBtn.getShell();
					AppService appService = getAppService();
					String propName1 = TrackerNames.TRACKER_PROJECT_UID;
					String value1 = ConnectJcrUtils.get(project, ConnectNames.CONNECT_UID);
					String pathCreated = ConnectWorkbenchUtils.createAndConfigureEntity(shell, referenceSession,
							appService, getAppWorkbenchService(), mainMixin, propName1, value1);
					if (EclipseUiUtils.notEmpty(pathCreated))
						refreshViewer(filterTxt.getText());

					// Session tmpSession = null;
					// try {
					// AppService as = getAppService();
					// tmpSession =
					// project.getSession().getRepository().login();
					// Node draftIssue = as.createDraftEntity(tmpSession,
					// TrackerTypes.TRACKER_ISSUE);
					// draftIssue.setProperty(TrackerNames.TRACKER_PROJECT_UID,
					// ConnectJcrUtils.get(project, ConnectNames.CONNECT_UID));
					// ConfigureIssueWizard wizard = new
					// ConfigureIssueWizard(getUserAdminService(),
					// getTrackerService(), draftIssue);
					// WizardDialog dialog = new WizardDialog(addBtn.getShell(),
					// wizard);
					// if (dialog.open() == Window.OK) {
					// String issueBasePath =
					// as.getBaseRelPath(TrackerTypes.TRACKER_ISSUE);
					// Node parent = tmpSession.getNode("/" + issueBasePath);
					// Node issue = getTrackerService().publishEntity(parent,
					// TrackerTypes.TRACKER_ISSUE,
					// draftIssue);
					// issue = getTrackerService().saveEntity(issue, false);
					// project.getSession().refresh(true);
					// refreshViewer(filterTxt.getText());
					// }
					// } catch (RepositoryException e1) {
					// throw new TrackerException("Unable to create issue on " +
					// project, e1);
					// } finally {
					// JcrUtils.logoutQuietly(tmpSession);
					// }
				}
			});
		}
	}

	private class VersionsPage extends AbstractEditorPage {
		public final static String ID = "projectEditor.issuesPage";

		private TableViewer tableViewer;
		private Text filterTxt;

		public VersionsPage(AbstractTrackerEditor editor) {
			super(editor, ID, "Versions");
		}

		protected void createFormContent(IManagedForm mf) {
			// ScrolledForm form = mf.getForm();
			// Composite body = form.getBody();
			ScrolledComposite form = mf.getForm();
			Composite body = new Composite(form, SWT.NONE);

			GridLayout mainLayout = new GridLayout();

			body.setLayout(mainLayout);
			Composite filterCmp = new Composite(body, SWT.NO_FOCUS);
			createFilterPart(filterCmp);
			filterCmp.setLayoutData(EclipseUiUtils.fillWidth());
			Composite tableCmp = new Composite(body, SWT.NO_FOCUS);
			appendVersionsPart(mf, tableCmp);
			tableCmp.setLayoutData(EclipseUiUtils.fillAll());

			// form.reflow(true);
		}

		private void appendVersionsPart(IManagedForm mf, Composite parent) {
			List<ColumnDefinition> columnDefs = new ArrayList<ColumnDefinition>();
			columnDefs.add(new ColumnDefinition(getJcrLP(TrackerNames.TRACKER_ID), "ID", 80));
			columnDefs.add(new ColumnDefinition(new TrackerLps().new VersionDateLabelProvider(), "Release Date", 120));
			columnDefs.add(new ColumnDefinition(getCountLP(true), "Open issues", 120));
			columnDefs.add(new ColumnDefinition(getCountLP(false), "All issues", 120));
			columnDefs.add(new ColumnDefinition(getJcrLP(Property.JCR_DESCRIPTION), "Description", 300));
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
					return comp.compare(viewer, ConnectJcrUtils.get(n2, TrackerNames.TRACKER_ID),
							ConnectJcrUtils.get(n1, TrackerNames.TRACKER_ID));
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
				CmsUiUtils.setItemHeight(table, TrackerUiConstants.DEFAULT_ROW_HEIGHT);
				CmsUiUtils.markup(table);
				table.addSelectionListener(new EditionRwtAdapter(part));
			}
			refreshViewer(null);
		}

		private void refreshViewer(String filter) {
			NodeIterator nit = TrackerUtils.getAllVersions(project, filter);
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
			addBtn.setToolTipText("Create a new version");
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
					String mainMixin = TrackerTypes.TRACKER_VERSION;
					Session referenceSession = ConnectJcrUtils.getSession(project);
					Shell shell = addBtn.getShell();
					TrackerService trackerService = getTrackerService();
					String propName1 = TrackerNames.TRACKER_PROJECT_UID;
					String value1 = ConnectJcrUtils.get(project, ConnectNames.CONNECT_UID);
					String pathCreated = ConnectWorkbenchUtils.createAndConfigureEntity(shell, referenceSession,
							trackerService, getAppWorkbenchService(), mainMixin, propName1, value1);
					if (EclipseUiUtils.notEmpty(pathCreated))
						refreshViewer(filterTxt.getText());
					// // FIXME this will fail for users with limited rights
					// ConfigureVersionWizard wizard = new
					// ConfigureVersionWizard(getTrackerService(), project);
					// WizardDialog dialog = new WizardDialog(addBtn.getShell(),
					// wizard);
					// if (dialog.open() == Window.OK) {
					// try {
					// project.getSession().save();
					// refreshViewer(filterTxt.getText());
					// } catch (RepositoryException e1) {
					// throw new TrackerException("Unable to create a version
					// for" + project, e1);
					// }
					// }
				}
			});
		}
	}

	private class ComponentsPage extends AbstractEditorPage {
		public final static String ID = "projectEditor.componentsPage";

		private TableViewer tableViewer;
		private Text filterTxt;

		public ComponentsPage(AbstractTrackerEditor editor) {
			super(editor, ID, "Components");
		}

		protected void createFormContent(final IManagedForm mf) {
			// ScrolledForm form = mf.getForm();
			// Composite body = form.getBody();

			ScrolledComposite form = mf.getForm();
			Composite body = new Composite(form, SWT.NONE);

			GridLayout mainLayout = new GridLayout();
			body.setLayout(mainLayout);
			Composite filterCmp = new Composite(body, SWT.NO_FOCUS);
			createFilterPart(filterCmp);
			filterCmp.setLayoutData(EclipseUiUtils.fillWidth());
			Composite tableCmp = new Composite(body, SWT.NO_FOCUS);
			appendComponentsPart(mf, tableCmp);
			tableCmp.setLayoutData(EclipseUiUtils.fillAll());
			// form.reflow(true);
		}

		private void appendComponentsPart(IManagedForm mf, Composite parent) {
			List<ColumnDefinition> columnDefs = new ArrayList<ColumnDefinition>();
			columnDefs.add(new ColumnDefinition(getJcrLP(Property.JCR_TITLE), "Title", 150));
			columnDefs.add(new ColumnDefinition(getJcrLP(Property.JCR_DESCRIPTION), "Description", 300));
			columnDefs.add(new ColumnDefinition(getCountLP(true), "Open issues", 120));
			columnDefs.add(new ColumnDefinition(getCountLP(false), "All issues", 120));
			if (canEdit())
				columnDefs.add(new ColumnDefinition(getEditionLP(), "", 120));

			tableViewer = TrackerUiUtils.createTableViewer(parent, SWT.SINGLE, columnDefs);
			tableViewer.setComparator(new ViewerComparator() {
				private static final long serialVersionUID = 1L;

				public int compare(Viewer viewer, Object e1, Object e2) {
					try {
						Node n1 = (Node) e1;
						Node n2 = (Node) e2;
						return ConnectJcrUtils.get(n1, Property.JCR_TITLE)
								.compareToIgnoreCase(ConnectJcrUtils.get(n2, Property.JCR_TITLE));
					} catch (ConnectException e) {
						// TODO clean this: silently catch exception when an
						// item has been deleted.
					}
					return 0;
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
				CmsUiUtils.setItemHeight(table, TrackerUiConstants.DEFAULT_ROW_HEIGHT);
				CmsUiUtils.markup(table);
				table.addSelectionListener(new EditionRwtAdapter(part));
			}
			refreshViewer(null);
		}

		private void refreshViewer(String filter) {
			NodeIterator nit = TrackerUtils.getComponents(project, filter);
			if (nit == null || !nit.hasNext())
				tableViewer.setInput(null);
			else
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
			addBtn.setToolTipText("Create a new component");
			addBtn.setImage(ConnectImages.ADD);
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

					String mainMixin = TrackerTypes.TRACKER_COMPONENT;
					Session referenceSession = ConnectJcrUtils.getSession(project);
					Shell shell = addBtn.getShell();
					TrackerService trackerService = getTrackerService();
					String propName1 = TrackerNames.TRACKER_PROJECT_UID;
					String value1 = ConnectJcrUtils.get(project, ConnectNames.CONNECT_UID);
					String pathCreated = ConnectWorkbenchUtils.createAndConfigureEntity(shell, referenceSession,
							trackerService, getAppWorkbenchService(), mainMixin, propName1, value1);
					if (EclipseUiUtils.notEmpty(pathCreated))
						refreshViewer(filterTxt.getText());

					//
					// // FIXME save will fail for users with limited rights
					// ConfigureComponentWizard wizard = new
					// ConfigureComponentWizard(getTrackerService(), project);
					// WizardDialog dialog = new WizardDialog(addBtn.getShell(),
					// wizard);
					// if (dialog.open() == Window.OK) {
					// try {
					// project.getSession().save();
					// refreshViewer(filterTxt.getText());
					// } catch (RepositoryException e1) {
					// throw new TrackerException("Unable to create a version
					// for" + project, e1);
					// }
					// }
				}
			});
		}
	}

	// LOCAL HELPERS

	// Shorten this call
	private static ColumnLabelProvider getJcrLP(String propName) {
		return new SimpleJcrNodeLabelProvider(propName);
	}

	private static ColumnLabelProvider getCountLP(boolean onlyOpen) {
		return new ColumnLabelProvider() {
			private static final long serialVersionUID = -998161071505982347L;

			@Override
			public String getText(Object element) {
				Node category = (Node) element;
				long currNb = TrackerUtils.getIssueNb(category, onlyOpen);
				return currNb + "";
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

	// private void addDClickListener(TableViewer tableViewer) {
	// tableViewer.addDoubleClickListener(new IDoubleClickListener() {
	// @Override
	// public void doubleClick(DoubleClickEvent event) {
	// Object element = ((IStructuredSelection)
	// event.getSelection()).getFirstElement();
	// String jcrId = ConnectJcrUtils.getIdentifier((Node) element);
	// CommandUtils.callCommand(getAppWorkbenchService().getOpenEntityEditorCmdId(),
	// OpenEntityEditor.PARAM_JCR_ID, jcrId);
	// }
	// });
	// }

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
							ConfigureVersionWizard wizard = new ConfigureVersionWizard(getTrackerService(), node);
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

	private void addDClickListener(TableViewer tableViewer) {
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				Object element = ((IStructuredSelection) event.getSelection()).getFirstElement();
				// String jcrId = ConnectJcrUtils.getIdentifier((Node) element);
				// CommandUtils.callCommand(getAppWorkbenchService().getOpenEntityEditorCmdId(),
				// ConnectEditor.PARAM_JCR_ID, jcrId);
				getAppWorkbenchService().openEntityEditor((Node) element);
			}
		});
	}

//	private Label createFormBoldLabel(FormToolkit toolkit, Composite parent, String value) {
//		Label label = toolkit.createLabel(parent, " " + value, SWT.END);
//		label.setFont(EclipseUiUtils.getBoldFont(parent));
//		GridData twd = new GridData(SWT.END, SWT.BOTTOM);
//		label.setLayoutData(twd);
//		return label;
//	}

}
