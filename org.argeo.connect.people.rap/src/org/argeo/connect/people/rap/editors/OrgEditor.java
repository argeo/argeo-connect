package org.argeo.connect.people.rap.editors;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.rap.PeopleRapConstants;
import org.argeo.connect.people.rap.PeopleRapPlugin;
import org.argeo.connect.people.rap.PeopleRapUtils;
import org.argeo.connect.people.rap.editors.parts.TagLikeListPart;
import org.argeo.connect.people.rap.editors.tabs.ActivityList;
import org.argeo.connect.people.rap.editors.tabs.ContactList;
import org.argeo.connect.people.rap.editors.tabs.HistoryLog;
import org.argeo.connect.people.rap.editors.tabs.JobList;
import org.argeo.connect.people.rap.editors.tabs.OrgAdminInfo;
import org.argeo.connect.people.rap.editors.utils.AbstractEntityCTabEditor;
import org.argeo.connect.people.rap.providers.OrgOverviewLabelProvider;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
import org.eclipse.ui.forms.AbstractFormPart;

/**
 * Editor page that display an organisation with corresponding details
 */
public class OrgEditor extends AbstractEntityCTabEditor {
	final static Log log = LogFactory.getLog(OrgEditor.class);

	public final static String ID = PeopleRapPlugin.PLUGIN_ID + ".orgEditor";

	// Main business Objects
	private Node org;

	// getFormToolkit()s
	// private ListgetFormToolkit() listTK;
	// private LegalInfogetFormToolkit() legalTK;
	// private ActivitygetFormToolkit() activityTK;

	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		org = getNode();

		String shortName = CommonsJcrUtils.get(org,
				PeopleNames.PEOPLE_LEGAL_NAME);
		if (CommonsJcrUtils.checkNotEmptyString(shortName)) {
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

		Composite titleCmp = getFormToolkit().createComposite(parent,
				SWT.NO_FOCUS);
		titleCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		populateTitleComposite(titleCmp);

		// Tag Management
		Composite tagsCmp = new TagLikeListPart(this, parent, SWT.NO_FOCUS,
				getPeopleService(), getPeopleWorkbenchService(),
				PeopleConstants.RESOURCE_TAG, org, PeopleNames.PEOPLE_TAGS,
				"Enter a new tag");

		tagsCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		// Mailing list management
		Composite mlCmp = new TagLikeListPart(this, parent, SWT.NO_FOCUS,
				getPeopleService(), getPeopleWorkbenchService(),
				PeopleTypes.PEOPLE_MAILING_LIST, org,
				PeopleNames.PEOPLE_MAILING_LISTS, "Add a mailing");

		mlCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
	}

	protected void populateTabFolder(CTabFolder folder) {
		// Contact informations
		String tooltip = "Contact information for "
				+ JcrUtils.get(org, PeopleNames.PEOPLE_LEGAL_NAME);
		Composite innerPannel = addTabToFolder(folder, CTAB_COMP_STYLE,
				"Details", PeopleRapConstants.CTAB_CONTACT_DETAILS, tooltip);
		innerPannel.setLayout(EclipseUiUtils.noSpaceGridLayout());
		ContactList cpc = new ContactList(this, innerPannel, SWT.NO_FOCUS,
				getNode(), getPeopleService(), getPeopleWorkbenchService());
		cpc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// Activities and tasks
		tooltip = "Activities and tasks related to "
				+ JcrUtils.get(org, Property.JCR_TITLE);
		innerPannel = addTabToFolder(folder, CTAB_COMP_STYLE, "Activity log",
				PeopleRapConstants.CTAB_ACTIVITY_LOG, tooltip);
		innerPannel.setLayout(EclipseUiUtils.noSpaceGridLayout());
		Composite activitiesCmp = new ActivityList(this, innerPannel, SWT.NONE,
				getPeopleService(), getPeopleWorkbenchService(), org);
		activitiesCmp.setLayoutData(EclipseUiUtils.fillAll());

		// Employees
		tooltip = "Known employees of "
				+ JcrUtils.get(org, PeopleNames.PEOPLE_LEGAL_NAME);
		innerPannel = addTabToFolder(folder, CTAB_COMP_STYLE, "Team",
				PeopleRapConstants.CTAB_EMPLOYEES, tooltip);
		innerPannel.setLayout(EclipseUiUtils.noSpaceGridLayout());
		Composite employeesCmp = new JobList(this, innerPannel, SWT.NONE,
				getPeopleService(), getPeopleWorkbenchService(), org);
		employeesCmp.setLayoutData(EclipseUiUtils.fillAll());

		// Legal informations
		tooltip = "Legal information for "
				+ JcrUtils.get(org, PeopleNames.PEOPLE_LEGAL_NAME);
		innerPannel = addTabToFolder(folder, CTAB_COMP_STYLE, "Admin.",
				PeopleRapConstants.CTAB_LEGAL_INFO, tooltip);
		innerPannel.setLayout(EclipseUiUtils.noSpaceGridLayout());
		Composite legalCmp = new OrgAdminInfo(this, innerPannel, SWT.NONE, org);
		legalCmp.setLayoutData(EclipseUiUtils.fillAll());

		// History panel
		tooltip = "History of information about "
				+ JcrUtils.get(org, Property.JCR_TITLE);
		innerPannel = addTabToFolder(folder, CTAB_COMP_STYLE, "History",
				PeopleRapConstants.CTAB_HISTORY, tooltip);
		innerPannel.setLayout(EclipseUiUtils.noSpaceGridLayout());
		Composite historyLogCmp = new HistoryLog(this, innerPannel, SWT.NONE,
				getPeopleService(), org);
		historyLogCmp.setLayoutData(EclipseUiUtils.fillAll());

	}

