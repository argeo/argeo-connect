package org.argeo.connect.people.ui.providers;

import org.argeo.connect.people.ui.utils.PeopleHtmlUtils;
import org.argeo.eclipse.ui.jcr.lists.SimpleJcrRowLabelProvider;

/**
 * Wraps the getText() method of the SimpleJcrRowLabelProvider to remove invalid
 * characters, typically the ampersand from the returned String
 */
public class JcrRowHtmlLabelProvider extends SimpleJcrRowLabelProvider {
	private static final long serialVersionUID = 2134911527741337612L;

	public JcrRowHtmlLabelProvider(String selectorName, String propertyName) {
		super(selectorName, propertyName);
	}

	@Override
	public String getText(Object element) {
		return PeopleHtmlUtils.cleanHtmlString(super.getText(element));
	}
}
