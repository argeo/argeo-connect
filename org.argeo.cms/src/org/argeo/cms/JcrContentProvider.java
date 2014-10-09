package org.argeo.cms;

import java.util.ArrayList;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class JcrContentProvider implements IStructuredContentProvider {
	private static final long serialVersionUID = -1333678161322488674L;

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (!(newInput instanceof Node))
			throw new CmsException("Input " + newInput + " must be a node");
	}

	@Override
	public Object[] getElements(Object inputElement) {
		try {
			Node node = (Node) inputElement;
			ArrayList<Node> arr = new ArrayList<Node>();
			NodeIterator nit = node.getNodes();
			while (nit.hasNext()) {
				arr.add(nit.nextNode());
			}
			return arr.toArray();
		} catch (RepositoryException e) {
			throw new CmsException("Cannot get elements", e);
		}

	}

}
