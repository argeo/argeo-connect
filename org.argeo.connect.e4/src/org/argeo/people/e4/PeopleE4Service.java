package org.argeo.people.e4;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.argeo.connect.UserAdminService;
import org.argeo.connect.e4.AppE4Service;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.ui.ConnectImages;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.people.ContactValueCatalogs;
import org.argeo.people.PeopleException;
import org.argeo.people.PeopleService;
import org.argeo.people.PeopleTypes;
import org.argeo.people.ui.PeopleWorkbenchService;
import org.argeo.people.ui.dialogs.NewOrgWizard;
import org.argeo.people.ui.dialogs.NewPersonWizard;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Image;

/**
 * Centralise here the definition of context specific parameter (for instance
 * the name of the command to open editors so that it is easily extended by
 * specific extensions
 */
public class PeopleE4Service implements PeopleWorkbenchService, AppE4Service {
	private UserAdminService userAdminService;
	private PeopleService peopleService;
	private ResourcesService resourcesService;

	@Override
	public String getEntityEditorId(Node curNode) {
		try {
			if (curNode.isNodeType(PeopleTypes.PEOPLE_MAILING_LIST)) {
				// return MailingListEditor.ID;
			} else if (curNode.isNodeType(PeopleTypes.PEOPLE_PERSON)) {
				return "org.argeo.suite.e4.partdescriptor.personEditor";
			} else if (curNode.isNodeType(PeopleTypes.PEOPLE_ORG)) {
				return "org.argeo.suite.e4.partdescriptor.orgEditor";
			} else
				return null;
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to open editor for node", re);
		}
		return null;
	}

	@Override
	public String getSearchEntityEditorId(String nodeType) {
		if (PeopleTypes.PEOPLE_PERSON.equals(nodeType) || PeopleTypes.PEOPLE_ORG.equals(nodeType)
				|| PeopleTypes.PEOPLE_MAILING_LIST.equals(nodeType))
			return "org.argeo.suite.e4.partdescriptor.searchEntityPart";
		return null;
	}

	@Override
	public Image getIconForType(Node entity) {
		try {
			if (entity.isNodeType(PeopleTypes.PEOPLE_PERSON))
				return ConnectImages.PERSON;
			else if (entity.isNodeType(PeopleTypes.PEOPLE_ORG))
				return ConnectImages.ORG;
			else if (entity.isNodeType(PeopleTypes.PEOPLE_MAILING_LIST))
				return ConnectImages.MAILING_LIST;
			else if (entity.isNodeType(PeopleTypes.PEOPLE_CONTACT))
				return getContactIcon(entity);
			else
				return null;
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to get image for node", re);
		}
	}

	@Override
	public Wizard getCreationWizard(Node node) {
		if (ConnectJcrUtils.isNodeType(node, PeopleTypes.PEOPLE_PERSON))
			return new NewPersonWizard(node);
		else if (ConnectJcrUtils.isNodeType(node, PeopleTypes.PEOPLE_ORG))
			return new NewOrgWizard(node);
		else
			return null;
		// throw new PeopleException("No defined wizard for node " + node);
	}

	/**
	 * Specific management of contact icons. Might be overridden by client
	 * application
	 */
	protected Image getContactIcon(Node entity) throws RepositoryException {
		Node contactable = entity.getParent().getParent();
		String category = ConnectJcrUtils.get(entity, Property.JCR_TITLE);

		// EMAIL
		if (entity.isNodeType(PeopleTypes.PEOPLE_MAIL)) {
			return ConnectImages.DEFAULT_MAIL;
		}
		// PHONES
		else if (entity.isNodeType(PeopleTypes.PEOPLE_MOBILE))
			return ConnectImages.MOBILE;
		else if (entity.isNodeType(PeopleTypes.PEOPLE_FAX))
			return ConnectImages.FAX;
		else if (entity.isNodeType(PeopleTypes.PEOPLE_TELEPHONE_NUMBER) || entity.isNodeType(PeopleTypes.PEOPLE_PHONE))
			return ConnectImages.DEFAULT_PHONE;
		// ADDRESS
		else if (entity.isNodeType(PeopleTypes.PEOPLE_POSTAL_ADDRESS)) {
			if (contactable.isNodeType(PeopleTypes.PEOPLE_PERSON))
				if (entity.isNodeType(PeopleTypes.PEOPLE_CONTACT_REF))
					return ConnectImages.WORK;
				else
					return ConnectImages.DEFAULT_ADDRESS;
			else
				return ConnectImages.WORK;
		}
		// URL
		else if (entity.isNodeType(PeopleTypes.PEOPLE_URL)) {
			// return ContactImages.PRIVATE_HOME_PAGE;
			return ConnectImages.DEFAULT_URL;
		}
		// SOCIAL MEDIA
		else if (entity.isNodeType(PeopleTypes.PEOPLE_SOCIAL_MEDIA)) {
			if (ContactValueCatalogs.CONTACT_CAT_GOOGLEPLUS.equals(category))
				return ConnectImages.GOOGLEPLUS;
			else if (ContactValueCatalogs.CONTACT_CAT_FACEBOOK.equals(category))
				return ConnectImages.FACEBOOK;
			else if (ContactValueCatalogs.CONTACT_CAT_LINKEDIN.equals(category))
				return ConnectImages.LINKEDIN;
			else if (ContactValueCatalogs.CONTACT_CAT_XING.equals(category))
				return ConnectImages.XING;
			return ConnectImages.DEFAULT_SOCIAL_MEDIA;
		}
		// IMPP
		else if (entity.isNodeType(PeopleTypes.PEOPLE_IMPP)) {
			return ConnectImages.DEFAULT_IMPP;
		}
		return null;
	}

	public void setUserAdminService(UserAdminService userAdminService) {
		this.userAdminService = userAdminService;
	}

	public void setPeopleService(PeopleService peopleService) {
		this.peopleService = peopleService;
	}

	public void setResourcesService(ResourcesService resourcesService) {
		this.resourcesService = resourcesService;
	}

}
