package org.argeo.connect.people.ui;

import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.ui.listeners.NodeListDoubleClickListener;

public class PeopleUiServiceImpl implements PeopleUiService {

	@Override
	public NodeListDoubleClickListener getNewNodeListDoubleClickListener(
			PeopleService peopleService, String currentTableId) {
		return new NodeListDoubleClickListener(peopleService, currentTableId);

	}
}
