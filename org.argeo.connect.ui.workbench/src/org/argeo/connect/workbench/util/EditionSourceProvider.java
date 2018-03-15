package org.argeo.connect.workbench.util;

import java.util.HashMap;
import java.util.Map;

import org.argeo.connect.workbench.ConnectUiPlugin;
import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;

/**
 * Used to manage the editing state of an ui editor that is bound to a node.
 * 
 * TODO: check this to simplify: http://wiki.eclipse.org/RCP_FAQ#
 * How_can_I_get_my_views_and_editors_to_coordinate_with_each_other.3F
 */
public class EditionSourceProvider extends AbstractSourceProvider {
	public final static String EDITING_STATE = ConnectUiPlugin.PLUGIN_ID + ".editingState";
	private final static String EDITING = "editing";
	private final static String NOT_EDITING = "notEditing";
	private final static String NOT_EDITABLE = "notEditable";
	boolean isEditing = false;
	boolean isEditable = true;

	@Override
	public String[] getProvidedSourceNames() {
		return new String[] { EDITING_STATE };
	}

	@Override
	public Map<String, String> getCurrentState() {
		Map<String, String> currentState = new HashMap<String, String>(1);
		String currEditingState = null;
		if (isEditable)
			currEditingState = isEditing ? EDITING : NOT_EDITING;
		else
			currEditingState = NOT_EDITABLE;
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
	public void setCurrentItemEditingState(boolean isEditable, boolean isEditing) {
		this.isEditing = isEditing;
		this.isEditable = isEditable;
		String currEditingState = null;
		if (isEditable)
			currEditingState = isEditing ? EDITING : NOT_EDITING;
		else
			currEditingState = NOT_EDITABLE;
		fireSourceChanged(ISources.WORKBENCH, EDITING_STATE, currEditingState);
	}
}
