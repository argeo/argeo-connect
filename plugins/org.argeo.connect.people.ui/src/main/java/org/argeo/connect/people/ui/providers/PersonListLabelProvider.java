package org.argeo.connect.people.ui.providers;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.PeopleHtmlUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.eclipse.jface.viewers.LabelProvider;

/**
 * Provide a single column label provider for person lists
 */
public class PersonListLabelProvider extends LabelProvider implements
		PeopleNames {

	private static final long serialVersionUID = 1L;

	public PersonListLabelProvider() {
	}

	@Override
	public String getText(Object element) {
		Node person = (Node) element;
		String currValue = null;
		StringBuilder builder = new StringBuilder();
		builder.append("<b>");
		builder.append(CommonsJcrUtils.getStringValue(person, PEOPLE_LAST_NAME));

		currValue = CommonsJcrUtils.getStringValue(person, PEOPLE_FIRST_NAME);

		if (currValue != null) {
			builder.append(", ");
			builder.append(currValue);
		}
		builder.append("</b>");

		try {
			NodeIterator ni = person.getNode(PEOPLE_JOBS).getNodes();
			while (ni.hasNext()) {
				Node currNode = ni.nextNode();
				if (currNode.isNodeType(PeopleTypes.PEOPLE_JOB)) {
					// TODO check if existing
					Node org = person.getSession().getNodeByIdentifier(
							currNode.getProperty(PEOPLE_REF_UID).getString());
					builder.append(" [");
					builder.append(CommonsJcrUtils.getStringValue(currNode,
							PEOPLE_ROLE));
					builder.append(", ");
					builder.append(CommonsJcrUtils.getStringValue(org,
							PEOPLE_LEGAL_NAME));
					builder.append("]");
				}
			}

		} catch (RepositoryException re) {
			// Cannot get the org, fail silently
		}
		String result = PeopleHtmlUtils.cleanHtmlString(builder.toString());
		return result;
	}
}
