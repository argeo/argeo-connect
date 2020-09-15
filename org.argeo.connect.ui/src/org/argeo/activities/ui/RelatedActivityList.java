package org.argeo.activities.ui;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.activities.ActivitiesException;
import org.argeo.activities.ActivitiesNames;
import org.argeo.activities.ActivitiesService;
import org.argeo.activities.ActivitiesTypes;
import org.argeo.activities.ActivityValueCatalogs;
import org.argeo.api.NodeConstants;
import org.argeo.cms.CmsUserManager;
import org.argeo.cms.ui.eclipse.forms.AbstractFormPart;
import org.argeo.cms.ui.eclipse.forms.FormToolkit;
import org.argeo.connect.SystemAppService;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.ui.AppWorkbenchService;
import org.argeo.connect.ui.ConnectEditor;
import org.argeo.connect.ui.ConnectUiUtils;
import org.argeo.connect.ui.SystemWorkbenchService;
import org.argeo.connect.ui.util.LazyCTabControl;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
//import org.eclipse.ui.forms.AbstractFormPart;
//import org.eclipse.ui.forms.widgets.FormToolkit;

public class RelatedActivityList extends LazyCTabControl {
	private static final long serialVersionUID = 5906357274592489553L;

	// private final static Log log = LogFactory.getLog(ActivityList.class);

	// public final static String CTAB_ID = ActivitiesUiPlugin.PLUGIN_ID +
	// ".ctab.activityList";
	public final static String CTAB_ID = "activityList";

	// Context
	private final CmsUserManager userAdminService;
	private final ResourcesService resourcesService;
	private final ActivitiesService activitiesService;
	private final SystemAppService systemAppService;
	private final SystemWorkbenchService systemWorkbenchService;
	private final Node entity;
	private final ConnectEditor editor;
	private final FormToolkit toolkit;

	// UI Objects
	private MyFormPart myFormPart;
	private MyActivityTableCmp activityTable;

	@Override
	public void refreshPartControl() {
		myFormPart.refresh();
		RelatedActivityList.this.layout(true, true);
	}

	public RelatedActivityList(Composite parent, int style, ConnectEditor editor,
			CmsUserManager userAdminService, ResourcesService resourcesService, ActivitiesService activitiesService,
			SystemAppService systemAppService, SystemWorkbenchService systemWorkbenchService, Node entity) {
		super(parent, style);
		this.userAdminService = userAdminService;
		this.resourcesService = resourcesService;
		this.activitiesService = activitiesService;
		this.systemAppService = systemAppService;
		this.systemWorkbenchService = systemWorkbenchService;
		this.entity = entity;
		this.editor = editor;
		this.toolkit = editor.getFormToolkit();
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());

		// if (peopleService.getUserAdminService().amIInRole(
		// PeopleConstants.ROLE_MEMBER)) {
		Composite addCmp = null;
		if (ConnectJcrUtils.canEdit(entity)) {
			addCmp = toolkit.createComposite(parent);
			addCmp.setLayoutData(EclipseUiUtils.fillWidth());
		}

		activityTable = new MyActivityTableCmp(parent, SWT.MULTI, entity);
		activityTable.setLayoutData(EclipseUiUtils.fillAll());
		activityTable.getTableViewer().addDoubleClickListener(new ActivityTableDCL(systemWorkbenchService));

		if (addCmp != null)
			addNewActivityPanel(addCmp, activityTable);

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

	private void addNewActivityPanel(final Composite addActivityBar, final MyActivityTableCmp activityTable) {
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
		final Text titleTxt = ConnectUiUtils.createGDText(toolkit, addActivityBar, "Title",
				"Enter a short title for the activity to create", 150, 1);

		// Description
		final Text descTxt = ConnectUiUtils.createGDText(toolkit, addActivityBar, "Description",
				"Enter a description for the activity to create", 300, 1);

		Button validBtn = toolkit.createButton(addActivityBar, "Add activity", SWT.PUSH);

		// toolkit.createLabel(addActivityBar, " OR ", SWT.NONE);
		//
		// final Link addTaskLk = new Link(addActivityBar, SWT.NONE);
		// addTaskLk.setText("<a>Add a task</a>");

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

		// addTaskLk.addSelectionListener(new SelectionAdapter() {
		// private static final long serialVersionUID = 1L;
		//
		// @Override
		// public void widgetSelected(SelectionEvent e) {
		// if (createTask(addTaskLk.getShell(), entity))
		// activityTable.refresh();
		// }
		// });
	}

	// LOCAL UI HELPERS

	private class ActivityTableDCL implements IDoubleClickListener {

		// private String openEditorCmdId;

		private AppWorkbenchService appWorkbenchService;

		public ActivityTableDCL(AppWorkbenchService appWorkbenchService) {
			// this.openEditorCmdId = openEditorCmdId;
			this.appWorkbenchService = appWorkbenchService;
		}

