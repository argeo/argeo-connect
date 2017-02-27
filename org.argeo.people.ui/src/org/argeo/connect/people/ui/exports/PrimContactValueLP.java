package org.argeo.connect.people.ui.exports;

import static org.argeo.eclipse.ui.EclipseUiUtils.notEmpty;

import javax.jcr.Node;

import org.argeo.connect.people.util.PeopleJcrUtils;
import org.argeo.connect.util.ConnectJcrUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;

/**
 * Enable simple retrieval of primary contact value. Use contact node type as
 * property name
 */
public class PrimContactValueLP extends ColumnLabelProvider {
	private static final long serialVersionUID = 2085668424125329226L;

	private String selectorName;
	private String propertyName;

	public PrimContactValueLP(String selectorName, String propertyName) {
		if (notEmpty(selectorName))
			this.selectorName = selectorName;
		this.propertyName = propertyName;
	}

	@Override
	public String getText(Object element) {
		Node currNode = ConnectJcrUtils.getNodeFromElement(element,
				selectorName);
		return PeopleJcrUtils.getPrimaryContactValue(currNode, propertyName);
	}
}
