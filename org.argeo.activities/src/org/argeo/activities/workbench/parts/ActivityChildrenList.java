package org.argeo.activities.workbench.parts;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.activities.ActivitiesException;
import org.argeo.activities.ActivitiesNames;
import org.argeo.activities.ActivitiesService;
import org.argeo.activities.ActivitiesTypes;
import org.argeo.activities.ActivityValueCatalogs;
import org.argeo.activities.ui.ActivityTable;
import org.argeo.activities.workbench.ActivitiesUiPlugin;
import org.argeo.cms.ui.workbench.util.CommandUtils;
import org.argeo.connect.ConnectNames;
import org.argeo.connect.UserAdminService;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.ui.util.LazyCTabControl;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.util.XPathUtils;
import org.argeo.connect.workbench.ConnectWorkbenchUtils;
import org.argeo.connect.workbench.SystemWorkbenchService;
import org.argeo.connect.workbench.commands.OpenEntityEditor;
import org.argeo.connect.workbench.parts.AbstractConnectEditor;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class ActivityChildrenList extends LazyCTabControl {
	private static final long serialVersionUID = 5906357274592489553L;

	// private final static Log log = LogFactory.getLog(ActivityList.class);

	public final static String CTAB_ID = ActivitiesUiPlugin.PLUGIN_ID + ".ctab.activityList";

	// Context
	private final UserAdminService userAdminService;
	private final ResourcesService resourcesService;
	private final ActivitiesService activitiesService;
	private final SystemWorkbenchService systemWorkbenchService;
	private final Node activity;
	private final AbstractConnectEditor editor;
	private final FormToolkit toolkit;

	// UI Objects
	private MyFormPart myFormPart;
	private MyActivityTableCmp activityTable;

	@Override
	public void refreshPartControl() {
		myFormPart.refresh();
		ActivityChildrenList.this.layout(true, true);
	}

	public ActivityChildrenList(Composite parent, int style, AbstractConnectEditor editor,
			UserAdminService userAdminService, ResourcesService resourcesService, ActivitiesService activitiesService,
			SystemWorkbenchService systemWorkbenchService, Node entity) {
		super(parent, style);
		this.userAdminService = userAdminService;
		this.resourcesService = resourcesService;
		this.activitiesService = activitiesService;
		this.systemWorkbenchService = systemWorkbenchService;
		this.activity = entity;
		this.editor = editor;
		this.toolkit = editor.getFormToolkit();
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());
		Composite addCmp = null;

		// TODO use finer rules to enable new activity addition
		if (ConnectJcrUtils.canEdit(activity)) {
			addCmp = toolkit.createComposite(parent);
			addCmp.setLayoutData(EclipseUiUtils.fillWidth());
		}

		activityTable = new MyActivityTableCmp(parent, SWT.MULTI, activity);
		activityTable.setLayoutData(EclipseUiUtils.fillAll());
		activityTable.getTableViewer()
				.addDoubleClickListener(new ActivityTableDCL(systemWorkbenchService.getOpenEntityEditorCmdId()));

		if (addCmp != null)
			addNewActivityPanel(addCmp, activity, activityTable);

		myFormPart = new MyFormPart();
		myFormPart.initialize(editor.getManagedForm());
		editor.getManagedForm().addPart(myFormPart);
	}

	private class MyFormPart extends AbstractFormPart {

		@Override
		public void refresh() {
			activityTable.refresh();
			super.refresh();
		}
	}

	private void addNewActivityPanel(final Composite addActivityBar, final Node entity,
			final MyActivityTableCmp activityTable) {
		// The Add Activity bar
		addActivityBar.setLayoutData(EclipseUiUtils.fillWidth());
		addActivityBar.setLayout(new GridLayout(7, false));

		// Activity type
		final Combo typeCmb = new Combo(addActivityBar, SWT.NONE | SWT.READ_ONLY);
		GridData gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		gd.widthHint = 100;
		typeCmb.setLayoutData(gd);
		typeCmb.setItems(ActivityValueCatalogs.getActivityTypeLabels());
		typeCmb.select(0);

		toolkit.adapt(typeCmb, true, true);

		// Title
		final Text titleTxt = ConnectWorkbenchUtils.createGDText(toolkit, addActivityBar, "Title",
				"Enter a short title for the activity to create", 150, 1);

		// Description
		final Text descTxt = ConnectWorkbenchUtils.createGDText(toolkit, addActivityBar, "Description",
				"Enter a description for the activity to create", 300, 1);

		Button validBtn = toolkit.createButton(addActivityBar, "Add activity", SWT.PUSH);

		toolkit.createLabel(addActivityBar, " OR ", SWT.NONE);

		final Link addTaskLk = new Link(addActivityBar, SWT.NONE);
		addTaskLk.setText("<a>Add a task</a>");

		// Selection and traverse listeners
		validBtn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				createActivity(entity, typeCmb, titleTxt, descTxt, activityTable);
			}
		});

		TraverseListener travList = new TraverseListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.keyCode == SWT.CR) {
					e.doit = false;
					createActivity(entity, typeCmb, titleTxt, descTxt, activityTable);
				}
			}
		};

		titleTxt.addTraverseListener(travList);
		descTxt.addTraverseListener(travList);

		addTaskLk.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (createChildTask(addTaskLk.getShell()))
					activityTable.refresh();
			}
		});
	}

	// LOCAL UI HELPERS

	private class ActivityTableDCL implements IDoubleClickListener {

		private String openEditorCmdId;

		public ActivityTableDCL(String openEditorCmdId) {
			this.openEditorCmdId = openEditorCmdId;
		}

		public void doubleClick(DoubleClickEvent event) {
			if (event.getSelection() == null || event.getSelection().isEmpty())
				return;
			Object obj = ((IStructuredSelection) event.getSelection()).getFirstElement();
			try {
				Node currNode;
				if (obj instanceof Node)
					currNode = (Node) obj;
				else
					return;
				String jcrId = currNode.getIdentifier();
				CommandUtils.callCommand(openEditorCmdId, OpenEntityEditor.PARAM_JCR_ID, jcrId);
			} catch (RepositoryException re) {
				throw new ActivitiesException("Unable to open editor for node", re);
			}
		}
	}

	private class MyActivityTableCmp extends ActivityTable {
		private static final long serialVersionUID = 1L;

		public MyActivityTableCmp(Composite parent, int style, Node entity) {
			super(parent, style, userAdminService, resourcesService, activitiesService, systemWorkbenchService, entity);
		}

		protected void refreshFilteredList() {
			try {
				StringBuilder builder = new StringBuilder();
				builder.append("//element(*, ");
				builder.append(ActivitiesTypes.ACTIVITIES_ACTIVITY);
				builder.append(")");
				// get child activities
				builder.append("[");
				builder.append(XPathUtils.getPropertyEquals(ActivitiesNames.ACTIVITIES_PARENT_UID,
						ConnectJcrUtils.get(activity, ConnectNames.CONNECT_UID)));
				builder.append("]");
				NodeIterator nit = XPathUtils.createQuery(activity.getSession(), builder.toString()).execute()
						.getNodes();

				List<Node> nodes = new ArrayList<Node>();
				while (nit.hasNext())
					nodes.add(nit.nextNode());
				getTableViewer().setInput(nodes.toArray());
			} catch (RepositoryException e) {
				throw new ActivitiesException("Unable to list child activities  under " + activity, e);
			}
		}
	}

	private void createActivity(Node entity, Combo typeLbCmb, Text titleTxt, Text descTxt, ActivityTable table) {
		String typeLbl = typeLbCmb.getText();
		String title = titleTxt.getText();
		String desc = descTxt.getText();
		String type = ActivityValueCatalogs.getKeyByValue(ActivityValueCatalogs.MAPS_ACTIVITY_TYPES, typeLbl);
		if (createActivity(type, title, desc)) {
			table.refresh();
			typeLbCmb.select(0);
			titleTxt.setText("");
			descTxt.setText("");
			typeLbCmb.setFocus();
		}
	}

	private boolean createChildTask(Shell shell) {
		Session session = null;
		try {
			session = activity.getSession().getRepository().login();
			Node tmpTask = activitiesService.createDraftEntity(session, ActivitiesTypes.ACTIVITIES_TASK);
			tmpTask.setProperty(ActivitiesNames.ACTIVITIES_PARENT_UID,
					activity.getProperty(ConnectNames.CONNECT_UID).getString());
			NewSimpleTaskWizard wizard = new NewSimpleTaskWizard(userAdminService, activitiesService, tmpTask);
			WizardDialog dialog = new WizardDialog(shell, wizard);
			if (dialog.open() == Window.OK) {
				Node childTask = activitiesService.publishEntity(null, ActivitiesTypes.ACTIVITIES_TASK, tmpTask);
				childTask = activitiesService.saveEntity(childTask, false);
				activity.getSession().refresh(true);
				return true;
			}
		} catch (RepositoryException e) {
			throw new ActivitiesException("Unable to create child task on " + activity, e);
		} finally {
			JcrUtils.logoutQuietly(session);
		}
		return false;
	}

	private boolean createActivity(String type, String title, String desc) {
		Session session = null;
		try {
			session = activity.getSession().getRepository().login();
			Node tmpChild = activitiesService.createDraftEntity(session, type);
			tmpChild.setProperty(ActivitiesNames.ACTIVITIES_PARENT_UID,
					activity.getProperty(ConnectNames.CONNECT_UID).getString());
			activitiesService.configureActivity(tmpChild, type, title, desc, null);
			Node childActivity = activitiesService.publishEntity(null, type, tmpChild);
			childActivity = activitiesService.saveEntity(childActivity, false);
			activity.getSession().refresh(true);
			return true;
		} catch (RepositoryException e) {
			throw new ActivitiesException("Unable to create child activity under " + activity, e);
		} finally {
			JcrUtils.logoutQuietly(session);
		}
	}
}
