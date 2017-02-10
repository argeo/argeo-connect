package org.argeo.connect.people.ui.exports;

import static org.argeo.eclipse.ui.EclipseUiUtils.notEmpty;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.util.DateTimeUtils;
import org.argeo.eclipse.ui.jcr.lists.SimpleJcrNodeLabelProvider;

/**
 * Provides a length given a row that contains a selector that has a length in
 * seconds LONG property and format it according to one of the pre-defined
 * format
 */
public class LengthLP extends SimpleJcrNodeLabelProvider {
	private static final long serialVersionUID = 1L;

	private String selectorName;
	private String propertyName;

	public final static String IN_MINUTES = "MM";
	public final static String HH_MM_SS = "HHMMSS";
	private String format;

	public LengthLP(String selectorName, String propertyName, String format) {
		super(propertyName);
		if (notEmpty(selectorName))
			this.selectorName = selectorName;
		this.propertyName = propertyName;
		this.format = format;
	}

	@Override
	public String getText(Object element) {
		try {
			Node currNode = ConnectJcrUtils.getNodeFromElement(element, selectorName);
			String result = "";
			if (currNode != null && currNode.hasProperty(propertyName)) {
				long length = currNode.getProperty(propertyName).getLong();
				if (HH_MM_SS.equals(format)) {
					result = String.format("%d:%02d:%02d", length / 3600, (length % 3600) / 60, (length % 60));
				} else if (IN_MINUTES.equals(format))
					result = "" + DateTimeUtils.roundSecondsToMinutes(length);
			}
			return result;
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to get selector " + selectorName + " from row " + element, re);
		}
	}
}
