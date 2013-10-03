package org.argeo.connect.people.ui.toolkits;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.InvalidItemStateException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.DynamicOperand;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelConstants;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;
import javax.jcr.query.qom.StaticOperand;

import org.argeo.ArgeoException;
import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.PeopleValueCatalogs;
import org.argeo.connect.people.ui.JcrUiUtils;
import org.argeo.connect.people.ui.PeopleHtmlUtils;
import org.argeo.connect.people.ui.PeopleImages;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.ui.commands.AddEntityReferenceWithPosition;
import org.argeo.connect.people.ui.commands.OpenEntityEditor;
import org.argeo.connect.people.ui.editors.EntityAbstractFormPart;
import org.argeo.connect.people.ui.providers.BasicNodeListContentProvider;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.PeopleJcrUtils;
import org.argeo.eclipse.ui.utils.CommandUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Centralizes creation of common controls (typically Text and composite widget)
 * for entity, to be used in various forms.
 */
public class EntityToolkit {
	// private final static Log log =
	// LogFactory.getLog(EntityPanelToolkit.class);

	private final FormToolkit toolkit;
	private final IManagedForm form;

	// private AbstractEntityEditor editor;

	public EntityToolkit(FormToolkit toolkit, IManagedForm form) {
		this.toolkit = toolkit;
		this.form = form;
	}

