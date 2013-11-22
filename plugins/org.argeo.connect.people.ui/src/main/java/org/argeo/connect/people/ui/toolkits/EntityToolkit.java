package org.argeo.connect.people.ui.toolkits;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.argeo.ArgeoException;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.ui.PeopleImages;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.utils.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Centralizes creation of common controls (typically Text and composite widget)
 * for entity, to be used in various forms.
 */
public class EntityToolkit {
	// private final static Log log = LogFactory.getLog(EntityToolkit.class);

	// private final FormToolkit toolkit;
	private final IManagedForm form;

	// private AbstractEntityEditor editor;

	public EntityToolkit(FormToolkit toolkit, IManagedForm form) {
		// this.toolkit = toolkit;
		this.form = form;
	}

	// ////////////////
	// Various panels
	public void populateTagPanel(final Composite parent, final Node entity,
			final String tagPropName, final String newTagMsg) {
		parent.setLayout(PeopleUiUtils.gridLayoutNoBorder(2));

		final Composite nlCmp = new Composite(parent, SWT.NO_FOCUS);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		nlCmp.setLayoutData(gd);
		RowLayout rl = new RowLayout(SWT.HORIZONTAL);
		rl.wrap = true;
		rl.marginLeft = 5;
		rl.marginRight = 0;
		nlCmp.setLayout(rl);

		final Text tagTxt = new Text(parent, SWT.BORDER);
		tagTxt.setMessage(newTagMsg);
		gd = new GridData(SWT.CENTER, SWT.TOP, false, false);
		gd.minimumWidth = 120;
		gd.widthHint = 130;
		tagTxt.setLayoutData(gd);

		tagTxt.addTraverseListener(new TraverseListener() {
			private static final long serialVersionUID = 1L;

			public void keyTraversed(TraverseEvent e) {
				if (e.keyCode == SWT.CR) {
					String newTag = tagTxt.getText();
					addTag(entity, tagPropName, newTag);
					e.doit = false;
					tagTxt.setText("");
				}
			}
		});

		AbstractFormPart editPart = new AbstractFormPart() {
			public void refresh() {
				super.refresh();
				// We redraw the full control at each refresh, might be a more
				// efficient way to do
				Control[] oldChildren = nlCmp.getChildren();
				for (Control child : oldChildren)
					child.dispose();

				try {
					if (entity.hasProperty(tagPropName)) {
						Value[] values = entity.getProperty(tagPropName)
								.getValues();
						for (final Value value : values) {
							Link link = new Link(nlCmp, SWT.NONE);
							link.setData(RWT.CUSTOM_VARIANT, "tag");
							link.setText("#" + value.getString() + "");
							link.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);

							final Button deleteBtn = new Button(nlCmp, SWT.FLAT);
							deleteBtn.setData(RWT.CUSTOM_VARIANT,
									PeopleUiConstants.CSS_FLAT_IMG_BUTTON);
							deleteBtn.setImage(PeopleImages.DELETE_BTN_LEFT);
							RowData rd = new RowData();
							rd.height = 16;
							rd.width = 18;
							deleteBtn.setLayoutData(rd);

							deleteBtn
									.addSelectionListener(new SelectionAdapter() {
										private static final long serialVersionUID = 1L;

										@Override
										public void widgetSelected(
												final SelectionEvent event) {
											try {

												String tagToRemove = value
														.getString();
												List<String> tags = new ArrayList<String>();
												Value[] values = entity
														.getProperty(
																tagPropName)
														.getValues();
												for (int i = 0; i < values.length; i++) {
													String curr = values[i]
															.getString();
													if (!tagToRemove
															.equals(curr))
														tags.add(curr);
												}

												boolean wasCheckedOut = CommonsJcrUtils
														.isNodeCheckedOutByMe(entity);
												if (!wasCheckedOut)
													CommonsJcrUtils
															.checkout(entity);
												entity.setProperty(
														tagPropName,
														tags.toArray(new String[tags
																.size()]));
												if (wasCheckedOut)
													form.dirtyStateChanged();
												else
													CommonsJcrUtils
															.saveAndCheckin(entity);
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
						// nlCmp.pack();
						nlCmp.layout(false);
						// parent.getParent().pack();
						parent.getParent().layout();
					}
				} catch (RepositoryException re) {
					throw new PeopleException(
							"Error while refreshing mailing list appartenance",
							re);
				}
			}
		};

		editPart.initialize(form);
		form.addPart(editPart);
	}

	private void addTag(Node tagable, String tagPropName, String newTag) {
		try {
			Value[] values;
			String[] valuesStr;
			String errMsg = null;
			if (tagable.hasProperty(tagPropName)) {
				values = tagable.getProperty(tagPropName).getValues();

				// Check dupplicate
				for (Value tag : values) {
					String curTagUpperCase = tag.getString().toUpperCase()
							.trim();
					if (newTag.toUpperCase().trim().equals(curTagUpperCase)) {
						errMsg = "This tag  \"" + newTag
								+ "\" already exists as \"" + tag.getString()
								+ "\" and thus could not be added.";
						MessageDialog.openError(PeopleUiPlugin.getDefault()
								.getWorkbench().getActiveWorkbenchWindow()
								.getShell(), "Dupplicates", errMsg);
						return;
					}
				}

				valuesStr = new String[values.length + 1];
				int i;
				for (i = 0; i < values.length; i++) {
					valuesStr[i] = values[i].getString();
				}
				valuesStr[i] = newTag;
			} else {
				valuesStr = new String[1];
				valuesStr[0] = newTag;
			}

			boolean wasCheckedout = CommonsJcrUtils.isNodeCheckedOut(tagable);
			if (!wasCheckedout)
				CommonsJcrUtils.checkout(tagable);
			tagable.setProperty(tagPropName, valuesStr);
			if (!wasCheckedout)
				CommonsJcrUtils.saveAndCheckin(tagable);
			else
				form.dirtyStateChanged();

			for (IFormPart part : form.getParts()) {
				((AbstractFormPart) part).markStale();
				part.refresh();
			}
		} catch (RepositoryException re) {
			throw new ArgeoException("Unable to set tags", re);
		}

	}

	// public void createContactPanelWithNotes(Composite panel, final Node
	// entity) {
	// panel.setLayout(new GridLayout(2, true));
	//
	// final ScrolledComposite contactListCmp = new ScrolledComposite(panel,
	// SWT.NO_FOCUS | SWT.V_SCROLL | SWT.H_SCROLL);
	// contactListCmp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
	// true));
	// contactListCmp.setLayout(PeopleUiUtils.gridLayoutNoBorder());
	//
	// final Composite innerCmp = new Composite(contactListCmp, SWT.NO_FOCUS);
	// innerCmp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	// populateDisplayContactPanel(innerCmp, entity);
	// contactListCmp.setContent(innerCmp);
	//
	// final Composite rightCmp = toolkit.createComposite(panel, SWT.NO_FOCUS);
	// GridData gd = new GridData(SWT.FILL, SWT.FILL, false, true);
	// gd.widthHint = 200;
	// gd.minimumWidth = 200;
	// rightCmp.setLayoutData(gd);
	//
	// populateNotePanel(rightCmp, entity);
	//
	// final Composite newContactCmp = toolkit.createComposite(panel,
	// SWT.NO_FOCUS);
	// gd = new GridData(SWT.FILL, SWT.BOTTOM, true, false);
	// gd.horizontalSpan = 2;
	// newContactCmp.setLayoutData(gd);
	// populateAddContactPanel(newContactCmp, entity);
	// }
	//
	// /**
	// * @param panel
	// * @param entity
	// */
	// public void createContactPanel(final Composite panel, final Node entity)
	// {
	// panel.setLayout(new GridLayout());
	//
	// final ScrolledComposite contactListCmp = new ScrolledComposite(panel,
	// SWT.NO_FOCUS | SWT.V_SCROLL);
	// contactListCmp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
	// true));
	// populateDisplayContactPanel(contactListCmp, entity);
	//
	// final Composite newContactCmp = toolkit.createComposite(panel,
	// SWT.NO_FOCUS);
	// newContactCmp.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true,
	// false));
	// populateAddContactPanel(newContactCmp, entity);
	// }
	//
	// /** Manage display and update of existing contact Nodes */
	// private void populateDisplayContactPanel(final Composite panel,
	// final Node entity) {
	// panel.setLayout(PeopleUiUtils.gridLayoutNoBorder());
	//
	// final Map<String, Composite> contactCmps = new HashMap<String,
	// Composite>();
	//
	// final AbstractFormPart sPart = new AbstractFormPart() {
	// public void refresh() {
	// try {
	// super.refresh();
	// // first: initialise composite for new contacts
	// Node contactsPar = entity
	// .getNode(PeopleNames.PEOPLE_CONTACTS);
	// NodeIterator ni = contactsPar.getNodes();
	// while (ni.hasNext()) {
	// Node currNode = ni.nextNode();
	// String currJcrId = currNode.getIdentifier();
	// if (!contactCmps.containsKey(currJcrId)) {
	// Composite currCmp = new Composite(panel,
	// SWT.NO_FOCUS);
	// populateEditableMailCmp(currCmp, currNode, currNode
	// .getParent().getParent());
	// contactCmps.put(currJcrId, currCmp);
	// }
	// }
	//
	// // then remove necessary composites
	// Session session = contactsPar.getSession();
	// for (String jcrId : contactCmps.keySet()) {
	// // TODO: enhance this
	// Composite currCmp = contactCmps.get(jcrId);
	// try {
	// session.getNodeByIdentifier(jcrId);
	// } catch (ItemNotFoundException infe) {
	// currCmp.dispose();
	// }
	// }
	// } catch (RepositoryException e) {
	// throw new PeopleException(
	// "Cannot refresh contact panel formPart", e);
	// }
	// }
	// };
	//
	// sPart.initialize(form);
	// form.addPart(sPart);
	// }
	//
	// private void populateEditableMailCmp(final Composite parent,
	// final Node contactNode, final Node parVersionableNode)
	// throws RepositoryException {
	// RowLayout rl = new RowLayout(SWT.WRAP);
	// rl.type = SWT.HORIZONTAL;
	// parent.setLayout(rl);
	// // String type = CommonsJcrUtils.getStringValue(contactNode,
	// // PeopleNames.PEOPLE_CONTACT_LABEL);
	// // if (CommonsJcrUtils.checkNotEmptyString(type))
	// // type = CommonsJcrUtils.get(contactNode,
	// // PeopleNames.PEOPLE_CONTACT_CATEGORY);
	//
	// // final Button categoryBtn =
	// createCategoryButton(parent, contactNode);
	// final Button primaryBtn = createPrimaryButton(parent);
	// final Button deleteBtn = createDeleteButton(parent);
	//
	// final Text valueTxt = createAddressTxt(
	// !contactNode.isNodeType(PeopleTypes.PEOPLE_ADDRESS), parent,
	// "Value", 150);
	// final Text labelTxt = createAddressTxt(true, parent, "", 120);
	//
	// final AbstractFormPart sPart = new AbstractFormPart() {
	//
	// @Override
	// public void commit(boolean onSave) {
	// try {
	// if (onSave
	// && contactNode
	// .isNodeType(PeopleTypes.PEOPLE_ADDRESS))
	// PeopleJcrUtils.updateDisplayAddress(contactNode);
	// super.commit(onSave);
	// } catch (RepositoryException e) {
	// throw new PeopleException(
	// "unable to update display address", e);
	// }
	// }
	//
	// public void refresh() {
	// super.refresh();
	// try {
	// boolean isCheckedOut = CommonsJcrUtils
	// .isNodeCheckedOutByMe(parVersionableNode);
	// boolean isPrimary = contactNode.getProperty(
	// PeopleNames.PEOPLE_IS_PRIMARY).getBoolean();
	// if (isPrimary)
	// primaryBtn.setImage(PeopleImages.PRIMARY_BTN);
	// else
	// primaryBtn.setImage(PeopleImages.PRIMARY_NOT_BTN);
	//
	// final AbstractFormPart afp = this;
	// if (contactNode.isNodeType(PeopleTypes.PEOPLE_ADDRESS)) {
	// populateAdresseCmp(parent, contactNode, afp);
	// } else {
	// String label = CommonsJcrUtils.get(contactNode,
	// PeopleNames.PEOPLE_CONTACT_LABEL);
	// labelTxt.setText(label);
	// labelTxt.setEnabled(isCheckedOut);
	// labelTxt.setMessage(isCheckedOut ? "Label" : "");
	// valueTxt.setText(contactNode.getProperty(
	// PeopleNames.PEOPLE_CONTACT_VALUE).getString());
	// valueTxt.setEnabled(isCheckedOut);
	// String nature = CommonsJcrUtils.get(contactNode,
	// PeopleNames.PEOPLE_CONTACT_LABEL);
	// String category = CommonsJcrUtils.get(contactNode,
	// PeopleNames.PEOPLE_CONTACT_CATEGORY);
	// String toolTip = nature + " " + category;
	// if (CommonsJcrUtils.checkNotEmptyString(toolTip))
	// valueTxt.setToolTipText(toolTip);
	// }
	// Composite cmp2 = parent.getParent();
	// cmp2.pack();
	// cmp2.getParent().layout(true);
	// } catch (Exception e) {
	// if (e instanceof InvalidItemStateException)
	// // TODO clean: this exception normally means node
	// // has already been removed.
	// ;
	// else
	// throw new PeopleException(
	// "unexpected error while refreshing", e);
	// }
	// }
	// };
	//
	// if (!contactNode.isNodeType(PeopleTypes.PEOPLE_ADDRESS)) {
	// PeopleUiUtils.addTxtModifyListener(sPart, valueTxt, contactNode,
	// PeopleNames.PEOPLE_CONTACT_VALUE, PropertyType.STRING);
	// PeopleUiUtils.addTxtModifyListener(sPart, labelTxt, contactNode,
	// PeopleNames.PEOPLE_CONTACT_LABEL, PropertyType.STRING);
	// }
	// sPart.refresh();
	// sPart.initialize(form);
	// form.addPart(sPart);
	// configureDeleteButton(deleteBtn, contactNode, parVersionableNode);
	// configurePrimaryButton(primaryBtn, contactNode, parVersionableNode);
	// }
	//
	// private void populateAdresseCmp(Composite parent, Node contactNode,
	// AbstractFormPart part) throws RepositoryException {
	// boolean isCheckedOut = CommonsJcrUtils
	// .isNodeCheckedOutByMe(contactNode);
	//
	// for (Control control : parent.getChildren()) {
	// if (!(control instanceof Button))
	// control.dispose();
	// }
	//
	// if (isCheckedOut) {
	// // specific for addresses
	// final Text streetTxt = createAddressTxt(
	// contactNode.isNodeType(PeopleTypes.PEOPLE_ADDRESS), parent,
	// "Street", 0);
	// final Text street2Txt = createAddressTxt(
	// contactNode.isNodeType(PeopleTypes.PEOPLE_ADDRESS), parent,
	// "Street Complement", 0);
	// final Text zipTxt = createAddressTxt(
	// contactNode.isNodeType(PeopleTypes.PEOPLE_ADDRESS), parent,
	// "Zip code", 0);
	// final Text cityTxt = createAddressTxt(
	// contactNode.isNodeType(PeopleTypes.PEOPLE_ADDRESS), parent,
	// "City", 0);
	// final Text stateTxt = createAddressTxt(
	// contactNode.isNodeType(PeopleTypes.PEOPLE_ADDRESS), parent,
	// "State", 0);
	// final Text countryTxt = createAddressTxt(
	// contactNode.isNodeType(PeopleTypes.PEOPLE_ADDRESS), parent,
	// "Country", 0);
	// final Text geoPointTxt = createAddressTxt(
	// contactNode.isNodeType(PeopleTypes.PEOPLE_ADDRESS), parent,
	// "Geopoint", 0);
	//
	// streetTxt.setText(CommonsJcrUtils.get(contactNode,
	// PeopleNames.PEOPLE_STREET));
	// streetTxt.setEnabled(isCheckedOut);
	// streetTxt.setMessage(isCheckedOut ? "Street" : "");
	// street2Txt.setText(CommonsJcrUtils.get(contactNode,
	// PeopleNames.PEOPLE_STREET_COMPLEMENT));
	// street2Txt.setEnabled(isCheckedOut);
	// street2Txt.setMessage(isCheckedOut ? "Street complement" : "");
	// zipTxt.setText(CommonsJcrUtils.get(contactNode,
	// PeopleNames.PEOPLE_ZIP_CODE));
	// zipTxt.setEnabled(isCheckedOut);
	// zipTxt.setMessage(isCheckedOut ? "Zip code" : "");
	// cityTxt.setText(CommonsJcrUtils.get(contactNode,
	// PeopleNames.PEOPLE_CITY));
	// cityTxt.setEnabled(isCheckedOut);
	// cityTxt.setMessage(isCheckedOut ? "City" : "");
	// stateTxt.setText(CommonsJcrUtils.get(contactNode,
	// PeopleNames.PEOPLE_STATE));
	// stateTxt.setEnabled(isCheckedOut);
	// stateTxt.setMessage(isCheckedOut ? "State" : "");
	// countryTxt.setText(CommonsJcrUtils.get(contactNode,
	// PeopleNames.PEOPLE_COUNTRY));
	// countryTxt.setEnabled(isCheckedOut);
	// countryTxt.setMessage(isCheckedOut ? "Country" : "");
	// geoPointTxt.setText(CommonsJcrUtils.get(contactNode,
	// PeopleNames.PEOPLE_GEOPOINT));
	// geoPointTxt.setEnabled(isCheckedOut);
	// geoPointTxt.setMessage(isCheckedOut ? "Geo point" : "");
	//
	// addAddressTxtModifyListener(part, streetTxt, contactNode,
	// PeopleNames.PEOPLE_STREET, PropertyType.STRING);
	// addAddressTxtModifyListener(part, street2Txt, contactNode,
	// PeopleNames.PEOPLE_STREET_COMPLEMENT, PropertyType.STRING);
	// addAddressTxtModifyListener(part, zipTxt, contactNode,
	// PeopleNames.PEOPLE_ZIP_CODE, PropertyType.STRING);
	// addAddressTxtModifyListener(part, cityTxt, contactNode,
	// PeopleNames.PEOPLE_CITY, PropertyType.STRING);
	// addAddressTxtModifyListener(part, stateTxt, contactNode,
	// PeopleNames.PEOPLE_STATE, PropertyType.STRING);
	// addAddressTxtModifyListener(part, countryTxt, contactNode,
	// PeopleNames.PEOPLE_COUNTRY, PropertyType.STRING);
	// PeopleUiUtils.addTxtModifyListener(part, geoPointTxt, contactNode,
	// PeopleNames.PEOPLE_GEOPOINT, PropertyType.STRING);
	// } else {
	// // READ ONLY
	// Text text = toolkit.createText(parent, null, SWT.BORDER);
	// RowData rd = new RowData();
	// text.setLayoutData(rd);
	// text.setText(CommonsJcrUtils.get(contactNode,
	// PeopleNames.PEOPLE_CONTACT_VALUE));
	// text.setEnabled(false);
	// // text.pack();
	// // text.getParent().getParent().layout(true);
	// }
	// parent.layout();
	// }
	//
	// private void addAddressTxtModifyListener(final AbstractFormPart part,
	// final Text text, final Node entity, final String propName,
	// final int propType) {
	// text.addModifyListener(new ModifyListener() {
	// private static final long serialVersionUID = 1549789407363632491L;
	//
	// @Override
	// public void modifyText(ModifyEvent event) {
	// if (JcrUiUtils.setJcrProperty(entity, propName, propType,
	// text.getText())) {
	// part.markDirty();
	// PeopleJcrUtils.updateDisplayAddress(entity);
	// }
	// }
	// });
	// }
	//
	// private Button createCategoryButton(Composite parent, Node contactNode) {
	// Button btn = new Button(parent, SWT.FLAT);
	// btn.setData(RWT.CUSTOM_VARIANT, PeopleUiConstants.CSS_FLAT_IMG_BUTTON);
	//
	// try {
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
	// btn.setImage(ContactImages.getImage(entityType, contactType,
	// nature, category));
	//
	// RowData rd = new RowData();
	// rd.height = 16;
	// rd.width = 16;
	// btn.setLayoutData(rd);
	// return btn;
	// } catch (RepositoryException re) {
	// throw new PeopleException("unable to get image for contact");
	// }
	// }
	//
	// private Button createDeleteButton(Composite parent) {
	// Button btn = new Button(parent, SWT.FLAT);
	// btn.setData(RWT.CUSTOM_VARIANT, PeopleUiConstants.CSS_FLAT_IMG_BUTTON);
	// btn.setImage(PeopleImages.DELETE_BTN);
	// RowData rd = new RowData();
	// rd.height = 16;
	// rd.width = 16;
	// btn.setLayoutData(rd);
	// return btn;
	// }
	//
	// private Button createPrimaryButton(Composite parent) {
	// Button btn = new Button(parent, SWT.FLAT);
	// btn.setData(RWT.CUSTOM_VARIANT, PeopleUiConstants.CSS_FLAT_IMG_BUTTON);
	// btn.setImage(PeopleImages.PRIMARY_NOT_BTN);
	// RowData rd = new RowData();
	// rd.height = 16;
	// rd.width = 16;
	// btn.setLayoutData(rd);
	// return btn;
	// }
	//
	// private void configureDeleteButton(Button btn, final Node node,
	// final Node parNode) { // , final AbstractFormPart
	// // genericContactFormPart
	// btn.addSelectionListener(new SelectionAdapter() {
	// private static final long serialVersionUID = 1L;
	//
	// @Override
	// public void widgetSelected(final SelectionEvent event) {
	// try {
	// boolean wasCheckedOut = CommonsJcrUtils
	// .isNodeCheckedOutByMe(parNode);
	// if (!wasCheckedOut)
	// CommonsJcrUtils.checkout(parNode);
	// node.remove();
	// if (wasCheckedOut)
	// parNode.getSession().save();
	// else
	// CommonsJcrUtils.saveAndCheckin(parNode);
	// } catch (RepositoryException e) {
	// throw new PeopleException("unable to initialise deletion",
	// e);
	// }
	// for (IFormPart part : form.getParts()) {
	// ((AbstractFormPart) part).markStale();
	// part.refresh();
	// }
	// }
	// });
	// }
	//
	// private void configurePrimaryButton(Button btn, final Node node,
	// final Node parNode) {
	// btn.addSelectionListener(new SelectionAdapter() {
	// private static final long serialVersionUID = 1L;
	//
	// @Override
	// public void widgetSelected(final SelectionEvent event) {
	// try {
	// boolean wasCheckedOut = CommonsJcrUtils
	// .isNodeCheckedOutByMe(parNode);
	// if (!wasCheckedOut)
	// CommonsJcrUtils.checkout(parNode);
	// boolean wasPrimary = false;
	// if (node.hasProperty(PeopleNames.PEOPLE_IS_PRIMARY)
	// && node.getProperty(PeopleNames.PEOPLE_IS_PRIMARY)
	// .getBoolean())
	// wasPrimary = true;
	// PeopleJcrUtils.markAsPrimary(node, !wasPrimary);
	// if (wasCheckedOut)
	// parNode.getSession().save();
	// else
	// CommonsJcrUtils.saveAndCheckin(parNode);
	// for (IFormPart part : form.getParts()) {
	// ((AbstractFormPart) part).markStale();
	// part.refresh();
	// }
	// } catch (RepositoryException e) {
	// throw new PeopleException("unable to initialise deletion",
	// e);
	// }
	// }
	// });
	// }
	//
	// public void populateNotePanel(final Composite parent, final Node entity)
	// {
	// parent.setLayout(new GridLayout());
	//
	// parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	// toolkit.createLabel(parent, "Notes: ", SWT.NONE);
	//
	// final Text notesTxt = toolkit.createText(parent, "", SWT.BORDER
	// | SWT.MULTI | SWT.WRAP);
	//
	// GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
	// gd.minimumWidth = 200;
	// gd.minimumHeight = 200;
	// notesTxt.setLayoutData(gd);
	//
	// AbstractFormPart notePart = new AbstractFormPart() {
	// public void refresh() {
	// super.refresh();
	// String desc = CommonsJcrUtils.getStringValue(entity,
	// Property.JCR_DESCRIPTION);
	// if (desc != null)
	// notesTxt.setText(desc);
	// notesTxt.setEnabled(CommonsJcrUtils
	// .isNodeCheckedOutByMe(entity));
	// parent.layout();
	// }
	// };
	//
	// PeopleUiUtils.addModifyListener(notesTxt, entity,
	// Property.JCR_DESCRIPTION, notePart);
	//
	// notePart.initialize(form);
	// form.addPart(notePart);
	// }
	//
	// /** Populate a composite that enable addition of a new contact */
	// public void populateAddContactPanel(Composite parent, final Node entity)
	// {
	// parent.setLayout(new GridLayout(2, false));
	//
	// final Combo addContactCmb = new Combo(parent, SWT.NONE | SWT.READ_ONLY
	// | SWT.NO_FOCUS);
	// GridData gd = new GridData(SWT.LEFT, SWT.TOP, false, false);
	// gd.widthHint = 100;
	// addContactCmb.setLayoutData(gd);
	// addContactCmb.setItems(ContactValueCatalogs.ARRAY_CONTACT_TYPES);
	// // Add a default value
	// addContactCmb.add("Add a contact", 0);
	// addContactCmb.select(0);
	//
	// final Composite editPanel = toolkit.createComposite(parent,
	// SWT.NO_FOCUS);
	// gd = new GridData(GridData.FILL_HORIZONTAL);
	// gd.grabExcessHorizontalSpace = true;
	// editPanel.setLayoutData(gd);
	//
	// editPanel.setVisible(false);
	//
	// AbstractFormPart editPart = new AbstractFormPart() {
	// // Update values on refresh
	// public void refresh() {
	// super.refresh();
	// editPanel.setVisible(false);
	// addContactCmb.select(0);
	// }
	// };
	//
	// editPart.initialize(form);
	// form.addPart(editPart);
	// // show the edit new contact panel when selection change
	// addContactCmb.addSelectionListener(new SelectionListener() {
	// private static final long serialVersionUID = 1L;
	//
	// @Override
	// public void widgetSelected(SelectionEvent e) {
	// try {
	//
	// int index = addContactCmb.getSelectionIndex();
	// if (index == 0)
	// editPanel.setVisible(false);
	// else {
	// editPanel.setVisible(true);
	// String selected = addContactCmb.getItem(index);
	// Control first;
	// first = populateNewContactComposite(
	// editPanel,
	// entity,
	// ContactValueCatalogs
	// .getKeyByValue(
	// ContactValueCatalogs.MAPS_CONTACT_TYPES,
	// selected));
	// if (first != null)
	// first.setFocus();
	// }
	// } catch (RepositoryException e1) {
	// throw new PeopleException(
	// "Unable to refresh add contact panel", e1);
	// }
	// editPanel.pack();
	// editPanel.getParent().getParent().layout();
	// }
	//
	// @Override
	// public void widgetDefaultSelected(SelectionEvent e) {
	// }
	// });
	//
	// }
	//
	// /** Populate an editable contact composite */
	// public Control populateNewContactComposite(Composite parent,
	// final Node entity, final String contactType)
	// throws RepositoryException {
	// RowData rd;
	//
	// if (parent.getLayout() == null) {
	// RowLayout layout = new RowLayout();
	// // Optionally set layout fields.
	// layout.wrap = true;
	// layout.marginTop = 0;
	// // Set the layout into the composite.
	// parent.setLayout(layout);
	// }
	//
	// // remove all controls
	// for (Control ctl : parent.getChildren()) {
	// ctl.dispose();
	// }
	//
	// // Nature (work or private) is only for persons
	// final Combo natureCmb = entity.isNodeType(PeopleTypes.PEOPLE_PERSON) ?
	// new Combo(
	// parent, SWT.NONE) : null;
	//
	// // No category for emails and web sites.
	// final Combo catCmb = !(contactType.equals(PeopleTypes.PEOPLE_URL) ||
	// contactType
	// .equals(PeopleTypes.PEOPLE_EMAIL)) ? new Combo(parent, SWT.NONE)
	// : null;
	//
	// final Text labelTxt = new Text(parent, SWT.BORDER);
	//
	// // For all contact
	// final Text valueTxt = createAddressTxt(
	// !contactType.equals(PeopleTypes.PEOPLE_ADDRESS), parent,
	// "Contact value", 200);
	//
	// // specific for addresses
	// final Text streetTxt = createAddressTxt(
	// contactType.equals(PeopleTypes.PEOPLE_ADDRESS), parent,
	// "Street", 150);
	// final Text street2Txt = createAddressTxt(
	// contactType.equals(PeopleTypes.PEOPLE_ADDRESS), parent,
	// "Street Complement", 150);
	// final Text zipTxt = createAddressTxt(
	// contactType.equals(PeopleTypes.PEOPLE_ADDRESS), parent,
	// "Zip code", 60);
	// final Text cityTxt = createAddressTxt(
	// contactType.equals(PeopleTypes.PEOPLE_ADDRESS), parent, "City",
	// 150);
	// final Text stateTxt = createAddressTxt(
	// contactType.equals(PeopleTypes.PEOPLE_ADDRESS), parent,
	// "State", 150);
	// final Text countryTxt = createAddressTxt(
	// contactType.equals(PeopleTypes.PEOPLE_ADDRESS), parent,
	// "Country", 150);
	// final Text geoPointTxt = createAddressTxt(
	// contactType.equals(PeopleTypes.PEOPLE_ADDRESS), parent,
	// "Geopoint", 200);
	//
	// labelTxt.setMessage("Label");
	// rd = new RowData(120, SWT.DEFAULT);
	// labelTxt.setLayoutData(rd);
	//
	// final Button primaryChk = toolkit.createButton(parent, "Primary",
	// SWT.CHECK);
	//
	// final Button validBtn = toolkit.createButton(parent, "Save", SWT.PUSH);
	//
	// if (natureCmb != null) {
	// natureCmb.setItems(ContactValueCatalogs.ARRAY_CONTACT_NATURES);
	// natureCmb.select(0);
	// natureCmb.addSelectionListener(new SelectionListener() {
	// private static final long serialVersionUID = 1L;
	//
	// @Override
	// public void widgetSelected(SelectionEvent e) {
	// try {
	// if (catCmb != null) {
	// catCmb.setItems(ContactValueCatalogs
	// .getCategoryList(entity
	// .getPrimaryNodeType().getName(),
	// contactType, natureCmb.getText()));
	// catCmb.select(0);
	// }
	// } catch (RepositoryException e1) {
	// throw new PeopleException(
	// "unable to retrieve category list", e1);
	// }
	// }
	//
	// @Override
	// public void widgetDefaultSelected(SelectionEvent e) {
	// }
	// });
	// }
	//
	// if (catCmb != null) {
	// catCmb.setItems(ContactValueCatalogs.getCategoryList(entity
	// .getPrimaryNodeType().getName(), contactType, null));
	// catCmb.select(0);
	// }
	//
	// validBtn.addSelectionListener(new SelectionListener() {
	// private static final long serialVersionUID = 1L;
	//
	// @Override
	// public void widgetSelected(SelectionEvent e) {
	//
	// String nature = natureCmb == null ? null : natureCmb.getText();
	// String value = valueTxt == null ? null : valueTxt.getText();
	// String cat = catCmb == null ? null : catCmb.getText();
	// String label = labelTxt.getText();
	// boolean isPrimary = primaryChk.getSelection();
	// // EntityPanelToolkit
	// boolean wasCheckedout = CommonsJcrUtils
	// .isNodeCheckedOut(entity);
	// if (!wasCheckedout)
	// CommonsJcrUtils.checkout(entity);
	// if (contactType.equals(PeopleTypes.PEOPLE_ADDRESS)) {
	// Node node = PeopleJcrUtils.createAddress(entity,
	// streetTxt.getText(), street2Txt.getText(),
	// zipTxt.getText(), cityTxt.getText(),
	// stateTxt.getText(), countryTxt.getText(),
	// geoPointTxt.getText(), isPrimary, nature, cat,
	// label);
	// PeopleJcrUtils.updateDisplayAddress(node);
	// } else
	// PeopleJcrUtils.createContact(entity, contactType,
	// contactType, value, isPrimary, nature, cat, label);
	//
	// if (!wasCheckedout)
	// CommonsJcrUtils.saveAndCheckin(entity);
	// else
	// form.dirtyStateChanged();
	// for (IFormPart part : form.getParts()) {
	// ((AbstractFormPart) part).markStale();
	// part.refresh();
	// }
	// validBtn.getParent().setVisible(false);
	// }
	//
	// @Override
	// public void widgetDefaultSelected(SelectionEvent e) {
	// }
	// });
	//
	// if (natureCmb != null)
	// return natureCmb;
	// else if (catCmb != null)
	// return catCmb;
	// else
	// return valueTxt;
	// }

	// private Text createAddressTxt(boolean create, Composite parent, String
	// msg,
	// int width) {
	// if (create) {
	// Text text = toolkit.createText(parent, null, SWT.BORDER);
	// text.setMessage(msg);
	// text.setLayoutData(width == 0 ? new RowData() : new RowData(width,
	// SWT.DEFAULT));
	// return text;
	// } else
	// return null;
	// }

	// public void populateTagsPanel(final Composite parent, final Node entity)
	// {
	// parent.setLayout(new FormLayout());
	// // Show only TAGS for the time being, so it is the same for R/O & Edit
	// // mode
	// final Composite panel = toolkit.createComposite(parent, SWT.NO_FOCUS);
	// PeopleUiUtils.setSwitchingFormData(panel);
	//
	// TableColumnLayout tableColumnLayout = new TableColumnLayout();
	// panel.setLayout(tableColumnLayout);
	//
	// int style = SWT.NO_SCROLL;
	// Table table = new Table(panel, style);
	// table.setLinesVisible(false);
	// table.setHeaderVisible(false);
	// // Enable markups
	// table.setData(RWT.CUSTOM_VARIANT,
	// PeopleUiConstants.CSS_STYLE_UNIQUE_CELL_TABLE);
	// table.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
	// table.setData(RWT.CUSTOM_ITEM_HEIGHT, Integer.valueOf(40));
	//
	// // Does not work: adding a tag within the <a> tag unvalid the
	// // target="_RWT" parameter
	// // ResourceManager resourceManager = RWT.getResourceManager();
	// // if (!resourceManager.isRegistered("icons/close.png")) {
	// // InputStream inputStream = this.getClass().getClassLoader()
	// // .getResourceAsStream("icons/close.png");
	// // try {
	// // resourceManager.register("icons/close.png", inputStream);
	// // } finally {
	// // IOUtils.closeQuietly(inputStream);
	// // }
	// // }
	// // final String src = RWT.getResourceManager().getLocation(
	// // "icons/close.png");
	//
	// final TableViewer viewer = new TableViewer(table);
	// viewer.setLabelProvider(new LabelProvider() {
	// private static final long serialVersionUID = 1L;
	//
	// @Override
	// public String getText(Object element) {
	// try {
	// Node node = (Node) element;
	// StringBuilder tags = new StringBuilder();
	// if (node.hasProperty(PeopleNames.PEOPLE_TAGS)) {
	// tags.append("<span style=\"font-size:15px;float:left;padding:0px;white-space:pre-wrap;text-decoration:none;\">");
	// Value[] values = entity.getProperty(
	// PeopleNames.PEOPLE_TAGS).getValues();
	// for (int i = 0; i < values.length; i++) {
	// String currStr = PeopleHtmlUtils
	// .cleanHtmlString(values[i].getString());
	//
	// tags.append("<i>#");
	// tags.append(currStr).append("&#160;");
	// tags.append("<small><a style=\"text-decoration:none;\" href=\"");
	// tags.append(currStr);
	// tags.append("\" target=\"_rwt\">X</a></small></i>")
	// .append("&#160;&#160; ");
	// }
	// tags.append("</span>");
	// }
	// return tags.toString();
	// } catch (RepositoryException re) {
	// throw new PeopleException("unable to get tags", re);
	// }
	// }
	//
	// });
	// viewer.setContentProvider(new BasicNodeListContentProvider());
	//
	// TableColumn singleColumn = new TableColumn(table, SWT.LEFT);
	// singleColumn.setData(RWT.CUSTOM_VARIANT,
	// PeopleUiConstants.CSS_STYLE_UNIQUE_CELL_TABLE);
	//
	// tableColumnLayout.setColumnData(singleColumn, new ColumnWeightData(90));
	//
	// final EntityAbstractFormPart editPart = new EntityAbstractFormPart() {
	// public void refresh() { // update display value
	// super.refresh();
	// List<Node> nodes = new ArrayList<Node>();
	// nodes.add(entity);
	// viewer.refresh();
	// }
	// };
	//
	// table.addSelectionListener(new SelectionAdapter() {
	// private static final long serialVersionUID = 1L;
	//
	// public void widgetSelected(SelectionEvent event) {
	// if (event.detail == RWT.HYPERLINK) {
	// try {
	//
	// String tagToRemove = event.text;
	// if (CommonsJcrUtils.checkNotEmptyString(tagToRemove)) {
	// List<String> tags = new ArrayList<String>();
	// if (entity.hasProperty(PeopleNames.PEOPLE_TAGS)) {
	// Value[] values = entity.getProperty(
	// PeopleNames.PEOPLE_TAGS).getValues();
	// for (int i = 0; i < values.length; i++) {
	// String curr = values[i].getString();
	// if (!tagToRemove.equals(curr))
	// tags.add(curr);
	// }
	// }
	// boolean wasCheckedout = CommonsJcrUtils
	// .isNodeCheckedOut(entity);
	// if (!wasCheckedout)
	// CommonsJcrUtils.checkout(entity);
	// entity.setProperty(PeopleNames.PEOPLE_TAGS,
	// tags.toArray(new String[tags.size()]));
	// if (!wasCheckedout)
	// CommonsJcrUtils.saveAndCheckin(entity);
	// else
	// form.dirtyStateChanged();
	// }
	// // Not enough we want to refresh the all form.
	// // editPart.refresh();
	// for (IFormPart part : form.getParts()) {
	// ((AbstractFormPart) part).markStale();
	// part.refresh();
	// }
	// } catch (RepositoryException re) {
	// throw new ArgeoException("Unable to set tags", re);
	// }
	// }
	// }
	// });
	// List<Node> nodes = new ArrayList<Node>();
	// nodes.add(entity);
	// viewer.setInput(nodes);
	//
	// editPart.initialize(form);
	// form.addPart(editPart);
	// }

	// public void populateAddTagComposite(Composite parent, final Node entity)
	// {
	// parent.setLayout(new RowLayout());
	// final Text tagTxt = new Text(parent, SWT.BORDER);
	// tagTxt.setMessage("Enter a new tag");
	// RowData rd = new RowData(120, SWT.DEFAULT);
	// tagTxt.setLayoutData(rd);
	//
	// tagTxt.addTraverseListener(new TraverseListener() {
	// private static final long serialVersionUID = 1L;
	//
	// public void keyTraversed(TraverseEvent e) {
	// if (e.keyCode == SWT.CR) {
	// String newTag = tagTxt.getText();
	// addTag(entity, newTag);
	// e.doit = false;
	// tagTxt.setText("");
	// }
	// }
	// });
	// }

}