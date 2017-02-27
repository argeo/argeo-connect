package org.argeo.people.workbench.rap.providers;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.connect.ConnectConstants;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.ui.ConnectUiConstants;
import org.argeo.connect.ui.ConnectUiUtils;
import org.argeo.connect.ui.workbench.AppWorkbenchService;
import org.argeo.connect.ui.workbench.ConnectWorkbenchUtils;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.people.PeopleException;
import org.argeo.people.PeopleNames;
import org.argeo.people.PeopleService;
import org.argeo.people.PeopleTypes;
import org.argeo.people.ui.PeopleUiSnippets;
import org.eclipse.jface.viewers.ColumnLabelProvider;

/** Create HTML Snippet to display a person's details in editors and lists */
public class PersonOverviewLabelProvider extends ColumnLabelProvider implements PeopleNames {

	private static final long serialVersionUID = 1L;

	private final ResourcesService resourceService;
	private final PeopleService peopleService;
	private final AppWorkbenchService appWorkbenchService;
	private final int listType;

	public PersonOverviewLabelProvider(int listType, ResourcesService resourceService, PeopleService peopleService,
			AppWorkbenchService appWorkbenchService) {
		this.listType = listType;
		this.resourceService = resourceService;
		this.peopleService = peopleService;
		this.appWorkbenchService = appWorkbenchService;
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
				entity = peopleService.getEntityByUid(node.getSession(), null,
						node.getProperty(PEOPLE_REF_UID).getString());
			} else
				throw new PeopleException("Unvalid node type. " + "Cannot display film information");
			String result;
			switch (listType) {
			case ConnectUiConstants.LIST_TYPE_OVERVIEW_TITLE:
				result = getOverviewTitle(entity);
				break;
			case ConnectUiConstants.LIST_TYPE_SMALL:
				result = getOverviewForList(entity, true);
				break;
			case ConnectUiConstants.LIST_TYPE_MEDIUM:
				result = getOverviewForList(entity, false);
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
		builder.append("<span " + ConnectUiConstants.ENTITY_HEADER_INNER_CSS_STYLE + " >");
		// first line
		builder.append("<b><big> ");
		String displayName = peopleService.getDisplayName(person);
		builder.append(displayName);
		builder.append("</big></b>");
		String fmn = PeopleUiSnippets.getLongName(person);
		String local = PeopleUiSnippets.getLocalisationInfo(resourceService, peopleService, person);
		String primaryContacts = PeopleUiSnippets.getPrimaryContacts(person);
		Boolean politeFormFlag = ConnectJcrUtils.getBooleanValue(person, PEOPLE_USE_POLITE_FORM);
		List<String> spokenLanguages = ConnectJcrUtils.getMultiAsList(person, PEOPLE_SPOKEN_LANGUAGES);

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
					String language = resourceService.getEncodedTagValue(ConnectJcrUtils.getSession(person),
							ConnectConstants.RESOURCE_LANG, str);
					builder.append(language).append(", ");
				}
				// remove last occurrence of the separator
				builder.delete(builder.length() - 2, builder.length());
			}
		}
		builder.append("</span>");
		return builder.toString();
	}

	private String getOverviewForList(Node person, boolean isSmallList) throws RepositoryException {
		StringBuilder builder = new StringBuilder().append("<span>");
		// first line
		builder.append("<b><big> ");
		builder.append(peopleService.getDisplayName(person));
		builder.append("</big> </b> ");
		String local = PeopleUiSnippets.getLocalisationInfo(resourceService, peopleService, person);
		if (EclipseUiUtils.notEmpty(local))
			builder.append(local);
		builder.append("<br/>");

		// Contacts
		String primContactsStr = PeopleUiSnippets.getPrimaryContacts(person);
		if (EclipseUiUtils.notEmpty(primContactsStr))
			builder.append(primContactsStr).append("<br/>");

		// Tags
		String clickableTags = ConnectWorkbenchUtils.getTags(resourceService, appWorkbenchService, person);
		if (EclipseUiUtils.notEmpty(clickableTags))
			builder.append(clickableTags).append("<br/>");

		builder.append("</span>");
		return builder.toString();
	}
}