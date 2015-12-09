package org.argeo.connect.people.web.providers;

import javax.jcr.Node;

import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.utils.JcrUiUtils;
import org.argeo.connect.people.web.PeopleWebConstants;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

public class EntitySingleLineLabelProvider implements ILabelProvider {

	private static final long serialVersionUID = 1L;
	private PeopleService peopleService;

	private final PersonOverviewLP personOLP;
	private final OrgOverviewLP orgOLP;

	public EntitySingleLineLabelProvider(PeopleService peopleService) {
		this.peopleService = peopleService;
		this.personOLP = new PersonOverviewLP(
				PeopleWebConstants.OVERVIEW_TYPE_SINGLE_LINE_LIST,
				peopleService);
		orgOLP = new OrgOverviewLP(
				PeopleWebConstants.OVERVIEW_TYPE_SINGLE_LINE_LIST,
				peopleService);
	}

	// @Override

	public String getText(Object element) {

		Node entity = (Node) element;
		if (JcrUiUtils.isNodeType(entity, PeopleTypes.PEOPLE_PERSON))
			return personOLP.getText(entity);
		else if (JcrUiUtils.isNodeType(entity, PeopleTypes.PEOPLE_ORG))
			return orgOLP.getText(entity);
		else {
			StringBuilder builder = new StringBuilder();
			builder.append("<b>");
			builder.append(peopleService.getDisplayName(entity));
			builder.append("</b>");
			return PeopleUiUtils.replaceAmpersand(builder.toString());
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