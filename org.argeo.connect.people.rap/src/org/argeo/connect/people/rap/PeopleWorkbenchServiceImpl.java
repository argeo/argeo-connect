package org.argeo.connect.people.rap;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.connect.people.ContactValueCatalogs;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.rap.commands.OpenEntityEditor;
import org.argeo.connect.people.rap.commands.OpenSearchEntityEditor;
import org.argeo.connect.people.rap.wizards.NewOrgWizard;
import org.argeo.connect.people.rap.wizards.NewPersonWizard;
import org.argeo.connect.people.util.JcrUiUtils;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Image;

/**
 * Centralize here the definition of context specific parameter (for instance
 * the name of the command to open editors so that it is easily extended by
 * specific extensions
 */
public class PeopleWorkbenchServiceImpl implements PeopleWorkbenchService {

	/**
	 * Provide the plugin specific ID of the {@code OpenEntityEditor} command
	 * and thus enable the opening plugin specific editors
	 */
	public String getOpenEntityEditorCmdId() {
		return OpenEntityEditor.ID;
	}

	@Override
	public String getOpenSearchEntityEditorCmdId() {
		return OpenSearchEntityEditor.ID;
	}

	@Override
	public String getOpenFileCmdId() {
		// TODO clean this.
		throw new PeopleException("OpenFile command is undefined for "
				+ "PeopleUiService base implementation");
	}

	@Override
	public String getDefaultEditorId() {
		throw new PeopleException("No default editor has been defined for "
				+ "PeopleUiService base implementation");
	}

	@Override
	public Wizard getCreationWizard(PeopleService peopleService, Node node) {
		if (JcrUiUtils.isNodeType(node, PeopleTypes.PEOPLE_PERSON))
			return new NewPersonWizard(node);
		else if (JcrUiUtils.isNodeType(node, PeopleTypes.PEOPLE_ORG))
			return new NewOrgWizard(node);
		else
			throw new PeopleException("No defined wizard for node " + node);
	}

	@Override
	public Image getIconForType(Node entity) {
		try {
			if (entity.isNodeType(PeopleTypes.PEOPLE_PERSON))
				return PeopleRapImages.ICON_PERSON;
			else if (entity.isNodeType(PeopleTypes.PEOPLE_ORG))
				return PeopleRapImages.ICON_ORG;
			else if (entity.isNodeType(PeopleTypes.PEOPLE_MAILING_LIST))
				return PeopleRapImages.ICON_MAILING_LIST;
			else if (entity.isNodeType(PeopleTypes.PEOPLE_GROUP))
				return PeopleRapImages.ICON_GROUP;
			else if (entity.isNodeType(PeopleTypes.PEOPLE_CONTACT))
				return getContactIcon(entity);
			else
				return null;
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to get image for node", re);
		}
	}

	/**
	 * Specific management of contact icons. Might be overridden by client
	 * application
	 */
	protected Image getContactIcon(Node entity) throws RepositoryException {

		Node contactable = entity.getParent().getParent();
		String category = JcrUiUtils.get(entity,
				PeopleNames.PEOPLE_CONTACT_CATEGORY);
		String nature = JcrUiUtils.get(entity,
				PeopleNames.PEOPLE_CONTACT_NATURE);
		// EMAIL
		if (entity.isNodeType(PeopleTypes.PEOPLE_EMAIL)) {
			return ContactImages.DEFAULT_MAIL;
		}
		// PHONE
		else if (entity.isNodeType(PeopleTypes.PEOPLE_PHONE)) {
			if (ContactValueCatalogs.CONTACT_CAT_MOBILE.equals(category))
				return ContactImages.MOBILE;
			else if (ContactValueCatalogs.CONTACT_CAT_FAX.equals(category))
				return ContactImages.FAX;
			else
				return ContactImages.DEFAULT_PHONE;
		}
		// ADDRESS
		else if (entity.isNodeType(PeopleTypes.PEOPLE_ADDRESS)) {
			if (contactable.isNodeType(PeopleTypes.PEOPLE_PERSON))
				if (ContactValueCatalogs.CONTACT_NATURE_PRO.equals(nature))
					return ContactImages.WORK;
				else
					return ContactImages.DEFAULT_ADDRESS;
			else
				return ContactImages.WORK;
		}
		// URL
		else if (entity.isNodeType(PeopleTypes.PEOPLE_URL)) {
			// return ContactImages.PRIVATE_HOME_PAGE;
			return ContactImages.DEFAULT_URL;
		}
		// SOCIAL MEDIA
		else if (entity.isNodeType(PeopleTypes.PEOPLE_SOCIAL_MEDIA)) {

			if (ContactValueCatalogs.CONTACT_CAT_GOOGLEPLUS.equals(category))
				return ContactImages.GOOGLEPLUS;
			else if (ContactValueCatalogs.CONTACT_CAT_FACEBOOK.equals(category))
				return ContactImages.FACEBOOK;
			else if (ContactValueCatalogs.CONTACT_CAT_LINKEDIN.equals(category))
				return ContactImages.LINKEDIN;
			else if (ContactValueCatalogs.CONTACT_CAT_XING.equals(category))
				return ContactImages.XING;
			return ContactImages.DEFAULT_SOCIAL_MEDIA;
		}
		// // IMPP
		else if (entity.isNodeType(PeopleTypes.PEOPLE_IMPP)) {
			return ContactImages.DEFAULT_IMPP;
		}
		return null;
	}
}
