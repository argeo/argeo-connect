package org.argeo.connect.ui.workbench.util;

import javax.jcr.Node;

import org.argeo.connect.ui.workbench.AppWorkbenchService;
import org.argeo.connect.util.ConnectJcrUtils;
import org.eclipse.swt.graphics.Image;

/**
 * Add an icon to the results, using the node type of the node retrieved using
 * the selector name. It uses a JcrHtmlLabelProvider rather than a
 * SimpleJcrRowLabelProvider: TO BE VALIDATED
 */
public class TitleIconRowLP extends JcrHtmlLabelProvider {
	private static final long serialVersionUID = 6064779874148619776L;

	private final AppWorkbenchService appWorkbenchService;
	private final String selectorName;

	public TitleIconRowLP(AppWorkbenchService appWbService, String selectorName, String propertyName) {
		super(selectorName, propertyName);
		this.appWorkbenchService = appWbService;
		this.selectorName = selectorName;
	}

	@Override
	public Image getImage(Object element) {
		Node node = ConnectJcrUtils.getNodeFromElement(element, selectorName);
		return appWorkbenchService.getIconForType(node);
	}
}
