package org.argeo.connect.people.web.pages;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.cms.CmsUiProvider;
import org.argeo.connect.people.ui.PeopleWebUtils;
import org.argeo.connect.people.web.parts.ActivitiesPart;
import org.argeo.connect.people.web.parts.ContactsWithNotePart;
import org.argeo.connect.people.web.parts.PersonHeaderPart;
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
	private PersonHeaderPart personHeaderPart;
	private ContactsWithNotePart contactsWithNotePart;
	private ActivitiesPart activitiesPart;

	@Override
	public Control createUi(Composite parent, Node context)
			throws RepositoryException {
		
		// TODO use a scrollable composite
		Composite body = new Composite(parent, SWT.NO_FOCUS);
		body.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		body.setLayout(PeopleWebUtils.noSpaceGridLayout());

		// header
		Composite headerCmp = new Composite(body, SWT.NO_FOCUS);
		headerCmp.setLayoutData(PeopleWebUtils.horizontalFillData());
		personHeaderPart.createUi(headerCmp, context);

		// contacts
		Composite contactCmp = new Composite(body, SWT.NO_FOCUS);
		contactCmp.setLayoutData(PeopleWebUtils.horizontalFillData());
		contactsWithNotePart.createUi(contactCmp, context);

		// activities
		Composite activitiesCmp = new Composite(body, SWT.NO_FOCUS);
		activitiesCmp.setLayoutData(PeopleWebUtils.horizontalFillData());
		activitiesPart.createUi(activitiesCmp, context);

		parent.layout();
		return body;
	}

	/* DEPENDENCY INJECTION */
	public void setPersonHeaderPart(PersonHeaderPart personHeaderPart) {
		this.personHeaderPart = personHeaderPart;
	}

	public void setContactsWithNotePart(
			ContactsWithNotePart contactsWithNotePart) {
		this.contactsWithNotePart = contactsWithNotePart;
	}

	public void setActivitiesPart(ActivitiesPart activitiesPart) {
		this.activitiesPart = activitiesPart;
	}
}
