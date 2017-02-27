package org.argeo.people.workbench.rap.providers;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.ui.ConnectUiConstants;
import org.argeo.connect.ui.ConnectUiUtils;
import org.argeo.connect.ui.workbench.AppWorkbenchService;
import org.argeo.connect.ui.workbench.ConnectWorkbenchUtils;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.people.PeopleException;
import org.argeo.people.PeopleNames;
import org.argeo.people.PeopleService;
import org.argeo.people.PeopleTypes;
import org.argeo.people.ui.PeopleUiSnippets;
import org.eclipse.jface.viewers.ColumnLabelProvider;

/** Label provider for organisation overview */
public class OrgOverviewLabelProvider extends ColumnLabelProvider {
	private static final long serialVersionUID = -7687462900742079263L;

	private final ResourcesService resourceService;
	private final PeopleService peopleService;
	private AppWorkbenchService appWorkbenchService;

	private boolean isSmallList;

	public OrgOverviewLabelProvider(boolean isSmallList, ResourcesService resourceService, PeopleService peopleService,
			AppWorkbenchService appWorkbenchService) {
		this.isSmallList = isSmallList;
		this.resourceService = resourceService;
		this.peopleService = peopleService;
		this.appWorkbenchService = appWorkbenchService;
	}

	@Override
	public String getText(Object element) {
		try {
			Node node = (Node) element;
			Node orga;
			if (node.isNodeType(PeopleTypes.PEOPLE_ORG))
				orga = node;
			else if (node.isNodeType(PeopleTypes.PEOPLE_JOB)) {
				orga = peopleService.getEntityByUid(node.getSession(), null,
						node.getProperty(PeopleNames.PEOPLE_REF_UID).getString());
				// TODO manage this more cleanly
				if (orga == null)
					return "Broken link. This organisation does not exist anymore. " + "It should have been deleted.";
			} else
				throw new PeopleException("Unvalid node type. " + "Cannot display org information");

			StringBuilder builder = new StringBuilder();

			if (isSmallList)
				builder.append("<span>");
			else
				builder.append("<span " + ConnectUiConstants.ENTITY_HEADER_INNER_CSS_STYLE + " >");
			builder.append("<big><b>");
			builder.append(ConnectJcrUtils.get(orga, Property.JCR_TITLE));
			builder.append("</b></big> ");

			String local = PeopleUiSnippets.getLocalisationInfo(resourceService, peopleService, orga);
			if (EclipseUiUtils.notEmpty(local))
				builder.append(local);

			builder.append("<br/>");

			String tmpStr;
			tmpStr = PeopleUiSnippets.getPrimaryContacts(orga);
			if (EclipseUiUtils.notEmpty(tmpStr)) {
				builder.append(tmpStr).append("<br/>");
			}

			if (isSmallList) {
				tmpStr = ConnectWorkbenchUtils.getTags(resourceService, appWorkbenchService, orga);
				if (EclipseUiUtils.notEmpty(tmpStr))
					builder.append(tmpStr);
			}

			builder.append("</span>");
			String result = ConnectUiUtils.replaceAmpersand(builder.toString());
			return result;
		} catch (RepositoryException re) {
			throw new PeopleException("Cannot create organizations content", re);
		}
	}
}