package org.argeo.connect.people.core.imports;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.people.ContactValueCatalogs;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.PeopleJcrUtils;
import org.argeo.jcr.JcrUtils;
import org.springframework.core.io.Resource;

/**
 * Base utility to load organizations data in a People repository from a .CSV
 * file
 **/
public class OrgCsvFileParser extends AbstractPeopleCsvFileParser {
	private final static Log log = LogFactory.getLog(OrgCsvFileParser.class);

	public OrgCsvFileParser(Session adminSession, PeopleService peopleService,
			Resource images) {
		super(adminSession, peopleService, images);
	}

	public OrgCsvFileParser(Session adminSession, PeopleService peopleService) {
		super(adminSession, peopleService);
	}

	@Override
	protected void processLine(Integer lineNumber, Map<String, String> line) {
		try {
			Node orgs = adminSession.getNode(getPeopleService().getBasePath(
					PeopleTypes.PEOPLE_ORG));
			String legalName = line.get(PEOPLE_LEGAL_NAME).trim();

			// TODO implement a less violent replacement
			String sLegalName = legalName.replaceAll("[^a-zA-Z0-9]", "");

			Node orga;
			String relPath = JcrUtils.firstCharsToPath(sLegalName, 2) + "/"
					+ sLegalName;
			if (!adminSession.nodeExists(orgs.getPath() + "/" + relPath)) {
				orga = JcrUtils.mkdirs(orgs, relPath, PeopleTypes.PEOPLE_ORG,
						NodeType.NT_UNSTRUCTURED);

				// remove special chars so that it is correctly displayed
				orga.setProperty(PEOPLE_LEGAL_NAME, legalName);
				orga.setProperty(Property.JCR_TITLE, legalName);
				orga.setProperty(PEOPLE_UID, UUID.randomUUID().toString());

				// Website and dummy picture
				String webSite = line.get("people:websiteUrl");
				if (!CommonsJcrUtils.isEmptyString(webSite)) {
					PeopleJcrUtils.createWebsite(getPeopleService(), orga,
							webSite, true, null, null);

					// picture
					InputStream is = null;
					try {
						String image = webSite.trim().toLowerCase() + ".jpg";
						Resource myImg = getPicture(image);
						if (myImg.exists()) {
							is = myImg.getInputStream();
							PeopleJcrUtils.setEntityPicture(orga, is, image);
						} else {
							// log.trace("No Org Image found for " + image);
						}

					} catch (IOException ioe) {
						log.warn("IOException while importing org image: "
								+ ioe.getMessage());
						// Silent
					} finally {
						IOUtils.closeQuietly(is);
					}

				}

				// address
				Node address = PeopleJcrUtils
						.createAddress(getPeopleService(), orga,
								line.get(PEOPLE_STREET),
								line.get(PEOPLE_STREET_COMPLEMENT),
								line.get(PEOPLE_ZIP_CODE),
								line.get(PEOPLE_CITY), line.get(PEOPLE_STATE),
								line.get(PEOPLE_COUNTRY), true, null,
								ContactValueCatalogs.CONTACT_CAT_HEADOFFICE,
								null);
				PeopleJcrUtils.updateDisplayAddress(address);

				String emailAddress = line.get("people:emailAddress").trim();
				if (!CommonsJcrUtils.isEmptyString(emailAddress)) {
					PeopleJcrUtils.createEmail(getPeopleService(), orga,
							emailAddress, true, null, null, null);
				}

				// Phone numbers
				String phone = line.get("people:phoneNb");
				if (!CommonsJcrUtils.isEmptyString(phone)) {
					PeopleJcrUtils.createPhone(getPeopleService(), orga, phone,
							true, null, ContactValueCatalogs.CONTACT_CAT_MAIN,
							null);
				}
				phone = line.get("people:faxNb");
				if (!CommonsJcrUtils.isEmptyString(phone)) {
					PeopleJcrUtils.createPhone(getPeopleService(), orga, phone,
							true, null, ContactValueCatalogs.CONTACT_CAT_FAX,
							null);
				}

				// Tags
				String tags = line.get(PEOPLE_TAGS);
				if (!CommonsJcrUtils.isEmptyString(tags))
					orga.setProperty(PEOPLE_TAGS,
							CommonsJcrUtils.parseAndClean(tags, ",", true));

				// Mailing lists
				String mailingLists = line.get(PEOPLE_MAILING_LISTS);
				if (!CommonsJcrUtils.isEmptyString(mailingLists))
					orga.setProperty(PEOPLE_MAILING_LISTS, CommonsJcrUtils
							.parseAndClean(mailingLists, ",", true));
				getPeopleService().saveEntity(orga, true);
			}

			if (log.isDebugEnabled())
				log.debug("Test data: loaded " + legalName);

		} catch (RepositoryException e) {
			throw new PeopleException("Cannot process line " + lineNumber + " "
					+ line, e);
		}
	}
}