package org.argeo.people.workbench.rap.editors.tabs;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.argeo.cms.util.CmsUtils;
import org.argeo.cms.widgets.ScrolledPage;
import org.argeo.connect.ConnectConstants;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.ui.ConnectUiStyles;
import org.argeo.connect.ui.ConnectUiUtils;
import org.argeo.connect.ui.util.LazyCTabControl;
import org.argeo.connect.ui.widgets.TagLikeDropDown;
import org.argeo.connect.ui.workbench.AppWorkbenchService;
import org.argeo.connect.ui.workbench.ConnectWorkbenchUtils;
import org.argeo.connect.ui.workbench.parts.AbstractConnectEditor;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;
import org.argeo.people.ContactService;
import org.argeo.people.ContactValueCatalogs;
import org.argeo.people.PeopleException;
import org.argeo.people.PeopleNames;
import org.argeo.people.PeopleService;
import org.argeo.people.PeopleTypes;
import org.argeo.people.util.PeopleJcrUtils;
import org.argeo.people.workbench.rap.composites.ContactAddressComposite;
import org.argeo.people.workbench.rap.composites.ContactComposite;
import org.argeo.people.workbench.rap.dialogs.PickUpOrgDialog;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.widgets.FormToolkit;

/** A panel to display contacts */
public class ContactList extends LazyCTabControl {
	private static final long serialVersionUID = 58381532068661087L;

	// Context
	private final ResourcesService resourcesService;
	private final PeopleService peopleService;
	private final AppWorkbenchService appWorkbenchService;
	private final Node entity;

	// UI Objects
	private final AbstractConnectEditor editor;
	private final FormToolkit toolkit;
	private ContactFormPart myFormPart;
	private Composite innerCmp;
	// Caches the add new contact combo
	private Combo addContactCmb;

	public ContactList(Composite parent, int style, AbstractConnectEditor editor, Node entityNode,
			ResourcesService resourcesService, PeopleService peopleService, AppWorkbenchService appWorkbenchService) {
		super(parent, style);
		this.editor = editor;
		this.toolkit = editor.getFormToolkit();
		this.entity = entityNode;
		this.resourcesService = resourcesService;
		this.peopleService = peopleService;
		this.appWorkbenchService = appWorkbenchService;
	}

	@Override
	public void refreshPartControl() {
		myFormPart.refresh();
		layout(true, true);
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout());
		ScrolledPage scrolled = new ScrolledPage(this, SWT.NO_FOCUS);
		scrolled.setLayoutData(EclipseUiUtils.fillAll());
		scrolled.setLayout(EclipseUiUtils.noSpaceGridLayout());
		innerCmp = new Composite(scrolled, SWT.NO_FOCUS);
		innerCmp.setLayoutData(EclipseUiUtils.fillAll());
		innerCmp.setLayout(EclipseUiUtils.noSpaceGridLayout());

