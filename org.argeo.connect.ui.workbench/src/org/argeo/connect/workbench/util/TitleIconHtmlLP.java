package org.argeo.connect.workbench.util;

import javax.jcr.Node;
import javax.jcr.Property;

import org.argeo.connect.util.ConnectUtils;
import org.argeo.connect.workbench.AppWorkbenchService;
import org.argeo.eclipse.ui.jcr.lists.SimpleJcrNodeLabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Add an icon to the results, using the AppWorkbenchService. It also clean the
 * retrieved texts to be compliant with RAP
 */
public class TitleIconHtmlLP extends SimpleJcrNodeLabelProvider {
	private static final long serialVersionUID = 6064779874148619776L;

	private final AppWorkbenchService appWorkbenchService;

	public TitleIconHtmlLP(AppWorkbenchService appWorkbenchService) {
		super(Property.JCR_TITLE);
		this.appWorkbenchService = appWorkbenchService;
	}

	public TitleIconHtmlLP(AppWorkbenchService appWorkbenchService, String propertyName) {
		super(propertyName);
		this.appWorkbenchService = appWorkbenchService;
	}

	@Override
	public String getText(Object element) {
		return ConnectUtils.replaceAmpersand(super.getText(element));
	}

	@Override
	public Image getImage(Object element) {
		return appWorkbenchService.getIconForType(((Node) element));
	}
}
