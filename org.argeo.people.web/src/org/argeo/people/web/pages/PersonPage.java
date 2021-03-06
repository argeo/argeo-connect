package org.argeo.people.web.pages;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.people.PeopleService;
import org.argeo.people.web.parts.ActivitiesPart;
import org.argeo.people.web.parts.ContactButtonsPart;
import org.argeo.people.web.parts.ContactsWithNotePart;
import org.argeo.people.web.parts.PersonHeaderPart;
import org.argeo.people.web.parts.SingleContactPart;
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

	public PersonPage() {
	}

	public PersonPage(PeopleService peopleService, ResourcesService resourcesService) {
		personHeaderPart = new PersonHeaderPart(peopleService);

		ContactButtonsPart cbp = new ContactButtonsPart();
		SingleContactPart scp = new SingleContactPart();
		scp.setResourcesService(resourcesService);
		scp.setContactButtonsPart(cbp);
		contactsWithNotePart = new ContactsWithNotePart(scp);

		activitiesPart = new ActivitiesPart(peopleService);
	}

	@Override
	public Control createUi(Composite parent, Node context) throws RepositoryException {

		// TODO use a scrollable composite
		Composite body = new Composite(parent, SWT.NO_FOCUS);
		body.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		body.setLayout(EclipseUiUtils.noSpaceGridLayout());

		// header
		Composite headerCmp = new Composite(body, SWT.NO_FOCUS);
		headerCmp.setLayoutData(EclipseUiUtils.fillWidth());
		personHeaderPart.createUi(headerCmp, context);

		// contacts
		Composite contactCmp = new Composite(body, SWT.NO_FOCUS);
		contactCmp.setLayoutData(EclipseUiUtils.fillWidth());
		contactsWithNotePart.createUi(contactCmp, context);

		// activities
		Composite activitiesCmp = new Composite(body, SWT.NO_FOCUS);
		activitiesCmp.setLayoutData(EclipseUiUtils.fillWidth());
		activitiesPart.createUi(activitiesCmp, context);

		parent.layout();
		return body;
	}

	/* DEPENDENCY INJECTION */
	public void setPersonHeaderPart(PersonHeaderPart personHeaderPart) {
		this.personHeaderPart = personHeaderPart;
	}

	public void setContactsWithNotePart(ContactsWithNotePart contactsWithNotePart) {
		this.contactsWithNotePart = contactsWithNotePart;
	}

	public void setActivitiesPart(ActivitiesPart activitiesPart) {
		this.activitiesPart = activitiesPart;
	}
}
