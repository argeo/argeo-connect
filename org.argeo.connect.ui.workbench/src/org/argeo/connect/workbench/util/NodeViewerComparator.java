package org.argeo.connect.workbench.util;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.argeo.connect.ConnectException;
import org.eclipse.jface.viewers.Viewer;

/**
 * Extends basic node comparator adding an experimental implementation to
 * compare to multiple valued String properties
 */

public class NodeViewerComparator extends org.argeo.eclipse.ui.jcr.lists.NodeViewerComparator {

	private static final long serialVersionUID = 5916940615038882583L;

	/**
	 * e1 and e2 must both be Jcr nodes.
	 * 
	 * @param viewer
	 * @param e1
	 * @param e2
	 * @return
	 */
	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		try {

			Node n1 = (Node) e1;
			Node n2 = (Node) e2;

			if (n1.hasProperty(propertyName) && n1.getProperty(propertyName).isMultiple())
				if (n1.getProperty(propertyName).getType() == PropertyType.STRING)
					return compareMultipleStrings(n1, n2);
				else
					throw new ConnectException(
							"Comparison of multiple value properties is only implemented for Strings");
			else if (n2.hasProperty(propertyName) && n2.getProperty(propertyName).isMultiple())
				if (n2.getProperty(propertyName).getType() == PropertyType.STRING)
					return compareMultipleStrings(n1, n2);
				else
					throw new ConnectException(
							"Comparison of multiple value properties is only implemented for Strings");
			else
				return super.compare(viewer, e1, e2);
		} catch (RepositoryException re) {
			throw new ConnectException("Unexpected error " + "while comparing nodes", re);
		}
	}

	private int compareMultipleStrings(Node n1, Node n2) throws RepositoryException {
		int rc = 0;

		if (!n1.hasProperty(propertyName))
			rc = -1;
		else if (!n2.hasProperty(propertyName))
			rc = 1;
		else {
			String s1 = getString(n1.getProperty(propertyName).getValues());
			String s2 = getString(n2.getProperty(propertyName).getValues());
			rc = s1.compareTo(s2);
		}

		if (direction == DESCENDING) {
			rc = -rc;
		}
		return rc;
	}

	private String getString(Value[] values) throws RepositoryException {
		StringBuilder builder = new StringBuilder();
		for (Value value : values)
			builder.append(value.getString());
		return builder.toString();
	}
}
