package org.argeo.connect.people.rap.editors;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ResourceService;
import org.argeo.connect.people.rap.PeopleRapConstants;
import org.argeo.connect.people.rap.PeopleRapPlugin;
import org.argeo.connect.people.rap.PeopleRapUtils;
import org.argeo.connect.people.rap.editors.parts.TagLikeListPart;
import org.argeo.connect.people.rap.editors.tabs.ActivityList;
import org.argeo.connect.people.rap.editors.tabs.ContactList;
import org.argeo.connect.people.rap.editors.tabs.JobList;
import org.argeo.connect.people.rap.editors.util.AbstractPeopleCTabEditor;
import org.argeo.connect.people.rap.providers.PersonOverviewLabelProvider;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.util.JcrUiUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.AbstractFormPart;

/** Edit a person with corresponding details */
public class PersonEditor extends AbstractPeopleCTabEditor implements
		PeopleNames {
	final static Log log = LogFactory.getLog(PersonEditor.class);
	public final static String ID = PeopleRapPlugin.PLUGIN_ID + ".personEditor";

	// Context
	private Node person;

	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		person = getNode();
	}

	@Override
	protected void updatePartName() {
		String shortName = JcrUiUtils.get(getNode(), PEOPLE_LAST_NAME);
		if (EclipseUiUtils.isEmpty(shortName)) {
			shortName = JcrUiUtils.get(getNode(), Property.JCR_TITLE);
		}
		if (EclipseUiUtils.notEmpty(shortName)) {
			if (shortName.length() > SHORT_NAME_LENGHT)
				shortName = shortName.substring(0, SHORT_NAME_LENGHT - 1)
						+ "...";
			setPartName(shortName);
		}
	}

	@Override
	protected void populateHeader(Composite parent) {
		GridLayout gl = EclipseUiUtils.noSpaceGridLayout();
		gl.marginBottom = 10;
		parent.setLayout(gl);

		// Main info
		Composite titleCmp = getFormToolkit().createComposite(parent,
				SWT.NO_FOCUS);
		titleCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		populateTitleComposite(titleCmp);

		// Tag Management
		Composite tagsCmp = new TagLikeListPart(this, parent, SWT.NO_FOCUS,
				getPeopleService(), getPeopleWorkbenchService(),
				PeopleConstants.RESOURCE_TAG, person, PeopleNames.PEOPLE_TAGS,
				"Add a tag");

		tagsCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		// Mailing list management
		Composite mlCmp = new TagLikeListPart(this, parent, SWT.NO_FOCUS,
				getPeopleService(), getPeopleWorkbenchService(),
				PeopleTypes.PEOPLE_MAILING_LIST, person, PEOPLE_MAILING_LISTS,
				"Add a mailing");

		mlCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
	}

	@Override
	protected void populateTabFolder(CTabFolder folder) {
		// Contact informations
		String tooltip = "Contact information for "
				+ JcrUtils.get(person, Property.JCR_TITLE);
		Composite innerPannel = addTabToFolder(folder, PeopleRapConstants.CTAB_COMP_STYLE,
				"Contact details", PeopleRapConstants.CTAB_CONTACT_DETAILS,
				tooltip);
		innerPannel.setLayout(EclipseUiUtils.noSpaceGridLayout());
		ContactList cpc = new ContactList(this, innerPannel, SWT.NO_FOCUS,
				getNode(), getPeopleService(), getPeopleWorkbenchService());
		cpc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// Activities and tasks
		tooltip = "Activities and tasks related to "
				+ JcrUtils.get(person, Property.JCR_TITLE);
		innerPannel = addTabToFolder(folder, PeopleRapConstants.CTAB_COMP_STYLE, "Activity log",
				PeopleRapConstants.CTAB_ACTIVITY_LOG, tooltip);
		innerPannel.setLayout(EclipseUiUtils.noSpaceGridLayout());
		Composite activitiesCmp = new ActivityList(this, innerPannel, SWT.NONE,
				getPeopleService(), getPeopleWorkbenchService(), person);
		activitiesCmp.setLayoutData(EclipseUiUtils.fillAll());

		// Jobs panel
		tooltip = "Organisations linked to "
				+ JcrUtils.get(person, Property.JCR_TITLE);
		innerPannel = addTabToFolder(folder, PeopleRapConstants.CTAB_COMP_STYLE, "Organisations",
				PeopleRapConstants.CTAB_JOBS, tooltip);
		innerPannel.setLayout(EclipseUiUtils.noSpaceGridLayout());
		Composite crewCmp = new JobList(this, innerPannel, SWT.NONE,
				getPeopleService(), getPeopleWorkbenchService(), person);
		crewCmp.setLayoutData(EclipseUiUtils.fillAll());

		// // History panel
		// tooltip = "History of information about "
		// + JcrUtils.get(person, Property.JCR_TITLE);
		// innerPannel = addTabToFolder(folder, CTAB_COMP_STYLE, "History",
		// PeopleRapConstants.CTAB_HISTORY, tooltip);
		// innerPannel.setLayout(EclipseUiUtils.noSpaceGridLayout());
		// Composite historyLogCmp = new HistoryLog(this, innerPannel, SWT.NONE,
		// getPeopleService(), person);
		// historyLogCmp.setLayoutData(EclipseUiUtils.fillAll());
		//
		// // folder.layout();
	}

	protected void populateTitleComposite(Composite parent) {
		parent.setLayout(new FormLayout());

		// READ ONLY
		final Composite readOnlyPanel = getFormToolkit().createComposite(
				parent, SWT.NO_FOCUS);
		PeopleRapUtils.setSwitchingFormData(readOnlyPanel);
		readOnlyPanel.setLayout(new GridLayout());

		// Add a label with info provided by the PersonOverviewLabelProvider
		final Label readOnlyInfoLbl = getFormToolkit().createLabel(
				readOnlyPanel, "", SWT.WRAP);
		CmsUtils.markup(readOnlyInfoLbl);
		final ColumnLabelProvider personLP = new PersonOverviewLabelProvider(
				PeopleRapConstants.LIST_TYPE_OVERVIEW_TITLE,
				getPeopleService(), getPeopleWorkbenchService());

		// EDIT
		final Composite editPanel = getFormToolkit().createComposite(parent,
				SWT.NO_FOCUS);
		PeopleRapUtils.setSwitchingFormData(editPanel);
		editPanel.setLayout(EclipseUiUtils.noSpaceGridLayout());

		// First Line - display Name management
		Composite firstCmp = getFormToolkit().createComposite(editPanel,
				SWT.NO_FOCUS);
		firstCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		RowLayout rl = new RowLayout(SWT.HORIZONTAL);
		rl.wrap = false;
		firstCmp.setLayout(rl);

		// Second Line main names
		Composite secondCmp = getFormToolkit().createComposite(editPanel,
				SWT.NO_FOCUS);
		secondCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		rl = new RowLayout(SWT.HORIZONTAL);
		rl.wrap = false;
		secondCmp.setLayout(rl);

		// Third Line: other Names
		Composite thirdCmp = getFormToolkit().createComposite(editPanel,
				SWT.NO_FOCUS);
		thirdCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		rl = new RowLayout(SWT.HORIZONTAL);
		rl.wrap = false;
		thirdCmp.setLayout(rl);

		// Fourth Line: Polite form & spoken languages
		Composite fourthCmp = getFormToolkit().createComposite(editPanel,
				SWT.NO_FOCUS);
		fourthCmp.setLayoutData(EclipseUiUtils.fillWidth());
		fourthCmp.setLayout(new GridLayout(4, false));

		// Create edit text
		final Text displayNameTxt = PeopleRapUtils.createRDText(
				getFormToolkit(), firstCmp, "Display name",
				"Default display name for this person", 300);
		final Button defineDistinctBtn = getFormToolkit().createButton(
				firstCmp, "Define a distinct display name", SWT.CHECK);
		defineDistinctBtn.setToolTipText("Default is \"Firstname LASTNAME\"");

		final Text salutationTxt = PeopleRapUtils.createRDText(
				getFormToolkit(), secondCmp, "Salutation", "Mr, Mrs...", 60);
		final Text firstNameTxt = PeopleRapUtils.createRDText(getFormToolkit(),
				secondCmp, "First Name", "Usual first name for this person",
				100);
		final Text middleNameTxt = PeopleRapUtils.createRDText(
				getFormToolkit(), secondCmp, "Middle Name",
				"The second name if it exists", 100);
		final Text lastNameTxt = PeopleRapUtils.createRDText(getFormToolkit(),
				secondCmp, "Last Name", "Usual last name for this person", 100);
		final Text suffixTxt = PeopleRapUtils.createRDText(getFormToolkit(),
				secondCmp, "Suffix", "Junior, the third...", 80);

		// final Text genderTxt = entityTK.createText(thirdCmp, "Gender", "...",
		// 80);
		final Text titleTxt = PeopleRapUtils.createRDText(getFormToolkit(),
				thirdCmp, "Title", "Doc., Sir...", 60);
		final Text maidenNameTxt = PeopleRapUtils.createRDText(
				getFormToolkit(), thirdCmp, "Maiden Name",
				"Birth Name before getting maried", 100);
		final Text nickNameTxt = PeopleRapUtils.createRDText(getFormToolkit(),
				thirdCmp, "Nickame", "A pseudonym...", 100);
		final Text latinPhoneticTxt = PeopleRapUtils.createRDText(
				getFormToolkit(), thirdCmp, "Latin Phonetic",
				"A helper to know how to pronounce this name", 100);

		// Fourth Line
		PeopleRapUtils.createBoldLabel(getFormToolkit(), fourthCmp,
				"Form Of Address");
		Composite politeCmp = getFormToolkit().createComposite(fourthCmp,
				SWT.NO_FOCUS);
		politeCmp
				.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = layout.marginTop = layout.marginBottom = layout.verticalSpacing = 0;
		politeCmp.setLayout(layout);
		final Button formalBtn = new Button(politeCmp, SWT.RADIO);
		formalBtn.setText("Formal");
		final Button informalBtn = new Button(politeCmp, SWT.RADIO);
		informalBtn.setText("Informal");

		PeopleRapUtils.createBoldLabel(getFormToolkit(), fourthCmp, "Language");
		Composite languageCmp = getFormToolkit().createComposite(fourthCmp,
				SWT.NO_FOCUS);
		languageCmp.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true,
				false));
		layout = new GridLayout(2, false);
		layout.marginHeight = layout.marginTop = layout.marginBottom = layout.verticalSpacing = 0;
		languageCmp.setLayout(layout);
		final Button deBtn = new Button(languageCmp, SWT.RADIO);
		deBtn.setText("German");
		final Button enBtn = new Button(languageCmp, SWT.RADIO);
		enBtn.setText("English");

		final AbstractFormPart editPart = new AbstractFormPart() {
			public void refresh() { // update display value
				super.refresh();
				// EDIT PART
				PeopleUiUtils.refreshTextWidgetValue(displayNameTxt, person,
						Property.JCR_TITLE);

				Boolean defineDistinct = JcrUiUtils.getBooleanValue(person,
						PEOPLE_USE_DISTINCT_DISPLAY_NAME);
				if (defineDistinct == null)
					defineDistinct = false;
				displayNameTxt.setEnabled(defineDistinct);
				defineDistinctBtn.setSelection(defineDistinct);

				PeopleUiUtils.refreshTextWidgetValue(salutationTxt, person,
						PEOPLE_SALUTATION);
				PeopleUiUtils.refreshTextWidgetValue(firstNameTxt, person,
						PEOPLE_FIRST_NAME);
				PeopleUiUtils.refreshTextWidgetValue(middleNameTxt, person,
						PEOPLE_MIDDLE_NAME);
				PeopleUiUtils.refreshTextWidgetValue(lastNameTxt, person,
						PEOPLE_LAST_NAME);
				PeopleUiUtils.refreshTextWidgetValue(nickNameTxt, person,
						PEOPLE_NICKNAME);
				// PeopleUiUtils.refreshTextValue(genderTxt, person,
				// PEOPLE_GENDER);
				PeopleUiUtils.refreshTextWidgetValue(maidenNameTxt, person,
						PEOPLE_MAIDEN_NAME);
				PeopleUiUtils.refreshTextWidgetValue(titleTxt, person,
						PEOPLE_HONORIFIC_TITLE);
				PeopleUiUtils.refreshTextWidgetValue(suffixTxt, person,
						PEOPLE_NAME_SUFFIX);
				PeopleUiUtils.refreshTextWidgetValue(latinPhoneticTxt, person,
						PEOPLE_LATIN_PHONETIC_SPELLING);

				refreshFormalRadio(formalBtn, person);
				refreshFormalRadio(informalBtn, person);
				refreshLangRadio(deBtn, person);
				refreshLangRadio(enBtn, person);

				// READ ONLY PART
				String roText = personLP.getText(person);
				readOnlyInfoLbl.setText(roText);

				// Manage switch
				if (isEditing())
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
					if (JcrUiUtils.setJcrProperty(person, PEOPLE_FIRST_NAME,
							PropertyType.STRING, firstNameTxt.getText())) {
						Boolean defineDistinct = JcrUiUtils.getBooleanValue(
								person, PEOPLE_USE_DISTINCT_DISPLAY_NAME);
						if (defineDistinct == null || !defineDistinct) {
							String displayName = getPeopleService()
									.getDisplayName(person);
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
					if (JcrUiUtils.setJcrProperty(person, PEOPLE_LAST_NAME,
							PropertyType.STRING, lastNameTxt.getText())) {
						Boolean defineDistinct = JcrUiUtils.getBooleanValue(
								person, PEOPLE_USE_DISTINCT_DISPLAY_NAME);
						if (defineDistinct == null || !defineDistinct) {
							String displayName = getPeopleService()
									.getDisplayName(person);
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
				boolean defineDistinct = defineDistinctBtn.getSelection();
				if (defineDistinct)
					if (JcrUiUtils.setJcrProperty(person, Property.JCR_TITLE,
							PropertyType.STRING, displayNameTxt.getText())) {
						editPart.markDirty();
					}
			}
		});

		defineDistinctBtn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean defineDistinct = defineDistinctBtn.getSelection();
				if (JcrUiUtils.setJcrProperty(person,
						PEOPLE_USE_DISTINCT_DISPLAY_NAME, PropertyType.BOOLEAN,
						defineDistinct)) {
					if (!defineDistinct) {
						String displayName = getPeopleService().getDisplayName(
								person);
						JcrUiUtils.setJcrProperty(person, Property.JCR_TITLE,
								PropertyType.STRING, displayName);
						displayNameTxt.setText(displayName);
						displayNameTxt.setEnabled(false);
					} else
						displayNameTxt.setEnabled(true);

				}
				editPart.markDirty();
			}
		});

		PeopleRapUtils.addTxtModifyListener(editPart, salutationTxt, person,
				PEOPLE_SALUTATION, PropertyType.STRING);
		PeopleRapUtils.addTxtModifyListener(editPart, middleNameTxt, person,
				PEOPLE_MIDDLE_NAME, PropertyType.STRING);
		PeopleRapUtils.addTxtModifyListener(editPart, lastNameTxt, person,
				PEOPLE_LAST_NAME, PropertyType.STRING);
		PeopleRapUtils.addTxtModifyListener(editPart, nickNameTxt, person,
				PEOPLE_NICKNAME, PropertyType.STRING);
		// entityTK.addTxtModifyListener(editPart, genderTxt, person,
		// PEOPLE_GENDER, PropertyType.STRING);
		PeopleRapUtils.addTxtModifyListener(editPart, maidenNameTxt, person,
				PEOPLE_MAIDEN_NAME, PropertyType.STRING);
		PeopleRapUtils.addTxtModifyListener(editPart, titleTxt, person,
				PEOPLE_HONORIFIC_TITLE, PropertyType.STRING);
		PeopleRapUtils.addTxtModifyListener(editPart, suffixTxt, person,
				PEOPLE_NAME_SUFFIX, PropertyType.STRING);
		PeopleRapUtils.addTxtModifyListener(editPart, latinPhoneticTxt, person,
				PEOPLE_LATIN_PHONETIC_SPELLING, PropertyType.STRING);

		Listener formalRadioListener = new Listener() {
			private static final long serialVersionUID = 1L;

			public void handleEvent(Event event) {
				Button btn = (Button) event.widget;
				if (!btn.getSelection())
					return;
				boolean value = "Formal".equals(btn.getText());
				if (JcrUiUtils.setJcrProperty(person, PEOPLE_USE_POLITE_FORM,
						PropertyType.BOOLEAN, value))
					editPart.markDirty();
			}
		};
		formalBtn.addListener(SWT.Selection, formalRadioListener);
		informalBtn.addListener(SWT.Selection, formalRadioListener);

		Listener langRadioListener = new Listener() {
			private static final long serialVersionUID = 1L;

			public void handleEvent(Event event) {
				Button btn = (Button) event.widget;
				try {
					if (!btn.getSelection())
						return;

					Session session = JcrUiUtils.getSession(person);
					String newValueIso = getPeopleService()
							.getResourceService().getEncodedTagCodeFromValue(
									session, PeopleConstants.RESOURCE_LANG,
									btn.getText());
					String oldValueIso = null;
					if (person.hasProperty(PEOPLE_SPOKEN_LANGUAGES)) {
						Value[] values = person.getProperty(
								PEOPLE_SPOKEN_LANGUAGES).getValues();
						if (values[0] != null)
							oldValueIso = values[0].getString();
					}
					if (!newValueIso.equals(oldValueIso)) {
						String[] newVals = { newValueIso };
						person.setProperty(PEOPLE_SPOKEN_LANGUAGES, newVals);
						editPart.markDirty();
					}
				} catch (RepositoryException e) {
					throw new PeopleException("Unable to update "
							+ "spooken language on " + person, e);
				}
			}

		};
		deBtn.addListener(SWT.Selection, langRadioListener);
		enBtn.addListener(SWT.Selection, langRadioListener);

		editPart.initialize(getManagedForm());
		getManagedForm().addPart(editPart);

	}

	private void refreshFormalRadio(Button button, Node entity) {
		Boolean tmp = false;
		try {
			if (entity.hasProperty(PEOPLE_USE_POLITE_FORM)) {
				boolean value = entity.getProperty(PEOPLE_USE_POLITE_FORM)
						.getBoolean();
				if ("Formal".equals(button.getText()) && value
						|| "Informal".equals(button.getText()) && !value)
					tmp = true;
			}
			button.setSelection(tmp);
			button.setEnabled(isEditing());
		} catch (RepositoryException re) {
			throw new PeopleException("Error getting polite form property on "
					+ entity, re);
		}
	}

	private void refreshLangRadio(Button button, Node entity) {
		Boolean tmp = false;
		try {
			if (entity.hasProperty(PEOPLE_SPOKEN_LANGUAGES)) {
				Value[] values = entity.getProperty(PEOPLE_SPOKEN_LANGUAGES)
						.getValues();
				String isoVal = null;
				if (values[0] != null)
					isoVal = values[0].getString();
				ResourceService rs = getPeopleService().getResourceService();
				if (isoVal != null
						&& rs.getEncodedTagValue(getSession(),
								PeopleConstants.RESOURCE_LANG, isoVal).equals(
								button.getText()))
					tmp = true;
			}
			button.setSelection(tmp);
			button.setEnabled(isEditing());
		} catch (RepositoryException re) {
			throw new PeopleException("Error getting polite form property on "
					+ entity, re);
		}
	}
}