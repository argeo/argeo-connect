package org.argeo.connect.streams.ui.providers;

import org.argeo.connect.streams.RssNames;
import org.eclipse.jface.viewers.LabelProvider;

/**
 * Provide a single column label provider for person lists
 */
public class RssListLabelProvider extends LabelProvider implements RssNames {

	private static final long serialVersionUID = 1L;

	public RssListLabelProvider() {
	}

	// @Override
	// public String getText(Object element) {
	// Node person = (Node) element;
	// String currValue = null;
	// StringBuilder builder = new StringBuilder();
	// builder.append("<b>");
	// builder.append(CommonsJcrUtils.getStringValue(person, PEOPLE_LAST_NAME));
	// currValue = CommonsJcrUtils.getStringValue(person, PEOPLE_FIRST_NAME);
	//
	// if (currValue != null) {
	// builder.append(", ");
	// builder.append(currValue);
	// }
	// builder.append("</b>");
	//
	// try {
	// NodeIterator ni = person.getNode(PEOPLE_JOBS).getNodes();
	// while (ni.hasNext()) {
	// Node currNode = ni.nextNode();
	// if (currNode.isNodeType(PeopleTypes.PEOPLE_JOB)) {
	// // TODO check if existing
	// Node org = person.getSession().getNodeByIdentifier(
	// currNode.getProperty(PEOPLE_REF_UID).getString());
	// builder.append(" [");
	// builder.append(CommonsJcrUtils.getStringValue(currNode,
	// PEOPLE_ROLE));
	// builder.append(", ");
	// builder.append(CommonsJcrUtils.getStringValue(org,
	// PEOPLE_LEGAL_NAME));
	// builder.append("]");
	// }
	// }
	//
	// } catch (RepositoryException re) {
	// // Cannot get the org, fail silently
	// }
	// return builder.toString();
	// }
}
