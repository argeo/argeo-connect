package org.argeo.connect.people.web.parts;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.connect.people.PeopleService;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/** Part that display a list of activities for a people:contactable node */
public class ActivitiesPart implements CmsUiProvider {

	private PeopleService peopleService;

	public ActivitiesPart() {
	}

	public ActivitiesPart(PeopleService peopleService) {
		this.peopleService = peopleService;
	}

	@Override
	public Control createUi(Composite parent, Node context) throws RepositoryException {
		// createContactPanel(parent, context);
		return parent;
	}

	// protected void createContactPanel(Composite parent, Node context)
	// throws RepositoryException {
	// parent.setLayout(new GridLayout());
	//
	// if (context.hasNode(PeopleNames.PEOPLE_CONTACTS)) {
	// NodeIterator nit = context.getNode(PeopleNames.PEOPLE_CONTACTS)
	// .getNodes();
	// while (nit.hasNext()) {
	// Composite contactCmp = new Composite(parent, SWT.NO_FOCUS);
	// contactCmp.setLayoutData(PeopleWebUtils.horizontalFillData());
	// singleContactPart.createUi(contactCmp, nit.nextNode());
	// }
	// }
	//
	// }

}