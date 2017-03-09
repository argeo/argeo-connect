package org.argeo.people.workbench.rap.parts;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.activities.ActivitiesService;
import org.argeo.activities.workbench.parts.ActivityList;
import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.ConnectConstants;
import org.argeo.connect.resources.ResourcesNames;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.ui.ConnectUiConstants;
import org.argeo.connect.ui.ConnectUiUtils;
import org.argeo.connect.ui.util.LazyCTabControl;
import org.argeo.connect.ui.widgets.TagLikeListPart;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.workbench.ConnectWorkbenchUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;
import org.argeo.people.PeopleException;
import org.argeo.people.PeopleNames;
import org.argeo.people.PeopleService;
import org.argeo.people.PeopleTypes;
import org.argeo.people.workbench.rap.PeopleRapConstants;
import org.argeo.people.workbench.rap.PeopleRapPlugin;
import org.argeo.people.workbench.rap.composites.MailingListListPart;
import org.argeo.people.workbench.rap.providers.PersonOverviewLabelProvider;
import org.argeo.people.workbench.rap.util.AbstractPeopleWithImgEditor;
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
public class PersonEditor extends AbstractPeopleWithImgEditor implements PeopleNames {
	final static Log log = LogFactory.getLog(PersonEditor.class);
	public final static String ID = PeopleRapPlugin.PLUGIN_ID + ".personEditor";

