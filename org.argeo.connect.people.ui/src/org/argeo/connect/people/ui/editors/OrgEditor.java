package org.argeo.connect.people.ui.editors;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.nodetype.NodeType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.composites.TagListComposite;
import org.argeo.connect.people.ui.editors.utils.AbstractEntityCTabEditor;
import org.argeo.connect.people.ui.providers.OrgOverviewLabelProvider;
import org.argeo.connect.people.ui.toolkits.ActivityToolkit;
import org.argeo.connect.people.ui.toolkits.ContactToolkit;
import org.argeo.connect.people.ui.toolkits.LegalInfoToolkit;
import org.argeo.connect.people.ui.toolkits.ListToolkit;
import org.argeo.connect.people.ui.utils.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;
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
	private ContactToolkit contactTK;
	private ListToolkit listTK;
	private LegalInfoToolkit legalTK;
	private ActivityToolkit activityTK;

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
	protected void createToolkits() {
		contactTK = new ContactToolkit(toolkit, getManagedForm(),
				getPeopleService(), getPeopleUiService());
		listTK = new ListToolkit(toolkit, getManagedForm(), getPeopleService(),
				getPeopleUiService());
		legalTK = new LegalInfoToolkit(toolkit, getManagedForm(), org);
		activityTK = new ActivityToolkit(toolkit, getManagedForm(),
				getPeopleService(), getPeopleUiService());
	}

	@Override
	protected void populateHeader(Composite parent) {
		GridLayout gl = PeopleUiUtils.gridLayoutNoBorder();
		gl.marginBottom = 10;
		parent.setLayout(gl);

		Composite titleCmp = toolkit.createComposite(parent, SWT.NO_FOCUS);
		titleCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		populateTitleComposite(titleCmp);

		// Tag Management
		Composite tagsCmp = new TagListComposite(parent, SWT.NO_FOCUS, toolkit,
				getManagedForm(), getPeopleService(), getPeopleUiService(),
				org, PeopleNames.PEOPLE_TAGS, getPeopleService()
						.getResourceBasePath(PeopleConstants.RESOURCE_TAG),
				NodeType.NT_UNSTRUCTURED, "Add a tag");
		tagsCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		// Mailing list management
		Composite mlCmp = new TagListComposite(parent, SWT.NO_FOCUS, toolkit,
				getManagedForm(), getPeopleService(), getPeopleUiService(),
				org, PeopleNames.PEOPLE_ML_INSTANCES, getPeopleService()
						.getResourceBasePath(PeopleTypes.PEOPLE_ML_INSTANCE),
				PeopleTypes.PEOPLE_ML_INSTANCE, "Add a mailing");
		mlCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
	}

	protected void populateTabFolder(CTabFolder folder) {
		// Contact informations
		String tooltip = "Contact information for "
				+ JcrUtils.get(org, PeopleNames.PEOPLE_LEGAL_NAME);
		Composite innerPannel = addTabToFolder(folder, CTAB_COMP_STYLE,
				"Details", PeopleUiConstants.PANEL_CONTACT_DETAILS, tooltip);
		contactTK.createContactPanelWithNotes(innerPannel, org);

		// Activities and tasks
		tooltip = "Activities and tasks related to "
				+ JcrUtils.get(org, Property.JCR_TITLE);
		innerPannel = addTabToFolder(folder, CTAB_COMP_STYLE, "Activity log",
				PeopleUiConstants.PANEL_ACTIVITY_LOG, tooltip);
		activityTK.populateActivityLogPanel(innerPannel, org);

		// Employees
		tooltip = "Known employees of "
				+ JcrUtils.get(org, PeopleNames.PEOPLE_LEGAL_NAME);
		innerPannel = addTabToFolder(folder, CTAB_COMP_STYLE, "Team",
				PeopleUiConstants.PANEL_EMPLOYEES, tooltip);
		listTK.populateEmployeesPanel(innerPannel, org);

		// Legal informations
		tooltip = "Legal information for "
				+ JcrUtils.get(org, PeopleNames.PEOPLE_LEGAL_NAME);
		innerPannel = addTabToFolder(folder, CTAB_COMP_STYLE, "Admin.",
				PeopleUiConstants.PANEL_LEGAL_INFO, tooltip);
		legalTK.populateLegalInfoPanel(innerPannel);
	}

	protected void populateTitleComposite(final Composite parent) {
		try {
			parent.setLayout(new FormLayout());
			// READ ONLY
			final Composite roPanelCmp = toolkit.createComposite(parent,
					SWT.NO_FOCUS);
			PeopleUiUtils.setSwitchingFormData(roPanelCmp);
			roPanelCmp.setLayout(new GridLayout());

			// Add a label with info provided by the OrgOverviewLabelProvider
			final Label orgInfoROLbl = toolkit.createLabel(roPanelCmp, "",
					SWT.WRAP);
			orgInfoROLbl
					.setData(PeopleUiConstants.MARKUP_ENABLED, Boolean.TRUE);
			final ColumnLabelProvider orgLP = new OrgOverviewLabelProvider(
					false, getPeopleService());

			// EDIT
			final Composite editPanelCmp = toolkit.createComposite(parent,
					SWT.NONE);
			PeopleUiUtils.setSwitchingFormData(editPanelCmp);
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
						PeopleUiUtils.refreshTextWidgetValue(displayNameTxt,
								org, PeopleNames.PEOPLE_LEGAL_NAME);
						displayNameTxt.setEnabled(false);
					} else {
						PeopleUiUtils.refreshTextWidgetValue(displayNameTxt,
								org, Property.JCR_TITLE);
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
					if (CommonsJcrUtils.setJcrProperty(org,
							PeopleNames.PEOPLE_USE_DEFAULT_DISPLAY_NAME,
							PropertyType.BOOLEAN, useDefault)) {
						if (useDefault) {
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