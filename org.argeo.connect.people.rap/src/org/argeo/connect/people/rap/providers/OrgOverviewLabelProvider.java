package org.argeo.connect.people.rap.providers;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.rap.PeopleRapConstants;
import org.argeo.connect.people.rap.PeopleRapSnippets;
import org.argeo.connect.people.ui.PeopleUiSnippets;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;

/**
 * Provide a single column label provider for person lists
 */
public class OrgOverviewLabelProvider extends ColumnLabelProvider {
	private static final long serialVersionUID = 1L;

	// private final static Log log = LogFactory
	// .getLog(OrgOverviewLabelProvider.class);

	private boolean isSmallList;
	private PeopleService peopleService;

	public OrgOverviewLabelProvider(boolean isSmallList,
			PeopleService peopleService) {
		this.isSmallList = isSmallList;
		this.peopleService = peopleService;
	}

	@Override
	public String getText(Object element) {

		try {
			Node node = (Node) element;
			Node orga;
			if (node.isNodeType(PeopleTypes.PEOPLE_ORG))
				orga = node;
			else if (node.isNodeType(PeopleTypes.PEOPLE_JOB)) {
				orga = peopleService.getEntityByUid(node.getSession(), node
						.getProperty(PeopleNames.PEOPLE_REF_UID).getString());
			} else
				throw new PeopleException("Unvalid node type. "
						+ "Cannot display org information");

			StringBuilder builder = new StringBuilder();

			if (isSmallList)
				builder.append("<span>");
			else
				builder.append("<span "
						+ PeopleRapConstants.PEOPLE_STYLE_ENTITY_HEADER + " >");
			builder.append("<big><b>");
			builder.append(CommonsJcrUtils.get(orga, Property.JCR_TITLE));
			builder.append("</b></big> ");

			String local = PeopleRapSnippets.getLocalisationInfo(peopleService,
					orga);
			if (CommonsJcrUtils.checkNotEmptyString(local))
				builder.append(local);

			builder.append("<br/>");

			String tmpStr;
			if (isSmallList) {
				tmpStr = PeopleRapSnippets.getTags(orga);
				if (CommonsJcrUtils.checkNotEmptyString(tmpStr))
					builder.append(tmpStr).append("<br/>");
			}

			tmpStr = PeopleUiSnippets.getPrimaryContacts(orga);
			if (CommonsJcrUtils.checkNotEmptyString(tmpStr)) {
				builder.append(tmpStr);
			}
			builder.append("</span>");
			String result = PeopleUiUtils.replaceAmpersand(builder.toString());
			return result;
		} catch (RepositoryException re) {
			throw new PeopleException("Cannot create organizations content", re);
		}
	}
}