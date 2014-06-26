package org.argeo.connect.people.ui.providers;

import org.argeo.connect.people.ContactValueCatalogs;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.ContactImages;
import org.eclipse.swt.graphics.Image;

/**
 * Centralize management of icon retrieval depending on a given Node
 * 
 * TODO move this in the PeopleUiService
 * 
 */
public class PeopleImageProvider {

	/**
	 * Return the correspondi ng image if found or default one depending only on
	 * the contact type
	 */
	public Image getContactIcon(String entityNType, String contactNType,
			String nature, String category) {

		// EMAIL
		if (PeopleTypes.PEOPLE_EMAIL.equals(contactNType)) {
			return ContactImages.DEFAULT_MAIL;
		}
		// PHONE
		else if (PeopleTypes.PEOPLE_PHONE.equals(contactNType)) {
			if (ContactValueCatalogs.CONTACT_CAT_FAX.equals(category))
				return ContactImages.FAX;
			else if (ContactValueCatalogs.CONTACT_CAT_MOBILE.equals(category))
				return ContactImages.MOBILE;
			if (entityNType.equals(PeopleTypes.PEOPLE_PERSON)) {
				if (ContactValueCatalogs.CONTACT_CAT_PRO_DIRECT
						.equals(category))
					return ContactImages.PHONE_DIRECT;
				else if (ContactValueCatalogs.CONTACT_NATURE_PRO.equals(nature))
					return ContactImages.WORK;
			} else if (entityNType.equals(PeopleTypes.PEOPLE_ORGANIZATION))
				if (ContactValueCatalogs.CONTACT_CAT_PRO_RECEPTION
						.equals(category))
					return ContactImages.PHONE_DIRECT;
			return ContactImages.DEFAULT_PHONE;
		}
		// ADDRESS
		else if (PeopleTypes.PEOPLE_ADDRESS.equals(contactNType)) {
			if (entityNType.equals(PeopleTypes.PEOPLE_PERSON)
					&& ContactValueCatalogs.CONTACT_NATURE_PRIVATE
							.equals(nature))
				return ContactImages.DEFAULT_ADDRESS;
			return ContactImages.WORK;
		}
		// URL
		else if (PeopleTypes.PEOPLE_URL.equals(contactNType)) {
			if (entityNType.equals(PeopleTypes.PEOPLE_PERSON)
					&& ContactValueCatalogs.CONTACT_NATURE_PRIVATE
							.equals(nature))
				return ContactImages.PRIVATE_HOME_PAGE;
			return ContactImages.DEFAULT_URL;
		}
		// SOCIAL MEDIA
		else if (PeopleTypes.PEOPLE_SOCIAL_MEDIA.equals(contactNType)) {
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
		// IMPP
		else if (PeopleTypes.PEOPLE_IMPP.equals(contactNType)) {
			return ContactImages.DEFAULT_IMPP;
		}
		return null;
	}
}