		myFormPart = new ContactFormPart();
		myFormPart.initialize(editor.getManagedForm());
		editor.getManagedForm().addPart(myFormPart);
	}

	private class ContactFormPart extends AbstractFormPart {

		@Override
		public void refresh() {
			super.refresh();
			try {
				if (innerCmp.isDisposed())
					return;

				// We redraw the full part at each refresh
				CmsUtils.clear(innerCmp);

				boolean checkedOut = editor.isEditing();
				GridData gd;
				Composite newContactCmp = null;

				// Add contact tool bar.
				if (checkedOut) {
					newContactCmp = toolkit.createComposite(innerCmp, SWT.NO_FOCUS);
					newContactCmp.setLayoutData(EclipseUiUtils.fillWidth());
					populateAddContactPanel(newContactCmp);
				}

				// list existing contacts
				Composite contactListCmp = toolkit.createComposite(innerCmp, SWT.NO_FOCUS);

				gd = new GridData(SWT.FILL, SWT.FILL, true, true);
				if (checkedOut) {
					long count = getCurrentContactCount();
					if (count < 5)
						gd.heightHint = 170;
					else
						gd.heightHint = (int) (count * 30 + 20);

				}
				contactListCmp.setLayoutData(gd);

				populateDisplayContactPanel(contactListCmp, checkedOut);
				contactListCmp.layout(true);

				// notes about current contact
				Composite noteCmp = toolkit.createComposite(innerCmp, SWT.NO_FOCUS);
				gd = new GridData(SWT.FILL, SWT.BOTTOM, true, false);
				gd.heightHint = 100;
				noteCmp.setLayoutData(gd);
				populateNotePanel(noteCmp);

				// innerCmp.pack(true);
				// innerCmp.setLayoutData(gd = new GridData(SWT.FILL, SWT.FILL,
				// true, true));
				// innerCmp.layout();
				// innerCmp.getParent().pack(true);
				// innerCmp.getParent().layout(true, true);
				ContactList.this.layout(true, true);
			} catch (Exception e) {
				throw new PeopleException("unexpected error while refreshing node " + entity, e);
			}
		}
	}

	private long getCurrentContactCount() {
		long result = 0;
		try {
			if (entity.hasNode(PeopleNames.PEOPLE_CONTACTS)) {
				Node contactsPar = entity.getNode(PeopleNames.PEOPLE_CONTACTS);
				NodeIterator ni = contactsPar.getNodes();
				result = ni.getSize();
			}
		} catch (RepositoryException re) {
			throw new PeopleException("Error while conting contacts for " + entity, re);
		}
		return result;
	}

	/** Manage display and update of existing contact Nodes */
	private void populateDisplayContactPanel(final Composite parent, boolean isCheckedOut) {
		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());
		try {
			String[] knownTypes = peopleService.getContactService().getKnownContactTypes();
			if (entity.hasNode(PeopleNames.PEOPLE_CONTACTS)) {
				Node contactsPar = entity.getNode(PeopleNames.PEOPLE_CONTACTS);

				for (String currType : knownTypes) {
					NodeIterator ni = contactsPar.getNodes();
					loop: while (ni.hasNext()) {
						Node currNode = ni.nextNode();
						if (!currNode.isNodeType(currType))
							continue loop;
						if (ConnectJcrUtils.isNodeType(currNode, PeopleTypes.PEOPLE_ADDRESS))
							new ContactAddressComposite(parent, SWT.NO_FOCUS, editor, myFormPart, resourcesService,
									peopleService, appWorkbenchService, currNode, entity);
						else
							new ContactComposite(parent, SWT.NO_FOCUS, editor, myFormPart, currNode, entity,
									resourcesService, appWorkbenchService, peopleService);
					}
				}
			}
		} catch (RepositoryException e) {
			throw new PeopleException("Cannot populate existing contact list for entity " + entity, e);
		}
	}

	private void populateNotePanel(Composite parent) {
		parent.setLayout(ConnectUiUtils.noSpaceGridLayout(2));
		Label label = ConnectWorkbenchUtils.createBoldLabel(toolkit, parent, "Notes: ");

		GridData gd = new GridData(SWT.RIGHT, SWT.TOP, false, false);
		gd.verticalIndent = 3;
		label.setLayoutData(gd);

		Text notesTxt = toolkit.createText(parent, "", SWT.BORDER | SWT.MULTI | SWT.WRAP);
		notesTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		ConnectWorkbenchUtils.refreshFormTextWidget(editor, notesTxt, entity, Property.JCR_DESCRIPTION);
		ConnectWorkbenchUtils.addTxtModifyListener(myFormPart, notesTxt, entity, Property.JCR_DESCRIPTION,
				PropertyType.STRING);
	}

	/** Populate a composite that enable addition of a new contact */
	private void populateAddContactPanel(Composite parent) {
		RowLayout layout = new RowLayout();
		layout.wrap = true;
		layout.marginTop = layout.marginBottom = 0;
		parent.setLayout(layout);

		// ADD CONTACT
		addContactCmb = new Combo(parent, SWT.NONE | SWT.READ_ONLY | SWT.NO_FOCUS);

		addContactCmb.setLayoutData(new RowData(140, SWT.DEFAULT));
		addContactCmb.setItems(ContactValueCatalogs.ARRAY_CONTACT_TYPES);
		addContactCmb.add("Add a contact", 0);
		addContactCmb.select(0);

		// NATURE(work or private) is only for persons
		Combo natureCmb = ConnectJcrUtils.isNodeType(entity, PeopleTypes.PEOPLE_PERSON)
				? new Combo(parent, SWT.READ_ONLY) : null;
		if (natureCmb != null) {
			natureCmb.setLayoutData(new RowData(100, SWT.DEFAULT));
			natureCmb.setItems(ContactValueCatalogs.ARRAY_CONTACT_NATURES);
			natureCmb.setVisible(false);
		}

		// Listeners
		addContactCmb.addSelectionListener(new MySelectionAdapter(entity, parent, addContactCmb, natureCmb));

		if (natureCmb != null) {
			natureCmb.addSelectionListener(new MySelectionAdapter(entity, parent, addContactCmb, natureCmb));
		}
	}

	private class MySelectionAdapter extends SelectionAdapter {
		private static final long serialVersionUID = 1L;
		private final Node entity;
		private final Composite parent;
		private final Combo addContactCmb;
		private final Combo natureCmb;

		public MySelectionAdapter(Node entity, Composite editPanel, Combo addContactCmb, Combo natureCmb) {
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
					if (index > 0 && natureCmb.getSelectionIndex() == -1)
						natureCmb.select(0);
				}

				if (index == 0) {
					resetAddContactEditPanel(parent, addContactCmb, natureCmb);
				} else {
					String type = ContactValueCatalogs.getKeyByValue(ContactValueCatalogs.MAPS_CONTACT_TYPES,
							addContactCmb.getItem(index));
					String nature = null;
					int natureIndex = -1;
					if (natureCmb != null)
						natureIndex = natureCmb.getSelectionIndex();
					if (natureIndex != -1)
						nature = ContactValueCatalogs.ARRAY_CONTACT_NATURES[natureIndex];

					removeOtherChildren(parent, addContactCmb, natureCmb);

					Control first = populateNewContactComposite(parent, entity, type, nature, addContactCmb);
					if (first != null)
						first.setFocus();
				}
			} catch (RepositoryException e1) {
				throw new PeopleException("Unable to refresh add contact panel", e1);
			}

			parent.layout(true, true);
			Control[] controls = new Control[1];
			controls[0] = parent;
			ContactList.this.layout(controls);
		}
	}

	private void removeOtherChildren(Composite editPanel, Combo chooseTypeCmb, Combo chooseNatureCmb) {
		// remove all controls
		for (Control ctl : editPanel.getChildren()) {
			if (!(chooseTypeCmb == ctl || chooseNatureCmb != null && chooseNatureCmb == ctl)) {
				// log.debug("Disposing control " + ctl.toString());
				ctl.dispose();
			}
		}
	}

	private void resetAddContactEditPanel(Composite editPanel, Combo chooseTypeCmb, Combo chooseNatureCmb) {
		// reset combo
		if (chooseTypeCmb.getSelectionIndex() != 0)
			chooseTypeCmb.select(0);
		if (chooseNatureCmb != null)
			chooseNatureCmb.setVisible(false);
		removeOtherChildren(editPanel, chooseTypeCmb, chooseNatureCmb);
	}

	/** Populate an editable contact composite */
	private Control populateNewContactComposite(Composite parent, final Node entity, final String contactType,
			final String nature, Combo addContactCombo) throws RepositoryException {

		if (contactType.equals(PeopleTypes.PEOPLE_URL) || contactType.equals(PeopleTypes.PEOPLE_EMAIL)) {
			return createMailWidgets(parent, entity, contactType, nature, addContactCombo);
		} else if (contactType.equals(PeopleTypes.PEOPLE_ADDRESS)) {
			if (nature != null && nature.equals(ContactValueCatalogs.CONTACT_NATURE_PRO))
				return createWorkAddressWidgets(parent, entity, contactType, nature, addContactCombo);
			else
				return createAddressWidgets(parent, contactType, nature, addContactCombo);
		} else {
			return createContactWidgets(parent, contactType, nature, addContactCombo);
		}
	}

	private Control createMailWidgets(Composite parent, final Node entity, final String contactType,
			final String nature, final Combo addContactCombo) {

		final Text valueTxt = createRowDataLT(parent, "Contact value", 200);

		final Text labelTxt = createRowDataLT(parent, "Label", 120);

		final Button primaryChk = toolkit.createButton(parent, "Primary", SWT.CHECK);

		final Button validBtn = toolkit.createButton(parent, "Add", SWT.PUSH);

		validBtn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {

				String value = valueTxt.getText();
				String label = labelTxt.getText();
				boolean isPrimary = primaryChk.getSelection();
				saveAndRefresh(contactType, JcrUtils.replaceInvalidChars(value), value, isPrimary, nature, null, label);
			}
		});

		TraverseListener travList = new TraverseListener() {
			private static final long serialVersionUID = 9192624317905937169L;

			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.keyCode == SWT.CR) {
					e.doit = false;
					String value = valueTxt.getText();
					String label = labelTxt.getText();
					boolean isPrimary = primaryChk.getSelection();
					saveAndRefresh(contactType, JcrUtils.replaceInvalidChars(value), value, isPrimary, nature, null,
							label);
				}
			}
		};

		valueTxt.addTraverseListener(travList);
		labelTxt.addTraverseListener(travList);
		return valueTxt;
	}

	private Control createContactWidgets(Composite parent, final String contactType, final String nature,
			final Combo addContactCombo) throws RepositoryException {

		final Text valueTxt = createRowDataLT(parent, "Contact value", 200);

		final Combo catCmb = new Combo(parent, SWT.READ_ONLY);
		catCmb.setItems(peopleService.getContactService().getContactCategories(entity.getPrimaryNodeType().getName(),
				contactType, nature));
		catCmb.select(0);

		final Text labelTxt = createRowDataLT(parent, "Label", 120);

		final Button primaryChk = toolkit.createButton(parent, "Primary", SWT.CHECK);

		final Button validBtn = toolkit.createButton(parent, "Add", SWT.PUSH);

		validBtn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				String value = valueTxt.getText();
				String label = labelTxt.getText();
				String cat = catCmb.getText();
				boolean isPrimary = primaryChk.getSelection();
				saveAndRefresh(contactType, JcrUtils.replaceInvalidChars(value), value, isPrimary, nature, cat, label);
			}
		});

		TraverseListener travList = new TraverseListener() {
			private static final long serialVersionUID = 9192624317905937169L;

			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.keyCode == SWT.CR) {
					e.doit = false;
					String value = valueTxt.getText();
					String label = labelTxt.getText();
					String cat = catCmb.getText();
					boolean isPrimary = primaryChk.getSelection();
					saveAndRefresh(contactType, JcrUtils.replaceInvalidChars(value), value, isPrimary, nature, cat,
							label);
				}
			}
		};

		valueTxt.addTraverseListener(travList);
		labelTxt.addTraverseListener(travList);
		catCmb.addTraverseListener(travList);
		return valueTxt;
	}

	private void saveAndRefresh(String contactType, String name, String value, boolean isPrimary, String nature,
			String category, String label) {

		PeopleJcrUtils.createContact(peopleService, resourcesService, entity, contactType, name, value, isPrimary,
				nature, category, label);
		addContactCmb.select(0);
		myFormPart.markDirty();
		myFormPart.refresh();
	}

	private Control createAddressWidgets(Composite parent, final String contactType, final String nature,
			final Combo addContactCombo) {

		final Combo catCmb = new Combo(parent, SWT.NONE);
		try {
			ContactService contactService = peopleService.getContactService();
			String entityType = entity.getPrimaryNodeType().getName();
			catCmb.setItems(contactService.getContactCategories(entityType, contactType, nature));
		} catch (RepositoryException e1) {
			throw new PeopleException("unable to get category list for " + contactType + " & " + nature, e1);
		}
		catCmb.select(0);

		final Text streetTxt = createRowDataLT(parent, "Street", 150);
		final Text street2Txt = createRowDataLT(parent, "Street Complement", 150);
		final Text zipTxt = createRowDataLT(parent, "Zip code", 60);
		final Text cityTxt = createRowDataLT(parent, "City", 150);
		final Text stateTxt = createRowDataLT(parent, "State", 150);
		// Country: dropdown + text
		Text countryTxt = createRowDataLT(parent, "Country", 150);
		final TagLikeDropDown countryDD = new TagLikeDropDown(ConnectJcrUtils.getSession(entity), resourcesService,
				ConnectConstants.RESOURCE_COUNTRY, countryTxt);
		final Text geoPointTxt = createRowDataLT(parent, "Geopoint", 200);
		final Text labelTxt = createRowDataLT(parent, "Label", 120);

		final Button primaryChk = toolkit.createButton(parent, "Primary", SWT.CHECK);

		final Button validBtn = toolkit.createButton(parent, "Save", SWT.PUSH);

		validBtn.addSelectionListener(new SelectionAdapter() {

			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				String cat = catCmb.getText();
				String label = labelTxt.getText();
				boolean isPrimary = primaryChk.getSelection();

				Node node = PeopleJcrUtils.createAddress(peopleService, resourcesService, entity, streetTxt.getText(),
						street2Txt.getText(), zipTxt.getText(), cityTxt.getText(), stateTxt.getText(),
						countryDD.getText(), geoPointTxt.getText(), isPrimary, nature, cat, label);
				PeopleJcrUtils.updateDisplayAddress(node);
				myFormPart.markDirty();
				myFormPart.refresh();
			}

		});

		TraverseListener travList = new TraverseListener() {
			private static final long serialVersionUID = 9192624317905937169L;

			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.keyCode == SWT.CR) {
					e.doit = false;
					String cat = catCmb.getText();
					String label = labelTxt.getText();
					boolean isPrimary = primaryChk.getSelection();

					Node node = PeopleJcrUtils.createAddress(peopleService, resourcesService, entity,
							streetTxt.getText(), street2Txt.getText(), zipTxt.getText(), cityTxt.getText(),
							stateTxt.getText(), countryDD.getText(), geoPointTxt.getText(), isPrimary, nature, cat,
							label);
					PeopleJcrUtils.updateDisplayAddress(node);
					myFormPart.markDirty();
					myFormPart.refresh();
				}
			}
		};

		streetTxt.addTraverseListener(travList);
		street2Txt.addTraverseListener(travList);
		zipTxt.addTraverseListener(travList);
		cityTxt.addTraverseListener(travList);
		stateTxt.addTraverseListener(travList);
		countryTxt.addTraverseListener(travList);
		geoPointTxt.addTraverseListener(travList);
		labelTxt.addTraverseListener(travList);
		catCmb.addTraverseListener(travList);

		return catCmb;
	}

	private Control createWorkAddressWidgets(final Composite parent, final Node entity, final String contactType,
			final String nature, final Combo addContactCombo) {
		try {
			final Combo catCmb = new Combo(parent, SWT.NONE);
			catCmb.setItems(peopleService.getContactService()
					.getContactCategories(entity.getPrimaryNodeType().getName(), contactType, nature));
			catCmb.select(0);

			final Text valueTxt = createRowDataLT(parent, "Linked company", 200);
			CmsUtils.style(valueTxt, ConnectUiStyles.FORCE_BORDER);
			valueTxt.setEnabled(false);

			final Link chooseOrgLk = new Link(parent, SWT.BOTTOM);

			toolkit.adapt(chooseOrgLk, false, false);
			chooseOrgLk.setText("<a>Pick up</a>");
			final PickUpOrgDialog diag = new PickUpOrgDialog(chooseOrgLk.getShell(), "Choose an organisation",
					entity.getSession(), appWorkbenchService, entity);

			final Text labelTxt = createRowDataLT(parent, "A custom label", 120);

			final String PROP_SELECTED_NODE = "selectedNode";

			chooseOrgLk.addSelectionListener(new SelectionAdapter() {
				private static final long serialVersionUID = -7118320199160680131L;

				@Override
				public void widgetSelected(final SelectionEvent event) {
					diag.open();
					Node currNode = diag.getSelected();
					valueTxt.setData(PROP_SELECTED_NODE, currNode);
					if (currNode != null) {
						valueTxt.setText(ConnectJcrUtils.get(currNode, Property.JCR_TITLE));
					}
				}
			});

			final Button primaryChk = toolkit.createButton(parent, "Primary", SWT.CHECK);

			final Button validBtn = toolkit.createButton(parent, "Add", SWT.PUSH);

			validBtn.addSelectionListener(new SelectionAdapter() {
				private static final long serialVersionUID = 1L;

				@Override
				public void widgetSelected(SelectionEvent e) {
					Node selected = (Node) valueTxt.getData(PROP_SELECTED_NODE);
					String label = labelTxt.getText();
					String cat = catCmb.getText();
					boolean isPrimary = primaryChk.getSelection();
					Node node = PeopleJcrUtils.createWorkAddress(peopleService, resourcesService, entity, selected,
							isPrimary, cat, label);
					PeopleJcrUtils.updateDisplayAddress(node);
					myFormPart.markDirty();
					myFormPart.refresh();
				}
			});
			return catCmb;
		} catch (RepositoryException e1) {
			throw new PeopleException(
					"JCR Error while creating work address widgets for " + contactType + " & " + nature, e1);
		}
	}

	private Text createRowDataLT(Composite parent, String msg, int width) {
		Text text = toolkit.createText(parent, null, SWT.BORDER);
		text.setMessage(msg);
		text.setLayoutData(width == 0 ? new RowData() : new RowData(width, SWT.DEFAULT));
		return text;
	}
}