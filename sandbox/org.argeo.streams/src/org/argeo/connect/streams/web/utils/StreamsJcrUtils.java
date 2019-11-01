package org.argeo.connect.streams.web.utils;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.ArgeoException;

public class StreamsJcrUtils {

	/**
	 * Concisely gets the string value of a property. Returns an empty String
	 * rather than null if this node doesn't have this property or if the
	 * corresponding property is an empty string.
	 */
	public static String get(Node node, String propertyName) {
		try {
			if (!node.hasProperty(propertyName))
				return "";
			else
				return node.getProperty(propertyName).getString();
		} catch (RepositoryException e) {
			throw new ArgeoException("Cannot get property " + propertyName
					+ " of " + node, e);
		}
	}
	
}
