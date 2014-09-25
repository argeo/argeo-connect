package org.argeo.connect.people.ui.utils;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;
import javax.jcr.query.qom.StaticOperand;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.ui.PeopleImages;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiService;
import org.argeo.connect.people.ui.commands.OpenEntityEditor;
import org.argeo.connect.people.ui.commands.OpenSearchEntityEditor;
import org.argeo.connect.people.ui.composites.dropdowns.PeopleAbstractDropDown;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.eclipse.ui.jcr.lists.NodeViewerComparator;
import org.argeo.eclipse.ui.jcr.lists.RowViewerComparator;
import org.argeo.eclipse.ui.utils.CommandUtils;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.resource.ImageDescriptor;
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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.services.IServiceLocator;

/** Some helper methods that factorize widely used snippets in people UI */
public class PeopleUiUtils {

	// Some methods that must be factorized in Commons Layers soon

	/**
	 * TODO Use commandUtils equivalent as soon as Commons v2.1.9 is released.
	 */
	@Deprecated
	public static void refreshParameterizedCommand(IMenuManager menuManager,
			IServiceLocator locator, String contributionId, String commandId,
			String label, ImageDescriptor icon, boolean showCommand,
			Map<String, String> params) {
		IContributionItem ici = menuManager.find(contributionId);
		if (ici != null)
			menuManager.remove(ici);
		if (showCommand) {
			CommandContributionItemParameter contributionItemParameter = new CommandContributionItemParameter(
					locator, null, commandId, SWT.PUSH);

			// Set Params
			contributionItemParameter.label = label;
			contributionItemParameter.icon = icon;

			if (params != null)
				contributionItemParameter.parameters = params;

			CommandContributionItem cci = new CommandContributionItem(
					contributionItemParameter);
			cci.setId(contributionId);
			menuManager.add(cci);
		}
	}

	/**
	 * Shortcut to refresh the value of a <code>Text</code> given a Node and a
	 * property Name
	 */
	public static String refreshTextWidgetValue(Text text, Node entity,
			String propName) {
		String tmpStr = CommonsJcrUtils.get(entity, propName);
		if (CommonsJcrUtils.checkNotEmptyString(tmpStr))
			text.setText(tmpStr);
		return tmpStr;
	}

	/**
	 * Shortcut to refresh a <code>DateTime</code> widget given a Node in a form
	 * and a property Name. Also manages its enable state. Note that, by
	 * default, we force setting of the time to noon. Might be later enhanced.
	 * 
	 * If the property does not yet exits, it is not created and the
	 */
	public static void refreshFormDateTimeWidget(DateTime dateTime, Node node,
			String propName) {
		try {
			Calendar dateToDisplay = null;
			if (node.hasProperty(propName))
				dateToDisplay = node.getProperty(propName).getDate();
			else
				dateToDisplay = GregorianCalendar.getInstance();

			dateTime.setDate(dateToDisplay.get(Calendar.YEAR),
					dateToDisplay.get(Calendar.MONTH),
					dateToDisplay.get(Calendar.DAY_OF_MONTH));
			dateTime.setTime(12, 0, 0);
			dateTime.setEnabled(CommonsJcrUtils.isNodeCheckedOutByMe(node));
		} catch (RepositoryException re) {
			throw new PeopleException(
					"unable to refresh DateTime widget for node " + node
							+ " and property " + propName, re);
		}
	}

	/**
	 * Shortcut to refresh a <code>Text</code> widget given a Node in a form and
	 * a property Name. Also manages its enable state
	 */
	public static String refreshFormTextWidget(Text text, Node entity,
			String propName) {
		String tmpStr = CommonsJcrUtils.get(entity, propName);
		if (CommonsJcrUtils.checkNotEmptyString(tmpStr))
			text.setText(tmpStr);
		text.setEnabled(CommonsJcrUtils.isNodeCheckedOutByMe(entity));
		return tmpStr;
	}

	/**
	 * Shortcut to refresh a <code>Text</code> widget given a Node in a form and
	 * a property Name. Also manages its enable state and set a default message
	 * if corresponding Text value is empty
	 */
	public static String refreshFormTextWidget(Text text, Node entity,
			String propName, String defaultMsg) {
		String tmpStr = refreshFormTextWidget(text, entity, propName);
		if (CommonsJcrUtils.isEmptyString(tmpStr)
				&& CommonsJcrUtils.checkNotEmptyString(defaultMsg))
			text.setMessage(defaultMsg);
		return tmpStr;
	}

