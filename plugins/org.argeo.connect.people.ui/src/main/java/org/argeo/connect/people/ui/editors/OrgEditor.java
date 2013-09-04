package org.argeo.connect.people.ui.editors;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.ui.JcrUiUtils;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.ui.providers.OrgOverviewLabelProvider;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

/**
 * Editor page that display an organisation with corresponding details
 */
public class OrgEditor extends AbstractEntityEditor {
	final static Log log = LogFactory.getLog(OrgEditor.class);

	public final static String ID = PeopleUiPlugin.PLUGIN_ID + ".orgEditor";

	// Main business Objects
	private Node org;

	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		org = getNode();

		String shortName = JcrUtils.get(org, PeopleNames.PEOPLE_LEGAL_NAME);
		if (shortName != null) {
			if (shortName.length() > SHORT_NAME_LENGHT)
				shortName = shortName.substring(0, SHORT_NAME_LENGHT - 1)
						+ "...";
			setPartName(shortName);
		}
	}

	protected void populateTabFolder(CTabFolder folder) {
		// Contact informations
		String tooltip = "Contact information for "
				+ JcrUtils.get(org, PeopleNames.PEOPLE_LEGAL_NAME);
		Composite innerPannel = addTabToFolder(folder, SWT.NO_FOCUS,
				"Org. details", "people:contactDetails", tooltip);
		EntityPanelToolkit.populateContactPanelWithNotes(innerPannel, org,
				toolkit, getManagedForm());
	}
	
	protected void populateMainInfoComposite(final Composite parent) {
		try {
			parent.setLayout(new FormLayout());
			// READ ONLY PANEL
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
					false);

			// EDIT PANEL
			final Composite editPanelCmp = toolkit.createComposite(parent,
					SWT.NONE);
			PeopleUiUtils.setSwitchingFormData(editPanelCmp);
			editPanelCmp.setData(RWT.CUSTOM_VARIANT,
					PeopleUiConstants.PEOPLE_CSS_GENERALINFO_COMPOSITE);
			editPanelCmp.setLayout(new GridLayout(2, false));

			// Legal Name
			Label lbl = toolkit.createLabel(editPanelCmp, "Name", SWT.NONE);
			lbl.setLayoutData(new GridData());
			final Text legalNameTxt = toolkit.createText(editPanelCmp, "",
					SWT.BORDER | SWT.SINGLE | SWT.LEFT);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.widthHint = 250;
			legalNameTxt.setLayoutData(gd);

			// Legal Status
			lbl = toolkit.createLabel(editPanelCmp, "Legal Status", SWT.NONE);
			lbl.setLayoutData(new GridData());
			final Text legalStatusTxt = toolkit.createText(editPanelCmp, "",
					SWT.BORDER | SWT.SINGLE | SWT.LEFT);
			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.widthHint = 250;
			legalStatusTxt.setLayoutData(gd);

			final EntityAbstractFormPart editPart = new EntityAbstractFormPart() {
				// Update values on refresh
				public void refresh() {
					super.refresh();

					// EDIT PART
					legalNameTxt.setText(JcrUtils.get(org,
							PeopleNames.PEOPLE_LEGAL_NAME));

					String legalStatus = CommonsJcrUtils.getStringValue(org,
							PeopleNames.PEOPLE_LEGAL_STATUS);
					if (legalStatus != null)
						legalStatusTxt.setText(legalStatus);

					// READ ONLY PART
					String roText = orgLP.getText(org);
					orgInfoROLbl.setText(roText);

					try {
						if (org.isCheckedOut())
							editPanelCmp.moveAbove(roPanelCmp);
						else
							editPanelCmp.moveBelow(roPanelCmp);
					} catch (RepositoryException e) {
						throw new PeopleException(
								"Unable to get checked out status", e);
					}
					editPanelCmp.getParent().layout();
					roPanelCmp.getParent().layout();
				}
			};

			// Listeners
			legalNameTxt.addModifyListener(new ModifyListener() {
				private static final long serialVersionUID = 1L;

				@Override
				public void modifyText(ModifyEvent event) {
					if (JcrUiUtils.setJcrProperty(org,
							PeopleNames.PEOPLE_LEGAL_NAME, PropertyType.STRING,
							legalNameTxt.getText()))
						editPart.markDirty();
				}
			});

			legalStatusTxt.addModifyListener(new ModifyListener() {
				private static final long serialVersionUID = 1L;

				@Override
				public void modifyText(ModifyEvent event) {
					if (JcrUiUtils.setJcrProperty(org,
							PeopleNames.PEOPLE_LEGAL_STATUS,
							PropertyType.STRING, legalStatusTxt.getText()))
						editPart.markDirty();
				}
			});
			getManagedForm().addPart(editPart);
		} catch (Exception e) {
			throw new PeopleException("Cannot create main info section", e);
		}
	}

	
}