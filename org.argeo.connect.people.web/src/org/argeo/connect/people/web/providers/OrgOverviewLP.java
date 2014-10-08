package org.argeo.connect.people.web.providers;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiSnippets;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.web.PeopleWebConstants;
import org.argeo.connect.people.web.PeopleWebSnippets;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

/**
 * Provide a single column label provider for person lists
 */
public class OrgOverviewLP implements ILabelProvider, PeopleNames {

	private static final long serialVersionUID = 1L;
	private final int listType;

	private PeopleService peopleService;

	public OrgOverviewLP(int listType, PeopleService peopleService) {
		this.listType = listType;
		this.peopleService = peopleService;
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
			return PeopleUiUtils.replaceAmpersand(result);
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

		String local = PeopleUiSnippets
				.getLocalisationInfo(peopleService, orga);
		if (CommonsJcrUtils.checkNotEmptyString(local))
			builder.append(local);

		builder.append("<br/>");

		String tmpStr = PeopleUiSnippets.getPrimaryContacts(orga);
		if (CommonsJcrUtils.checkNotEmptyString(tmpStr)) {
			builder.append(tmpStr);
		}
		builder.append("</span>");
		return builder.toString();
	}

	private String getOverviewForList(Node orga, boolean isSmallList)
			throws RepositoryException {

		StringBuilder builder = new StringBuilder();

		if (!isSmallList)
			builder.append("<span> <big>");
		builder.append("<b>");
		builder.append(peopleService.getDisplayName(orga));
		builder.append("</b> ");
		if (!isSmallList)
			builder.append("</big>");

		String local = PeopleUiSnippets
				.getLocalisationInfo(peopleService, orga);
		if (CommonsJcrUtils.checkNotEmptyString(local))
			builder.append(local);

		if (isSmallList)
			builder.append(PeopleUiConstants.NB_SPACE
					+ PeopleUiConstants.NB_SPACE);
		else
			builder.append("<br/>");

		String tmpStr = PeopleUiSnippets.getPrimaryContacts(orga);
		if (CommonsJcrUtils.checkNotEmptyString(tmpStr)) {
			builder.append(tmpStr);
		}

		String tags = PeopleWebSnippets.getTagLikeValues(peopleService, orga,
				PeopleNames.PEOPLE_TAGS, "#");
		String mailingLists = PeopleWebSnippets.getTagLikeValues(peopleService,
				orga, PeopleNames.PEOPLE_MAILING_LISTS, "@");

		if (isSmallList) {
			builder.append(
					PeopleUiConstants.NB_SPACE + PeopleUiConstants.NB_SPACE)
					.append(tags.trim());
			if (CommonsJcrUtils.checkNotEmptyString(tags)
					&& CommonsJcrUtils.checkNotEmptyString(mailingLists))
				builder.append(PeopleUiConstants.NB_SPACE);
			builder.append(mailingLists.trim());
		} else {
			builder.append("<br/>").append(tags.trim());
			if (CommonsJcrUtils.checkNotEmptyString(tags)
					&& CommonsJcrUtils.checkNotEmptyString(mailingLists))
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