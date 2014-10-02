package org.argeo.connect.people.web.providers;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
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

	private final static int OVERVIEW_HEADER = 1;
	private final static int OVERVIEW_LARGE_LIST = 2;
	private final static int OVERVIEW_SINGLE_LINE_LIST = 3;

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
			case OVERVIEW_HEADER:
				result = getOverviewTitle(entity);
				break;
			case OVERVIEW_LARGE_LIST:
				result = getOverviewForList(entity, false);
				break;
			case OVERVIEW_SINGLE_LINE_LIST:
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
		// String fmn = PeopleHtmlUtils.getFullMontyName(person);
		// String local = PeopleHtmlUtils.getLocalisationInfo(peopleService,
		// person);
		// String primaryContacts = PeopleHtmlUtils.getPrimaryContacts(person,
		// false);
		// Boolean politeFormFlag = CommonsJcrUtils.getBooleanValue(person,
		// PEOPLE_USE_POLITE_FORM);
		// List<String> spokenLanguages = CommonsJcrUtils.getMultiAsList(person,
		// PEOPLE_SPOKEN_LANGUAGES);
		//
		// if (CommonsJcrUtils.checkNotEmptyString(fmn)
		// || CommonsJcrUtils.checkNotEmptyString(local)) {
		// builder.append("<br/>").append(fmn);
		// if (CommonsJcrUtils.checkNotEmptyString(fmn)
		// && CommonsJcrUtils.checkNotEmptyString(local))
		// builder.append("&#160;&#160; ");
		// builder.append(local);
		// }
		// if (CommonsJcrUtils.checkNotEmptyString(primaryContacts))
		// builder.append("<br/>").append(primaryContacts);
		//
		// if (politeFormFlag != null || !spokenLanguages.isEmpty()) {
		// builder.append("<br/>");
		// if (politeFormFlag != null)
		// builder.append(politeFormFlag ? "Formal" : "Informal");
		// if (politeFormFlag != null && !spokenLanguages.isEmpty())
		// builder.append(" / ");
		// if (!spokenLanguages.isEmpty()) {
		// for (String str : spokenLanguages) {
		// builder.append(
		// ResourcesJcrUtils.getLangEnLabelFromIso(
		// peopleService, person.getSession(), str))
		// .append(", ");
		// }
		// // remove last occurence of the separator
		// builder.delete(builder.length() - 2, builder.length());
		// }
		// }

		builder.append("</span>");
		return builder.toString();
	}

	private String getOverviewForList(Node person, boolean isSmallList)
			throws RepositoryException {
		StringBuilder builder = new StringBuilder().append("<span>");
		// first line
		builder.append("<b><big> ");
		builder.append(peopleService.getDisplayName(person));
		builder.append("</big> </b> ");
		// String local = PeopleHtmlUtils.getLocalisationInfo(peopleService,
		// person);
		// if (CommonsJcrUtils.checkNotEmptyString(local))
		// builder.append(local);
		// builder.append("<br/>");
		//
		// // Contacts
		// if (PeopleHtmlUtils.getPrimaryContacts(person, isSmallList) != null)
		// builder.append(
		// PeopleHtmlUtils.getPrimaryContacts(person, isSmallList))
		// .append("<br/>");
		//
		// // Tags
		// if (PeopleHtmlUtils.getTags(person) != null)
		// builder.append(PeopleHtmlUtils.getTags(person)).append("<br/>");

		builder.append("</span>");

		return builder.toString();
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public Image getImage(Object element) {
		// TODO Auto-generated method stub
		return null;
	}
}
