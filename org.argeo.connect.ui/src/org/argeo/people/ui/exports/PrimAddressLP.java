package org.argeo.people.ui.exports;

import static org.argeo.eclipse.ui.EclipseUiUtils.notEmpty;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.eclipse.ui.jcr.lists.SimpleJcrNodeLabelProvider;
import org.argeo.people.PeopleException;
import org.argeo.people.PeopleNames;
import org.argeo.people.PeopleService;
import org.argeo.people.PeopleTypes;
import org.argeo.people.util.PeopleJcrUtils;

/** Enable simple retrieval of primary address information */
public class PrimAddressLP extends SimpleJcrNodeLabelProvider {
	private static final long serialVersionUID = 1L;

	private String selectorName;
	private String propertyName;
	private PeopleService peopleService;

	public PrimAddressLP(PeopleService peopleService, String selectorName, String propertyName) {
		super(propertyName);
		this.peopleService = peopleService;
		if (notEmpty(selectorName))
			this.selectorName = selectorName;
		this.propertyName = propertyName;
	}

	@Override
	public String getText(Object element) {
		try {
			Node currNode = ConnectJcrUtils.getNodeFromElement(element, selectorName);
			Node currContact = PeopleJcrUtils.getPrimaryContact(currNode, PeopleTypes.PEOPLE_POSTAL_ADDRESS);

			// Sanity check
			if (currContact == null)
				return "";

			// String contactNature = ConnectJcrUtils.get(currContact,
			// PeopleNames.PEOPLE_CONTACT_NATURE);
			if (currContact.isNodeType(PeopleTypes.PEOPLE_CONTACT_REF)) {
				String refUid = ConnectJcrUtils.get(currContact, PeopleNames.PEOPLE_REF_UID);
				if (EclipseUiUtils.isEmpty(refUid)) {
					// if (EclipseUiUtils.isEmpty(contactNature)
					// ||
					// ContactValueCatalogs.CONTACT_NATURE_PRIVATE.equals(contactNature)
					// ||
					// ContactValueCatalogs.CONTACT_OTHER.equals(contactNature))
					// return ConnectJcrUtils.get(currContact, propertyName);
					// else
					return "";
				}
				Node referencedEntity = peopleService.getEntityByUid(ConnectJcrUtils.getSession(currContact), null,
						refUid);
				if (referencedEntity == null)
					return "";
				Node referencedContact = PeopleJcrUtils.getPrimaryContact(referencedEntity,
						PeopleTypes.PEOPLE_POSTAL_ADDRESS);
				if (referencedContact == null)
					return "";
				else
					currContact = referencedContact;
			}
			return ConnectJcrUtils.get(currContact, propertyName);
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to get text from row " + element, re);
		}
	}
}
