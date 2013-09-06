package org.argeo.connect.streams.ui.providers;

import javax.jcr.Node;
import javax.jcr.Property;

import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.streams.RssNames;
import org.eclipse.jface.viewers.LabelProvider;

/**
 * Provide a single column label provider for person lists
 */
public class RssListLabelProvider extends LabelProvider implements RssNames {

	private static final long serialVersionUID = 1L;

	public RssListLabelProvider() {
	}

	@Override
	public String getText(Object element) {
		Node person = (Node) element;
		String currValue = null;
		StringBuilder builder = new StringBuilder();
		builder.append("<b>");
		builder.append(CommonsJcrUtils.getStringValue(person,
				Property.JCR_TITLE));
		builder.append("</b>");
		builder.append(RssHtmlProvider.pubDateSnippet(person));
		builder.append("<br/>");

		currValue = CommonsJcrUtils.getStringValue(person,
				Property.JCR_DESCRIPTION);
		if (currValue != null) {
			builder.append(currValue);
		}
		return builder.toString();
	}
}
