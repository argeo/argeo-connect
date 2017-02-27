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
import org.argeo.people.web.parts.OrgHeaderPart;
import org.argeo.people.web.parts.SingleContactPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Shows all information we have about a given organisation. Expects a context
 * that has the people:org NodeType
 */
public class OrgPage implements CmsUiProvider {

	/* We can override defaults parts with dependency injection */
	private OrgHeaderPart orgHeaderPart;
	private ContactsWithNotePart contactsWithNotePart;
	private ActivitiesPart activitiesPart;

	/** Inject various subparts */
	public OrgPage() {
	}

	public OrgPage(ResourcesService resourcesService, PeopleService peopleService) {
		orgHeaderPart = new OrgHeaderPart(resourcesService, peopleService);

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
		orgHeaderPart.createUi(headerCmp, context);

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
	public void setOrgHeaderPart(OrgHeaderPart orgHeaderPart) {
		this.orgHeaderPart = orgHeaderPart;
	}

	public void setContactsWithNotePart(ContactsWithNotePart contactsWithNotePart) {
		this.contactsWithNotePart = contactsWithNotePart;
	}

	public void setActivitiesPart(ActivitiesPart activitiesPart) {
		this.activitiesPart = activitiesPart;
	}
}
