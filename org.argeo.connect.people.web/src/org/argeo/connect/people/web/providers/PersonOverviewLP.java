package org.argeo.connect.people.web.providers;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.ResourcesJcrUtils;
import org.argeo.connect.people.web.PeopleLabelsUtils;
import org.argeo.connect.people.web.PeopleWebConstants;
import org.argeo.connect.people.web.PeopleWebUtils;
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

	public PersonOverviewLP(int listType, PeopleService peopleService) {
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
			return PeopleWebUtils.replaceAmpersand(result);
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
		String fmn = PeopleLabelsUtils.getFullMontyName(person);
		String localisationStr = PeopleLabelsUtils.getLocalisationInfo(peopleService,
				person);
		String primContactStr = PeopleLabelsUtils.getPrimaryContacts(person);
		Boolean politeFormFlag = CommonsJcrUtils.getBooleanValue(person,
				PEOPLE_USE_POLITE_FORM);
		List<String> spokenLanguages = CommonsJcrUtils.getMultiAsList(person,
				PEOPLE_SPOKEN_LANGUAGES);

		if (CommonsJcrUtils.checkNotEmptyString(fmn)
				|| CommonsJcrUtils.checkNotEmptyString(localisationStr)) {
			builder.append("<br/>").append(fmn);
			if (CommonsJcrUtils.checkNotEmptyString(fmn)
					&& CommonsJcrUtils.checkNotEmptyString(localisationStr))
				builder.append(PeopleWebUtils.NB_SPACE
						+ PeopleWebUtils.NB_SPACE);
			builder.append(localisationStr);
		}
		if (CommonsJcrUtils.checkNotEmptyString(primContactStr))
			builder.append("<br/>").append(primContactStr);

		if (politeFormFlag != null || !spokenLanguages.isEmpty()) {
			builder.append("<br/>");
			if (politeFormFlag != null)
				builder.append(politeFormFlag ? "Formal" : "Informal");
			if (politeFormFlag != null && !spokenLanguages.isEmpty())
				builder.append(" / ");
			if (!spokenLanguages.isEmpty()) {
				for (String str : spokenLanguages) {
					builder.append(
							ResourcesJcrUtils.getLangEnLabelFromIso(
									peopleService, person.getSession(), str))
							.append(", ");
				}
				// remove last occurence of the separator
				builder.delete(builder.length() - 2, builder.length());
			}
		}

		builder.append("</span>");
		return builder.toString();
	}

	private String getOverviewForList(Node person, boolean isSmallList)
			throws RepositoryException {
		StringBuilder builder = new StringBuilder();

		if (isSmallList) {
			builder.append("<b>");
			builder.append(peopleService.getDisplayName(person));
			builder.append("</b> ");
		} else {
			builder.append("<span>").append("<b><big> ");
			builder.append(peopleService.getDisplayName(person));
			builder.append("</big></b> ");
		}

		String local = PeopleLabelsUtils.getLocalisationInfo(peopleService,
				person);
		if (CommonsJcrUtils.checkNotEmptyString(local))
			builder.append(local);
		if (!isSmallList)
			builder.append("<br/>");

		// Contacts
		String primContactStr = PeopleLabelsUtils.getPrimaryContacts(person);
		if (CommonsJcrUtils.checkNotEmptyString(primContactStr))
			builder.append(primContactStr);
		if (!isSmallList)
			builder.append("<br/>");

		// Tags
		String tags = PeopleLabelsUtils.getTagLikeValues(person,
				PeopleNames.PEOPLE_TAGS, "#");
		String mailingLists = PeopleLabelsUtils.getTagLikeValues(person,
				PeopleNames.PEOPLE_MAILING_LISTS, "@");
		if (isSmallList) {
			builder.append(tags.trim());
			if (CommonsJcrUtils.checkNotEmptyString(tags)
					&& CommonsJcrUtils.checkNotEmptyString(mailingLists))
				builder.append(PeopleWebUtils.NB_SPACE);
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
