package org.argeo.people.workbench.rap.parts;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.cms.ui.eclipse.forms.AbstractFormPart;
import org.argeo.connect.ui.ConnectUiConstants;
import org.argeo.connect.ui.ConnectUiUtils;
import org.argeo.connect.ui.ConnectWorkbenchUtils;
import org.argeo.connect.workbench.parts.AbstractConnectCTabEditor;
import org.argeo.people.PeopleException;
import org.argeo.people.ui.providers.GroupLabelProvider;
import org.argeo.people.workbench.rap.PeopleRapPlugin;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
//import org.eclipse.ui.forms.AbstractFormPart;

/**
 * Editor page that display a group with corresponding details
 */
public abstract class GroupEditor extends AbstractConnectCTabEditor {
	final static Log log = LogFactory.getLog(GroupEditor.class);

	public final static String ID = PeopleRapPlugin.PLUGIN_ID + ".groupEditor";

	// Main business Objects
	private Node group;

	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		group = getNode();
	}

	@Override
	protected void populateHeader(final Composite parent) {
		try {
			parent.setLayout(new FormLayout());

			// READ ONLY PANEL
			final Composite roPanelCmp = getFormToolkit().createComposite(
					parent, SWT.NO_FOCUS);
			ConnectUiUtils.setSwitchingFormData(roPanelCmp);
			roPanelCmp.setLayout(new GridLayout());

			// Add a label with info provided by the FilmOverviewLabelProvider
			final Label titleROLbl = getFormToolkit().createLabel(roPanelCmp,
					"", SWT.WRAP);
			titleROLbl.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
			final ColumnLabelProvider groupTitleLP = new GroupLabelProvider(
					ConnectUiConstants.LIST_TYPE_OVERVIEW_TITLE);

			// EDIT PANEL
			final Composite editPanel = getFormToolkit().createComposite(
					parent, SWT.NO_FOCUS);
			ConnectUiUtils.setSwitchingFormData(editPanel);

			// intern layout
			editPanel.setLayout(new GridLayout(1, false));
			final Text titleTxt = ConnectUiUtils.createGDText(getFormToolkit(),
					editPanel, "A title", "The title of this group", 200, 1);
			final Text descTxt = ConnectUiUtils.createGDText(getFormToolkit(),
					editPanel, "A Description", "", 400, 1);

			AbstractFormPart editPart = new AbstractFormPart() {
				public void refresh() {
					super.refresh();
					// EDIT PART
					ConnectUiUtils.refreshTextWidgetValue(titleTxt, group,
							Property.JCR_TITLE);
					ConnectUiUtils.refreshTextWidgetValue(descTxt, group,
							Property.JCR_DESCRIPTION);

					// READ ONLY PART
					titleROLbl.setText(groupTitleLP.getText(group));
					// Manage switch
					if (isEditing())
						editPanel.moveAbove(roPanelCmp);
					else
						editPanel.moveBelow(roPanelCmp);
					editPanel.getParent().layout();
				}
			};

			// Listeners
			ConnectWorkbenchUtils.addTxtModifyListener(editPart, titleTxt, group,
					Property.JCR_TITLE, PropertyType.STRING);
			ConnectWorkbenchUtils.addTxtModifyListener(editPart, descTxt, group,
					Property.JCR_DESCRIPTION, PropertyType.STRING);

			// compulsory because we broke normal life cycle while implementing
			// IManageForm
			editPart.initialize(getManagedForm());
			getManagedForm().addPart(editPart);
		} catch (Exception e) {
			throw new PeopleException("Cannot create main info section", e);
		}
	}
}