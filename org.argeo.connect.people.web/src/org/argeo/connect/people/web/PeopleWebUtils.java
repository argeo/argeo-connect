package org.argeo.connect.people.web;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;

/** Helper methods for People Web UI */
public class PeopleWebUtils {
	/**
	 * Cleans a String by replacing any '&' by its HTML encoding '&#38;' to
	 * avoid <code>SAXParseException</code> while rendering HTML with RWT
	 */
	public static String replaceAmpersand(String value) {
		value = value.replaceAll("&(?![#a-zA-Z0-9]+;)", "&#38;");
		return value;
	}

	/** Centralizes management of no-break space character */
	public static String NB_SPACE= "&#160;";
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
}
