package org.argeo.tracker.internal.workbench;

import static org.eclipse.ui.forms.widgets.TableWrapData.FILL_GRAB;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.activities.ActivitiesNames;
import org.argeo.cms.ArgeoNames;
import org.argeo.cms.ui.workbench.util.CommandUtils;
import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.AppService;
import org.argeo.connect.ConnectNames;
import org.argeo.connect.ui.ConnectUiConstants;
import org.argeo.connect.ui.ConnectUiSnippets;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.workbench.TechnicalInfoPage;
import org.argeo.connect.workbench.commands.OpenEntityEditor;
import org.argeo.connect.workbench.parts.AskTitleDescriptionDialog;
import org.argeo.eclipse.ui.ColumnDefinition;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.eclipse.ui.jcr.lists.SimpleJcrNodeLabelProvider;
import org.argeo.jcr.JcrUtils;
import org.argeo.tracker.TrackerException;
import org.argeo.tracker.TrackerNames;
import org.argeo.tracker.TrackerTypes;
import org.argeo.tracker.core.TrackerUtils;
import org.argeo.tracker.core.VersionComparator;
import org.argeo.tracker.internal.ui.TrackerLps;
import org.argeo.tracker.internal.ui.TrackerUiConstants;
import org.argeo.tracker.internal.ui.TrackerUiUtils;
import org.argeo.tracker.internal.ui.dialogs.ConfigureMilestoneWizard;
import org.argeo.tracker.internal.ui.dialogs.ConfigureProjectWizard;
import org.argeo.tracker.internal.ui.dialogs.ConfigureTaskWizard;
import org.argeo.tracker.internal.ui.dialogs.ConfigureVersionWizard;
import org.argeo.tracker.ui.MilestoneListComposite;
import org.argeo.tracker.workbench.TrackerUiPlugin;
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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

/** Default editor to display and edit a project definition */
public class ProjectEditor extends AbstractTrackerEditor {
	private static final long serialVersionUID = -2589214457345896922L;
	public static final String ID = TrackerUiPlugin.PLUGIN_ID + ".projectEditor";

	// Ease implementation
	private Node project;

	// Current pages
	private MainPage projectMainPage;
	private TasksPage tasksPage;
	private MilestonesPage milestonesPage;
	private TechnicalInfoPage techInfoPage;

	@Override
	protected void addPages() {
		project = getNode();
		try {
			projectMainPage = new MainPage(this);
			addPage(projectMainPage);

			tasksPage = new TasksPage(this);
			addPage(tasksPage);

			milestonesPage = new MilestonesPage(this);
			addPage(milestonesPage);

			techInfoPage = new TechnicalInfoPage(this, TrackerUiPlugin.PLUGIN_ID + ".issueEditor.issueMainPage",
					getNode());
			addPage(techInfoPage);
		} catch (PartInitException e) {
			throw new TrackerException("Cannot add pages for editor of " + getNode(), e);
		}
	}

	// Specific pages
	private class MainPage extends FormPage implements ArgeoNames {
		public final static String ID = TrackerUiPlugin.PLUGIN_ID + ".projectEditor.mainPage";

