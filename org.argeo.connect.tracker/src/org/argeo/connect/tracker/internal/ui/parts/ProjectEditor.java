package org.argeo.connect.tracker.internal.ui.parts;

import static org.eclipse.ui.forms.widgets.TableWrapData.FILL_GRAB;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.argeo.cms.ui.workbench.util.CommandUtils;
import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.workbench.rap.commands.OpenEntityEditor;
import org.argeo.connect.tracker.PeopleTrackerService;
import org.argeo.connect.tracker.TrackerException;
import org.argeo.connect.tracker.TrackerNames;
import org.argeo.connect.tracker.TrackerService;
import org.argeo.connect.tracker.core.TrackerUtils;
import org.argeo.connect.tracker.core.VersionComparator;
import org.argeo.connect.tracker.internal.ui.AbstractTrackerEditor;
import org.argeo.connect.tracker.internal.ui.TrackerImages;
import org.argeo.connect.tracker.internal.ui.TrackerLps;
import org.argeo.connect.tracker.internal.ui.TrackerUiConstants;
import org.argeo.connect.tracker.internal.ui.TrackerUiUtils;
import org.argeo.connect.tracker.internal.ui.controls.CategoryOverviewChart;
import org.argeo.connect.tracker.internal.ui.dialogs.NewComponentWizard;
import org.argeo.connect.tracker.internal.ui.dialogs.NewIssueWizard;
import org.argeo.connect.tracker.internal.ui.dialogs.NewVersionWizard;
import org.argeo.connect.tracker.ui.TrackerUiPlugin;
import org.argeo.connect.ui.workbench.TechnicalInfoPage;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.ColumnDefinition;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.eclipse.ui.jcr.lists.SimpleJcrNodeLabelProvider;
import org.argeo.jcr.JcrUtils;
import org.argeo.node.ArgeoNames;
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
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PartInitException;
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
	private static final long serialVersionUID = 1787310296792025442L;
	// private final static Log log = LogFactory.getLog(ProjectEditor.class);
	public static final String ID = TrackerUiPlugin.PLUGIN_ID + ".projectEditor";

	// Context
	private TrackerService issueService;
	private Node project;

	// Current pages
	private MainPage projectMainPage;
	private IssuesPage issuesPage;
	private VersionsPage milestonesPage;
	private ComponentsPage componentsPage;
	private TechnicalInfoPage techInfoPage;

	// Ease implementation

	@Override
	protected void addPages() {
		// Initialise the nodes
		project = getNode();
		issueService = ((PeopleTrackerService) getPeopleService()).getTrackerService();
		try {
			projectMainPage = new MainPage(this);
			addPage(projectMainPage);

			issuesPage = new IssuesPage(this);
			addPage(issuesPage);

			milestonesPage = new VersionsPage(this);
			addPage(milestonesPage);

			componentsPage = new ComponentsPage(this);
			addPage(componentsPage);

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
			super(editor, ID, "Main");
		}

		protected void createFormContent(final IManagedForm mf) {
			ScrolledForm form = mf.getForm();
			Composite body = form.getBody();
			TableWrapLayout layout = new TableWrapLayout();
			body.setLayout(layout);
			appendOverviewPart(body);
			appendIssueListPart(body);
		}

		/** Creates the general section */
		private void appendOverviewPart(Composite parent) {
			FormToolkit tk = getManagedForm().getToolkit();

			Section section = TrackerUiUtils.addFormSection(tk, parent,
					ConnectJcrUtils.get(project, Property.JCR_TITLE) + " - Overview");
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
		}

		// Some monitoring indicators
		private Section appendIssueListPart(Composite parent) {
			FormToolkit tk = getManagedForm().getToolkit();
			Section section = TrackerUiUtils.addFormSection(tk, parent, "Future Milestones");
			section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

			final Composite body = (Composite) section.getClient();
			body.setLayout(new TableWrapLayout());

			SectionPart part = new SectionPart(section) {

				@Override
				public void refresh() {

					if (body.isDisposed())
						return;
					CmsUtils.clear(body);

					// TODO enhance picking up and ordering of displayed
					// milestones
					NodeIterator nit = TrackerUtils.getMilestones(project, null);
					while (nit.hasNext()) {
						Node currMilestone = nit.nextNode();
						appendMilestoneCmp(body, currMilestone);
					}

					refreshViewer();
					super.refresh();
				}
			};
			getManagedForm().addPart(part);
			return section;
		}

		private void refreshViewer() {

		}
	}

	private void appendMilestoneCmp(Composite parent, Node milestone) {
		String currTitle = ConnectJcrUtils.get(milestone, Property.JCR_TITLE);
		String currId = ConnectJcrUtils.get(milestone, TrackerNames.TRACKER_ID);
		int totalNb = (int) TrackerUtils.getIssues(project, null, TrackerNames.TRACKER_TARGET_ID, currId).getSize();
		int openNb = (int) TrackerUtils.getIssues(project, null, TrackerNames.TRACKER_TARGET_ID, currId, true)
				.getSize();
		int closeNb = totalNb - openNb;

		if (totalNb <= 0)
			return;

		Composite boxCmp = new Composite(parent, SWT.NO_FOCUS | SWT.BORDER); //
		boxCmp.setLayoutData(new TableWrapData(FILL_GRAB));

		TableWrapLayout layout = new TableWrapLayout();
		layout.numColumns = 2;
		boxCmp.setLayout(layout);

		Link titleLk = new Link(boxCmp, SWT.WRAP);
		titleLk.setLayoutData(new TableWrapData(FILL_GRAB));
		titleLk.setFont(EclipseUiUtils.getBoldFont(boxCmp));
		titleLk.setText("<a>" + currId + "</a>");
		titleLk.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 5342086098924045174L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				String jcrId = ConnectJcrUtils.getIdentifier(milestone);
				CommandUtils.callCommand(getAoWbService().getOpenEntityEditorCmdId(), OpenEntityEditor.PARAM_JCR_ID,
						jcrId);
			}
		});

		Composite chartCmp = new Composite(boxCmp, SWT.NO_FOCUS);
		TableWrapData twd = new TableWrapData();
		twd.rowspan = 3;
		twd.heightHint = 40;
		twd.valign = TableWrapData.CENTER;
		chartCmp.setLayoutData(twd);
		chartCmp.setLayout(EclipseUiUtils.noSpaceGridLayout());

		CategoryOverviewChart coc = new CategoryOverviewChart(chartCmp, SWT.NO_FOCUS);
		coc.setInput(currTitle, closeNb, totalNb);
		coc.setLayoutData(new GridData(310, 40));
		coc.layout(true, true);

		Label datesLbl = new Label(boxCmp, SWT.WRAP);
		String ddVal = ConnectJcrUtils.getDateFormattedAsString(milestone, PeopleNames.PEOPLE_DUE_DATE,
				TrackerUiConstants.defaultDateFormat);
		if (EclipseUiUtils.isEmpty(ddVal)) {
			datesLbl.setText("No due date defined");
			datesLbl.setFont(EclipseUiUtils.getItalicFont(boxCmp));
		} else
			datesLbl.setText("Due by " + ddVal);

		Label descLbl = new Label(boxCmp, SWT.WRAP);
		String desc = ConnectJcrUtils.get(milestone, Property.JCR_DESCRIPTION);
		if (EclipseUiUtils.isEmpty(desc))
			descLbl.setText("-");
		else
			descLbl.setText(desc);
	}

	private class IssuesPage extends FormPage implements ArgeoNames {
		public final static String ID = TrackerUiPlugin.PLUGIN_ID + ".projectEditor.issuesPage";

		private TableViewer tableViewer;

		public IssuesPage(FormEditor editor) {
			super(editor, ID, "Issues");
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

			form.reflow(true);
		}

		private void appendIssuesPart(Composite parent) {
			List<ColumnDefinition> columnDefs = new ArrayList<ColumnDefinition>();
			columnDefs.add(new ColumnDefinition(getJcrLP(TrackerNames.TRACKER_ID), "ID", 40));
			columnDefs.add(new ColumnDefinition(getJcrLP(Property.JCR_TITLE), "Title", 300));
			columnDefs.add(new ColumnDefinition(getJcrLP(PeopleNames.PEOPLE_TASK_STATUS), "Status", 100));
			columnDefs.add(new ColumnDefinition(new TrackerLps().new ImportanceLabelProvider(), "Importance", 100));
			columnDefs.add(new ColumnDefinition(new TrackerLps().new PriorityLabelProvider(), "Priority", 100));
			columnDefs.add(new ColumnDefinition(getJcrLP(TrackerNames.TRACKER_TARGET_ID), "Target", 80));
			columnDefs.add(new ColumnDefinition(new TrackerLps().new CommentNbLabelProvider(), "Comments", 120));

			tableViewer = TrackerUiUtils.createTableViewer(parent, SWT.SINGLE, columnDefs);
			addDClickListener(tableViewer);
			refreshViewer(null);
		}

		private void refreshViewer(String filter) {
			NodeIterator nit = TrackerUtils.getIssues(project, filter);
			tableViewer.setInput(JcrUtils.nodeIteratorToList(nit).toArray(new Node[0]));
		}

		private void createFilterPart(Composite parent) {
			GridLayout layout = EclipseUiUtils.noSpaceGridLayout(new GridLayout(2, false));
			layout.horizontalSpacing = 5;
			parent.setLayout(layout);
			parent.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			final Text filterTxt = new Text(parent, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH | SWT.ICON_CANCEL);
			filterTxt.setLayoutData(EclipseUiUtils.fillWidth());

			final Button addBtn = new Button(parent, SWT.PUSH);
			addBtn.setToolTipText("Create a new issue");
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

					NewIssueWizard wizard = new NewIssueWizard(getPeopleService(), project);
					WizardDialog dialog = new WizardDialog(addBtn.getShell(), wizard);
					if (dialog.open() == Window.OK) {
						try {
							project.getSession().save();
							refreshViewer(filterTxt.getText());
						} catch (RepositoryException e1) {
							throw new TrackerException("Unable to create issue on " + project, e1);
						}
					}
				}
			});
		}
	}

	private class VersionsPage extends FormPage implements ArgeoNames {
		public final static String ID = TrackerUiPlugin.PLUGIN_ID + ".projectEditor.issuesPage";

		private TableViewer tableViewer;

		public VersionsPage(FormEditor editor) {
			super(editor, ID, "Milestones");
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
			appendVersionsPart(tableCmp);
			tableCmp.setLayoutData(EclipseUiUtils.fillAll());

			form.reflow(true);
		}

		private void appendVersionsPart(Composite parent) {
			List<ColumnDefinition> columnDefs = new ArrayList<ColumnDefinition>();
			columnDefs.add(new ColumnDefinition(getJcrLP(TrackerNames.TRACKER_ID), "ID", 80));
			columnDefs.add(new ColumnDefinition(new TrackerLps().new VersionDateLabelProvider(), "Date", 120));
			columnDefs.add(new ColumnDefinition(getJcrLP(Property.JCR_DESCRIPTION), "Description", 300));
			columnDefs.add(new ColumnDefinition(getCountLP(true), "Open issues", 120));
			columnDefs.add(new ColumnDefinition(getCountLP(false), "All issues", 120));

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
			refreshViewer(null);
		}

		private void refreshViewer(String filter) {
			NodeIterator nit = TrackerUtils.getAllVersions(project, filter);
			tableViewer.setInput(JcrUtils.nodeIteratorToList(nit).toArray(new Node[0]));
		}

		private void createFilterPart(Composite parent) {
			GridLayout layout = EclipseUiUtils.noSpaceGridLayout(new GridLayout(2, false));
			layout.horizontalSpacing = 5;
			parent.setLayout(layout);
			parent.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			final Text filterTxt = new Text(parent, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH | SWT.ICON_CANCEL);
			filterTxt.setLayoutData(EclipseUiUtils.fillWidth());

			final Button addBtn = new Button(parent, SWT.PUSH);
			addBtn.setToolTipText("Create a new version");
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
					NewVersionWizard wizard = new NewVersionWizard(issueService, project);
					WizardDialog dialog = new WizardDialog(addBtn.getShell(), wizard);
					if (dialog.open() == Window.OK) {
						try {
							project.getSession().save();
							refreshViewer(filterTxt.getText());
						} catch (RepositoryException e1) {
							throw new TrackerException("Unable to create a version for" + project, e1);
						}
					}
				}
			});
		}
	}

	private class ComponentsPage extends FormPage implements ArgeoNames {
		public final static String ID = TrackerUiPlugin.PLUGIN_ID + ".projectEditor.componentsPage";

		private TableViewer tableViewer;

		public ComponentsPage(FormEditor editor) {
			super(editor, ID, "Components");
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
			appendComponentsPart(tableCmp);
			tableCmp.setLayoutData(EclipseUiUtils.fillAll());
			form.reflow(true);
		}

		private void appendComponentsPart(Composite parent) {
			List<ColumnDefinition> columnDefs = new ArrayList<ColumnDefinition>();
			columnDefs.add(new ColumnDefinition(getJcrLP(Property.JCR_TITLE), "Title", 150));
			columnDefs.add(new ColumnDefinition(getJcrLP(Property.JCR_DESCRIPTION), "Description", 300));
			columnDefs.add(new ColumnDefinition(getCountLP(true), "Open issues", 120));
			columnDefs.add(new ColumnDefinition(getCountLP(false), "All issues", 120));

			tableViewer = TrackerUiUtils.createTableViewer(parent, SWT.SINGLE, columnDefs);
			tableViewer.setComparator(new ViewerComparator() {
				private static final long serialVersionUID = 1L;

				public int compare(Viewer viewer, Object e1, Object e2) {
					Node n1 = (Node) e1;
					Node n2 = (Node) e2;
					return ConnectJcrUtils.get(n1, Property.JCR_TITLE)
							.compareToIgnoreCase(ConnectJcrUtils.get(n2, Property.JCR_TITLE));
				};
			});
			addDClickListener(tableViewer);
			refreshViewer(null);
		}

		private void refreshViewer(String filter) {
			NodeIterator nit = TrackerUtils.getComponents(project, filter);
			if (nit == null || !nit.hasNext())
				tableViewer.setInput(null);
			else
				tableViewer.setInput(JcrUtils.nodeIteratorToList(nit).toArray(new Node[0]));
		}

		private void createFilterPart(Composite parent) {
			GridLayout layout = EclipseUiUtils.noSpaceGridLayout(new GridLayout(2, false));
			layout.horizontalSpacing = 5;
			parent.setLayout(layout);
			parent.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			final Text filterTxt = new Text(parent, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH | SWT.ICON_CANCEL);
			filterTxt.setLayoutData(EclipseUiUtils.fillWidth());
			final Button addBtn = new Button(parent, SWT.PUSH);
			addBtn.setToolTipText("Create a new component");
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
					NewComponentWizard wizard = new NewComponentWizard(issueService, project);
					WizardDialog dialog = new WizardDialog(addBtn.getShell(), wizard);
					if (dialog.open() == Window.OK) {
						try {
							project.getSession().save();
							refreshViewer(filterTxt.getText());
						} catch (RepositoryException e1) {
							throw new TrackerException("Unable to create a version for" + project, e1);
						}
					}
				}
			});
		}
	}

	// MENU CONFIGURATION
	// private void addListSectionMenu(SectionPart sectionPart) {
	// ToolBarManager toolBarManager =
	// TrackerUiUtils.addMenu(sectionPart.getSection());
	// String tooltip = "Create a new issue";
	// // Action action = new CreateIssue(tooltip,
	// // TrackerImages.ICON_DESC_CREATE, sectionPart);
	// // toolBarManager.add(action);
	// toolBarManager.update(true);
	// }
	//
	// // MENU ACTIONS
	// private class ForceRefresh extends Action {
	// private static final long serialVersionUID = -6798429720348536525L;
	// private final SectionPart sectionPart;
	//
	// private ForceRefresh(String name, ImageDescriptor img, SectionPart
	// sectionPart) {
	// super(name, img);
	// this.sectionPart = sectionPart;
	// }
	//
	// @Override
	// public void run() {
	// sectionPart.refresh();
	// }
	// }

	// private class CreateIssue extends Action {
	// private static final long serialVersionUID = -1337713097184522588L;
	//
	// private final SectionPart sectionPart;
	//
	// private CreateIssue(String name, ImageDescriptor img, SectionPart
	// sectionPart) {
	// super(name, img);
	// this.sectionPart = sectionPart;
	//
	// }
	//
	// @Override
	// public void run() {
	// // TODO Do something
	// // sectionPart.refresh();
	// }
	// }

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

	private void addDClickListener(TableViewer tableViewer) {
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				Object element = ((IStructuredSelection) event.getSelection()).getFirstElement();
				String jcrId = ConnectJcrUtils.getIdentifier((Node) element);
				CommandUtils.callCommand(getAoWbService().getOpenEntityEditorCmdId(), OpenEntityEditor.PARAM_JCR_ID,
						jcrId);
			}
		});
	}
}
