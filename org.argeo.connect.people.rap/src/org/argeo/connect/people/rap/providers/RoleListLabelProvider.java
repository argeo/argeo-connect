package org.argeo.connect.people.rap.providers;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.rap.utils.PeopleHtmlUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;

/**
 * Provide a single column label provider for person lists
 */
public class RoleListLabelProvider extends ColumnLabelProvider implements
		PeopleNames {
	private static final long serialVersionUID = 1L;

	public RoleListLabelProvider() {
	}

	@Override
	public String getText(Object element) {
		try {
			Node node = (Node) element;
			Node link;
			if (node.isNodeType(PeopleTypes.PEOPLE_POSITION)) {
				link = node;
			} else
				throw new PeopleException("Unvalid node type. "
						+ "Cannot display role information");

			String pos = CommonsJcrUtils.get(link,
					PeopleNames.PEOPLE_ROLE);

			// ROLE
			StringBuilder builder = new StringBuilder();
			if (CommonsJcrUtils.checkNotEmptyString(pos))
				builder.append("<b>").append(pos).append("</b> <br/>");
			// TODO ? manage multiple roles
			// for (String token : pos.split(","))
			// builder.append(token).append("<br/>");

			// DEPARTMENT
			if (link.isNodeType(PeopleTypes.PEOPLE_JOB)) {
				String dep = CommonsJcrUtils.get(link,
						PeopleNames.PEOPLE_DEPARTMENT);
				if (CommonsJcrUtils.checkNotEmptyString(dep))
					builder.append("Department: ").append(dep);

			}
			return PeopleHtmlUtils.cleanHtmlString(builder.toString());
		} catch (RepositoryException re) {
			throw new PeopleException("Cannot create organizations content", re);
		}
	}
}