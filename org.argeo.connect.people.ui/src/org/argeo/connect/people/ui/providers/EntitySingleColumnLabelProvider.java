package org.argeo.connect.people.ui.providers;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiService;
import org.argeo.connect.people.ui.utils.PeopleHtmlUtils;
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
	private PersonListLabelProvider personLp;
	private GroupLabelProvider groupLp = new GroupLabelProvider(
			PeopleUiConstants.LIST_TYPE_SMALL);
	private TagLabelProvider mlInstanceLp;

	public EntitySingleColumnLabelProvider(PeopleService peopleService,
			PeopleUiService peopleUiService) {
		personLp = new PersonListLabelProvider(peopleService);
		mlInstanceLp = new TagLabelProvider(PeopleUiConstants.LIST_TYPE_SMALL,
				peopleService.getBasePath(null), PeopleTypes.PEOPLE_ENTITY,
				PEOPLE_ML_INSTANCES);
	}

	@Override
	public String getText(Object element) {
		try {
			Node entity = (Node) element;
			String result;
			if (entity.isNodeType(PeopleTypes.PEOPLE_PERSON))
				result = personLp.getText(element);
			else if (entity.isNodeType(PeopleTypes.PEOPLE_ORGANIZATION))
				result = orgLp.getText(element);
			else if (entity.isNodeType(PeopleTypes.PEOPLE_ML_INSTANCE))
				result = mlInstanceLp.getText(element);
			else if (entity.isNodeType(PeopleTypes.PEOPLE_GROUP))
				result = groupLp.getText(element);
			else
				result = "";
			return PeopleHtmlUtils.cleanHtmlString(result);
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