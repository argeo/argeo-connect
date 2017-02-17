package org.argeo.connect.people.web.pages;

import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.cms.ui.LifeCycleUiProvider;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.resources.ResourcesService;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Calls the correct {@link CmsUiProvider} depending on the context NodeType
 **/
public class PeopleDynamicPages implements LifeCycleUiProvider {

	/* DEPENDENCY INJECTION */
	private Map<String, CmsUiProvider> dynamicPages;
	private PeopleService peopleService;
	private ResourcesService resourcesService;

	private Map<String, String> iconPathes;

	private CmsUiProvider queryPage;

	// Relevant pages
	@Override
	public Control createUi(Composite parent, Node context) throws RepositoryException {

		for (String key : dynamicPages.keySet()) {
			if (context.isNodeType(key))
				return dynamicPages.get(key).createUi(parent, context);
		}

		if (context.isNodeType(NodeType.NT_QUERY))
			return queryPage.createUi(parent, context);

		if (context.getPath().equals("/"))
			return null;

		// FIXME : implement a specific NodeType for Tags
		// if (context.getPath().lastIndexOf("people:resources/people:tags") !=
		// -1)
		// return dynamicPages.get(PeopleTypes.PEOPLE_TAG).createUi(parent,
		// context);

		throw new PeopleException("No dynamic pages defined for " + context);
	}

	@Override
	public void init(Session adminSession) throws RepositoryException {
		queryPage = new PeopleQueryPage(peopleService, resourcesService, iconPathes);
	}

	@Override
	public void destroy() {
	}

	/* DEPENDENCY INJECTION */
	public void setDynamicPages(Map<String, CmsUiProvider> dynamicPages) {
		this.dynamicPages = dynamicPages;
	}

	public void setPeopleService(PeopleService peopleService) {
		this.peopleService = peopleService;
	}

	public void setResourcesService(ResourcesService resourcesService) {
		this.resourcesService = resourcesService;
	}

	public void setIconPathes(Map<String, String> iconPathes) {
		this.iconPathes = iconPathes;
	}
}
