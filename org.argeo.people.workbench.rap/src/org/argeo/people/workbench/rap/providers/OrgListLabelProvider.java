package org.argeo.people.workbench.rap.providers;

import javax.jcr.Node;
import javax.jcr.Property;

import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.ui.ConnectUiUtils;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.people.PeopleNames;
import org.argeo.people.PeopleService;
import org.argeo.people.ui.PeopleUiSnippets;
import org.eclipse.jface.viewers.LabelProvider;

/** Label provider for organisations single column lists */
public class OrgListLabelProvider extends LabelProvider implements PeopleNames {
	private static final long serialVersionUID = -579265588183299317L;

	private final ResourcesService resourcesService;
	private final PeopleService peopleService;

	/**
	 * 
	 * @param resourcesService
	 * @param peopleService
	 */
	public OrgListLabelProvider(ResourcesService resourcesService, PeopleService peopleService) {
		this.resourcesService = resourcesService;
		this.peopleService = peopleService;
	}

	@Override
	public String getText(Object element) {
		Node orga = (Node) element;
		StringBuilder builder = new StringBuilder();
		builder.append("<b>");
		builder.append(ConnectJcrUtils.get(orga, Property.JCR_TITLE));
		builder.append("</b> ");

		String local = PeopleUiSnippets.getLocalisationInfo(resourcesService, peopleService, orga);
		if (EclipseUiUtils.notEmpty(local))
			builder.append(local);
		String result = ConnectUiUtils.replaceAmpersand(builder.toString());
		return result;
	}
}
