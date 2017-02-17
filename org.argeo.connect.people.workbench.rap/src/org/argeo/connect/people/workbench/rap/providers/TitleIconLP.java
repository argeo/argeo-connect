package org.argeo.connect.people.workbench.rap.providers;

import javax.jcr.Node;

import org.argeo.connect.ui.ConnectUiUtils;
import org.argeo.connect.ui.workbench.AppWorkbenchService;
import org.argeo.eclipse.ui.jcr.lists.SimpleJcrNodeLabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Add an icon to the results, using the WorkbenchService. It also clean the
 * retrieved texts to be compliant with RAP
 */
public class TitleIconLP extends SimpleJcrNodeLabelProvider {
	private static final long serialVersionUID = 6064779874148619776L;

	private final AppWorkbenchService peopleWorkbenchService;

	public TitleIconLP(AppWorkbenchService peopleWorkbenchService, String propertyName) {
		super(propertyName);
		this.peopleWorkbenchService = peopleWorkbenchService;
	}

	@Override
	public String getText(Object element) {
		return ConnectUiUtils.replaceAmpersand(super.getText(element));
	}

	@Override
	public Image getImage(Object element) {
		return peopleWorkbenchService.getIconForType(((Node) element));
	}
}