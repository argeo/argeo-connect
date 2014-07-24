package org.argeo.connect.people.ui.providers;

import javax.jcr.RepositoryException;
import javax.jcr.query.Row;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.ui.PeopleUiService;
import org.eclipse.swt.graphics.Image;

/**
 * Add an icon to the results, using the node type of the node retrieved using
 * the selector name. It uses a JcrRowHtmlLabelProvider rather than a
 * SimpleJcrRowLabelProvider: TO BE VALIDATED
 */
public class TitleWithIconLP extends JcrRowHtmlLabelProvider {
	// WAS public class TitleWithIconLP extends JcrRowHtmlLabelProvider {
	private static final long serialVersionUID = 6064779874148619776L;

	private final PeopleUiService peopleUiService;
	private final String selectorName;

	public TitleWithIconLP(PeopleUiService peopleUiService,
			String selectorName, String propertyName) {
		super(selectorName, propertyName);
		this.peopleUiService = peopleUiService;
		this.selectorName = selectorName;
	}

	@Override
	public Image getImage(Object element) {
		try {
			return peopleUiService.getIconForType(((Row) element)
					.getNode(selectorName));
			// WAS return peopleUiService.getIconForType(((Row) element)
			// .getNode(PeopleTypes.PEOPLE_ENTITY));
		} catch (RepositoryException e) {
			throw new PeopleException(
					"unable to retrieve image for " + element, e);
		}
	}

}