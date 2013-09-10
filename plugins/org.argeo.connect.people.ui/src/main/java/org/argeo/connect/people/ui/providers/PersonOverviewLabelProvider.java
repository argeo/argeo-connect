package org.argeo.connect.people.ui.providers;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.PeopleHtmlUtils;
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
				entity = peopleService.getEntityByUid(node.getSession(), node
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

			builder.append("</big> </b>");
			String fmn = PeopleHtmlUtils.getFullMontyName(entity);
			String local = PeopleHtmlUtils.getLocalisationInfo(entity);
			if (isSmallList) {
				if (CommonsJcrUtils.checkNotEmptyString(local))
					builder.append("[").append(local).append("]");
			} else {
				if (CommonsJcrUtils.checkNotEmptyString(fmn)
						|| CommonsJcrUtils.checkNotEmptyString(local)) {
					builder.append("<br/>").append(fmn);
					if (CommonsJcrUtils.checkNotEmptyString(fmn)
							&& CommonsJcrUtils.checkNotEmptyString(local))
						builder.append(" ~ ");
					builder.append(local);
				}
			}
			builder.append("<br/>");

			if (PeopleHtmlUtils.getTags(entity) != null)
				builder.append(PeopleHtmlUtils.getTags(entity)).append("<br/>");

			if (PeopleHtmlUtils.getPrimaryContacts(entity, isSmallList) != null)
				builder.append(
						PeopleHtmlUtils.getPrimaryContacts(entity, isSmallList))
						.append("<br/>");

			if (!isSmallList)
				builder.append(PeopleHtmlUtils.getLastUpdateSnippet(entity));
			builder.append("</span>");
			return builder.toString();
		} catch (RepositoryException re) {
			throw new PeopleException("Cannot create organizations content", re);
		}
	}
}
