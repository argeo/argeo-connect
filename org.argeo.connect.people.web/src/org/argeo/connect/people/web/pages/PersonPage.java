package org.argeo.connect.people.web.pages;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.cms.CmsUiProvider;
import org.argeo.connect.people.web.PeopleWebUtils;
import org.argeo.connect.people.web.parts.PersonHeaderUiProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Shows all information we have about a given person. Expects a context that
 * has the people:person NodeType
 */
public class PersonPage implements CmsUiProvider {

	/* DEPENDENCY INJECTION */
	private PersonHeaderUiProvider personHeaderUP;

	@Override
	public Control createUi(Composite parent, Node context)
			throws RepositoryException {

		Composite body = new Composite(parent, SWT.NO_FOCUS);
		body.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		body.setLayout(PeopleWebUtils.noSpaceGridLayout());

		// header
		Composite headerCmp = new Composite(body, SWT.NO_FOCUS);
		headerCmp.setLayoutData(PeopleWebUtils.horizontalFillData());
		headerCmp.setLayout(PeopleWebUtils.noSpaceGridLayout());
		personHeaderUP.createUi(headerCmp, context);

		// contacts
		Composite contactCmp = new Composite(body, SWT.NO_FOCUS);
		contactCmp.setLayoutData(PeopleWebUtils.horizontalFillData());
		createContactPanel(contactCmp, context);

		// activities
		Composite activityCmp = new Composite(body, SWT.NO_FOCUS);
		activityCmp.setLayoutData(PeopleWebUtils.horizontalFillData());
		createActivityPanel(activityCmp, context);

		parent.layout();
		return body;
	}

	private void createContactPanel(Composite parent, Node context) {
	}

	private void createActivityPanel(Composite parent, Node context) {
	}

	/* DEPENDENCY INJECTION */
	public void setPersonHeaderUP(PersonHeaderUiProvider personHeaderUP) {
		this.personHeaderUP = personHeaderUP;
	}

}