	protected void populateTitleComposite(final Composite parent) {
		try {
			parent.setLayout(new FormLayout());
			// READ ONLY
			final Composite roPanelCmp = getFormToolkit().createComposite(
					parent, SWT.NO_FOCUS);
			PeopleRapUtils.setSwitchingFormData(roPanelCmp);
			roPanelCmp.setLayout(new GridLayout());

			// Add a label with info provided by the OrgOverviewLabelProvider
			final Label orgInfoROLbl = getFormToolkit().createLabel(roPanelCmp,
					"", SWT.WRAP);
			orgInfoROLbl.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
			final ColumnLabelProvider orgLP = new OrgOverviewLabelProvider(
					false, getPeopleService(), getPeopleWorkbenchService());

			// EDIT
			final Composite editPanelCmp = getFormToolkit().createComposite(
					parent, SWT.NONE);
			PeopleRapUtils.setSwitchingFormData(editPanelCmp);
			editPanelCmp.setLayout(new GridLayout(2, false));

			// Create edit text
			final Text displayNameTxt = PeopleRapUtils.createGDText(
					getFormToolkit(), editPanelCmp, "Display name",
					"Display name used for this organisation", 300, 1);
			final Button useDistinctDisplayBtn = getFormToolkit().createButton(
					editPanelCmp, "Use a specific display name", SWT.CHECK);
			useDistinctDisplayBtn
					.setToolTipText("Use a display name that is not the legal name");

			final AbstractFormPart editPart = new AbstractFormPart() {
				// Update values on refresh
				public void refresh() {
					super.refresh();
					// EDIT PART
					boolean useDistinct = PeopleRapUtils.refreshFormCheckBox(
							OrgEditor.this, useDistinctDisplayBtn, org,
							PeopleNames.PEOPLE_USE_DISTINCT_DISPLAY_NAME);

					if (useDistinct) {
						PeopleUiUtils.refreshTextWidgetValue(displayNameTxt,
								org, Property.JCR_TITLE);
						displayNameTxt.setEnabled(true);
					} else {
						PeopleUiUtils.refreshTextWidgetValue(displayNameTxt,
								org, PeopleNames.PEOPLE_LEGAL_NAME);
						displayNameTxt.setEnabled(false);
					}
					// READ ONLY PART
					String roText = orgLP.getText(org);
					orgInfoROLbl.setText(roText);

					if (isEditing())
						editPanelCmp.moveAbove(roPanelCmp);
					else
						editPanelCmp.moveBelow(roPanelCmp);
					orgInfoROLbl.pack();
					editPanelCmp.getParent().layout();
				}
			};

			PeopleRapUtils.addModifyListener(displayNameTxt, org,
					Property.JCR_TITLE, editPart);

			useDistinctDisplayBtn.addSelectionListener(new SelectionAdapter() {
				private static final long serialVersionUID = 1L;

				@Override
				public void widgetSelected(SelectionEvent e) {
					boolean defineDistinct = useDistinctDisplayBtn
							.getSelection();
					if (CommonsJcrUtils.setJcrProperty(org,
							PeopleNames.PEOPLE_USE_DISTINCT_DISPLAY_NAME,
							PropertyType.BOOLEAN, defineDistinct)) {
						if (!defineDistinct) {
							String displayName = CommonsJcrUtils.get(org,
									PeopleNames.PEOPLE_LEGAL_NAME);
							CommonsJcrUtils.setJcrProperty(org,
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

			editPart.initialize(getManagedForm());
			getManagedForm().addPart(editPart);
		} catch (Exception e) {
			throw new PeopleException("Cannot create main info section", e);
		}
	}
}