package org.argeo.connect.people.web.pages;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.cms.CmsUiProvider;
import org.argeo.connect.people.PeopleService;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/** Shows all information we have about a given organization. */
public class OrganizationPage implements CmsUiProvider {

	/* DEPENDENCY INJECTION */
	// private PeopleService peopleService;

	@Override
	public Control createUi(Composite parent, Node context)
			throws RepositoryException {

		return null;
	}

	/* DEPENDENCY INJECTION */
	public void setPeopleService(PeopleService peopleService) {
		// this.peopleService = peopleService;
	}
}
