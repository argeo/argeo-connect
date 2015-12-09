package org.argeo.connect.people.rap.providers;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.rap.PeopleRapConstants;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.utils.JcrUiUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;

/**
 * Provide a label provider for group members
 */
public class GroupLabelProvider extends ColumnLabelProvider implements
		PeopleNames {
	private final static Log log = LogFactory.getLog(GroupLabelProvider.class);

	private static final long serialVersionUID = 9156065705311297011L;
	private final int listType;

	public GroupLabelProvider(int listType) {
		this.listType = listType;
		// this.peopleService = peopleService;
	}

	@Override
	public String getText(Object element) {
		Node entity = (Node) element;
		String result;
		switch (listType) {
		case PeopleRapConstants.LIST_TYPE_OVERVIEW_TITLE:
			result = getOverviewTitle(entity);
			break;
		// case PeopleUiConstants.LIST_TYPE_OVERVIEW_DETAIL:
		// result = getOverviewDetails(entity);
		// break;
		case PeopleRapConstants.LIST_TYPE_SMALL:
			result = getOneLineLabel(entity);
			break;
		// case PeopleUiConstants.LIST_TYPE_MEDIUM:
		// result = getOverviewForList(entity, false);
		// break;
		default:
			throw new PeopleException(
					"Undefined list type - Unable to provide text for group");
		}
		return PeopleUiUtils.replaceAmpersand(result);
	}

	private String getOverviewTitle(Node entity) {
		try {
			StringBuilder builder = new StringBuilder();
			builder.append("<span style='font-size:15px;'>");
			// first line
			builder.append("<b><big> ");
			builder.append(JcrUiUtils.get(entity, Property.JCR_TITLE));
			builder.append("</big></b>");

			if (entity.hasNode(PEOPLE_MEMBERS)) { // Nb of members

				long start = System.currentTimeMillis();
				int membersNb = -1; // countMembers(entity.getNode(PEOPLE_MEMBERS),
									// 0);
				long end = System.currentTimeMillis();

				if (log.isDebugEnabled())
					log.debug("Counted " + membersNb + " members in "
							+ (end - start) + " ms");

				builder.append("<i>(").append(membersNb)
						.append(" members)</i>");
			}
			// Description
			String desc = JcrUiUtils.get(entity, Property.JCR_DESCRIPTION);
			if (EclipseUiUtils.notEmpty(desc))
				builder.append("<br />").append(desc);
			builder.append("</span>");
			return builder.toString();
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to get text for link", e);
		}

	}

	/** recursivly count members in the sub tree */
	// private int countMembers(Node node, int currCount) {
	// try {
	// NodeIterator ni = node.getNodes();
	// while (ni.hasNext()) {
	// currCount = countMembers(ni.nextNode(), currCount);
	// }
	// if (JcrUiUtils.isNodeType(node,
	// PeopleTypes.PEOPLE_MAILING_LIST_ITEM)) {
	// currCount++;
	// }
	// return currCount;
	// } catch (RepositoryException re) {
	// throw new PeopleException("Unable to count members for node "
	// + node, re);
	// }
	// }

	private String getOneLineLabel(Node entity) {
		// try {
		StringBuilder builder = new StringBuilder();
		builder.append("<b>");
		builder.append(JcrUiUtils.get(entity, Property.JCR_TITLE));
		builder.append("</b>");

		// Nb of members
		// if (entity.hasNode(PEOPLE_MEMBERS)) {
		// int membersNb = countMembers(entity.getNode(PEOPLE_MEMBERS), 0);
		// builder.append("<i>(").append(membersNb)
		// .append(" members)</i>");
		// }
		return builder.toString();
		// } catch (RepositoryException e) {
		// throw new PeopleException("Unable to get small text for link", e);
		// }

	}

}