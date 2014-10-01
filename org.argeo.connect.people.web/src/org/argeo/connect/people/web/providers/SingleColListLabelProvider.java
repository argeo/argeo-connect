package org.argeo.connect.people.web.providers;

import javax.jcr.Node;

import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.web.PeopleWebUtils;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

public class SingleColListLabelProvider implements ILabelProvider {
	// extends LabelProvider implements
	// PeopleNames {

	private static final long serialVersionUID = 1L;
	private PeopleService peopleService;

	public SingleColListLabelProvider(PeopleService peopleService) {
		this.peopleService = peopleService;
	}

	// @Override

	public String getText(Object element) {
		Node person = (Node) element;
		StringBuilder builder = new StringBuilder();
		builder.append("<b>");
		builder.append(peopleService.getDisplayName(person));
		builder.append("</b>");

		// try {
		// NodeIterator ni = person.getNode(PEOPLE_JOBS).getNodes();
		// while (ni.hasNext()) {
		// Node currNode = ni.nextNode();
		// if (currNode.isNodeType(PeopleTypes.PEOPLE_JOB)) {
		// Node org = peopleService.getEntityByUid(
		// person.getSession(),
		// currNode.getProperty(PEOPLE_REF_UID).getString());
		// builder.append(" [");
		// String role = CommonsJcrUtils.get(currNode, PEOPLE_ROLE);
		// if (CommonsJcrUtils.checkNotEmptyString(role))
		// builder.append(role).append(", ");
		// builder.append(org != null ? CommonsJcrUtils.get(org,
		// Property.JCR_TITLE) : "-");
		// builder.append("]");
		// }
		// }
		//
		// } catch (RepositoryException re) {
		// // Cannot get corresponding jobs, fail silently
		// }
		String result = PeopleWebUtils.replaceAmpersand(builder.toString());
		return result;
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