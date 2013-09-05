package org.argeo.connect.people.ui.editors;

import javax.jcr.Node;
import javax.jcr.PropertyType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.ui.JcrUiUtils;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.ui.providers.PersonOverviewLabelProvider;
import org.argeo.connect.people.ui.toolkits.EntityPanelToolkit;
import org.argeo.connect.people.ui.toolkits.ListPanelToolkit;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
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
		// Create usefull toolkits
		EntityPanelToolkit entityPanelToolkit = new EntityPanelToolkit(toolkit,
				getManagedForm());
		ListPanelToolkit listPanelToolkit = new ListPanelToolkit(toolkit,
				getManagedForm(), getPeopleServices(), getPeopleUiServices());

		// Contact informations
		String tooltip = "Contact information for "
				+ JcrUtils.get(person, PeopleNames.PEOPLE_LAST_NAME);
		Composite innerPannel = addTabToFolder(folder, SWT.NO_FOCUS,
				"Contact details", PeopleUiConstants.PANEL_CONTACT_DETAILS,
				tooltip);
		entityPanelToolkit.populateContactPanelWithNotes(innerPannel, person);

		// Jobs panel
		tooltip = "Organisations linked to "
				+ JcrUtils.get(person, PeopleNames.PEOPLE_LAST_NAME);
		innerPannel = addTabToFolder(folder, SWT.NO_FOCUS, "Organisations",
				PeopleUiConstants.PANEL_JOBS, tooltip);
		listPanelToolkit.populateJobsPanel(innerPannel, person);

		// Films panel
		tooltip = "Films related to "
				+ JcrUtils.get(person, PeopleNames.PEOPLE_LAST_NAME);
		innerPannel = addTabToFolder(folder, SWT.NO_FOCUS, "Films",
				PeopleUiConstants.PANEL_PRODUCTIONS, tooltip);
		listPanelToolkit.populateFilmsPanel(innerPannel, person);
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

		// Add a label with info provided by the OrgOverviewLabelProvider
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
		final Text salutationTxt = createText(editPanel, "Salutation",
				"Mr, Mrs...", 40);
		final Text titleTxt = createText(editPanel, "Title", "Doc., Sir...", 60);
		final Text firstNameTxt = createText(editPanel, "First Name",
				"Usual first name for this person", 100);
		final Text lastNameTxt = createText(editPanel, "Last Name",
				"Usual last name for this person", 100);
		final Text suffixTxt = createText(editPanel, "Suffix",
				"Junior, the third...", 80);

		final EntityAbstractFormPart editPart = new EntityAbstractFormPart() {
			public void refresh() { // update display value
				super.refresh();
				// EDIT PART
				refreshTextValue(salutationTxt, person, PeopleNames.PEOPLE_SALUTATION);
				refreshTextValue(firstNameTxt, person, PeopleNames.PEOPLE_FIRST_NAME);
				refreshTextValue(lastNameTxt, person, PeopleNames.PEOPLE_LAST_NAME);
				refreshTextValue(titleTxt, person, PeopleNames.PEOPLE_PERSON_TITLE);
				refreshTextValue(suffixTxt, person, PeopleNames.PEOPLE_NAME_SUFFIX);

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

	private void refreshTextValue(Text text, Node entity, String propName) {
		String tmpStr = CommonsJcrUtils.getStringValue(entity, propName);
		if (CommonsJcrUtils.checkNotEmptyString(tmpStr))
			text.setText(tmpStr);
	}

	private Text createText(Composite parent, String msg, String toolTip,
			int width) {
		Text text = toolkit.createText(parent, "", SWT.BORDER | SWT.SINGLE
				| SWT.LEFT);
		text.setMessage(msg);
		text.setToolTipText(toolTip);
		text.setLayoutData(new RowData(width, SWT.DEFAULT));
		return text;
	}
}