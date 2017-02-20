package org.argeo.connect.ui.workbench.util;

import javax.jcr.Node;

import org.argeo.connect.ui.ConnectUiUtils;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.eclipse.ui.jcr.lists.SimpleJcrNodeLabelProvider;

/**
 * Wraps the getText() method of the SimpleJcrNodeLabelProvider to remove
 * invalid characters, typically the ampersand from the returned String.
 * Retrieves relevant node using the selector name or the Row.getNode() method
 * if no name has been provided considering then that the row contains only one
 * node (typically for xpath queries). It also works if element is directly a
 * node
 */
public class JcrHtmlLabelProvider extends SimpleJcrNodeLabelProvider {
	private static final long serialVersionUID = 2134911527741337612L;

	private String selectorName;

	public JcrHtmlLabelProvider(String selectorName, String propertyName) {
		super(propertyName);
		if (EclipseUiUtils.notEmpty(selectorName))
			this.selectorName = selectorName;
	}

	public JcrHtmlLabelProvider(String propertyName) {
		super(propertyName);
	}

	@Override
	public String getText(Object element) {
		Node currNode = ConnectJcrUtils.getNodeFromElement(element, selectorName);
		return ConnectUiUtils.replaceAmpersand(super.getText(currNode));
	}
}