package org.argeo.connect.people.ui.exports;

import static org.argeo.eclipse.ui.EclipseUiUtils.notEmpty;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.util.PeopleJcrUtils;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;

/**
 * Enable simple retrieval of primary organisation name for persons.
 * 
 * The requested primary organisation name is the one of the organisation linked
 * with the primary address if such exists and is a work address. See v0.4
 * specification for more info
 */
public class PrimOrgNameLP extends ColumnLabelProvider {
	private final static Log log = LogFactory.getLog(PrimOrgNameLP.class);
	private static final long serialVersionUID = 1L;

	private PeopleService peopleService;
	private String selectorName;

	public PrimOrgNameLP(PeopleService peopleService, String selectorName) {
		this.peopleService = peopleService;
		if (notEmpty(selectorName))
			this.selectorName = selectorName;
	}

	@Override
	public String getText(Object element) {
		Node currNode = ConnectJcrUtils.getNodeFromElement(element, selectorName);
		try {
			Node currContact = PeopleJcrUtils.getPrimaryContact(currNode, PeopleTypes.PEOPLE_ADDRESS);
			// Sanity check
			if (currContact == null || !currContact.isNodeType(PeopleTypes.PEOPLE_CONTACT_REF))
				return "";

			String refUid = ConnectJcrUtils.get(currContact, PeopleNames.PEOPLE_REF_UID);
			if (EclipseUiUtils.isEmpty(refUid)) {
				log.error("Empty UID: unable to get linked contact for " + currNode
						+ "\nThis usually happens when legacy " + "imported address are not correctly defined");
				return null;
			}
			Node referencedEntity = peopleService.getEntityByUid(ConnectJcrUtils.getSession(currContact), null, refUid);
			if (referencedEntity == null)
				return "";
			return ConnectJcrUtils.get(referencedEntity, Property.JCR_TITLE);
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to get primary org name from row " + element, re);
		}
	}
}
