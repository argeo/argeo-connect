package org.argeo.people.web.providers;

import static org.argeo.eclipse.ui.EclipseUiUtils.notEmpty;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.connect.ConnectConstants;
import org.argeo.connect.resources.ResourcesNames;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.ui.ConnectUiConstants;
import org.argeo.connect.ui.ConnectUiUtils;
import org.argeo.connect.util.ConnectJcrUtils;
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

/**
 * Provides HTML Snippet to display details of a person in various UI contexts
 */
public class PersonOverviewLP implements ILabelProvider, PeopleNames {

	private static final long serialVersionUID = 1L;
	private final int listType;

	private PeopleService peopleService;
	private ResourcesService resourceService;

	public PersonOverviewLP(ResourcesService resourceService, PeopleService peopleService, int listType) {
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
			return ConnectUiUtils.replaceAmpersand(result);
		} catch (RepositoryException re) {
			throw new PeopleException("Cannot create organizations content", re);
		}
	}

	private String getOverviewTitle(Node person) throws RepositoryException {
		StringBuilder builder = new StringBuilder();
		builder.append("<span style='font-size:15px;'  >");
		// first line
		builder.append("<b><big> ");
		String displayName = peopleService.getDisplayName(person);
		builder.append(displayName);
		builder.append("</big></b>");
		String fmn = PeopleUiSnippets.getLongName(person);
		String localisationStr = PeopleUiSnippets.getLocalisationInfo(resourceService, peopleService, person);
		String primContactStr = PeopleUiSnippets.getPrimaryContacts(person);
		Boolean politeFormFlag = ConnectJcrUtils.getBooleanValue(person, PEOPLE_USE_POLITE_FORM);
		List<String> spokenLanguages = ConnectJcrUtils.getMultiAsList(person, PEOPLE_SPOKEN_LANGUAGES);

		if (notEmpty(fmn) || notEmpty(localisationStr)) {
			builder.append("<br/>").append(fmn);
			if (notEmpty(fmn) && notEmpty(localisationStr))
				builder.append(ConnectUiConstants.NB_SPACE + ConnectUiConstants.NB_SPACE);
			builder.append(localisationStr);
		}
		if (notEmpty(primContactStr))
			builder.append("<br/>").append(primContactStr.trim());

		if (politeFormFlag != null || !spokenLanguages.isEmpty()) {
			builder.append("<br/>");
			if (politeFormFlag != null)
				builder.append(politeFormFlag ? "Formal" : "Informal");
			if (politeFormFlag != null && !spokenLanguages.isEmpty())
				builder.append(" / ");
			if (!spokenLanguages.isEmpty()) {
				for (String str : spokenLanguages) {
					builder.append(resourceService.getEncodedTagValue(person.getSession(),
							ConnectConstants.RESOURCE_LANG, str)).append(", ");
				}
				// remove last occurence of the separator
				builder.delete(builder.length() - 2, builder.length());
			}
		}

		builder.append("</span>");
		return builder.toString();
	}

	private String getOverviewForList(Node person, boolean isSmallList) throws RepositoryException {
		StringBuilder builder = new StringBuilder();

		if (isSmallList) {
			builder.append("<b>");
			builder.append(peopleService.getDisplayName(person));
			builder.append("</b>");
		} else {
			builder.append("<span>").append("<b><big>");
			builder.append(peopleService.getDisplayName(person));
			builder.append("</big></b>");
		}

		String local = PeopleUiSnippets.getLocalisationInfo(resourceService, peopleService, person);
		if (notEmpty(local)) {
			builder.append(ConnectUiConstants.NB_DOUBLE_SPACE);
			builder.append(local);
		}

		if (isSmallList)
			builder.append(ConnectUiConstants.NB_DOUBLE_SPACE);
		else
			builder.append("<br/>");

		// Contacts
		String primContactStr = PeopleUiSnippets.getPrimaryContacts(person);
		if (notEmpty(primContactStr)) {
			builder.append(primContactStr.trim());
			if (isSmallList)
				builder.append(ConnectUiConstants.NB_DOUBLE_SPACE);
			else
				builder.append("<br/>");
		}

		// Tags
		String tags = PeopleWebSnippets.getTagLikeValues(resourceService, ConnectConstants.RESOURCE_TAG, person,
				ResourcesNames.CONNECT_TAGS, "#");
		String mailingLists = PeopleWebSnippets.getTagLikeValues(resourceService, PeopleTypes.PEOPLE_MAILING_LIST,
				person, PeopleNames.PEOPLE_MAILING_LISTS, "@");
		if (isSmallList) {
			builder.append(tags);
			if (notEmpty(tags) && notEmpty(mailingLists))
				builder.append(ConnectUiConstants.NB_SPACE);
			builder.append(mailingLists);
		} else {
			builder.append("<br/>").append(tags);
			if (notEmpty(tags) && notEmpty(mailingLists))
				builder.append("<br/>");
			builder.append(mailingLists);
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
