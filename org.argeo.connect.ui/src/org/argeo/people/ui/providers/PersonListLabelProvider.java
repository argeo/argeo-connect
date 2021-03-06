package org.argeo.people.ui.providers;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.util.ConnectUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.people.PeopleNames;
import org.argeo.people.PeopleService;
import org.argeo.people.PeopleTypes;
import org.eclipse.jface.viewers.LabelProvider;

/** Provide a single column label provider for person lists */
public class PersonListLabelProvider extends LabelProvider implements PeopleNames {

	private static final long serialVersionUID = 1L;
	private PeopleService peopleService;

	public PersonListLabelProvider(PeopleService peopleService) {
		this.peopleService = peopleService;
	}

	@Override
	public String getText(Object element) {
		Node person = (Node) element;
		StringBuilder builder = new StringBuilder();
		builder.append("<b>");
		builder.append(peopleService.getDisplayName(person));
		builder.append("</b>");
		try {
			NodeIterator ni = person.getNode(PEOPLE_JOBS).getNodes();
			while (ni.hasNext()) {
				Node currNode = ni.nextNode();
				if (currNode.isNodeType(PeopleTypes.PEOPLE_JOB)) {
					Node org = peopleService.getEntityByUid(person.getSession(), null,
							currNode.getProperty(PEOPLE_REF_UID).getString());
					builder.append(" [");
					String role = ConnectJcrUtils.get(currNode, PEOPLE_ROLE);
					if (EclipseUiUtils.notEmpty(role))
						builder.append(role).append(", ");
					builder.append(org != null ? ConnectJcrUtils.get(org, Property.JCR_TITLE) : "-");
					builder.append("]");
				}
			}
		} catch (RepositoryException re) {
			// Cannot get corresponding jobs, fail silently
		}
		String result = ConnectUtils.replaceAmpersand(builder.toString());
		return result;
	}
}
