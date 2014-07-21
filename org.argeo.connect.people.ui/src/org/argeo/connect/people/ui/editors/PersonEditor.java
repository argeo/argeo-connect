package org.argeo.connect.people.ui.editors;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.composites.ContactPanelComposite;
import org.argeo.connect.people.ui.composites.TagListComposite;
import org.argeo.connect.people.ui.editors.utils.AbstractEntityCTabEditor;
import org.argeo.connect.people.ui.providers.PersonOverviewLabelProvider;
import org.argeo.connect.people.ui.toolkits.ActivityToolkit;
import org.argeo.connect.people.ui.toolkits.HistoryToolkit;
import org.argeo.connect.people.ui.toolkits.ListToolkit;
import org.argeo.connect.people.ui.utils.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.ResourcesJcrUtils;
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

/**
 * Editor page that display a person with corresponding details
 */
public class PersonEditor extends AbstractEntityCTabEditor implements
		PeopleNames {
	final static Log log = LogFactory.getLog(PersonEditor.class);

	// local constants
	public final static String ID = PeopleUiPlugin.PLUGIN_ID + ".personEditor";
	// Main business Objects
	private Node person;

	// Usefull toolkits
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
		String shortName = CommonsJcrUtils.get(getNode(), PEOPLE_LAST_NAME);
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
		activityTK = new ActivityToolkit(toolkit, getManagedForm(),
				getPeopleService(), getPeopleUiService());
		listTK = new ListToolkit(toolkit, getManagedForm(), getPeopleService(),
				getPeopleUiService());
		historyTK = new HistoryToolkit(toolkit, getManagedForm(),
				getRepository(), getPeopleService(), person);
	}

	@Override
	protected void populateHeader(Composite parent) {
		GridLayout gl = PeopleUiUtils.gridLayoutNoBorder();
		gl.marginBottom = 10;
		parent.setLayout(gl);

		// Main info
		Composite titleCmp = toolkit.createComposite(parent, SWT.NO_FOCUS);
		titleCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		populateTitleComposite(titleCmp);

		// Tag Management
		Composite tagsCmp = new TagListComposite(parent, SWT.NO_FOCUS, toolkit,
				getManagedForm(), getPeopleService(), getPeopleUiService(),
				person, PEOPLE_TAGS, getPeopleService().getResourceBasePath(
						PeopleConstants.RESOURCE_TAG),
				NodeType.NT_UNSTRUCTURED, "Add a tag");
		tagsCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		// Mailing list management
		Composite mlCmp = new TagListComposite(parent, SWT.NO_FOCUS, toolkit,
				getManagedForm(), getPeopleService(), getPeopleUiService(),
				person, PEOPLE_MAILING_LISTS, getPeopleService()
						.getResourceBasePath(PeopleTypes.PEOPLE_MAILING_LIST),
				PeopleTypes.PEOPLE_MAILING_LIST, "Add a mailing");
		mlCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
	}

	@Override
	protected void populateTabFolder(CTabFolder folder) {
		// Contact informations
		String tooltip = "Contact information for "
				+ JcrUtils.get(person, Property.JCR_TITLE);
		Composite innerPannel = addTabToFolder(folder, CTAB_COMP_STYLE,
				"Contact details", PeopleUiConstants.PANEL_CONTACT_DETAILS,
				tooltip);
		innerPannel.setLayout(PeopleUiUtils.gridLayoutNoBorder());
		ContactPanelComposite cpc = new ContactPanelComposite(innerPannel,
				SWT.NO_FOCUS, toolkit, getManagedForm(), getNode(),
				getPeopleService(), getPeopleUiService());
		cpc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

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

		// Fourth Line: Polite form & spoken languages
		Composite fourthCmp = toolkit.createComposite(editPanel, SWT.NO_FOCUS);
		fourthCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		fourthCmp.setLayout(new GridLayout(4, false));

		// Create edit text
		final Text displayNameTxt = PeopleUiUtils.createRDText(toolkit,
				firstCmp, "Display name",
				"Default display name for this person", 300);
		final Button defineDistinctBtn = toolkit.createButton(firstCmp,
				"Define a distinct display name", SWT.CHECK);
		defineDistinctBtn.setToolTipText("Default is \"Firstname LASTNAME\"");

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

		// Fourth Line
		PeopleUiUtils.createBoldLabel(toolkit, fourthCmp, "Form Of Address");
		Composite politeCmp = toolkit.createComposite(fourthCmp, SWT.NO_FOCUS);
		politeCmp
				.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = layout.marginTop = layout.marginBottom = layout.verticalSpacing = 0;
		politeCmp.setLayout(layout);
		final Button formalBtn = new Button(politeCmp, SWT.RADIO);
		formalBtn.setText("Formal");
		final Button informalBtn = new Button(politeCmp, SWT.RADIO);
		informalBtn.setText("Informal");

		PeopleUiUtils.createBoldLabel(toolkit, fourthCmp, "Language");
		Composite languageCmp = toolkit
				.createComposite(fourthCmp, SWT.NO_FOCUS);
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

				Boolean defineDistinct = CommonsJcrUtils.getBooleanValue(
						person, PEOPLE_USE_DISTINCT_DISPLAY_NAME);
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
							PEOPLE_FIRST_NAME, PropertyType.STRING,
							firstNameTxt.getText())) {
						Boolean defineDistinct = CommonsJcrUtils
								.getBooleanValue(person,
										PEOPLE_USE_DISTINCT_DISPLAY_NAME);
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
					if (CommonsJcrUtils.setJcrProperty(person,
							PEOPLE_LAST_NAME, PropertyType.STRING,
							lastNameTxt.getText())) {
						Boolean defineDistinct = CommonsJcrUtils
								.getBooleanValue(person,
										PEOPLE_USE_DISTINCT_DISPLAY_NAME);
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
				boolean useDefault = defineDistinctBtn.getSelection();
				// if use default, do nothing
				if (!useDefault)
					if (CommonsJcrUtils.setJcrProperty(person,
							Property.JCR_TITLE, PropertyType.STRING,
							displayNameTxt.getText())) {
						editPart.markDirty();
					}
			}
		});

		defineDistinctBtn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean defineDistinct = defineDistinctBtn.getSelection();
				if (CommonsJcrUtils.setJcrProperty(person,
						PEOPLE_USE_DISTINCT_DISPLAY_NAME,
						PropertyType.BOOLEAN, defineDistinct)) {
					if (!defineDistinct) {
						String displayName = getPeopleService().getDisplayName(
								person);
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
				PEOPLE_SALUTATION, PropertyType.STRING);
		PeopleUiUtils.addTxtModifyListener(editPart, middleNameTxt, person,
				PEOPLE_MIDDLE_NAME, PropertyType.STRING);
		PeopleUiUtils.addTxtModifyListener(editPart, lastNameTxt, person,
				PEOPLE_LAST_NAME, PropertyType.STRING);
		PeopleUiUtils.addTxtModifyListener(editPart, nickNameTxt, person,
				PEOPLE_NICKNAME, PropertyType.STRING);
		// entityTK.addTxtModifyListener(editPart, genderTxt, person,
		// PEOPLE_GENDER, PropertyType.STRING);
		PeopleUiUtils.addTxtModifyListener(editPart, maidenNameTxt, person,
				PEOPLE_MAIDEN_NAME, PropertyType.STRING);
		PeopleUiUtils.addTxtModifyListener(editPart, titleTxt, person,
				PEOPLE_HONORIFIC_TITLE, PropertyType.STRING);
		PeopleUiUtils.addTxtModifyListener(editPart, suffixTxt, person,
				PEOPLE_NAME_SUFFIX, PropertyType.STRING);
		PeopleUiUtils.addTxtModifyListener(editPart, latinPhoneticTxt, person,
				PEOPLE_LATIN_PHONETIC_SPELLING, PropertyType.STRING);

		Listener formalRadioListener = new Listener() {
			private static final long serialVersionUID = 1L;

			public void handleEvent(Event event) {
				Button btn = (Button) event.widget;
				if (!btn.getSelection())
					return;
				boolean value = "Formal".equals(btn.getText());
				if (CommonsJcrUtils.setJcrProperty(person,
						PEOPLE_USE_POLITE_FORM, PropertyType.BOOLEAN, value))
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
					String newValueIso = ResourcesJcrUtils
							.getLangIsoFromEnLabel(getPeopleService(),
									getSession(), btn.getText());

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
			button.setEnabled(CommonsJcrUtils.isNodeCheckedOutByMe(entity));
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
				if (isoVal != null
						&& ResourcesJcrUtils.getLangEnLabelFromIso(
								getPeopleService(), getSession(), isoVal)
								.equals(button.getText()))
					tmp = true;
			}
			button.setSelection(tmp);
			button.setEnabled(CommonsJcrUtils.isNodeCheckedOutByMe(entity));
		} catch (RepositoryException re) {
			throw new PeopleException("Error getting polite form property on "
					+ entity, re);
		}
	}
}