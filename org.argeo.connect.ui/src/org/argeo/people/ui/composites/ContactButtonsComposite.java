package org.argeo.people.ui.composites;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.cms.ui.eclipse.forms.AbstractFormPart;
import org.argeo.cms.ui.util.CmsUiUtils;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.ui.ConnectEditor;
import org.argeo.connect.ui.ConnectImages;
import org.argeo.connect.ui.ConnectUiStyles;
import org.argeo.connect.ui.ConnectUiUtils;
import org.argeo.connect.ui.SystemWorkbenchService;
import org.argeo.people.PeopleException;
import org.argeo.people.PeopleNames;
import org.argeo.people.PeopleService;
import org.argeo.people.util.PeopleJcrUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
//import org.eclipse.ui.forms.AbstractFormPart;

/** Centralise management of contacts buttons */
public class ContactButtonsComposite extends Composite {
	private static final long serialVersionUID = 2331713954300845292L;

	// Context
	private final ResourcesService resourcesService;
	private final PeopleService peopleService;
	private final SystemWorkbenchService systemWorkbenchService;
	private final Node contactNode;
	private final boolean isEditing;
	private final Node parentVersionableNode;
	private final AbstractFormPart formPart;

	/**
	 * 
	 * @param editor
	 * @param formPart
	 * @param parent
	 * @param style
	 * @param contactNode
	 * @param parentVersionableNode
	 * @param resourcesService
	 * @param peopleService
	 * @param systemWorkbenchService
	 */
	public ContactButtonsComposite(ConnectEditor editor, AbstractFormPart formPart, Composite parent, int style,
			Node contactNode, Node parentVersionableNode, ResourcesService resourcesService,
			PeopleService peopleService, SystemWorkbenchService systemWorkbenchService) {
		super(parent, style);
		this.resourcesService = resourcesService;
		this.peopleService = peopleService;
		this.systemWorkbenchService = systemWorkbenchService;
		this.contactNode = contactNode;
		this.parentVersionableNode = parentVersionableNode;
		this.formPart = formPart;
		this.isEditing = editor.isEditing();
		populate();
	}

	private void populate() {
		// Initialization
		Composite buttCmp = this;
		GridLayout gl = ConnectUiUtils.noSpaceGridLayout(isEditing ? 3 : 2);
		buttCmp.setLayout(gl);

		// final Button categoryBtn =
		createCategoryButton(buttCmp);

		// Primary management
		Button primaryBtn = createPrimaryButton(buttCmp);
		if (primaryBtn != null)
			configurePrimaryButton(primaryBtn);

		// Deletion
		if (isEditing) {
			Button deleteBtn = createDeleteButton(buttCmp);
			configureDeleteButton(deleteBtn);
		}
	}

	private void createCategoryButton(Composite parent) {
		Label btn = new Label(parent, SWT.NONE);
		// CmsUiUtils.style(btn, ConnectUiStyles.FLAT_BTN);

		btn.setImage(systemWorkbenchService.getIconForType(contactNode));
		GridData gd = new GridData();
		// gd.widthHint = 16;
		// gd.heightHint = 16;
		btn.setLayoutData(gd);
		// return btn;
	}

	private Button createDeleteButton(Composite parent) {
		Button btn = new Button(parent, SWT.FLAT | SWT.CENTER);
		CmsUiUtils.style(btn, ConnectUiStyles.FLAT_BTN);
		btn.setImage(ConnectImages.DELETE);
		GridData gd = new GridData();
		gd.widthHint = 20;
		gd.heightHint = 16;
		btn.setLayoutData(gd);
		return btn;
	}

	private Button createPrimaryButton(Composite parent) {
		try {
			// update image
			boolean isPrimary = (contactNode.hasProperty(PeopleNames.PEOPLE_IS_PRIMARY)
					&& contactNode.getProperty(PeopleNames.PEOPLE_IS_PRIMARY).getBoolean());
			if (isEditing) {
				Button btn = new Button(parent, SWT.FLAT | SWT.CENTER);
				// CmsUiUtils.style(btn, ConnectUiStyles.FLAT_BTN);

				btn.setImage(isPrimary ? ConnectImages.PRIMARY : ConnectImages.PRIMARY_NOT);
				// btn.setEnabled(isEditing);
				GridData gd = new GridData();
				gd.widthHint = 20;
				gd.heightHint = 16;
				btn.setLayoutData(gd);
				return btn;
			} else {
				Label lbl = new Label(parent, SWT.NONE);
				lbl.setImage(isPrimary ? ConnectImages.PRIMARY : ConnectImages.PRIMARY_NOT);
				return null;
			}
			// primaryBtn.setGrayed(false);
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
						PeopleJcrUtils.updatePrimaryCache(resourcesService, peopleService, parentVersionableNode,
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
				boolean hasChanged = PeopleJcrUtils.markAsPrimary(resourcesService, peopleService,
						parentVersionableNode, contactNode);

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
