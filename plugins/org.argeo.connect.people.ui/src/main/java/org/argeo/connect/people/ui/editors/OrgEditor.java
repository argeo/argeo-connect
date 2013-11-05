package org.argeo.connect.people.ui.editors;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.ui.JcrUiUtils;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.ui.providers.OrgOverviewLabelProvider;
import org.argeo.connect.people.ui.toolkits.EntityToolkit;
import org.argeo.connect.people.ui.toolkits.LegalInfoToolkit;
import org.argeo.connect.people.ui.toolkits.ListToolkit;
import org.argeo.connect.people.utils.CommonsJcrUtils;
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

	public final static String ID = PeopleUiPlugin.PLUGIN_ID + ".orgEditor";

	// Main business Objects
	private Node org;

	// Toolkits
	private EntityToolkit entityTK;
	private ListToolkit listTK;
	private LegalInfoToolkit legalTK;

	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		org = getEntity();

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
	protected void createToolkits() {
		entityTK = new EntityToolkit(toolkit, getManagedForm());
		listTK = new ListToolkit(toolkit, getManagedForm(), getPeopleService(),
				getPeopleUiService());
		legalTK = new LegalInfoToolkit(toolkit, getManagedForm(), org);
	}

	protected void populateTabFolder(CTabFolder folder) {
		// Contact informations
		String tooltip = "Contact information for "
				+ JcrUtils.get(org, PeopleNames.PEOPLE_LEGAL_NAME);
		Composite innerPannel = addTabToFolder(folder, CTAB_COMP_STYLE,
				"Details", PeopleUiConstants.PANEL_CONTACT_DETAILS, tooltip);
		entityTK.createContactPanelWithNotes(innerPannel, org);

		// Legal informations
		tooltip = "Legal information for "
				+ JcrUtils.get(org, PeopleNames.PEOPLE_LEGAL_NAME);
		innerPannel = addTabToFolder(folder, CTAB_COMP_STYLE, "Admin.",
				PeopleUiConstants.PANEL_LEGAL_INFO, tooltip);
		legalTK.populateLegalInfoPanel(innerPannel);

		// Employees
		tooltip = "Known employees of "
				+ JcrUtils.get(org, PeopleNames.PEOPLE_LEGAL_NAME);
		innerPannel = addTabToFolder(folder, CTAB_COMP_STYLE, "Team",
				PeopleUiConstants.PANEL_EMPLOYEES, tooltip);
		listTK.populateEmployeesPanel(innerPannel, org);
	}

	@Override
	protected void populateMainInfoDetails(final Composite parent) {
		// Branche Management
		Composite tagsCmp = toolkit.createComposite(parent, SWT.NO_FOCUS);
		tagsCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		entityTK.populateTagPanel(tagsCmp, org,
				PeopleNames.PEOPLE_ORG_BRANCHES, "Enter a new branche");

		// keep last update.
		super.populateMainInfoDetails(parent);
	}

	@Override
	protected void populateTitleComposite(final Composite parent) {
		try {
			parent.setLayout(new FormLayout());
			// READ ONLY
			final Composite roPanelCmp = toolkit.createComposite(parent,
					SWT.NO_FOCUS);
			PeopleUiUtils.setSwitchingFormData(roPanelCmp);
			roPanelCmp.setData(RWT.CUSTOM_VARIANT,
					PeopleUiConstants.PEOPLE_CSS_GENERALINFO_COMPOSITE);
			roPanelCmp.setLayout(new GridLayout());

			// Add a label with info provided by the OrgOverviewLabelProvider
			final Label orgInfoROLbl = toolkit.createLabel(roPanelCmp, "",
					SWT.WRAP);
			orgInfoROLbl.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
			final ColumnLabelProvider orgLP = new OrgOverviewLabelProvider(
					false, getPeopleService());

			// EDIT
			final Composite editPanelCmp = toolkit.createComposite(parent,
					SWT.NONE);
			PeopleUiUtils.setSwitchingFormData(editPanelCmp);
			editPanelCmp.setData(RWT.CUSTOM_VARIANT,
					PeopleUiConstants.PEOPLE_CSS_GENERALINFO_COMPOSITE);
			editPanelCmp.setLayout(new GridLayout(2, false));

			// Create edit text
			final Text displayNameTxt = PeopleUiUtils.createGDText(toolkit,
					editPanelCmp, "Display name",
					"Default display name for this organisation", 300, 1);
			final Button defaultDisplayBtn = toolkit.createButton(editPanelCmp,
					"Use default display name", SWT.CHECK);
			defaultDisplayBtn.setToolTipText("Use Legal name by default");

			final AbstractFormPart editPart = new AbstractFormPart() {
				// Update values on refresh
				public void refresh() {
					super.refresh();

					// EDIT PART
					boolean useDefault = PeopleUiUtils.refreshCheckBoxWidget(
							defaultDisplayBtn, org,
							PeopleNames.PEOPLE_USE_DEFAULT_DISPLAY_NAME);

					if (useDefault) {
						PeopleUiUtils.refreshTextValue(displayNameTxt, org,
								PeopleNames.PEOPLE_LEGAL_NAME);
						displayNameTxt.setEnabled(false);
					} else {
						PeopleUiUtils.refreshTextValue(displayNameTxt, org,
								Property.JCR_TITLE);
						displayNameTxt.setEnabled(true);
					}
					// READ ONLY PART
					String roText = orgLP.getText(org);
					orgInfoROLbl.setText(roText);

					if (CommonsJcrUtils.isNodeCheckedOutByMe(org))
						editPanelCmp.moveAbove(roPanelCmp);
					else
						editPanelCmp.moveBelow(roPanelCmp);
					orgInfoROLbl.pack();
					editPanelCmp.getParent().layout();
				}
			};

			PeopleUiUtils.addModifyListener(displayNameTxt, org,
					Property.JCR_TITLE, editPart);

			defaultDisplayBtn.addSelectionListener(new SelectionAdapter() {
				private static final long serialVersionUID = 1L;

				@Override
				public void widgetSelected(SelectionEvent e) {
					boolean useDefault = defaultDisplayBtn.getSelection();
					if (JcrUiUtils.setJcrProperty(org,
							PeopleNames.PEOPLE_USE_DEFAULT_DISPLAY_NAME,
							PropertyType.BOOLEAN, useDefault)) {
						if (useDefault) {
							String displayName = CommonsJcrUtils.get(org,
									PeopleNames.PEOPLE_LEGAL_NAME);
							JcrUiUtils.setJcrProperty(org, Property.JCR_TITLE,
									PropertyType.STRING, displayName);
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