package org.argeo.connect.people.rap.providers;

import javax.jcr.Node;
import javax.jcr.Property;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.TagService;
import org.argeo.connect.people.rap.PeopleUiConstants;
import org.argeo.connect.people.rap.utils.PeopleHtmlUtils;
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
	private final TagService tagService;

	public TagLabelProvider(TagService tagService, int listType,
			String tagableParentPath, String tagableType, String tagPropName) {
		this.tagService = tagService;
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

		builder.append("<span " + PeopleUiConstants.PEOPLE_STYLE_ENTITY_HEADER
				+ " >");

		// first line
		builder.append("<b><big> ");
		builder.append(CommonsJcrUtils.get(entity, Property.JCR_TITLE));
		builder.append("</big></b>");
		long membersNb = tagService.countMembers(entity, tagableParentPath,
				tagableType, tagPropName);
		builder.append(" <i>(").append(membersNb).append(" members)</i>");

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
		long membersNb = tagService.countMembers(entity, tagableParentPath,
				tagableType, tagPropName);
		builder.append(" <i>(").append(membersNb).append(" members)</i>");
		return builder.toString();
	}

}