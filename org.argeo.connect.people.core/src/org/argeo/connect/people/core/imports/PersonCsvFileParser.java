package org.argeo.connect.people.core.imports;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.people.ContactValueCatalogs;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.OrgJcrUtils;
import org.argeo.connect.people.utils.PeopleJcrUtils;
import org.argeo.connect.people.utils.PersonJcrUtils;
import org.argeo.jcr.JcrUtils;
import org.springframework.core.io.Resource;

/** Base utility to load person data in a people repository from a .CSV file **/
public class PersonCsvFileParser extends AbstractPeopleCsvFileParser {
	private final static Log log = LogFactory.getLog(PersonCsvFileParser.class);

	private final String[] personProps = { PEOPLE_PRIMARY_EMAIL,
			PEOPLE_SALUTATION, PEOPLE_HONORIFIC_TITLE, PEOPLE_NAME_SUFFIX,
			PEOPLE_NICKNAME, PEOPLE_MAIDEN_NAME,
			PEOPLE_LATIN_PHONETIC_SPELLING, PEOPLE_NICKNAME };

	public PersonCsvFileParser(Session adminSession,
			PeopleService peopleService, Resource images) {
		super(adminSession, peopleService, images);
	}

	public PersonCsvFileParser(Session adminSession, PeopleService peopleService) {
		super(adminSession, peopleService);
	}

	@Override
	protected void processLine(Integer lineNumber, Map<String, String> line) {
		try {
			// Basic info
			String lastName = line.get(PEOPLE_LAST_NAME).trim();
			String firstName = line.get(PEOPLE_FIRST_NAME).trim();
			String peopleUid = UUID.randomUUID().toString();

			// Create corresponding node
			String path = getPeopleService().getDefaultPathForEntity(peopleUid,
					PeopleTypes.PEOPLE_PERSON);
			Node parent = JcrUtils.mkdirs(adminSession, path);
			Node person = parent.addNode(peopleUid, PeopleTypes.PEOPLE_PERSON);

			// Mandatory properties
			person.setProperty(PEOPLE_UID, peopleUid);
			if (CommonsJcrUtils.checkNotEmptyString(lastName))
				person.setProperty(PEOPLE_LAST_NAME, lastName);
			if (CommonsJcrUtils.checkNotEmptyString(firstName))
				person.setProperty(PEOPLE_FIRST_NAME, firstName);
			person.setProperty(Property.JCR_TITLE, getPeopleService()
					.getDisplayName(person));

			// Image
			InputStream is = null;
			try {
				String imgName = lastName.trim().toLowerCase() + ".jpg";
				Resource myImg = getPicture(imgName);
				if (myImg.exists()) {
					is = myImg.getInputStream();
					PeopleJcrUtils.setEntityPicture(person, is, imgName);
				}
			} catch (IOException ioe) { // Unable to get image
				// Silent
			} finally {
				IOUtils.closeQuietly(is);
			}

			// All String properties
			for (String propName : personProps) {
				String value = line.get(propName);
				if (value != null && !value.trim().equals(""))
					person.setProperty(propName, value);
			}

			// Tags
			String tags = line.get(PEOPLE_TAGS);
			if (!CommonsJcrUtils.isEmptyString(tags))
				person.setProperty(PEOPLE_TAGS,
						CommonsJcrUtils.parseAndClean(tags, ",", true));

			// Mailing lists
			String mailingLists = line.get(PEOPLE_MAILING_LISTS);
			if (!CommonsJcrUtils.isEmptyString(mailingLists))
				person.setProperty(PEOPLE_MAILING_LISTS,
						CommonsJcrUtils.parseAndClean(mailingLists, ",", true));

			// TODO Add spoken languages.

			// CONTACTS
			String phone = line.get("people:phoneNumber").trim();
			if (!CommonsJcrUtils.isEmptyString(phone)) {
				PeopleJcrUtils.createPhone(getPeopleService(), person, phone,
						true, ContactValueCatalogs.CONTACT_NATURE_PRO,
						ContactValueCatalogs.CONTACT_CAT_MOBILE, null);
			}

			phone = line.get("PhoneDirect").trim();
			if (!CommonsJcrUtils.isEmptyString(phone)) {
				PeopleJcrUtils.createPhone(getPeopleService(), person, phone,
						false, ContactValueCatalogs.CONTACT_NATURE_PRO,
						ContactValueCatalogs.CONTACT_CAT_MAIN, null);
			}

			String emailAddress = JcrUtils.replaceInvalidChars(line.get(
					"people:emailAddress").trim());
			if (!CommonsJcrUtils.isEmptyString(emailAddress)) {
				PeopleJcrUtils.createEmail(getPeopleService(), person,
						emailAddress, true,
						ContactValueCatalogs.CONTACT_NATURE_PRO, null, null);
			}

			emailAddress = JcrUtils.replaceInvalidChars(line.get(
					"people:emailAddressOther").trim());
			if (!CommonsJcrUtils.isEmptyString(emailAddress)) {
				PeopleJcrUtils
						.createEmail(getPeopleService(), person, emailAddress,
								false,
								ContactValueCatalogs.CONTACT_NATURE_PRIVATE,
								null, null);
			}

			String facebook = line.get("Facebook");
			if (!CommonsJcrUtils.isEmptyString(facebook)) {
				PeopleJcrUtils.createSocialMedia(getPeopleService(), person,
						facebook, true,
						ContactValueCatalogs.CONTACT_NATURE_PRIVATE,
						ContactValueCatalogs.CONTACT_CAT_FACEBOOK, null);
			}

			// Add birth date
			String birthDate = line.get(PEOPLE_BIRTH_DATE);
			if (!CommonsJcrUtils.isEmptyString(birthDate))
				setDateValueFromString(person, PEOPLE_BIRTH_DATE, birthDate);

			// Add Note
			String note = line.get(Property.JCR_DESCRIPTION);
			if (!CommonsJcrUtils.isEmptyString(note))
				person.setProperty(Property.JCR_DESCRIPTION, note);

			// ORGANISATION
			String orgWebsite = line.get(PeopleTypes.PEOPLE_ORG);
			if (CommonsJcrUtils.checkNotEmptyString(orgWebsite)) {
				Node orga = OrgJcrUtils.getOrgWithWebSite(adminSession,
						orgWebsite);
				if (orga != null) {
					PersonJcrUtils.addJob(person, orga, line.get(PEOPLE_ROLE),
							false);
				}
			}

			getPeopleService().saveEntity(person, true);
			if (log.isDebugEnabled())
				log.debug("Test data: loaded " + lastName);
		} catch (RepositoryException e) {
			throw new PeopleException("Cannot process line " + lineNumber + " "
					+ line, e);
		}
	}
}