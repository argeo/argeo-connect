package org.argeo.connect.ui;

import javax.jcr.Node;

import org.argeo.cms.i18n.Localized;
import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.util.ConnectUtils;
//import org.argeo.connect.workbench.AppWorkbenchService;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

/** Helper methods for various Connect UIs */
public class ConnectUiUtils {
	/** simply add an empty line in a grid data to give some air */
	public static Label addEmptyLine(Composite parent, int height, int colSpan) {
		// Empty line that act as a padding
		Label emptyLbl = new Label(parent, SWT.NONE);
		emptyLbl.setText("");
		GridData gd = EclipseUiUtils.fillWidth(colSpan);
		gd.heightHint = height;
		emptyLbl.setLayoutData(gd);
		return emptyLbl;
	}

	/**
	 * Calls <code>ConnectJcrUtils.get(Node node, String propName)</code> method and
	 * replace any '&' by its html encoding '&amp;' to avoid
	 * <code>IllegalArgumentException</code> while rendering html read only snippets
	 */
	public static String getRwtCompliantString(Node node, String propName) {
		String value = ConnectJcrUtils.get(node, propName);
		value = ConnectUtils.replaceAmpersand(value);
		return value;
	}

	/**
	 * Dispose all control children of this composite. Useful for violent refreshes.
	 * 
	 * @param parent
	 */
	// public static void disposeAllChildren(Composite parent) {
	// // We redraw the full control at each refresh, might be a more
	// // efficient way to do
	// Control[] oldChildren = parent.getChildren();
	// for (Control child : oldChildren)
	// child.dispose();
	// }

	public static boolean isNumbers(String content) {
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
	 * Shortcut to create a {@link GridLayout} with the given column number with no
	 * margin and no spacing (default are normally 5 px). makeColumnsEqualWidth
	 * parameter is set to false.
	 */
	public static GridLayout noSpaceGridLayout(int nbOfCol) {
		GridLayout gl = new GridLayout(nbOfCol, false);
		gl.marginWidth = gl.marginHeight = gl.horizontalSpacing = gl.verticalSpacing = 0;
		return gl;
	}

	/**
	 * Shortcut to refresh the value of a <code>Text</code> given a Node and a
	 * property Name
	 */
	public static String refreshTextWidgetValue(Text text, Node entity, String propName) {
		String tmpStr = ConnectJcrUtils.get(entity, propName);
		if (EclipseUiUtils.notEmpty(tmpStr))
			text.setText(tmpStr);
		return tmpStr;
	}

	/**
	 * Cleans a String by replacing any '&' by its HTML encoding '&nbsp;' to insure
	 * they are displayed in SWT.Link controls
	 */
	public static String replaceAmpersandforSWTLink(String value) {
		value = value.replaceAll("&", "&&");
		return value;
	}

	/**
	 * Creates a basic right-aligned vertical-centered bold label with no specific
	 * toolkit.
	 */
	public static Label createBoldLabel(Composite parent, String value) {
		Label label = new Label(parent, SWT.LEAD);
		label.setText(" " + value);
		label.setFont(EclipseUiUtils.getBoldFont(parent));
		label.setLayoutData(new GridData(SWT.LEAD, SWT.CENTER, false, false));
		return label;
	}

	public static Label createBoldLabel(Composite parent, Localized localized) {
		Label label = new Label(parent, SWT.LEAD);
		label.setText(localized.lead());
		label.setFont(EclipseUiUtils.getBoldFont(parent));
		label.setLayoutData(new GridData(SWT.LEAD, SWT.CENTER, false, false));
		return label;
	}

	/**
	 * Creates a basic right aligned bold label with no specific toolkit. precise
	 * vertical alignment
	 */
	public static Label createBoldLabel(Composite parent, String value, int verticalAlign) {
		Label label = new Label(parent, SWT.LEAD);
		label.setText(" " + value);
		label.setFont(EclipseUiUtils.getBoldFont(parent));
		label.setLayoutData(new GridData(SWT.LEAD, verticalAlign, false, false));
		return label;
	}

	/**
	 * Shortcut to create a delete button that will be used in composites that
	 * display a multi value property in tag-like manner
	 */
	public static Button createDeleteButton(Composite parent) {
		Button button = new Button(parent, SWT.FLAT | SWT.PUSH);
		CmsUtils.style(button, ConnectUiStyles.SMALL_DELETE_BTN);
		RowData rd = new RowData();
		rd.height = 8;
		rd.width = 8;
		button.setLayoutData(rd);
		return button;
	}

	/** Creates a text widget with RowData already set */
	public static Text createRDText(Object toolkit, Composite parent, String msg, String toolTip, int width) {
		Text text = new Text(parent, SWT.BORDER | SWT.SINGLE | SWT.LEFT);
		text.setText("");
		text.setMessage(msg);
		text.setToolTipText(toolTip);
		text.setLayoutData(width == 0 ? new RowData() : new RowData(width, SWT.DEFAULT));
		return text;
	}

	/**
	 * Creates the basic right aligned bold label that is used in various forms
	 * using a pre-defined toolkit.
	 */
	public static Label createBoldLabel(Object toolkit, Composite parent, String value) {

		// We add a blank space before to workaround the cropping of the word
		// first letter in some OS/Browsers (typically MAC/Firefox 31 )
		Label label = new Label(parent, SWT.RIGHT);
		label.setText(" " + value);
		label.setFont(EclipseUiUtils.getBoldFont(parent));
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
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
	@Deprecated
	public static Text createGDText(Object toolkit, Composite parent, String msg, String toolTip, int width,
			int colSpan) {
		Text text = new Text(parent, SWT.BORDER | SWT.SINGLE | SWT.LEFT);
		text.setText("");
		text.setMessage(msg);
		text.setToolTipText(toolTip);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.widthHint = width;
		gd.horizontalSpan = colSpan;
		text.setLayoutData(gd);
		return text;
	}

	public static void setTableDefaultStyle(Table table, int customItemHeight) {
		table.setLinesVisible(true);
		table.setHeaderVisible(false);
		CmsUtils.setItemHeight(table, customItemHeight);
		CmsUtils.markup(table);
	}

	public static void setTableDefaultStyle(TableViewer viewer, int customItemHeight) {
		setTableDefaultStyle(viewer.getTable(), customItemHeight);
	}

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

}
