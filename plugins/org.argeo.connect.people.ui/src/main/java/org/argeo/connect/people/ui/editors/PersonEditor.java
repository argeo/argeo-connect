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
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.composites.TagListComposite;
import org.argeo.connect.people.ui.editors.utils.AbstractEntityCTabEditor;
import org.argeo.connect.people.ui.providers.PersonOverviewLabelProvider;
import org.argeo.connect.people.ui.toolkits.ActivityToolkit;
import org.argeo.connect.people.ui.toolkits.ContactToolkit;
import org.argeo.connect.people.ui.toolkits.HistoryToolkit;
import org.argeo.connect.people.ui.toolkits.ListToolkit;
import org.argeo.connect.people.ui.utils.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.PeopleJcrUtils;
import org.argeo.connect.people.utils.PersonJcrUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
import org.eclipse.ui.forms.AbstractFormPart;

/**
 * Editor page that display a person with corresponding details
 */
public class PersonEditor extends AbstractEntityCTabEditor {
	final static Log log = LogFactory.getLog(PersonEditor.class);

	// local constants
	public final static String ID = PeopleUiPlugin.PLUGIN_ID + ".personEditor";
	// Main business Objects
	private Node person;

	// Usefull toolkits
	private ContactToolkit contactTK;
	private ActivityToolkit activityTK;
	private ListToolkit listTK;
	private HistoryToolkit historyTK;

	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		person = getNode();

	}

	@Override
	protected void updatePartName() {
		String shortName = CommonsJcrUtils.get(getNode(),
				PeopleNames.PEOPLE_LAST_NAME);
		if (CommonsJcrUtils.isEmptyString(shortName)) {
			shortName = CommonsJcrUtils.get(getNode(), Property.JCR_TITLE);
		}
		if (CommonsJcrUtils.checkNotEmptyString(shortName)) {
			if (shortName.length() > SHORT_NAME_LENGHT)
				shortName = shortName.substring(0, SHORT_NAME_LENGHT - 1)
						+ "...";
			setPartName(shortName);
		}
	}

	@Override
	protected void createToolkits() {
		contactTK = new ContactToolkit(toolkit, getManagedForm(),
				getPeopleService(), getPeopleUiService());
		activityTK = new ActivityToolkit(toolkit, getManagedForm(),
				getPeopleService(), getPeopleUiService());
		listTK = new ListToolkit(toolkit, getManagedForm(), getPeopleService(),
				getPeopleUiService());
		historyTK = new HistoryToolkit(toolkit, getManagedForm(),
				getRepository(), getPeopleService(), person);
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
	protected void populateHeader(Composite parent) {
		parent.setLayout(PeopleUiUtils.gridLayoutNoBorder());

		Composite titleCmp = toolkit.createComposite(parent, SWT.NO_FOCUS);
		titleCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		populateTitleComposite(titleCmp);

		// Tag Management
		Composite tagsCmp = new TagListComposite(parent, SWT.NO_FOCUS, toolkit,
				getManagedForm(), getPeopleService(), getPeopleUiService(),
				person, getPeopleService().getResourceBasePath(
						PeopleConstants.RESOURCE_TAG), PeopleNames.PEOPLE_TAGS,
				"Add a tag");
		tagsCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		// Mailing list management
		Composite mlCmp = toolkit.createComposite(parent, SWT.NO_FOCUS);
		mlCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		contactTK.populateMailingListMembershipPanel(mlCmp, person);
	}

	@Override
	protected void populateTabFolder(CTabFolder folder) {
		// Contact informations
		String tooltip = "Contact information for "
				+ JcrUtils.get(person, Property.JCR_TITLE);
		Composite innerPannel = addTabToFolder(folder, CTAB_COMP_STYLE,
				"Contact details", PeopleUiConstants.PANEL_CONTACT_DETAILS,
				tooltip);
		contactTK.createContactPanelWithNotes(innerPannel, person);

		// Activities and tasks
		tooltip = "Activities and tasks related to "
				+ JcrUtils.get(person, Property.JCR_TITLE);
		innerPannel = addTabToFolder(folder, CTAB_COMP_STYLE, "Activity log",
				PeopleUiConstants.PANEL_ACTIVITY_LOG, tooltip);
		activityTK.populateActivityLogPanel(innerPannel, person);

		// Jobs panel
		tooltip = "Organisations linked to "
				+ JcrUtils.get(person, Property.JCR_TITLE);
		innerPannel = addTabToFolder(folder, CTAB_COMP_STYLE, "Organisations",
				PeopleUiConstants.PANEL_JOBS, tooltip);
		listTK.populateJobsPanel(innerPannel, person);

		// // Film participation panel
		// // TODO: move this in specific film project
		// tooltip = "Films related to "
		// + JcrUtils.get(person, Property.JCR_TITLE);
		// innerPannel = addTabToFolder(folder, CTAB_COMP_STYLE, "Films",
		// PeopleUiConstants.PANEL_PRODUCTIONS, tooltip);
		// listTK.populateParticipationPanel(innerPannel, person);

		// History panel
		// TODO: make this dynamic
		tooltip = "History of information about "
				+ JcrUtils.get(person, Property.JCR_TITLE);
		innerPannel = addTabToFolder(folder, CTAB_COMP_STYLE, "History",
				PeopleUiConstants.PANEL_HISTORY, tooltip);
		historyTK.populateHistoryPanel(innerPannel);
		folder.layout();
	}

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
		readOnlyInfoLbl.setData(PeopleUiConstants.MARKUP_ENABLED, Boolean.TRUE);
		final ColumnLabelProvider personLP = new PersonOverviewLabelProvider(
				PeopleUiConstants.LIST_TYPE_OVERVIEW_TITLE, getPeopleService());

		// EDIT
		final Composite editPanel = toolkit.createComposite(parent,
				SWT.NO_FOCUS);
		PeopleUiUtils.setSwitchingFormData(editPanel);
		// editPanel.setData(RWT.CUSTOM_VARIANT,
		// PeopleUiConstants.PEOPLE_CSS_GENERALINFO_COMPOSITE);
		editPanel.setLayout(PeopleUiUtils.gridLayoutNoBorder());

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
		final Text displayNameTxt = PeopleUiUtils.createRDText(toolkit,
				firstCmp, "Display name",
				"Default display name for this person", 300);
		final Button defaultDisplayBtn = toolkit.createButton(firstCmp,
				"Use default display name", SWT.CHECK);
		defaultDisplayBtn.setToolTipText("Default is \"Firstname LASTNAME\"");

		final Text salutationTxt = PeopleUiUtils.createRDText(toolkit,
				secondCmp, "Salutation", "Mr, Mrs...", 60);
		final Text firstNameTxt = PeopleUiUtils.createRDText(toolkit,
				secondCmp, "First Name", "Usual first name for this person",
				100);
		final Text middleNameTxt = PeopleUiUtils.createRDText(toolkit,
				secondCmp, "Middle Name", "The second name if it exists", 100);
		final Text lastNameTxt = PeopleUiUtils.createRDText(toolkit, secondCmp,
				"Last Name", "Usual last name for this person", 100);
		final Text suffixTxt = PeopleUiUtils.createRDText(toolkit, secondCmp,
				"Suffix", "Junior, the third...", 80);

		// final Text genderTxt = entityTK.createText(thirdCmp, "Gender", "...",
		// 80);
		final Text titleTxt = PeopleUiUtils.createRDText(toolkit, thirdCmp,
				"Title", "Doc., Sir...", 60);
		final Text maidenNameTxt = PeopleUiUtils.createRDText(toolkit,
				thirdCmp, "Maiden Name", "Birth Name before getting maried",
				100);
		final Text nickNameTxt = PeopleUiUtils.createRDText(toolkit, thirdCmp,
				"Nickame", "A pseudonym...", 100);
		final Text latinPhoneticTxt = PeopleUiUtils.createRDText(toolkit,
				thirdCmp, "Latin Phonetic",
				"A helper to know how to pronounce this name", 100);

		final AbstractFormPart editPart = new AbstractFormPart() {
			public void refresh() { // update display value
				super.refresh();
				// EDIT PART
				PeopleUiUtils.refreshTextWidgetValue(displayNameTxt, person,
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

				PeopleUiUtils.refreshTextWidgetValue(salutationTxt, person,
						PeopleNames.PEOPLE_SALUTATION);
				PeopleUiUtils.refreshTextWidgetValue(firstNameTxt, person,
						PeopleNames.PEOPLE_FIRST_NAME);
				PeopleUiUtils.refreshTextWidgetValue(middleNameTxt, person,
						PeopleNames.PEOPLE_MIDDLE_NAME);
				PeopleUiUtils.refreshTextWidgetValue(lastNameTxt, person,
						PeopleNames.PEOPLE_LAST_NAME);
				PeopleUiUtils.refreshTextWidgetValue(nickNameTxt, person,
						PeopleNames.PEOPLE_NICKNAME);
				// PeopleUiUtils.refreshTextValue(genderTxt, person,
				// PeopleNames.PEOPLE_GENDER);
				PeopleUiUtils.refreshTextWidgetValue(maidenNameTxt, person,
						PeopleNames.PEOPLE_MAIDEN_NAME);
				PeopleUiUtils.refreshTextWidgetValue(titleTxt, person,
						PeopleNames.PEOPLE_PERSON_TITLE);
				PeopleUiUtils.refreshTextWidgetValue(suffixTxt, person,
						PeopleNames.PEOPLE_NAME_SUFFIX);
				PeopleUiUtils.refreshTextWidgetValue(latinPhoneticTxt, person,
						PeopleNames.PEOPLE_LATIN_PHONETIC_SPELLING);

				// READ ONLY PART
				String roText = personLP.getText(person);
				readOnlyInfoLbl.setText(roText);

				// Manage switch
				if (CommonsJcrUtils.isNodeCheckedOutByMe(person))
					editPanel.moveAbove(readOnlyPanel);
				else
					editPanel.moveBelow(readOnlyPanel);

				readOnlyInfoLbl.pack();
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
					if (CommonsJcrUtils.setJcrProperty(person,
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
					if (CommonsJcrUtils.setJcrProperty(person,
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
					if (CommonsJcrUtils.setJcrProperty(person,
							Property.JCR_TITLE, PropertyType.STRING,
							displayNameTxt.getText())) {
						editPart.markDirty();
					}
			}
		});

		defaultDisplayBtn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean useDefault = defaultDisplayBtn.getSelection();
				if (CommonsJcrUtils.setJcrProperty(person,
						PeopleNames.PEOPLE_USE_DEFAULT_DISPLAY_NAME,
						PropertyType.BOOLEAN, useDefault)) {
					if (useDefault) {
						String displayName = PersonJcrUtils
								.getPersonDisplayName(person);
						CommonsJcrUtils.setJcrProperty(person,
								Property.JCR_TITLE, PropertyType.STRING,
								displayName);
						displayNameTxt.setText(displayName);
						displayNameTxt.setEnabled(false);
					} else
						displayNameTxt.setEnabled(true);

				}
				editPart.markDirty();
			}
		});

		PeopleUiUtils.addTxtModifyListener(editPart, salutationTxt, person,
				PeopleNames.PEOPLE_SALUTATION, PropertyType.STRING);
		PeopleUiUtils.addTxtModifyListener(editPart, middleNameTxt, person,
				PeopleNames.PEOPLE_MIDDLE_NAME, PropertyType.STRING);
		PeopleUiUtils.addTxtModifyListener(editPart, lastNameTxt, person,
				PeopleNames.PEOPLE_LAST_NAME, PropertyType.STRING);
		PeopleUiUtils.addTxtModifyListener(editPart, nickNameTxt, person,
				PeopleNames.PEOPLE_NICKNAME, PropertyType.STRING);
		// entityTK.addTxtModifyListener(editPart, genderTxt, person,
		// PeopleNames.PEOPLE_GENDER, PropertyType.STRING);
		PeopleUiUtils.addTxtModifyListener(editPart, maidenNameTxt, person,
				PeopleNames.PEOPLE_MAIDEN_NAME, PropertyType.STRING);
		PeopleUiUtils.addTxtModifyListener(editPart, titleTxt, person,
				PeopleNames.PEOPLE_PERSON_TITLE, PropertyType.STRING);
		PeopleUiUtils.addTxtModifyListener(editPart, suffixTxt, person,
				PeopleNames.PEOPLE_NAME_SUFFIX, PropertyType.STRING);
		PeopleUiUtils
				.addTxtModifyListener(editPart, latinPhoneticTxt, person,
						PeopleNames.PEOPLE_LATIN_PHONETIC_SPELLING,
						PropertyType.STRING);

		editPart.initialize(getManagedForm());
		getManagedForm().addPart(editPart);

	}
}