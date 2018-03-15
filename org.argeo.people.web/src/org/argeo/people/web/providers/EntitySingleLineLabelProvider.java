package org.argeo.people.web.providers;

import javax.jcr.Node;

import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.util.ConnectUtils;
import org.argeo.people.PeopleService;
import org.argeo.people.PeopleTypes;
import org.argeo.people.web.PeopleWebConstants;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

/** Simple label provider for Org and Person Viewers */
public class EntitySingleLineLabelProvider implements ILabelProvider {

	private static final long serialVersionUID = 1L;
	private PeopleService peopleService;

	private final PersonOverviewLP personOLP;
	private final OrgOverviewLP orgOLP;

	public EntitySingleLineLabelProvider(ResourcesService resourceService, PeopleService peopleService) {
		this.peopleService = peopleService;
		this.personOLP = new PersonOverviewLP(resourceService, peopleService,
				PeopleWebConstants.OVERVIEW_TYPE_SINGLE_LINE_LIST);
		orgOLP = new OrgOverviewLP(resourceService, peopleService, PeopleWebConstants.OVERVIEW_TYPE_SINGLE_LINE_LIST);
	}

	public String getText(Object element) {
		Node entity = (Node) element;
		if (ConnectJcrUtils.isNodeType(entity, PeopleTypes.PEOPLE_PERSON))
			return personOLP.getText(entity);
		else if (ConnectJcrUtils.isNodeType(entity, PeopleTypes.PEOPLE_ORG))
			return orgOLP.getText(entity);
		else {
			StringBuilder builder = new StringBuilder();
			builder.append("<b>");
			builder.append(peopleService.getDisplayName(entity));
			builder.append("</b>");
			return ConnectUtils.replaceAmpersand(builder.toString());
		}
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
	}

	@Override
	public Image getImage(Object element) {
		return null;
	}
}
