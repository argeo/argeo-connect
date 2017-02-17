package org.argeo.connect.people.workbench.rap.providers;

import javax.jcr.Node;
import javax.jcr.Property;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.workbench.rap.PeopleRapConstants;
import org.argeo.connect.resources.ResourceService;
import org.argeo.connect.ui.ConnectUiUtils;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
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

	private final ResourceService tagService;

	public TagLabelProvider(ResourceService resourceService, int listType) {
		this.tagService = resourceService;
		this.listType = listType;
	}

	@Override
	public String getText(Object element) {
		Node entity = (Node) element;
		String result;
		switch (listType) {
		case PeopleRapConstants.LIST_TYPE_OVERVIEW_TITLE:
			result = getOverviewTitle(entity);
			break;
		case PeopleRapConstants.LIST_TYPE_SMALL:
			result = getOneLineLabel(entity);
			break;
		default:
			throw new PeopleException(
					"Undefined list type - Unable to provide text for group");
		}
		return ConnectUiUtils.replaceAmpersand(result);
	}

	private String getOverviewTitle(Node entity) {
		StringBuilder builder = new StringBuilder();

		builder.append("<span " + PeopleRapConstants.PEOPLE_STYLE_ENTITY_HEADER
				+ ">");

		// first line
		builder.append("<b><big> ");
		builder.append(ConnectJcrUtils.get(entity, Property.JCR_TITLE));
		builder.append("</big></b>");
		long membersNb = tagService.countMembers(entity);
		builder.append(" <i>(").append(membersNb).append(" members)</i>");

		// Description
		String desc = ConnectJcrUtils.get(entity, Property.JCR_DESCRIPTION);
		if (EclipseUiUtils.notEmpty(desc))
			builder.append("<br />").append(desc);

		builder.append("</span>");
		return builder.toString();
	}

	private String getOneLineLabel(Node entity) {
		StringBuilder builder = new StringBuilder();
		builder.append("<b>");
		builder.append(ConnectJcrUtils.get(entity, Property.JCR_TITLE));
		builder.append("</b>");
		long membersNb = tagService.countMembers(entity);
		builder.append(" <i>(").append(membersNb).append(" members)</i>");
		return builder.toString();
	}

}