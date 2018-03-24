package org.argeo.tracker.internal.workbench;

import static org.argeo.eclipse.ui.EclipseUiUtils.notEmpty;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.activities.ActivitiesNames;
import org.argeo.activities.ui.AssignedToLP;
import org.argeo.cms.ArgeoNames;
import org.argeo.connect.AppService;
import org.argeo.connect.ConnectNames;
import org.argeo.connect.ui.ConnectColumnDefinition;
import org.argeo.connect.ui.ConnectImages;
import org.argeo.connect.ui.IJcrTableViewer;
import org.argeo.connect.ui.util.JcrRowLabelProvider;
import org.argeo.connect.ui.util.UserNameLP;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.ColumnDefinition;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.eclipse.ui.jcr.lists.SimpleJcrNodeLabelProvider;
import org.argeo.jcr.JcrUtils;
import org.argeo.tracker.TrackerException;
import org.argeo.tracker.TrackerNames;
import org.argeo.tracker.TrackerTypes;
import org.argeo.tracker.core.TrackerUtils;
import org.argeo.tracker.ui.TrackerLps;
import org.argeo.tracker.ui.dialogs.ConfigureIssueWizard;
import org.argeo.tracker.workbench.TrackerUiPlugin;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ScrolledForm;

/**
 * Basic editor to display a list of all issues that are related to a given
 * category (a milestone, a version, a component ...) within a project. It also
 * enable business admin to edit the main info of this category (typically :
 * title & description)
 */
public class CategoryEditor extends AbstractTrackerEditor implements IJcrTableViewer {
	private static final long serialVersionUID = -6492660981141107302L;
	// private final static Log log = LogFactory.getLog(CategoryEditor.class);
	public static final String ID = TrackerUiPlugin.PLUGIN_ID + ".categoryEditor";

	// Context
	private Node project;
	private Node category;
	private String relevantPropName;
	private String officeID;

