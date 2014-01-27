package org.argeo.connect.people.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.DynamicOperand;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;
import javax.jcr.query.qom.StaticOperand;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.film.FilmTypes;
import org.argeo.connect.people.ActivityService;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.UserManagementService;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.jcr.JcrUtils;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.context.SecurityContextHolder;

/** Concrete access to people services */
public class PeopleServiceImpl implements PeopleService {
	private final static Log log = LogFactory.getLog(PeopleServiceImpl.class);

	/* DEPENDENCY INJECTION */
	private Repository repository;

	/* Other services */
	private UserManagementService userManagementService = new UserManagementServiceImpl();
	private ActivityService activityService = new ActivityServiceImpl(userManagementService);
	
	// private Map<Integer, String> managedRoles;

	// business catalogs maintained in file business catalogs of the specs
	// bundle
	// private Map<String, Object> businessCatalogs;

	// private Session adminSession = null;

	/* Life cycle management */
	/**
	 * Call by each startup in order to make sure the backend is ready to
	 * receive/provide data.
	 */
	public void init() {
		// Do nothing
		log.info("People's backend has been initialized");
	}

	/** Clean shutdown of the backend. */
	public void destroy() {
		// Do nothing
	}

	/* ENTITY SERVICES */
	@Override
	public Map<String, String> getMapValuesForProperty(String propertyName) {
		return null;
	}

	@Override
	public Node getEntityByUid(Session session, String uid) {
		try {
			QueryManager queryManager = session.getWorkspace()
					.getQueryManager();
			QueryObjectModelFactory factory = queryManager.getQOMFactory();
			Selector source = factory.selector(PeopleTypes.PEOPLE_ENTITY,
					"entities");
			DynamicOperand dynOp = factory.propertyValue(
					source.getSelectorName(), PeopleNames.PEOPLE_UID);
			StaticOperand statOp = factory.literal(session.getValueFactory()
					.createValue(uid));
			Constraint defaultC = factory.comparison(dynOp,
					QueryObjectModelFactory.JCR_OPERATOR_EQUAL_TO, statOp);
			QueryObjectModel query = factory.createQuery(source, defaultC,
					null, null);
			QueryResult queryResult = query.execute();
			NodeIterator ni = queryResult.getNodes();

			if (ni.getSize() == 0)
				return null;
			else if (ni.getSize() > 1) {
				throw new PeopleException(
						"Problem retrieving entity by UID, we found "
								+ ni.getSize() + " correspnding entity(ies)");
			} else
				return ni.nextNode();
		} catch (RepositoryException e) {
			throw new PeopleException(
					"Unable to retrive entity of uid: " + uid, e);
		}
	}

	@Override
	public Node getEntityFromNodeReference(Node node, String propName) {
		try {
			return getEntityByUid(node.getSession(), node.getProperty(propName)
					.getString());
		} catch (RepositoryException re) {
			throw new PeopleException(
					"unable to get entity from reference node", re);
		}
	}

