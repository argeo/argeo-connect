package org.argeo.connect.streams.web.providers;

import java.util.List;

import javax.jcr.Node;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Canonical implementation for tables and viewers that display a list of Nodes
 */
public class SimpleNodeListContentProvider implements IStructuredContentProvider {
	private static final long serialVersionUID = 1L;
	private List<Node> nodes;

	public void dispose() {
	}

	/** Expects a list of nodes as a new input */
	@SuppressWarnings("unchecked")
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		nodes = (List<Node>) newInput;
	}

	public Object[] getElements(Object arg0) {
		return nodes.toArray();
	}
}