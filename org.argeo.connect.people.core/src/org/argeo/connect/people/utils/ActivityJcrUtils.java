package org.argeo.connect.people.utils;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.argeo.connect.people.ActivityValueCatalogs;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;

/**
 * Static utility methods to manage activity concepts in JCR. Rather use these
 * methods than direct JCR queries in order to ease model evolution.
 * 
 * Might be refactored as an ActivityService
 */

public class ActivityJcrUtils {

	/**
	 * 
	 * Get the label of an activity. It relies on the mixin of this node. If an
	 * activity Node is having more than one "activity type" mixin, the first
	 * found will be used to return type label.
	 * 
	 * @return
	 */
	public static String getActivityTypeLbl(Node activityNode) {
		try {
			for (String type : ActivityValueCatalogs.MAPS_ACTIVITY_TYPES
					.keySet()) {
				if (activityNode.isNodeType(type))
					return ActivityValueCatalogs.MAPS_ACTIVITY_TYPES.get(type);
			}
			throw new PeopleException("Undefined type for activity: "
					+ activityNode);
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to get type for activity "
					+ activityNode, e);
		}
	}

	/**
	 * Get the display name of the assigned to group for this task
	 * 
	 * @return
	 */
	public static String getAssignedToDisplayName(Node taskNode) {
		try {
			if (taskNode.hasProperty(PeopleNames.PEOPLE_ASSIGNED_TO)) {
				Node referencedManager = taskNode.getProperty(
						PeopleNames.PEOPLE_ASSIGNED_TO).getNode();
				return CommonsJcrUtils.get(referencedManager,
						Property.JCR_TITLE);
			} else
				return "";
		} catch (RepositoryException e) {
			throw new PeopleException(
					"Unable to get name of group assigned to " + taskNode, e);
		}
	}

	/** Get the display name for the manager of an activity. */
	public static String getActivityManagerDisplayName(Node activityNode) {
		// TODO return display name rather than ID
		String manager = CommonsJcrUtils.get(activityNode,
				PeopleNames.PEOPLE_REPORTED_BY);

		if (CommonsJcrUtils.isEmptyString(manager)) {
			// TODO workaround to try to return a manager name in case we are in
			// a legacy context
			try {
				if (activityNode.hasProperty(PeopleNames.PEOPLE_MANAGER)) {
					Node referencedManager = activityNode.getProperty(
							PeopleNames.PEOPLE_MANAGER).getNode();
					manager = referencedManager.getParent().getName();
				}
			} catch (RepositoryException e) {
				throw new PeopleException("Unable to legacy get "
						+ "manager name for activity " + activityNode, e);
			}
		}
		return manager;
	}
}