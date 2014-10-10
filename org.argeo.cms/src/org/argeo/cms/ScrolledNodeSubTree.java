package org.argeo.cms;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class ScrolledNodeSubTree extends ScrolledPage {
	private static final long serialVersionUID = 4566882270649377366L;

	public ScrolledNodeSubTree(Composite parent, int style) {
		super(parent, style);
	}

	/**
	 * Returns the composite related to this item in the composite tree, or null
	 * if not found.
	 */
	public Composite find(Item item) {
		try {
			Object basePath = getData(Property.JCR_PATH);
			if (basePath == null)
				throw new CmsException("Base path must be set");
			Node baseNode = item.getSession().getNode(basePath.toString());
			if (!item.getAncestor(baseNode.getDepth()).getPath()
					.equals(baseNode.getPath()))
				throw new CmsException(item + " is not a descendant of "
						+ baseNode);

			Composite currComposite = this;
			for (int i = baseNode.getDepth() + 1; i <= item.getDepth(); i++) {
				currComposite = findChild(currComposite, item.getAncestor(i)
						.getPath());
				// not found if one of the parent is not found
				if (currComposite == null)
					return null;
			}
			return currComposite;
		} catch (RepositoryException e) {
			throw new CmsException("Cannot find composite for " + item, e);
		}
	}

	protected Composite findChild(Composite composite, String path) {
		for (Control control : composite.getChildren()) {
			if (control instanceof Composite) {
				String childPath = control.getData(Property.JCR_PATH)
						.toString();
				if (childPath == null) {
					// if not a registered composite, scan Composite subtree,
					// it allows to deal with intermediary composites used for
					// layout purpose
					Composite subTreeScanResult = findChild(
							(Composite) control, path);
					if (subTreeScanResult != null)
						return subTreeScanResult;
				} else {
					if (childPath.equals(path))
						return (Composite) control;
				}
			}
		}
		return null;
	}
}
