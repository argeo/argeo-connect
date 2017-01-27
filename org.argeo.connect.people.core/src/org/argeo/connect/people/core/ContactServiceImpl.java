package org.argeo.connect.people.core;

import static org.argeo.connect.people.ContactValueCatalogs.ARRAY_IMPP;
import static org.argeo.connect.people.ContactValueCatalogs.ARRAY_ORG_ADDRESSES;
import static org.argeo.connect.people.ContactValueCatalogs.ARRAY_ORG_PHONES;
import static org.argeo.connect.people.ContactValueCatalogs.ARRAY_PERSON_HOME_ADDRESSES;
import static org.argeo.connect.people.ContactValueCatalogs.ARRAY_PERSON_PRIVATE_PHONES;
import static org.argeo.connect.people.ContactValueCatalogs.ARRAY_PERSON_PRO_PHONES;
import static org.argeo.connect.people.ContactValueCatalogs.ARRAY_PERSON_WORK_ADDRESSES;
import static org.argeo.connect.people.ContactValueCatalogs.CONTACT_NATURE_PRIVATE;
import static org.argeo.connect.people.ContactValueCatalogs.CONTACT_NATURE_PRO;
import static org.argeo.connect.people.ContactValueCatalogs.CONTACT_OTHER;
import static org.argeo.eclipse.ui.EclipseUiUtils.notEmpty;

import java.awt.Image;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.connect.people.ContactService;
import org.argeo.connect.people.ContactValueCatalogs;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.util.ConnectJcrUtils;

/**
 * Canonical implementation of the People's {@link ContactService}. Among
 * others, it defines the various possible values of a given contact property
 * given the already defined property of this contact instance
 */
public class ContactServiceImpl implements ContactService, PeopleNames {

	// private PeopleService peopleService;

	public ContactServiceImpl(PeopleService peopleService) {
		// this.peopleService = peopleService;
	}

	@Override
	public String[] getKnownContactTypes() {
		String[] knownTypes = { PeopleTypes.PEOPLE_EMAIL, PeopleTypes.PEOPLE_PHONE, PeopleTypes.PEOPLE_SOCIAL_MEDIA,
				PeopleTypes.PEOPLE_IMPP, PeopleTypes.PEOPLE_URL, PeopleTypes.PEOPLE_ADDRESS };
		return knownTypes;
	}

	@Override
	public String[] getKnownContactLabels() {
		return ContactValueCatalogs.ARRAY_CONTACT_TYPES;
	}

	@Override
	public String[] getContactTypeLabels(Node entity) {
		if (ConnectJcrUtils.isNodeType(entity, PeopleTypes.PEOPLE_PERSON)
				|| ConnectJcrUtils.isNodeType(entity, PeopleTypes.PEOPLE_ORG))
			return ContactValueCatalogs.ARRAY_CONTACT_TYPES;
		else
			return null;
	}

	@Override
	public String[] getContactPossibleValues(Node contact, String property) {
		try {
			// Retrieves parent entity to enable decision
			Node entity = contact.getParent().getParent();
			String nature = ConnectJcrUtils.get(contact, PEOPLE_CONTACT_NATURE);
			if (PEOPLE_CONTACT_CATEGORY.equals(property)) {
				if (contact.isNodeType(PeopleTypes.PEOPLE_PHONE)) {
					if (entity.isNodeType(PeopleTypes.PEOPLE_PERSON)) {
						if (notEmpty(nature) && (nature.equals(ContactValueCatalogs.CONTACT_NATURE_PRIVATE)
								|| nature.equals(ContactValueCatalogs.CONTACT_OTHER)))
							return ContactValueCatalogs.ARRAY_PERSON_PRIVATE_PHONES;
						else
							return ContactValueCatalogs.ARRAY_PERSON_PRO_PHONES;
					} else if (entity.isNodeType(PeopleTypes.PEOPLE_ORG))
						return ContactValueCatalogs.ARRAY_ORG_PHONES;
				} else if (contact.isNodeType(PeopleTypes.PEOPLE_ADDRESS)) {
					if (entity.isNodeType(PeopleTypes.PEOPLE_PERSON))
						if (notEmpty(nature) && nature.equals(ContactValueCatalogs.CONTACT_NATURE_PRO))
							return ContactValueCatalogs.ARRAY_PERSON_WORK_ADDRESSES;
						else
							return ContactValueCatalogs.ARRAY_PERSON_HOME_ADDRESSES;
					else if (entity.isNodeType(PeopleTypes.PEOPLE_ORG))
						return ContactValueCatalogs.ARRAY_ORG_ADDRESSES;
				} else if (entity.isNodeType(PeopleTypes.PEOPLE_SOCIAL_MEDIA))
					return ContactValueCatalogs.ARRAY_SOCIAL_MEDIA;
				else if (entity.isNodeType(PeopleTypes.PEOPLE_IMPP))
					return ContactValueCatalogs.ARRAY_IMPP;
			}
			return new String[0];
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to get " + property + " value list for " + contact, re);
		}
	}

	@Override
	public String[] getContactCategories(String contactableType, String contactType, String nature) {

		if (PeopleTypes.PEOPLE_PHONE.equals(contactType)) {
			if (PeopleTypes.PEOPLE_PERSON.equals(contactableType)) {
				if (notEmpty(nature) && (nature.equals(CONTACT_NATURE_PRIVATE) || nature.equals(CONTACT_OTHER)))
					return ARRAY_PERSON_PRIVATE_PHONES;
				else
					return ARRAY_PERSON_PRO_PHONES;
			} else if (PeopleTypes.PEOPLE_ORG.equals(contactableType))
				return ARRAY_ORG_PHONES;
		} else if (PeopleTypes.PEOPLE_ADDRESS.equals(contactType)) {
			if (PeopleTypes.PEOPLE_PERSON.equals(contactableType)) {
				if (notEmpty(nature) && nature.equals(CONTACT_NATURE_PRO))
					return ARRAY_PERSON_WORK_ADDRESSES;
				else
					return ARRAY_PERSON_HOME_ADDRESSES;
			} else if (PeopleTypes.PEOPLE_ORG.equals(contactableType))
				return ARRAY_ORG_ADDRESSES;
		} else if (PeopleTypes.PEOPLE_SOCIAL_MEDIA.equals(contactType))
			return ContactValueCatalogs.ARRAY_SOCIAL_MEDIA;
		else if (PeopleTypes.PEOPLE_IMPP.equals(contactType))
			return ARRAY_IMPP;

		return null;
	}

	@Override
	public Image getContactIcon(Node contact) {
		return null;
	}
}