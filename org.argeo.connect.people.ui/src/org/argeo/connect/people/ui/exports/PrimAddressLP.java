package org.argeo.connect.people.ui.exports;

import static org.argeo.eclipse.ui.EclipseUiUtils.notEmpty;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.connect.people.ContactValueCatalogs;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.util.PeopleJcrUtils;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.eclipse.ui.jcr.lists.SimpleJcrNodeLabelProvider;

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
			Node currContact = PeopleJcrUtils.getPrimaryContact(currNode, PeopleTypes.PEOPLE_ADDRESS);

			// Sanity check
			if (currContact == null)
				return "";

			String contactNature = ConnectJcrUtils.get(currContact, PeopleNames.PEOPLE_CONTACT_NATURE);
			if (currContact.isNodeType(PeopleTypes.PEOPLE_CONTACT_REF)) {
				String refUid = ConnectJcrUtils.get(currContact, PeopleNames.PEOPLE_REF_UID);
				if (EclipseUiUtils.isEmpty(refUid)) {
					if (EclipseUiUtils.isEmpty(contactNature)
							|| ContactValueCatalogs.CONTACT_NATURE_PRIVATE.equals(contactNature)
							|| ContactValueCatalogs.CONTACT_OTHER.equals(contactNature))
						return ConnectJcrUtils.get(currContact, propertyName);
					else
						return "";
				}
				Node referencedEntity = peopleService.getEntityByUid(ConnectJcrUtils.getSession(currContact), refUid);
				if (referencedEntity == null)
					return "";
				Node referencedContact = PeopleJcrUtils.getPrimaryContact(referencedEntity, PeopleTypes.PEOPLE_ADDRESS);
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