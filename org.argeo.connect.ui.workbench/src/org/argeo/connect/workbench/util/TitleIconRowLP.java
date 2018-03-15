package org.argeo.connect.workbench.util;

import javax.jcr.Node;

import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.workbench.SystemWorkbenchService;
import org.eclipse.swt.graphics.Image;

/**
 * Add an icon to the results, using the node type of the node retrieved using
 * the selector name. It uses a JcrHtmlLabelProvider rather than a
 * SimpleJcrRowLabelProvider: TO BE VALIDATED
 */
public class TitleIconRowLP extends JcrHtmlLabelProvider {
	private static final long serialVersionUID = 6064779874148619776L;

	private final SystemWorkbenchService systemWorkbenchService;
	private final String selectorName;

	public TitleIconRowLP(SystemWorkbenchService systemWorkbenchService, String selectorName, String propertyName) {
		super(selectorName, propertyName);
		this.systemWorkbenchService = systemWorkbenchService;
		this.selectorName = selectorName;
	}

	@Override
	public Image getImage(Object element) {
		Node node = ConnectJcrUtils.getNodeFromElement(element, selectorName);
		return systemWorkbenchService.getIconForType(node);
	}
}