	// Ease implementation
	private Text filterTxt;
	private AbstractFormPart issueListPart;

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
	}

	@Override
	protected void addPages() {
		// Initialise local cache
		category = getNode();
		project = TrackerUtils.getProjectFromChild(category);
		officeID = ConnectJcrUtils.get(category, TrackerNames.TRACKER_ID);
		relevantPropName = TrackerUtils.getRelevantPropName(category);
		try {
			addPage(new MainPage(this));

			// if (CurrentUser.isInRole(NodeConstants.ROLE_ADMIN))
			// addPage(new TechnicalInfoPage(this, ID + ".techInfoPage", getNode()));
		} catch (PartInitException e) {
			throw new TrackerException("Cannot add pages for editor of " + getNode(), e);
		}
	}

	protected void updatePartName() {
		String name = getCategoryTitle();
		if (notEmpty(name))
			setPartName(name);
		else
			super.updatePartName();
	}

	private class MainPage extends FormPage implements ArgeoNames {
		public final static String PAGE_ID = ID + ".mainPage";

		private TableViewer tableViewer;

		public MainPage(FormEditor editor) {
			super(editor, PAGE_ID, "Overview");
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
			appendIssuesPart(tableCmp, mf);
			tableCmp.setLayoutData(EclipseUiUtils.fillAll());
			form.reflow(true);
		}

		/** Creates the list of issues relevant for this category */
		private void appendIssuesPart(Composite parent, IManagedForm mf) {
			List<ColumnDefinition> columnDefs = new ArrayList<ColumnDefinition>();
			columnDefs.add(new ColumnDefinition(new SimpleJcrNodeLabelProvider(TrackerNames.TRACKER_ID), "ID", 40));
			columnDefs.add(new ColumnDefinition(new SimpleJcrNodeLabelProvider(Property.JCR_TITLE), "Title", 300));
			columnDefs.add(new ColumnDefinition(new SimpleJcrNodeLabelProvider(ActivitiesNames.ACTIVITIES_TASK_STATUS),
					"Status", 100));
			columnDefs.add(new ColumnDefinition(new TrackerLps().new ImportanceLabelProvider(), "Importance", 100));
			columnDefs.add(new ColumnDefinition(new TrackerLps().new PriorityLabelProvider(), "Priority", 100));

			// Create and configure the table
			tableViewer = TrackerUiUtils.createTableViewer(parent, SWT.SINGLE, columnDefs);
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

			issueListPart = new AbstractFormPart() {
				@Override
				public void refresh() {
					refreshViewer(filterTxt.getText());
					super.refresh();
				}
			};
			mf.addPart(issueListPart);
			refreshViewer(null);
		}

		public void setActive(boolean active) {
			issueListPart.markStale();
			super.setActive(active);
		}

		private void refreshViewer(String filter) {
			NodeIterator nit = TrackerUtils.getIssues(project, filter, relevantPropName, officeID);
			tableViewer.setInput(JcrUtils.nodeIteratorToList(nit).toArray(new Node[0]));
			tableViewer.refresh();
		}

		// Add the filter ability
		private void createFilterPart(Composite parent) {
			GridLayout layout = EclipseUiUtils.noSpaceGridLayout(new GridLayout(2, false));
			layout.horizontalSpacing = 5;
			parent.setLayout(layout);
			parent.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			filterTxt = new Text(parent, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH | SWT.ICON_CANCEL);
			filterTxt.setLayoutData(EclipseUiUtils.fillWidth());

			final Button addBtn = new Button(parent, SWT.PUSH);
			addBtn.setToolTipText("Create a new issue");
			addBtn.setImage(ConnectImages.ADD);

			filterTxt.addModifyListener(new ModifyListener() {
				private static final long serialVersionUID = 8130545587125370689L;

				public void modifyText(ModifyEvent event) {
					refreshViewer(filterTxt.getText());
				}
			});

			addBtn.addSelectionListener(new SelectionAdapter() {
				private static final long serialVersionUID = -7249705366574519524L;

				@Override
				public void widgetSelected(SelectionEvent e) {

					Session tmpSession = null;
					Session targetSession = null;
					try {
						AppService as = getAppService();
						tmpSession = project.getSession().getRepository().login();
						Node draftIssue = as.createDraftEntity(tmpSession, TrackerTypes.TRACKER_ISSUE);
						draftIssue.setProperty(TrackerNames.TRACKER_PROJECT_UID,
								ConnectJcrUtils.get(project, ConnectNames.CONNECT_UID));

						if (ConnectJcrUtils.isNodeType(category, TrackerTypes.TRACKER_COMPONENT))
							ConnectJcrUtils.setMultiValueStringPropFromString(draftIssue,
									TrackerNames.TRACKER_COMPONENT_IDS, officeID, "; ");
						else if (ConnectJcrUtils.isNodeType(category, TrackerTypes.TRACKER_VERSION))
							ConnectJcrUtils.setMultiValueStringPropFromString(draftIssue,
									TrackerNames.TRACKER_VERSION_IDS, officeID, "; ");

						ConfigureIssueWizard wizard = new ConfigureIssueWizard(getUserAdminService(),
								getTrackerService(), draftIssue);

						WizardDialog dialog = new WizardDialog(addBtn.getShell(), wizard);
						if (dialog.open() == Window.OK) {
							targetSession = category.getSession().getRepository().login();
							String issueBasePath = as.getBaseRelPath(TrackerTypes.TRACKER_ISSUE);
							Node parent = targetSession.getNode("/" + issueBasePath);
							Node issue = as.publishEntity(parent, TrackerTypes.TRACKER_ISSUE, draftIssue);
							issue = as.saveEntity(issue, false);
							project.getSession().refresh(true);
							refreshViewer(filterTxt.getText());
						}
					} catch (RepositoryException e1) {
						throw new TrackerException("Unable to create issue on " + project, e1);
					} finally {
						JcrUtils.logoutQuietly(tmpSession);
						JcrUtils.logoutQuietly(targetSession);
					}
				}
			});
		}
	}

	private String getCategoryTitle() {
		String name = ConnectJcrUtils.get(getNode(), Property.JCR_TITLE);
		if (notEmpty(name)) {
			Node project = TrackerUtils.getProjectFromChild(getNode());
			String pname = ConnectJcrUtils.get(project, Property.JCR_TITLE);
			name = name + (notEmpty(pname) ? " (" + pname + ")" : "");
		}
		return name;
	}

	@Override
	public Object[] getElements(String extractId) {
		String filter = "";
		if (filterTxt != null && !filterTxt.isDisposed())
			filter = filterTxt.getText();
		NodeIterator nit = TrackerUtils.getIssues(project, filter, relevantPropName, officeID);
		return JcrUtils.nodeIteratorToList(nit).toArray(new Node[0]);
	}

	@Override
	public List<ConnectColumnDefinition> getColumnDefinition(String extractId) {
		List<ConnectColumnDefinition> columns = new ArrayList<ConnectColumnDefinition>();
		columns.add(new ConnectColumnDefinition("ID", new SimpleJcrNodeLabelProvider(TrackerNames.TRACKER_ID)));
		columns.add(
				new ConnectColumnDefinition("Status", new JcrRowLabelProvider(ActivitiesNames.ACTIVITIES_TASK_STATUS)));
		columns.add(new ConnectColumnDefinition("Title", new JcrRowLabelProvider(Property.JCR_TITLE)));
		columns.add(new ConnectColumnDefinition("Description", new JcrRowLabelProvider(Property.JCR_DESCRIPTION)));
		columns.add(new ConnectColumnDefinition("Assigned To",
				new AssignedToLP(getActivitiesService(), null, Property.JCR_DESCRIPTION)));
		columns.add(
				new ConnectColumnDefinition("Due Date", new JcrRowLabelProvider(ActivitiesNames.ACTIVITIES_DUE_DATE)));
		columns.add(new ConnectColumnDefinition("Importance", new TrackerLps().new ImportanceLabelProvider()));
		columns.add(new ConnectColumnDefinition("Priority", new TrackerLps().new PriorityLabelProvider()));
		columns.add(new ConnectColumnDefinition("Components",
				new SimpleJcrNodeLabelProvider(TrackerNames.TRACKER_COMPONENT_IDS)));
		// columns.add(new ConnectColumnDefinition("Target Milestone",
		// new SimpleJcrNodeLabelProvider(TrackerNames.TRACKER_MILESTONE_ID)));

		columns.add(new ConnectColumnDefinition("Wake-Up Date",
				new JcrRowLabelProvider(ActivitiesNames.ACTIVITIES_WAKE_UP_DATE)));
		columns.add(
				new ConnectColumnDefinition("Close Date", new JcrRowLabelProvider(ConnectNames.CONNECT_CLOSE_DATE)));
		columns.add(new ConnectColumnDefinition("Closed by",
				new UserNameLP(getUserAdminService(), null, ConnectNames.CONNECT_CLOSED_BY)));
		return columns;
	}
}
