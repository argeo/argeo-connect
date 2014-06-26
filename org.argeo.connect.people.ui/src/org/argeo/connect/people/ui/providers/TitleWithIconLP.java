package org.argeo.connect.people.ui.providers;

import javax.jcr.RepositoryException;
import javax.jcr.query.Row;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.PeopleUiService;
import org.argeo.eclipse.ui.jcr.lists.SimpleJcrRowLabelProvider;
import org.eclipse.swt.graphics.Image;

public class TitleWithIconLP extends SimpleJcrRowLabelProvider {
	private static final long serialVersionUID = 6064779874148619776L;

	private final PeopleUiService peopleUiService;

	public TitleWithIconLP(PeopleUiService peopleUiService,
			String selectorName, String propertyName) {
		super(selectorName, propertyName);
		this.peopleUiService = peopleUiService;
	}

	@Override
	public Image getImage(Object element) {
		try {
			return peopleUiService.getIconForType(((Row) element)
					.getNode(PeopleTypes.PEOPLE_ENTITY));
		} catch (RepositoryException e) {
			throw new PeopleException(
					"unable to retrieve image for " + element, e);
		}
	}

}