package org.argeo.connect.ui.util;

import java.util.List;

import javax.jcr.Node;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Default implementation of a content provider for all tables and viewer that
 * display a list of Nodes
 */
public class BasicNodeListContentProvider implements IStructuredContentProvider {
	private static final long serialVersionUID = 1L;
	// keep a cache of the Nodes in the content provider to be able to
	// manage long request
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
