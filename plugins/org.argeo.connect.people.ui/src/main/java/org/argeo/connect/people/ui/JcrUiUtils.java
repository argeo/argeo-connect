package org.argeo.connect.people.ui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.argeo.ArgeoException;

/** Some static utils methods that might be factorized in a near future */
public class JcrUiUtils {

	/** Convert a {@link NodeIterator} to a list of {@link Node} */
	public static List<Node> nodeIteratorToList(NodeIterator nodeIterator,
			int limit) {
		List<Node> nodes = new ArrayList<Node>();
		int i = 0;
		while (nodeIterator.hasNext() && i < limit) {
			nodes.add(nodeIterator.nextNode());
			i++;
		}
		return nodes;
	}

	/**
	 * Centralizes management of updating property value. Among other to avoid
	 * infinite loop when the new value is the same as the ones that is already
	 * stored in JCR.
	 * 
	 * @return true if the value as changed
	 */
	public boolean setJcrProperty(Node node, String propName, int propertyType,
			Object value) {
		try {
			// int propertyType = getPic().getProperty(propName).getType();
			switch (propertyType) {
			case PropertyType.STRING:
				if (node.hasProperty(propName)
						&& node.getProperty(propName).getString()
								.equals((String) value))
					// nothing changed yet
					return false;
				else {
					node.setProperty(propName, (String) value);
					return true;
				}
			case PropertyType.BOOLEAN:
				if (node.hasProperty(propName)
						&& node.getProperty(propName).getBoolean() == (Boolean) value)
					// nothing changed yet
					return false;
				else {
					node.setProperty(propName, (Boolean) value);
					return true;
				}
			case PropertyType.DATE:
				if (node.hasProperty(propName)
						&& node.getProperty(propName).getDate()
								.equals((Calendar) value))
					// nothing changed yet
					return false;
				else {
					node.setProperty(propName, (Calendar) value);
					return true;
				}

			default:
				throw new ArgeoException("Unimplemented property save");
			}
		} catch (RepositoryException re) {
			throw new ArgeoException("Unexpected error while setting property",
					re);
		}
	}

}
