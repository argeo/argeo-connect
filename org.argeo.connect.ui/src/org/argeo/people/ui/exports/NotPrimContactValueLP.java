package org.argeo.people.ui.exports;

import static org.argeo.eclipse.ui.EclipseUiUtils.notEmpty;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.people.PeopleException;
import org.argeo.people.PeopleNames;
import org.argeo.people.util.PeopleJcrUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;

/**
 * Enable simple retrieval of all contact contact value that are not the primary
 * ones for a given type (Typically, all mails that are not the primary mail).
 * Use contact node type as property name
 */
public class NotPrimContactValueLP extends ColumnLabelProvider {
	private static final long serialVersionUID = 2085668424125329226L;

	private String selectorName;
	private String propertyName;

	public NotPrimContactValueLP(String selectorName, String propertyName) {
		if (notEmpty(selectorName))
			this.selectorName = selectorName;
		this.propertyName = propertyName;
	}

	@Override
	public String getText(Object element) {
		try {
			Node currNode = ConnectJcrUtils.getNodeFromElement(element, selectorName);

			List<Node> nodes = PeopleJcrUtils.getContactOfType(currNode, propertyName);

			StringBuilder builder = new StringBuilder();
			loop: for (Node currContact : nodes) {

				if (currContact.hasProperty(PeopleNames.PEOPLE_IS_PRIMARY)
						&& currContact.getProperty(PeopleNames.PEOPLE_IS_PRIMARY).getBoolean())
					continue loop;

				builder.append(currContact.getProperty(PeopleNames.PEOPLE_CONTACT_VALUE).getString());
				builder.append(", ");
			}

			String result = builder.toString();
			if (result.lastIndexOf(", ") > 0)
				result = result.substring(0, result.length() - 2);

			return result;
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to get text from row " + element, re);
		}
	}
}
