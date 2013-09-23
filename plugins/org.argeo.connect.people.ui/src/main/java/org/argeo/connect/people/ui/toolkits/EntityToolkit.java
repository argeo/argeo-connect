package org.argeo.connect.people.ui.toolkits;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.argeo.ArgeoException;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.PeopleValueCatalogs;
import org.argeo.connect.people.ui.JcrUiUtils;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.ui.editors.EntityAbstractFormPart;
import org.argeo.connect.people.ui.providers.BasicNodeListContentProvider;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.PeopleJcrUtils;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Centralize the creation of common controls (typically Text and composite
 * widget) for entity, to be used in various forms.
 */
public class EntityToolkit {
	// private final static Log log =
	// LogFactory.getLog(EntityPanelToolkit.class);

	private final FormToolkit toolkit;
	private final IManagedForm form;

	public EntityToolkit(FormToolkit toolkit, IManagedForm form) {
		this.toolkit = toolkit;
		this.form = form;
	}

	// ///////////////
	// TEXT widgets
	public void addTxtModifyListener(final AbstractFormPart part,
			final Text text, final Node entity, final String propName,
			final int propType) {
		text.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 3940522217518729442L;

			@Override
			public void modifyText(ModifyEvent event) {
				if (JcrUiUtils.setJcrProperty(entity, propName, propType,
						text.getText()))
					part.markDirty();
			}
		});
	}

	public void addNbOnlyTxtModifyListener(final AbstractFormPart part,
			final Text text, final Node entity, final String propName,
			final int propType) {
		final ControlDecoration decoration = new ControlDecoration(text,
				SWT.TOP | SWT.LEFT);
		decoration.setImage(PeopleUiPlugin.getDefault().getWorkbench()
				.getSharedImages().getImage(ISharedImages.IMG_DEC_FIELD_ERROR));
		decoration.hide();

		text.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 1L;

			public void modifyText(ModifyEvent event) {
				String lengthStr = text.getText();
				if (!isNumbers(lengthStr)) {
					text.setBackground(new Color(text.getDisplay(), 250, 200,
							150));
					decoration.show();
					decoration
							.setDescriptionText("The lenght must only be a number: "
									+ lengthStr);
				} else {
					text.setBackground(null);
					decoration.hide();
					Long length = null;
					if (CommonsJcrUtils.checkNotEmptyString(lengthStr))
						length = new Long(lengthStr);
					if (JcrUiUtils.setJcrProperty(entity, propName, propType,
							length))
						part.markDirty();
				}
			}
		});
	}

	private static boolean isNumbers(String content) {
		int length = content.length();
		for (int i = 0; i < length; i++) {
			char ch = content.charAt(i);
			if (!Character.isDigit(ch)) {
				return false;
			}
		}
		return true;
	}

	public void refreshTextValue(Text text, Node entity, String propName) {
		String tmpStr = CommonsJcrUtils.getStringValue(entity, propName);
		if (CommonsJcrUtils.checkNotEmptyString(tmpStr))
			text.setText(tmpStr);
	}

	public Text createText(Composite parent, String msg, String toolTip,
			int width) {
		Text text = toolkit.createText(parent, "", SWT.BORDER | SWT.SINGLE
				| SWT.LEFT);
		text.setMessage(msg);
		text.setToolTipText(toolTip);
		text.setLayoutData(new RowData(width, SWT.DEFAULT));
		return text;
	}

	public Text createTextforGrid(Composite parent, String msg, String toolTip,
			int widthHint, int style) {
		Text text = toolkit.createText(parent, "", SWT.BORDER | SWT.SINGLE
				| SWT.LEFT);
		text.setMessage(msg);
		text.setToolTipText(toolTip);
		GridData gd = new GridData(style);
		gd.widthHint = widthHint;
		text.setLayoutData(gd);
		return text;
	}

	// ////////////////
	// Various panels

	public void populateTagsROPanel(final Composite parent, final Node entity) {
		parent.setLayout(new FormLayout());
		// Show only TAGS for the time being, so it is the same for R/O & Edit
		// mode
		final Composite panel = toolkit.createComposite(parent, SWT.NO_FOCUS);
		PeopleUiUtils.setSwitchingFormData(panel);

		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		panel.setLayout(tableColumnLayout);

		int style = SWT.NO_SCROLL;
		Table table = new Table(panel, style);
		table.setLinesVisible(false);
		table.setHeaderVisible(false);
		// Enable markups
		table.setData(RWT.CUSTOM_VARIANT,
				PeopleUiConstants.CSS_STYLE_UNIQUE_CELL_TABLE);
		table.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
		table.setData(RWT.CUSTOM_ITEM_HEIGHT, Integer.valueOf(40));

		// Does not work: adding a tag within the <a> tag unvalid the
		// target="_RWT" parameter
		// ResourceManager resourceManager = RWT.getResourceManager();
		// if (!resourceManager.isRegistered("icons/close.png")) {
		// InputStream inputStream = this.getClass().getClassLoader()
		// .getResourceAsStream("icons/close.png");
		// try {
		// resourceManager.register("icons/close.png", inputStream);
		// } finally {
		// IOUtils.closeQuietly(inputStream);
		// }
		// }
		// final String src = RWT.getResourceManager().getLocation(
		// "icons/close.png");

		final TableViewer viewer = new TableViewer(table);
		viewer.setLabelProvider(new LabelProvider() {
			private static final long serialVersionUID = 1L;

			@Override
			public String getText(Object element) {
				try {
					Node node = (Node) element;
					StringBuilder tags = new StringBuilder();
					if (node.hasProperty(PeopleNames.PEOPLE_TAGS)) {
						tags.append("<span style=\"font-size:15px;float:left;padding:0px;white-space:pre-wrap;text-decoration:none;\">");
						Value[] values = entity.getProperty(
								PeopleNames.PEOPLE_TAGS).getValues();
						for (int i = 0; i < values.length; i++) {
							String currStr = values[i].getString();
							tags.append("<i>#");
							tags.append(currStr).append("&#160;");
							tags.append("<small><a style=\"text-decoration:none;\" href=\"");
							tags.append(currStr);
							tags.append("\" target=\"_rwt\">X</a></small></i>")
									.append("&#160;&#160; ");
						}
						tags.append("</span>");
					}
					return tags.toString();
				} catch (RepositoryException re) {
					throw new PeopleException("unable to get tags", re);
				}
			}

		});
		viewer.setContentProvider(new BasicNodeListContentProvider());

		TableColumn singleColumn = new TableColumn(table, SWT.LEFT);
		singleColumn.setData(RWT.CUSTOM_VARIANT,
				PeopleUiConstants.CSS_STYLE_UNIQUE_CELL_TABLE);

		tableColumnLayout.setColumnData(singleColumn, new ColumnWeightData(90));

		final EntityAbstractFormPart editPart = new EntityAbstractFormPart() {
			public void refresh() { // update display value
				super.refresh();
				List<Node> nodes = new ArrayList<Node>();
				nodes.add(entity);
				viewer.refresh();
			}
		};

		table.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			public void widgetSelected(SelectionEvent event) {
				if (event.detail == RWT.HYPERLINK) {
					try {

						String tagToRemove = event.text;
						if (CommonsJcrUtils.checkNotEmptyString(tagToRemove)) {
							List<String> tags = new ArrayList<String>();
							if (entity.hasProperty(PeopleNames.PEOPLE_TAGS)) {
								Value[] values = entity.getProperty(
										PeopleNames.PEOPLE_TAGS).getValues();
								for (int i = 0; i < values.length; i++) {
									String curr = values[i].getString();
									if (!tagToRemove.equals(curr))
										tags.add(curr);
								}
							}
							boolean wasCheckedout = CommonsJcrUtils
									.isNodeCheckedOut(entity);
							if (!wasCheckedout)
								CommonsJcrUtils.checkout(entity);
							entity.setProperty(PeopleNames.PEOPLE_TAGS,
									tags.toArray(new String[tags.size()]));
							if (!wasCheckedout)
								CommonsJcrUtils.saveAndCheckin(entity);
							else
								form.dirtyStateChanged();
						}
						editPart.refresh();
					} catch (RepositoryException re) {
						throw new ArgeoException("Unable to set tags", re);
					}
				}
			}
		});
		List<Node> nodes = new ArrayList<Node>();
		nodes.add(entity);
		viewer.setInput(nodes);

		editPart.initialize(form);
		form.addPart(editPart);
	}

	public void populateAddTagComposite(Composite parent, final Node entity) {
		parent.setLayout(new RowLayout());
		final Text tagTxt = new Text(parent, SWT.BORDER);
		tagTxt.setMessage("Enter a new tag");
		RowData rd = new RowData(200, SWT.DEFAULT);
		tagTxt.setLayoutData(rd);

		final Button validBtn = toolkit.createButton(parent, "Add", SWT.PUSH);
		rd = new RowData(80, SWT.DEFAULT);
		validBtn.setLayoutData(rd);

		validBtn.addSelectionListener(new SelectionListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				String newTag = tagTxt.getText();
				addTag(entity, newTag);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		tagTxt.addTraverseListener(new TraverseListener() {
			private static final long serialVersionUID = 1L;

			public void keyTraversed(TraverseEvent e) {
				if (e.keyCode == SWT.CR) {
					String newTag = tagTxt.getText();
					addTag(entity, newTag);
					e.doit = false;
				}
			}
		});
	}

	private void addTag(Node tagable, String newTag) {
		boolean wasCheckedout = CommonsJcrUtils.isNodeCheckedOut(tagable);
		if (!wasCheckedout)
			CommonsJcrUtils.checkout(tagable);
		try {
			Value[] values;
			String[] valuesStr;
			if (tagable.hasProperty(PeopleNames.PEOPLE_TAGS)) {
				values = tagable.getProperty(PeopleNames.PEOPLE_TAGS)
						.getValues();
				valuesStr = new String[values.length + 1];
				int i;
				for (i = 0; i < values.length; i++) {
					valuesStr[i] = values[i].getString();
				}
				valuesStr[i] = newTag;
			} else {
				valuesStr = new String[1];
				valuesStr[0] = newTag;
			}
			tagable.setProperty(PeopleNames.PEOPLE_TAGS, valuesStr);
		} catch (RepositoryException re) {
			throw new ArgeoException("Unable to set tags", re);
		}
		if (!wasCheckedout)
			CommonsJcrUtils.saveAndCheckin(tagable);
		else
			form.dirtyStateChanged();

		for (IFormPart part : form.getParts()) {
			((AbstractFormPart) part).markStale();
			part.refresh();
		}
	}

	public void populateContactPanelWithNotes(Composite panel, final Node entity) {
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
		panel.layout();

		sPart.initialize(form);
		form.addPart(sPart);
		}

	public void populateContactPanel(final Composite panel, final Node entity) {
		panel.setLayout(new GridLayout());
		GridData gd;
		// Hyperlink addNewMailLink = toolkit.createHyperlink(panel,
		// "Add a new contact address", SWT.NO_FOCUS);

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
		panel.layout();

		// This must be moved in the called method.
		contactListCmp.setLayout(new GridLayout(2, false));
		refreshContactPanel(contactListCmp, entity, sPart);
		sPart.initialize(form);
		form.addPart(sPart);
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
		notePart.initialize(form);
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
		
		editPart.initialize(form);
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