package org.argeo.connect.ui.workbench;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.argeo.cms.ui.CmsEditable;
import org.argeo.cms.ui.workbench.util.CommandUtils;
import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.ConnectConstants;
import org.argeo.connect.ConnectException;
import org.argeo.connect.ConnectNames;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.ui.ConnectUiConstants;
import org.argeo.connect.ui.ConnectUiSnippets;
import org.argeo.connect.ui.ConnectUiStyles;
import org.argeo.connect.ui.ConnectUiUtils;
import org.argeo.connect.ui.widgets.ConnectAbstractDropDown;
import org.argeo.connect.ui.workbench.commands.OpenEntityEditor;
import org.argeo.connect.ui.workbench.commands.OpenSearchEntityEditor;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.eclipse.ui.jcr.lists.NodeViewerComparator;
import org.argeo.eclipse.ui.jcr.lists.RowViewerComparator;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.widgets.FormToolkit;

/** Helper methods to ease UI implementation in a Connect Workbench */
public class ConnectWorkbenchUtils {

	// Experimental
	// public static PeopleService getPeopleService() {
	// return (PeopleService)
	// Display.getDefault().getData(PeopleRapConstants.KEY_PEOPLE_SERVICE);
	// }

	/**
	 * Shortcut to retrieve the current active page. It assumes the bundle
	 * WorkbenchUiPlugin is started (it is by the way the basis of all apps
	 * based upon argeo framework that use the workbench)
	 */
	@Deprecated
	public static IWorkbenchPage getActivePage() {
		return null;
		// return
		// WorkbenchUiPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
	}

	/**
	 * Helper to call a command with a few parameter easily. The additional
	 * parameters must be Pairs with each time a parameterID and a
	 * parameterValue couple in this order
	 */
	public static void callCommand(String commandID, String... parameters) {
		Map<String, String> params = new HashMap<String, String>();
		for (int i = 0; i < (parameters.length / 2); i++)
			params.put(parameters[i * 2], parameters[i * 2 + 1]);
		CommandUtils.callCommand(commandID, params);
	}

	/**
	 * Shortcut to refresh a <code>DateTime</code> widget given a Node in a form
	 * and a property Name. Also manages its enable state. Note that, by
	 * default, we force setting of the time to noon. Might be later enhanced.
	 * 
	 * If the property does not yet exits, it is not created and the
	 */
	public static void refreshFormDateTimeWidget(CmsEditable editable, DateTime dateTime, Node node, String propName) {
		try {
			Calendar dateToDisplay = null;
			if (node.hasProperty(propName))
				dateToDisplay = node.getProperty(propName).getDate();
			else
				dateToDisplay = GregorianCalendar.getInstance();

			dateTime.setDate(dateToDisplay.get(Calendar.YEAR), dateToDisplay.get(Calendar.MONTH),
					dateToDisplay.get(Calendar.DAY_OF_MONTH));
			dateTime.setTime(12, 0, 0);
			dateTime.setEnabled(editable.isEditing());
		} catch (RepositoryException re) {
			throw new ConnectException(
					"unable to refresh DateTime widget for node " + node + " and property " + propName, re);
		}
	}

	/**
	 * Shortcut to refresh a <code>Text</code> widget given a Node in a form and
	 * a property Name. Also manages its enable state
	 */
	public static String refreshFormTextWidget(CmsEditable editable, Text text, Node node, String propName) {
		String newStr = ConnectJcrUtils.get(node, propName);
		String oldStr = text.getText();
		if (!newStr.equals(oldStr))
			text.setText(newStr);
		text.setEnabled(editable.isEditing());
		return newStr;
	}

	/**
	 * Shortcut to refresh a <code>Text</code> widget given a Node in a form and
	 * a property Name. Also manages its enable state and set a default message
	 * if corresponding Text value is empty
	 */
	public static String refreshFormText(CmsEditable editable, Text text, Node entity, String propName,
			String defaultMsg) {
		String tmpStr = refreshFormTextWidget(editable, text, entity, propName);
		if (EclipseUiUtils.isEmpty(tmpStr) && EclipseUiUtils.notEmpty(defaultMsg))
			text.setMessage(defaultMsg);
		return tmpStr;
	}

	/**
	 * Shortcut to select an item of a <code>Combo</code> widget given a Node in
	 * a form, a property Name. Also manages its enable state.
	 */
	public static void refreshFormCombo(CmsEditable editable, Combo combo, Node node, String propName) {
		String currValue = ConnectJcrUtils.get(node, propName);
		if (EclipseUiUtils.notEmpty(currValue))
			combo.select(combo.indexOf(currValue));
		combo.setEnabled(editable.isEditing());
	}

