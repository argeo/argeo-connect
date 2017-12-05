package org.argeo.people.workbench.rap;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.argeo.connect.ui.ConnectImages;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.people.ContactValueCatalogs;
import org.argeo.people.PeopleException;
import org.argeo.people.PeopleTypes;
import org.argeo.people.workbench.PeopleWorkbenchService;
import org.argeo.people.workbench.rap.dialogs.NewOrgWizard;
import org.argeo.people.workbench.rap.dialogs.NewPersonWizard;
import org.argeo.people.workbench.rap.parts.MailingListEditor;
import org.argeo.people.workbench.rap.parts.OrgEditor;
import org.argeo.people.workbench.rap.parts.PeopleSearchEntityEditor;
import org.argeo.people.workbench.rap.parts.PersonEditor;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Image;

/**
 * Centralise here the definition of context specific parameter (for instance
 * the name of the command to open editors so that it is easily extended by
 * specific extensions
 */
public class PeopleWorkbenchServiceImpl implements PeopleWorkbenchService {

	@Override
	public String getEntityEditorId(Node curNode) {
		try {
			// if (curNode.isNodeType(ResourcesTypes.RESOURCES_TAG_INSTANCE))
			// return TagEditor.ID;
			// else
			if (curNode.isNodeType(PeopleTypes.PEOPLE_MAILING_LIST))
				return MailingListEditor.ID;
			// else if (curNode.isNodeType(PeopleTypes.PEOPLE_TASK))
			// return TaskEditor.ID;
			// else if (curNode.isNodeType(PeopleTypes.PEOPLE_ACTIVITY))
			// return ActivityEditor.ID;
			else if (curNode.isNodeType(PeopleTypes.PEOPLE_PERSON))
				return PersonEditor.ID;
			else if (curNode.isNodeType(PeopleTypes.PEOPLE_ORG)) {
				return OrgEditor.ID;
				// } else if (curNode.isNodeType(PeopleTypes.PEOPLE_GROUP)) {
				// return GroupEditor.ID;
			} else
				return null;
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to open editor for node", re);
		}
	}

	@Override
	public String getSearchEntityEditorId(String nodeType) {
		if (PeopleTypes.PEOPLE_PERSON.equals(nodeType) || PeopleTypes.PEOPLE_ORG.equals(nodeType)
				|| PeopleTypes.PEOPLE_MAILING_LIST.equals(nodeType))
			return PeopleSearchEntityEditor.ID;
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
	 * Provide the plugin specific ID of the {@code OpenEntityEditor} command
	 * and thus enable the opening plugin specific editors
	 */
	// public String getOpenEntityEditorCmdId() {
	// return OpenEntityEditor.ID;
	// }

	// @Override
	// public String getOpenSearchEntityEditorCmdId() {
	// return OpenSearchEntityEditor.ID;
	// }

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
}
