package org.argeo.connect.people.ui;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.connect.film.FilmTypes;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.editors.FilmEditor;
import org.argeo.connect.people.ui.editors.GroupEditor;
import org.argeo.connect.people.ui.editors.OrgEditor;
import org.argeo.connect.people.ui.editors.PersonEditor;
import org.argeo.connect.people.ui.listeners.NodeListDoubleClickListener;

public class PeopleUiServiceImpl implements PeopleUiService {

	@Override
	public NodeListDoubleClickListener getNewNodeListDoubleClickListener(
			PeopleService peopleService) {
		return new NodeListDoubleClickListener(peopleService);

	}

	@Override
	public NodeListDoubleClickListener getNewNodeListDoubleClickListener(
			PeopleService peopleService, String parentNodeType) {
		return new NodeListDoubleClickListener(peopleService, parentNodeType);

	}

	@Override
	public NodeListDoubleClickListener getNewNodeListDoubleClickListener(
			PeopleService peopleService, String parentNodeType,
			String currentTableId) {
		return new NodeListDoubleClickListener(peopleService, parentNodeType,
				currentTableId);

	}

	@Override
	public String getEditorIdFromNode(Node curNode) {
		try {
			if (curNode.isNodeType(PeopleTypes.PEOPLE_PERSON))
				return PersonEditor.ID;
			else if (curNode.isNodeType(PeopleTypes.PEOPLE_ORGANIZATION)) {
				return OrgEditor.ID;
			} else if (curNode.isNodeType(PeopleTypes.PEOPLE_MAILING_LIST)) {
				return GroupEditor.ID;
			} else if (curNode.isNodeType(PeopleTypes.PEOPLE_GROUP)) {
				return GroupEditor.ID;
			} else
				return null;
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to open editor for node", re);
		}
	}
}
