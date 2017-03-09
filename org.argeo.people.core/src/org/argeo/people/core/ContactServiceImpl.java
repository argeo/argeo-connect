package org.argeo.people.core;

import static org.argeo.people.ContactValueCatalogs.ARRAY_ORG_PHONES;

import java.awt.Image;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.people.ContactService;
import org.argeo.people.ContactValueCatalogs;
import org.argeo.people.PeopleException;
import org.argeo.people.PeopleNames;
import org.argeo.people.PeopleTypes;

/**
 * Canonical implementation of the People's {@link ContactService}. Among
 * others, it defines the various possible values of a given contact property
 * given the already defined property of this contact instance
 */
public class ContactServiceImpl implements ContactService, PeopleNames {

	public ContactServiceImpl() {
	}

	@Override
	public String[] getKnownContactTypes() {
		return PeopleTypes.KNOWN_CONTACT_TYPES;
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
	public String[] getContactPossibleCategories(Node contact) {
		try {
			// Retrieves parent entity to enable decision
			Node entity = contact.getParent().getParent();
			boolean isPro = contact.isNodeType(PeopleTypes.PEOPLE_CONTACT_REF);

			if (contact.isNodeType(PeopleTypes.PEOPLE_PHONE)) {
				if (entity.isNodeType(PeopleTypes.PEOPLE_PERSON)) {
					if (isPro)
						return ContactValueCatalogs.ARRAY_PERSON_PRO_PHONES;
					else
						return ContactValueCatalogs.ARRAY_PERSON_PRIVATE_PHONES;
				} else if (entity.isNodeType(PeopleTypes.PEOPLE_ORG))
					return ContactValueCatalogs.ARRAY_ORG_PHONES;
			} else if (contact.isNodeType(PeopleTypes.PEOPLE_POSTAL_ADDRESS)) {
				if (entity.isNodeType(PeopleTypes.PEOPLE_PERSON))
					if (isPro)
						return ContactValueCatalogs.ARRAY_PERSON_WORK_ADDRESSES;
					else
						return ContactValueCatalogs.ARRAY_PERSON_HOME_ADDRESSES;
				else if (entity.isNodeType(PeopleTypes.PEOPLE_ORG))
					return ContactValueCatalogs.ARRAY_ORG_ADDRESSES;
			} else if (entity.isNodeType(PeopleTypes.PEOPLE_SOCIAL_MEDIA))
				return ContactValueCatalogs.ARRAY_SOCIAL_MEDIA;
			else if (entity.isNodeType(PeopleTypes.PEOPLE_IMPP))
				return ContactValueCatalogs.ARRAY_IMPP;
			return new String[0];
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to get category list for " + contact, re);
		}
	}

	@Override
	public String[] getContactCategories(String contactableType, String contactType, boolean isPro) {

		if (PeopleTypes.PEOPLE_TELEPHONE_NUMBER.equals(contactType)) {
			if (PeopleTypes.PEOPLE_PERSON.equals(contactableType)) {
				if (isPro)
					return ContactValueCatalogs.ARRAY_PERSON_PRO_PHONES;
				else
					return ContactValueCatalogs.ARRAY_PERSON_PRIVATE_PHONES;
			} else if (PeopleTypes.PEOPLE_ORG.equals(contactableType))
				return ARRAY_ORG_PHONES;
		} else if (PeopleTypes.PEOPLE_MOBILE.equals(contactType)) {
			return ContactValueCatalogs.ARRAY_MOBILES;
		} else if (PeopleTypes.PEOPLE_FAX.equals(contactType)) {
			return ContactValueCatalogs.ARRAY_FAXES;
		} else if (PeopleTypes.PEOPLE_POSTAL_ADDRESS.equals(contactType)) {
			if (PeopleTypes.PEOPLE_PERSON.equals(contactableType)) {
				if (isPro)
					return ContactValueCatalogs.ARRAY_PERSON_WORK_ADDRESSES;
				else
					return ContactValueCatalogs.ARRAY_PERSON_HOME_ADDRESSES;
			} else if (PeopleTypes.PEOPLE_ORG.equals(contactableType))
				return ContactValueCatalogs.ARRAY_ORG_ADDRESSES;
		} else if (PeopleTypes.PEOPLE_SOCIAL_MEDIA.equals(contactType))
			return ContactValueCatalogs.ARRAY_SOCIAL_MEDIA;
		else if (PeopleTypes.PEOPLE_IMPP.equals(contactType))
			return ContactValueCatalogs.ARRAY_IMPP;
		return null;
	}

	@Override
	public Image getContactIcon(Node contact) {
		return null;
	}
}