	/**
	 * Shortcut to select an item of a <code>Combo</code> widget given a Node in
	 * a form, a property Name. Also manages its enable state.
	 */
	public static void refreshFormComboValue(Combo combo, Node node,
			String propName) {
		String currValue = CommonsJcrUtils.get(node, propName);
		if (CommonsJcrUtils.checkNotEmptyString(currValue))
			combo.select(combo.indexOf(currValue));
		combo.setEnabled(CommonsJcrUtils.isNodeCheckedOutByMe(node));
	}

	/**
	 * Shortcut to refresh a Check box <code>Button</code> widget given a Node
	 * in a form and a property Name.
	 */
	public static boolean refreshCheckBoxWidget(Button button, Node entity,
			String propName) {
		Boolean tmp = null;
		try {
			if (entity.hasProperty(propName)) {
				tmp = entity.getProperty(propName).getBoolean();
				button.setSelection(tmp);
			} else
				tmp = false;
			button.setEnabled(CommonsJcrUtils.isNodeCheckedOutByMe(entity));
		} catch (RepositoryException re) {
			throw new PeopleException("unable get boolean value for property "
					+ propName);
		}
		return tmp;
	}

	/**
	 * Shortcut to refresh the text underlying a DropDown widget given a Node
	 * and a property Name.
	 */
	public static String refreshDropDownWidget(PeopleAbstractDropDown dropDown,
			Node entity, String propName) {
		String tmp = null;
		try {
			if (entity.hasProperty(propName)) {
				tmp = entity.getProperty(propName).getString();
				dropDown.reset(CommonsJcrUtils.get(entity, propName));
			} else
				dropDown.reset(null);
		} catch (RepositoryException re) {
			throw new PeopleException("unable get boolean value for property "
					+ propName);
		}
		return tmp;
	}

	/**
	 * Shortcut to refresh a radio <code>Button</code> widget given a Node in a
	 * form and a property Name. Also manage its enabled state
	 */
	public static void refreshRadioWidget(Button button, Node entity,
			String propName) {
		Boolean tmp = null;
		try {
			if (entity.hasProperty(propName)) {
				tmp = entity.getProperty(propName).getString()
						.equals(button.getText());
			} else
				tmp = false;
			button.setSelection(tmp);
			button.setEnabled(CommonsJcrUtils.isNodeCheckedOutByMe(entity));
		} catch (RepositoryException re) {
			throw new PeopleException(
					"Unable to get boolean value for property " + propName
							+ " on " + entity, re);
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
	public static SelectionAdapter getSelectionAdapter(final int index,
			final int propertyType, final String selectorName,
			final String propertyName, final RowViewerComparator comparator,
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
	public static SelectionAdapter getSelectionAdapter(final int index,
			final int propertyType, final String propertyName,
			final NodeViewerComparator comparator, final TableViewer viewer) {
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
	public static void addSelectionListener(final DateTime dateTime,
			final Node node, final String propName, final AbstractFormPart part) {
		dateTime.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				Calendar value = GregorianCalendar.getInstance();
				value.set(dateTime.getYear(), dateTime.getMonth(),
						dateTime.getDay(), dateTime.getHours(),
						dateTime.getMinutes());
				if (CommonsJcrUtils.setJcrProperty(node, propName,
						PropertyType.DATE, value))
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
	public static void addCheckBoxListener(final Button button,
			final Node node, final String propName, final AbstractFormPart part) {
		button.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean value = button.getSelection();
				if (CommonsJcrUtils.setJcrProperty(node, propName,
						PropertyType.BOOLEAN, value))
					part.markDirty();
			}
		});
	}

	/**
	 * Shortcut to add a default modify listeners to a <code>Text</code> widget
	 * that is bound a JCR String Property. Any change in the text is
	 * immediately stored in the active session, but no save is done.
	 */
	public static void addModifyListener(final Text text, final Node node,
			final String propName, final AbstractFormPart part) {
		text.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void modifyText(ModifyEvent event) {
				if (CommonsJcrUtils.setJcrProperty(node, propName,
						PropertyType.STRING, text.getText()))
					part.markDirty();
			}
		});
	}

