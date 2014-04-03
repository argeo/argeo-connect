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
			Node referencedManager = taskNode.getProperty(
					PeopleNames.PEOPLE_ASSIGNED_TO).getNode();
			return CommonsJcrUtils.get(referencedManager, Property.JCR_TITLE);
		} catch (RepositoryException e) {
			throw new PeopleException(
					"Unable to get name of group assigned to " + taskNode, e);
		}
	}

	/**
	 * Get the display name for the manager of an activity.
	 * 
	 * @return
	 */
	public static String getActivityManagerDisplayName(Node activityNode) {
		try {
			Node referencedManager = activityNode.getProperty(
					PeopleNames.PEOPLE_MANAGER).getNode();
			return referencedManager.getParent().getName();

		} catch (RepositoryException e) {
			throw new PeopleException("Unable to get type for activity "
					+ activityNode, e);
		}
	}
}