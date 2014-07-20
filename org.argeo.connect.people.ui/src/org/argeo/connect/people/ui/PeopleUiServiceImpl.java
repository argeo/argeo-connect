package org.argeo.connect.people.ui;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Query;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.commands.OpenEntityEditor;
import org.argeo.connect.people.ui.commands.OpenSearchByTagEditor;
import org.argeo.connect.people.ui.commands.OpenSearchEntityEditor;
import org.argeo.connect.people.ui.wizards.NewOrgWizard;
import org.argeo.connect.people.ui.wizards.NewPersonWizard;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Image;

/**
 * Centralize here the definition of context specific parameter (for instance
 * the name of the command to open editors so that it is easily extended by
 * specific extensions
 */
public class PeopleUiServiceImpl implements PeopleUiService {

	/**
	 * Provide the plugin specific ID of the {@code OpenEntityEditor} command
	 * and thus enable the opening plugin specific editors
	 */
	public String getOpenEntityEditorCmdId() {
		return OpenEntityEditor.ID;
	}

	@Override
	public String getOpenSearchEntityEditorCmdId() {
		return OpenSearchEntityEditor.ID;
	}

	@Override
	public String getOpenSearchByTagEditorCmdId() {
		return OpenSearchByTagEditor.ID;
	}

	@Override
	public String getOpenFileCmdId() {
		// TODO clean this.
		throw new PeopleException("OpenFile command is undefined for "
				+ "PeopleUiService base implementation");
		// return "org.argeo.connect.people.ui.specific.openFile";
	}

	@Override
	public String getDefaultEditorId() {
		throw new PeopleException("No default editor has been defined for "
				+ "PeopleUiService base implementation");
	}

	@Override
	public Wizard getCreationWizard(PeopleService peopleService, Node node) {
		if (CommonsJcrUtils.isNodeType(node, PeopleTypes.PEOPLE_PERSON))
			return new NewPersonWizard(peopleService, node);
		else if (CommonsJcrUtils.isNodeType(node, PeopleTypes.PEOPLE_ORG))
			return new NewOrgWizard(peopleService, node);
		else
			throw new PeopleException("No defined wizard for node " + node);
	}

	public List<String> getValueList(Session session, String basePath,
			String filter) {
		return getValueList(session, NodeType.MIX_TITLE, basePath, filter);
	}

	public List<String> getValueList(Session session, String nodeType,
			String basePath, String filter) {
		List<String> values = new ArrayList<String>();
		try {
			String queryStr = "select * from [" + nodeType
					+ "] as nodes where ISDESCENDANTNODE('" + basePath + "')";
			if (CommonsJcrUtils.checkNotEmptyString(filter))
				queryStr += " AND nodes.[" + Property.JCR_TITLE + "] like '%"
						+ filter + "%'";
			queryStr += " ORDER BY nodes.[" + Property.JCR_TITLE + "]";

			Query query = session.getWorkspace().getQueryManager()
					.createQuery(queryStr, Query.JCR_SQL2);
			NodeIterator nit = query.execute().getNodes();

			while (nit.hasNext()) {
				Node curr = nit.nextNode();
				if (curr.hasProperty(Property.JCR_TITLE))
					values.add(curr.getProperty(Property.JCR_TITLE).getString());
			}
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to get tags values for node ", re);
		}
		return values;
	}

	@Override
	public List<String> getInstancePropCatalog(Session session,
			String resourcePath, String propertyName, String filter) {
		List<String> result = new ArrayList<String>();
		try {
			if (session.nodeExists(resourcePath)) {
				Node node = session.getNode(resourcePath);
				if (node.hasProperty(propertyName)) {
					Value[] values = node.getProperty(propertyName).getValues();
					for (Value value : values) {
						String curr = value.getString();
						if (CommonsJcrUtils.isEmptyString(filter)
								|| CommonsJcrUtils.checkNotEmptyString(curr)
								&& curr.toLowerCase().contains(
										filter.toLowerCase()))
							result.add(curr);
					}
				}
			}
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to get " + propertyName
					+ " values for node at path " + resourcePath, re);
		}

