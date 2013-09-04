package org.argeo.connect.people.ui.providers;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;

/**
 * Provide a single column label provider for person lists
 */
public class PersonOverviewLabelProvider extends ColumnLabelProvider implements
		PeopleNames {

	private static final long serialVersionUID = 1L;

	private boolean isSmallList = true;
	private PeopleService peopleService;

	public PersonOverviewLabelProvider(boolean isSmallList,
			PeopleService peopleService) {
		this.isSmallList = isSmallList;
		this.peopleService = peopleService;
	}

	@Override
	public String getText(Object element) {
		try {
			Node node = (Node) element;
			Node entity;
			if (node.isNodeType(PeopleTypes.PEOPLE_PERSON))
				entity = node;
			// FIXME works with luck
			else if (node.isNodeType(PeopleTypes.PEOPLE_JOB))
				entity = node.getParent().getParent();
			else if (node.isNodeType(PeopleTypes.PEOPLE_POSITION)) {
				entity = peopleService.getEntityById(node.getSession(), node
						.getProperty(PEOPLE_REF_UID).getString());
			} else
				throw new PeopleException("Unvalid node type. "
						+ "Cannot display film information");

			StringBuilder builder = new StringBuilder();
			if (isSmallList)
				builder.append("<span>");
			else
				builder.append("<span style='font-size:15px;'>");

			String currValue = null;
			// first line
			builder.append("<b><big> ").append(
					CommonsJcrUtils.getStringValue(entity, PEOPLE_LAST_NAME));
			currValue = CommonsJcrUtils.getStringValue(entity,
					PEOPLE_FIRST_NAME);
			if (currValue != null) {
				builder.append(", ");
				builder.append(currValue);
			}

			builder.append("</big> </b>").append("<br/>").append("</span>");

			// currValue = CommonsJcrUtils.getStringValue(person,
			// MSM_PRIMARY_EMAIL);
			// // builder.append("<small>");
			// if (currValue != null) {
			// builder.append(" <a href=\"mailto:");
			// builder.append(currValue);
			// builder.append("\">");
			// builder.append(currValue);
			// builder.append("</a>");
			// }
			return builder.toString();
		} catch (RepositoryException re) {
			throw new PeopleException("Cannot create organizations content", re);
		}
	}
}
