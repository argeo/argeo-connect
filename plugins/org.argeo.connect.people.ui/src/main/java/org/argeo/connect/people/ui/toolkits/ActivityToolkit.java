package org.argeo.connect.people.ui.toolkits;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.ArgeoException;
import org.argeo.connect.people.ActivityService;
import org.argeo.connect.people.ActivityValueCatalogs;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.commands.OpenEntityEditor;
import org.argeo.connect.people.ui.composites.ActivityTableComposite;
import org.argeo.connect.people.ui.utils.PeopleUiUtils;
import org.argeo.connect.people.ui.wizards.NewSimpleTaskWizard;
import org.argeo.eclipse.ui.utils.CommandUtils;
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
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Centralizes creation of commonly used people activity controls (typically
 * Text and composite widget) to be used in various forms.
 */
public class ActivityToolkit {
	// private final static Log log = LogFactory.getLog(ActivityToolkit.class);

	private final FormToolkit toolkit;
	private final IManagedForm form;
	private final PeopleService peopleService;
	private final ActivityService activityService;

	public ActivityToolkit(FormToolkit toolkit, IManagedForm form,
			PeopleService peopleService) {
		this.toolkit = toolkit;
		this.form = form;
		this.peopleService = peopleService;
		this.activityService = peopleService.getActivityService();
	}

	public void populateActivityLogPanel(final Composite parent,
			final Node entity, final String openEditorCmdId) {
		parent.setLayout(new GridLayout()); // .gridLayoutNoBorder());
		try {
			// The Activity bar
			Composite addActivityBar = toolkit.createComposite(parent);
			addActivityBar.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true,
					false));
			GridLayout gl = new GridLayout(7, false);
			gl.marginHeight = gl.marginWidth = 0;
			addActivityBar.setLayout(gl);

			// Activity type
			final Combo addContactCmb = new Combo(addActivityBar, SWT.NONE
					| SWT.READ_ONLY);
			GridData gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
			gd.widthHint = 100;
			addContactCmb.setLayoutData(gd);
			addContactCmb.setItems(ActivityValueCatalogs
					.getActivityTypeLabels());
			addContactCmb.select(0);

			toolkit.adapt(addContactCmb, true, true);

			// Title
			final Text titleTxt = PeopleUiUtils.createGDText(toolkit,
					addActivityBar, "Title",
					"Enter a short title for the activity to create", 150, 1);

			// Description
			final Text descTxt = PeopleUiUtils.createGDText(toolkit,
					addActivityBar, "Description",
					"Enter a description for the activity to create", 300, 1);

			Button validBtn = toolkit.createButton(addActivityBar,
					"Add activity", SWT.PUSH);

			toolkit.createLabel(addActivityBar, " OR ", SWT.NONE);

			// TODO implement add task
			final Link addTaskLk = new Link(addActivityBar, SWT.NONE);
			addTaskLk.setText("<a>Add a task</a>");

			// The Table that displays corresponding activities
			final MyActivityTableCmp tmpCmp = new MyActivityTableCmp(parent,
					SWT.MULTI, entity);
			tmpCmp.populate();
			// TableViewer viewer = tmpCmp.getTableViewer();
			tmpCmp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

			// Life cycle management
			AbstractFormPart sPart = new AbstractFormPart() {
				public void refresh() {
					super.refresh();
					tmpCmp.refresh();
				}
			};
			sPart.initialize(form);
			form.addPart(sPart);

			// Selection and traverse listeners
			validBtn.addSelectionListener(new SelectionAdapter() {
				private static final long serialVersionUID = 1L;

				@Override
				public void widgetSelected(SelectionEvent e) {
					createActivity(entity, addContactCmb, titleTxt, descTxt,
							tmpCmp);
				}
			});

			titleTxt.addTraverseListener(new TraverseListener() {
				private static final long serialVersionUID = 1L;

				@Override
				public void keyTraversed(TraverseEvent e) {
					if (e.keyCode == SWT.CR) {
						e.doit = false;
						createActivity(entity, addContactCmb, titleTxt,
								descTxt, tmpCmp);
					}
				}
			});

			descTxt.addTraverseListener(new TraverseListener() {
				private static final long serialVersionUID = 1L;

				@Override
				public void keyTraversed(TraverseEvent e) {
					if (e.keyCode == SWT.CR) {
						e.doit = false;
						createActivity(entity, addContactCmb, titleTxt,
								descTxt, tmpCmp);
					}
				}
			});

			addTaskLk.addSelectionListener(new SelectionAdapter() {
				private static final long serialVersionUID = 1L;

				@Override
				public void widgetSelected(SelectionEvent e) {
					createTask(addTaskLk.getShell(), entity);
				}
			});

			// Doubleclick listener
			tmpCmp.getTableViewer().addDoubleClickListener(
					new ActivityTableDCL(openEditorCmdId));

		} catch (RepositoryException re) {
			throw new PeopleException("unable to create activity log", re);
		}
	}

	// ///////////////////////
	// HELPERS

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

	private class MyActivityTableCmp extends ActivityTableComposite {
		private static final long serialVersionUID = 1L;
		private Node entity;

		public MyActivityTableCmp(Composite parent, int style, Node entity)
				throws RepositoryException {
			super(parent, style, entity.getSession());
			this.entity = entity;
		}

		protected void refreshFilteredList() {
			try {
				List<Node> nodes = new ArrayList<Node>();
				PropertyIterator pit = entity.getReferences(null);
				while (pit.hasNext()) {
					Property currProp = pit.nextProperty();
					Node currNode = currProp.getParent();
					if (currNode.isNodeType(PeopleTypes.PEOPLE_ACTIVITY))
						nodes.add(currNode);
				}
				getTableViewer().setInput(nodes.toArray());
			} catch (RepositoryException e) {
				throw new ArgeoException("Unable to list activities", e);
			}
		}
	}

	private Node createActivity(Node entity, Combo typeLbCmb, Text titleTxt,
			Text descTxt, ActivityTableComposite table) {
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
			// Create an independent session.
			session = relatedEntity.getSession().getRepository().login();
			NewSimpleTaskWizard wizard = new NewSimpleTaskWizard(session,
					activityService);
			List<Node> relatedTo = new ArrayList<Node>();
			relatedTo.add(relatedEntity);
			wizard.setRelatedTo(relatedTo);
			WizardDialog dialog = new WizardDialog(shell, wizard);
			dialog.open();
			// int result = dialog.open();
			// Node createdTask = wizard.getCreatedTask();
			session.save();
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to create task node", e);
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
			session.save();
			return activity;
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to create activity node", e);
		} finally {
			JcrUtils.logoutQuietly(session);
		}
	}

}