		public void doubleClick(DoubleClickEvent event) {
			if (event.getSelection() == null || event.getSelection().isEmpty())
				return;
			Object obj = ((IStructuredSelection) event.getSelection()).getFirstElement();
			// try {
			Node currNode;
			if (obj instanceof Node)
				currNode = (Node) obj;
			else
				return;
			// String jcrId = currNode.getIdentifier();
			// CommandUtils.callCommand(openEditorCmdId, ConnectEditor.PARAM_JCR_ID, jcrId);
			appWorkbenchService.openEntityEditor(currNode);
			// } catch (RepositoryException re) {
			// throw new ActivitiesException("Unable to open editor for node", re);
			// }
		}
	}

	private class MyActivityTableCmp extends ActivityTable {
		private static final long serialVersionUID = 1L;

		public MyActivityTableCmp(Composite parent, int style, Node entity) {
			super(parent, style, userAdminService, resourcesService, activitiesService, systemWorkbenchService, entity);
		}

		protected void refreshFilteredList() {
			try {
				List<Node> nodes = new ArrayList<Node>();
				PropertyIterator pit = entity.getReferences(ActivitiesNames.ACTIVITIES_RELATED_TO); //
				while (pit.hasNext()) {
					Property currProp = pit.nextProperty();
					Node currNode = currProp.getParent();
					if (currNode.isNodeType(ActivitiesTypes.ACTIVITIES_ACTIVITY) && !nodes.contains(currNode))
						nodes.add(currNode);
				}
				getTableViewer().setInput(nodes.toArray());
			} catch (RepositoryException e) {
				throw new ActivitiesException("Unable to list activities", e);
			}
		}
	}

	private void createActivity(Node entity, Combo typeLbCmb, Text titleTxt, Text descTxt, ActivityTable table) {
		String typeLbl = typeLbCmb.getText();
		String title = titleTxt.getText();
		String desc = descTxt.getText();
		String type = ActivityValueCatalogs.getKeyByValue(ActivityValueCatalogs.MAPS_ACTIVITY_TYPES, typeLbl);
		if (createActivity(entity, type, title, desc)) {
			table.refresh();
			typeLbCmb.select(0);
			titleTxt.setText("");
			descTxt.setText("");
			typeLbCmb.setFocus();
		}
	}

//	private boolean createTask(Shell shell, Node relatedEntity) {
//		Session tmpSession = null;
//		Session targetSession = null;
//		try {
//			tmpSession = relatedEntity.getSession().getRepository().login();
//			Node draftTask = systemAppService.createDraftEntity(tmpSession, ActivitiesTypes.ACTIVITIES_TASK);
//			Wizard wizard = systemWorkbenchService.getCreationWizard(draftTask);
//
//			WizardDialog dialog = new WizardDialog(shell, wizard);
//			if (dialog.open() == Window.OK) {
//				List<Node> relatedTo = new ArrayList<Node>();
//				relatedTo.add(tmpSession.getNode(relatedEntity.getPath()));
//				ConnectJcrUtils.setMultipleReferences(draftTask, ActivitiesNames.ACTIVITIES_RELATED_TO, relatedTo);
//
//				targetSession = relatedEntity.getSession().getRepository().login();
//				Node targetParent = targetSession
//						.getNode("/" + systemAppService.getBaseRelPath(ActivitiesTypes.ACTIVITIES_TASK));
//				String currMainType = systemAppService.getMainNodeType(draftTask);
//				Node newTask = systemAppService.publishEntity(targetParent, currMainType, draftTask);
//				systemAppService.saveEntity(newTask, false);
//				relatedEntity.getSession().refresh(true);
//				return true;
//			}
//		} catch (RepositoryException e) {
//			throw new ActivitiesException("Unable to create task node related to " + relatedEntity, e);
//		} finally {
//			JcrUtils.logoutQuietly(tmpSession);
//			JcrUtils.logoutQuietly(targetSession);
//		}
//		return false;
//	}

	private boolean createActivity(Node relatedEntity, String type, String title, String desc) {
		Session tmpSession = null;
		Session targetSession = null;

		try {
			tmpSession = relatedEntity.getSession().getRepository().login(NodeConstants.HOME_WORKSPACE);
			targetSession = relatedEntity.getSession().getRepository().login();
			List<Node> relatedTo = new ArrayList<Node>();
			relatedTo.add(targetSession.getNode(relatedEntity.getPath()));
			Node draftActivity = systemAppService.createDraftEntity(tmpSession, type);
			activitiesService.configureActivity(draftActivity, type, title, desc, relatedTo);

			Node targetParent = targetSession
					.getNode("/" + systemAppService.getBaseRelPath(ActivitiesTypes.ACTIVITIES_ACTIVITY));
			Node createdActivity = systemAppService.publishEntity(targetParent, type, draftActivity);
			systemAppService.saveEntity(createdActivity, false);
			relatedEntity.getSession().refresh(true);
			return true;
		} catch (RepositoryException e) {
			throw new ActivitiesException("Unable to create activity node related to " + relatedEntity, e);
		} finally {
			JcrUtils.logoutQuietly(tmpSession);
			JcrUtils.logoutQuietly(targetSession);
		}
	}
}
