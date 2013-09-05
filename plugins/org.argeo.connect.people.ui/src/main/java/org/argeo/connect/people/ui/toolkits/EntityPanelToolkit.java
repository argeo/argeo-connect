package org.argeo.connect.people.ui.toolkits;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.PeopleValueCatalogs;
import org.argeo.connect.people.ui.JcrUiUtils;
import org.argeo.connect.people.ui.editors.EntityAbstractFormPart;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.PeopleJcrUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;

/**
 * Centralize the creation of common panels for entity, to be used in various
 * forms.
 */
public class EntityPanelToolkit {
	// private final static Log log =
	// LogFactory.getLog(EntityPanelToolkit.class);

	private final FormToolkit toolkit;
	private final IManagedForm form;

	public EntityPanelToolkit(FormToolkit toolkit, IManagedForm form) {
		// formToolkit
		// managedForm
		this.toolkit = toolkit;
		this.form = form;
	}

	public void populateContactPanelWithNotes(Composite panel,
			final Node entity) {
		panel.setLayout(new GridLayout(2, false));
		GridData gd;
		final Composite contactListCmp = toolkit.createComposite(panel,
				SWT.NO_FOCUS);
		gd = new GridData(GridData.FILL_BOTH);
		gd.grabExcessVerticalSpace = true;
		gd.grabExcessHorizontalSpace = true;
		contactListCmp.setLayoutData(gd);

		final Composite rightCmp = toolkit.createComposite(panel, SWT.NO_FOCUS);
		gd = new GridData(GridData.FILL_BOTH);
		gd.grabExcessVerticalSpace = true;
		gd.grabExcessHorizontalSpace = true;
		rightCmp.setLayoutData(gd);

		populateNotePanel(rightCmp, entity);

		final Composite newContactCmp = toolkit.createComposite(panel,
				SWT.NO_FOCUS);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalSpan = 2;
		newContactCmp.setLayoutData(gd);

		final EntityAbstractFormPart sPart = new EntityAbstractFormPart() {
			public void refresh() {
				try {
					super.refresh();
					refreshContactPanel(contactListCmp, entity, this);
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

		populateAddContactPanel(newContactCmp, entity);

		// This must be moved in the called method.
		contactListCmp.setLayout(new GridLayout(2, false));
		refreshContactPanel(contactListCmp, entity, sPart);
		form.addPart(sPart);
		panel.layout();
	}

	public void populateContactPanel(final Composite panel, final Node entity) {
		panel.setLayout(new GridLayout());
		GridData gd;
		Hyperlink addNewMailLink = toolkit.createHyperlink(panel,
				"Add a new contact address", SWT.NO_FOCUS);

		final Composite contactListCmp = toolkit.createComposite(panel,
				SWT.NO_FOCUS);
		gd = new GridData(GridData.FILL_BOTH);
		gd.grabExcessVerticalSpace = true;
		gd.grabExcessHorizontalSpace = true;
		contactListCmp.setLayoutData(gd);

		final Composite newContactCmp = toolkit.createComposite(panel,
				SWT.NO_FOCUS);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace = true;
		newContactCmp.setLayoutData(gd);

		final EntityAbstractFormPart sPart = new EntityAbstractFormPart() {
			public void refresh() {
				try {
					super.refresh();
					refreshContactPanel(contactListCmp, entity, this);
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
					panel.layout();
				} catch (RepositoryException e) {
					throw new PeopleException(
							"Cannot refresh contact panel formPart", e);
				}
			}
		};

		populateAddContactPanel(newContactCmp, entity);

		// This must be moved in the called method.
		contactListCmp.setLayout(new GridLayout(2, false));
		refreshContactPanel(contactListCmp, entity, sPart);
		form.addPart(sPart);
		panel.layout();

		// addNewMailLink.addHyperlinkListener(new SimpleHyperlinkListener() {
		// @Override
		// public void linkActivated(HyperlinkEvent e) {
		// PeopleJcrUtils.createEmail(entity, "test@mail.de", 100, "test",
		// null);
		// refreshContactPanel(contactComposite, entity, toolkit, sPart);
		// sPart.markDirty();
		// }
		// });
	}

	public void populateNotePanel(final Composite rightPartComp,
			final Node entity) {
		rightPartComp.setLayout(new GridLayout());

		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.grabExcessVerticalSpace = true;
		rightPartComp.setLayoutData(gd);
		toolkit.createLabel(rightPartComp, "Notes: ", SWT.NONE);

		final Text notesTxt = toolkit.createText(rightPartComp, "", SWT.BORDER
				| SWT.MULTI | SWT.WRAP);
		gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL
				| GridData.GRAB_HORIZONTAL);
		gd.widthHint = 200;
		gd.minimumHeight = 200;
		gd.grabExcessVerticalSpace = true;
		notesTxt.setLayoutData(gd);

		final EntityAbstractFormPart notePart = new EntityAbstractFormPart() {
			public void refresh() {
				super.refresh();
				String desc = CommonsJcrUtils.getStringValue(entity,
						Property.JCR_DESCRIPTION);
				if (desc != null)
					notesTxt.setText(desc);
				notesTxt.setEnabled(CommonsJcrUtils
						.isNodeCheckedOutByMe(entity));
				rightPartComp.layout();
			}
		};

		notesTxt.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 7535211104983287096L;

			@Override
			public void modifyText(ModifyEvent event) {
				if (JcrUiUtils.setJcrProperty(entity, Property.JCR_DESCRIPTION,
						PropertyType.STRING, notesTxt.getText()))
					notePart.markDirty();
			}
		});
		form.addPart(notePart);
	}

