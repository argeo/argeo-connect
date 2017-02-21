package org.argeo.connect.people.core.imports;

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
import org.argeo.connect.people.ContactValueCatalogs;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.util.PeopleJcrUtils;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.jcr.JcrUtils;
import org.springframework.core.io.Resource;

/**
 * Base utility to load organizations data in a People repository from a .CSV
 * file
 **/
public class OrgCsvFileParser extends AbstractPeopleCsvFileParser {
	private final static Log log = LogFactory.getLog(OrgCsvFileParser.class);

	private Node peopleParentNode;

	public OrgCsvFileParser(Session adminSession, ResourcesService resourceService, PeopleService peopleService,
			Resource images) {
		super(adminSession, resourceService, peopleService, images);
		peopleParentNode = ConnectJcrUtils.getNode(adminSession, peopleService.getDefaultBasePath());
	}

	public OrgCsvFileParser(Session adminSession, PeopleService peopleService, ResourcesService resourceService) {
		this(adminSession, resourceService, peopleService, null);
	}

	@Override
	protected void processLine(Integer lineNumber, Map<String, String> line) {
		try {

			String peopleUid = UUID.randomUUID().toString();
			String relPath = getPeopleService().getDefaultRelPath(peopleUid);
			Node orga = JcrUtils.mkdirs(peopleParentNode, relPath);
			orga.addMixin(PeopleTypes.PEOPLE_ORG);

			String legalName = line.get(PEOPLE_LEGAL_NAME).trim();
			orga.setProperty(PEOPLE_LEGAL_NAME, legalName);
			orga.setProperty(Property.JCR_TITLE, legalName);
			orga.setProperty(ConnectNames.CONNECT_UID, UUID.randomUUID().toString());

			// Website and dummy picture
			String webSite = line.get("people:websiteUrl");
			if (notEmpty(webSite)) {
				PeopleJcrUtils.createWebsite(getPeopleService(), getResourceService(), orga, webSite, true, null, null);

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
					log.warn("IOException while importing org image: " + ioe.getMessage());
					// Silent
				} finally {
					IOUtils.closeQuietly(is);
				}
			}

			// address
			Node address = PeopleJcrUtils.createAddress(getPeopleService(), getResourceService(), orga,
					line.get(PEOPLE_STREET), line.get(PEOPLE_STREET_COMPLEMENT), line.get(PEOPLE_ZIP_CODE),
					line.get(PEOPLE_CITY), line.get(PEOPLE_STATE), line.get(PEOPLE_COUNTRY), true, null,
					ContactValueCatalogs.CONTACT_CAT_HEADOFFICE, null);
			PeopleJcrUtils.updateDisplayAddress(address);

			String emailAddress = line.get("people:emailAddress").trim();
			if (notEmpty(emailAddress)) {
				PeopleJcrUtils.createEmail(getPeopleService(), getResourceService(), orga, emailAddress, true, null,
						null, null);
			}

			// Phone numbers
			String phone = line.get("people:phoneNb");
			if (notEmpty(phone))
				PeopleJcrUtils.createPhone(getPeopleService(), getResourceService(), orga, phone, true, null,
						ContactValueCatalogs.CONTACT_CAT_MAIN, null);
			phone = line.get("people:faxNb");

			if (notEmpty(phone))
				PeopleJcrUtils.createPhone(getPeopleService(), getResourceService(), orga, phone, true, null,
						ContactValueCatalogs.CONTACT_CAT_FAX, null);

			// Tags
			String tags = line.get(ConnectNames.CONNECT_TAGS);
			if (notEmpty(tags))
				orga.setProperty(ConnectNames.CONNECT_TAGS, ConnectJcrUtils.parseAndClean(tags, ",", true));

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