		return result;
	}

	@Override
	public Image getIconForType(Node entity) {
		try {
			if (entity.isNodeType(PeopleTypes.PEOPLE_PERSON))
				return PeopleImages.ICON_PERSON;
			else if (entity.isNodeType(PeopleTypes.PEOPLE_ORG))
				return PeopleImages.ICON_ORG;
			else if (entity.isNodeType(PeopleTypes.PEOPLE_MAILING_LIST))
				return PeopleImages.ICON_MAILING_LIST;
			else if (entity.isNodeType(PeopleTypes.PEOPLE_GROUP))
				return PeopleImages.ICON_GROUP;
			else if (entity.isNodeType(PeopleTypes.PEOPLE_CONTACT))
				return getContactIcon(entity);
			else
				return null;
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to get image for node", re);
		}
	}

	/**
	 * Specific management of contact icons. Might be overridden by client
	 * application
	 */
	protected Image getContactIcon(Node entity) throws RepositoryException {

		// EMAIL
		if (entity.isNodeType(PeopleTypes.PEOPLE_EMAIL)) {
			return ContactImages.DEFAULT_MAIL;
		}
		// PHONE
		else if (entity.isNodeType(PeopleTypes.PEOPLE_PHONE)) {
			// if (ContactValueCatalogs.CONTACT_CAT_FAX.equals(category))
			// return ContactImages.FAX;
			// else if
			// (ContactValueCatalogs.CONTACT_CAT_MOBILE.equals(category))
			// return ContactImages.MOBILE;
			// if (entityNType.equals(PeopleTypes.PEOPLE_PERSON)) {
			// if (ContactValueCatalogs.CONTACT_CAT_PRO_DIRECT
			// .equals(category))
			// return ContactImages.PHONE_DIRECT;
			// else if (ContactValueCatalogs.CONTACT_NATURE_PRO.equals(nature))
			// return ContactImages.WORK;
			// } else if (entityNType.equals(PeopleTypes.PEOPLE_ORG))
			// if (ContactValueCatalogs.CONTACT_CAT_PRO_RECEPTION
			// .equals(category))
			// return ContactImages.PHONE_DIRECT;
			return ContactImages.DEFAULT_PHONE;
		}
		// // ADDRESS
		// else if (PeopleTypes.PEOPLE_ADDRESS.equals(contactNType)) {
		// if (entityNType.equals(PeopleTypes.PEOPLE_PERSON)
		// && ContactValueCatalogs.CONTACT_NATURE_PRIVATE
		// .equals(nature))
		// return ContactImages.DEFAULT_ADDRESS;
		// return ContactImages.WORK;
		// }
		// // URL
		// else if (PeopleTypes.PEOPLE_URL.equals(contactNType)) {
		// if (entityNType.equals(PeopleTypes.PEOPLE_PERSON)
		// && ContactValueCatalogs.CONTACT_NATURE_PRIVATE
		// .equals(nature))
		// return ContactImages.PRIVATE_HOME_PAGE;
		// return ContactImages.DEFAULT_URL;
		// }
		// // SOCIAL MEDIA
		// else if (PeopleTypes.PEOPLE_SOCIAL_MEDIA.equals(contactNType)) {
		// if (ContactValueCatalogs.CONTACT_CAT_GOOGLEPLUS.equals(category))
		// return ContactImages.GOOGLEPLUS;
		// else if (ContactValueCatalogs.CONTACT_CAT_FACEBOOK.equals(category))
		// return ContactImages.FACEBOOK;
		// else if (ContactValueCatalogs.CONTACT_CAT_LINKEDIN.equals(category))
		// return ContactImages.LINKEDIN;
		// else if (ContactValueCatalogs.CONTACT_CAT_XING.equals(category))
		// return ContactImages.XING;
		// return ContactImages.DEFAULT_SOCIAL_MEDIA;
		// }
		// // IMPP
		// else if (PeopleTypes.PEOPLE_IMPP.equals(contactNType)) {
		// return ContactImages.DEFAULT_IMPP;
		// }
		return null;
	}

}
