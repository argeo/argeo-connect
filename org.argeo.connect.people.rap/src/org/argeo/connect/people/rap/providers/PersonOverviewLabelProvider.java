package org.argeo.connect.people.rap.providers;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.rap.PeopleUiConstants;
import org.argeo.connect.people.rap.utils.PeopleHtmlUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.ResourcesJcrUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;

/**
 * Provides HTML Snippet to display details of a person in various editors and
 * lists
 */
public class PersonOverviewLabelProvider extends ColumnLabelProvider implements
		PeopleNames {

	private static final long serialVersionUID = 1L;

	private final int listType;

	// private boolean isSmallList = true;
	private PeopleService peopleService;

	public PersonOverviewLabelProvider(int listType, PeopleService peopleService) {
		this.listType = listType;
		this.peopleService = peopleService;
	}

	@Override
	public String getText(Object element) {
		try {
			Node node = (Node) element;
			Node entity;
			if (node.isNodeType(PeopleTypes.PEOPLE_PERSON))
				entity = node;
			else if (node.isNodeType(PeopleTypes.PEOPLE_JOB))
				entity = node.getParent().getParent();
			else if (node.isNodeType(PeopleTypes.PEOPLE_POSITION)) {
				entity = peopleService.getEntityByUid(node.getSession(), node
						.getProperty(PEOPLE_REF_UID).getString());
			} else
				throw new PeopleException("Unvalid node type. "
						+ "Cannot display film information");
			String result;
			switch (listType) {
			case PeopleUiConstants.LIST_TYPE_OVERVIEW_TITLE:
				result = getOverviewTitle(entity);
				break;
//			case PeopleUiConstants.LIST_TYPE_OVERVIEW_DETAIL:
//				result = getOverviewDetails(entity);
//				break;
			case PeopleUiConstants.LIST_TYPE_SMALL:
				result = getOverviewForList(entity, true);
				break;
			case PeopleUiConstants.LIST_TYPE_MEDIUM:
				result = getOverviewForList(entity, false);
				break;
			default:
				throw new PeopleException("Unable to provide text for person");
			}
			return PeopleHtmlUtils.cleanHtmlString(result);
		} catch (RepositoryException re) {
			throw new PeopleException("Cannot create organizations content", re);
		}
	}

	private String getOverviewTitle(Node person) throws RepositoryException {
		StringBuilder builder = new StringBuilder();
		builder.append("<span " + PeopleUiConstants.PEOPLE_STYLE_ENTITY_HEADER
				+ " >");
		// first line
		builder.append("<b><big> ");
		String displayName = peopleService.getDisplayName(person);
		builder.append(displayName);
		builder.append("</big></b>");
		String fmn = PeopleHtmlUtils.getFullMontyName(person);
		String local = PeopleHtmlUtils.getLocalisationInfo(peopleService,
				person);
		String primaryContacts = PeopleHtmlUtils.getPrimaryContacts(person);
		Boolean politeFormFlag = CommonsJcrUtils.getBooleanValue(person,
				PEOPLE_USE_POLITE_FORM);
		List<String> spokenLanguages = CommonsJcrUtils.getMultiAsList(person,
				PEOPLE_SPOKEN_LANGUAGES);

		if (CommonsJcrUtils.checkNotEmptyString(fmn)
				|| CommonsJcrUtils.checkNotEmptyString(local)) {
			builder.append("<br/>").append(fmn);
			if (CommonsJcrUtils.checkNotEmptyString(fmn)
					&& CommonsJcrUtils.checkNotEmptyString(local))
				builder.append("&#160;&#160; ");
			builder.append(local);
		}
		if (CommonsJcrUtils.checkNotEmptyString(primaryContacts))
			builder.append("<br/>").append(primaryContacts);

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

	//
	// private String getOverviewDetails(Node person) throws RepositoryException
	// {
	// StringBuilder builder = new StringBuilder();
	// builder.append("<span style='font-size:15px;'>");
	//
	// String primaryContacts = PeopleHtmlUtils.getPrimaryContacts(person);
	// if (CommonsJcrUtils.checkNotEmptyString(primaryContacts))
	// builder.append(primaryContacts).append("<br/>");
	// builder.append(PeopleHtmlUtils.getLastUpdateSnippet(person));
	//
	// builder.append("</span>");
	// return builder.toString();
	// }

	private String getOverviewForList(Node person, boolean isSmallList)
			throws RepositoryException {
		StringBuilder builder = new StringBuilder().append("<span>");
		// first line
		builder.append("<b><big> ");
		builder.append(peopleService.getDisplayName(person));
		builder.append("</big> </b> ");
		String local = PeopleHtmlUtils.getLocalisationInfo(peopleService,
				person);
		if (CommonsJcrUtils.checkNotEmptyString(local))
			builder.append(local);
		builder.append("<br/>");

		// Contacts
		String primContactsStr = PeopleHtmlUtils.getPrimaryContacts(person);
		if (CommonsJcrUtils.checkNotEmptyString(primContactsStr))
			builder.append(primContactsStr).append("<br/>");

		// Tags
		if (PeopleHtmlUtils.getTags(person) != null)
			builder.append(PeopleHtmlUtils.getTags(person)).append("<br/>");

		builder.append("</span>");

		return builder.toString();
	}

}