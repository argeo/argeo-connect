package org.argeo.connect.people.workbench.rap.providers;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.ui.ConnectUiUtils;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
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

			String pos = ConnectJcrUtils.get(link, PeopleNames.PEOPLE_ROLE);

			// ROLE
			StringBuilder builder = new StringBuilder();
			if (EclipseUiUtils.notEmpty(pos))
				builder.append("<b>").append(pos).append("</b> <br/>");

			// DEPARTMENT
			if (link.isNodeType(PeopleTypes.PEOPLE_JOB)) {
				String dep = ConnectJcrUtils
						.get(link, PeopleNames.PEOPLE_DEPARTMENT);
				if (EclipseUiUtils.notEmpty(dep))
					builder.append("Department: ").append(dep);

			}
			return ConnectUiUtils.replaceAmpersand(builder.toString());
		} catch (RepositoryException re) {
			throw new PeopleException("Cannot create organizations content", re);
		}
	}
}