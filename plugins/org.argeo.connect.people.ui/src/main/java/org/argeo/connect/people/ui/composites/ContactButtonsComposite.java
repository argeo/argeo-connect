package org.argeo.connect.people.ui.composites;

import javax.jcr.InvalidItemStateException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.ui.ContactImages;
import org.argeo.connect.people.ui.PeopleImages;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.PeopleJcrUtils;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Centralizes management of contacts buttons.
 * 
 */
public class ContactButtonsComposite extends Composite {
	private static final long serialVersionUID = 2331713954300845292L;

	protected final Node contactNode;
	protected final Node parentVersionableNode;
	protected final FormToolkit toolkit;
	protected final IManagedForm form;

	// Don't forget to unregister on dispose
	private AbstractFormPart formPart;

	public ContactButtonsComposite(Composite parent, int style,
			FormToolkit toolkit, IManagedForm form, Node contactNode,
			Node parentVersionableNode) {
		super(parent, style);
		this.contactNode = contactNode;
		this.toolkit = toolkit;
		this.form = form;
		this.parentVersionableNode = parentVersionableNode;
		populate();
	}

	private void populate() {
		// Initialization
		Composite buttCmp = this;

		// buttCmp.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
		// false));

		RowLayout rl = new RowLayout(SWT.WRAP);
		rl.marginWidth = 0;
		rl.type = SWT.HORIZONTAL;
		buttCmp.setLayout(rl);

		// final Button categoryBtn =
		createCategoryButton(buttCmp, contactNode);
		final Button primaryBtn = createPrimaryButton(buttCmp);
		final Button deleteBtn = createDeleteButton(buttCmp);

		formPart = new AbstractFormPart() {
			public void refresh() {
				super.refresh();
				try {
					// refresh buttons
					boolean isPrimary = contactNode.getProperty(
							PeopleNames.PEOPLE_IS_PRIMARY).getBoolean();
					if (isPrimary)
						primaryBtn.setImage(PeopleImages.PRIMARY_BTN);
					else
						primaryBtn.setImage(PeopleImages.PRIMARY_NOT_BTN);
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
		// formPart.refresh();
		formPart.initialize(form);
		form.addPart(formPart);
	}

	private Button createCategoryButton(Composite parent, Node contactNode) {
		Button btn = new Button(parent, SWT.FLAT);
		btn.setData(RWT.CUSTOM_VARIANT, PeopleUiConstants.CSS_FLAT_IMG_BUTTON);
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

			btn.setImage(ContactImages.getImage(entityType, contactType,
					nature, category));

			RowData rd = new RowData();
			rd.height = 16;
			rd.width = 16;
			btn.setLayoutData(rd);
			return btn;
		} catch (RepositoryException re) {
			throw new PeopleException("unable to get image for contact");
		}
	}

	private Button createDeleteButton(Composite parent) {
		Button btn = new Button(parent, SWT.FLAT);
		btn.setData(RWT.CUSTOM_VARIANT, PeopleUiConstants.CSS_FLAT_IMG_BUTTON);
		btn.setImage(PeopleImages.DELETE_BTN);
		RowData rd = new RowData();
		rd.height = 16;
		rd.width = 16;
		btn.setLayoutData(rd);
		return btn;
	}

	private Button createPrimaryButton(Composite parent) {
		Button btn = new Button(parent, SWT.FLAT);
		btn.setData(RWT.CUSTOM_VARIANT, PeopleUiConstants.CSS_FLAT_IMG_BUTTON);
		btn.setImage(PeopleImages.PRIMARY_NOT_BTN);
		RowData rd = new RowData();
		rd.height = 16;
		rd.width = 16;
		btn.setLayoutData(rd);
		return btn;
	}

	private void configureDeleteButton(Button btn, final Node node,
			final Node parNode) { // , final AbstractFormPart
									// genericContactFormPart
		btn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				try {
					boolean wasCheckedOut = CommonsJcrUtils
							.isNodeCheckedOutByMe(parNode);
					if (!wasCheckedOut)
						CommonsJcrUtils.checkout(parNode);
					node.remove();
					if (wasCheckedOut)
						parNode.getSession().save();
					else
						CommonsJcrUtils.saveAndCheckin(parNode);
				} catch (RepositoryException e) {
					throw new PeopleException("unable to initialise deletion",
							e);
				}
				for (IFormPart part : form.getParts()) {
					if (part != formPart) {
						((AbstractFormPart) part).markStale();
						part.refresh();
					}
				}
			}
		});
	}

	private void configurePrimaryButton(Button btn, final Node node,
			final Node parNode) {
		btn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				try {
					boolean wasCheckedOut = CommonsJcrUtils
							.isNodeCheckedOutByMe(parNode);
					if (!wasCheckedOut)
						CommonsJcrUtils.checkout(parNode);
					boolean wasPrimary = false;
					if (node.hasProperty(PeopleNames.PEOPLE_IS_PRIMARY)
							&& node.getProperty(PeopleNames.PEOPLE_IS_PRIMARY)
									.getBoolean())
						wasPrimary = true;
					PeopleJcrUtils.markAsPrimary(node, !wasPrimary);
					if (wasCheckedOut)
						parNode.getSession().save();
					else
						CommonsJcrUtils.saveAndCheckin(parNode);
					for (IFormPart part : form.getParts()) {
						((AbstractFormPart) part).markStale();
						part.refresh();
					}
				} catch (RepositoryException e) {
					throw new PeopleException("unable to initialise deletion",
							e);
				}
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