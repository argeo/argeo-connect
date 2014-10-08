package org.argeo.connect.people.web.parts;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.argeo.cms.CmsUiProvider;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.ui.PeopleWebUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/** Part that display a list of contacts for a people:contactable node */
public class ContactsPart implements CmsUiProvider {
	/* dependency injection */
	private SingleContactPart singleContactPart;

	@Override
	public Control createUi(Composite parent, Node context)
			throws RepositoryException {
		createContactPanel(parent, context);
		return parent;
	}

	protected void createContactPanel(Composite parent, Node context)
			throws RepositoryException {
		parent.setLayout(PeopleWebUtils.noSpaceGridLayout());

		if (context.hasNode(PeopleNames.PEOPLE_CONTACTS)) {
			NodeIterator nit = context.getNode(PeopleNames.PEOPLE_CONTACTS)
					.getNodes();
			while (nit.hasNext()) {
				Composite contactCmp = new Composite(parent, SWT.NO_FOCUS);
				contactCmp.setLayoutData(PeopleWebUtils.horizontalFillData());
				singleContactPart.createUi(contactCmp, nit.nextNode());
			}
		}

	}

	/* DEPENDENCY INJECTION */
	public void setSingleContactPart(SingleContactPart singleButtonPart) {
		this.singleContactPart = singleButtonPart;
	}
}