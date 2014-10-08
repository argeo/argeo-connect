package org.argeo.connect.people.rap.providers;

import javax.jcr.Node;

import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.rap.PeopleRapSnippets;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.eclipse.jface.viewers.LabelProvider;

/**
 * Provide a single column label provider for organisations lists
 */
public class OrgListLabelProvider extends LabelProvider implements PeopleNames {

	private static final long serialVersionUID = -7643487102948380168L;
	private final PeopleService peopleService;

	public OrgListLabelProvider(PeopleService peopleService) {
		this.peopleService = peopleService;
	}

	@Override
	public String getText(Object element) {
		Node orga = (Node) element;
		StringBuilder builder = new StringBuilder();
		builder.append("<b>");
		builder.append(CommonsJcrUtils.get(orga, PEOPLE_LEGAL_NAME));
		builder.append("</b> ");

		String local = PeopleRapSnippets.getLocalisationInfo(peopleService, orga);
		if (CommonsJcrUtils.checkNotEmptyString(local))
			builder.append(local);

		String result = PeopleRapSnippets.cleanHtmlString(builder.toString());
		return result;
	}
}