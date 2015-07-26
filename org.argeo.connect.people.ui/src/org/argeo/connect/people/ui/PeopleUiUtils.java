package org.argeo.connect.people.ui;

import javax.jcr.Node;

import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/** Helper methods for various People UIs */
public class PeopleUiUtils {
	// private final static Log log = LogFactory.getLog(PeopleUiUtils.class);

	/**
	 * Cleans a String by replacing any '&' by its HTML encoding '&#38;' to
	 * avoid <code>SAXParseException</code> while rendering HTML with RWT
	 */
	public static String replaceAmpersand(String value) {
		value = value.replaceAll("&(?![#a-zA-Z0-9]+;)",
				PeopleUiConstants.AMPERSAND);
		return value;
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
	 * Dispose all control children of this composite. Useful for violent
	 * refreshes.
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
	 * Calls <code>CommonsJcrUtils.get(Node node, String propName)</code> method
	 * and replace any '&' by its html encoding '&amp;' to avoid
	 * <code>IllegalArgumentException</code> while rendering html read only
	 * snippets
	 */
	public static String getRwtCompliantString(Node node, String propName) {
		String value = CommonsJcrUtils.get(node, propName);
		value = replaceAmpersand(value);
		return value;
	}
}