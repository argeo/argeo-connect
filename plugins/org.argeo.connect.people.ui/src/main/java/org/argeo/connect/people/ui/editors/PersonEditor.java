package org.argeo.connect.people.ui.editors;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.PersonJcrUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

// import org.eclipse.rap.rwt.RWT;

/**
 * Sample editor page that display reference controls and manage life cycle of a
 * given Node
 */
public class PersonEditor extends AbstractEntityEditor {
	final static Log log = LogFactory.getLog(PersonEditor.class);

	// local constants
	public final static String ID = PeopleUiPlugin.PLUGIN_ID + ".personEditor";
	// Main business Objects
	private Node person;

	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		person = getNode();
		setPartName(JcrUtils.getStringPropertyQuietly(person,
				PeopleNames.PEOPLE_LAST_NAME));
	}

	@Override
	protected void createMainInfoSection(final Composite parent) {
		parent.setLayout(new GridLayout());
		Composite buttons = toolkit.createComposite(parent, SWT.NO_FOCUS);
		buttons.setLayout(new GridLayout());
		Button switchBtn = toolkit.createButton(buttons, "Switch", SWT.PUSH
				| SWT.RIGHT);
		Composite switchingPanel = toolkit
				.createComposite(parent, SWT.NO_FOCUS);
		switchingPanel.setLayout(new FormLayout());

		// READ ONLY
		final Composite mainInfoCmpRO = toolkit.createComposite(switchingPanel,
				SWT.NO_FOCUS);
		FormData fdLabel = new FormData();
		fdLabel.top = new FormAttachment(0, 0);
		fdLabel.left = new FormAttachment(0, 0);
		fdLabel.right = new FormAttachment(100, 0);
		fdLabel.bottom = new FormAttachment(100, 0);
		mainInfoCmpRO.setLayoutData(fdLabel);
		mainInfoCmpRO.setData(RWT.CUSTOM_VARIANT,
				PeopleUiConstants.PEOPLE_CSS_GENERALINFO_COMPOSITE);

		// Intern layout
		mainInfoCmpRO.setLayout(new GridLayout(2, false));
		// display name
		final Label displayNameROLbl = toolkit.createLabel(mainInfoCmpRO, "",
				SWT.NONE);

		displayNameROLbl.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true,
				false, 2, 1));
		displayNameROLbl.setData(RWT.CUSTOM_VARIANT,
				PeopleUiConstants.PEOPLE_CSS_GENERALINFO_TITLE);
		// tags
		final Label tagsROLbl = toolkit
				.createLabel(mainInfoCmpRO, "", SWT.WRAP);
		tagsROLbl.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false, 2,
				1));
		tagsROLbl.setData(RWT.CUSTOM_VARIANT,
				PeopleUiConstants.PEOPLE_CSS_GENERALINFO_TAGS);

		// EDIT
		final Composite mainInfoCmpEdit = toolkit.createComposite(
				switchingPanel, SWT.NO_FOCUS);
		fdLabel = new FormData();
		fdLabel.top = new FormAttachment(0, 0);
		fdLabel.left = new FormAttachment(0, 0);
		fdLabel.right = new FormAttachment(100, 0);
		fdLabel.bottom = new FormAttachment(100, 0);
		mainInfoCmpEdit.setLayoutData(fdLabel);
		mainInfoCmpEdit.setData(RWT.CUSTOM_VARIANT,
				PeopleUiConstants.PEOPLE_CSS_GENERALINFO_COMPOSITE);

		// intern layout
		mainInfoCmpEdit.setLayout(new GridLayout(10, false));
		// Salutation
		Label lbl = toolkit
				.createLabel(mainInfoCmpEdit, "Salutation", SWT.NONE);
		lbl.setLayoutData(new GridData());
		final Text salutationTxt = toolkit.createText(mainInfoCmpEdit, "",
				SWT.BORDER | SWT.SINGLE | SWT.LEFT);
		GridData gd = new GridData();
		gd.widthHint = 40;
		salutationTxt.setLayoutData(gd);

		// Title
		lbl = toolkit.createLabel(mainInfoCmpEdit, "Title", SWT.NONE);
		lbl.setLayoutData(new GridData());
		final Text titleTxt = toolkit.createText(mainInfoCmpEdit, "",
				SWT.BORDER | SWT.SINGLE | SWT.LEFT);
		gd = new GridData();
		gd.widthHint = 50;
		titleTxt.setLayoutData(gd);

		// first Name
		lbl = toolkit.createLabel(mainInfoCmpEdit, "First Name", SWT.NONE);
		lbl.setLayoutData(new GridData());
		final Text firstNameTxt = toolkit.createText(mainInfoCmpEdit, "",
				SWT.BORDER | SWT.SINGLE | SWT.LEFT);
		firstNameTxt.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
				| GridData.FILL_HORIZONTAL));

		// Last name
		lbl = toolkit.createLabel(mainInfoCmpEdit, "Name", SWT.NONE);
		lbl.setLayoutData(new GridData());
		final Text lastNameTxt = toolkit.createText(mainInfoCmpEdit, "",
				SWT.BORDER | SWT.SINGLE | SWT.LEFT);
		lastNameTxt.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
				| GridData.FILL_HORIZONTAL));

		// Name suffix
		lbl = toolkit.createLabel(mainInfoCmpEdit, "Suffix", SWT.NONE);
		lbl.setLayoutData(new GridData());
		final Text suffixTxt = toolkit.createText(mainInfoCmpEdit, "",
				SWT.BORDER | SWT.SINGLE | SWT.LEFT);
		gd = new GridData();
		gd.widthHint = 50;
		suffixTxt.setLayoutData(gd);

		final EntityAbstractFormPart editPart = new EntityAbstractFormPart() {
			public void refresh() { // update display value
				super.refresh();
				// EDIT PART
				String salut = CommonsJcrUtils.getStringValue(person,
						PeopleNames.PEOPLE_SALUTATION);
				if (CommonsJcrUtils.checkNotEmptyString(salut))
					salutationTxt.setText(salut);

				String firstName = JcrUtils.get(person,
						PeopleNames.PEOPLE_FIRST_NAME);
				if (CommonsJcrUtils.checkNotEmptyString(firstName))
					firstNameTxt.setText(firstName);

				String lastName = JcrUtils.get(person,
						PeopleNames.PEOPLE_LAST_NAME);
				if (CommonsJcrUtils.checkNotEmptyString(lastName))
					lastNameTxt.setText(lastName);

				String title = CommonsJcrUtils.getStringValue(person,
						PeopleNames.PEOPLE_PERSON_TITLE);
				if (title != null)
					titleTxt.setText(title);

				String suff = CommonsJcrUtils.getStringValue(person,
						PeopleNames.PEOPLE_NAME_SUFFIX);
				if (suff != null)
					suffixTxt.setText(suff);

				// READ ONLY PART
				displayNameROLbl.setText(PersonJcrUtils.getDisplayName(person));
				tagsROLbl.setText(PersonJcrUtils.getTags(person));
				try {
					if (person.isCheckedOut())
						mainInfoCmpEdit.moveAbove(mainInfoCmpRO);
					else
						mainInfoCmpEdit.moveBelow(mainInfoCmpRO);
				} catch (RepositoryException e) {
					throw new PeopleException(
							"Unable to get checked out status", e);
				}
				mainInfoCmpEdit.getParent().layout();
			}
		};

		// Listeners
		salutationTxt.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = -8632477454943247841L;

			@Override
			public void modifyText(ModifyEvent event) {
				if (setJcrProperty(person, PeopleNames.PEOPLE_SALUTATION,
						PropertyType.STRING, salutationTxt.getText()))
					editPart.markDirty();
			}
		});
		titleTxt.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 3041117992838491824L;

			@Override
			public void modifyText(ModifyEvent event) {
				if (setJcrProperty(person, PeopleNames.PEOPLE_PERSON_TITLE,
						PropertyType.STRING, titleTxt.getText()))
					editPart.markDirty();
			}
		});

		firstNameTxt.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = -8632477454943247841L;

			@Override
			public void modifyText(ModifyEvent event) {
				if (setJcrProperty(person, PeopleNames.PEOPLE_FIRST_NAME,
						PropertyType.STRING, firstNameTxt.getText()))
					editPart.markDirty();
			}
		});

		lastNameTxt.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 3041117992838491824L;

			@Override
			public void modifyText(ModifyEvent event) {
				if (setJcrProperty(person, PeopleNames.PEOPLE_LAST_NAME,
						PropertyType.STRING, lastNameTxt.getText()))
					editPart.markDirty();
			}
		});

		suffixTxt.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 3041117992838491824L;

			@Override
			public void modifyText(ModifyEvent event) {
				if (setJcrProperty(person, PeopleNames.PEOPLE_NAME_SUFFIX,
						PropertyType.STRING, suffixTxt.getText()))
					editPart.markDirty();
			}
		});

		getManagedForm().addPart(editPart);
		editPart.refresh();

		switchBtn.addSelectionListener(new SelectionListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					if (person.isCheckedOut()) {
						person.checkin();
						editPart.refresh();
					} else {
						person.checkout();
						editPart.refresh();
					}
				} catch (RepositoryException e1) {
					e1.printStackTrace();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});

	}

	protected void populateTabFolder(CTabFolder folder) {
		// // Contact informations
		// CTabItem currItem = addTabToFolder(folder, SWT.NO_FOCUS, "Contacts",
		// "msm:contacts");
		// currItem.setToolTipText("Contact information for "
		// + JcrUtils.get(person, PeopleNames.PEOPLE_LAST_NAME));
		// createDetailsContent(currItem);
		//
		// // Organisation informations
		// currItem = addTabToFolder(folder, SWT.NO_FOCUS, "Organisations",
		// "msm:organisations");
		// currItem.setToolTipText("Organisations linked to "
		// + JcrUtils.get(person, PeopleNames.PEOPLE_LAST_NAME));
		// createOrgaContent(currItem);
		//
		// // Film informations
		// currItem = addTabToFolder(folder, SWT.NO_FOCUS, "Films",
		// "msm:films");
		// currItem.setToolTipText("Films related to "
		// + JcrUtils.get(person, PeopleNames.PEOPLE_LAST_NAME));
		// createFilmContent(currItem);
	}

	private void createDetailsContent(CTabItem item) {
		// if (item.getControl() == null) {
		// CTabFolder folder = item.getParent();
		//
		// folder.setLayout(new GridLayout());
		// final Composite body = toolkit
		// .createComposite(folder, SWT.NO_FOCUS);
		// body.setLayoutData(new GridData(GridData.FILL_BOTH
		// | GridData.GRAB_VERTICAL | GridData.GRAB_HORIZONTAL));
		// body.setLayout(new GridLayout(2, false));
		//
		// // LEFT PART
		// // Add a contacts section
		// createContactComposite(body);
		//
		// // RIGHT PART
		// final Composite rightPartComp = toolkit.createComposite(body,
		// SWT.NO_FOCUS);
		// GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		// gd.grabExcessVerticalSpace = true;
		// rightPartComp.setLayoutData(gd);
		// rightPartComp.setLayout(new GridLayout(1, false));
		// toolkit.createLabel(rightPartComp, "Notes: ", SWT.NONE);
		//
		// final Text notesTxt = toolkit.createText(rightPartComp, "",
		// SWT.BORDER | SWT.MULTI | SWT.WRAP);
		// gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL
		// | GridData.GRAB_HORIZONTAL);
		// gd.widthHint = 200;
		// gd.minimumHeight = 200;
		// gd.grabExcessVerticalSpace = true;
		// notesTxt.setLayoutData(gd);
		//
		// final MsmAbstractFormPart notePart = new MsmAbstractFormPart() {
		// public void refresh() {
		// super.refresh();
		// String desc = CommonsJcrUtils.getStringValue(person,
		// Property.JCR_DESCRIPTION);
		// if (desc != null)
		// notesTxt.setText(desc);
		// notesTxt.setEnabled(isCheckoutedByMe());
		// }
		// };
		//
		// notesTxt.addModifyListener(new ModifyListener() {
		// private static final long serialVersionUID = 7535211104983287096L;
		//
		// @Override
		// public void modifyText(ModifyEvent event) {
		// if (setJcrProperty(Property.JCR_DESCRIPTION,
		// PropertyType.STRING, notesTxt.getText()))
		// notePart.markDirty();
		// }
		// });
		// getManagedForm().addPart(notePart);
		//
		// // compulsory
		// item.setControl(body);
		// }
	}

	private void createContactComposite(Composite parent) {
		// final Composite leftPart = toolkit.createComposite(parent, SWT.TOP);
		// leftPart.setLayoutData(new
		// GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		// leftPart.setLayout(new GridLayout());
		// // Add a new mail hyper link
		// Hyperlink addNewMailLink = toolkit.createHyperlink(leftPart,
		// "Add a new contact address", SWT.NONE);
		//
		// final MsmAbstractFormPart sPart = new MsmAbstractFormPart() {
		// public void refresh() {
		// try {
		// super.refresh();
		// for (String path : controls.keySet()) {
		// Text txt = controls.get(path);
		// Node currNode = getSession().getNode(path);
		// String propName = (String) txt.getData("propName");
		// String value = CommonsJcrUtils.getStringValue(currNode,
		// propName);
		// if (value != null)
		// txt.setText(value);
		// txt.setEnabled(isCheckoutedByMe());
		// }
		// } catch (RepositoryException e) {
		// throw new MsmException(
		// "Cannot refresh generic details formPart", e);
		// }
		// }
		// };
		// final Composite contactComposite = toolkit.createComposite(leftPart,
		// SWT.NONE);
		// contactComposite.setLayout(new GridLayout(2, false));
		// GridData gd = new GridData(GridData.FILL_BOTH);
		// gd.grabExcessVerticalSpace = true;
		//
		// refreshContactSessionBody(contactComposite, sPart);
		// getManagedForm().addPart(sPart);
		//
		// addNewMailLink.addHyperlinkListener(new MyHyperlinkListener() {
		// @Override
		// public void linkActivated(HyperlinkEvent e) {
		//
		// // try {
		// PeopleJcrUtils.createEmail(person, "test@mail.de", false,
		// "test", null);
		// refreshContactSessionBody(contactComposite, sPart);
		// sPart.markDirty();
		// // } catch (RepositoryException re) {
		// // throw new MsmException("Error while adding a new mail", re);
		// // }
		//
		// // try {
		// //
		// // getManagedForm().removePart(sPart);
		// // section.getClient().dispose();
		// // Composite sectionBody = toolkit
		// // .createComposite(section);
		// // final MsmAbstractFormPart sPart =
		// // createMailSectionBody(sectionBody);
		// // section.setClient(sectionBody);
		// // section.getParent().layout(true);
		// // getManagedForm().addPart(sPart);
		// // forceRefresh();
		// //
		// }
		// });

	}

	// private void refreshContactSessionBody(Composite body,
	// final MsmAbstractFormPart part) {
	//
	// // Clean old controls
	// for (Control comp : body.getChildren()) {
	// comp.dispose();
	// }
	//
	// // FIXME work in progress
	// final Map<String, Text> controls = new HashMap<String, Text>();
	//
	// NodeIterator ni;
	// try {
	// ni = person.getNode(PeopleNames.PEOPLE_CONTACTS).getNodes();
	// while (ni.hasNext()) {
	// Node currNode = ni.nextNode();
	// if (!currNode.isNodeType(MsmTypes.PEOPLE_ADDRESS)) {
	// String type = CommonsJcrUtils.getStringValue(currNode,
	// PeopleNames.PEOPLE_CONTACT_TYPE);
	// if (type == null)
	// type = CommonsJcrUtils.getStringValue(currNode,
	// PeopleNames.PEOPLE_CONTACT_CATEGORY);
	// if (type == null)
	// type = PeopleJcrUtils.getContactTypeAsString(currNode);
	// toolkit.createLabel(body, type, SWT.NO_FOCUS);
	// Text currCtl = toolkit.createText(body, null, SWT.BORDER);
	// GridData gd = new GridData();
	// gd.widthHint = 200;
	// gd.heightHint = 14;
	// currCtl.setLayoutData(gd);
	// currCtl.setData("propName", PeopleNames.PEOPLE_CONTACT_VALUE);
	// controls.put(currNode.getPath(), currCtl);
	// }
	// }
	//
	// for (final String name : controls.keySet()) {
	// final Text txt = controls.get(name);
	// txt.addModifyListener(new ModifyListener() {
	// private static final long serialVersionUID = -5544439293866885125L;
	//
	// @Override
	// public void modifyText(ModifyEvent event) {
	//
	// try {
	// Node currNode = getSession().getNode(name);
	// String propName = (String) txt.getData("propName");
	//
	// if (currNode.hasProperty(propName)
	// && currNode.getProperty(propName)
	// .getString().equals(txt.getText())) {
	// // nothing changed yet
	// } else {
	// currNode.setProperty(propName, txt.getText());
	// part.markDirty();
	// }
	//
	// } catch (RepositoryException e) {
	// throw new MsmException(
	// "Cannot refresh mail formPart", e);
	// }
	// }
	// });
	// }
	//
	// } catch (RepositoryException e) {
	// throw new MsmException("Error while getting properties", e);
	// }
	// part.setTextControls(controls);
	// part.refresh();
	// body.layout();
	// body.pack(true);
	// // body.redraw();
	// // body.getParent().redraw();
	// body.getParent().pack(true);
	// body.getParent().layout();
	// }
	//
	// private MsmAbstractFormPart createContactSectionBody(Composite body) {
	// body.setLayout(new GridLayout(2, false));
	//
	// // FIXME work in progress
	// final Map<String, Text> controls = new HashMap<String, Text>();
	//
	// // Workaround to fix a focus problem
	// Text defaultTxt = null;
	//
	// NodeIterator ni;
	// try {
	// ni = person.getNode(PeopleNames.PEOPLE_CONTACTS).getNodes();
	// while (ni.hasNext()) {
	// Node currNode = ni.nextNode();
	// if (!currNode.isNodeType(MsmTypes.PEOPLE_ADDRESS)) {
	// String type = CommonsJcrUtils.getStringValue(currNode,
	// PeopleNames.PEOPLE_CONTACT_TYPE);
	// if (type == null)
	// type = CommonsJcrUtils.getStringValue(currNode,
	// PeopleNames.PEOPLE_CONTACT_CATEGORY);
	// if (type == null)
	// type = PeopleJcrUtils.getContactTypeAsString(currNode);
	// toolkit.createLabel(body, type, SWT.NO_FOCUS);
	// Text currCtl = toolkit.createText(body, null, SWT.BORDER);
	// GridData gd = new GridData();
	// gd.widthHint = 200;
	// gd.heightHint = 14;
	// currCtl.setLayoutData(gd);
	// currCtl.setData("propName", PeopleNames.PEOPLE_CONTACT_VALUE);
	// controls.put(currNode.getPath(), currCtl);
	//
	// if (defaultTxt == null)
	// defaultTxt = currCtl;
	// }
	// }
	//
	// final MsmAbstractFormPart part = new MsmAbstractFormPart() {
	// public void refresh() {
	// try {
	// super.refresh();
	// for (String path : controls.keySet()) {
	// Text txt = controls.get(path);
	// Node currNode = getSession().getNode(path);
	// String propName = (String) txt.getData("propName");
	// String value = CommonsJcrUtils.getStringValue(
	// currNode, propName);
	// if (value != null)
	// txt.setText(value);
	// txt.setEnabled(isCheckoutedByMe());
	// }
	// } catch (RepositoryException e) {
	// throw new MsmException(
	// "Cannot refresh generic details formPart", e);
	// }
	// }
	// };
	//
	// for (final String name : controls.keySet()) {
	// final Text txt = controls.get(name);
	// txt.addModifyListener(new ModifyListener() {
	// private static final long serialVersionUID = -5544439293866885125L;
	//
	// @Override
	// public void modifyText(ModifyEvent event) {
	//
	// try {
	// Node currNode = getSession().getNode(name);
	// String propName = (String) txt.getData("propName");
	//
	// if (currNode.hasProperty(propName)
	// && currNode.getProperty(propName)
	// .getString().equals(txt.getText())) {
	// // nothing changed yet
	// } else {
	// currNode.setProperty(propName, txt.getText());
	// part.markDirty();
	// }
	//
	// } catch (RepositoryException e) {
	// throw new MsmException(
	// "Cannot refresh mail formPart", e);
	// }
	// }
	// });
	// }
	//
	// body.addFocusListener(new MyFocusListener(defaultTxt));
	//
	// return part;
	// } catch (RepositoryException e) {
	// throw new MsmException("Error while getting properties", e);
	// }
	// }
	//
	// private class MyFocusListener implements FocusListener {
	// private static final long serialVersionUID = 1L;
	// private Text defaultTxt;
	//
	// public MyFocusListener(Text defaultTxt) {
	// this.defaultTxt = defaultTxt;
	// }
	//
	// @Override
	// public void focusLost(FocusEvent event) {
	// }
	//
	// @Override
	// public void focusGained(FocusEvent event) {
	// if (defaultTxt != null)
	// defaultTxt.setFocus();
	// }
	// }
	//
	// private void createOrgaContent(CTabItem item) {
	// try {
	// if (item.getControl() == null) {
	// CTabFolder folder = item.getParent();
	//
	// final Composite body = toolkit.createComposite(folder,
	// SWT.NO_FOCUS);
	//
	// TableViewer viewer = new TableViewer(body);
	//
	// TableColumnLayout tableColumnLayout = createColumns(body,
	// viewer);
	//
	// body.setLayout(tableColumnLayout);
	//
	// // Corresponding table & style
	// Table table = viewer.getTable();
	// table.setLinesVisible(true);
	// table.setHeaderVisible(false);
	//
	// // Enable markups
	// table.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
	// table.setData(RWT.CUSTOM_ITEM_HEIGHT, Integer.valueOf(60));
	//
	// // compulsory content provider
	// viewer.setContentProvider(new BasicContentProvider());
	// viewer.addDoubleClickListener(new NodeDoubleClickListener());
	//
	// item.setControl(body);
	// List<Node> orgas = new ArrayList<Node>();
	// PropertyIterator pi = person.getReferences();
	// while (pi.hasNext()) {
	// // Check if have the right type of node
	// Property prop = pi.nextProperty();
	// if (prop.getParent().getParent().getParent()
	// .isNodeType(MsmTypes.PEOPLE_ORGANIZATION)) {
	// Node linkItem = prop.getParent();
	// orgas.add(linkItem);
	// }
	// }
	//
	// viewer.setInput(orgas);
	// }
	// } catch (RepositoryException re) {
	// throw new MsmException("Cannot create organizations list", re);
	// }
	//
	// }
	//
	// private TableColumnLayout createColumns(final Composite parent,
	// final TableViewer viewer) {
	// int[] bounds = { 150, 300 };
	// TableColumnLayout tableColumnLayout = new TableColumnLayout();
	//
	// // Role
	// TableViewerColumn col = createTableViewerColumn(viewer, "", SWT.LEFT,
	// bounds[0]);
	// col.setLabelProvider(new RoleListLabelProvider());
	// tableColumnLayout.setColumnData(col.getColumn(), new ColumnWeightData(
	// 80, 20, true));
	//
	// // Company
	// col = createTableViewerColumn(viewer, "", SWT.LEFT, bounds[1]);
	// col.setLabelProvider(new OrgOverviewLabelProvider(true));
	// tableColumnLayout.setColumnData(col.getColumn(), new ColumnWeightData(
	// 200, 80, true));
	// return tableColumnLayout;
	// }
	//
	// private void createFilmContent(CTabItem item) {
	// try {
	// if (item.getControl() == null) {
	// CTabFolder folder = item.getParent();
	// final Composite body = toolkit.createComposite(folder);
	// TableViewer viewer = new TableViewer(body);
	// TableColumnLayout tableColumnLayout = createFilmColumns(body,
	// viewer);
	// body.setLayout(tableColumnLayout);
	//
	// // Corresponding table & style
	// Table table = viewer.getTable();
	// table.setLinesVisible(true);
	// table.setHeaderVisible(false);
	//
	// // Enable markups
	// table.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
	// table.setData(RWT.CUSTOM_ITEM_HEIGHT, Integer.valueOf(70));
	//
	// // compulsory content provider
	// viewer.setContentProvider(new BasicContentProvider());
	// viewer.addDoubleClickListener(new NodeDoubleClickListener());
	//
	// item.setControl(body);
	// List<Node> films = new ArrayList<Node>();
	// PropertyIterator pi = person.getReferences();
	// // Check if have the right type of node
	// while (pi.hasNext()) {
	// Property prop = pi.nextProperty();
	// if (prop.getParent().getParent().getParent()
	// .isNodeType(MsmTypes.PEOPLE_FILM)) {
	// Node linkItem = prop.getParent();
	// films.add(linkItem);
	// }
	// }
	// viewer.setInput(films);
	// }
	// } catch (RepositoryException re) {
	// throw new MsmException("Cannot create organizations list", re);
	// }
	//
	// }
	//
	// private TableColumnLayout createFilmColumns(final Composite parent,
	// final TableViewer viewer) {
	// int[] bounds = { 150, 300 };
	// TableColumnLayout tableColumnLayout = new TableColumnLayout();
	//
	// // Role
	// TableViewerColumn col = createTableViewerColumn(viewer, "", SWT.LEFT,
	// bounds[0]);
	// col.setLabelProvider(new RoleListLabelProvider());
	// tableColumnLayout.setColumnData(col.getColumn(), new ColumnWeightData(
	// 80, 20, true));
	//
	// // Film
	// col = createTableViewerColumn(viewer, "", SWT.LEFT, bounds[1]);
	// col.setLabelProvider(new FilmOverviewLabelProvider(true));
	// tableColumnLayout.setColumnData(col.getColumn(), new ColumnWeightData(
	// 200, 80, true));
	// return tableColumnLayout;
	// }
}