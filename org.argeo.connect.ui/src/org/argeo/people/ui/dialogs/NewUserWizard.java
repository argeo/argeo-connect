package org.argeo.people.ui.dialogs;

import javax.jcr.Node;

import org.argeo.connect.UserAdminService;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.people.PeopleException;
import org.argeo.people.PeopleNames;
import org.argeo.people.PeopleService;
import org.argeo.people.ui.PeopleMsg;
import org.eclipse.jface.wizard.Wizard;

/** Ask first & last name. Update the passed node on finish */
public class NewUserWizard extends Wizard implements PeopleNames {
	// private final static Log log = LogFactory.getLog(NewPersonWizard.class);

	// Context
	private Node person;

	private NewPersonPage newPersonPage;
	private UserAdminService userAdminService;
	private PeopleService peopleService;
	private ResourcesService resourcesService;

	public NewUserWizard(Node person, UserAdminService userAdminService, PeopleService peopleService,
			ResourcesService resourcesService) {
		this.person = person;
		this.userAdminService = userAdminService;
		this.peopleService = peopleService;
		this.resourcesService = resourcesService;
	}

	@Override
	public void addPages() {
		try {
			newPersonPage = new NewPersonPage("New person page");
			addPage(newPersonPage);
		} catch (Exception e) {
			throw new PeopleException("Cannot add page to wizard", e);
		}
		setWindowTitle(PeopleMsg.personWizardWindowTitle.lead());
	}

	/**
	 * Called when the user click on 'Finish' in the wizard. The task is then
	 * created and the corresponding session saved.
	 */
	@Override
	public boolean performFinish() {
		newPersonPage.updateNode(person, peopleService, resourcesService);
		userAdminService.createUserFromPerson(person);
		return true;
	}

	@Override
	public boolean performCancel() {
		return true;
	}

	@Override
	public boolean canFinish() {
		return newPersonPage.isPageComplete();
	}

}
