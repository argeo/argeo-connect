package org.argeo.connect.people.ui.composites;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.argeo.connect.people.ContactValueCatalogs;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.dialogs.PickUpOrgDialog;
import org.argeo.connect.people.ui.utils.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.PeopleJcrUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * 
 * The special panel that display contacts
 */
public class ContactPanelComposite extends Composite {
	private static final long serialVersionUID = 58381532068661087L;

	private final FormToolkit toolkit;
	private final IManagedForm form;

	private final PeopleService peopleService;
	private final Node entity;
	private ContactFormPart formPart;

	private Composite innerCmp;

	public ContactPanelComposite(Composite parent, int style,
			FormToolkit toolkit, IManagedForm form, Node entityNode,
			PeopleService peopleService) {
		super(parent, style);
		this.toolkit = toolkit;
		this.form = form;
		this.entity = entityNode;
		this.peopleService = peopleService;
		populate();
	}

	private void populate() {
		Composite parent = this;
		parent.setLayout(PeopleUiUtils.gridLayoutNoBorder());

		// Does not scroll...

		// Add a scrolled container
		ScrolledComposite container = new ScrolledComposite(parent,
				SWT.NO_FOCUS | SWT.V_SCROLL); // SWT.H_SCROLL |
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		// container.setAlwaysShowScrollBars(true);
		innerCmp = new Composite(container, SWT.NO_FOCUS);
		innerCmp.setLayout(new GridLayout());
		formPart = new ContactFormPart();

		container.setExpandHorizontal(true);
		container.setExpandVertical(false);
		container.setLayout(PeopleUiUtils.gridLayoutNoBorder());
		innerCmp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		formPart.initialize(form);
		form.addPart(formPart);

		container.setContent(innerCmp);
	}

	private class ContactFormPart extends AbstractFormPart {

		@Override
		public void refresh() {
			super.refresh();
			try {
				if (innerCmp.isDisposed())
					return;

				// We redraw the full part at each refresh
				Control[] oldChildren = innerCmp.getChildren();
				for (Control child : oldChildren)
					child.dispose();

				boolean checkedOut = CommonsJcrUtils
						.isNodeCheckedOutByMe(entity);
				GridData gd;
				Composite newContactCmp = null;

				// Add contact tool bar.
				if (checkedOut) {
					newContactCmp = toolkit.createComposite(innerCmp,
							SWT.NO_FOCUS);
					gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
					newContactCmp.setLayoutData(gd);
					populateAddContactPanel(newContactCmp);
				}

				// list existing contacts
				Composite contactListCmp = toolkit.createComposite(innerCmp,
						SWT.NO_FOCUS);
				contactListCmp.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
						true, true));
				populateDisplayContactPanel(contactListCmp, checkedOut);
				contactListCmp.layout(true);

				// notes about current contact
				Composite noteCmp = toolkit.createComposite(innerCmp,
						SWT.NO_FOCUS);
				gd = new GridData(SWT.FILL, SWT.BOTTOM, true, false);
				gd.heightHint = 60;
				noteCmp.setLayoutData(gd);
				populateNotePanel(noteCmp);

