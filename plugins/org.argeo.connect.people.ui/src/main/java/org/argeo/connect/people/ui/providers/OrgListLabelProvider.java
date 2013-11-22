package org.argeo.connect.people.ui.providers;

import javax.jcr.Node;

import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.ui.utils.PeopleHtmlUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.PeopleJcrUtils;
import org.eclipse.jface.viewers.LabelProvider;

/**
 * Provide a single column label provider for organisations lists
 */
public class OrgListLabelProvider extends LabelProvider implements PeopleNames {

	private static final long serialVersionUID = -7643487102948380168L;

	public OrgListLabelProvider() {
	}

	@Override
	public String getText(Object element) {
		Node orga = (Node) element;
		StringBuilder builder = new StringBuilder();
		builder.append("<b>");
		builder.append(CommonsJcrUtils.getStringValue(orga, PEOPLE_LEGAL_NAME));
		builder.append("</b>");

		if (PeopleJcrUtils.getTownFromItem(orga) != null) {
			builder.append(" [");
			String town = PeopleJcrUtils.getTownFromItem(orga);
			String country = PeopleJcrUtils.getCountryFromItem(orga);
			if (!CommonsJcrUtils.isEmptyString(town)) {
				builder.append(town);
				if (!CommonsJcrUtils.isEmptyString(country))
					builder.append(", ");
			}
			if (!CommonsJcrUtils.isEmptyString(country))
				builder.append(country);
			builder.append("]");
		}
		String result = PeopleHtmlUtils.cleanHtmlString(builder.toString());
		return result;
	}
}