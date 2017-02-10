package org.argeo.connect.people.ui.exports;

import static org.argeo.eclipse.ui.EclipseUiUtils.notEmpty;

import javax.jcr.Node;

import org.argeo.connect.people.ActivityService;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.jcr.lists.SimpleJcrNodeLabelProvider;

/**
 * Returns the assigned to display name given a row that contains a Task
 * selector
 */
public class AssignedToLP extends SimpleJcrNodeLabelProvider {
	private static final long serialVersionUID = 1L;

	private String selectorName;
	private ActivityService activityService;

	public AssignedToLP(PeopleService peopleService, String selectorName, String propertyName) {
		super(propertyName);
		if (notEmpty(selectorName))
			this.selectorName = selectorName;
		activityService = peopleService.getActivityService();
	}

	@Override
	public String getText(Object element) {
		Node currNode = ConnectJcrUtils.getNodeFromElement(element, selectorName);
		return activityService.getAssignedToDisplayName(currNode);
	}
}
