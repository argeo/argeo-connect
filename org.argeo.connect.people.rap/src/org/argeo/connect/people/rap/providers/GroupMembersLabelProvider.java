package org.argeo.connect.people.rap.providers;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.utils.JcrUiUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;

/**
 * Provide a label provider for group members
 */
public class GroupMembersLabelProvider extends ColumnLabelProvider implements
		PeopleNames {
	private static final long serialVersionUID = -7480621656515913263L;

	private PeopleService peopleService;

	public GroupMembersLabelProvider(PeopleService peopleService) {
		this.peopleService = peopleService;
	}

	@Override
	public String getText(Object element) {
		try {
			// Initialisation
			Node position = (Node) element;
			// Get referenced entity
			Node referencedEntity = peopleService.getEntityByUid(
					position.getSession(),
					JcrUiUtils.get(position, PEOPLE_REF_UID));

			// Create HTML snippet
			StringBuilder builder = new StringBuilder();
			builder.append("<b>");
			builder.append(JcrUiUtils.get(referencedEntity, Property.JCR_TITLE));
			builder.append("</b>");
			String role = JcrUiUtils.get(position, PEOPLE_ROLE);
			if (EclipseUiUtils.notEmpty(role))
				builder.append(" [").append(role).append("]");
			return builder.toString();
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to get text for link", e);
		}

	}
}