	// Context
	private ActivitiesService activitiesService;
	private PeopleService peopleService;
	private Node person;

	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		person = getNode();
	}

	@Override
	protected void updatePartName() {
		String shortName = ConnectJcrUtils.get(getNode(), Property.JCR_TITLE);
		if (EclipseUiUtils.notEmpty(shortName)) {
			if (shortName.length() > SHORT_NAME_LENGHT)
				shortName = shortName.substring(0, SHORT_NAME_LENGHT - 1) + "...";
			setPartName(shortName);
		}
	}

	@Override
	protected void populateHeader(Composite parent) {
		GridLayout gl = EclipseUiUtils.noSpaceGridLayout();
		gl.marginBottom = 10;
		parent.setLayout(gl);

		// Main info
		Composite titleCmp = getFormToolkit().createComposite(parent, SWT.NO_FOCUS);
		titleCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		populateTitleComposite(titleCmp);

		// Tags Management
		Composite tagsCmp = new TagLikeListPart(this, parent, SWT.NO_FOCUS, getResourcesService(),
				getSystemWorkbenchService(), ConnectConstants.RESOURCE_TAG, person, ResourcesNames.CONNECT_TAGS,
				"Add a tag");
		tagsCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		// Mailing lists management
		Composite mlCmp = new MailingListListPart(this, parent, SWT.NO_FOCUS, getResourcesService(),
				getSystemWorkbenchService(), PeopleTypes.PEOPLE_MAILING_LIST, person, PEOPLE_MAILING_LISTS,
				"Add a mailing");
		mlCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
	}

	@Override
	protected void populateTabFolder(CTabFolder folder) {
		// Contact informations
		String tooltip = "Contact information for " + JcrUtils.get(person, Property.JCR_TITLE);
		LazyCTabControl cpc = new ContactListCTab(folder, SWT.NO_FOCUS, this, getNode(), getResourcesService(),
				getPeopleService(), getSystemWorkbenchService());
		cpc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		addLazyTabToFolder(folder, cpc, "Contact details", PeopleRapConstants.CTAB_CONTACT_DETAILS, tooltip);

		// Activities and tasks
		tooltip = "Activities and tasks related to " + JcrUtils.get(person, Property.JCR_TITLE);
		LazyCTabControl activitiesCmp = new ActivityList(folder, SWT.NO_FOCUS, this, getUserAdminService(),
				getResourcesService(), getActivitiesService(), getSystemWorkbenchService(), person);
		activitiesCmp.setLayoutData(EclipseUiUtils.fillAll());
		addLazyTabToFolder(folder, activitiesCmp, "Activity log", PeopleRapConstants.CTAB_ACTIVITY_LOG, tooltip);

		// Jobs panel
		tooltip = "Organisations linked to " + JcrUtils.get(person, Property.JCR_TITLE);
		LazyCTabControl crewCmp = new JobListCTab(folder, SWT.NO_FOCUS, this, getResourcesService(), getPeopleService(),
				getSystemWorkbenchService(), person);
		crewCmp.setLayoutData(EclipseUiUtils.fillAll());
		addLazyTabToFolder(folder, crewCmp, "Organisations", PeopleRapConstants.CTAB_JOBS, tooltip);
	}

	protected void populateTitleComposite(Composite parent) {
		parent.setLayout(new FormLayout());

		// READ ONLY
		final Composite readOnlyPanel = getFormToolkit().createComposite(parent, SWT.NO_FOCUS);
		ConnectWorkbenchUtils.setSwitchingFormData(readOnlyPanel);
		readOnlyPanel.setLayout(new GridLayout());

		// Add a label with info provided by the PersonOverviewLabelProvider
		final Label readOnlyInfoLbl = getFormToolkit().createLabel(readOnlyPanel, "", SWT.WRAP);
		CmsUtils.markup(readOnlyInfoLbl);
		final ColumnLabelProvider personLP = new PersonOverviewLabelProvider(
				ConnectUiConstants.LIST_TYPE_OVERVIEW_TITLE, getResourcesService(), getPeopleService(),
				getSystemWorkbenchService());

		// EDIT
		final Composite editPanelCmp = getFormToolkit().createComposite(parent, SWT.NO_FOCUS);
		ConnectWorkbenchUtils.setSwitchingFormData(editPanelCmp);
		editPanelCmp.setLayout(EclipseUiUtils.noSpaceGridLayout());

		// First Line - display Name management
		Composite firstCmp = getFormToolkit().createComposite(editPanelCmp, SWT.NO_FOCUS);
		firstCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		RowLayout rl = new RowLayout(SWT.HORIZONTAL);
		rl.wrap = false;
		firstCmp.setLayout(rl);

		// Second Line main names
		Composite secondCmp = getFormToolkit().createComposite(editPanelCmp, SWT.NO_FOCUS);
		secondCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		rl = new RowLayout(SWT.HORIZONTAL);
		rl.wrap = false;
		secondCmp.setLayout(rl);

		// Third Line: other Names
		Composite thirdCmp = getFormToolkit().createComposite(editPanelCmp, SWT.NO_FOCUS);
		thirdCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		rl = new RowLayout(SWT.HORIZONTAL);
		rl.wrap = false;
		thirdCmp.setLayout(rl);

		// Fourth Line: Polite form & spoken languages
		Composite fourthCmp = getFormToolkit().createComposite(editPanelCmp, SWT.NO_FOCUS);
		fourthCmp.setLayoutData(EclipseUiUtils.fillWidth());
		fourthCmp.setLayout(new GridLayout(4, false));

		// Create edit text
		final Text displayNameTxt = ConnectWorkbenchUtils.createRDText(getFormToolkit(), firstCmp, "Display name",
				"Default display name for this person", 300);
		final Button defineDistinctBtn = getFormToolkit().createButton(firstCmp, "Define a distinct display name",
				SWT.CHECK);
		if (!EclipseUiUtils.isEmpty(ConnectJcrUtils.get(person, PeopleNames.PEOPLE_DISPLAY_NAME)))
			defineDistinctBtn.setSelection(true);

		final Text salutationTxt = ConnectWorkbenchUtils.createRDText(getFormToolkit(), secondCmp, "Salutation",
				"Mr, Mrs...", 60);
		final Text firstNameTxt = ConnectWorkbenchUtils.createRDText(getFormToolkit(), secondCmp, "First Name",
				"Usual first name for this person", 100);
		final Text middleNameTxt = ConnectWorkbenchUtils.createRDText(getFormToolkit(), secondCmp, "Middle Name",
				"The second name if it exists", 100);
		final Text lastNameTxt = ConnectWorkbenchUtils.createRDText(getFormToolkit(), secondCmp, "Last Name",
				"Usual last name for this person", 100);
		final Text suffixTxt = ConnectWorkbenchUtils.createRDText(getFormToolkit(), secondCmp, "Suffix",
				"Junior, the third...", 80);

		// final Text genderTxt = entityTK.createText(thirdCmp, "Gender", "...",
		// 80);
		final Text titleTxt = ConnectWorkbenchUtils.createRDText(getFormToolkit(), thirdCmp, "Title", "Doc., Sir...",
				60);
		final Text maidenNameTxt = ConnectWorkbenchUtils.createRDText(getFormToolkit(), thirdCmp, "Maiden Name",
				"Birth Name before getting maried", 100);
		final Text nickNameTxt = ConnectWorkbenchUtils.createRDText(getFormToolkit(), thirdCmp, "Nickame",
				"A pseudonym...", 100);
		final Text latinPhoneticTxt = ConnectWorkbenchUtils.createRDText(getFormToolkit(), thirdCmp, "Latin Phonetic",
				"A helper to know how to pronounce this name", 100);

		// Fourth Line
		ConnectWorkbenchUtils.createBoldLabel(getFormToolkit(), fourthCmp, "Form Of Address");
		Composite politeCmp = getFormToolkit().createComposite(fourthCmp, SWT.NO_FOCUS);
		politeCmp.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = layout.marginTop = layout.marginBottom = layout.verticalSpacing = 0;
		politeCmp.setLayout(layout);
		final Button formalBtn = new Button(politeCmp, SWT.RADIO);
		formalBtn.setText("Formal");
		final Button informalBtn = new Button(politeCmp, SWT.RADIO);
		informalBtn.setText("Informal");

		ConnectWorkbenchUtils.createBoldLabel(getFormToolkit(), fourthCmp, "Language");
		Composite languageCmp = getFormToolkit().createComposite(fourthCmp, SWT.NO_FOCUS);
		languageCmp.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		layout = new GridLayout(2, false);
		layout.marginHeight = layout.marginTop = layout.marginBottom = layout.verticalSpacing = 0;
		languageCmp.setLayout(layout);
		final Button deBtn = new Button(languageCmp, SWT.RADIO);
		deBtn.setText("German");
		final Button enBtn = new Button(languageCmp, SWT.RADIO);
		enBtn.setText("English");

		final AbstractFormPart editPart = new AbstractFormPart() {
			public void refresh() { // update display value
				// EDIT PART
				boolean useDistinct = defineDistinctBtn.getSelection();

				if (useDistinct)
					ConnectUiUtils.refreshTextWidgetValue(displayNameTxt, person, PeopleNames.PEOPLE_DISPLAY_NAME);
				else
					displayNameTxt.setText(getPeopleService().getPersonService().getDefaultDisplayName(person));
				displayNameTxt.setEnabled(useDistinct);

				ConnectUiUtils.refreshTextWidgetValue(salutationTxt, person, PEOPLE_SALUTATION);
				ConnectUiUtils.refreshTextWidgetValue(firstNameTxt, person, PEOPLE_FIRST_NAME);
				ConnectUiUtils.refreshTextWidgetValue(middleNameTxt, person, PEOPLE_MIDDLE_NAME);
				ConnectUiUtils.refreshTextWidgetValue(lastNameTxt, person, PEOPLE_LAST_NAME);
				ConnectUiUtils.refreshTextWidgetValue(nickNameTxt, person, PEOPLE_NICKNAME);
				ConnectUiUtils.refreshTextWidgetValue(maidenNameTxt, person, PEOPLE_MAIDEN_NAME);
				ConnectUiUtils.refreshTextWidgetValue(titleTxt, person, PEOPLE_HONORIFIC_TITLE);
				ConnectUiUtils.refreshTextWidgetValue(suffixTxt, person, PEOPLE_NAME_SUFFIX);
				ConnectUiUtils.refreshTextWidgetValue(latinPhoneticTxt, person, PEOPLE_LATIN_PHONETIC_SPELLING);

				refreshFormalRadio(formalBtn, person);
				refreshFormalRadio(informalBtn, person);
				refreshLangRadio(deBtn, person);
				refreshLangRadio(enBtn, person);

				// READ ONLY PART
				String roText = personLP.getText(person);
				readOnlyInfoLbl.setText(roText);

				// Manage switch
				if (isEditing())
					editPanelCmp.moveAbove(readOnlyPanel);
				else
					editPanelCmp.moveBelow(readOnlyPanel);

				readOnlyInfoLbl.pack();
				editPanelCmp.getParent().layout();
				super.refresh();
			}
		};

		// Listeners

		ConnectWorkbenchUtils.addModifyListener(firstNameTxt, person, PeopleNames.PEOPLE_FIRST_NAME, editPart);
		ConnectWorkbenchUtils.addModifyListener(lastNameTxt, person, PeopleNames.PEOPLE_LAST_NAME, editPart);
		addDNameModifyListener(displayNameTxt, defineDistinctBtn, person, PeopleNames.PEOPLE_DISPLAY_NAME, editPart);

		defineDistinctBtn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					boolean defineDistinct = defineDistinctBtn.getSelection();
					String displayName = getPeopleService().getPersonService().getDefaultDisplayName(person);
					if (defineDistinct) {
						ConnectJcrUtils.setJcrProperty(person, PeopleNames.PEOPLE_DISPLAY_NAME, PropertyType.STRING,
								displayName);
					} else if (person.hasProperty(PeopleNames.PEOPLE_DISPLAY_NAME)) {
						displayNameTxt.setText(displayName);
						person.getProperty(PeopleNames.PEOPLE_DISPLAY_NAME).remove();
					}
					displayNameTxt.setEnabled(defineDistinct);
					editPart.markDirty();
				} catch (RepositoryException e1) {
					throw new PeopleException("Unable to reset display name management for " + person, e1);
				}
			}
		});

		ConnectWorkbenchUtils.addTxtModifyListener(editPart, salutationTxt, person, PEOPLE_SALUTATION,
				PropertyType.STRING);
		ConnectWorkbenchUtils.addTxtModifyListener(editPart, middleNameTxt, person, PEOPLE_MIDDLE_NAME,
				PropertyType.STRING);
		ConnectWorkbenchUtils.addTxtModifyListener(editPart, lastNameTxt, person, PEOPLE_LAST_NAME,
				PropertyType.STRING);
		ConnectWorkbenchUtils.addTxtModifyListener(editPart, nickNameTxt, person, PEOPLE_NICKNAME, PropertyType.STRING);
		// entityTK.addTxtModifyListener(editPart, genderTxt, person,
		// PEOPLE_GENDER, PropertyType.STRING);
		ConnectWorkbenchUtils.addTxtModifyListener(editPart, maidenNameTxt, person, PEOPLE_MAIDEN_NAME,
				PropertyType.STRING);
		ConnectWorkbenchUtils.addTxtModifyListener(editPart, titleTxt, person, PEOPLE_HONORIFIC_TITLE,
				PropertyType.STRING);
		ConnectWorkbenchUtils.addTxtModifyListener(editPart, suffixTxt, person, PEOPLE_NAME_SUFFIX,
				PropertyType.STRING);
		ConnectWorkbenchUtils.addTxtModifyListener(editPart, latinPhoneticTxt, person, PEOPLE_LATIN_PHONETIC_SPELLING,
				PropertyType.STRING);

		Listener formalRadioListener = new Listener() {
			private static final long serialVersionUID = 1L;

			public void handleEvent(Event event) {
				Button btn = (Button) event.widget;
				if (!btn.getSelection())
					return;
				boolean value = "Formal".equals(btn.getText());
				if (ConnectJcrUtils.setJcrProperty(person, PEOPLE_USE_POLITE_FORM, PropertyType.BOOLEAN, value))
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

					Session session = ConnectJcrUtils.getSession(person);
					String newValueIso = getResourcesService().getEncodedTagCodeFromValue(session,
							ConnectConstants.RESOURCE_LANG, btn.getText());
					String oldValueIso = null;
					if (person.hasProperty(PEOPLE_SPOKEN_LANGUAGES)) {
						Value[] values = person.getProperty(PEOPLE_SPOKEN_LANGUAGES).getValues();
						if (values[0] != null)
							oldValueIso = values[0].getString();
					}
					if (!newValueIso.equals(oldValueIso)) {
						String[] newVals = { newValueIso };
						person.setProperty(PEOPLE_SPOKEN_LANGUAGES, newVals);
						editPart.markDirty();
					}
				} catch (RepositoryException e) {
					throw new PeopleException("Unable to update " + "spooken language on " + person, e);
				}
			}

		};
		deBtn.addListener(SWT.Selection, langRadioListener);
		enBtn.addListener(SWT.Selection, langRadioListener);

		editPart.initialize(getManagedForm());
		getManagedForm().addPart(editPart);
	}

	private void addDNameModifyListener(final Text text, final Button useDistinctBtn, final Node node,
			final String propName, final AbstractFormPart part) {
		text.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void modifyText(ModifyEvent event) {
				if (useDistinctBtn.getSelection()) {
					if (ConnectJcrUtils.setJcrProperty(node, propName, PropertyType.STRING, text.getText()))
						part.markDirty();
				}
			}
		});
	}

	private void refreshFormalRadio(Button button, Node entity) {
		Boolean tmp = false;
		try {
			if (entity.hasProperty(PEOPLE_USE_POLITE_FORM)) {
				boolean value = entity.getProperty(PEOPLE_USE_POLITE_FORM).getBoolean();
				if ("Formal".equals(button.getText()) && value || "Informal".equals(button.getText()) && !value)
					tmp = true;
			}
			button.setSelection(tmp);
			button.setEnabled(isEditing());
		} catch (RepositoryException re) {
			throw new PeopleException("Error getting polite form property on " + entity, re);
		}
	}

	private void refreshLangRadio(Button button, Node entity) {
		Boolean tmp = false;
		try {
			if (entity.hasProperty(PEOPLE_SPOKEN_LANGUAGES)) {
				Value[] values = entity.getProperty(PEOPLE_SPOKEN_LANGUAGES).getValues();
				String isoVal = null;
				if (values[0] != null)
					isoVal = values[0].getString();
				ResourcesService rs = getResourcesService();
				if (isoVal != null && rs.getEncodedTagValue(getSession(), ConnectConstants.RESOURCE_LANG, isoVal)
						.equals(button.getText()))
					tmp = true;
			}
			button.setSelection(tmp);
			button.setEnabled(isEditing());
		} catch (RepositoryException re) {
			throw new PeopleException("Error getting polite form property on " + entity, re);
		}
	}

	protected ActivitiesService getActivitiesService() {
		return activitiesService;
	}

	protected PeopleService getPeopleService() {
		return peopleService;
	}

	/* DEPENDENCY INJECTION */
	public void setActivitiesService(ActivitiesService activitiesService) {
		this.activitiesService = activitiesService;
	}

	public void setPeopleService(PeopleService peopleService) {
		this.peopleService = peopleService;
	}
}
