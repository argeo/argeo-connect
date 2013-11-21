package org.argeo.connect.people.utils;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
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
 * static utilitary methods to manage CRM contact concepts in JCR. Rather use
 * these methods than direct Jcr queries in order to ease model evolution.
 * 
 * Must be refactored in a ContactService
 */

public class ContactJcrUtils {

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

			String relPath = "/"+  PeopleJcrUtils.getRelPathForEntity(referencedNode);
			String absPath = parentNode.getPath() + relPath;
			if (session.nodeExists(absPath))
				return session.getNode(absPath);

			boolean wasCheckedout = CommonsJcrUtils
					.isNodeCheckedOutByMe(referencingNode);
			if (!wasCheckedout)
				CommonsJcrUtils.checkout(referencingNode);

			// add the corresponding node
			Node parlink = JcrUtils.mkdirs(parentNode,
					JcrUtils.parentPath(relPath), NodeType.NT_UNSTRUCTURED);
			Node link = parlink.addNode(JcrUtils.lastPathElement(relPath),
					linkNodeType);
			link.setProperty(PeopleNames.PEOPLE_REF_UID, referencedNode
					.getProperty(PeopleNames.PEOPLE_UID).getString());
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

}