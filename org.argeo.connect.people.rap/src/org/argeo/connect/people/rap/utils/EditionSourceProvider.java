package org.argeo.connect.people.rap.utils;

import java.util.HashMap;
import java.util.Map;

import org.argeo.connect.people.rap.PeopleRapPlugin;
import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;

/**
 * Used to manage the editing state of an ui editor that is bound to a node.
 * 
 * TODO: check this to simplify: http://wiki.eclipse.org/RCP_FAQ#
 * How_can_I_get_my_views_and_editors_to_coordinate_with_each_other.3F
 */
public class EditionSourceProvider extends AbstractSourceProvider {
	public final static String EDITING_STATE = PeopleRapPlugin.PLUGIN_ID
			+ ".editingState";
	private final static String EDITING = "editing";
	private final static String NOT_EDITING = "notEditing";
	boolean isEditing = false;

	@Override
	public String[] getProvidedSourceNames() {
		return new String[] { EDITING_STATE };
	}

	@Override
	public Map<String, String> getCurrentState() {
		Map<String, String> currentState = new HashMap<String, String>(1);
		String currEditingState = isEditing ? EDITING : NOT_EDITING;
		currentState.put(EDITING_STATE, currEditingState);
		return currentState;
	}

	@Override
	public void dispose() {
	}

	/**
	 * Notify the framework that the check out state has been updated, so that
	 * it can disable or enable check out button when relevant item is
	 * respectively checked-out or checked in..
	 */
	public void setCurrentItemEditingState(boolean isEditing) {
		this.isEditing = isEditing;
		String currEditingState = isEditing ? EDITING : NOT_EDITING;
		fireSourceChanged(ISources.WORKBENCH, EDITING_STATE, currEditingState);
	}
}