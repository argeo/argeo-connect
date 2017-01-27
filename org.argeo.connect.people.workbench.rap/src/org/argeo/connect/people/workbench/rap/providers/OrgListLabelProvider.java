package org.argeo.connect.people.workbench.rap.providers;

import javax.jcr.Node;

import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.ui.PeopleUiSnippets;
import org.argeo.connect.ui.ConnectUiUtils;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
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
		builder.append(ConnectJcrUtils.get(orga, PEOPLE_LEGAL_NAME));
		builder.append("</b> ");

		String local = PeopleUiSnippets.getLocalisationInfo(peopleService,
				orga);
		if (EclipseUiUtils.notEmpty(local))
			builder.append(local);

		String result = ConnectUiUtils.replaceAmpersand(builder.toString());
		return result;
	}
}