	/**
	 * Shortcut to refresh a Check box <code>Button</code> with an encoded
	 * boolean flag widget given a node in a form and a property name.
	 */
	public static boolean refreshFlagFormCheckBox(CmsEditable editable, Button button, Node entity, String propName,
			int cache) {
		long val = 0;
		Boolean tmp = null;
		try {
			if (entity.hasProperty(propName)) {
				val = entity.getProperty(propName).getLong();
				tmp = (val & cache) != 0;
				button.setSelection(tmp);
			} else
				tmp = false;
			button.setEnabled(editable.isEditing());
		} catch (RepositoryException re) {
			throw new ConnectException("unable get boolean value for property " + propName);
		}
		return tmp;
	}

	/**
	 * Shortcut to refresh a Check box <code>Button</code> widget given a Node
	 * in a form and a property Name.
	 */
	public static boolean refreshFormCheckBox(CmsEditable editable, Button button, Node entity, String propName) {
		Boolean tmp = null;
		try {
			if (entity.hasProperty(propName)) {
				tmp = entity.getProperty(propName).getBoolean();
				button.setSelection(tmp);
			} else
				tmp = false;
			button.setEnabled(editable.isEditing());
		} catch (RepositoryException re) {
			throw new ConnectException("unable get boolean value for property " + propName);
		}
		return tmp;
	}

	/**
	 * Shortcut to refresh the text underlying a DropDown widget given a Node
	 * and a property Name.
	 */
	public static String refreshDropDown(ConnectAbstractDropDown dropDown, Node entity, String propName) {
		String tmp = null;
		try {
			if (entity.hasProperty(propName)) {
				tmp = entity.getProperty(propName).getString();
				dropDown.reset(ConnectJcrUtils.get(entity, propName));
			} else
				dropDown.reset(null);
		} catch (RepositoryException re) {
			throw new ConnectException("unable get boolean value for property " + propName);
		}
		return tmp;
	}

	/**
	 * Shortcut to refresh a radio <code>Button</code> widget given a Node in a
	 * form and a property Name. Also manage its enabled state
	 */
	public static void refreshFormRadio(CmsEditable editor, Button button, Node entity, String propName) {
		Boolean tmp = null;
		try {
			if (entity.hasProperty(propName)) {
				tmp = entity.getProperty(propName).getString().equals(button.getText());
			} else
				tmp = false;
			button.setSelection(tmp);
			button.setEnabled(editor.isEditing());
		} catch (RepositoryException re) {
			throw new ConnectException("Unable to get boolean value for property " + propName + " on " + entity, re);
		}
	}

