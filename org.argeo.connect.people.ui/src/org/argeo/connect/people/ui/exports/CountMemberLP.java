package org.argeo.connect.people.ui.exports;

import javax.jcr.Node;

import org.argeo.connect.people.ResourceService;
import org.argeo.connect.util.ConnectJcrUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;

public class CountMemberLP extends ColumnLabelProvider {
	private static final long serialVersionUID = 1L;

	private final ResourceService resourceService;

	public CountMemberLP(ResourceService resourceService) {
		this.resourceService = resourceService;
	}

	@Override
	public String getText(Object element) {
		Node currNode = ConnectJcrUtils.getNodeFromElement(element, null);
		long count = resourceService.countMembers(currNode);
		return "" + count;
	}
}
