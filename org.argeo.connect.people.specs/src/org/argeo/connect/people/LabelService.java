package org.argeo.connect.people;

import java.util.Map;

import javax.jcr.Node;

/**
 * Provides method interfaces to manage labels in various people contexts.
 * Implementing applications should extend and/or override the canonical
 * implementation in order to provide business specific behaviours.
 * 
 * The correct instance of this interface is usually acquired through the
 * peopleService.
 * */
public interface LabelService {

	/* NAMES and TYPES LABELS */
	/**
	 * Returns a canonical English label for each of the JCR Property or node
	 * types defined in the current People Repository. If no such label is
	 * defined, returns the property/node name
	 * 
	 **/
	public String getItemDefaultEnLabel(String itemName);

	/**
	 * Returns a label for each of the JCR property name or node type defined in
	 * the current People Repository in a internationalised context. If the
	 * correct item label is not found for this language, the English label is
	 * returned. If such a label is not defined, it returns the item name.
	 * 
	 **/
	public String getItemLabel(String itemName, String langIso);

	/* VALUES LIST */
	/**
	 * Returns the pre-defined possible English values of a Node property
	 * defined in the current People Repository. Throws an exception if the
	 * corresponding list is not found.
	 **/
	public String[] getDefinedValues(Node node, String propertyName);

	/**
	 * Returns a map with the pre-defined possible English values and
	 * corresponding of a Node property defined in the current People
	 * Repository. Throws an exception if the corresponding list is not found.
	 **/
	public Map<String, String> getDefinedValueMap(Node node, String propertyName);
}