		public MainPage(FormEditor editor) {
			super(editor, ID, "Overview");
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

			Section section = TrackerUiUtils.addFormSection(tk, parent,
					ConnectJcrUtils.get(project, Property.JCR_TITLE));
			section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

			Composite body = (Composite) section.getClient();
			TableWrapLayout layout = new TableWrapLayout();
			layout.numColumns = 4;
			body.setLayout(layout);

			final Label descLbl = tk.createLabel(body, "", SWT.WRAP);
			TableWrapData twd = new TableWrapData(FILL_GRAB);
			twd.colspan = 4;
			descLbl.setLayoutData(twd);

			SectionPart part = new SectionPart((Section) body.getParent()) {

				@Override
				public void refresh() {
					String desc = ConnectJcrUtils.get(project, Property.JCR_DESCRIPTION);
					descLbl.setText(desc);
					descLbl.getParent().layout();
					getManagedForm().reflow(true);
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
			form.reflow(true);
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

					Session tmpSession = null;
					try {
						AppService as = getAppService();
						tmpSession = project.getSession().getRepository().login();
						Node draftTask = as.createDraftEntity(tmpSession, TrackerTypes.TRACKER_TASK);
						draftTask.setProperty(TrackerNames.TRACKER_PROJECT_UID,
								ConnectJcrUtils.get(project, ConnectNames.CONNECT_UID));
						ConfigureTaskWizard wizard = new ConfigureTaskWizard(getUserAdminService(), getTrackerService(),
								draftTask);
						WizardDialog dialog = new WizardDialog(addBtn.getShell(), wizard);
						if (dialog.open() == Window.OK) {
							String taskBasePath = as.getBaseRelPath(TrackerTypes.TRACKER_TASK);
							Node parent = tmpSession.getNode("/" + taskBasePath);
							Node task = getTrackerService().publishEntity(parent, TrackerTypes.TRACKER_TASK, draftTask);
							task = getTrackerService().saveEntity(task, false);
							project.getSession().refresh(true);
							refreshViewer(filterTxt.getText());
						}
					} catch (RepositoryException e1) {
						throw new TrackerException("Unable to create issue on " + project, e1);
					} finally {
						JcrUtils.logoutQuietly(tmpSession);
					}
				}
			});
		}
	}

	private class MilestonesPage extends FormPage implements ArgeoNames {
		public final static String ID = TrackerUiPlugin.PLUGIN_ID + ".projectEditor.issuesPage";

		private TableViewer tableViewer;
		private Text filterTxt;

		public MilestonesPage(FormEditor editor) {
			super(editor, ID, "Milestones");
		}

		protected void createFormContent(IManagedForm mf) {
			ScrolledForm form = mf.getForm();
			Composite body = form.getBody();
			GridLayout mainLayout = new GridLayout();

			body.setLayout(mainLayout);
			Composite filterCmp = new Composite(body, SWT.NO_FOCUS);
			createFilterPart(filterCmp);
			filterCmp.setLayoutData(EclipseUiUtils.fillWidth());
			Composite tableCmp = new Composite(body, SWT.NO_FOCUS);
			appendMilestonesPart(mf, tableCmp);
			tableCmp.setLayoutData(EclipseUiUtils.fillAll());

			form.reflow(true);
		}

		private void appendMilestonesPart(IManagedForm mf, Composite parent) {
			List<ColumnDefinition> columnDefs = new ArrayList<ColumnDefinition>();
			columnDefs.add(new ColumnDefinition(getJcrLP(Property.JCR_TITLE), "Name", 120));
			columnDefs.add(new ColumnDefinition(new TrackerLps().new MilestoneDateLabelProvider(), "Date", 120));
			columnDefs.add(new ColumnDefinition(getJcrLP(Property.JCR_DESCRIPTION), "Description", 300));
			columnDefs.add(new ColumnDefinition(getMSIssuesLP(true), "Open tasks", 120));
			columnDefs.add(new ColumnDefinition(getMSIssuesLP(false), "All tasks", 120));
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
			addBtn.setImage(TrackerImages.ICON_ADD);

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

					Session tmpSession = null;
					try {
						AppService as = getAppService();
						tmpSession = project.getSession().getRepository().login();
						Node draftMilestone = as.createDraftEntity(tmpSession, TrackerTypes.TRACKER_MILESTONE);
						draftMilestone.setProperty(TrackerNames.TRACKER_PROJECT_UID,
								ConnectJcrUtils.get(project, ConnectNames.CONNECT_UID));
						ConfigureMilestoneWizard wizard = new ConfigureMilestoneWizard(getUserAdminService(),
								getTrackerService(), draftMilestone);
						WizardDialog dialog = new WizardDialog(addBtn.getShell(), wizard);
						if (dialog.open() == Window.OK) {
							Node parent = tmpSession.getNode("/" + as.getBaseRelPath(TrackerTypes.TRACKER_MILESTONE));
							Node milestone = getTrackerService().publishEntity(parent, TrackerTypes.TRACKER_MILESTONE,
									draftMilestone);
							milestone = getTrackerService().saveEntity(milestone, false);
							project.getSession().refresh(true);
							refreshViewer(filterTxt.getText());
						}
					} catch (RepositoryException e1) {
						throw new TrackerException("Unable to create issue on " + project, e1);
					} finally {
						JcrUtils.logoutQuietly(tmpSession);
					}
				}
			});
		}
	}

	// LOCAL HELPERS

	// Shorten this call
	private static ColumnLabelProvider getJcrLP(String propName) {
		return new SimpleJcrNodeLabelProvider(propName);
	}

	// private static ColumnLabelProvider getCountLP(boolean onlyOpen) {
	// return new ColumnLabelProvider() {
	// private static final long serialVersionUID = -998161071505982347L;
	//
	// @Override
	// public String getText(Object element) {
	// Node category = (Node) element;
	// long currNb = TrackerUtils.getIssueNb(category, onlyOpen);
	// return currNb + "";
	// }
	// };
	// }
	//
	private ColumnLabelProvider getMSIssuesLP(boolean onlyOpen) {
		return new ColumnLabelProvider() {
			private static final long serialVersionUID = -998161071505982347L;

			@Override
			public String getText(Object element) {
				Node milestone = (Node) element;
				NodeIterator nit = TrackerUtils.getIssues(project, null, TrackerNames.TRACKER_MILESTONE_UID,
						ConnectJcrUtils.get(milestone, ConnectNames.CONNECT_UID), onlyOpen);
				long currNb = nit.getSize();
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
							ConfigureVersionWizard wizard = new ConfigureVersionWizard(getTrackerService(), project,
									node);
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