	/** Manage display and update of existing contact Nodes */
	private void refreshContactPanel(Composite panel, final Node entity,
			final EntityAbstractFormPart part) {

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
							PeopleNames.PEOPLE_CONTACT_LABEL);
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
		panel.redraw();
		panel.layout();
		// panel.pack(true);
		// panel.getParent().pack(true);
		panel.getParent().layout();
	}

	/** Populate a composite that enable addition of a new contact */
	public void populateAddContactPanel(Composite parent, final Node entity) {
		parent.setLayout(new GridLayout(2, false));

		final Combo addContactCmb = new Combo(parent, SWT.NONE | SWT.READ_ONLY
				| SWT.NO_FOCUS);
		GridData gd = new GridData(SWT.TOP, SWT.CENTER, false, false);
		gd.widthHint = 100;
		addContactCmb.setLayoutData(gd);
		addContactCmb.setItems(PeopleValueCatalogs.ARRAY_CONTACT_TYPES);
		// Add a default value
		addContactCmb.add("Add a contact", 0);
		addContactCmb.select(0);

		final Composite editPanel = toolkit.createComposite(parent,
				SWT.NO_FOCUS);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace = true;
		editPanel.setLayoutData(gd);

		editPanel.setVisible(false);

		final EntityAbstractFormPart editPart = new EntityAbstractFormPart() {
			// Update values on refresh
			public void refresh() {
				super.refresh();
				editPanel.setVisible(false);
				addContactCmb.select(0);
			}
		};
		form.addPart(editPart);
		parent.layout();

		// show the edit new contact panel when selection change
		addContactCmb.addSelectionListener(new SelectionListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				String selected = addContactCmb.getItem(addContactCmb
						.getSelectionIndex());
				populateEditableContactComposite(editPanel, entity,
						PeopleValueCatalogs.getKeyByValue(
								PeopleValueCatalogs.MAPS_CONTACT_TYPES,
								selected));
				editPanel.setVisible(true);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

	}

	/** Populate an editable contact composite */
	public void populateEditableContactComposite(Composite parent,
			final Node entity, final String contactType) {

		if (parent.getLayout() == null) {
			RowLayout layout = new RowLayout();
			// Optionally set layout fields.
			layout.wrap = true;
			// Set the layout into the composite.
			parent.setLayout(layout);
		}

		// remove all controls
		for (Control ctl : parent.getChildren()) {
			ctl.dispose();
		}

		// Specific case of the post mail address
		if (PeopleTypes.PEOPLE_ADDRESS.equals(contactType)) {

		} else {
			final Text valueTxt = new Text(parent, SWT.BORDER);
			valueTxt.setMessage("Value");
			RowData rd = new RowData(200, SWT.DEFAULT);
			valueTxt.setLayoutData(rd);

			final Combo addCatCmb = new Combo(parent, SWT.NONE);
			addCatCmb.setItems(PeopleValueCatalogs.ARRAY_CONTACT_CATEGORIES);
			addCatCmb.select(0);

			final Combo addLabelCmb = new Combo(parent, SWT.NONE);
			if (PeopleTypes.PEOPLE_PHONE.equals(contactType))
				addLabelCmb.setItems(PeopleValueCatalogs.ARRAY_PHONE_TYPES);
			addLabelCmb.select(0);

			final Button primaryChk = toolkit.createButton(parent, "Primary",
					SWT.CHECK);

			final Button validBtn = toolkit.createButton(parent, "Save",
					SWT.PUSH);
			validBtn.addSelectionListener(new SelectionListener() {
				private static final long serialVersionUID = 1L;

				@Override
				public void widgetSelected(SelectionEvent e) {
					String value = valueTxt.getText();
					String cat = addCatCmb.getText();
					String label = addLabelCmb.getText();
					boolean isPrimary = primaryChk.getSelection();
					// EntityPanelToolkit
					boolean wasCheckedout = CommonsJcrUtils
							.isNodeCheckedOut(entity);
					if (!wasCheckedout)
						CommonsJcrUtils.checkout(entity);
					PeopleJcrUtils
							.createContact(entity, contactType, contactType,
									value, isPrimary ? 1 : 100, cat, label);
					if (!wasCheckedout)
						CommonsJcrUtils.saveAndCheckin(entity);
					else
						form.dirtyStateChanged();
					for (IFormPart part : form.getParts()) {
						((AbstractFormPart) part).markStale();
						part.refresh();
					}
					validBtn.getParent().setVisible(false);
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
			parent.pack();
			parent.redraw();
			parent.layout();

			parent.getParent().layout();
		}
	}
}