package org.argeo.connect.people;

import java.awt.Image;

import javax.jcr.Node;

/**
 * Provides method interfaces to manage contact concept in a people repository.
 * Implementing applications should extend and/or override the canonical
 * implementation in order to provide business specific behaviours.
 * 
 * The correct instance of this interface is usually acquired through the
 * peopleService.
 * */
public interface ContactService {

	/** Retrieves the valid possible contact types */
	public String[] getKnownContactTypes();

	/** Retrieves the valid possible contact type labels */
	public String[] getKnownContactLabels();

	/** Returns the list of possible contacts given an entity */
	public String[] getContactTypeLabels(Node entity);

	/**
	 * Returns the list of possible values given the other information we already
	 * have for this contact instance
	 */
	public String[] getContactPossibleValues(Node contact, String property);

	/**
	 * Returns the list of valid categories, depending on the contact type and contact
	 * nature before the creation of the contact
	 */
	public String[] getContactCategories(String contactableType,
			String contactType, String nature);

	/** Return corresponding icon given a contact instance */
	public Image getContactIcon(Node contact);

}