package org.argeo.connect.ui.util;

import javax.jcr.Node;

import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.eclipse.ui.jcr.lists.SimpleJcrNodeLabelProvider;

/**
 * Wraps the getText() method of the SimpleJcrNodeLabelProvider. Retrieves
 * relevant node using the selector name or the Row.getNode() method if no name
 * has been provided considering then that the row contains only one node
 * (typically for xpath queries)
 */
public class JcrRowLabelProvider extends SimpleJcrNodeLabelProvider {
	private static final long serialVersionUID = 3265805393751537765L;

	private String selectorName;

	public JcrRowLabelProvider(String selectorName, String propertyName) {
		super(propertyName);
		if (EclipseUiUtils.notEmpty(selectorName))
			this.selectorName = selectorName;
	}

	public JcrRowLabelProvider(String selectorName, String propertyName,
			String dateFormatPattern, String numberFormatPattern) {
		super(propertyName, dateFormatPattern, numberFormatPattern);
		if (EclipseUiUtils.notEmpty(selectorName))
			this.selectorName = selectorName;
	}

	public JcrRowLabelProvider(String propertyName) {
		super(propertyName);
	}

	@Override
	public String getText(Object element) {
		Node currNode = ConnectJcrUtils.getNodeFromElement(element, selectorName);
		return super.getText(currNode);
	}
}
