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

import org.argeo.connect.film.FilmTypes;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.jcr.JcrUtils;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.context.SecurityContextHolder;

/** Concrete access to people services */
public class PeopleServiceImpl implements PeopleService {

	/* DEPENDENCY INJECTION */
	private Repository repository;
	private Map<Integer, String> managedRoles;

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

			if (ni.getSize() != 1) {
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

	/**
	 * Creates and returns a model specific Node to store a reference, depending
	 * on the two object we want to link togeteher. Overwrite to add some new
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
			}

			if (parentNode == null || linkNodeType == null)
				throw new PeopleException("Unsupported reference: from "
						+ referencingNode + " to " + referencedNode);

			// TODO manage duplicates
			boolean wasCheckedout = CommonsJcrUtils
					.isNodeCheckedOutByMe(referencingNode);
			if (!wasCheckedout)
				CommonsJcrUtils.checkout(referencingNode);

			// add the corresponding node
			Node link = parentNode.addNode(role, linkNodeType);
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
	/** returns true if the current user is in the specified role */
	public boolean isUserInRole(Integer userRole) {
		String role = managedRoles.get(userRole);
		Authentication authen = SecurityContextHolder.getContext()
				.getAuthentication();
		for (GrantedAuthority ga : authen.getAuthorities()) {
			if (ga.getAuthority().equals(role))
				return true;
		}
		return false;
		// return currentUserService.getCurrentUser().getRoles().contains(role);
	}

	/** returns the current user ID **/
	public String getCurrentUserId() {
		Authentication authen = SecurityContextHolder.getContext()
				.getAuthentication();
		return authen.getName();
	}

	/** Returns a human readable display name using the user ID **/
	public String getUserDisplayName(String userId) {
		// FIXME Must use a commons utils
		return userId;
	}

	/** Expose injected repository */
	public Repository getRepository() {
		return repository;
	}

	/* DEPENDENCY INJECTION */
	public void setManagedRoles(Map<Integer, String> managedRoles) {
		this.managedRoles = managedRoles;
	}

	// Inject a map with all business catalogs
	public void setBusinessCatalogs(Map<String, Object> businessCatalogs) {
		// this.businessCatalogs = businessCatalogs;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}
}
