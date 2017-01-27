package org.argeo.connect.people.workbench.rap.editors.tabs;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.cms.ui.workbench.util.CommandUtils;
import org.argeo.connect.people.ActivityService;
import org.argeo.connect.people.ActivityValueCatalogs;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.workbench.rap.PeopleRapUtils;
import org.argeo.connect.people.workbench.rap.PeopleWorkbenchService;
import org.argeo.connect.people.workbench.rap.commands.OpenEntityEditor;
import org.argeo.connect.people.workbench.rap.editors.parts.ActivityTable;
import org.argeo.connect.people.workbench.rap.editors.util.AbstractPeopleEditor;
import org.argeo.connect.people.workbench.rap.editors.util.LazyCTabControl;
import org.argeo.connect.people.workbench.rap.wizards.NewSimpleTaskWizard;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
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

public class ActivityList extends LazyCTabControl {
	private static final long serialVersionUID = 5906357274592489553L;

	// private final static Log log = LogFactory.getLog(ActivityList.class);

	// Context
	private final AbstractPeopleEditor editor;
	private final FormToolkit toolkit;
	private final PeopleService peopleService;
	private final PeopleWorkbenchService peopleWorkbenchService;
	private final ActivityService activityService;
	private final Node entity;

	// UI Objects
	private MyFormPart myFormPart;
	private MyActivityTableCmp activityTable;

	@Override
	public void refreshPartControl() {
		myFormPart.refresh();
		ActivityList.this.layout(true, true);
	}

	public ActivityList(Composite parent, int style,
			AbstractPeopleEditor editor, PeopleService peopleService,
			PeopleWorkbenchService peopleWorkbenchService, Node entity) {
		super(parent, style);
		this.editor = editor;
		this.toolkit = editor.getFormToolkit();
		this.peopleService = peopleService;
		activityService = peopleService.getActivityService();
		this.peopleWorkbenchService = peopleWorkbenchService;
		this.entity = entity;
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());
		Composite addCmp = null;
		// if (peopleService.getUserAdminService().amIInRole(
		// PeopleConstants.ROLE_MEMBER)) {
		if (ConnectJcrUtils.canEdit(entity)) {
			addCmp = toolkit.createComposite(parent);
			addCmp.setLayoutData(EclipseUiUtils.fillWidth());
		}

		// The Table that displays corresponding activities
		activityTable = new MyActivityTableCmp(parent, SWT.MULTI, entity);
		activityTable.setLayoutData(EclipseUiUtils.fillAll());
		if (addCmp != null)
			addNewActivityPanel(addCmp, entity,
					peopleWorkbenchService.getOpenEntityEditorCmdId(),
					activityTable);
		// Doubleclick listener
		activityTable.getTableViewer().addDoubleClickListener(
				new ActivityTableDCL(peopleWorkbenchService
						.getOpenEntityEditorCmdId()));

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

