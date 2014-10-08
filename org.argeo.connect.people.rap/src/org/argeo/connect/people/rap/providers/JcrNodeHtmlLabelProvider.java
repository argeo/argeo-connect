package org.argeo.connect.people.ui.providers;

import org.argeo.connect.people.ui.utils.PeopleHtmlUtils;
import org.argeo.eclipse.ui.jcr.lists.SimpleJcrNodeLabelProvider;

/**
 * Wraps the getText() method of the SimpleJcrNodeLabelProvider to remove
 * invalid characters, typically the ampersand from the returned String
 */
public class JcrNodeHtmlLabelProvider extends SimpleJcrNodeLabelProvider {
	private static final long serialVersionUID = 2134911527741337612L;

	public JcrNodeHtmlLabelProvider(String propertyName) {
		super(propertyName);
	}

	@Override
	public String getText(Object element) {
		return PeopleHtmlUtils.cleanHtmlString(super.getText(element));
	}
}
