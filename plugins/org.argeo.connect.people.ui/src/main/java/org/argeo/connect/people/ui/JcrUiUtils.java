package org.argeo.connect.people.ui;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

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
}
