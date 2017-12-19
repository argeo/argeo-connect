package org.argeo.people.workbench.rap.parts;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.activities.ActivitiesService;
import org.argeo.activities.workbench.parts.RelatedActivityList;
import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.ui.util.LazyCTabControl;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.workbench.ConnectWorkbenchUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;
import org.argeo.people.PeopleException;
import org.argeo.people.PeopleNames;
import org.argeo.people.PeopleService;
import org.argeo.people.workbench.rap.PeopleRapConstants;
import org.argeo.people.workbench.rap.PeopleRapPlugin;
import org.argeo.people.workbench.rap.providers.OrgOverviewLabelProvider;
import org.argeo.people.workbench.rap.util.AbstractPeopleWithImgEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

/** Display an organisation with corresponding details */
public class OrgEditor extends AbstractPeopleWithImgEditor {
	public final static String ID = PeopleRapPlugin.PLUGIN_ID + ".orgEditor";
	private BundleContext bc = FrameworkUtil.getBundle(OrgEditor.class).getBundleContext();

	final static Log log = LogFactory.getLog(OrgEditor.class);

	// Context
	private ActivitiesService activitiesService;
	private PeopleService peopleService;
	private Node org;

	public OrgEditor() {
		peopleService = bc.getService(bc.getServiceReference(PeopleService.class));
		activitiesService = bc.getService(bc.getServiceReference(ActivitiesService.class));
	}

	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		org = getNode();
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

		Composite titleCmp = getFormToolkit().createComposite(parent, SWT.NO_FOCUS);
		titleCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		populateTitleComposite(titleCmp);

