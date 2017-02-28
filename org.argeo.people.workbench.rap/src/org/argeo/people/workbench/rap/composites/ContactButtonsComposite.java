package org.argeo.people.workbench.rap.composites;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.ui.ConnectUiStyles;
import org.argeo.connect.ui.ConnectUiUtils;
import org.argeo.connect.workbench.AppWorkbenchService;
import org.argeo.connect.workbench.parts.AbstractConnectEditor;
import org.argeo.people.PeopleException;
import org.argeo.people.PeopleNames;
import org.argeo.people.PeopleService;
import org.argeo.people.util.PeopleJcrUtils;
import org.argeo.people.workbench.rap.PeopleRapImages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.AbstractFormPart;

/** Centralize management of contacts buttons */
public class ContactButtonsComposite extends Composite {
	private static final long serialVersionUID = 2331713954300845292L;

	// Context
	private final ResourcesService resourceService;
	private final PeopleService peopleService;
	private final AppWorkbenchService appWorkbenchService;
	private final Node contactNode;
	private final boolean isEditing;
	private final Node parentVersionableNode;
	private final AbstractFormPart formPart;

	// private final PeopleImageProvider imageProvider = new
	// PeopleImageProvider();

	public ContactButtonsComposite(AbstractConnectEditor editor, AbstractFormPart formPart, Composite parent, int style,
			Node contactNode, Node parentVersionableNode, ResourcesService resourceService, PeopleService peopleService,
			AppWorkbenchService appWorkbenchService) {
		super(parent, style);
		this.resourceService = resourceService;
		this.peopleService = peopleService;
		this.appWorkbenchService = appWorkbenchService;
		this.contactNode = contactNode;
		this.parentVersionableNode = parentVersionableNode;
		this.formPart = formPart;
		this.isEditing = editor.isEditing();
		populate();
	}

	private void populate() {
		// Initialization
		Composite buttCmp = this;
		GridLayout gl = ConnectUiUtils.noSpaceGridLayout(3);
		buttCmp.setLayout(gl);

		// final Button categoryBtn =
		createCategoryButton(buttCmp);

		// Primary management
		Button primaryBtn = createPrimaryButton(buttCmp);
		configurePrimaryButton(primaryBtn);

		// Deletion
		if (isEditing) {
			Button deleteBtn = createDeleteButton(buttCmp);
			configureDeleteButton(deleteBtn);
		}
	}

	private Button createCategoryButton(Composite parent) {
		Button btn = new Button(parent, SWT.FLAT);
		CmsUtils.style(btn, ConnectUiStyles.FLAT_BTN);

		btn.setImage(appWorkbenchService.getIconForType(contactNode));
		GridData gd = new GridData();
		gd.widthHint = 16;
		gd.heightHint = 16;
		btn.setLayoutData(gd);
		return btn;
	}

	private Button createDeleteButton(Composite parent) {
		Button btn = new Button(parent, SWT.FLAT | SWT.BOTTOM);
		CmsUtils.style(btn, ConnectUiStyles.FLAT_BTN);
		btn.setImage(PeopleRapImages.DELETE_BTN);
		GridData gd = new GridData();
		gd.widthHint = 16;
		gd.heightHint = 16;
		btn.setLayoutData(gd);
		return btn;
	}

	private Button createPrimaryButton(Composite parent) {
		try {
			Button btn = new Button(parent, SWT.FLAT);
			CmsUtils.style(btn, ConnectUiStyles.FLAT_BTN);

			// update image
			boolean isPrimary = (contactNode.hasProperty(PeopleNames.PEOPLE_IS_PRIMARY)
					&& contactNode.getProperty(PeopleNames.PEOPLE_IS_PRIMARY).getBoolean());
			if (isPrimary)
				btn.setImage(PeopleRapImages.PRIMARY_BTN);
			else
				btn.setImage(PeopleRapImages.PRIMARY_NOT_BTN);
			btn.setEnabled(isEditing);
			// primaryBtn.setGrayed(false);

			GridData gd = new GridData();
			gd.widthHint = 16;
			gd.heightHint = 16;
			btn.setLayoutData(gd);
			return btn;
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to create primary button for node " + contactNode, re);
		}
	}

	private void configureDeleteButton(final Button btn) {
		btn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				try {
					// update primary cache
					if (PeopleJcrUtils.isPrimary(parentVersionableNode, contactNode))
						PeopleJcrUtils.updatePrimaryCache(resourceService, peopleService, parentVersionableNode,
								contactNode, false);

					contactNode.remove();
					formPart.markDirty();
					formPart.refresh();
				} catch (RepositoryException e) {
					throw new PeopleException("unable to initialise deletion", e);
				}
			}
		});
	}

	private void configurePrimaryButton(final Button btn) {
		btn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				boolean hasChanged = PeopleJcrUtils.markAsPrimary(resourceService, peopleService, parentVersionableNode,
						contactNode);

				if (hasChanged) {
					formPart.markDirty();
					formPart.refresh();
				}
			}
		});
	}

	@Override
	public boolean setFocus() {
		return true;
	}
}
