package org.argeo.connect.people.web.providers;

import static org.argeo.eclipse.ui.EclipseUiUtils.notEmpty;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiSnippets;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.utils.JcrUiUtils;
import org.argeo.connect.people.web.PeopleWebConstants;
import org.argeo.connect.people.web.PeopleWebSnippets;
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
			return PeopleUiUtils.replaceAmpersand(result);
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
		String fmn = PeopleUiSnippets.getFullMontyName(person);
		String localisationStr = PeopleUiSnippets.getLocalisationInfo(
				peopleService, person);
		String primContactStr = PeopleUiSnippets.getPrimaryContacts(person);
		Boolean politeFormFlag = JcrUiUtils.getBooleanValue(person,
				PEOPLE_USE_POLITE_FORM);
		List<String> spokenLanguages = JcrUiUtils.getMultiAsList(person,
				PEOPLE_SPOKEN_LANGUAGES);

		if (notEmpty(fmn) || notEmpty(localisationStr)) {
			builder.append("<br/>").append(fmn);
			if (notEmpty(fmn) && notEmpty(localisationStr))
				builder.append(PeopleUiConstants.NB_SPACE
						+ PeopleUiConstants.NB_SPACE);
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
					builder.append(
							peopleService.getResourceService()
									.getEncodedTagValue(person.getSession(),
											PeopleConstants.RESOURCE_LANG, str))
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
			builder.append("</b>");
		} else {
			builder.append("<span>").append("<b><big>");
			builder.append(peopleService.getDisplayName(person));
			builder.append("</big></b>");
		}

		String local = PeopleUiSnippets.getLocalisationInfo(peopleService,
				person);
		if (notEmpty(local)) {
			builder.append(PeopleUiConstants.NB_DOUBLE_SPACE);
			builder.append(local);
		}

		if (isSmallList)
			builder.append(PeopleUiConstants.NB_DOUBLE_SPACE);
		else
			builder.append("<br/>");

		// Contacts
		String primContactStr = PeopleUiSnippets.getPrimaryContacts(person);
		if (notEmpty(primContactStr)) {
			builder.append(primContactStr.trim());
			if (isSmallList)
				builder.append(PeopleUiConstants.NB_DOUBLE_SPACE);
			else
				builder.append("<br/>");
		}

		// Tags
		String tags = PeopleWebSnippets.getTagLikeValues(peopleService,
				PeopleConstants.RESOURCE_TAG, person, PeopleNames.PEOPLE_TAGS,
				"#");
		String mailingLists = PeopleWebSnippets.getTagLikeValues(peopleService,
				PeopleTypes.PEOPLE_MAILING_LIST, person,
				PeopleNames.PEOPLE_MAILING_LISTS, "@");
		if (isSmallList) {
			builder.append(tags);
			if (notEmpty(tags) && notEmpty(mailingLists))
				builder.append(PeopleUiConstants.NB_SPACE);
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
