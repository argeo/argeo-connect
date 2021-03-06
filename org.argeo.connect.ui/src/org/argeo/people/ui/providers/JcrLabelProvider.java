package org.argeo.people.ui.providers;

import javax.jcr.Node;

import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.eclipse.ui.jcr.lists.SimpleJcrNodeLabelProvider;

/**
 * Wraps the getText() method of the SimpleJcrNodeLabelProvider. Works with
 * single nodes, single node row and multiple nodes rows
 */
public class JcrLabelProvider extends SimpleJcrNodeLabelProvider {
	private static final long serialVersionUID = 3265805393751537765L;

	private String selectorName;

	public JcrLabelProvider(String selectorName, String propertyName) {
		super(propertyName);
		if (EclipseUiUtils.notEmpty(selectorName))
			this.selectorName = selectorName;
	}

	public JcrLabelProvider(String propertyName) {
		super(propertyName);
	}

	@Override
	public String getText(Object element) {
		Node currNode = ConnectJcrUtils.getNodeFromElement(element, selectorName);
		return super.getText(currNode);
	}
}