				innerCmp.pack(true);
				innerCmp.getParent().pack(true);
				// parent.layout();
				innerCmp.getParent().layout();
			} catch (Exception e) {
				throw new PeopleException(
						"unexpected error while refreshing node " + entity, e);
			}
		}
	}

	/** Manage display and update of existing contact Nodes */
	private void populateDisplayContactPanel(final Composite parent,
			boolean isCheckedOut) {
		parent.setLayout(PeopleUiUtils.gridLayoutNoBorder());
		try {
			if (entity.hasNode(PeopleNames.PEOPLE_CONTACTS)) {
				Node contactsPar = entity.getNode(PeopleNames.PEOPLE_CONTACTS);
				NodeIterator ni = contactsPar.getNodes();
				while (ni.hasNext()) {
					Node currNode = ni.nextNode();
					if (CommonsJcrUtils.isNodeType(currNode,
							PeopleTypes.PEOPLE_ADDRESS))
						new ContactAddressComposite(parent, SWT.NO_FOCUS,
								toolkit, formPart, peopleService, currNode,
								entity);
					else
						new ContactComposite(parent, SWT.NO_FOCUS, toolkit,
								formPart, currNode, entity);
				}
			}
		} catch (RepositoryException e) {
			throw new PeopleException(
					"Cannot populate existing contact list for entity "
							+ entity, e);
		}
	}

	private void populateNotePanel(Composite parent) {
		parent.setLayout(PeopleUiUtils.gridLayoutNoBorder(2));
		toolkit.createLabel(parent, "Notes: ", SWT.TOP);
		Text notesTxt = toolkit.createText(parent, "", SWT.BORDER | SWT.MULTI
				| SWT.WRAP);
		notesTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		PeopleUiUtils.refreshFormTextWidget(notesTxt, entity,
				Property.JCR_DESCRIPTION);
		PeopleUiUtils.addTxtModifyListener(formPart, notesTxt, entity,
				Property.JCR_DESCRIPTION, PropertyType.STRING);
	}

	/** Populate a composite that enable addition of a new contact */
	private void populateAddContactPanel(Composite parent) {
		RowLayout layout = new RowLayout();
		layout.wrap = true;
		layout.marginTop = layout.marginBottom = 0;
		parent.setLayout(layout);

		// ADD CONTACT
		Combo addContactCmb = new Combo(parent, SWT.NONE | SWT.READ_ONLY
				| SWT.NO_FOCUS);

		addContactCmb.setLayoutData(new RowData(100, SWT.DEFAULT));
		addContactCmb.setItems(ContactValueCatalogs.ARRAY_CONTACT_TYPES);
		addContactCmb.add("Add a contact", 0);
		addContactCmb.select(0);

		// NATURE(work or private) is only for persons
		Combo natureCmb = CommonsJcrUtils.isNodeType(entity,
				PeopleTypes.PEOPLE_PERSON) ? new Combo(parent, SWT.READ_ONLY)
				: null;
		if (natureCmb != null) {
			natureCmb.setLayoutData(new RowData(100, SWT.DEFAULT));
			natureCmb.setItems(ContactValueCatalogs.ARRAY_CONTACT_NATURES);
		}

		// Listeners
		addContactCmb.addSelectionListener(new MySelectionAdapter(entity,
				parent, addContactCmb, natureCmb));

		if (natureCmb != null) {
			natureCmb.addSelectionListener(new MySelectionAdapter(entity,
					parent, addContactCmb, natureCmb));
		}
	}

	private class MySelectionAdapter extends SelectionAdapter {
		private static final long serialVersionUID = 1L;
		private final Node entity;
		private final Composite parent;
		private final Combo addContactCmb;
		private final Combo natureCmb;

		public MySelectionAdapter(Node entity, Composite editPanel,
				Combo addContactCmb, Combo natureCmb) {
			this.entity = entity;
			this.parent = editPanel;
			this.addContactCmb = addContactCmb;
			this.natureCmb = natureCmb;
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			try {
				int index = addContactCmb.getSelectionIndex();
				if (natureCmb != null) {
					natureCmb.setVisible(index > 0);
				}

				if (index == 0) {
					resetAddContactEditPanel(parent, addContactCmb, natureCmb);
				} else {
					String type = ContactValueCatalogs.getKeyByValue(
							ContactValueCatalogs.MAPS_CONTACT_TYPES,
							addContactCmb.getItem(index));
					String nature = null;
					int natureIndex = -1;
					if (natureCmb != null)
						natureIndex = natureCmb.getSelectionIndex();
					if (natureIndex != -1)
						nature = ContactValueCatalogs.ARRAY_CONTACT_NATURES[natureIndex];

					removeOtherChildren(parent, addContactCmb, natureCmb);

					Control first = populateNewContactComposite(parent, entity,
							type, nature, addContactCmb);
					if (first != null)
						first.setFocus();
				}
			} catch (RepositoryException e1) {
				throw new PeopleException(
						"Unable to refresh add contact panel", e1);
			}
			// innerCmp.pack();
			innerCmp.layout();
		}
	}

	private void removeOtherChildren(Composite editPanel, Combo chooseTypeCmb,
			Combo chooseNatureCmb) {
		// remove all controls
		for (Control ctl : editPanel.getChildren()) {
			if (!(chooseTypeCmb == ctl || chooseNatureCmb != null
					&& chooseNatureCmb == ctl))
				ctl.dispose();
		}
	}

	private void resetAddContactEditPanel(Composite editPanel,
			Combo chooseTypeCmb, Combo chooseNatureCmb) {
		// reset combo
		if (chooseTypeCmb.getSelectionIndex() != 0)
			chooseTypeCmb.select(0);
		if (chooseNatureCmb != null)
			chooseNatureCmb.setVisible(false);
		removeOtherChildren(editPanel, chooseTypeCmb, chooseNatureCmb);
	}

	/** Populate an editable contact composite */
	private Control populateNewContactComposite(Composite parent,
			final Node entity, final String contactType, final String nature,
			Combo addContactCombo) throws RepositoryException {

		if (contactType.equals(PeopleTypes.PEOPLE_URL)
				|| contactType.equals(PeopleTypes.PEOPLE_EMAIL)) {
			return createMailWidgets(parent, entity, contactType, nature,
					addContactCombo);
		} else if (contactType.equals(PeopleTypes.PEOPLE_ADDRESS)) {
			if (nature != null
					&& nature.equals(ContactValueCatalogs.CONTACT_NATURE_PRO))
				return createWorkAddressWidgets(parent, entity, contactType,
						nature, addContactCombo);
			else
				return createAddressWidgets(parent, contactType, nature,
						addContactCombo);
		} else {
			return createContactWidgets(parent, contactType, nature,
					addContactCombo);
		}
	}

	private Control createMailWidgets(Composite parent, final Node entity,
			final String contactType, final String nature,
			final Combo addContactCombo) {

		final Text valueTxt = createRowDataLT(parent, "Contact value", 200);

		final Text labelTxt = createRowDataLT(parent, "Label", 120);

		final Button primaryChk = toolkit.createButton(parent, "Primary",
				SWT.CHECK);

		final Button validBtn = toolkit.createButton(parent, "Add", SWT.PUSH);

		validBtn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {

				String value = valueTxt.getText();
				String label = labelTxt.getText();
				boolean isPrimary = primaryChk.getSelection();
				saveAndRefresh(contactType, contactType, value, isPrimary,
						nature, null, label);
			}
		});

		valueTxt.addTraverseListener(new TraverseListener() {
			private static final long serialVersionUID = 9192624317905937169L;

			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.keyCode == SWT.CR) {
					e.doit = false;

					String value = valueTxt.getText();
					String label = labelTxt.getText();
					boolean isPrimary = primaryChk.getSelection();
					saveAndRefresh(contactType, contactType, value, isPrimary,
							nature, null, label);
				}
			}
		});

		return valueTxt;
	}

	private Control createContactWidgets(Composite parent,
			final String contactType, final String nature,
			final Combo addContactCombo) {

		final Text valueTxt = createRowDataLT(parent, "Contact value", 200);

		final Combo catCmb = new Combo(parent, SWT.READ_ONLY);
		try {
			catCmb.setItems(ContactValueCatalogs.getCategoryList(entity
					.getPrimaryNodeType().getName(), contactType, nature));
		} catch (RepositoryException e1) {
			throw new PeopleException("unable to get category list for "
					+ contactType + " & " + nature, e1);
		}
		catCmb.select(0);

		final Text labelTxt = createRowDataLT(parent, "Label", 120);

		final Button primaryChk = toolkit.createButton(parent, "Primary",
				SWT.CHECK);

		final Button validBtn = toolkit.createButton(parent, "Add", SWT.PUSH);

		validBtn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				String value = valueTxt.getText();
				String label = labelTxt.getText();
				String cat = catCmb.getText();
				boolean isPrimary = primaryChk.getSelection();
				saveAndRefresh(contactType, contactType, value, isPrimary,
						nature, cat, label);
			}
		});

		valueTxt.addTraverseListener(new TraverseListener() {
			private static final long serialVersionUID = 9192624317905937169L;

			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.keyCode == SWT.CR) {
					e.doit = false;
					String value = valueTxt.getText();
					String label = labelTxt.getText();
					String cat = catCmb.getText();
					boolean isPrimary = primaryChk.getSelection();
					saveAndRefresh(contactType, contactType, value, isPrimary,
							nature, cat, label);
				}
			}
		});
		return valueTxt;
	}

	private void saveAndRefresh(String contactType, String name, String value,
			boolean isPrimary, String nature, String category, String label) {

		PeopleJcrUtils.createContact(entity, contactType, contactType, value,
				isPrimary, nature, null, label);
		formPart.markDirty();
		formPart.refresh();
	}

	private Control createAddressWidgets(Composite parent,
			final String contactType, final String nature,
			final Combo addContactCombo) {

		final Combo catCmb = new Combo(parent, SWT.NONE);
		try {
			catCmb.setItems(ContactValueCatalogs.getCategoryList(entity
					.getPrimaryNodeType().getName(), contactType, nature));
		} catch (RepositoryException e1) {
			throw new PeopleException("unable to get category list for "
					+ contactType + " & " + nature, e1);
		}
		catCmb.select(0);

		final Text labelTxt = createRowDataLT(parent, "Label", 120);

		final Text streetTxt = createRowDataLT(parent, "Street", 150);
		final Text street2Txt = createRowDataLT(parent, "Street Complement",
				150);
		final Text zipTxt = createRowDataLT(parent, "Zip code", 60);
		final Text cityTxt = createRowDataLT(parent, "City", 150);
		final Text stateTxt = createRowDataLT(parent, "State", 150);
		final Text countryTxt = createRowDataLT(parent, "Country", 150);
		final Text geoPointTxt = createRowDataLT(parent, "Geopoint", 200);

		final Button primaryChk = toolkit.createButton(parent, "Primary",
				SWT.CHECK);

		final Button validBtn = toolkit.createButton(parent, "Save", SWT.PUSH);

		validBtn.addSelectionListener(new SelectionAdapter() {

			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				String cat = catCmb.getText();
				String label = labelTxt.getText();
				boolean isPrimary = primaryChk.getSelection();

				Node node = PeopleJcrUtils.createAddress(entity,
						streetTxt.getText(), street2Txt.getText(),
						zipTxt.getText(), cityTxt.getText(),
						stateTxt.getText(), countryTxt.getText(),
						geoPointTxt.getText(), isPrimary, nature, cat, label);
				PeopleJcrUtils.updateDisplayAddress(node);
				formPart.markDirty();
				formPart.refresh();
			}

		});
		return catCmb;
	}

	private Control createWorkAddressWidgets(final Composite parent,
			final Node entity, final String contactType, final String nature,
			final Combo addContactCombo) {
		try {
			final Combo catCmb = new Combo(parent, SWT.NONE);
			catCmb.setItems(ContactValueCatalogs.getCategoryList(entity
					.getPrimaryNodeType().getName(), contactType, nature));
			catCmb.select(0);

			final Text labelTxt = createRowDataLT(parent, "Label", 120);

			final Link chooseOrgLk = new Link(parent, SWT.BOTTOM);
			toolkit.adapt(chooseOrgLk, false, false);
			chooseOrgLk.setText("<a>Pick up an Organisation</a>");
			final PickUpOrgDialog diag = new PickUpOrgDialog(
					chooseOrgLk.getShell(), "Choose an organisation",
					entity.getSession(), entity);

			final Text valueTxt = createRowDataLT(parent, "Contact value", 200);
			final String PROP_SELECTED_NODE = "selectedNode";

			valueTxt.setEnabled(false);

			chooseOrgLk.addSelectionListener(new SelectionAdapter() {
				private static final long serialVersionUID = -7118320199160680131L;

				@Override
				public void widgetSelected(final SelectionEvent event) {
					diag.open();
					Node currNode = diag.getSelected();
					valueTxt.setData(PROP_SELECTED_NODE, currNode);
					if (currNode != null) {
						valueTxt.setText(CommonsJcrUtils.get(currNode,
								Property.JCR_TITLE));
					}
				}
			});

			final Button primaryChk = toolkit.createButton(parent, "Primary",
					SWT.CHECK);

			final Button validBtn = toolkit.createButton(parent, "Add",
					SWT.PUSH);

			validBtn.addSelectionListener(new SelectionAdapter() {
				private static final long serialVersionUID = 1L;

				@Override
				public void widgetSelected(SelectionEvent e) {
					Node selected = (Node) valueTxt.getData(PROP_SELECTED_NODE);
					String label = labelTxt.getText();
					String cat = catCmb.getText();
					boolean isPrimary = primaryChk.getSelection();
					Node node = PeopleJcrUtils.createWorkAddress(entity,
							selected, isPrimary, cat, label);
					PeopleJcrUtils.updateDisplayAddress(node);
					formPart.markDirty();
					formPart.refresh();
				}
			});
			return catCmb;
		} catch (RepositoryException e1) {
			throw new PeopleException(
					"JCR Error while creating work address widgets for "
							+ contactType + " & " + nature, e1);
		}
	}

	private Text createRowDataLT(Composite parent, String msg, int width) {
		Text text = toolkit.createText(parent, null, SWT.BORDER);
		text.setMessage(msg);
		text.setLayoutData(width == 0 ? new RowData() : new RowData(width,
				SWT.DEFAULT));
		return text;
	}
}