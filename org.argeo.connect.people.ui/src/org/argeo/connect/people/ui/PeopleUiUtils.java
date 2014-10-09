package org.argeo.connect.people.ui;

import javax.jcr.Node;

import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
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

	/* LENGHT AND DURATION MANAGEMENT */
	/** returns corresponding hours for a HH:MM:SS representation */
	public static long getHoursFromLength(long lengthInSeconds) {
		return (lengthInSeconds / (60 * 60)) % 60;
	}

	/** returns corresponding minutes for a HH:MM:SS representation */
	public static long getMinutesFromLength(long lengthInSeconds) {
		return (lengthInSeconds / 60) % 60;
	}

	/** returns corresponding seconds for a HH:MM:SS representation */
	public static long getSecondsFromLength(long lengthInSeconds) {
		return lengthInSeconds % 60;
	}

	/** returns the length in second of a HH:MM:SS representation */
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
