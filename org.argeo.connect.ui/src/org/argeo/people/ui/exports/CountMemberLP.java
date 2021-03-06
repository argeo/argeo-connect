package org.argeo.people.ui.exports;

import javax.jcr.Node;

import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.util.ConnectJcrUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;

public class CountMemberLP extends ColumnLabelProvider {
	private static final long serialVersionUID = 1L;

	private final ResourcesService resourcesService;

	public CountMemberLP(ResourcesService resourcesService) {
		this.resourcesService = resourcesService;
	}

	@Override
	public String getText(Object element) {
		Node currNode = ConnectJcrUtils.getNodeFromElement(element, null);
		long count = resourcesService.countMembers(currNode);
		return "" + count;
	}
}