	/**
	 * Creates and returns a model specific Node to store a reference, depending
	 * on the two object we want to link together. Overwrite to add some new
	 * link type
	 * */
	@Override
	public Node createEntityReference(Node referencingNode,
			Node referencedNode, String role) {
		try {
			Node parentNode = null;
			String linkNodeType = null;
			if (referencingNode.isNodeType(PeopleTypes.PEOPLE_PERSON)) {
				if (referencedNode.isNodeType(PeopleTypes.PEOPLE_ORGANIZATION)) {
					linkNodeType = PeopleTypes.PEOPLE_JOB;
					parentNode = referencingNode
							.getNode(PeopleNames.PEOPLE_JOBS);
				}
			} else if (referencingNode.isNodeType(FilmTypes.FILM)) {
				if (referencedNode.isNodeType(PeopleTypes.PEOPLE_ORGANIZATION)
						|| referencedNode.isNodeType(PeopleTypes.PEOPLE_PERSON)) {
					linkNodeType = PeopleTypes.PEOPLE_MEMBER;
					parentNode = referencingNode
							.getNode(PeopleNames.PEOPLE_MEMBERS);
				}
			} else if (referencingNode
					.isNodeType(PeopleTypes.PEOPLE_MAILING_LIST)) {
				if (referencedNode.isNodeType(PeopleTypes.PEOPLE_PERSON)) {
					linkNodeType = PeopleTypes.PEOPLE_MAILING_LIST_ITEM;
					parentNode = referencingNode
							.getNode(PeopleNames.PEOPLE_MEMBERS);
				}
			} else if (referencingNode.isNodeType(PeopleTypes.PEOPLE_GROUP)
					&& !referencingNode
							.isNodeType(PeopleTypes.PEOPLE_MAILING_LIST)) {
				if (referencedNode.isNodeType(PeopleTypes.PEOPLE_ORGANIZATION)
						|| referencedNode.isNodeType(PeopleTypes.PEOPLE_PERSON)
						|| (referencedNode.isNodeType(PeopleTypes.PEOPLE_GROUP) && referencedNode
								.getIdentifier() != referencingNode
								.getIdentifier())) {
					linkNodeType = PeopleTypes.PEOPLE_MEMBER;
					parentNode = referencingNode
							.getNode(PeopleNames.PEOPLE_MEMBERS);
				}
			}
			if (parentNode == null || linkNodeType == null)
				throw new PeopleException("Unsupported reference: from "
						+ referencingNode + "("
						+ referencingNode.getPrimaryNodeType() + ")" + " to "
						+ referencedNode + "("
						+ referencedNode.getPrimaryNodeType() + ")");

			// TODO manage duplicates
			boolean wasCheckedout = CommonsJcrUtils
					.isNodeCheckedOutByMe(referencingNode);
			if (!wasCheckedout)
				CommonsJcrUtils.checkout(referencingNode);

			// add the corresponding node

			Node link = parentNode.addNode(CommonsJcrUtils
					.checkNotEmptyString(role) ? role : "Unnamed_role",
					linkNodeType);
			if (CommonsJcrUtils.checkNotEmptyString(role))
				link.setProperty(PeopleNames.PEOPLE_ROLE, role);
			link.setProperty(PeopleNames.PEOPLE_REF_UID, referencedNode
					.getProperty(PeopleNames.PEOPLE_UID).getString());

			if (!wasCheckedout)
				CommonsJcrUtils.saveAndCheckin(referencingNode);
			else
				referencingNode.getSession().save();

			return link;
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to retrieve related entities", e);
		}
	}

	@Override
	public List<Node> getRelatedEntities(Node entity, String linkNodeType,
			String relatedEntityType) {
		try {
			Session session = entity.getSession();
			QueryManager queryManager = session.getWorkspace()
					.getQueryManager();
			QueryObjectModelFactory factory = queryManager.getQOMFactory();
			final String typeSelector = "relatedType";
			Selector source = factory.selector(linkNodeType, typeSelector);
			DynamicOperand dynOp = factory.propertyValue(
					source.getSelectorName(), PeopleNames.PEOPLE_REF_UID);
			StaticOperand statOp = factory.literal(session.getValueFactory()
					.createValue(
							entity.getProperty(PeopleNames.PEOPLE_UID)
									.getString()));
			Constraint defaultC = factory.comparison(dynOp,
					QueryObjectModelFactory.JCR_OPERATOR_EQUAL_TO, statOp);
			QueryObjectModel query = factory.createQuery(source, defaultC,
					null, null);
			QueryResult queryResult = query.execute();
			NodeIterator ni = queryResult.getNodes();

			if (relatedEntityType == null)
				return JcrUtils.nodeIteratorToList(ni);
			else {

				List<Node> result = new ArrayList<Node>();
				while (ni.hasNext()) {
					Node currNode = ni.nextNode();
					if (currNode.getParent().getParent()
							.isNodeType(relatedEntityType))
						result.add(currNode);
				}
				return result;
			}

		} catch (RepositoryException e) {
			throw new PeopleException("Unable to retrieve related entities", e);
		}

	}

	/* Persons */

	/* USER MANAGEMENT */
	//TODO move this to user management service
	
	/** Expose injected repository */
	public Repository getRepository() {
		return repository;
	}

	/* DEPENDENCY INJECTION */
	// public void setManagedRoles(Map<Integer, String> managedRoles) {
	// this.managedRoles = managedRoles;
	// }

	// Inject a map with all business catalogs
	public void setBusinessCatalogs(Map<String, Object> businessCatalogs) {
		// this.businessCatalogs = businessCatalogs;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	@Override
	public ActivityService getActivityService() {
		return activityService;
	}

	@Override
	public UserManagementService getUserManagementService() {
		return userManagementService;
	}
}
