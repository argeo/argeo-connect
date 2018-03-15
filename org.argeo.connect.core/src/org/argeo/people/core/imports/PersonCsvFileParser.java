package org.argeo.people.core.imports;

import static org.argeo.connect.util.ConnectUtils.notEmpty;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.UUID;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.ConnectNames;
import org.argeo.connect.resources.ResourcesNames;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.jcr.JcrUtils;
import org.argeo.people.ContactValueCatalogs;
import org.argeo.people.PeopleException;
import org.argeo.people.PeopleService;
import org.argeo.people.PeopleTypes;
import org.argeo.people.util.OrgJcrUtils;
import org.argeo.people.util.PeopleJcrUtils;
import org.argeo.people.util.PersonJcrUtils;

/** Base utility to load person data in a people repository from a .CSV file **/
public class PersonCsvFileParser extends AbstractPeopleCsvFileParser {
	private final static Log log = LogFactory.getLog(PersonCsvFileParser.class);

	private final String[] personProps = { PEOPLE_PRIMARY_EMAIL, PEOPLE_SALUTATION, PEOPLE_HONORIFIC_TITLE,
			PEOPLE_NAME_SUFFIX, PEOPLE_NICKNAME, PEOPLE_MAIDEN_NAME, PEOPLE_NICKNAME };

	private Node peopleParentNode;

	public PersonCsvFileParser(Session adminSession, ResourcesService resourcesService, PeopleService peopleService,
			URI images) {
		super(adminSession, resourcesService, peopleService, images);
		peopleParentNode = ConnectJcrUtils.getNode(adminSession, null,
				peopleService.getBaseRelPath(PeopleTypes.PEOPLE_PERSON));
	}

	public PersonCsvFileParser(Session adminSession, ResourcesService resourcesService, PeopleService peopleService) {
		this(adminSession, resourcesService, peopleService, null);
	}

	@Override
	protected void processLine(Integer lineNumber, Map<String, String> line) {
		try {
			// Basic info
			String lastName = line.get(PEOPLE_LAST_NAME).trim();
			String firstName = line.get(PEOPLE_FIRST_NAME).trim();
			String connectUid = UUID.randomUUID().toString();

			// Create corresponding node
			String relPath = getPeopleService().getDefaultRelPath(getSession(), PeopleTypes.PEOPLE_PERSON, connectUid);
			Node person = JcrUtils.mkdirs(peopleParentNode, relPath);
			person.addMixin(PeopleTypes.PEOPLE_PERSON);

			// Mandatory properties
			person.setProperty(ConnectNames.CONNECT_UID, connectUid);
			if (notEmpty(lastName))
				person.setProperty(PEOPLE_LAST_NAME, lastName);
			if (notEmpty(firstName))
				person.setProperty(PEOPLE_FIRST_NAME, firstName);
			person.setProperty(Property.JCR_TITLE, getPeopleService().getDisplayName(person));

			// Image
			InputStream is = null;
			try {
				String imgName = lastName.trim().toLowerCase() + ".jpg";
				URI myImg = getPicture(imgName);
				// if (myImg.exists()) {
				is = myImg.toURL().openStream();
				PeopleJcrUtils.setEntityPicture(person, is, imgName);
				// } else {
				// // default person image
				// imgName = "person.jpg";
				// myImg = getPicture(imgName);
				// if (myImg.exists()) {
				// is = myImg.getInputStream();
				// PeopleJcrUtils.setEntityPicture(person, is, imgName);
				// }
				// }
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
			String tags = line.get(ResourcesNames.CONNECT_TAGS);
			if (notEmpty(tags))
				person.setProperty(ResourcesNames.CONNECT_TAGS, ConnectJcrUtils.parseAndClean(tags, ",", true));

			// Mailing lists
			String mailingLists = line.get(PEOPLE_MAILING_LISTS);
			if (notEmpty(mailingLists))
				person.setProperty(PEOPLE_MAILING_LISTS, ConnectJcrUtils.parseAndClean(mailingLists, ",", true));

			// TODO Add spoken languages.

			// CONTACTS
			String phone = line.get("people:phoneNumber").trim();
			if (notEmpty(phone)) {
				PeopleJcrUtils.createContact(getResourcesService(), getPeopleService(), person,
						PeopleTypes.PEOPLE_MOBILE, phone, true, ContactValueCatalogs.CONTACT_NATURE_PRO, null);
			}

			phone = line.get("PhoneDirect").trim();
			if (notEmpty(phone)) {
				PeopleJcrUtils.createPhone(getResourcesService(), getPeopleService(), person, phone, false,
						ContactValueCatalogs.CONTACT_CAT_MAIN, null);
			}

			String emailAddress = JcrUtils.replaceInvalidChars(line.get("people:emailAddress").trim());
			if (notEmpty(emailAddress)) {
				PeopleJcrUtils.createEmail(getResourcesService(), getPeopleService(), person, emailAddress, true, null,
						null);
			}

			emailAddress = JcrUtils.replaceInvalidChars(line.get("people:emailAddressOther").trim());
			if (notEmpty(emailAddress)) {
				PeopleJcrUtils.createEmail(getResourcesService(), getPeopleService(), person, emailAddress, false, null,
						null);
			}

			String facebook = line.get("Facebook");
			if (notEmpty(facebook)) {
				PeopleJcrUtils.createSocialMedia(getResourcesService(), getPeopleService(), person, facebook, true,
						ContactValueCatalogs.CONTACT_CAT_FACEBOOK, null);
			}

			// Add birth date
			String birthDate = line.get(PEOPLE_BIRTH_DATE);
			if (notEmpty(birthDate))
				setDateValueFromString(person, PEOPLE_BIRTH_DATE, birthDate);

			// Add Note
			String note = line.get(Property.JCR_DESCRIPTION);
			if (notEmpty(note))
				person.setProperty(Property.JCR_DESCRIPTION, note);

			// ORGANISATION
			String orgWebsite = line.get(PeopleTypes.PEOPLE_ORG);
			if (notEmpty(orgWebsite)) {
				Node orga = OrgJcrUtils.getOrgWithWebSite(getSession(), orgWebsite);
				if (orga != null) {
					PersonJcrUtils.addJob(getResourcesService(), getPeopleService(), person, orga,
							line.get(PEOPLE_ROLE), false);
				}
			}

			getPeopleService().saveEntity(person, true);
			if (log.isDebugEnabled())
				log.debug("Test data: loaded " + lastName);
		} catch (RepositoryException e) {
			throw new PeopleException("Cannot process line " + lineNumber + " " + line, e);
		}
	}
}
