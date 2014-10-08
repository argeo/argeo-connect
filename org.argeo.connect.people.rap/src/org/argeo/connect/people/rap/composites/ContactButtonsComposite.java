package org.argeo.connect.people.rap.composites;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.rap.PeopleImages;
import org.argeo.connect.people.rap.PeopleRapConstants;
import org.argeo.connect.people.rap.PeopleWorkbenchService;
import org.argeo.connect.people.rap.utils.PeopleRapUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.PeopleJcrUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Centralizes management of contacts buttons.
 * 
 */
public class ContactButtonsComposite extends Composite {
	private static final long serialVersionUID = 2331713954300845292L;

	// Context
	private final PeopleService peopleService;
	private final PeopleWorkbenchService peopleUiService;
	private final Node contactNode;
	private final boolean isCheckedOut;
	private final Node parentVersionableNode;
	private final AbstractFormPart formPart;

	// private final PeopleImageProvider imageProvider = new
	// PeopleImageProvider();

	public ContactButtonsComposite(Composite parent, int style,
			FormToolkit toolkit, AbstractFormPart formPart, Node contactNode,
			Node parentVersionableNode, PeopleWorkbenchService peopleUiService,
			PeopleService peopleService) {
		super(parent, style);
		this.peopleService = peopleService;
		this.peopleUiService = peopleUiService;
		this.contactNode = contactNode;
		this.parentVersionableNode = parentVersionableNode;
		this.formPart = formPart;
		this.isCheckedOut = CommonsJcrUtils
				.isNodeCheckedOutByMe(parentVersionableNode);
		populate();
	}

	private void populate() {
		// Initialization
		Composite buttCmp = this;
		GridLayout gl = PeopleRapUtils.noSpaceGridLayout(3);
		buttCmp.setLayout(gl);

		// final Button categoryBtn =
		createCategoryButton(buttCmp);

		// Primary management
		Button primaryBtn = createPrimaryButton(buttCmp);
		configurePrimaryButton(primaryBtn);

		// Deletion
		if (isCheckedOut) {
			Button deleteBtn = createDeleteButton(buttCmp);
			configureDeleteButton(deleteBtn);
		}
	}

	private Button createCategoryButton(Composite parent) {
		Button btn = new Button(parent, SWT.FLAT);
		btn.setData(PeopleRapConstants.CUSTOM_VARIANT,
				PeopleRapConstants.PEOPLE_CLASS_FLAT_BTN);

		btn.setImage(peopleUiService.getIconForType(contactNode));
		GridData gd = new GridData();
		gd.widthHint = 16;
		gd.heightHint = 16;
		btn.setLayoutData(gd);
		return btn;

		// try {
		//
		//
		// String category = null;
		// if (contactNode.hasProperty(PeopleNames.PEOPLE_CONTACT_CATEGORY))
		// category = CommonsJcrUtils.get(contactNode,
		// PeopleNames.PEOPLE_CONTACT_CATEGORY);
		// String nature = null;
		// if (contactNode.hasProperty(PeopleNames.PEOPLE_CONTACT_NATURE))
		// nature = CommonsJcrUtils.get(contactNode,
		// PeopleNames.PEOPLE_CONTACT_NATURE);
		//
		// String contactType = contactNode.getPrimaryNodeType().getName();
		// String entityType = contactNode.getParent().getParent()
		// .getPrimaryNodeType().getName();
		//
		// // btn.setImage(imageProvider.getContactIcon(entityType, contactType,
		// // nature, category));
		//
		// GridData gd = new GridData();
		// gd.widthHint = 16;
		// gd.heightHint = 16;
		// btn.setLayoutData(gd);
		// return btn;
		// } catch (RepositoryException re) {
		// throw new PeopleException("unable to get image for contact");
		// }
	}

	private Button createDeleteButton(Composite parent) {
		Button btn = new Button(parent, SWT.FLAT | SWT.BOTTOM);
		btn.setData(PeopleRapConstants.CUSTOM_VARIANT,
				PeopleRapConstants.PEOPLE_CLASS_FLAT_BTN);
		btn.setImage(PeopleImages.DELETE_BTN);
		GridData gd = new GridData();
		gd.widthHint = 16;
		gd.heightHint = 16;
		btn.setLayoutData(gd);
		return btn;
	}

	private Button createPrimaryButton(Composite parent) {
		try {
			Button btn = new Button(parent, SWT.FLAT);
			btn.setData(PeopleRapConstants.CUSTOM_VARIANT,
					PeopleRapConstants.PEOPLE_CLASS_FLAT_BTN);

			// update image
			boolean isPrimary = (contactNode
					.hasProperty(PeopleNames.PEOPLE_IS_PRIMARY) && contactNode
					.getProperty(PeopleNames.PEOPLE_IS_PRIMARY).getBoolean());
			if (isPrimary)
				btn.setImage(PeopleImages.PRIMARY_BTN);
			else
				btn.setImage(PeopleImages.PRIMARY_NOT_BTN);
			btn.setEnabled(isCheckedOut);
			// primaryBtn.setGrayed(false);

			GridData gd = new GridData();
			gd.widthHint = 16;
			gd.heightHint = 16;
			btn.setLayoutData(gd);
			return btn;
		} catch (RepositoryException re) {
			throw new PeopleException(
					"Unable to create primary button for node " + contactNode,
					re);
		}
	}

	private void configureDeleteButton(final Button btn) {
		btn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				try {
					// update primary cache
					if (PeopleJcrUtils.isPrimary(parentVersionableNode,
							contactNode))
						PeopleJcrUtils.updatePrimaryCache(peopleService,
								parentVersionableNode, contactNode, false);

					contactNode.remove();
					formPart.markDirty();
					formPart.refresh();
				} catch (RepositoryException e) {
					throw new PeopleException("unable to initialise deletion",
							e);
				}
			}
		});
	}

	private void configurePrimaryButton(final Button btn) {
		btn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				boolean hasChanged = PeopleJcrUtils.markAsPrimary(
						peopleService, parentVersionableNode, contactNode);

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