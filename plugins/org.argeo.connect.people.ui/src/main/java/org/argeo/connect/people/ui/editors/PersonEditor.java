package org.argeo.connect.people.ui.editors;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.ui.JcrUiUtils;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.ui.providers.PersonOverviewLabelProvider;
import org.argeo.connect.people.ui.toolkits.EntityToolkit;
import org.argeo.connect.people.ui.toolkits.ListToolkit;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.PeopleJcrUtils;
import org.argeo.connect.people.utils.PersonJcrUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

/**
 * Default connect Person editor page
 */
public class PersonEditor extends AbstractEntityCTabEditor {
	final static Log log = LogFactory.getLog(PersonEditor.class);

	// local constants
	public final static String ID = PeopleUiPlugin.PLUGIN_ID + ".personEditor";
	// Main business Objects
	private Node person;

	// Usefull toolkits
	private EntityToolkit entityTK;
	private ListToolkit listTK;

	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		person = getEntity();

	}

	@Override
	protected void updatePartName() {
		String shortName = CommonsJcrUtils.get(getEntity(),
				PeopleNames.PEOPLE_LAST_NAME);
		if (CommonsJcrUtils.checkNotEmptyString(shortName)) {
			if (shortName.length() > SHORT_NAME_LENGHT)
				shortName = shortName.substring(0, SHORT_NAME_LENGHT - 1)
						+ "...";
			setPartName(shortName);
		}
	}

	@Override
	protected void createToolkits() {
		entityTK = new EntityToolkit(toolkit, getManagedForm());
		listTK = new ListToolkit(toolkit, getManagedForm(),
				getPeopleServices(), getPeopleUiServices());
	}

	@Override
	protected boolean canSave() {
		try {
			String lastName = CommonsJcrUtils.get(person,
					PeopleNames.PEOPLE_LAST_NAME);
			String firstName = CommonsJcrUtils.get(person,
					PeopleNames.PEOPLE_FIRST_NAME);
			String displayName = CommonsJcrUtils
					.get(person, Property.JCR_TITLE);
			boolean useDefaultDisplay = person.getProperty(
					PeopleNames.PEOPLE_USE_DEFAULT_DISPLAY_NAME).getBoolean();

			if (lastName.length() < 2 && firstName.length() < 2
					&& (useDefaultDisplay || displayName.length() < 2)) {
				String msg = "Please note that you must define a first name, a "
						+ "last name or a display name that is at least 2 character long.";
				MessageDialog.openError(PeopleUiPlugin.getDefault()
						.getWorkbench().getDisplay().getActiveShell(),
						"Non-valid information", msg);

				return false;
			} else {
				PeopleJcrUtils.checkPathAndMoveIfNeeded(person,
						PeopleConstants.PEOPLE_BASE_PATH + "/"
								+ PeopleNames.PEOPLE_PERSONS);
				return true;
			}

		} catch (RepositoryException re) {
			throw new PeopleException("Unable to determine savable status", re);
		}
	}

	@Override
	protected void populateTabFolder(CTabFolder folder) {
		// Contact informations
		String tooltip = "Contact information for "
				+ JcrUtils.get(person, PeopleNames.PEOPLE_LAST_NAME);
		Composite innerPannel = addTabToFolder(folder, CTAB_COMP_STYLE,
				"Contact details", PeopleUiConstants.PANEL_CONTACT_DETAILS,
				tooltip);
		entityTK.populateContactPanelWithNotes(innerPannel, person);

		// Jobs panel
		tooltip = "Organisations linked to "
				+ JcrUtils.get(person, PeopleNames.PEOPLE_LAST_NAME);
		innerPannel = addTabToFolder(folder, CTAB_COMP_STYLE, "Organisations",
				PeopleUiConstants.PANEL_JOBS, tooltip);
		listTK.populateJobsPanel(innerPannel, person);

		// Film participation panel
		// TODO: move this in specific film project
		tooltip = "Films related to "
				+ JcrUtils.get(person, PeopleNames.PEOPLE_LAST_NAME);
		innerPannel = addTabToFolder(folder, CTAB_COMP_STYLE, "Films",
				PeopleUiConstants.PANEL_PRODUCTIONS, tooltip);
		listTK.populateFilmsPanel(innerPannel, person);
		folder.layout();
	}

	@Override
	protected void populateTitleComposite(Composite parent) {
		parent.setLayout(new FormLayout());

		// READ ONLY
		final Composite readOnlyPanel = toolkit.createComposite(parent,
				SWT.NO_FOCUS);
		PeopleUiUtils.setSwitchingFormData(readOnlyPanel);
		readOnlyPanel.setLayout(new GridLayout());

		// Add a label with info provided by the PersonOverviewLabelProvider
		final Label readOnlyInfoLbl = toolkit.createLabel(readOnlyPanel, "",
				SWT.WRAP);
		readOnlyInfoLbl.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
		final ColumnLabelProvider personLP = new PersonOverviewLabelProvider(
				PersonOverviewLabelProvider.LIST_TYPE_OVERVIEW_TITLE,
				getPeopleServices());

		// EDIT
		final Composite editPanel = toolkit.createComposite(parent,
				SWT.NO_FOCUS);
		PeopleUiUtils.setSwitchingFormData(editPanel);
		// editPanel.setData(RWT.CUSTOM_VARIANT,
		// PeopleUiConstants.PEOPLE_CSS_GENERALINFO_COMPOSITE);
		editPanel.setLayout(gridLayoutNoBorder());

		// First Line - display Name management
		Composite firstCmp = toolkit.createComposite(editPanel, SWT.NO_FOCUS);
		firstCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		RowLayout rl = new RowLayout(SWT.HORIZONTAL);
		rl.wrap = false;
		firstCmp.setLayout(rl);

		// Second Line main names
		Composite secondCmp = toolkit.createComposite(editPanel, SWT.NO_FOCUS);
		secondCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		rl = new RowLayout(SWT.HORIZONTAL);
		rl.wrap = false;
		secondCmp.setLayout(rl);

		// Third Line: other Names
		Composite thirdCmp = toolkit.createComposite(editPanel, SWT.NO_FOCUS);
		thirdCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		rl = new RowLayout(SWT.HORIZONTAL);
		rl.wrap = false;
		thirdCmp.setLayout(rl);

		// Create edit text
		final Text displayNameTxt = entityTK.createText(firstCmp,
				"Display name", "Default display name for this person", 300);
		final Button defaultDisplayBtn = toolkit.createButton(firstCmp,
				"Use default display name", SWT.CHECK);
		defaultDisplayBtn
				.setToolTipText("Default is Firstname LASTNAMEMr, Mrs...");

		final Text salutationTxt = entityTK.createText(secondCmp, "Salutation",
				"Mr, Mrs...", 60);
		final Text firstNameTxt = entityTK.createText(secondCmp, "First Name",
				"Usual first name for this person", 100);
		final Text middleNameTxt = entityTK.createText(secondCmp,
				"Middle Name", "The second name if it exists", 100);
		final Text lastNameTxt = entityTK.createText(secondCmp, "Last Name",
				"Usual last name for this person", 100);
		final Text nickNameTxt = entityTK.createText(secondCmp, "Nickame",
				"A pseudonym...", 100);

		final Text genderTxt = entityTK.createText(thirdCmp, "Gender", "...",
				80);
		final Text maidenNameTxt = entityTK.createText(thirdCmp, "Maiden Name",
				"Birth Name before getting maried", 100);
		final Text titleTxt = entityTK.createText(thirdCmp, "Title",
				"Doc., Sir...", 60);
		final Text suffixTxt = entityTK.createText(thirdCmp, "Suffix",
				"Junior, the third...", 80);
		final Text latinPhoneticTxt = entityTK.createText(thirdCmp,
				"Latin Phonetic",
				"A helper to know how to pronounce this name", 100);

		final EntityAbstractFormPart editPart = new EntityAbstractFormPart() {
			public void refresh() { // update display value
				super.refresh();
				// EDIT PART

				entityTK.refreshTextValue(displayNameTxt, person,
						Property.JCR_TITLE);

				try {
					boolean useDefault = person.getProperty(
							PeopleNames.PEOPLE_USE_DEFAULT_DISPLAY_NAME)
							.getBoolean();
					displayNameTxt.setEnabled(!useDefault);
					defaultDisplayBtn.setSelection(useDefault);
				} catch (RepositoryException e) {
					throw new PeopleException(
							"Unable to refresh use default display name property",
							e);
				}

				entityTK.refreshTextValue(salutationTxt, person,
						PeopleNames.PEOPLE_SALUTATION);
				entityTK.refreshTextValue(firstNameTxt, person,
						PeopleNames.PEOPLE_FIRST_NAME);
				entityTK.refreshTextValue(middleNameTxt, person,
						PeopleNames.PEOPLE_MIDDLE_NAME);
				entityTK.refreshTextValue(lastNameTxt, person,
						PeopleNames.PEOPLE_LAST_NAME);
				entityTK.refreshTextValue(nickNameTxt, person,
						PeopleNames.PEOPLE_NICKNAME);

				entityTK.refreshTextValue(genderTxt, person,
						PeopleNames.PEOPLE_GENDER);
				entityTK.refreshTextValue(maidenNameTxt, person,
						PeopleNames.PEOPLE_MAIDEN_NAME);
				entityTK.refreshTextValue(titleTxt, person,
						PeopleNames.PEOPLE_PERSON_TITLE);
				entityTK.refreshTextValue(suffixTxt, person,
						PeopleNames.PEOPLE_NAME_SUFFIX);
				entityTK.refreshTextValue(latinPhoneticTxt, person,
						PeopleNames.PEOPLE_LATIN_PHONETIC_SPELLING);

				// READ ONLY PART
				String roText = personLP.getText(person);
				readOnlyInfoLbl.setText(roText);

				// Manage switch
				if (CommonsJcrUtils.isNodeCheckedOutByMe(person))
					editPanel.moveAbove(readOnlyPanel);
				else
					editPanel.moveBelow(readOnlyPanel);
				editPanel.getParent().layout();
			}
		};

		// Listeners

		// Specific listeners to manage correctly display name
		firstNameTxt.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void modifyText(ModifyEvent event) {
				try {
					if (JcrUiUtils.setJcrProperty(person,
							PeopleNames.PEOPLE_FIRST_NAME, PropertyType.STRING,
							firstNameTxt.getText())) {
						if (person.getProperty(
								PeopleNames.PEOPLE_USE_DEFAULT_DISPLAY_NAME)
								.getBoolean()) {
							String displayName = PersonJcrUtils
									.getPersonDisplayName(person);
							person.setProperty(Property.JCR_TITLE, displayName);
							displayNameTxt.setText(displayName);
						}
						editPart.markDirty();
					}
				} catch (RepositoryException e) {
					throw new PeopleException("Unable to update property", e);
				}
			}
		});

		lastNameTxt.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void modifyText(ModifyEvent event) {
				try {
					if (JcrUiUtils.setJcrProperty(person,
							PeopleNames.PEOPLE_LAST_NAME, PropertyType.STRING,
							lastNameTxt.getText())) {
						if (person.getProperty(
								PeopleNames.PEOPLE_USE_DEFAULT_DISPLAY_NAME)
								.getBoolean()) {
							String displayName = PersonJcrUtils
									.getPersonDisplayName(person);
							person.setProperty(Property.JCR_TITLE, displayName);
							displayNameTxt.setText(displayName);
						}
						editPart.markDirty();
					}
				} catch (RepositoryException e) {
					throw new PeopleException("Unable to update property", e);
				}
			}
		});

		displayNameTxt.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void modifyText(ModifyEvent event) {
				boolean useDefault = defaultDisplayBtn.getSelection();
				// if use default, do nothing
				if (!useDefault)
					if (JcrUiUtils.setJcrProperty(person, Property.JCR_TITLE,
							PropertyType.STRING, displayNameTxt.getText())) {
						editPart.markDirty();
					}
			}
		});

		defaultDisplayBtn.addSelectionListener(new SelectionListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean useDefault = defaultDisplayBtn.getSelection();
				if (JcrUiUtils.setJcrProperty(person,
						PeopleNames.PEOPLE_USE_DEFAULT_DISPLAY_NAME,
						PropertyType.BOOLEAN, useDefault)) {
					if (useDefault) {
						String displayName = PersonJcrUtils
								.getPersonDisplayName(person);
						JcrUiUtils.setJcrProperty(person, Property.JCR_TITLE,
								PropertyType.STRING, displayName);
						displayNameTxt.setText(displayName);
						displayNameTxt.setEnabled(false);
					} else
						displayNameTxt.setEnabled(true);

				}
				editPart.markDirty();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		entityTK.addTxtModifyListener(editPart, salutationTxt, person,
				PeopleNames.PEOPLE_SALUTATION, PropertyType.STRING);
		entityTK.addTxtModifyListener(editPart, middleNameTxt, person,
				PeopleNames.PEOPLE_MIDDLE_NAME, PropertyType.STRING);
		entityTK.addTxtModifyListener(editPart, lastNameTxt, person,
				PeopleNames.PEOPLE_LAST_NAME, PropertyType.STRING);
		entityTK.addTxtModifyListener(editPart, nickNameTxt, person,
				PeopleNames.PEOPLE_NICKNAME, PropertyType.STRING);

		entityTK.addTxtModifyListener(editPart, genderTxt, person,
				PeopleNames.PEOPLE_GENDER, PropertyType.STRING);
		entityTK.addTxtModifyListener(editPart, maidenNameTxt, person,
				PeopleNames.PEOPLE_MAIDEN_NAME, PropertyType.STRING);
		entityTK.addTxtModifyListener(editPart, titleTxt, person,
				PeopleNames.PEOPLE_PERSON_TITLE, PropertyType.STRING);
		entityTK.addTxtModifyListener(editPart, suffixTxt, person,
				PeopleNames.PEOPLE_NAME_SUFFIX, PropertyType.STRING);
		entityTK.addTxtModifyListener(editPart, latinPhoneticTxt, person,
				PeopleNames.PEOPLE_LATIN_PHONETIC_SPELLING, PropertyType.STRING);

		editPart.initialize(getManagedForm());
		getManagedForm().addPart(editPart);

	}

	@Override
	protected void populateMainInfoDetails(final Composite parent) {
		// Tag management.
		Composite tagsCmp = toolkit.createComposite(parent, SWT.NO_FOCUS);
		tagsCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		tagsCmp.setLayout(gridLayoutNoBorder(2));

		Composite leftCmp = toolkit.createComposite(tagsCmp, SWT.NO_FOCUS);
		leftCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		entityTK.populateTagsROPanel(leftCmp, person);

		Composite rightCmp = toolkit.createComposite(tagsCmp, SWT.NO_FOCUS);
		rightCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		entityTK.populateAddTagComposite(rightCmp, person);

		// keep last update.
		super.populateMainInfoDetails(parent);
	}
}