	/**
	 * Shortcut to add a Text Modifylistener that updates a property on a Node
	 */
	public static void addTxtModifyListener(final AbstractFormPart part,
			final Text text, final Node entity, final String propName,
			final int propType) {
		text.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 1549789407363632491L;

			@Override
			public void modifyText(ModifyEvent event) {
				if (CommonsJcrUtils.setJcrProperty(entity, propName, propType,
						text.getText()))
					part.markDirty();
			}
		});
	}

	/**
	 * Shortcut to add a SelectionListener on a combo that updates a property on
	 * a Node
	 */
	public static void addComboSelectionListener(final AbstractFormPart part,
			final Combo combo, final Node entity, final String propName,
			final int propType) {
		combo.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = combo.getSelectionIndex();
				if (index != -1) {
					String selectedCategory = combo.getItem(index);
					if (CommonsJcrUtils.setJcrProperty(entity, propName,
							propType, selectedCategory))
						part.markDirty();
				}

			}
		});
	}

	/**
	 * Shortcut to add a Text Modifylistener that updates a LONG property on a
	 * Node. Checks the input validity while the user is typing
	 */
	public static void addNbOnlyTxtModifyListener(IWorkbench workbench,
			final AbstractFormPart part, final Text text, final Node entity,
			final String propName, final int propType) {
		final ControlDecoration decoration = new ControlDecoration(text,
				SWT.TOP | SWT.LEFT);
		decoration.setImage(workbench.getSharedImages().getImage(
				ISharedImages.IMG_DEC_FIELD_ERROR));
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
					if (CommonsJcrUtils.setJcrProperty(entity, propName,
							propType, length))
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

	/**
	 * Simply create a link to open a search editor with the given parameters
	 * 
	 * @param peopleUiService
	 * @param parent
	 * @param label
	 * @param nodeType
	 * @param basePath
	 * @return
	 */
	public static Link createOpenSearchEditorLink(
			final PeopleUiService peopleUiService, Composite parent,
			final String label, final String nodeType, final String basePath) {
		Link link = new Link(parent, SWT.NONE);
		link.setText("<a>" + label + "</a>");
		link.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				Map<String, String> params = new HashMap<String, String>();
				params.put(OpenSearchEntityEditor.PARAM_NODE_TYPE, nodeType);
				params.put(OpenSearchEntityEditor.PARAM_EDITOR_NAME, label);
				params.put(OpenSearchEntityEditor.PARAM_BASE_PATH, basePath);
				CommandUtils.callCommand(
						peopleUiService.getOpenSearchEntityEditorCmdId(),
						params);
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
	public static Link createOpenEntityEditorLink(
			final PeopleUiService peopleUiService, Composite parent,
			final String label, final Node entity) {
		Link link = new Link(parent, SWT.NONE);
		link.setText("<a>" + label + "</a>");
		link.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				Map<String, String> params = new HashMap<String, String>();
				params.put(OpenEntityEditor.PARAM_JCR_ID,
						CommonsJcrUtils.getIdentifier(entity));
				CommandUtils.callCommand(
						peopleUiService.getOpenEntityEditorCmdId(), params);
			}
		});
		return link;
	}

	/**
	 * Shortcut to create a delete button that will be used in composites that
	 * display a multi value property in Tag Like manner
	 */
	public static Button createDeleteButton(Composite parent) {
		Button button = new Button(parent, SWT.FLAT);
		button.setData(PeopleUiConstants.CUSTOM_VARIANT,
				PeopleUiConstants.CSS_FLAT_IMG_BUTTON);
		button.setImage(PeopleImages.DELETE_BTN_LEFT);
		RowData rd = new RowData();
		rd.height = 16;
		rd.width = 16;
		button.setLayoutData(rd);
		return button;
	}

	/** Creates a text widget with RowData already set */
	public static Text createRDText(FormToolkit toolkit, Composite parent,
			String msg, String toolTip, int width) {
		Text text = toolkit.createText(parent, "", SWT.BORDER | SWT.SINGLE
				| SWT.LEFT);
		text.setMessage(msg);
		text.setToolTipText(toolTip);
		text.setLayoutData(width == 0 ? new RowData() : new RowData(width,
				SWT.DEFAULT));
		return text;
	}

	/**
	 * Creates the basic right aligned bold label that is used in various forms.
	 */
	public static Label createBoldLabel(FormToolkit toolkit, Composite parent,
			String value) {
		Label label = toolkit.createLabel(parent, value, SWT.RIGHT);
		label.setFont(EclipseUiUtils.getBoldFont(parent));
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		return label;
	}

	/**
	 * Creates a basic right aligned vertical centered bold label with no
	 * specific toolkit.
	 * 
	 */
	public static Label createBoldLabel(Composite parent, String value) {
		Label label = new Label(parent, SWT.RIGHT);
		label.setText(value);
		label.setFont(EclipseUiUtils.getBoldFont(parent));
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		return label;
	}

	/**
	 * Creates a basic right aligned bold label with no specific toolkit.
	 * precise vertical alignment
	 */
	public static Label createBoldLabel(Composite parent, String value,
			int verticalAlign) {
		Label label = new Label(parent, SWT.RIGHT);
		label.setText(value);
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
	public static Text createGDText(FormToolkit toolkit, Composite parent,
			String msg, String toolTip, int width, int colSpan) {
		Text text = toolkit.createText(parent, "", SWT.BORDER | SWT.SINGLE
				| SWT.LEFT);
		text.setMessage(msg);
		text.setToolTipText(toolTip);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.widthHint = width;
		gd.horizontalSpan = colSpan;
		text.setLayoutData(gd);
		return text;
	}

	public static void setTableDefaultStyle(TableViewer viewer,
			int customItemHeight) {
		Table table = viewer.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(false);
		table.setData(PeopleUiConstants.MARKUP_ENABLED, Boolean.TRUE);
		table.setData(PeopleUiConstants.CUSTOM_ITEM_HEIGHT,
				Integer.valueOf(customItemHeight));
	}

	// /////////////////////////////
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
	public static FormData createformData(int left, int top, int right,
			int bottom) {
		FormData formData = new FormData();
		formData.left = new FormAttachment(left, 0);
		formData.top = new FormAttachment(top, 0);
		formData.right = new FormAttachment(right, 0);
		formData.bottom = new FormAttachment(bottom, 0);
		return formData;
	}

	/**
	 * Shortcut to create a {@link GridData} with default parameters SWT.FILL,
	 * SWT.CENTER, true, false.
	 */
	public static GridData horizontalFillData() {
		return new GridData(SWT.FILL, SWT.CENTER, true, false);
	}

	/**
	 * Shortcut to create a {@link GridLayout} with no margin and no spacing
	 * (default are normally 5 px).
	 */
	public static GridLayout noSpaceGridLayout() {
		return noSpaceGridLayout(1);
	}

	/**
	 * Shortcut to create a {@link GridLayout} with the given column number with
	 * no margin and no spacing (default are normally 5 px).
	 * makeColumnsEqualWidth parameter is set to false.
	 */
	public static GridLayout noSpaceGridLayout(int nbOfCol) {
		GridLayout gl = new GridLayout(nbOfCol, false);
		gl.marginWidth = gl.marginHeight = gl.horizontalSpacing = gl.verticalSpacing = 0;
		return gl;
	}

	/* LENGHT AND DURATION MANAGEMENT */
	/** returns corresponding seconds for a HH:MM:SS representation */
	public static long getSecondsFromLength(long lengthInSeconds) {
		return lengthInSeconds % 60;
	}

	/** returns corresponding minutes for a HH:MM:SS representation */
	public static long getMinutesFromLength(long lengthInSeconds) {
		return (lengthInSeconds / 60) % 60;
	}

	/** returns corresponding hours for a HH:MM:SS representation */
	public static long getHoursFromLength(long lengthInSeconds) {
		return (lengthInSeconds / (60 * 60)) % 60;
	}

	public static long getLengthFromHMS(int hours, int min, int secs) {
		return 60 * 60 * hours + 60 * min + secs;
	}

	/** Approximate the length in seconds in minute, round to the closest minute */
	public static long roundSecondsToMinutes(long lengthInSeconds) {
		long grounded = (lengthInSeconds / 60);
		if (getSecondsFromLength(lengthInSeconds) > 30)
			grounded++;
		return grounded;
	}

	/** format a duration in second using a hh:mm:ss pattern */
	public static String getLengthFormattedAsString(long lengthInSeconds) {
		return String.format("%02d:%02d:%02d",
				getHoursFromLength(lengthInSeconds),
				getMinutesFromLength(lengthInSeconds),
				getSecondsFromLength(lengthInSeconds));
	}

	/* QOM HELPERS */
	/**
	 * returns and(constraintA, constraintB) if constraintA != null, or
	 * constraintB otherwise (that cannot be null)
	 */
	public static Constraint localAnd(QueryObjectModelFactory factory,
			Constraint defaultC, Constraint newC) throws RepositoryException {
		if (defaultC == null)
			return newC;
		else
			return factory.and(defaultC, newC);
	}

	/** widely used pattern in various UI Parts */
	public static Constraint getFreeTextConstraint(Session session,
			QueryObjectModelFactory factory, Selector source, String filter)
			throws RepositoryException {
		Constraint defaultC = null;
		if (CommonsJcrUtils.checkNotEmptyString(filter)) {
			String[] strs = filter.trim().split(" ");
			for (String token : strs) {
				StaticOperand so = factory.literal(session.getValueFactory()
						.createValue("*" + token + "*"));
				Constraint currC = factory.fullTextSearch(
						source.getSelectorName(), null, so);
				defaultC = PeopleUiUtils.localAnd(factory, defaultC, currC);
			}
		}
		return defaultC;
	}
}