	private void addNewActivityPanel(final Composite addActivityBar,
			final Node entity, final String openEditorCmdId,
			final MyActivityTableCmp activityTable) {
		// The Add Activity bar
		addActivityBar.setLayoutData(EclipseUiUtils.fillWidth());
		addActivityBar.setLayout(new GridLayout(7, false));

		// Activity type
		final Combo typeCmb = new Combo(addActivityBar, SWT.NONE
				| SWT.READ_ONLY);
		GridData gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		gd.widthHint = 100;
		typeCmb.setLayoutData(gd);
		typeCmb.setItems(ActivityValueCatalogs.getActivityTypeLabels());
		typeCmb.select(0);

		toolkit.adapt(typeCmb, true, true);

		// Title
		final Text titleTxt = PeopleRapUtils.createGDText(toolkit,
				addActivityBar, "Title",
				"Enter a short title for the activity to create", 150, 1);

		// Description
		final Text descTxt = PeopleRapUtils.createGDText(toolkit,
				addActivityBar, "Description",
				"Enter a description for the activity to create", 300, 1);

		Button validBtn = toolkit.createButton(addActivityBar, "Add activity",
				SWT.PUSH);

		toolkit.createLabel(addActivityBar, " OR ", SWT.NONE);

		final Link addTaskLk = new Link(addActivityBar, SWT.NONE);
		addTaskLk.setText("<a>Add a task</a>");

		// Selection and traverse listeners
		validBtn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				createActivity(entity, typeCmb, titleTxt, descTxt,
						activityTable);
			}
		});

		TraverseListener travList = new TraverseListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.keyCode == SWT.CR) {
					e.doit = false;
					createActivity(entity, typeCmb, titleTxt, descTxt,
							activityTable);
				}
			}
		};

		titleTxt.addTraverseListener(travList);
		descTxt.addTraverseListener(travList);

		addTaskLk.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				createTask(addTaskLk.getShell(), entity);
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
			Object obj = ((IStructuredSelection) event.getSelection())
					.getFirstElement();
			try {
				Node currNode;
				if (obj instanceof Node)
					currNode = (Node) obj;
				else
					return;
				String jcrId = currNode.getIdentifier();
				CommandUtils.callCommand(openEditorCmdId,
						OpenEntityEditor.PARAM_JCR_ID, jcrId);
			} catch (RepositoryException re) {
				throw new PeopleException("Unable to open editor for node", re);
			}
		}
	}

	private class MyActivityTableCmp extends ActivityTable {
		private static final long serialVersionUID = 1L;

		public MyActivityTableCmp(Composite parent, int style, Node entity) {
			super(parent, style, peopleService, peopleWorkbenchService, entity);
		}

		protected void refreshFilteredList() {
			try {
				List<Node> nodes = new ArrayList<Node>();
				PropertyIterator pit = entity
						.getReferences(PeopleNames.PEOPLE_RELATED_TO); //
				while (pit.hasNext()) {
					Property currProp = pit.nextProperty();
					Node currNode = currProp.getParent();
					if (currNode.isNodeType(PeopleTypes.PEOPLE_ACTIVITY)
							&& !nodes.contains(currNode))
						nodes.add(currNode);
				}
				getTableViewer().setInput(nodes.toArray());
			} catch (RepositoryException e) {
				throw new PeopleException("Unable to list activities", e);
			}
		}
	}

	private Node createActivity(Node entity, Combo typeLbCmb, Text titleTxt,
			Text descTxt, ActivityTable table) {
		String typeLbl = typeLbCmb.getText();
		String title = titleTxt.getText();
		String desc = descTxt.getText();
		String type = ActivityValueCatalogs.getKeyByValue(
				ActivityValueCatalogs.MAPS_ACTIVITY_TYPES, typeLbl);
		Node activity = createActivity(entity, type, title, desc);
		if (activity != null) {
			table.refresh();
			typeLbCmb.select(0);
			titleTxt.setText("");
			descTxt.setText("");
			typeLbCmb.setFocus();
		}
		return activity;
	}

	private void createTask(Shell shell, Node relatedEntity) {
		Session session = null;
		try {
			// FIXME session management is not clean here
			// Create an independent session.
			session = relatedEntity.getSession().getRepository().login();
			NewSimpleTaskWizard wizard = new NewSimpleTaskWizard(session,
					peopleService);
			List<Node> relatedTo = new ArrayList<Node>();
			relatedTo.add(relatedEntity);
			wizard.setRelatedTo(relatedTo);
			WizardDialog dialog = new WizardDialog(shell, wizard);
			dialog.open();
			session.save();
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to create task node related to "
					+ relatedEntity, e);
		} finally {
			JcrUtils.logoutQuietly(session);
		}
	}

	private Node createActivity(Node relatedEntity, String type, String title,
			String desc) {
		Session session = null;
		try {
			// Create an independent session.
			session = relatedEntity.getSession().getRepository().login();
			List<Node> relatedTo = new ArrayList<Node>();
			relatedTo.add(relatedEntity);
			Node activity = activityService.createActivity(session, type,
					title, desc, relatedTo);
			ConnectJcrUtils.saveAndPublish(activity, true);
			return activity;
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to create activity node", e);
		} finally {
			JcrUtils.logoutQuietly(session);
		}
	}
}