package org.argeo.connect.people.ui.editors;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.providers.GroupLabelProvider;
import org.argeo.connect.people.ui.toolkits.GroupToolkit;
import org.argeo.connect.people.ui.utils.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.PeopleJcrUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.AbstractFormPart;

/**
 * Editor page that display a group with corresponding details
 */
public class GroupEditor extends AbstractEntityCTabEditor {
	final static Log log = LogFactory.getLog(GroupEditor.class);

	public final static String ID = PeopleUiPlugin.PLUGIN_ID + ".groupEditor";

	// Main business Objects
	private Node group;
	private GroupToolkit groupToolkit;

	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		group = getEntity();
	}

	@Override
	protected void createToolkits() {
		groupToolkit = new GroupToolkit(toolkit, getManagedForm(),
				getPeopleService());
		// listToolkit = new ListToolkit(toolkit, getManagedForm(),
		// getPeopleServices(), getPeopleUiServices());
	}

	@Override
	protected boolean canSave() {
		String displayName = CommonsJcrUtils.get(group, Property.JCR_TITLE);
		if (displayName.length() < 2) {
			String msg = "Please note that you must define a group title"
					+ " that is at least 2 character long.";
			MessageDialog.openError(PeopleUiPlugin.getDefault().getWorkbench()
					.getDisplay().getActiveShell(), "Non-valid information",
					msg);

			return false;
		} else {
			PeopleJcrUtils.checkPathAndMoveIfNeeded(group,
					PeopleConstants.PEOPLE_BASE_PATH + "/"
							+ PeopleNames.PEOPLE_PERSONS);
			return true;
		}
	}

	@Override
	protected void populateTabFolder(CTabFolder folder) {
		// The member list
		String tooltip = "Members of group "
				+ JcrUtils.get(group, Property.JCR_TITLE);
		Composite innerPannel = addTabToFolder(folder, CTAB_COMP_STYLE,
				"Members", PeopleUiConstants.PANEL_MEMBERS, tooltip);
		groupToolkit.createMemberList(innerPannel, group);
	}

	@Override
	protected void populateTitleComposite(final Composite parent) {
		try {
			parent.setLayout(new FormLayout());

			// READ ONLY PANEL
			final Composite roPanelCmp = toolkit.createComposite(parent,
					SWT.NO_FOCUS);
			PeopleUiUtils.setSwitchingFormData(roPanelCmp);
			roPanelCmp.setLayout(new GridLayout());

			// Add a label with info provided by the FilmOverviewLabelProvider
			final Label titleROLbl = toolkit.createLabel(roPanelCmp, "",
					SWT.WRAP);
			titleROLbl.setData(PeopleUiConstants.MARKUP_ENABLED, Boolean.TRUE);
			final ColumnLabelProvider groupTitleLP = new GroupLabelProvider(
					PeopleUiConstants.LIST_TYPE_OVERVIEW_TITLE);

			// EDIT PANEL
			final Composite editPanel = toolkit.createComposite(parent,
					SWT.NO_FOCUS);
			PeopleUiUtils.setSwitchingFormData(editPanel);

			// intern layout
			editPanel.setLayout(new GridLayout(1, false));
			final Text titleTxt = PeopleUiUtils.createGDText(toolkit,
					editPanel, "A title", "The title of this group", 200, 1);
			final Text descTxt = PeopleUiUtils.createGDText(toolkit, editPanel,
					"A Description", "", 400, 1);

			AbstractFormPart editPart = new AbstractFormPart() {
				public void refresh() {
					super.refresh();
					// EDIT PART
					PeopleUiUtils.refreshTextWidgetValue(titleTxt, group,
							Property.JCR_TITLE);
					PeopleUiUtils.refreshTextWidgetValue(descTxt, group,
							Property.JCR_DESCRIPTION);

					// READ ONLY PART
					titleROLbl.setText(groupTitleLP.getText(group));
					// Manage switch
					if (CommonsJcrUtils.isNodeCheckedOutByMe(group))
						editPanel.moveAbove(roPanelCmp);
					else
						editPanel.moveBelow(roPanelCmp);
					editPanel.getParent().layout();
				}
			};

			// Listeners
			PeopleUiUtils.addTxtModifyListener(editPart, titleTxt, group,
					Property.JCR_TITLE, PropertyType.STRING);
			PeopleUiUtils.addTxtModifyListener(editPart, descTxt, group,
					Property.JCR_DESCRIPTION, PropertyType.STRING);

			// compulsory because we broke normal life cycle while implementing
			// IManageForm
			editPart.initialize(getManagedForm());
			getManagedForm().addPart(editPart);
		} catch (Exception e) {
			// } catch (RepositoryException e) {
			throw new PeopleException("Cannot create main info section", e);
		}
	}

}