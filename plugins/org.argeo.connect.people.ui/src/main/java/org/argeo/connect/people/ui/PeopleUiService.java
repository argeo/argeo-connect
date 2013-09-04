package org.argeo.connect.people.ui;

import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.ui.listeners.NodeListDoubleClickListener;

/**
 * Enable overwriting of listeners and UI part factories with project specific
 * implementations
 */
public interface PeopleUiService {

	/**
	 * Overwrite this method to provide project specific listener to enable
	 * management of project specific UI Parts
	 */
	public NodeListDoubleClickListener getNewNodeListDoubleClickListener(
			PeopleService peopleService, String currentTableId);

}
