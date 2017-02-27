package org.argeo.connect.people.web.parts;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.connect.people.ui.PeopleUiSnippets;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * Displays a single people:contact node including buttons and formatted display
 * of the contact value
 */
public class SingleContactPart implements CmsUiProvider {
	/* DEPENDENCY INJECTION */
	private ResourcesService resourcesService;
	private ContactButtonsPart contactButtonsPart;

	@Override
	public Control createUi(Composite parent, Node context) throws RepositoryException {
		parent.setLayout(new GridLayout(2, false));
		Composite left = new Composite(parent, SWT.NO_FOCUS);
		contactButtonsPart.createUi(left, context);
		Composite right = new Composite(parent, SWT.NO_FOCUS);
		populateReadOnlyPanel(right, context);
		parent.layout();

		return parent;
	}

	protected void populateReadOnlyPanel(final Composite readOnlyPanel, Node context) {
		readOnlyPanel.setLayout(EclipseUiUtils.noSpaceGridLayout());
		Label label = new Label(readOnlyPanel, SWT.WRAP);
		label.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
		String addressHtml = PeopleUiSnippets.getContactDisplaySnippet(resourcesService, context);
		label.setText(addressHtml);
	}

	/* DEPENDENCY INJECTION */
	public void setResourcesService(ResourcesService resourcesService) {
		this.resourcesService = resourcesService;
	}

	public void setContactButtonsPart(ContactButtonsPart contactButtonsPart) {
		this.contactButtonsPart = contactButtonsPart;
	}
}
