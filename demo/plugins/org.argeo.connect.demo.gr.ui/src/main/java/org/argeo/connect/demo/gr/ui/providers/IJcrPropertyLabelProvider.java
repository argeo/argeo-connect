package org.argeo.connect.demo.gr.ui.providers;

import javax.jcr.Node;

/**
 * Wrapper utility to insure we can have a well formatted string corresponding
 * to the given Node and current context only with a property name. It addresses
 * by instance case when the given property is on a child node or a node
 * referenced by given node
 */
public interface IJcrPropertyLabelProvider {
	/**
	 * Returns a correctly formatted String for given node and property name
	 * depending on the current context
	 */
	public String getFormattedPropertyValue(Node node, String propertyName);
}
