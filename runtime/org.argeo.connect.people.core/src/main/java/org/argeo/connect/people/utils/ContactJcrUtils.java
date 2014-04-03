package org.argeo.connect.people.utils;

import java.util.UUID;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.DynamicOperand;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;
import javax.jcr.query.qom.StaticOperand;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.jcr.JcrUtils;

/**
 * Static utility methods to manage CRM contact concepts in JCR. Rather use
 * these methods than direct JCR queries in order to ease model evolution.
 * 
 * Might be refactored in a ContactService
 */

public class ContactJcrUtils {

	////////////////////////////////
	// MAILING LIST MANAGEMENT 
		
	/**
	 * 
	 * Add a new member to a given Mailing List
	 * 
	 * @param referencingNode
	 * @param referencedNode
	 *            a person or an organisation
	 * @return
	 */
	public static Node addToMailingList(Node referencingNode,
			Node referencedNode) {
		try {
			Session session = referencingNode.getSession();

			// Sanity check
			if (!(referencedNode.isNodeType(PeopleTypes.PEOPLE_PERSON) || referencedNode
					.isNodeType(PeopleTypes.PEOPLE_ORGANIZATION)))
				throw new PeopleException("Unsupported reference: from "
						+ referencingNode + "("
						+ referencingNode.getPrimaryNodeType() + ")" + " to "
						+ referencedNode + "("
						+ referencedNode.getPrimaryNodeType() + ")");

			String linkNodeType = PeopleTypes.PEOPLE_MAILING_LIST_ITEM;
			Node parentNode = referencingNode
					.getNode(PeopleNames.PEOPLE_MEMBERS);

			String relPath = "/"
					+ PeopleJcrUtils.getRelPathForEntity(referencedNode);
			String absPath = parentNode.getPath() + relPath;

			if (session.nodeExists(JcrUtils.parentPath(absPath))) {
				// Insure it's the same entity and not an entity with the same
				// name.
				Node parent = session.getNode(JcrUtils.parentPath(absPath));
				NodeIterator ni = parent.getNodes();
				String referencedUid = CommonsJcrUtils.get(referencedNode,
						PeopleNames.PEOPLE_UID);
				while (ni.hasNext()) {
					Node currNode = ni.nextNode();
					if (referencedUid.equals(CommonsJcrUtils.get(currNode,
							PeopleNames.PEOPLE_REF_UID)))
						return currNode;
				}
			}

			boolean wasCheckedout = CommonsJcrUtils
					.isNodeCheckedOutByMe(referencingNode);
			if (!wasCheckedout)
				CommonsJcrUtils.checkout(referencingNode);

			// add the corresponding node
			Node parlink = JcrUtils.mkdirs(parentNode,
					JcrUtils.parentPath(relPath), NodeType.NT_UNSTRUCTURED);
			Node link = parlink.addNode(JcrUtils.lastPathElement(relPath),
					linkNodeType);
			String uid = referencedNode.getProperty(PeopleNames.PEOPLE_UID)
					.getString();
			link.setProperty(PeopleNames.PEOPLE_REF_UID, uid);
			if (!wasCheckedout)
				CommonsJcrUtils.saveAndCheckin(referencingNode);
			else
				referencingNode.getSession().save();
			return link;
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to add " + referencedNode
					+ " to mailing list " + referencingNode, e);
		}
	}

	public static Node createMailingList(Node parentNode, String name)
			throws RepositoryException {
		String relPath = getMailingListRelPath(name);
		Node ml = JcrUtils.mkdirs(parentNode, relPath,
				PeopleTypes.PEOPLE_MAILING_LIST, NodeType.NT_UNSTRUCTURED);
		ml.setProperty(PeopleNames.PEOPLE_UID, UUID.randomUUID().toString());
		ml.setProperty(Property.JCR_TITLE, name);

		// leave it checked out on creation to fasten import process
		// CommonsJcrUtils.saveAndCheckin(ml);
		ml.getSession().save();
		return ml;
	}

	/** Returns the relativ path of a mailing list given its name */
	public static String getMailingListRelPath(String name) {
		String cleanedName = JcrUtils.replaceInvalidChars(name);
		String relPath = null;
		if (cleanedName.length() > 1)
			relPath = JcrUtils.firstCharsToPath(cleanedName, 2) + "/"
					+ cleanedName;
		else
			throw new PeopleException(
					"Mailing list name must be at least 2 valid characters long");
		return relPath;
	}

	/**
	 * Check if a given Node is already member of the given mailing list
	 * 
	 * @return
	 */
	public static boolean isMailingMember(Node referencingNode,
			Node referencedNode) {
		try {
			Session session = referencingNode.getSession();
			QueryManager queryManager = session.getWorkspace()
					.getQueryManager();
			QueryObjectModelFactory factory = queryManager.getQOMFactory();
			Selector source = factory.selector(
					PeopleTypes.PEOPLE_MAILING_LIST_ITEM,
					PeopleTypes.PEOPLE_MAILING_LIST_ITEM);

			String parentPath = referencingNode.getNode(
					PeopleNames.PEOPLE_MEMBERS).getPath();
			Constraint c1 = factory.descendantNode(source.getSelectorName(),
					parentPath);

			DynamicOperand dynOp = factory.propertyValue(
					source.getSelectorName(), PeopleNames.PEOPLE_REF_UID);
			StaticOperand statOp = factory.literal(session.getValueFactory()
					.createValue(
							referencedNode.getProperty(PeopleNames.PEOPLE_UID)
									.getString()));
			Constraint c2 = factory.comparison(dynOp,
					QueryObjectModelFactory.JCR_OPERATOR_EQUAL_TO, statOp);

			Constraint defaultC = factory.and(c1, c2);

			QueryObjectModel query = factory.createQuery(source, defaultC,
					null, null);
			QueryResult queryResult = query.execute();
			NodeIterator ni = queryResult.getNodes();

			if (ni.getSize() == 1)
				return true;
			else {
				return false;
			}
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to add " + referencedNode
					+ " to mailing list " + referencingNode, e);
		}
	}

	// public static Node getMailingListByName(Session session, String name,
	// String basePath) throws RepositoryException {
	// QueryManager queryManager = session.getWorkspace().getQueryManager();
	// QueryObjectModelFactory factory = queryManager.getQOMFactory();
	// Selector source = factory.selector(PeopleTypes.PEOPLE_MAILING_LIST,
	// PeopleTypes.PEOPLE_MAILING_LIST);
	//
	// Constraint constraint = factory.descendantNode(
	// source.getSelectorName(), basePath);
	//
	// DynamicOperand dynOp = factory.propertyValue(source.getSelectorName(),
	// Property.JCR_TITLE);
	// StaticOperand statOp = factory.literal(session.getValueFactory()
	// .createValue(name.toLowerCase()));
	// Constraint defaultC = factory.comparison(factory.lowerCase(dynOp),
	// QueryObjectModelFactory.JCR_OPERATOR_EQUAL_TO, statOp);
	//
	// QueryObjectModel query = factory.createQuery(source,
	// factory.and(constraint, defaultC), null, null);
	// QueryResult result = query.execute();
	// NodeIterator ni = result.getNodes();
	//
	// long resultNb = ni.getSize();
	//
	// if (resultNb == 0)
	// return null;
	// else if (resultNb == 1)
	// return ni.nextNode();
	// else
	// throw new PeopleException("More than one mailing list with name "
	// + name + " has been found.");
	// }

}