		// Tags Management
//		Composite tagsCmp = new TagLikeListPart(this, parent, SWT.NO_FOCUS, getResourcesService(),
//				getSystemWorkbenchService(), ConnectConstants.RESOURCE_TAG, org, ResourcesNames.CONNECT_TAGS,
//				"Add a tag");
//		tagsCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
//
//		// Mailing lists management
//		Composite mlCmp = new MailingListListPart(this, parent, SWT.NO_FOCUS, getResourcesService(),
//				getSystemWorkbenchService(), PeopleTypes.PEOPLE_MAILING_LIST, org, PeopleNames.PEOPLE_MAILING_LISTS,
//				"Add a mailing");
//		mlCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
	}

	protected void populateTabFolder(CTabFolder folder) {
		// Contact informations
		String tooltip = "Contact information for " + JcrUtils.get(org, PeopleNames.PEOPLE_LEGAL_NAME);
		LazyCTabControl cpc = new ContactListCTab(folder, SWT.NO_FOCUS, this, getNode(), getResourcesService(),
				getPeopleService(), getSystemWorkbenchService());
		cpc.setLayoutData(EclipseUiUtils.fillAll());
		addLazyTabToFolder(folder, cpc, "Details", PeopleRapConstants.CTAB_CONTACT_DETAILS, tooltip);

		// Activities and tasks
		tooltip = "Activities and tasks related to " + JcrUtils.get(org, Property.JCR_TITLE);
		LazyCTabControl activitiesCmp = new RelatedActivityList(folder, SWT.NO_FOCUS, this, getUserAdminService(),
				getResourcesService(), getActivitiesService(), getSystemAppService(), getSystemWorkbenchService(), org);
		activitiesCmp.setLayoutData(EclipseUiUtils.fillAll());
		addLazyTabToFolder(folder, activitiesCmp, "Activity log", PeopleRapConstants.CTAB_ACTIVITY_LOG, tooltip);

		// Employees
		tooltip = "Known employees of " + JcrUtils.get(org, PeopleNames.PEOPLE_LEGAL_NAME);
		LazyCTabControl employeesCmp = new JobListCTab(folder, SWT.NO_FOCUS, this, getResourcesService(),
				getPeopleService(), getSystemWorkbenchService(), org);
		employeesCmp.setLayoutData(EclipseUiUtils.fillAll());
		addLazyTabToFolder(folder, employeesCmp, "Team", PeopleRapConstants.CTAB_EMPLOYEES, tooltip);

		// Legal informations
		tooltip = "Legal information for " + JcrUtils.get(org, PeopleNames.PEOPLE_LEGAL_NAME);
		LazyCTabControl legalCmp = new OrgAdminInfoCTab(folder, SWT.NO_FOCUS, this, org);
		legalCmp.setLayoutData(EclipseUiUtils.fillAll());
		addLazyTabToFolder(folder, legalCmp, "Admin.", PeopleRapConstants.CTAB_LEGAL_INFO, tooltip);
	}

	protected void populateTitleComposite(final Composite parent) {
		try {
			parent.setLayout(new FormLayout());
			// READ ONLY
			final Composite roPanelCmp = getFormToolkit().createComposite(parent, SWT.NO_FOCUS);
			ConnectWorkbenchUtils.setSwitchingFormData(roPanelCmp);
			roPanelCmp.setLayout(new GridLayout());

			// Add a label with info provided by the OrgOverviewLabelProvider
			final Label orgInfoROLbl = getFormToolkit().createLabel(roPanelCmp, "", SWT.WRAP);
			CmsUtils.markup(orgInfoROLbl);
			final ColumnLabelProvider orgLP = new OrgOverviewLabelProvider(false, getResourcesService(),
					getPeopleService(), getSystemWorkbenchService());

			// EDIT
			final Composite editPanelCmp = getFormToolkit().createComposite(parent, SWT.NONE);
			ConnectWorkbenchUtils.setSwitchingFormData(editPanelCmp);
			editPanelCmp.setLayout(new GridLayout(2, false));

			// Create edit text
//			final Text displayNameTxt = ConnectWorkbenchUtils.createGDText(getFormToolkit(), editPanelCmp,
//					"Display name", "Display name used for this organisation", 300, 1);
//			final Button useDistinctDisplayBtn = getFormToolkit().createButton(editPanelCmp,
//					"Use a specific display name", SWT.CHECK);
//			useDistinctDisplayBtn.setToolTipText("Use a display name that is not the legal name");
//			// Initialise checkbox
//			if (!EclipseUiUtils.isEmpty(ConnectJcrUtils.get(org, PeopleNames.PEOPLE_DISPLAY_NAME)))
//				useDistinctDisplayBtn.setSelection(true);

			final AbstractFormPart editPart = new AbstractFormPart() {
				// Update values on refresh
				public void refresh() {
					// EDIT PART
//					boolean useDistinct = useDistinctDisplayBtn.getSelection();
//					if (useDistinct)
//						ConnectUiUtils.refreshTextWidgetValue(displayNameTxt, org, PeopleNames.PEOPLE_DISPLAY_NAME);
//					else
//						displayNameTxt.setText(getPeopleService().getPersonService().getDefaultDisplayName(org));
//					displayNameTxt.setEnabled(useDistinct);

					// READ ONLY PART
					String roText = orgLP.getText(org);
					orgInfoROLbl.setText(roText);

//					if (isEditing())
//						editPanelCmp.moveAbove(roPanelCmp);
//					else
						editPanelCmp.moveBelow(roPanelCmp);
					orgInfoROLbl.pack();
					editPanelCmp.getParent().layout();
					super.refresh();
				}
			};

//			useDistinctDisplayBtn.addSelectionListener(new SelectionAdapter() {
//				private static final long serialVersionUID = 1L;
//
//				@Override
//				public void widgetSelected(SelectionEvent e) {
//					try {
//						boolean defineDistinct = useDistinctDisplayBtn.getSelection();
//						String dName = ConnectJcrUtils.get(org, PeopleNames.PEOPLE_LEGAL_NAME);
//						if (defineDistinct) {
//							ConnectJcrUtils.setJcrProperty(org, PeopleNames.PEOPLE_DISPLAY_NAME, PropertyType.STRING,
//									dName);
//						} else if (org.hasProperty(PeopleNames.PEOPLE_DISPLAY_NAME)) {
//							displayNameTxt.setText(dName);
//							org.getProperty(PeopleNames.PEOPLE_DISPLAY_NAME).remove();
//						}
//						displayNameTxt.setEnabled(defineDistinct);
//						editPart.markDirty();
//					} catch (RepositoryException e1) {
//						throw new PeopleException("Unable to reset display name management for " + org, e1);
//					}
//				}
//			});
//
//			addDNameModifyListener(displayNameTxt, useDistinctDisplayBtn, org, PeopleNames.PEOPLE_DISPLAY_NAME,
//					editPart);

			editPart.initialize(getManagedForm());
			getManagedForm().addPart(editPart);
		} catch (Exception e) {
			throw new PeopleException("Cannot create main info section", e);
		}
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
