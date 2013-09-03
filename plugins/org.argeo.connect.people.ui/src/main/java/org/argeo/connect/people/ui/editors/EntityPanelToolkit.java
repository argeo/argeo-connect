package org.argeo.connect.people.ui.editors;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.ui.listeners.SimpleHyperlinkListener;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.PeopleJcrUtils;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;

/**
 * Centralize the creation of common panels for entity, to be used in various
 * forms.
 */
public class EntityPanelToolkit {
	// private final static Log log =
	// LogFactory.getLog(EntityPanelToolkit.class);

	public static void populateContactPanel(Composite panel, final Node entity,
			final FormToolkit toolkit, final IManagedForm form) {
		panel.setLayout(new GridLayout());

		// TODO manage adding a new contact
		Hyperlink addNewMailLink = toolkit.createHyperlink(panel,
				"Add a new contact address", SWT.NO_FOCUS);

		final EntityAbstractFormPart sPart = new EntityAbstractFormPart() {
			public void refresh() {
				try {
					super.refresh();
					for (String path : controls.keySet()) {
						Text txt = controls.get(path);
						Node currNode = entity.getSession().getNode(path);
						String propName = (String) txt.getData("propName");
						String value = CommonsJcrUtils.getStringValue(currNode,
								propName);
						if (value != null)
							txt.setText(value);
						txt.setEnabled(entity.isCheckedOut());
					}
				} catch (RepositoryException e) {
					throw new PeopleException(
							"Cannot refresh contact panel formPart", e);
				}
			}
		};
		final Composite contactComposite = toolkit.createComposite(panel,
				SWT.NO_FOCUS);
		contactComposite.setLayout(new GridLayout(2, false));
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.grabExcessVerticalSpace = true;

		refreshContactPanel(contactComposite, entity, toolkit, sPart);
		form.addPart(sPart);

		addNewMailLink.addHyperlinkListener(new SimpleHyperlinkListener() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				PeopleJcrUtils.createEmail(entity, "test@mail.de", 100, "test",
						null);
				refreshContactPanel(contactComposite, entity, toolkit, sPart);
				sPart.markDirty();
			}
		});
	}

	/** Manage display and update of existing contact Nodes */
	private static void refreshContactPanel(Composite panel, final Node entity,
			FormToolkit toolkit, final EntityAbstractFormPart part) {

		// Clean old controls
		for (Control ctl : panel.getChildren()) {
			ctl.dispose();
		}

		// FIXME work in progress
		final Map<String, Text> controls = new HashMap<String, Text>();

		NodeIterator ni;
		try {
			ni = entity.getNode(PeopleNames.PEOPLE_CONTACTS).getNodes();
			while (ni.hasNext()) {
				Node currNode = ni.nextNode();
				if (!currNode.isNodeType(PeopleTypes.PEOPLE_ADDRESS)) {
					String type = CommonsJcrUtils.getStringValue(currNode,
							PeopleNames.PEOPLE_CONTACT_TYPE);
					if (type == null)
						type = CommonsJcrUtils.getStringValue(currNode,
								PeopleNames.PEOPLE_CONTACT_CATEGORY);
					if (type == null)
						type = PeopleJcrUtils.getContactTypeAsString(currNode);
					toolkit.createLabel(panel, type, SWT.NO_FOCUS);
					Text currCtl = toolkit.createText(panel, null, SWT.BORDER);
					GridData gd = new GridData();
					gd.widthHint = 200;
					gd.heightHint = 14;
					currCtl.setLayoutData(gd);
					currCtl.setData("propName",
							PeopleNames.PEOPLE_CONTACT_VALUE);
					controls.put(currNode.getPath(), currCtl);
				}
			}

			for (final String name : controls.keySet()) {
				final Text txt = controls.get(name);
				txt.addModifyListener(new ModifyListener() {
					private static final long serialVersionUID = 1L;

					@Override
					public void modifyText(ModifyEvent event) {
						String propName = (String) txt.getData("propName");
						try {
							Node currNode = entity.getSession().getNode(name);
							if (currNode.hasProperty(propName)
									&& currNode.getProperty(propName)
											.getString().equals(txt.getText())) {
								// nothing changed yet
							} else {
								currNode.setProperty(propName, txt.getText());
								part.markDirty();
							}
						} catch (RepositoryException e) {
							throw new PeopleException(
									"Unexpected error in modify listener for property "
											+ propName, e);
						}
					}
				});
			}

		} catch (RepositoryException e) {
			throw new PeopleException("Error while getting properties", e);
		}
		part.setTextControls(controls);
		part.refresh();
		panel.layout();
		panel.pack(true);
		panel.getParent().pack(true);
		panel.getParent().layout();
	}

	/** Populate a composite that enable addition of a new contact */
	public static void populateNewContactPanel(Composite parent,
			final Node entity, final FormToolkit toolkit,
			final IManagedForm form) {
		parent.setLayout(new GridLayout(2, false));

		final Combo addContactCmb = new Combo(parent, SWT.NONE);
		addContactCmb.setText("Add contact");

		final Composite panel = toolkit.createComposite(parent, SWT.NO_FOCUS);
		// EMPTY READ ONLY PANEL
		final Composite roPanelCmp = toolkit.createComposite(panel,
				SWT.NO_FOCUS);
		PeopleUiUtils.setSwitchingFormData(roPanelCmp);
		roPanelCmp.setData(RWT.CUSTOM_VARIANT,
				PeopleUiConstants.PEOPLE_CSS_GENERALINFO_COMPOSITE);
		roPanelCmp.setLayout(new GridLayout());

		// DUMMY LABEL
		Label dummyLbl = toolkit.createLabel(roPanelCmp, "", SWT.WRAP);
		dummyLbl.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
		dummyLbl.setText("Remove me");

		// EDIT PANEL
		final Composite editPanelCmp = toolkit.createComposite(panel,
				SWT.NO_FOCUS);
		PeopleUiUtils.setSwitchingFormData(editPanelCmp);
		editPanelCmp.setData(RWT.CUSTOM_VARIANT,
				PeopleUiConstants.PEOPLE_CSS_GENERALINFO_COMPOSITE);
		editPanelCmp.setLayout(new GridLayout());

		// DUMMY LABEL
		dummyLbl = toolkit.createLabel(roPanelCmp, "edit remove", SWT.WRAP);
		dummyLbl.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);

		final EntityAbstractFormPart editPart = new EntityAbstractFormPart() {
			// Update values on refresh
			public void refresh() {
				super.refresh();
			}
		};
		form.addPart(editPart);
	}

	/** Populate an editable contact composite */
	public static void populateEditableContactComposite(Composite parent,
			final Node entity, final FormToolkit toolkit,
			final IManagedForm form) {
		RowLayout layout = new RowLayout();
		// Optionally set layout fields.
		layout.wrap = true;
		// Set the layout into the composite.
		parent.setLayout(layout);

		final Combo addTypeCmb = new Combo(parent, SWT.NONE);
		final Combo addCatCmb = new Combo(parent, SWT.NONE);
		final Combo addLabelCmb = new Combo(parent, SWT.NONE);
		final Button primaryChk = toolkit.createButton(parent, "Primary",
				SWT.CHECK);

		final Composite panel = toolkit.createComposite(parent, SWT.NO_FOCUS);
		// EMPTY READ ONLY PANEL
		final Composite roPanelCmp = toolkit.createComposite(panel,
				SWT.NO_FOCUS);
		PeopleUiUtils.setSwitchingFormData(roPanelCmp);
		roPanelCmp.setData(RWT.CUSTOM_VARIANT,
				PeopleUiConstants.PEOPLE_CSS_GENERALINFO_COMPOSITE);
		roPanelCmp.setLayout(new GridLayout());

		// DUMMY LABEL
		Label dummyLbl = toolkit.createLabel(roPanelCmp, "", SWT.WRAP);
		dummyLbl.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
		dummyLbl.setText("Remove me");

		// EDIT PANEL
		final Composite editPanelCmp = toolkit.createComposite(panel,
				SWT.NO_FOCUS);
		PeopleUiUtils.setSwitchingFormData(editPanelCmp);
		editPanelCmp.setData(RWT.CUSTOM_VARIANT,
				PeopleUiConstants.PEOPLE_CSS_GENERALINFO_COMPOSITE);
		editPanelCmp.setLayout(new GridLayout());

		// DUMMY LABEL
		dummyLbl = toolkit.createLabel(roPanelCmp, "edit remove", SWT.WRAP);
		dummyLbl.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);

		final EntityAbstractFormPart editPart = new EntityAbstractFormPart() {
			// Update values on refresh
			public void refresh() {
				super.refresh();
			}
		};
		form.addPart(editPart);
	}

}