	// public EntityToolkit(FormToolkit toolkit, IManagedForm form,
	// AbstractEntityEditor editor) {
	// this.toolkit = toolkit;
	// this.form = form;
	// this.editor = editor;
	// }

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
							.setDescriptionText("Length can only be a number: "
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

	private List<Node> getNodeReferencing(Node entity) {
		try {
			Session session = entity.getSession();
			QueryManager queryManager = session.getWorkspace()
					.getQueryManager();
			QueryObjectModelFactory qomFactory = queryManager.getQOMFactory();

			Selector source = qomFactory.selector(
					PeopleTypes.PEOPLE_MAILING_LIST_ITEM,
					PeopleTypes.PEOPLE_MAILING_LIST_ITEM);

			// Parse the String
			StaticOperand so = qomFactory.literal(entity.getProperty(
					PeopleNames.PEOPLE_UID).getValue());
			DynamicOperand dop = qomFactory.propertyValue(
					source.getSelectorName(), PeopleNames.PEOPLE_REF_UID);
			Constraint defaultC = qomFactory.comparison(dop,
					QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO, so);

			QueryObjectModel query;
			query = qomFactory.createQuery(source, defaultC, null, null);
			QueryResult result = query.execute();
			NodeIterator ni = result.getNodes();

			return JcrUtils.nodeIteratorToList(ni);
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to list entities", e);
		}
	}

	public void populateGroupMembershipPanel(final Composite parent,
			final Node entity) {
		parent.setLayout(PeopleUiUtils.gridLayoutNoBorder(2));

		final Button addBtn = new Button(parent, SWT.FLAT);
		addBtn.setData(RWT.CUSTOM_VARIANT,
				PeopleUiConstants.CSS_FLAT_IMG_BUTTON);
		addBtn.setImage(PeopleImages.ADD_BTN);
		addBtn.setToolTipText("Register "
				+ CommonsJcrUtils.get(entity, Property.JCR_TITLE)
				+ " in various mailing lists");

		GridData gd = new GridData(SWT.CENTER, SWT.CENTER, false, false);
		gd.heightHint = 16;
		gd.widthHint = 28;
		addBtn.setLayoutData(gd);

		final Composite nlCmp = new Composite(parent, SWT.NO_FOCUS);
		RowLayout rl = new RowLayout(SWT.HORIZONTAL);
		rl.wrap = true;
		rl.marginLeft = 0;
		rl.marginRight = 0;
		nlCmp.setLayout(rl);

		final EntityAbstractFormPart editPart = new EntityAbstractFormPart() {
			public void refresh() {
				super.refresh();
				// We redraw the full control at each refresh, might be a more
				// efficient way to do
				Control[] oldChildren = nlCmp.getChildren();
				for (Control child : oldChildren)
					child.dispose();

				try {
					List<Node> referencings = getNodeReferencing(entity);

					for (final Node node : referencings) {
						final Node parNode = node.getParent().getParent();
						Link link = new Link(nlCmp, SWT.NONE);
						link.setText("<a>"
								+ PeopleHtmlUtils
										.cleanHtmlString(CommonsJcrUtils.get(
												parNode, Property.JCR_TITLE))
								+ "</a>");
						link.setToolTipText(CommonsJcrUtils.get(parNode,
								Property.JCR_DESCRIPTION));

						link.addSelectionListener(new SelectionAdapter() {
							private static final long serialVersionUID = 1L;

							@Override
							public void widgetSelected(
									final SelectionEvent event) {
								Map<String, String> params = new HashMap<String, String>();
								params.put(OpenEntityEditor.PARAM_ENTITY_TYPE,
										PeopleTypes.PEOPLE_MAILING_LIST);
								params.put(OpenEntityEditor.PARAM_ENTITY_UID,
										CommonsJcrUtils.get(parNode,
												PeopleNames.PEOPLE_UID));
								CommandUtils.callCommand(OpenEntityEditor.ID,
										params);
							}
						});

						final Button deleteBtn = new Button(nlCmp, SWT.FLAT);
						deleteBtn.setData(RWT.CUSTOM_VARIANT,
								PeopleUiConstants.CSS_FLAT_IMG_BUTTON);
						deleteBtn.setImage(PeopleImages.DELETE_BTN_LEFT);
						RowData rd = new RowData();
						rd.height = 16;
						rd.width = 18;
						deleteBtn.setLayoutData(rd);

						deleteBtn.addSelectionListener(new SelectionAdapter() {
							private static final long serialVersionUID = 1L;

							@Override
							public void widgetSelected(
									final SelectionEvent event) {
								try {
									boolean wasCheckedOut = CommonsJcrUtils
											.isNodeCheckedOutByMe(parNode);
									if (!wasCheckedOut)
										CommonsJcrUtils.checkout(parNode);
									node.remove();
									if (wasCheckedOut)
										parNode.getSession().save();
									else
										CommonsJcrUtils.saveAndCheckin(parNode);
								} catch (RepositoryException e) {
									throw new PeopleException(
											"unable to initialise deletion", e);
								}
								for (IFormPart part : form.getParts()) {
									((AbstractFormPart) part).markStale();
									part.refresh();
								}
							}
						});

					}
					nlCmp.pack();
					nlCmp.redraw();
					nlCmp.layout();
					parent.getParent().layout();
				} catch (RepositoryException re) {
					throw new PeopleException(
							"Error while refreshing mailing list appartenance",
							re);
				}
			}
		};

		addBtn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				Map<String, String> params = new HashMap<String, String>();
				try {
					params.put(
							AddEntityReferenceWithPosition.PARAM_REFERENCED_JCR_ID,
							entity.getIdentifier());
					params.put(
							AddEntityReferenceWithPosition.PARAM_TO_SEARCH_NODE_TYPE,
							PeopleTypes.PEOPLE_MAILING_LIST);
					params.put(AddEntityReferenceWithPosition.PARAM_DIALOG_ID,
							PeopleUiConstants.DIALOG_ADD_ML_MEMBERSHIP);
					CommandUtils.callCommand(AddEntityReferenceWithPosition.ID,
							params);
				} catch (RepositoryException e1) {
					throw new PeopleException(
							"Unable to get parent Jcr identifier", e1);
				}
			}
		});

		editPart.initialize(form);
		form.addPart(editPart);
	}

	public void populateTagsPanel(final Composite parent, final Node entity) {
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
							String currStr = PeopleHtmlUtils
									.cleanHtmlString(values[i].getString());

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
						// Not enough we want to refresh the all form.
						// editPart.refresh();
						for (IFormPart part : form.getParts()) {
							((AbstractFormPart) part).markStale();
							part.refresh();
						}
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
				tagTxt.setText("");
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
					tagTxt.setText("");
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

	public void createContactPanelWithNotes(Composite panel, final Node entity) {
		panel.setLayout(new GridLayout(2, false));

		final ScrolledComposite contactListCmp = new ScrolledComposite(panel,
				SWT.NO_FOCUS | SWT.V_SCROLL | SWT.H_SCROLL);
		contactListCmp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true));
		contactListCmp.setLayout(new FillLayout());
		final Composite innerCmp = new Composite(contactListCmp, SWT.NO_FOCUS);
		// innerCmp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
		// true));
		populateDisplayContactPanel(innerCmp, entity);
		contactListCmp.setContent(innerCmp);

		final Composite rightCmp = toolkit.createComposite(panel, SWT.NO_FOCUS);
		rightCmp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		populateNotePanel(rightCmp, entity);

		final Composite newContactCmp = toolkit.createComposite(panel,
				SWT.NO_FOCUS);
		GridData gd = new GridData(SWT.FILL, SWT.BOTTOM, true, false);
		gd.horizontalSpan = 2;
		newContactCmp.setLayoutData(gd);
		populateAddContactPanel(newContactCmp, entity);
	}

	/**
	 * @param panel
	 * @param entity
	 */
	public void createContactPanel(final Composite panel, final Node entity) {
		panel.setLayout(new GridLayout());

		final ScrolledComposite contactListCmp = new ScrolledComposite(panel,
				SWT.NO_FOCUS | SWT.V_SCROLL | SWT.H_SCROLL);
		contactListCmp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true));
		populateDisplayContactPanel(contactListCmp, entity);

		final Composite newContactCmp = toolkit.createComposite(panel,
				SWT.NO_FOCUS);
		newContactCmp.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true,
				false));
		populateAddContactPanel(newContactCmp, entity);
	}

	/** Manage display and update of existing contact Nodes */
	private void populateDisplayContactPanel(final Composite panel,
			final Node entity) {
		panel.setLayout(PeopleUiUtils.gridLayoutNoBorder());

		final Map<String, Composite> contactCmps = new HashMap<String, Composite>();

		final AbstractFormPart sPart = new AbstractFormPart() {
			public void refresh() {
				try {
					super.refresh();
					// first: initialise composite for new contacts
					Node contactsPar = entity
							.getNode(PeopleNames.PEOPLE_CONTACTS);
					NodeIterator ni = contactsPar.getNodes();
					while (ni.hasNext()) {
						Node currNode = ni.nextNode();
						String currJcrId = currNode.getIdentifier();
						if (!contactCmps.containsKey(currJcrId)) {
							Composite currCmp = new Composite(panel,
									SWT.NO_FOCUS);
							contactCmps.put(currJcrId, currCmp);
						}
					}

					// then refresh or create all composites
					Session session = contactsPar.getSession();
					for (String jcrId : contactCmps.keySet()) {
						Composite currCmp = contactCmps.get(jcrId);
						Node currNode = null;
						try {
							currNode = session.getNodeByIdentifier(jcrId);
						} catch (ItemNotFoundException infe) {
							currCmp.dispose();
						}
						if (currNode != null) {
							if (currCmp.getChildren().length == 0)
								populateDisplayCmp(currCmp, currNode);
							else {
								refreshDisplayCmp(currCmp, currNode);
							}
						}
					}
				} catch (RepositoryException e) {
					throw new PeopleException(
							"Cannot refresh contact panel formPart", e);
				}

				panel.layout();
				panel.redraw();
				panel.pack();
				panel.getParent().layout();
				panel.getParent().redraw();
				panel.getParent().pack();
			}
		};

		sPart.initialize(form);
		form.addPart(sPart);

		// // Clean old controls
		// for (Control ctl : panel.getChildren()) {
		// ctl.dispose();
		// }
		//
		// // FIXME work in progress
		// final Map<String, Text> controls = new HashMap<String, Text>();
		//
		// NodeIterator ni;
		// try {
		// ni = entity.getNode(PeopleNames.PEOPLE_CONTACTS).getNodes();
		// while (ni.hasNext()) {
		// Node currNode = ni.nextNode();
		// if (!currNode.isNodeType(PeopleTypes.PEOPLE_ADDRESS)) {
		// String type = CommonsJcrUtils.getStringValue(currNode,
		// PeopleNames.PEOPLE_CONTACT_LABEL);
		// if (type == null)
		// type = CommonsJcrUtils.getStringValue(currNode,
		// PeopleNames.PEOPLE_CONTACT_CATEGORY);
		// if (type == null)
		// type = PeopleJcrUtils.getContactTypeAsString(currNode);
		// toolkit.createLabel(panel, type, SWT.NO_FOCUS);
		// Text currCtl = toolkit.createText(panel, null, SWT.BORDER);
		// GridData gd = new GridData();
		// gd.widthHint = 200;
		// gd.heightHint = 14;
		// currCtl.setLayoutData(gd);
		// currCtl.setData("propName",
		// PeopleNames.PEOPLE_CONTACT_VALUE);
		// controls.put(currNode.getPath(), currCtl);
		// }
		// }
		//
		// for (final String name : controls.keySet()) {
		// final Text txt = controls.get(name);
		// txt.addModifyListener(new ModifyListener() {
		// private static final long serialVersionUID = 1L;
		//
		// @Override
		// public void modifyText(ModifyEvent event) {
		// String propName = (String) txt.getData("propName");
		// try {
		// Node currNode = entity.getSession().getNode(name);
		// if (currNode.hasProperty(propName)
		// && currNode.getProperty(propName)
		// .getString().equals(txt.getText())) {
		// // nothing changed yet
		// } else {
		// currNode.setProperty(propName, txt.getText());
		// part.markDirty();
		// }
		// } catch (RepositoryException e) {
		// throw new PeopleException(
		// "Unexpected error in modify listener for property "
		// + propName, e);
		// }
		// }
		// });
		// }
		//
		// } catch (RepositoryException e) {
		// throw new PeopleException("Error while getting properties", e);
		// }
		// panel.redraw();
		// panel.layout();
		// // panel.pack(true);
		// // panel.getParent().pack(true);
		// panel.getParent().layout();
	}

	private void populateDisplayCmp(Composite parent, Node contactNode)
			throws RepositoryException {

		if (contactNode.isNodeType(PeopleTypes.PEOPLE_EMAIL)
				|| contactNode.isNodeType(PeopleTypes.PEOPLE_PHONE)
				|| contactNode.isNodeType(PeopleTypes.PEOPLE_IMPP)
				|| contactNode.isNodeType(PeopleTypes.PEOPLE_URL))
			populateEditableMailCmp(parent, contactNode, contactNode
					.getParent().getParent());
	}

	private void populateEditableMailCmp(Composite parent,
			final Node contactNode, final Node parVersionableNode) {
		RowLayout rl = new RowLayout(SWT.WRAP);
		rl.type = SWT.HORIZONTAL;
		parent.setLayout(rl);
		String type = CommonsJcrUtils.getStringValue(contactNode,
				PeopleNames.PEOPLE_CONTACT_LABEL);
		if (CommonsJcrUtils.checkNotEmptyString(type))
			type = CommonsJcrUtils.get(contactNode,
					PeopleNames.PEOPLE_CONTACT_CATEGORY);

		final Button categoryBtn = createCategoryButton(parent, contactNode);
		final Button primaryBtn = createPrimaryButton(parent);
		final Button deleteBtn = createDeleteButton(parent);

		final Text valueTxt = toolkit.createText(parent, null, SWT.BORDER);
		RowData rd = new RowData();
		rd.width = 200;
		rd.height = 14;
		valueTxt.setLayoutData(rd);
		valueTxt.setData("propName", PeopleNames.PEOPLE_CONTACT_VALUE);

		final AbstractFormPart sPart = new AbstractFormPart() {
			public void refresh() {
				super.refresh();
				try {
					// TODO clean this workaround to insure node has not been
					// already removed.
					valueTxt.setText(contactNode.getProperty(
							PeopleNames.PEOPLE_CONTACT_VALUE).getString());
					valueTxt.setEnabled(CommonsJcrUtils
							.isNodeCheckedOutByMe(parVersionableNode));
					boolean isPrimary = contactNode.getProperty(
							PeopleNames.PEOPLE_IS_PRIMARY).getBoolean();
					if (isPrimary)
						primaryBtn.setImage(PeopleImages.PRIMARY_BTN);
					else
						primaryBtn.setImage(PeopleImages.PRIMARY_NOT_BTN);

				} catch (Exception e) {
					if (e instanceof InvalidItemStateException)
						;
					else
						throw new PeopleException(
								"unexpected error while refreshing", e);
					// Node has already been removed, continue.
				}
			}
		};

		sPart.refresh();
		sPart.initialize(form);
		form.addPart(sPart);
		configureDeleteButton(deleteBtn, contactNode, parVersionableNode);
		configurePrimaryButton(primaryBtn, contactNode, parVersionableNode);
	}

	private Button createCategoryButton(Composite parent, Node contactNode) {
		Button btn = new Button(parent, SWT.FLAT);
		btn.setData(RWT.CUSTOM_VARIANT, PeopleUiConstants.CSS_FLAT_IMG_BUTTON);

		try {

			if (contactNode.isNodeType(PeopleTypes.PEOPLE_EMAIL))
				btn.setImage(PeopleImages.MAIL);
			else if (contactNode.isNodeType(PeopleTypes.PEOPLE_PHONE)) {
				String label = CommonsJcrUtils.get(contactNode,
						PeopleNames.PEOPLE_CONTACT_LABEL);
				if (PeopleConstants.CONTACT_LABEL_FAX.equals(label))
					btn.setImage(PeopleImages.FAX);
				else if (PeopleConstants.CONTACT_LABEL_MOBILE.equals(label))
					btn.setImage(PeopleImages.MOBILE);
				else if (PeopleConstants.CONTACT_LABEL_RECEPTION.equals(label))
					btn.setImage(PeopleImages.PHONE_DIRECT);
				// else if (PeopleConstants.CONTACT_CATEGORY_WORK.equals(label))
				// btn.setImage(PeopleImages.WORK);
				// else if
				// (PeopleConstants.CONTACT_CATEGORY_PRIVATE.equals(label))
				// btn.setImage(PeopleImages.PHONE);

			} else if (contactNode.isNodeType(PeopleTypes.PEOPLE_URL)) {
				// String category = CommonsJcrUtils.get(contactNode,
				// PeopleNames.PEOPLE_CONTACT_CATEGORY);
				// TODO manage all possibilities.
				btn.setImage(PeopleImages.WWW);
			} else if (contactNode.isNodeType(PeopleTypes.PEOPLE_IMPP)) {

			}

			RowData rd = new RowData();
			rd.height = 16;
			rd.width = 16;
			btn.setLayoutData(rd);
			return btn;
		} catch (RepositoryException re) {
			throw new PeopleException("unable to get image for contact");
		}
	}

	private Button createDeleteButton(Composite parent) {
		Button btn = new Button(parent, SWT.FLAT);
		btn.setData(RWT.CUSTOM_VARIANT, PeopleUiConstants.CSS_FLAT_IMG_BUTTON);
		btn.setImage(PeopleImages.DELETE_BTN);
		RowData rd = new RowData();
		rd.height = 16;
		rd.width = 16;
		btn.setLayoutData(rd);
		return btn;
	}

	private Button createPrimaryButton(Composite parent) {
		Button btn = new Button(parent, SWT.FLAT);
		btn.setData(RWT.CUSTOM_VARIANT, PeopleUiConstants.CSS_FLAT_IMG_BUTTON);
		btn.setImage(PeopleImages.PRIMARY_NOT_BTN);
		RowData rd = new RowData();
		rd.height = 16;
		rd.width = 16;
		btn.setLayoutData(rd);
		return btn;
	}

	private void configureDeleteButton(Button btn, final Node node,
			final Node parNode) { // , final AbstractFormPart
									// genericContactFormPart
		btn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				try {
					boolean wasCheckedOut = CommonsJcrUtils
							.isNodeCheckedOutByMe(parNode);
					if (!wasCheckedOut)
						CommonsJcrUtils.checkout(parNode);
					node.remove();
					if (wasCheckedOut)
						parNode.getSession().save();
					else
						CommonsJcrUtils.saveAndCheckin(parNode);
				} catch (RepositoryException e) {
					throw new PeopleException("unable to initialise deletion",
							e);
				}
				for (IFormPart part : form.getParts()) {
					((AbstractFormPart) part).markStale();
					part.refresh();
				}
			}
		});

	}

	private void configurePrimaryButton(Button btn, final Node node,
			final Node parNode) {
		btn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				try {
					boolean wasCheckedOut = CommonsJcrUtils
							.isNodeCheckedOutByMe(parNode);
					if (!wasCheckedOut)
						CommonsJcrUtils.checkout(parNode);
					boolean wasPrimary = false;
					if (node.hasProperty(PeopleNames.PEOPLE_IS_PRIMARY)
							&& node.getProperty(PeopleNames.PEOPLE_IS_PRIMARY)
									.getBoolean())
						wasPrimary = true;
					PeopleJcrUtils.markAsPrimary(node, !wasPrimary);
					if (wasCheckedOut)
						parNode.getSession().save();
					else
						CommonsJcrUtils.saveAndCheckin(parNode);
					for (IFormPart part : form.getParts()) {
						((AbstractFormPart) part).markStale();
						part.refresh();
					}
				} catch (RepositoryException e) {
					throw new PeopleException("unable to initialise deletion",
							e);
				}
			}
		});

	}

	private void refreshDisplayCmp(Composite parent, Node contactNode)
			throws RepositoryException {
		Control[] children = parent.getChildren();
		for (Control control : children) {
			if (control instanceof Label) {
				((Label) control).setText("Updated label");
			}
		}
	}

	public void populateNotePanel(final Composite parent, final Node entity) {
		parent.setLayout(new GridLayout());

		parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		toolkit.createLabel(parent, "Notes: ", SWT.NONE);

		final Text notesTxt = toolkit.createText(parent, "", SWT.BORDER
				| SWT.MULTI | SWT.WRAP);

		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.widthHint = 200;
		gd.minimumHeight = 200;
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
				parent.layout();
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
					PeopleJcrUtils.createContact(entity, contactType,
							contactType, value, isPrimary, cat, label);

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