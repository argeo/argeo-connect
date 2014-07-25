package org.argeo.connect.people.core;

import java.awt.Image;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.connect.people.ContactService;
import org.argeo.connect.people.ContactValueCatalogs;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.utils.CommonsJcrUtils;

public class ContactServiceImpl implements ContactService, PeopleNames {

	// private PeopleService peopleService;

	public ContactServiceImpl(PeopleService peopleService) {
		// this.peopleService = peopleService;
	}

	@Override
	public String[] getKnownContactTypes() {
		return ContactValueCatalogs.ARRAY_CONTACT_TYPES;
	}

	@Override
	public String[] getContactTypeLabels(Node entity) {
		if (CommonsJcrUtils.isNodeType(entity, PeopleTypes.PEOPLE_PERSON)
				|| CommonsJcrUtils.isNodeType(entity, PeopleTypes.PEOPLE_ORG))
			return ContactValueCatalogs.ARRAY_CONTACT_TYPES;
		return null;
	}

	@Override
	public String[] getContactPossibleValues(Node contact, String property) {
		try {
			// retrieve info to ease decision
			Node entity = contact.getParent().getParent();
			String nature = CommonsJcrUtils.get(contact, PEOPLE_CONTACT_NATURE);
			if (PEOPLE_CONTACT_CATEGORY.equals(property)) {
				if (contact.isNodeType(PeopleTypes.PEOPLE_PHONE)) {
					if (entity.isNodeType(PeopleTypes.PEOPLE_PERSON)) {
						if (CommonsJcrUtils.checkNotEmptyString(nature)
								&& (nature
										.equals(ContactValueCatalogs.CONTACT_NATURE_PRIVATE) || nature
										.equals(ContactValueCatalogs.CONTACT_OTHER)))
							return ContactValueCatalogs.ARRAY_PERSON_PRIVATE_PHONES;
						else
							return ContactValueCatalogs.ARRAY_PERSON_PRO_PHONES;
					} else if (entity.isNodeType(PeopleTypes.PEOPLE_ORG))
						return ContactValueCatalogs.ARRAY_ORG_PHONES;
				} else if (contact.isNodeType(PeopleTypes.PEOPLE_ADDRESS)) {
					if (entity.isNodeType(PeopleTypes.PEOPLE_PERSON))
						if (CommonsJcrUtils.checkNotEmptyString(nature)
								&& (nature
										.equals(ContactValueCatalogs.CONTACT_NATURE_PRIVATE) || nature
										.equals(ContactValueCatalogs.CONTACT_OTHER)))
							return ContactValueCatalogs.ARRAY_PERSON_HOME_ADDRESSES;
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
			throw new PeopleException("unable to get " + property
					+ " value list for " + contact, re);
		}
	}

	@Override
	public String[] getContactCategories(String contactableType,
			String contactType, String nature) {

		if (PeopleTypes.PEOPLE_PHONE.equals(contactType)) {
			if (PeopleTypes.PEOPLE_PERSON.equals(contactableType)) {
				if (CommonsJcrUtils.checkNotEmptyString(nature)
						&& (nature
								.equals(ContactValueCatalogs.CONTACT_NATURE_PRIVATE) || nature
								.equals(ContactValueCatalogs.CONTACT_OTHER)))
					return ContactValueCatalogs.ARRAY_PERSON_PRIVATE_PHONES;
				else
					return ContactValueCatalogs.ARRAY_PERSON_PRO_PHONES;
			} else if (PeopleTypes.PEOPLE_ORG.equals(contactableType))
				return ContactValueCatalogs.ARRAY_ORG_PHONES;
		} else if (PeopleTypes.PEOPLE_ADDRESS.equals(contactType)) {
			if (PeopleTypes.PEOPLE_PERSON.equals(contactableType)) {
				if (CommonsJcrUtils.checkNotEmptyString(nature)
						&& (nature
								.equals(ContactValueCatalogs.CONTACT_NATURE_PRIVATE) || nature
								.equals(ContactValueCatalogs.CONTACT_OTHER)))
					return ContactValueCatalogs.ARRAY_PERSON_HOME_ADDRESSES;
				else
					return ContactValueCatalogs.ARRAY_PERSON_HOME_ADDRESSES;
			} else if (PeopleTypes.PEOPLE_ORG.equals(contactType))
				return ContactValueCatalogs.ARRAY_ORG_ADDRESSES;
		} else if (PeopleTypes.PEOPLE_SOCIAL_MEDIA.equals(contactType))
			return ContactValueCatalogs.ARRAY_SOCIAL_MEDIA;
		else if (PeopleTypes.PEOPLE_IMPP.equals(contactType))
			return ContactValueCatalogs.ARRAY_IMPP;

		return null;
	}

	@Override
	public Image getContactIcon(Node contact) {
		// TODO Auto-generated method stub
		return null;
	}

}
