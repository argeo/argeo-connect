package org.argeo.connect.people.ui.toolkits;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.DynamicOperand;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelConstants;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;
import javax.jcr.query.qom.StaticOperand;

import org.argeo.connect.people.ContactValueCatalogs;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.PeopleImages;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.commands.AddEntityReference;
import org.argeo.connect.people.ui.commands.ForceRefresh;
import org.argeo.connect.people.ui.commands.OpenEntityEditor;
import org.argeo.connect.people.ui.composites.ContactAddressComposite;
import org.argeo.connect.people.ui.composites.ContactComposite;
import org.argeo.connect.people.ui.dialogs.PickUpOrgDialog;
import org.argeo.connect.people.ui.utils.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.PeopleJcrUtils;
import org.argeo.eclipse.ui.utils.CommandUtils;
import org.argeo.jcr.JcrUtils;
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
	private final PeopleService peopleService;

	public ContactToolkit(FormToolkit toolkit, IManagedForm form,
			PeopleService peopleService) {
		this.toolkit = toolkit;
		this.form = form;
		this.peopleService = peopleService;
	}

	/**
	 * Populate a parent composite with controls to manage mailing list
	 * membership of an organisation or a person
	 * 
	 * @param parent
	 * @param entity
	 */
	public void populateMailingListMembershipPanel(final Composite parent,
			final Node entity, final String openEditorCmdId) {
		GridLayout gl = PeopleUiUtils.gridLayoutNoBorder(2);
		gl.marginBottom = 5;
		parent.setLayout(gl);

		final Composite nlCmp = new Composite(parent, SWT.NO_FOCUS);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		nlCmp.setLayoutData(gd);

		RowLayout rl = new RowLayout(SWT.HORIZONTAL);
		rl.wrap = true;
		rl.marginHeight = 0;
		rl.marginLeft = 5;
		rl.marginRight = 0;
		nlCmp.setLayout(rl);

		final Button addBtn = new Button(parent, SWT.PUSH);
		addBtn.setText("Add new mailing list(s)");
		gd = new GridData(SWT.CENTER, SWT.TOP, false, false);
		gd.widthHint = 135;
		addBtn.setLayoutData(gd);

		AbstractFormPart editPart = new AbstractFormPart() {
			public void refresh() {
				super.refresh();
				// We redraw the full control at each refresh, might be a more
				// efficient way to do
				Control[] oldChildren = nlCmp.getChildren();
				for (Control child : oldChildren)
					child.dispose();

				try {
					List<Node> referencings = getNodeReferencing(entity);

					for (final Node node : referencings) {
						final Node parNode = getParentMailingList(node);
						Composite tagCmp = toolkit.createComposite(nlCmp,
								SWT.NO_FOCUS);
						tagCmp.setLayout(PeopleUiUtils.gridLayoutNoBorder(2));

						Link link = new Link(tagCmp, SWT.NONE);
						link.setText("<a>"
								+ CommonsJcrUtils.get(parNode,
										Property.JCR_TITLE) + "</a>");
						link.setToolTipText(CommonsJcrUtils.get(parNode,
								Property.JCR_DESCRIPTION));
						link.setData(PeopleUiConstants.MARKUP_ENABLED,
								Boolean.TRUE);

						link.addSelectionListener(new SelectionAdapter() {
							private static final long serialVersionUID = 1L;

							@Override
							public void widgetSelected(
									final SelectionEvent event) {
								Map<String, String> params = new HashMap<String, String>();
								params.put(OpenEntityEditor.PARAM_ENTITY_UID,
										CommonsJcrUtils.get(parNode,
												PeopleNames.PEOPLE_UID));
								CommandUtils.callCommand(openEditorCmdId,
										params);
							}
						});

						if (CommonsJcrUtils.isNodeCheckedOutByMe(entity)) {
							final Button deleteBtn = new Button(tagCmp,
									SWT.FLAT);
							deleteBtn.setData(PeopleUiConstants.CUSTOM_VARIANT,
									PeopleUiConstants.CSS_FLAT_IMG_BUTTON);
							deleteBtn.setImage(PeopleImages.DELETE_BTN_LEFT);

							deleteBtn
									.addSelectionListener(new SelectionAdapter() {
										private static final long serialVersionUID = 1L;

										@Override
										public void widgetSelected(
												final SelectionEvent event) {
											try {
												boolean wasCheckedOut = CommonsJcrUtils
														.isNodeCheckedOutByMe(parNode);
												if (!wasCheckedOut)
													CommonsJcrUtils
															.checkout(parNode);
												node.remove();
												if (wasCheckedOut)
													parNode.getSession().save();
												else
													CommonsJcrUtils
															.saveAndCheckin(parNode);
											} catch (RepositoryException e) {
												throw new PeopleException(
														"unable to initialise deletion",
														e);
											}
											for (IFormPart part : form
													.getParts()) {
												((AbstractFormPart) part)
														.markStale();
												part.refresh();
											}
										}
									});
						}
					}
					addBtn.setVisible(CommonsJcrUtils
							.isNodeCheckedOutByMe(entity));
					nlCmp.layout(false);
					parent.getParent().layout();
				} catch (RepositoryException re) {
					throw new PeopleException(
							"Error while refreshing mailing list appartenance",
							re);
				}
			}
		};

		addBtn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				Map<String, String> params = new HashMap<String, String>();
				try {
					params.put(AddEntityReference.PARAM_REFERENCED_JCR_ID,
							entity.getIdentifier());
					params.put(AddEntityReference.PARAM_TO_SEARCH_NODE_TYPE,
							PeopleTypes.PEOPLE_MAILING_LIST);
					params.put(AddEntityReference.PARAM_DIALOG_ID,
							PeopleUiConstants.DIALOG_ADD_ML_MEMBERSHIP);
					CommandUtils.callCommand(AddEntityReference.ID, params);
				} catch (RepositoryException e1) {
					throw new PeopleException(
							"Unable to get parent Jcr identifier", e1);
				}
			}
		});

		editPart.initialize(form);
		form.addPart(editPart);
	}

	/** Recursively retrieves the parent Mailing list **/
	private Node getParentMailingList(Node node) throws RepositoryException {
		if (node.isNodeType(PeopleTypes.PEOPLE_MAILING_LIST))
			return node;
		else
			return getParentMailingList(node.getParent());
	}

	private List<Node> getNodeReferencing(Node entity) {
		try {
			Session session = entity.getSession();
			QueryManager queryManager = session.getWorkspace()
					.getQueryManager();
			QueryObjectModelFactory qomFactory = queryManager.getQOMFactory();

			Selector source = qomFactory.selector(
					PeopleTypes.PEOPLE_MAILING_LIST_ITEM,
					PeopleTypes.PEOPLE_MAILING_LIST_ITEM);

			// Parse the String
			StaticOperand so = qomFactory.literal(entity.getProperty(
					PeopleNames.PEOPLE_UID).getValue());
			DynamicOperand dop = qomFactory.propertyValue(
					source.getSelectorName(), PeopleNames.PEOPLE_REF_UID);
			Constraint defaultC = qomFactory.comparison(dop,
					QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO, so);

			QueryObjectModel query;
			query = qomFactory.createQuery(source, defaultC, null, null);
			QueryResult result = query.execute();
			NodeIterator ni = result.getNodes();

			return JcrUtils.nodeIteratorToList(ni);
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to list entities", e);
		}
	}

	public void createContactPanelWithNotes(Composite parent, final Node entity) {
		parent.setLayout(new GridLayout());

		// Add a scrolled conatiner
		// ScrolledComposite contactListCmp = new ScrolledComposite(parent,
		// SWT.NO_FOCUS | SWT.H_SCROLL | SWT.V_SCROLL);
		// contactListCmp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
		// true));
		// // contactListCmp.setMinSize(350, 100);
		// contactListCmp.setExpandHorizontal(false);
		// contactListCmp.setExpandVertical(false);
		// contactListCmp.setLayout(PeopleUiUtils.gridLayoutNoBorder());
		// Composite innerCmp = new Composite(contactListCmp, SWT.NO_FOCUS);
		// innerCmp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		// populateDisplayContactPanel(innerCmp, entity);
		// contactListCmp.setContent(innerCmp);

		// Add contact tool bar.
		Composite newContactCmp = toolkit.createComposite(parent, SWT.NO_FOCUS);
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		newContactCmp.setLayoutData(gd);
		populateAddContactPanel(newContactCmp, entity);

		Composite contactListCmp = new Composite(parent, SWT.NO_FOCUS);
		contactListCmp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true));
		populateDisplayContactPanel(contactListCmp, entity);

		// notes about current contact
		Composite noteCmp = toolkit.createComposite(parent, SWT.NO_FOCUS);
		gd = new GridData(SWT.FILL, SWT.BOTTOM, true, false);
		gd.minimumHeight = 60;
		noteCmp.setLayoutData(gd);
		populateNotePanel(noteCmp, entity);

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
					if (entity.hasNode(PeopleNames.PEOPLE_CONTACTS)) {
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
									currCmp = new ContactAddressComposite(
											parent, SWT.NO_FOCUS, toolkit,
											form, peopleService, currNode,
											entity);
								else
									currCmp = new ContactComposite(parent,
											SWT.NO_FOCUS, toolkit, form,
											currNode, entity);
								contactCmps.put(currJcrId, currCmp);
							}
						}
					}
					// then remove necessary composites
					Session session = entity.getSession();
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
					// parent.layout();
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
		parent.setLayout(PeopleUiUtils.gridLayoutNoBorder());

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
		parent.setLayout(PeopleUiUtils.gridLayoutNoBorder(3));

		// ADD CONTACT
		final Combo addContactCmb = new Combo(parent, SWT.NONE | SWT.READ_ONLY
				| SWT.NO_FOCUS);
		GridData gd = new GridData(SWT.LEFT, SWT.TOP, false, false);
		gd.widthHint = 100;
		addContactCmb.setLayoutData(gd);
		addContactCmb.setItems(ContactValueCatalogs.ARRAY_CONTACT_TYPES);
		addContactCmb.add("Add a contact", 0);

		// NATURE(work or private) is only for persons
		final Combo natureCmb = CommonsJcrUtils.isNodeType(entity,
				PeopleTypes.PEOPLE_PERSON) ? new Combo(parent, SWT.READ_ONLY)
				: null;
		if (natureCmb != null) {
			gd = new GridData(SWT.LEFT, SWT.TOP, false, false);
			gd.widthHint = 100;
			natureCmb.setLayoutData(gd);
			natureCmb.setItems(ContactValueCatalogs.ARRAY_CONTACT_NATURES);
		}

		// NEW CONTACT info composite
		final Composite editPanel = toolkit.createComposite(parent,
				SWT.NO_FOCUS);
		editPanel
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		RowLayout layout = new RowLayout();
		layout.wrap = true;
		layout.marginTop = layout.marginBottom = 0;
		editPanel.setLayout(layout);

		AbstractFormPart editPart = new AbstractFormPart() {
			// Update values on refresh
			public void refresh() {
				super.refresh();
				editPanel.setVisible(false);

				boolean ischeckedOut = CommonsJcrUtils
						.isNodeCheckedOutByMe(entity);
				addContactCmb.setVisible(ischeckedOut);
				addContactCmb.select(0);
				if (natureCmb != null) {
					natureCmb.select(0);
					// unvisible while no contact selected
					natureCmb.setVisible(false);
				}
			}
		};

		editPart.initialize(form);
		form.addPart(editPart);

		// Listeners
		addContactCmb.addSelectionListener(new MySelectionAdapter(entity,
				editPanel, addContactCmb, natureCmb));

		if (natureCmb != null) {
			natureCmb.addSelectionListener(new MySelectionAdapter(entity,
					editPanel, addContactCmb, natureCmb));
		}

		//
		// // set default selection after everything is initialized to ease
		// layout
		resetAddContactEditPanel(editPanel, addContactCmb);
	}

	private class MySelectionAdapter extends SelectionAdapter {
		private static final long serialVersionUID = 1L;
		private final Node entity;
		private final Composite editPanel;
		private final Combo addContactCmb;
		private final Combo natureCmb;

		public MySelectionAdapter(Node entity, Composite editPanel,
				Combo addContactCmb, Combo natureCmb) {
			this.entity = entity;
			this.editPanel = editPanel;
			this.addContactCmb = addContactCmb;
			this.natureCmb = natureCmb;
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			try {
				int index = addContactCmb.getSelectionIndex();
				if (natureCmb != null) {
					natureCmb.setVisible(index != 0);
				}
				if (index == 0) {
					resetAddContactEditPanel(editPanel, addContactCmb);
				} else {
					editPanel.setVisible(true);
					String type = ContactValueCatalogs.getKeyByValue(
							ContactValueCatalogs.MAPS_CONTACT_TYPES,
							addContactCmb.getItem(index));

					String nature = natureCmb == null ? null
							: ContactValueCatalogs.ARRAY_CONTACT_NATURES[natureCmb
									.getSelectionIndex()];
					Control first = populateNewContactComposite(editPanel,
							entity, type, nature, addContactCmb);
					if (first != null)
						first.setFocus();
				}
			} catch (RepositoryException e1) {
				throw new PeopleException(
						"Unable to refresh add contact panel", e1);
			}
			// editPanel.pack();
			editPanel.layout(true);
			editPanel.getParent().getParent().layout();
		}
	}

	private void resetAddContactEditPanel(Composite editPanel,
			Combo chooseTypeCmb) {
		if (chooseTypeCmb.getSelectionIndex() != 0)
			chooseTypeCmb.select(0);
		editPanel.setVisible(false);
		// remove all controls
		for (Control ctl : editPanel.getChildren()) {
			ctl.dispose();
		}
		toolkit.createLabel(editPanel, "");
	}

	/** Populate an editable contact composite */
	public Control populateNewContactComposite(Composite parent,
			final Node entity, final String contactType, final String nature,
			Combo addContactCombo) throws RepositoryException {
		// RowData rd;
		// remove all controls
		for (Control ctl : parent.getChildren()) {
			ctl.dispose();
		}

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
				return createAddressWidgets(parent, entity, contactType,
						nature, addContactCombo);
		} else {
			return createContactWidgets(parent, entity, contactType, nature,
					addContactCombo);
		}
	}

	private Control createMailWidgets(Composite parent, final Node entity,
			final String contactType, final String nature,
			final Combo addContactCombo) {

		final Text labelTxt = createAddressTxt(true, parent, "Label", 120);

		final Text valueTxt = createAddressTxt(true, parent, "Contact value",
				200);

		final Button primaryChk = toolkit.createButton(parent, "Primary",
				SWT.CHECK);

		final Button validBtn = toolkit.createButton(parent, "Save", SWT.PUSH);

		validBtn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {

				String value = valueTxt.getText();
				String label = labelTxt.getText();
				boolean isPrimary = primaryChk.getSelection();
				// EntityPanelToolkit
				savePreservingState(entity, contactType, contactType, value,
						isPrimary, nature, null, label, addContactCombo);
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
					// EntityPanelToolkit
					savePreservingState(entity, contactType, contactType,
							value, isPrimary, nature, null, label,
							addContactCombo);
				}
			}
		});

		return labelTxt;
	}

	private Control createContactWidgets(Composite parent, final Node entity,
			final String contactType, final String nature,
			final Combo addContactCombo) {

		final Combo catCmb = new Combo(parent, SWT.READ_ONLY);
		try {
			catCmb.setItems(ContactValueCatalogs.getCategoryList(entity
					.getPrimaryNodeType().getName(), contactType, nature));
		} catch (RepositoryException e1) {
			throw new PeopleException("unable to get category list for "
					+ contactType + " & " + nature, e1);
		}
		catCmb.select(0);

		final Text labelTxt = createAddressTxt(true, parent, "Label", 120);

		final Text valueTxt = createAddressTxt(true, parent, "Contact value",
				200);

		final Button primaryChk = toolkit.createButton(parent, "Primary",
				SWT.CHECK);

		final Button validBtn = toolkit.createButton(parent, "Save", SWT.PUSH);

		validBtn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {

				String value = valueTxt.getText();
				String label = labelTxt.getText();
				String cat = catCmb.getText();

				boolean isPrimary = primaryChk.getSelection();
				// EntityPanelToolkit
				savePreservingState(entity, contactType, contactType, value,
						isPrimary, nature, cat, label, addContactCombo);
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
					// EntityPanelToolkit
					savePreservingState(entity, contactType, contactType,
							value, isPrimary, nature, cat, label,
							addContactCombo);
				}
			}
		});

		return catCmb;
	}

	private void savePreservingState(Node entity, String contactType,
			String name, String value, boolean isPrimary, String nature,
			String category, String label, Combo addContactCmb) {
		boolean wasCheckedout = CommonsJcrUtils.isNodeCheckedOut(entity);
		if (!wasCheckedout)
			CommonsJcrUtils.checkout(entity);

		PeopleJcrUtils.createContact(entity, contactType, contactType, value,
				isPrimary, nature, null, label);

		if (!wasCheckedout)
			CommonsJcrUtils.saveAndCheckin(entity);
		else
			form.dirtyStateChanged();

		CommandUtils.callCommand(ForceRefresh.ID);
		addContactCmb.select(0);
	}

	private Control createAddressWidgets(Composite parent, final Node entity,
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

		final Text labelTxt = createAddressTxt(true, parent, "Label", 120);

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

				boolean wasCheckedout = CommonsJcrUtils
						.isNodeCheckedOut(entity);
				if (!wasCheckedout)
					CommonsJcrUtils.checkout(entity);
				Node node = PeopleJcrUtils.createAddress(entity,
						streetTxt.getText(), street2Txt.getText(),
						zipTxt.getText(), cityTxt.getText(),
						stateTxt.getText(), countryTxt.getText(),
						geoPointTxt.getText(), isPrimary, nature, cat, label);
				PeopleJcrUtils.updateDisplayAddress(node);

				if (!wasCheckedout)
					CommonsJcrUtils.saveAndCheckin(entity);
				else
					form.dirtyStateChanged();

				CommandUtils.callCommand(ForceRefresh.ID);
				validBtn.getParent().setVisible(false);
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

			final Text labelTxt = createAddressTxt(true, parent, "Label", 120);

			final Link chooseOrgLk = new Link(parent, SWT.BOTTOM);
			toolkit.adapt(chooseOrgLk, false, false);
			chooseOrgLk.setText("<a>Pick up an Organisation</a>");
			final PickUpOrgDialog diag = new PickUpOrgDialog(
					chooseOrgLk.getShell(), "Choose an organisation",
					entity.getSession(), entity);

			final Text valueTxt = createAddressTxt(true, parent,
					"Contact value", 200);
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

			final Button validBtn = toolkit.createButton(parent, "Save",
					SWT.PUSH);

			validBtn.addSelectionListener(new SelectionAdapter() {
				private static final long serialVersionUID = 1L;

				@Override
				public void widgetSelected(SelectionEvent e) {

					Node selected = (Node) valueTxt.getData(PROP_SELECTED_NODE);
					String label = labelTxt.getText();
					String cat = catCmb.getText();
					boolean isPrimary = primaryChk.getSelection();

					boolean wasCheckedout = CommonsJcrUtils
							.isNodeCheckedOut(entity);
					if (!wasCheckedout)
						CommonsJcrUtils.checkout(entity);
					Node node = PeopleJcrUtils.createWorkAddress(entity,
							selected, isPrimary, cat, label);
					PeopleJcrUtils.updateDisplayAddress(node);

					if (!wasCheckedout)
						CommonsJcrUtils.saveAndCheckin(entity);
					else
						form.dirtyStateChanged();
					validBtn.getParent().setVisible(false);
					CommandUtils.callCommand(ForceRefresh.ID);
				}

			});
			return catCmb;
		} catch (RepositoryException e1) {
			throw new PeopleException(
					"JCR Error while creating work address widgets for "
							+ contactType + " & " + nature, e1);
		}

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