package org.argeo.connect.people.ui;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.rap.rwt.RWT;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.widgets.FormToolkit;

/** Some helper methods that factorize widely used snippets in people UI */
public class PeopleUiUtils {
	/** shortcut to set form data while dealing with switching panel */
	public static void setSwitchingFormData(Composite composite) {
		FormData fdLabel = new FormData();
		fdLabel.top = new FormAttachment(0, 0);
		fdLabel.left = new FormAttachment(0, 0);
		fdLabel.right = new FormAttachment(100, 0);
		fdLabel.bottom = new FormAttachment(100, 0);
		composite.setLayoutData(fdLabel);
	}

	public static void setTableDefaultStyle(TableViewer viewer,
			int customItemHeight) {
		Table table = viewer.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(false);
		table.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
		table.setData(RWT.CUSTOM_ITEM_HEIGHT, Integer.valueOf(customItemHeight));
	}

	/**
	 * Shortcut to refresh the value of a <code>Text</code> given a Node and a
	 * property Name
	 */
	public static String refreshTextWidgetValue(Text text, Node entity,
			String propName) {
		String tmpStr = CommonsJcrUtils.getStringValue(entity, propName);
		if (CommonsJcrUtils.checkNotEmptyString(tmpStr))
			text.setText(tmpStr);
		return tmpStr;
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
	 * a form, a property NameAlso manages its enable state and set a default
	 * message if corresponding Text value is empty
	 */
	public static void refreshFormComboValue(Combo combo, Node entity,
			String propName) {
		String currValue = CommonsJcrUtils.get(entity, propName);
		if (CommonsJcrUtils.checkNotEmptyString(currValue))
			combo.select(combo.indexOf(currValue));
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
			}
		} catch (RepositoryException re) {
			throw new PeopleException("unable get boolean value for property "
					+ propName);
		}
		return tmp;
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
				if (JcrUiUtils.setJcrProperty(node, propName,
						PropertyType.STRING, text.getText()))
					part.markDirty();
			}
		});
	}

	/**
	 * Shortcut to provide a gridlayout with no margin and no spacing (dafault
	 * are normally 5 px)
	 */
	public static GridLayout gridLayoutNoBorder() {
		return gridLayoutNoBorder(1);
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
				if (JcrUiUtils.setJcrProperty(entity, propName, propType,
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
					if (JcrUiUtils.setJcrProperty(entity, propName, propType,
							selectedCategory))
						part.markDirty();
				}

			}
		});
	}

	/**
	 * Shortcut to add a Text Modifylistener that updates a LONG property on a
	 * Node. Checks the input validity while the user is typing
	 */
	public static void addNbOnlyTxtModifyListener(final AbstractFormPart part,
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

	/**
	 * Shortcut to provide a gridlayout with no margin and no spacing (default
	 * are normally 5 px) with the given column number (equals width is false).
	 */
	public static GridLayout gridLayoutNoBorder(int nbOfCol) {
		GridLayout gl = new GridLayout(nbOfCol, false);
		gl.marginWidth = gl.marginHeight = gl.horizontalSpacing = gl.verticalSpacing = 0;
		return gl;
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

}
