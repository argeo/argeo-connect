package org.argeo.connect.people.ui.providers;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.connect.film.FilmTypes;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleTypes;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Provide a single column label provider for entity lists display varies with
 * node type
 */
public class EntitySingleColumnLabelProvider extends LabelProvider implements
		PeopleNames {

	private static final long serialVersionUID = 1L;

	private OrgListLabelProvider orgLp = new OrgListLabelProvider();
	private PersonListLabelProvider personLp = new PersonListLabelProvider();
	private FilmListLabelProvider filmLp = new FilmListLabelProvider();

	public EntitySingleColumnLabelProvider() {
	}

	@Override
	public String getText(Object element) {
		try {
			Node entity = (Node) element;
			if (entity.isNodeType(PeopleTypes.PEOPLE_PERSON))
				return personLp.getText(element);
			else if (entity.isNodeType(PeopleTypes.PEOPLE_ORGANIZATION))
				return orgLp.getText(element);
			else if (entity.isNodeType(FilmTypes.FILM))
				return filmLp.getText(element);
			else
				return "";
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to get formatted value for node",
					re);
		}
	}
	
	/** Overwrite this method to provide project specific images */
	@Override 
	public Image getImage(Object element) {
		return null;
	}
	
}
