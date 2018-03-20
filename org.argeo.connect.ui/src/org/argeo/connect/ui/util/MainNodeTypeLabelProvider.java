package org.argeo.connect.ui.util;

import javax.jcr.Node;

import org.argeo.connect.AppService;
import org.argeo.connect.util.ConnectJcrUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;

/**
 * Best effort to determine the most import type of the current Node (or of the
 * node retrieved in current Row with this selector Name) and returns a human
 * friendly label.
 */
public class MainNodeTypeLabelProvider extends ColumnLabelProvider {
	private static final long serialVersionUID = 6026760216411653801L;

	private AppService appService;
	private String selectorName = null;

	public MainNodeTypeLabelProvider(AppService appService) {
		this.appService = appService;
	}

	public MainNodeTypeLabelProvider(AppService appService, String selectorName) {
		this.appService = appService;
		this.selectorName = selectorName;
	}

	@Override
	public String getText(Object element) {
		Node currNode = ConnectJcrUtils.getNodeFromElement(element, selectorName);
		String label = appService.getMainNodeType(currNode);
		return appService.getLabel(label);
	}
}
