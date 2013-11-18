package org.argeo.connect.people.ui.toolkits;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.connect.people.ContactValueCatalogs;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.ui.composites.ContactAddressComposite;
import org.argeo.connect.people.ui.composites.ContactComposite;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.PeopleJcrUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Centralizes creation of commonly used people contact controls (typically Text
 * and composite widget) to be used in various forms.
 */
public class ContactToolkit {

	private final FormToolkit toolkit;
	private final IManagedForm form;

	public ContactToolkit(FormToolkit toolkit, IManagedForm form) {
		this.toolkit = toolkit;
		this.form = form;
	}

	public void createContactPanelWithNotes(Composite parent, final Node entity) {
		parent.setLayout(new GridLayout(2, true));

		// Scrolled list of existing contacts
		ScrolledComposite contactListCmp = new ScrolledComposite(parent,
				SWT.NO_FOCUS | SWT.H_SCROLL | SWT.V_SCROLL);
		contactListCmp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true));
		contactListCmp.setMinSize(350, 100);
		contactListCmp.setExpandHorizontal(true);
		contactListCmp.setExpandVertical(true);
		contactListCmp.setLayout(PeopleUiUtils.gridLayoutNoBorder());
		Composite innerCmp = new Composite(contactListCmp, SWT.NO_FOCUS);
		innerCmp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		populateDisplayContactPanel(innerCmp, entity);
		contactListCmp.setContent(innerCmp);

		// notes about current contact
		Composite rightCmp = toolkit.createComposite(parent, SWT.NO_FOCUS);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, false, true);
		gd.widthHint = 200;
		gd.minimumWidth = 200;
		rightCmp.setLayoutData(gd);
		populateNotePanel(rightCmp, entity);

		// Add contact tool bar.
		final Composite newContactCmp = toolkit.createComposite(parent,
				SWT.NO_FOCUS);
		gd = new GridData(SWT.FILL, SWT.BOTTOM, true, false);
		gd.horizontalSpan = 2;
		newContactCmp.setLayoutData(gd);
		populateAddContactPanel(newContactCmp, entity);

		parent.layout();
	}

	/**
	 * @param panel
	 * @param entity
	 */
	public void createContactPanel(final Composite panel, final Node entity) {
		panel.setLayout(new GridLayout());

		final ScrolledComposite contactListCmp = new ScrolledComposite(panel,
				SWT.NO_FOCUS | SWT.V_SCROLL);
		contactListCmp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true));
		populateDisplayContactPanel(contactListCmp, entity);

		final Composite newContactCmp = toolkit.createComposite(panel,
				SWT.NO_FOCUS);
		newContactCmp.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true,
				false));
		populateAddContactPanel(newContactCmp, entity);
	}

	/** Manage display and update of existing contact Nodes */
	public void populateDisplayContactPanel(final Composite parent,
			final Node entity) {
		parent.setLayout(PeopleUiUtils.gridLayoutNoBorder());

		final Map<String, Composite> contactCmps = new HashMap<String, Composite>();
		AbstractFormPart formPart = new AbstractFormPart() {
			public void refresh() {
				try {
					super.refresh();
					// first: initialise composite for new contacts
					Node contactsPar = entity
							.getNode(PeopleNames.PEOPLE_CONTACTS);
					NodeIterator ni = contactsPar.getNodes();
					while (ni.hasNext()) {
						Node currNode = ni.nextNode();
						String currJcrId = currNode.getIdentifier();
						if (!contactCmps.containsKey(currJcrId)) {
							Composite currCmp = null;
							if (CommonsJcrUtils.isNodeType(currNode,
									PeopleTypes.PEOPLE_ADDRESS))
								currCmp = new ContactAddressComposite(parent,
										SWT.NO_FOCUS, toolkit, form, currNode,
										entity);
							else
								currCmp = new ContactComposite(parent,
										SWT.NO_FOCUS, toolkit, form, currNode,
										entity);
							contactCmps.put(currJcrId, currCmp);
						}
					}

					// then remove necessary composites
					Session session = contactsPar.getSession();
					for (String jcrId : contactCmps.keySet()) {
						// TODO: enhance this
						Composite currCmp = contactCmps.get(jcrId);
						try {
							session.getNodeByIdentifier(jcrId);
						} catch (ItemNotFoundException infe) {
							currCmp.dispose();
						}
					}
					parent.pack(true);
					parent.getParent().pack(true);
					parent.layout();
					parent.getParent().layout();

				} catch (RepositoryException e) {
					throw new PeopleException(
							"Cannot refresh contact panel formPart", e);
				}
			}
		};
		formPart.refresh();
		formPart.initialize(form);
		form.addPart(formPart);
	}

	public void populateNotePanel(final Composite parent, final Node entity) {
		parent.setLayout(new GridLayout());

		parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		toolkit.createLabel(parent, "Notes: ", SWT.NONE);

		final Text notesTxt = toolkit.createText(parent, "", SWT.BORDER
				| SWT.MULTI | SWT.WRAP);

		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.minimumWidth = 200;
		gd.minimumHeight = 200;
		notesTxt.setLayoutData(gd);

		AbstractFormPart notePart = new AbstractFormPart() {
			public void refresh() {
				super.refresh();
				String desc = CommonsJcrUtils.getStringValue(entity,
						Property.JCR_DESCRIPTION);
				if (desc != null)
					notesTxt.setText(desc);
				notesTxt.setEnabled(CommonsJcrUtils
						.isNodeCheckedOutByMe(entity));
				parent.layout();
			}
		};

		PeopleUiUtils.addModifyListener(notesTxt, entity,
				Property.JCR_DESCRIPTION, notePart);

		notePart.initialize(form);
		form.addPart(notePart);
	}

	/** Populate a composite that enable addition of a new contact */
	public void populateAddContactPanel(Composite parent, final Node entity) {
		parent.setLayout(new GridLayout(2, false));

		final Combo addContactCmb = new Combo(parent, SWT.NONE | SWT.READ_ONLY
				| SWT.NO_FOCUS);
		GridData gd = new GridData(SWT.LEFT, SWT.TOP, false, false);
		gd.widthHint = 100;
		addContactCmb.setLayoutData(gd);
		addContactCmb.setItems(ContactValueCatalogs.ARRAY_CONTACT_TYPES);
		// Add a default value
		addContactCmb.add("Add a contact", 0);

		final Composite editPanel = toolkit.createComposite(parent,
				SWT.NO_FOCUS);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace = true;
		editPanel.setLayoutData(gd);

		editPanel.setVisible(false);

		if (editPanel.getLayout() == null) {
			RowLayout layout = new RowLayout();
			// Optionally set layout fields.
			layout.wrap = true;
			layout.marginTop = 0;
			// Set the layout into the composite.
			editPanel.setLayout(layout);
		}

		AbstractFormPart editPart = new AbstractFormPart() {
			// Update values on refresh
			public void refresh() {
				super.refresh();
				editPanel.setVisible(false);
				addContactCmb.select(0);
			}
		};

		editPart.initialize(form);
		form.addPart(editPart);
		// show the edit new contact panel when selection change
		addContactCmb.addSelectionListener(new SelectionListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {

					int index = addContactCmb.getSelectionIndex();
					if (index == 0) {
						editPanel.setVisible(false);
						// remove all controls
						for (Control ctl : editPanel.getChildren()) {
							ctl.dispose();
						}
						toolkit.createLabel(editPanel, "");
					} else {
						editPanel.setVisible(true);
						String selected = addContactCmb.getItem(index);
						Control first;
						first = populateNewContactComposite(
								editPanel,
								entity,
								ContactValueCatalogs
										.getKeyByValue(
												ContactValueCatalogs.MAPS_CONTACT_TYPES,
												selected));
						if (first != null)
							first.setFocus();
					}
				} catch (RepositoryException e1) {
					throw new PeopleException(
							"Unable to refresh add contact panel", e1);
				}
				editPanel.pack();
				editPanel.layout(true);
				editPanel.getParent().getParent().layout();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// set default selection after everything is initialized to ease layout
		addContactCmb.select(0);

	}

	/** Populate an editable contact composite */
	public Control populateNewContactComposite(Composite parent,
			final Node entity, final String contactType)
			throws RepositoryException {
		RowData rd;

		// if (parent.getLayout() == null) {
		// RowLayout layout = new RowLayout();
		// // Optionally set layout fields.
		// layout.wrap = true;
		// layout.marginTop = 0;
		// // Set the layout into the composite.
		// parent.setLayout(layout);
		// }

		// remove all controls
		for (Control ctl : parent.getChildren()) {
			ctl.dispose();
		}

		// Nature (work or private) is only for persons
		final Combo natureCmb = entity.isNodeType(PeopleTypes.PEOPLE_PERSON) ? new Combo(
				parent, SWT.NONE) : null;

		// No category for emails and web sites.
		final Combo catCmb = !(contactType.equals(PeopleTypes.PEOPLE_URL) || contactType
				.equals(PeopleTypes.PEOPLE_EMAIL)) ? new Combo(parent, SWT.NONE)
				: null;

		final Text labelTxt = new Text(parent, SWT.BORDER);

		// For all contact
		final Text valueTxt = createAddressTxt(
				!contactType.equals(PeopleTypes.PEOPLE_ADDRESS), parent,
				"Contact value", 200);

		// specific for addresses
		final Text streetTxt = createAddressTxt(
				contactType.equals(PeopleTypes.PEOPLE_ADDRESS), parent,
				"Street", 150);
		final Text street2Txt = createAddressTxt(
				contactType.equals(PeopleTypes.PEOPLE_ADDRESS), parent,
				"Street Complement", 150);
		final Text zipTxt = createAddressTxt(
				contactType.equals(PeopleTypes.PEOPLE_ADDRESS), parent,
				"Zip code", 60);
		final Text cityTxt = createAddressTxt(
				contactType.equals(PeopleTypes.PEOPLE_ADDRESS), parent, "City",
				150);
		final Text stateTxt = createAddressTxt(
				contactType.equals(PeopleTypes.PEOPLE_ADDRESS), parent,
				"State", 150);
		final Text countryTxt = createAddressTxt(
				contactType.equals(PeopleTypes.PEOPLE_ADDRESS), parent,
				"Country", 150);
		final Text geoPointTxt = createAddressTxt(
				contactType.equals(PeopleTypes.PEOPLE_ADDRESS), parent,
				"Geopoint", 200);

		labelTxt.setMessage("Label");
		rd = new RowData(120, SWT.DEFAULT);
		labelTxt.setLayoutData(rd);

		final Button primaryChk = toolkit.createButton(parent, "Primary",
				SWT.CHECK);

		final Button validBtn = toolkit.createButton(parent, "Save", SWT.PUSH);

		if (natureCmb != null) {
			natureCmb.setItems(ContactValueCatalogs.ARRAY_CONTACT_NATURES);
			natureCmb.select(0);
			natureCmb.addSelectionListener(new SelectionListener() {
				private static final long serialVersionUID = 1L;

				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						if (catCmb != null) {
							catCmb.setItems(ContactValueCatalogs
									.getCategoryList(entity
											.getPrimaryNodeType().getName(),
											contactType, natureCmb.getText()));
							catCmb.select(0);
						}
					} catch (RepositoryException e1) {
						throw new PeopleException(
								"unable to retrieve category list", e1);
					}
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
		}

		if (catCmb != null) {
			catCmb.setItems(ContactValueCatalogs.getCategoryList(entity
					.getPrimaryNodeType().getName(), contactType, null));
			catCmb.select(0);
		}

		validBtn.addSelectionListener(new SelectionListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {

				String nature = natureCmb == null ? null : natureCmb.getText();
				String value = valueTxt == null ? null : valueTxt.getText();
				String cat = catCmb == null ? null : catCmb.getText();
				String label = labelTxt.getText();
				boolean isPrimary = primaryChk.getSelection();
				// EntityPanelToolkit
				boolean wasCheckedout = CommonsJcrUtils
						.isNodeCheckedOut(entity);
				if (!wasCheckedout)
					CommonsJcrUtils.checkout(entity);
				if (contactType.equals(PeopleTypes.PEOPLE_ADDRESS)) {
					Node node = PeopleJcrUtils.createAddress(entity,
							streetTxt.getText(), street2Txt.getText(),
							zipTxt.getText(), cityTxt.getText(),
							stateTxt.getText(), countryTxt.getText(),
							geoPointTxt.getText(), isPrimary, nature, cat,
							label);
					PeopleJcrUtils.updateDisplayAddress(node);
				} else
					PeopleJcrUtils.createContact(entity, contactType,
							contactType, value, isPrimary, nature, cat, label);

				if (!wasCheckedout)
					CommonsJcrUtils.saveAndCheckin(entity);
				else
					form.dirtyStateChanged();
				for (IFormPart part : form.getParts()) {
					((AbstractFormPart) part).markStale();
					part.refresh();
				}
				validBtn.getParent().setVisible(false);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		if (natureCmb != null)
			return natureCmb;
		else if (catCmb != null)
			return catCmb;
		else
			return valueTxt;
	}

	private Text createAddressTxt(boolean create, Composite parent, String msg,
			int width) {
		if (create) {
			Text text = toolkit.createText(parent, null, SWT.BORDER);
			text.setMessage(msg);
			text.setLayoutData(width == 0 ? new RowData() : new RowData(width,
					SWT.DEFAULT));
			return text;
		} else
			return null;
	}

}