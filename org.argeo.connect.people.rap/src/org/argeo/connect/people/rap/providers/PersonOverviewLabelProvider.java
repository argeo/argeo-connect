package org.argeo.connect.people.rap.providers;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.rap.PeopleRapConstants;
import org.argeo.connect.people.rap.PeopleRapSnippets;
import org.argeo.connect.people.rap.PeopleWorkbenchService;
import org.argeo.connect.people.ui.PeopleUiSnippets;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.util.JcrUiUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
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
	private PeopleWorkbenchService peopleWorkbenchService;

	public PersonOverviewLabelProvider(int listType,
			PeopleService peopleService,
			PeopleWorkbenchService peopleWorkbenchService) {
		this.listType = listType;
		this.peopleService = peopleService;
		this.peopleWorkbenchService = peopleWorkbenchService;
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
			case PeopleRapConstants.LIST_TYPE_OVERVIEW_TITLE:
				result = getOverviewTitle(entity);
				break;
			// case PeopleUiConstants.LIST_TYPE_OVERVIEW_DETAIL:
			// result = getOverviewDetails(entity);
			// break;
			case PeopleRapConstants.LIST_TYPE_SMALL:
				result = getOverviewForList(entity, true);
				break;
			case PeopleRapConstants.LIST_TYPE_MEDIUM:
				result = getOverviewForList(entity, false);
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
		builder.append("<span " + PeopleRapConstants.PEOPLE_STYLE_ENTITY_HEADER
				+ " >");
		// first line
		builder.append("<b><big> ");
		String displayName = peopleService.getDisplayName(person);
		builder.append(displayName);
		builder.append("</big></b>");
		String fmn = PeopleUiSnippets.getFullMontyName(person);
		String local = PeopleUiSnippets.getLocalisationInfo(peopleService,
				person);
		String primaryContacts = PeopleUiSnippets.getPrimaryContacts(person);
		Boolean politeFormFlag = JcrUiUtils.getBooleanValue(person,
				PEOPLE_USE_POLITE_FORM);
		List<String> spokenLanguages = JcrUiUtils.getMultiAsList(person,
				PEOPLE_SPOKEN_LANGUAGES);

		if (EclipseUiUtils.notEmpty(fmn) || EclipseUiUtils.notEmpty(local)) {
			builder.append("<br/>").append(fmn);
			if (EclipseUiUtils.notEmpty(fmn) && EclipseUiUtils.notEmpty(local))
				builder.append("&#160;&#160; ");
			builder.append(local);
		}
		if (EclipseUiUtils.notEmpty(primaryContacts))
			builder.append("<br/>").append(primaryContacts);

		if (politeFormFlag != null || !spokenLanguages.isEmpty()) {
			builder.append("<br/>");
			if (politeFormFlag != null)
				builder.append(politeFormFlag ? "Formal" : "Informal");
			if (politeFormFlag != null && !spokenLanguages.isEmpty())
				builder.append(" / ");
			if (!spokenLanguages.isEmpty()) {
				for (String str : spokenLanguages) {
					String language = peopleService.getResourceService()
							.getEncodedTagValue(JcrUiUtils.getSession(person),
									PeopleConstants.RESOURCE_LANG, str);
					builder.append(language).append(", ");
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
	// if (JcrUiUtils.checkNotEmptyString(primaryContacts))
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
		String local = PeopleUiSnippets.getLocalisationInfo(peopleService,
				person);
		if (EclipseUiUtils.notEmpty(local))
			builder.append(local);
		builder.append("<br/>");

		// Contacts
		String primContactsStr = PeopleUiSnippets.getPrimaryContacts(person);
		if (EclipseUiUtils.notEmpty(primContactsStr))
			builder.append(primContactsStr).append("<br/>");

		// Tags
		String clickableTags = PeopleRapSnippets.getTags(peopleService,
				peopleWorkbenchService, person);
		if (EclipseUiUtils.notEmpty(clickableTags))
			builder.append(clickableTags).append("<br/>");

		builder.append("</span>");

		return builder.toString();
	}

}
