package org.argeo.connect.people.workbench.rap.providers;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.ui.ConnectUiConstants;
import org.argeo.connect.ui.ConnectUiUtils;
import org.argeo.connect.ui.util.TagLabelProvider;
import org.argeo.connect.ui.workbench.AppWorkbenchService;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Provide a single column label provider for entity lists. Icon and displayed
 * text vary with the element node type
 */
public class EntitySingleColumnLabelProvider extends LabelProvider implements PeopleNames {
	private static final long serialVersionUID = -2613028516709467900L;

	private AppWorkbenchService appWorkbenchService;

	private OrgListLabelProvider orgLp;
	private PersonListLabelProvider personLp;
	private GroupLabelProvider groupLp = new GroupLabelProvider(ConnectUiConstants.LIST_TYPE_SMALL);
	private TagLabelProvider mlInstanceLp;

	public EntitySingleColumnLabelProvider(ResourcesService resourceService, PeopleService peopleService,
			AppWorkbenchService appWorkbenchService) {
		this.appWorkbenchService = appWorkbenchService;
		personLp = new PersonListLabelProvider(peopleService);
		orgLp = new OrgListLabelProvider(resourceService, peopleService);
		mlInstanceLp = new TagLabelProvider(resourceService, ConnectUiConstants.LIST_TYPE_SMALL);
	}

	@Override
	public String getText(Object element) {
		try {
			Node entity = (Node) element;
			String result;
			if (entity.isNodeType(PeopleTypes.PEOPLE_PERSON))
				result = personLp.getText(element);
			else if (entity.isNodeType(PeopleTypes.PEOPLE_ORG))
				result = orgLp.getText(element);
			else if (entity.isNodeType(PeopleTypes.PEOPLE_MAILING_LIST))
				result = mlInstanceLp.getText(element);
			else if (entity.isNodeType(PeopleTypes.PEOPLE_GROUP))
				result = groupLp.getText(element);
			else
				result = "";
			return ConnectUiUtils.replaceAmpersand(result);
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to get formatted value for node", re);
		}
	}

	/** Overwrite this method to provide project specific images */
	@Override
	public Image getImage(Object element) {
		return appWorkbenchService.getIconForType((Node) element);
	}
}
