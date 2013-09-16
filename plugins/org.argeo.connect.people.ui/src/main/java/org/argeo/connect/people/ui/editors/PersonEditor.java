package org.argeo.connect.people.ui.editors;

import javax.jcr.Node;
import javax.jcr.PropertyType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.ui.providers.PersonOverviewLabelProvider;
import org.argeo.connect.people.ui.toolkits.EntityToolkit;
import org.argeo.connect.people.ui.toolkits.ListToolkit;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

/**
 * Default connect Person editor page
 */
public class PersonEditor extends AbstractEntityEditor {
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
		person = getNode();
		setPartName(JcrUtils.getStringPropertyQuietly(person,
				PeopleNames.PEOPLE_LAST_NAME));
	}
	
	@Override
	protected void createToolkits() {
		entityTK = new EntityToolkit(toolkit, getManagedForm());
		listTK = new ListToolkit(toolkit, getManagedForm(),
				getPeopleServices(), getPeopleUiServices());
	}

	protected void populateTabFolder(CTabFolder folder) {
		// Contact informations
		String tooltip = "Contact information for "
				+ JcrUtils.get(person, PeopleNames.PEOPLE_LAST_NAME);
		Composite innerPannel = addTabToFolder(folder, SWT.NO_FOCUS,
				"Contact details", PeopleUiConstants.PANEL_CONTACT_DETAILS,
				tooltip);
		entityTK.populateContactPanelWithNotes(innerPannel, person);

		// Jobs panel
		tooltip = "Organisations linked to "
				+ JcrUtils.get(person, PeopleNames.PEOPLE_LAST_NAME);
		innerPannel = addTabToFolder(folder, SWT.NO_FOCUS, "Organisations",
				PeopleUiConstants.PANEL_JOBS, tooltip);
		listTK.populateJobsPanel(innerPannel, person);

		// Film participation panel
		// TODO: move this in specific film project
		tooltip = "Films related to "
				+ JcrUtils.get(person, PeopleNames.PEOPLE_LAST_NAME);
		innerPannel = addTabToFolder(folder, SWT.NO_FOCUS, "Films",
				PeopleUiConstants.PANEL_PRODUCTIONS, tooltip);
		listTK.populateFilmsPanel(innerPannel, person);
		folder.layout();
	}

	@Override
	protected void populateMainInfoComposite(final Composite switchingPanel) {
		switchingPanel.setLayout(new FormLayout());

		// READ ONLY
		final Composite readOnlyPanel = toolkit.createComposite(switchingPanel,
				SWT.NO_FOCUS);
		PeopleUiUtils.setSwitchingFormData(readOnlyPanel);
		readOnlyPanel.setData(RWT.CUSTOM_VARIANT,
				PeopleUiConstants.PEOPLE_CSS_GENERALINFO_COMPOSITE);
		readOnlyPanel.setLayout(new GridLayout());

		// Add a label with info provided by the PersonOverviewLabelProvider
		final Label readOnlyInfoLbl = toolkit.createLabel(readOnlyPanel, "",
				SWT.WRAP);
		readOnlyInfoLbl.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
		final ColumnLabelProvider personLP = new PersonOverviewLabelProvider(
				false, getPeopleServices());

		// EDIT
		final Composite editPanel = toolkit.createComposite(switchingPanel,
				SWT.NO_FOCUS);
		PeopleUiUtils.setSwitchingFormData(editPanel);
		editPanel.setData(RWT.CUSTOM_VARIANT,
				PeopleUiConstants.PEOPLE_CSS_GENERALINFO_COMPOSITE);

		// intern layout
		RowLayout rl = new RowLayout(SWT.HORIZONTAL);
		rl.wrap = true;
		editPanel.setLayout(rl);

		// Create edit text
		final Text salutationTxt = entityTK.createText(editPanel, "Salutation",
				"Mr, Mrs...", 60);
		final Text titleTxt = entityTK.createText(editPanel, "Title",
				"Doc., Sir...", 60);
		final Text firstNameTxt = entityTK.createText(editPanel, "First Name",
				"Usual first name for this person", 100);
		final Text lastNameTxt = entityTK.createText(editPanel, "Last Name",
				"Usual last name for this person", 100);
		final Text suffixTxt = entityTK.createText(editPanel, "Suffix",
				"Junior, the third...", 80);

		final EntityAbstractFormPart editPart = new EntityAbstractFormPart() {
			public void refresh() { // update display value
				super.refresh();
				// EDIT PART
				entityTK.refreshTextValue(salutationTxt, person,
						PeopleNames.PEOPLE_SALUTATION);
				entityTK.refreshTextValue(firstNameTxt, person,
						PeopleNames.PEOPLE_FIRST_NAME);
				entityTK.refreshTextValue(lastNameTxt, person,
						PeopleNames.PEOPLE_LAST_NAME);
				entityTK.refreshTextValue(titleTxt, person,
						PeopleNames.PEOPLE_PERSON_TITLE);
				entityTK.refreshTextValue(suffixTxt, person,
						PeopleNames.PEOPLE_NAME_SUFFIX);

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
		entityTK.addTxtModifyListener(editPart, salutationTxt, person,
				PeopleNames.PEOPLE_SALUTATION, PropertyType.STRING);
		entityTK.addTxtModifyListener(editPart, titleTxt, person,
				PeopleNames.PEOPLE_PERSON_TITLE, PropertyType.STRING);
		entityTK.addTxtModifyListener(editPart, firstNameTxt, person,
				PeopleNames.PEOPLE_FIRST_NAME, PropertyType.STRING);
		entityTK.addTxtModifyListener(editPart, lastNameTxt, person,
				PeopleNames.PEOPLE_LAST_NAME, PropertyType.STRING);
		entityTK.addTxtModifyListener(editPart, suffixTxt, person,
				PeopleNames.PEOPLE_NAME_SUFFIX, PropertyType.STRING);

		getManagedForm().addPart(editPart);
	}
}