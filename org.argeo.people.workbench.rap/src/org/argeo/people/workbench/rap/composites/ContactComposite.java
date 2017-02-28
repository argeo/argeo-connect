package org.argeo.people.workbench.rap.composites;

import javax.jcr.Node;
import javax.jcr.PropertyType;

import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.ui.ConnectUiUtils;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.workbench.AppWorkbenchService;
import org.argeo.connect.workbench.ConnectWorkbenchUtils;
import org.argeo.connect.workbench.parts.AbstractConnectEditor;
import org.argeo.people.PeopleNames;
import org.argeo.people.PeopleService;
import org.argeo.people.PeopleTypes;
import org.argeo.people.ui.PeopleUiSnippets;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Simple widget composite to display and edit contact information. Must be
 * disposed and re-created when the corresponding form part refreshes
 */
public class ContactComposite extends Composite {
	private static final long serialVersionUID = -789885142022513273L;

	private final ResourcesService resourceService;
	private final PeopleService peopleService;
	private final AppWorkbenchService appWorkbenchService;
	private final Node contactNode;
	private final Node parentVersionableNode;

	private final AbstractConnectEditor editor;
	private final FormToolkit toolkit;
	private final AbstractFormPart formPart;
	private final boolean isCheckedOut;

	public ContactComposite(Composite parent, int style, AbstractConnectEditor editor, AbstractFormPart formPart,
			Node contactNode, Node parentVersionableNode, ResourcesService resourceService,
			AppWorkbenchService appWorkbenchService, PeopleService peopleService) {
		super(parent, style);
		this.resourceService = resourceService;
		this.peopleService = peopleService;
		this.appWorkbenchService = appWorkbenchService;
		this.contactNode = contactNode;
		this.parentVersionableNode = parentVersionableNode;

		this.editor = editor;
		this.toolkit = editor.getFormToolkit();
		this.formPart = formPart;
		this.isCheckedOut = editor.isEditing();
		populate();
	}

	private void populate() {
		// Initialization
		Composite parent = this;
		parent.setLayout(ConnectUiUtils.noSpaceGridLayout(2));

		// buttons
		Composite buttCmp = new ContactButtonsComposite(editor, formPart, parent, SWT.NO_FOCUS, contactNode,
				parentVersionableNode, resourceService, peopleService, appWorkbenchService);
		toolkit.adapt(buttCmp, false, false);
		buttCmp.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		Composite dataCmp = toolkit.createComposite(parent, SWT.NO_FOCUS);
		dataCmp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		if (!isCheckedOut) // READ ONLY
			populateReadOnlyPanel(dataCmp);
		else
			populateEditPanel(dataCmp);
	}

	protected void populateReadOnlyPanel(final Composite readOnlyPanel) {
		readOnlyPanel.setLayout(new GridLayout());
		final Label readOnlyInfoLbl = toolkit.createLabel(readOnlyPanel, "", SWT.WRAP);
		readOnlyInfoLbl.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
		String addressHtml = PeopleUiSnippets.getContactDisplaySnippet(resourceService, contactNode);
		readOnlyInfoLbl.setText(addressHtml);
	}

	protected void populateEditPanel(final Composite parent) {
		RowLayout rl = new RowLayout(SWT.WRAP);
		rl.type = SWT.HORIZONTAL;
		rl.center = true;
		rl.marginWidth = 0;
		parent.setLayout(rl);

		boolean hasCat = !(ConnectJcrUtils.isNodeType(contactNode, PeopleTypes.PEOPLE_URL)
				|| ConnectJcrUtils.isNodeType(contactNode, PeopleTypes.PEOPLE_EMAIL));

		// The widgets
		final Text valueTxt = createAddressTxt(parent, "Value", 150);
		final Text labelTxt = createAddressTxt(parent, "", 120);
		final Combo catCmb = hasCat ? new Combo(parent, SWT.READ_ONLY) : null;

		if (catCmb != null) {
			catCmb.setItems(peopleService.getContactService().getContactPossibleValues(contactNode,
					PeopleNames.PEOPLE_CONTACT_CATEGORY));
			catCmb.select(0);
		}

		ConnectWorkbenchUtils.refreshFormText(editor, labelTxt, contactNode, PeopleNames.PEOPLE_CONTACT_LABEL, "Label");
		ConnectWorkbenchUtils.refreshFormText(editor, valueTxt, contactNode, PeopleNames.PEOPLE_CONTACT_VALUE, "Value");
		if (catCmb != null)
			ConnectWorkbenchUtils.refreshFormCombo(editor, catCmb, contactNode, PeopleNames.PEOPLE_CONTACT_CATEGORY);

		// Listeners
		ConnectWorkbenchUtils.addTxtModifyListener(formPart, valueTxt, contactNode, PeopleNames.PEOPLE_CONTACT_VALUE,
				PropertyType.STRING);
		ConnectWorkbenchUtils.addTxtModifyListener(formPart, labelTxt, contactNode, PeopleNames.PEOPLE_CONTACT_LABEL,
				PropertyType.STRING);
		if (catCmb != null)
			ConnectWorkbenchUtils.addComboSelectionListener(formPart, catCmb, contactNode, PeopleNames.PEOPLE_CONTACT_CATEGORY,
					PropertyType.STRING);
	}

	protected Text createAddressTxt(Composite parent, String msg, int width) {
		Text text = toolkit.createText(parent, null, SWT.BORDER);
		text.setMessage(msg);
		text.setLayoutData(width == 0 ? new RowData() : new RowData(width, SWT.DEFAULT));
		return text;
	}

	@Override
	public boolean setFocus() {
		return true;
	}
}
