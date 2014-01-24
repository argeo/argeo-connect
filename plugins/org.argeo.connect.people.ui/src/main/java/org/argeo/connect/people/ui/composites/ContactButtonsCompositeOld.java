package org.argeo.connect.people.ui.composites;

import javax.jcr.InvalidItemStateException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.ui.PeopleImages;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.commands.ForceRefresh;
import org.argeo.connect.people.ui.providers.PeopleImageProvider;
import org.argeo.connect.people.ui.utils.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.PeopleJcrUtils;
import org.argeo.eclipse.ui.utils.CommandUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Centralizes management of contacts buttons.
 * 
 */
public class ContactButtonsCompositeOld extends Composite {
	private static final long serialVersionUID = 2331713954300845292L;

	private final Node contactNode;
	private final Node parentVersionableNode;
	// private final FormToolkit toolkit;
	private final IManagedForm form;

	private final PeopleImageProvider imageProvider = new PeopleImageProvider();

	// Don't forget to unregister on dispose
	private AbstractFormPart formPart;

	public ContactButtonsCompositeOld(Composite parent, int style,
			FormToolkit toolkit, IManagedForm form, Node contactNode,
			Node parentVersionableNode) {
		super(parent, style);
		this.contactNode = contactNode;
		// this.toolkit = toolkit;
		this.form = form;
		this.parentVersionableNode = parentVersionableNode;
		populate();
	}

	private void populate() {
		// Initialization
		Composite buttCmp = this;
		GridLayout gl = PeopleUiUtils.gridLayoutNoBorder(3);
		buttCmp.setLayout(gl);

		// final Button categoryBtn =
		createCategoryButton(buttCmp, contactNode);
		final Button primaryBtn = createPrimaryButton(buttCmp);
		final Button deleteBtn = createDeleteButton(buttCmp);

		formPart = new AbstractFormPart() {
			public void refresh() {
				super.refresh();
				try {
					// refresh buttons
					boolean isPrimary = (contactNode
							.hasProperty(PeopleNames.PEOPLE_IS_PRIMARY) && contactNode
							.getProperty(PeopleNames.PEOPLE_IS_PRIMARY)
							.getBoolean());
					if (isPrimary)
						primaryBtn.setImage(PeopleImages.PRIMARY_BTN);
					else
						primaryBtn.setImage(PeopleImages.PRIMARY_NOT_BTN);
					boolean checkedOut = CommonsJcrUtils
							.isNodeCheckedOutByMe(parentVersionableNode);
					primaryBtn.setEnabled(checkedOut);
					primaryBtn.setGrayed(false);
					deleteBtn.setVisible(checkedOut);
				} catch (Exception e) {
					if (e instanceof InvalidItemStateException) {
						// TODO clean: this exception normally means node
						// has already been removed.
					} else
						throw new PeopleException(
								"unexpected error while refreshing", e);
				}
			}
		};

		configureDeleteButton(deleteBtn, contactNode, parentVersionableNode);
		configurePrimaryButton(primaryBtn, contactNode, parentVersionableNode);
		// To force update of primary button on creation
		formPart.refresh();
		formPart.initialize(form);
		form.addPart(formPart);
	}

	private Button createCategoryButton(Composite parent, Node contactNode) {
		Button btn = new Button(parent, SWT.FLAT);
		btn.setData(PeopleUiConstants.CUSTOM_VARIANT,
				PeopleUiConstants.CSS_FLAT_IMG_BUTTON);
		try {
			String category = null;
			if (contactNode.hasProperty(PeopleNames.PEOPLE_CONTACT_CATEGORY))
				category = CommonsJcrUtils.get(contactNode,
						PeopleNames.PEOPLE_CONTACT_CATEGORY);
			String nature = null;
			if (contactNode.hasProperty(PeopleNames.PEOPLE_CONTACT_NATURE))
				nature = CommonsJcrUtils.get(contactNode,
						PeopleNames.PEOPLE_CONTACT_NATURE);

			String contactType = contactNode.getPrimaryNodeType().getName();
			String entityType = contactNode.getParent().getParent()
					.getPrimaryNodeType().getName();

			btn.setImage(imageProvider.getContactIcon(entityType, contactType,
					nature, category));

			GridData gd = new GridData();
			gd.widthHint = 16;
			gd.heightHint = 16;
			btn.setLayoutData(gd);
			return btn;
		} catch (RepositoryException re) {
			throw new PeopleException("unable to get image for contact");
		}
	}

	private Button createDeleteButton(Composite parent) {
		Button btn = new Button(parent, SWT.FLAT | SWT.BOTTOM);
		btn.setData(PeopleUiConstants.CUSTOM_VARIANT,
				PeopleUiConstants.CSS_FLAT_IMG_BUTTON);
		btn.setImage(PeopleImages.DELETE_BTN);
		GridData gd = new GridData();
		gd.widthHint = 16;
		gd.heightHint = 16;
		btn.setLayoutData(gd);
		return btn;
	}

	private Button createPrimaryButton(Composite parent) {
		Button btn = new Button(parent, SWT.FLAT);
		btn.setData(PeopleUiConstants.CUSTOM_VARIANT,
				PeopleUiConstants.CSS_FLAT_IMG_BUTTON);
		btn.setImage(PeopleImages.PRIMARY_NOT_BTN);
		GridData gd = new GridData();
		gd.widthHint = 16;
		gd.heightHint = 16;
		btn.setLayoutData(gd);
		return btn;
	}

	private void configureDeleteButton(final Button btn, final Node node,
			final Node parNode) { // , final AbstractFormPart
									// genericContactFormPart
		btn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				try {
					// update primary cache
					if (PeopleJcrUtils.isPrimary(parNode, node))
						PeopleJcrUtils.updatePrimaryCache(parNode, node, false);

					boolean wasCheckedOut = CommonsJcrUtils
							.isNodeCheckedOutByMe(parNode);

					if (!wasCheckedOut)
						CommonsJcrUtils.checkout(parNode);
					node.remove();

					if (wasCheckedOut)
						formPart.markDirty();
					else
						CommonsJcrUtils.saveAndCheckin(parNode);
					CommandUtils.callCommand(ForceRefresh.ID);
				} catch (RepositoryException e) {
					throw new PeopleException("unable to initialise deletion",
							e);
				}
			}
		});
	}

	private void configurePrimaryButton(final Button btn, final Node node,
			final Node parNode) {
		btn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				boolean wasCheckedOut = CommonsJcrUtils
						.isNodeCheckedOutByMe(parNode);
				if (!wasCheckedOut)
					CommonsJcrUtils.checkout(parNode);

				boolean hasChanged = PeopleJcrUtils
						.markAsPrimary(parNode, node);

				if (hasChanged) {
					if (wasCheckedOut)
						formPart.markDirty();
					else
						CommonsJcrUtils.saveAndCheckin(parNode);
					CommandUtils.callCommand(ForceRefresh.ID);
				} else if (!wasCheckedOut)
					CommonsJcrUtils.cancelAndCheckin(parNode);
			}
		});
	}

	@Override
	public boolean setFocus() {
		return true;
	}

	protected void disposePart(AbstractFormPart part) {
		if (part != null) {
			form.removePart(part);
			part.dispose();
		}
	}

	@Override
	public void dispose() {
		disposePart(formPart);
		super.dispose();
	}
}