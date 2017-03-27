package org.argeo.people.core.imports;

import static org.argeo.eclipse.ui.EclipseUiUtils.notEmpty;

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
import org.argeo.connect.ConnectNames;
import org.argeo.connect.resources.ResourcesNames;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.jcr.JcrUtils;
import org.argeo.people.ContactValueCatalogs;
import org.argeo.people.PeopleException;
import org.argeo.people.PeopleService;
import org.argeo.people.PeopleTypes;
import org.argeo.people.util.PeopleJcrUtils;
import org.springframework.core.io.Resource;

/**
 * Base utility to load organisations data in a People repository from a .CSV
 * file
 **/
public class OrgCsvFileParser extends AbstractPeopleCsvFileParser {
	private final static Log log = LogFactory.getLog(OrgCsvFileParser.class);

	private Node peopleParentNode;

	public OrgCsvFileParser(Session adminSession, ResourcesService resourcesService, PeopleService peopleService,
			Resource images) {
		super(adminSession, resourcesService, peopleService, images);
		peopleParentNode = ConnectJcrUtils.getNode(adminSession, null,
				peopleService.getBaseRelPath(PeopleTypes.PEOPLE_ORG));
	}

	public OrgCsvFileParser(Session adminSession, PeopleService peopleService, ResourcesService resourceService) {
		this(adminSession, resourceService, peopleService, null);
	}

	@Override
	protected void processLine(Integer lineNumber, Map<String, String> line) {
		try {

			String connectUid = UUID.randomUUID().toString();
			String relPath = getPeopleService().getDefaultRelPath(getSession(), PeopleTypes.PEOPLE_ORG, connectUid);
			Node orga = JcrUtils.mkdirs(peopleParentNode, relPath);
			orga.addMixin(PeopleTypes.PEOPLE_ORG);

			String legalName = line.get(PEOPLE_LEGAL_NAME).trim();
			orga.setProperty(PEOPLE_LEGAL_NAME, legalName);
			orga.setProperty(Property.JCR_TITLE, legalName);
			orga.setProperty(ConnectNames.CONNECT_UID, UUID.randomUUID().toString());

			// Website and dummy picture
			String webSite = line.get("people:websiteUrl");
			if (notEmpty(webSite)) {
				PeopleJcrUtils.createWebsite(getResourcesService(), getPeopleService(), orga, webSite, true, null);

				// picture
				InputStream is = null;
				String image = webSite.trim().toLowerCase() + ".jpg";
				try {
					Resource myImg = getPicture(image);
					if (myImg.exists()) {
						is = myImg.getInputStream();
						PeopleJcrUtils.setEntityPicture(orga, is, image);
					}
				} catch (IOException ioe) {// Silent
					log.warn("Cannot import org image " + image, ioe);
				} finally {
					IOUtils.closeQuietly(is);
				}
			}

			// address
			String street = line.get(PEOPLE_STREET), street2 = line.get(PEOPLE_STREET_COMPLEMENT),
					zip = line.get(PEOPLE_ZIP_CODE), city = line.get(PEOPLE_CITY), state = line.get(PEOPLE_STATE),
					country = line.get(PEOPLE_COUNTRY);

			if (notEmpty(street) || notEmpty(street2) || notEmpty(zip) || notEmpty(city) || notEmpty(state)
					|| notEmpty(country))
				PeopleJcrUtils.createAddress(getResourcesService(), getPeopleService(), orga, street, street2, zip,
						city, state, country, true, ContactValueCatalogs.CONTACT_CAT_HEADOFFICE, null);

			String emailAddress = line.get("people:emailAddress").trim();
			if (notEmpty(emailAddress)) {
				PeopleJcrUtils.createEmail(getResourcesService(), getPeopleService(), orga, emailAddress, true, null,
						null);
			}

			// Phone numbers
			String phone = line.get("people:phoneNb");
			if (notEmpty(phone))
				PeopleJcrUtils.createPhone(getResourcesService(), getPeopleService(), orga, phone, true,
						ContactValueCatalogs.CONTACT_CAT_MAIN, null);
			phone = line.get("people:faxNb");
			if (notEmpty(phone))
				PeopleJcrUtils.createContact(getResourcesService(), getPeopleService(), orga, PeopleTypes.PEOPLE_FAX,
						phone, true, null, null);

			// Tags
			String tags = line.get(ResourcesNames.CONNECT_TAGS);
			if (notEmpty(tags))
				orga.setProperty(ResourcesNames.CONNECT_TAGS, ConnectJcrUtils.parseAndClean(tags, ",", true));

			// Mailing lists
			String mailingLists = line.get(PEOPLE_MAILING_LISTS);
			if (notEmpty(mailingLists))
				orga.setProperty(PEOPLE_MAILING_LISTS, ConnectJcrUtils.parseAndClean(mailingLists, ",", true));
			getPeopleService().saveEntity(orga, true);

			if (log.isDebugEnabled())
				log.debug("Test data: loaded " + legalName);

		} catch (RepositoryException e) {
			throw new PeopleException("Cannot process line " + lineNumber + " " + line, e);
		}
	}
}
