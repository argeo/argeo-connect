package org.argeo.connect.people.ui.editors;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.ui.JcrUiUtils;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.PersonJcrUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
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

	protected void populateTabFolder(CTabFolder folder) {
		// Contact informations
		String tooltip = "Contact information for "
				+ JcrUtils.get(person, PeopleNames.PEOPLE_LAST_NAME);
		Composite innerPannel = addTabToFolder(folder, SWT.NO_FOCUS,
				"Contact details", "people:contactDetails", tooltip);
		EntityPanelToolkit.populateContactPanelWithNotes(innerPannel, person,
				toolkit, getManagedForm());

		// CTabItem currItem = addTabToFolder(folder, SWT.NO_FOCUS, "Contacts",
		// "msm:contacts");
		// currItem.setToolTipText(
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
		folder.layout();
	}

	@Override
	protected void populateMainInfoComposite(final Composite switchingPanel) {
		switchingPanel.setLayout(new FormLayout());

		// READ ONLY
		final Composite mainInfoCmpRO = toolkit.createComposite(switchingPanel,
				SWT.NO_FOCUS);
		PeopleUiUtils.setSwitchingFormData(mainInfoCmpRO);
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
		PeopleUiUtils.setSwitchingFormData(mainInfoCmpEdit);
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
				if (JcrUiUtils.setJcrProperty(person,
						PeopleNames.PEOPLE_SALUTATION, PropertyType.STRING,
						salutationTxt.getText()))
					editPart.markDirty();
			}
		});
		titleTxt.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 3041117992838491824L;

			@Override
			public void modifyText(ModifyEvent event) {
				if (JcrUiUtils.setJcrProperty(person,
						PeopleNames.PEOPLE_PERSON_TITLE, PropertyType.STRING,
						titleTxt.getText()))
					editPart.markDirty();
			}
		});

		firstNameTxt.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = -8632477454943247841L;

			@Override
			public void modifyText(ModifyEvent event) {
				if (JcrUiUtils.setJcrProperty(person,
						PeopleNames.PEOPLE_FIRST_NAME, PropertyType.STRING,
						firstNameTxt.getText()))
					editPart.markDirty();
			}
		});

		lastNameTxt.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 3041117992838491824L;

			@Override
			public void modifyText(ModifyEvent event) {
				if (JcrUiUtils.setJcrProperty(person,
						PeopleNames.PEOPLE_LAST_NAME, PropertyType.STRING,
						lastNameTxt.getText()))
					editPart.markDirty();
			}
		});

		suffixTxt.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 3041117992838491824L;

			@Override
			public void modifyText(ModifyEvent event) {
				if (JcrUiUtils.setJcrProperty(person,
						PeopleNames.PEOPLE_NAME_SUFFIX, PropertyType.STRING,
						suffixTxt.getText()))
					editPart.markDirty();
			}
		});
		getManagedForm().addPart(editPart);
	}
}