package org.argeo.connect.people.ui.wizards;

import java.util.Calendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.ActivitiesImages;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.dialogs.PickUpByNodeTypeDialog;
import org.argeo.connect.people.ui.dialogs.PickUpRelatedDialog;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;

/**
 * Generic wizard to created a new task. Caller might use setRelatedTo method to
 * add some related entities after creation and before opening the wizard
 * dialog.
 * 
 * A new session is started using the parent Node. This new session is used to
 * save the newly created task upon finish or to discard information.
 */

public class CreateSimpleTaskWizard extends Wizard {
	private final static Log log = LogFactory
			.getLog(CreateSimpleTaskWizard.class);

	private Session currSession;
	private PeopleService peopleService;
	private Node parentNode;
	// Business objects
	private List<Node> relatedTo;
	protected Node assignedToGroupNode;
	protected Calendar dueDate;
	protected Calendar awakeDate;

	
	public void setRelatedTo(List<Node> relatedTo) {
		this.relatedTo = relatedTo;
	}

	// This page widgets
	protected Text titleTxt;
	protected Text descTxt;
	private DateTime dueDateDt;
	
	
	
	protected TableViewer itemsViewer;

	public CreateSimpleTaskWizard(PeopleService peopleService, Node parentNode) {
		try {
			this.peopleService = peopleService;
			// creates its own session to create the task.
			String path = parentNode.getPath();
			this.currSession = parentNode.getSession().getRepository().login();
			this.parentNode = currSession.getNode(path);
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to login to add an entity", e);
		}
	}

	// //////////////////////////////////////
	// Generic part to use when Overriding

	// /**
	// * Called by the wizard performFinish() method. Overwrite to perform real
	// * addition of new items to a given Node depending on its nature, dealing
	// * with duplicate and check out state among others.
	// */
	// protected abstract boolean addChildren(List<Node> newChildren)
	// throws RepositoryException;
	//
	// /** performs the effective refresh of the list */
	// protected abstract void refreshFilteredList();
	//
	// // /** Define the display message for current Wizard */
	// // protected abstract String getCurrDescription();
	//
	// /**
	// * Overwrite to provide the correct Label provider depending on the
	// * currently being added type of entities
	// */
	// protected abstract EntitySingleColumnLabelProvider defineLabelProvider();

	// Exposes to children
	protected Session getSession() {
		return currSession;
	}

	protected PeopleService getPeopleService() {
		return peopleService;
	}

	@Override
	public void addPages() {
		// Configure the wizard	
		setDefaultPageImageDescriptor(ActivitiesImages.TODO_IMGDESC);
		try {
			SelectChildrenPage page = new SelectChildrenPage("Main page");
			addPage(page);
		} catch (Exception e) {
			throw new PeopleException("Cannot add page to wizard", e);
		}
	}

	/**
	 * Called when the user click on 'Finish' in the wizard. The task is then
	 * created and the corresponding session saved.
	 */
	@Override
	public boolean performFinish() {
		log.debug("Implement creation and save");
		//
		// try {
		// } catch (Exception e) {
		// throw new PeopleException("Unable to finish", e);
		// } finally {
		// //JcrUtils.logoutQuietly(currSession);
		// }
		return true;
	}

	@Override
	public boolean performCancel() {
		JcrUtils.discardQuietly(currSession);
		// JcrUtils.logoutQuietly(currSession);
		return true;
	}

	@Override
	public boolean canFinish() {
		return true;
	}

	@Override
	public void dispose() {
		super.dispose();
		JcrUtils.logoutQuietly(currSession);
	}

	protected class SelectChildrenPage extends WizardPage {
		private static final long serialVersionUID = 1L;

		public SelectChildrenPage(String pageName) {
			super(pageName);
			setTitle("New task...");
			setMessage("Create a new task with basic information.");
			// setDescription("Create a new task");
		}

		public void createControl(Composite parent) {
			parent.setLayout(new GridLayout(3, false));
			
			// TITLE 
			createLabel(parent, "Title");
			titleTxt = new Text(parent, SWT.BORDER);
			titleTxt.setMessage("A title for the new task");
			GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
			gd.horizontalSpan = 2; 
			titleTxt.setLayoutData(gd);

			// ASSIGNED TO
			createLabel(parent, "Assigned to");
			final Text assignedToTxt = new Text(parent, SWT.BORDER | SWT.NO_FOCUS);
			assignedToTxt.setMessage("Assign a group to manage this task");
			gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
			assignedToTxt.setLayoutData(gd);
			assignedToTxt.setEnabled(false);
			
			Link assignedToLk = new Link(parent, SWT.NONE);
			assignedToLk.setText("<a>Pick up</a>");
			assignedToLk.addSelectionListener(new SelectionAdapter() {
				private static final long serialVersionUID = 1L;

				@Override
				public void widgetSelected(final SelectionEvent event) {
					try {
						PickUpByNodeTypeDialog diag = new PickUpByNodeTypeDialog(
								assignedToTxt.getShell(), "Choose a group",
								currSession, PeopleTypes.PEOPLE_GROUP);
						diag.open();
						assignedToGroupNode = diag.getSelected();
						// TODO use correct group name
						assignedToTxt.setText(assignedToGroupNode.getName());
					} catch (RepositoryException e) {
						throw new PeopleException(
								"Unable to pick up a group node to assign to", e);
					}
				}
			});
			
			// DUE DATE
			createLabel(parent, "Due date");
			dueDateDt = new DateTime(parent, SWT.RIGHT | SWT.DATE | SWT.MEDIUM
					| SWT.DROP_DOWN);
			dueDateDt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
					2, 1));
			
			// DESCRIPTION 
			Label label = new Label(parent, SWT.RIGHT | SWT.TOP);
			label.setText("Description");
			gd = new GridData(SWT.RIGHT, SWT.TOP, false, false);
			label.setLayoutData(gd);
			
			descTxt = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.WRAP);
			descTxt.setMessage("A description");
			gd = new GridData(SWT.FILL, SWT.FILL, true, true);
			gd.horizontalSpan = 2; 
			gd.heightHint = 150;
			descTxt.setLayoutData(gd);
			
			// Don't forget this.
			setControl(titleTxt);
		}

	}
	
	private void createLabel(Composite parent, String text){
		Label label = new Label(parent, SWT.RIGHT);
		label.setText(text);
		
		GridData gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
		label.setLayoutData(gd);
	}
	
}
