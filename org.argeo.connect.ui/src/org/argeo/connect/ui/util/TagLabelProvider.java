package org.argeo.connect.ui.util;

import javax.jcr.Node;
import javax.jcr.Property;

import org.argeo.connect.ConnectException;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.ui.ConnectUiConstants;
import org.argeo.connect.ui.ConnectUiUtils;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;

/**
 * Provide a label provider for group members
 */
public class TagLabelProvider extends ColumnLabelProvider {
	// private final static Log log = LogFactory
	// .getLog(TagLabelProvider.class);

	private static final long serialVersionUID = 9156065705311297011L;
	private final int listType;

	private final ResourcesService resourceService;

	public TagLabelProvider(ResourcesService resourceService, int listType) {
		this.resourceService = resourceService;
		this.listType = listType;
	}

	@Override
	public String getText(Object element) {
		Node entity = (Node) element;
		String result;
		switch (listType) {
		case ConnectUiConstants.LIST_TYPE_OVERVIEW_TITLE:
			result = getOverviewTitle(entity);
			break;
		case ConnectUiConstants.LIST_TYPE_SMALL:
			result = getOneLineLabel(entity);
			break;
		default:
			throw new ConnectException("Undefined list type - Unable to provide text for group");
		}
		return ConnectUiUtils.replaceAmpersand(result);
	}

	private String getOverviewTitle(Node entity) {
		StringBuilder builder = new StringBuilder();

		builder.append("<span " + ConnectUiConstants.ENTITY_HEADER_INNER_CSS_STYLE + ">");

		// first line
		builder.append("<b><big> ");
		builder.append(ConnectJcrUtils.get(entity, Property.JCR_TITLE));
		builder.append("</big></b>");
		long membersNb = resourceService.countMembers(entity);
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
		long membersNb = resourceService.countMembers(entity);
		builder.append(" <i>(").append(membersNb).append(" members)</i>");
		return builder.toString();
	}
}
