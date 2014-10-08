package org.argeo.connect.people.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

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
	 * Shortcut to create a {@link GridData} with default parameters SWT.FILL,
	 * SWT.FILL, true, true.
	 */
	public static GridData fillGridData() {
		return new GridData(SWT.FILL, SWT.FILL, true, true);
	}

	/**
	 * Shortcut to create a {@link GridData} with default parameters SWT.FILL,
	 * SWT.CENTER, true, false.
	 */
	public static GridData horizontalFillData() {
		return new GridData(SWT.FILL, SWT.CENTER, true, false);
	}

	/**
	 * Shortcut to create a {@link GridData} with default parameters SWT.FILL,
	 * SWT.CENTER, true, false, horizontalSpan, 1.
	 */
	public static GridData horizontalFillData(int horizontalSpan) {
		return new GridData(SWT.FILL, SWT.CENTER, true, false, horizontalSpan,
				1);
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

	/**
	 * Dispose all control children of this composite. Useful for violent
	 * refreshes.
	 * 
	 * @param parent
	 */
	public static void disposeAllChildren(Composite parent) {
		// We redraw the full control at each refresh, might be a more
		// efficient way to do
		Control[] oldChildren = parent.getChildren();
		for (Control child : oldChildren)
			child.dispose();
	}
}
