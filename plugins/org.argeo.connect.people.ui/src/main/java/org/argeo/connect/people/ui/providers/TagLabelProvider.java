package org.argeo.connect.people.ui.providers;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.utils.PeopleHtmlUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;

/**
 * Provide a label provider for group members
 */
public class TagLabelProvider extends ColumnLabelProvider implements
		PeopleNames {
	// private final static Log log = LogFactory
	// .getLog(TagLabelProvider.class);

	private static final long serialVersionUID = 9156065705311297011L;
	private final int listType;

	private final String tagableParentPath;
	private final String tagableType;
	private final String tagPropName;

	public TagLabelProvider(int listType, String tagableParentPath,
			String tagableType, String tagPropName) {
		this.listType = listType;
		this.tagableParentPath = tagableParentPath;
		this.tagableType = tagableType;
		this.tagPropName = tagPropName;
	}

	@Override
	public String getText(Object element) {
		Node entity = (Node) element;
		String result;
		switch (listType) {
		case PeopleUiConstants.LIST_TYPE_OVERVIEW_TITLE:
			result = getOverviewTitle(entity);
			break;
		case PeopleUiConstants.LIST_TYPE_SMALL:
			result = getOneLineLabel(entity);
			break;
		default:
			throw new PeopleException(
					"Undefined list type - Unable to provide text for group");
		}
		return PeopleHtmlUtils.cleanHtmlString(result);
	}

	private String getOverviewTitle(Node entity) {
		StringBuilder builder = new StringBuilder();

		builder.append("<span "
				+ PeopleUiConstants.PEOPLE_CSS_EDITOR_HEADER_ROSTYLE + " >");

		// first line
		builder.append("<b><big> ");
		builder.append(CommonsJcrUtils.get(entity, Property.JCR_TITLE));
		builder.append("</big></b>");
		long membersNb = countMembers(entity);
		builder.append("<i>(").append(membersNb).append(" members)</i>");

		// Description
		String desc = CommonsJcrUtils.get(entity, Property.JCR_DESCRIPTION);
		if (CommonsJcrUtils.checkNotEmptyString(desc))
			builder.append("<br />").append(desc);

		builder.append("</span>");
		return builder.toString();
	}

	private String getOneLineLabel(Node entity) {
		StringBuilder builder = new StringBuilder();
		builder.append("<b>");
		builder.append(CommonsJcrUtils.get(entity, Property.JCR_TITLE));
		builder.append("</b>");
		long membersNb = countMembers(entity);
		builder.append("<i>(").append(membersNb).append(" members)</i>");
		return builder.toString();
	}

	/** Count members that have such a tag in the tagable sub tree */
	private long countMembers(Node tag) {
		Query query;
		NodeIterator nit;
		try {
			Session session = tag.getSession();
			String tagValue = CommonsJcrUtils.get(tag, Property.JCR_TITLE);
			// Retrieve existing tags
			if (session.nodeExists(tagableParentPath)) {
				String queryString = "select * from [" + tagableType
						+ "] as tagged where ISDESCENDANTNODE('"
						+ tagableParentPath + "') AND tagged.[" + tagPropName
						+ "]='" + tagValue + "'";
				query = session.getWorkspace().getQueryManager()
						.createQuery(queryString, Query.JCR_SQL2);
				nit = query.execute().getNodes();
				return nit.getSize();
			}
			return 0;
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to count members for " + tag, re);
		}
	}

}