	/**
	 * Creates a new selection adapter in order to provide sort abilities to a
	 * table that displays JCR Rows
	 * 
	 * @param index
	 * @param propertyType
	 * @param selectorName
	 * @param propertyName
	 * @param comparator
	 * @param viewer
	 * @return
	 */
	public static SelectionAdapter getSelectionAdapter(final int index, final int propertyType,
			final String selectorName, final String propertyName, final RowViewerComparator comparator,
			final TableViewer viewer) {
		SelectionAdapter selectionAdapter = new SelectionAdapter() {
			private static final long serialVersionUID = -3452356616673385039L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				Table table = viewer.getTable();
				comparator.setColumn(propertyType, selectorName, propertyName);
				int dir = table.getSortDirection();
				if (table.getSortColumn() == table.getColumn(index)) {
					dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
				} else {
					dir = SWT.DOWN;
				}
				table.setSortDirection(dir);
				table.setSortColumn(table.getColumn(index));
				viewer.refresh();
			}
		};
		return selectionAdapter;
	}

	/**
	 * Creates a new selection adapter in order to provide sort abilities to a
	 * table that displays JCR nodes
	 * 
	 * @param index
	 * @param propertyType
	 * @param selectorName
	 * @param propertyName
	 * @param comparator
	 * @param viewer
	 * @return
	 */
	public static SelectionAdapter getSelectionAdapter(final int index, final int propertyType,
			final String propertyName, final NodeViewerComparator comparator, final TableViewer viewer) {
		SelectionAdapter selectionAdapter = new SelectionAdapter() {
			private static final long serialVersionUID = -3452356616673385039L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				Table table = viewer.getTable();
				comparator.setColumn(propertyType, propertyName);
				int dir = table.getSortDirection();
				if (table.getSortColumn() == table.getColumn(index)) {
					dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
				} else {
					dir = SWT.DOWN;
				}
				table.setSortDirection(dir);
				table.setSortColumn(table.getColumn(index));
				viewer.refresh();
			}
		};
		return selectionAdapter;
	}

	/**
	 * Shortcut to add a default modify listeners to a <code>DateTime</code>
	 * widget that is bound a JCR String Property. Any change in the text is
	 * immediately stored in the active session, but no save is done.
	 */
	public static void addSelectionListener(final DateTime dateTime, final Node node, final String propName,
			final AbstractFormPart part) {
		dateTime.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				Calendar value = GregorianCalendar.getInstance();
				value.set(dateTime.getYear(), dateTime.getMonth(), dateTime.getDay(), dateTime.getHours(),
						dateTime.getMinutes());
				if (ConnectJcrUtils.setJcrProperty(node, propName, PropertyType.DATE, value))
					part.markDirty();
			}
		});
	}

	/**
	 * Shortcut to add a default selection listener to a Check Box
	 * <code>Button</code> widget that is bound a JCR boolean property. Any
	 * change in the selection is immediately stored in the active session, but
	 * not saved
	 */
	public static void addCheckBoxListener(final Button button, final Node node, final String propName,
			final AbstractFormPart part) {
		button.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean value = button.getSelection();
				if (ConnectJcrUtils.setJcrProperty(node, propName, PropertyType.BOOLEAN, value))
					part.markDirty();
			}
		});
	}

	/**
	 * Shortcut to add a default selection listener to a Check Box
	 * <code>Button</code> widget that is bound a JCR boolean property. Any
	 * change in the selection is immediately stored in the active session, but
	 * not saved
	 */
	public static void addFlagCheckBoxListener(final Button button, final Node node, final String propName,
			final int cache, final AbstractFormPart part) {
		button.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean value = button.getSelection();

				Long oldValue = ConnectJcrUtils.getLongValue(node, propName);
				if (oldValue == null)
					oldValue = 0L;
				boolean oldFlag = (oldValue & cache) != 0;
				if (value != oldFlag) {
					long newValue = oldValue ^ cache;
					ConnectJcrUtils.setJcrProperty(node, propName, PropertyType.LONG, newValue);
					part.markDirty();
				}
			}
		});
	}

	/**
	 * Shortcut to add a default modify listeners to a <code>Text</code> widget
	 * that is bound a JCR String Property. Any change in the text is
	 * immediately stored in the active session, but no save is done.
	 */
	public static void addModifyListener(final Text text, final Node node, final String propName,
			final AbstractFormPart part) {
		text.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void modifyText(ModifyEvent event) {
				if (ConnectJcrUtils.setJcrProperty(node, propName, PropertyType.STRING, text.getText()))
					part.markDirty();
			}
		});
	}

	/**
	 * Shortcut to add a Text Modifylistener that updates a property on a Node
	 */
	public static void addTxtModifyListener(final AbstractFormPart part, final Text text, final Node entity,
			final String propName, final int propType) {
		text.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 1549789407363632491L;

			@Override
			public void modifyText(ModifyEvent event) {
				if (ConnectJcrUtils.setJcrProperty(entity, propName, propType, text.getText()))
					part.markDirty();
			}
		});
	}

	/**
	 * Shortcut to add a SelectionListener on a combo that updates a property on
	 * a Node
	 */
	public static void addComboSelectionListener(final AbstractFormPart part, final Combo combo, final Node entity,
			final String propName, final int propType) {
		combo.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = combo.getSelectionIndex();
				if (index != -1) {
					String selectedCategory = combo.getItem(index);
					if (ConnectJcrUtils.setJcrProperty(entity, propName, propType, selectedCategory))
						part.markDirty();
				}

			}
		});
	}

	/**
	 * Shortcut to add a Text Modifylistener that updates a LONG property on a
	 * Node. Checks the input validity while the user is typing
	 */
	public static void addNbOnlyTxtModifyListener(IWorkbench workbench, final AbstractFormPart part, final Text text,
			final Node entity, final String propName, final int propType) {
		final ControlDecoration decoration = new ControlDecoration(text, SWT.TOP | SWT.LEFT);
		decoration.setImage(workbench.getSharedImages().getImage(ISharedImages.IMG_DEC_FIELD_ERROR));
		decoration.hide();

		text.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 1L;

			public void modifyText(ModifyEvent event) {
				String lengthStr = text.getText();
				if (!ConnectUiUtils.isNumbers(lengthStr)) {
					text.setBackground(new Color(text.getDisplay(), 250, 200, 150));
					decoration.show();
					decoration.setDescriptionText("Length can only be a number: " + lengthStr);
				} else {
					text.setBackground(null);
					decoration.hide();
					Long length = null;
					if (EclipseUiUtils.notEmpty(lengthStr))
						length = new Long(lengthStr);
					if (ConnectJcrUtils.setJcrProperty(entity, propName, propType, length))
						part.markDirty();
				}
			}
		});
	}

	/**
	 * Simply create a link to open a search editor with the given parameters
	 * 
	 * @param appWorkbenchService
	 * @param parent
	 * @param label
	 * @param nodeType
	 * @return
	 */
	public static Link createOpenSearchEditorLink(final AppWorkbenchService appWorkbenchService, Composite parent,
			final String label, final String nodeType) {
		Link link = new Link(parent, SWT.NONE);
		link.setText("<a>" + label + "</a>");
		link.setLayoutData(EclipseUiUtils.fillWidth());
		link.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				Map<String, String> params = new HashMap<String, String>();
				params.put(OpenSearchEntityEditor.PARAM_NODE_TYPE, nodeType);
				params.put(OpenSearchEntityEditor.PARAM_EDITOR_NAME, label);
				String cmdId = appWorkbenchService.getOpenSearchEntityEditorCmdId();
				CommandUtils.callCommand(cmdId, params);
			}
		});
		return link;
	}

	/**
	 * Simply create a link to open whatever editor
	 * 
	 * @param iwPage
	 * @param parent
	 * @param editorInput
	 * @param editorId
	 * @param label
	 * @return
	 */
	public static Link createOpenEditorLink(final IWorkbenchPage iwPage, Composite parent,
			final IEditorInput editorInput, final String editorId, final String label) {
		Link link = new Link(parent, SWT.NONE);
		link.setText("<a>" + label + "</a>");
		link.setLayoutData(EclipseUiUtils.fillWidth());
		link.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				try {
					IEditorPart iep = iwPage.findEditor(editorInput);
					if (iep == null) {
						iwPage.openEditor(editorInput, editorId);
					} else
						iwPage.activate(iep);

				} catch (PartInitException e) {
					throw new ConnectException("Unable to open editor with ID " + editorId, e);
				}
			}
		});
		return link;
	}

	/**
	 * Simply create a link to open an entity editor for the given entity node
	 * 
	 * @param parent
	 * @param label
	 * @param entity
	 * @return
	 */
	public static Link createOpenEntityEditorLink(final AppWorkbenchService appWorkbenchService, Composite parent,
			final String label, final Node entity) {
		Link link = new Link(parent, SWT.NONE);
		link.setText("<a>" + label + "</a>");
		link.setLayoutData(EclipseUiUtils.fillWidth());
		link.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				Map<String, String> params = new HashMap<String, String>();
				params.put(OpenEntityEditor.PARAM_JCR_ID, ConnectJcrUtils.getIdentifier(entity));
				CommandUtils.callCommand(appWorkbenchService.getOpenEntityEditorCmdId(), params);
			}
		});
		return link;
	}

	/**
	 * Shortcut to create a delete button that will be used in composites that
	 * display a multi value property in tag-like manner
	 */
	public static Button createDeleteButton(Composite parent) {
		Button button = new Button(parent, SWT.FLAT | SWT.PUSH);
		CmsUtils.style(button, ConnectUiStyles.SMALL_DELETE_BTN);
		RowData rd = new RowData();
		rd.height = 16;
		rd.width = 16;
		button.setLayoutData(rd);
		return button;
	}

	/** Creates a text widget with RowData already set */
	public static Text createRDText(FormToolkit toolkit, Composite parent, String msg, String toolTip, int width) {
		Text text = toolkit.createText(parent, "", SWT.BORDER | SWT.SINGLE | SWT.LEFT);
		text.setMessage(msg);
		text.setToolTipText(toolTip);
		text.setLayoutData(width == 0 ? new RowData() : new RowData(width, SWT.DEFAULT));
		return text;
	}

	/**
	 * Creates the basic right aligned bold label that is used in various forms
	 * using a pre-defined toolkit.
	 */
	public static Label createBoldLabel(FormToolkit toolkit, Composite parent, String value) {

		// We add a blank space before to workaround the cropping of the word
		// first letter in some OS/Browsers (typically MAC/Firefox 31 )
		Label label = toolkit.createLabel(parent, " " + value, SWT.RIGHT);
		label.setFont(EclipseUiUtils.getBoldFont(parent));
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		return label;
	}

	/**
	 * Creates a basic right-aligned vertical-centered bold label with no
	 * specific toolkit.
	 */
	public static Label createBoldLabel(Composite parent, String value) {
		Label label = new Label(parent, SWT.RIGHT);
		label.setText(" " + value);
		label.setFont(EclipseUiUtils.getBoldFont(parent));
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		return label;
	}

	/**
	 * Creates a basic right aligned bold label with no specific toolkit.
	 * precise vertical alignment
	 */
	public static Label createBoldLabel(Composite parent, String value, int verticalAlign) {
		Label label = new Label(parent, SWT.RIGHT);
		label.setText(" " + value);
		label.setFont(EclipseUiUtils.getBoldFont(parent));
		label.setLayoutData(new GridData(SWT.RIGHT, verticalAlign, false, false));
		return label;
	}

	/**
	 * Creates a text widget with GridData already set
	 * 
	 * @param toolkit
	 * @param parent
	 * @param msg
	 * @param toolTip
	 * @param width
	 * @param colSpan
	 * @return
	 */
	public static Text createGDText(FormToolkit toolkit, Composite parent, String msg, String toolTip, int width,
			int colSpan) {
		Text text = toolkit.createText(parent, "", SWT.BORDER | SWT.SINGLE | SWT.LEFT);
		text.setMessage(msg);
		text.setToolTipText(toolTip);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.widthHint = width;
		gd.horizontalSpan = colSpan;
		text.setLayoutData(gd);
		return text;
	}

	public static void setTableDefaultStyle(TableViewer viewer, int customItemHeight) {
		setTableDefaultStyle(viewer.getTable(), customItemHeight);
	}

	public static void setTableDefaultStyle(Table table, int customItemHeight) {
		table.setLinesVisible(true);
		table.setHeaderVisible(false);
		CmsUtils.setItemHeight(table, customItemHeight);
		CmsUtils.markup(table);
	}

	// Layouts and LayoutData

	/** shortcut to set form data while dealing with switching panel */
	public static void setSwitchingFormData(Composite composite) {
		FormData fdLabel = new FormData();
		fdLabel.top = new FormAttachment(0, 0);
		fdLabel.left = new FormAttachment(0, 0);
		fdLabel.right = new FormAttachment(100, 0);
		fdLabel.bottom = new FormAttachment(100, 0);
		composite.setLayoutData(fdLabel);
	}

	/**
	 * Shortcut to quickly get a FormData object with configured FormAttachment
	 * 
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 * @return
	 */
	public static FormData createformData(int left, int top, int right, int bottom) {
		FormData formData = new FormData();
		formData.left = new FormAttachment(left, 0);
		formData.top = new FormAttachment(top, 0);
		formData.right = new FormAttachment(right, 0);
		formData.bottom = new FormAttachment(bottom, 0);
		return formData;
	}

	/**
	 * Create the text value of a link that enable calling the
	 * <code>OpenEditor</code> command from a cell of a HTML list
	 */
	public static String getOpenEditorSnippet(String commandId, Node relevantNode, String value) {
		String toEditJcrId = ConnectJcrUtils.getIdentifier(relevantNode);
		String href = commandId + "/" + OpenEntityEditor.PARAM_JCR_ID + "=" + toEditJcrId;
		return ConnectUiSnippets.getRWTLink(href, value);
	}

	/** display clickable tags that are linked to the current entity */
	public static String getTags(ResourcesService resourceService, AppWorkbenchService appWorkbenchService,
			Node entity) {
		try {
			StringBuilder tags = new StringBuilder();
			if (entity.hasProperty(ConnectNames.CONNECT_TAGS)) {
				for (Value value : entity.getProperty((ConnectNames.CONNECT_TAGS)).getValues())
					tags.append("#").append(ConnectWorkbenchUtils.getTagLink(ConnectJcrUtils.getSession(entity),
							resourceService, appWorkbenchService, ConnectConstants.RESOURCE_TAG, value.getString()))
							.append("  ");
			}
			return ConnectUiUtils.replaceAmpersand(tags.toString());
		} catch (RepositoryException e) {
			throw new ConnectException("Error while getting tags for entity", e);
		}
	}

	/**
	 * Generate a href link that will call the openEntityEditor Command for this
	 * tag if it is already registered. The corresponding Label / List must have
	 * a HtmlRWTAdapter to catch when the user click on the link
	 */
	public static String getTagLink(Session session, ResourcesService resourceService,
			AppWorkbenchService appWorkbenchService, String tagId, String value) {
		String commandId = appWorkbenchService.getOpenEntityEditorCmdId();
		Node tag = resourceService.getRegisteredTag(session, tagId, value);
		if (tag == null)
			return value;
		String tagJcrId = ConnectJcrUtils.getIdentifier(tag);
		String href = commandId + ConnectUiConstants.HREF_SEPARATOR;
		href += OpenEntityEditor.PARAM_JCR_ID + "=" + tagJcrId;
		return ConnectUiSnippets.getRWTLink(href, value);
	}
}
