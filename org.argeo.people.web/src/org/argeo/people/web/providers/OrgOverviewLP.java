package org.argeo.people.web.providers;

import static org.argeo.eclipse.ui.EclipseUiUtils.notEmpty;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.connect.ConnectConstants;
import org.argeo.connect.resources.ResourcesNames;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.ui.ConnectUiConstants;
import org.argeo.connect.ui.ConnectUiUtils;
import org.argeo.people.PeopleException;
import org.argeo.people.PeopleNames;
import org.argeo.people.PeopleService;
import org.argeo.people.PeopleTypes;
import org.argeo.people.ui.PeopleUiSnippets;
import org.argeo.people.web.PeopleWebConstants;
import org.argeo.people.web.PeopleWebSnippets;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

/** Single column label provider for Organisation lists */
public class OrgOverviewLP implements ILabelProvider, PeopleNames {

	private static final long serialVersionUID = 1L;
	private final int listType;

	private PeopleService peopleService;
	private ResourcesService resourcesService;

	/**
	 * 
	 * @param resourcesService
	 * @param peopleService
	 * @param listType
	 */
	public OrgOverviewLP(ResourcesService resourcesService, PeopleService peopleService, int listType) {
		this.listType = listType;
		this.peopleService = peopleService;
		this.resourcesService = resourcesService;
	}

	@Override
	public String getText(Object element) {

		try {
			Node entity = (Node) element;
			String result;
			switch (listType) {
			case PeopleWebConstants.OVERVIEW_TYPE_HEADER:
				result = getOverviewTitle(entity);
				break;
			case PeopleWebConstants.OVERVIEW_TYPE_LARGE_LIST:
				result = getOverviewForList(entity, false);
				break;
			case PeopleWebConstants.OVERVIEW_TYPE_SINGLE_LINE_LIST:
				result = getOverviewForList(entity, true);
				break;
			default:
				throw new PeopleException("Unable to provide text for person");
			}
			return ConnectUiUtils.replaceAmpersand(result);
		} catch (RepositoryException re) {
			throw new PeopleException("Cannot create organizations content", re);
		}
	}

	private String getOverviewTitle(Node orga) throws RepositoryException {
		StringBuilder builder = new StringBuilder();
		builder.append("<span style='font-size:15px;' >");
		builder.append("<big><b>");
		builder.append(peopleService.getDisplayName(orga));
		builder.append("</b></big> ");

		String local = PeopleUiSnippets.getLocalisationInfo(resourcesService, peopleService, orga);
		if (notEmpty(local))
			builder.append(local);

		builder.append("<br/>");

		String tmpStr = PeopleUiSnippets.getPrimaryContacts(orga);
		if (notEmpty(tmpStr)) {
			builder.append(tmpStr);
		}
		builder.append("</span>");
		return builder.toString();
	}

	private String getOverviewForList(Node orga, boolean isSmallList) throws RepositoryException {

		StringBuilder builder = new StringBuilder();

		if (!isSmallList)
			builder.append("<span> <big>");
		builder.append("<b>");
		builder.append(peopleService.getDisplayName(orga));
		builder.append("</b> ");
		if (!isSmallList)
			builder.append("</big>");

		String local = PeopleUiSnippets.getLocalisationInfo(resourcesService, peopleService, orga);
		if (notEmpty(local))
			builder.append(local);

		if (isSmallList)
			builder.append(ConnectUiConstants.NB_SPACE + ConnectUiConstants.NB_SPACE);
		else
			builder.append("<br/>");

		String tmpStr = PeopleUiSnippets.getPrimaryContacts(orga);
		if (notEmpty(tmpStr))
			builder.append(tmpStr);

		String tags = PeopleWebSnippets.getTagLikeValues(resourcesService, ConnectConstants.RESOURCE_TAG, orga,
				ResourcesNames.CONNECT_TAGS, "#");
		String mailingLists = PeopleWebSnippets.getTagLikeValues(resourcesService, PeopleTypes.PEOPLE_MAILING_LIST, orga,
				PeopleNames.PEOPLE_MAILING_LISTS, "@");

		if (isSmallList) {
			builder.append(ConnectUiConstants.NB_SPACE + ConnectUiConstants.NB_SPACE).append(tags.trim());
			if (notEmpty(tags) && notEmpty(mailingLists))
				builder.append(ConnectUiConstants.NB_SPACE);
			builder.append(mailingLists.trim());
		} else {
			builder.append("<br/>").append(tags.trim());
			if (notEmpty(tags) && notEmpty(mailingLists))
				builder.append("<br/>");
			builder.append(mailingLists.trim());
			builder.append("</span>");
		}
		return builder.toString();
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
