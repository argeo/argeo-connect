package org.argeo.people;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.connect.AppService;

/** Provides method interfaces to manage a people repository */
public interface PeopleService extends AppService {

	/**
	 * Returns the corresponding people entity using the People UID that is
	 * stored under propName. Returns null if the property is undefined or if
	 * there is no entity with this people UID
	 */
	public Node getEntityFromNodeReference(Node node, String propName);

	/**
	 * Creates and returns a model specific Node to store a reference, depending
	 * on the two object we want to link together
	 */
	public Node createEntityReference(Node referencingNode, Node referencedNode, String role);

	/**
	 * Returns all entities with the given NodeType related to this entity or
	 * null if none has been found. Key for relation is implementation specific:
	 * it might be a JCR Identifier but must not.
	 * 
	 * @param relatedEntityType
	 *            Optionally, the type of the grand-parent node typically to
	 *            choose between an organisation, a group or a person in a group
	 */
	public List<Node> getRelatedEntities(Node entity, String linkNodeType, String relatedEntityType);

	/**
	 * Simply look for primary information and update primary cache if needed
	 */
	public void updatePrimaryCache(Node entity) throws PeopleException, RepositoryException;

	/* EXPOSE THE VARIOUS BUSINESS SERVICES */
	/** Returns the corresponding {@link PersonService} */
	public PersonService getPersonService();

	/** Returns the corresponding {@link ContactService} */
	public ContactService getContactService();

}
