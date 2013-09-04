package org.argeo.connect.people.ui.providers;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleTypes;
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
			if (node.isNodeType(PeopleTypes.PEOPLE_JOB)) {
				link = node;
			} else
				throw new PeopleException("Unvalid node type. "
						+ "Cannot display role information");
			String pos = CommonsJcrUtils.getStringValue(link,
					PeopleNames.PEOPLE_ROLE);
			if (pos == null)
				return "";
			else {
				StringBuilder sb = new StringBuilder();
				sb.append("<b>");
				for (String token : pos.split(","))
					sb.append(token).append("<br/>");
				sb.append("</b>");
				return sb.toString();
			}
		} catch (RepositoryException re) {
			throw new PeopleException("Cannot create organizations content", re);
		}
	}
}