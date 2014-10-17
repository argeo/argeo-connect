package org.argeo.connect.people.web.pages;

import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.cms.CmsUiProvider;
import org.argeo.connect.people.PeopleException;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/** Calls the correct {@link CmsUiProvider} depending on the context NodeType **/
public class PeopleDynamicPages implements CmsUiProvider {

	/* DEPENDENCY INJECTION */
	private Map<String, CmsUiProvider> dynamicPages;

	// Relevant pages
	@Override
	public Control createUi(Composite parent, Node context)
			throws RepositoryException {

		for (String key : dynamicPages.keySet()) {
			if (context.isNodeType(key))
				return dynamicPages.get(key).createUi(parent, context);
		}
		
		// FIXME : implement a specific NodeType for Tags
		// if (context.getPath().lastIndexOf("people:resources/people:tags") !=
		// -1)
		// return dynamicPages.get(PeopleTypes.PEOPLE_TAG).createUi(parent,
		// context);
		
		throw new PeopleException("No dynamic pages defined for " + context);
	}

	/* DEPENDENCY INJECTION */
	public void setDynamicPages(Map<String, CmsUiProvider> dynamicPages) {
		this.dynamicPages = dynamicPages;
	}
}