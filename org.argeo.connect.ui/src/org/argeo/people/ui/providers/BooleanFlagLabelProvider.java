package org.argeo.people.ui.providers;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.argeo.people.PeopleException;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Manage display of a boolean property, given an image for true and one for
 * false
 */
public class BooleanFlagLabelProvider extends ColumnLabelProvider {
	private static final long serialVersionUID = 1L;

	private final String propertyName;
	private final Image imgTrue;
	private final Image imgFalse;

	public BooleanFlagLabelProvider(String propertyName, Image imgTrue,
			Image imgFalse) {
		this.propertyName = propertyName;
		this.imgTrue = imgTrue;
		this.imgFalse = imgFalse;
	}

	@Override
	public String getText(Object element) {
		return null;
	}

	@Override
	public Image getImage(Object element) {
		boolean isPrimary = false;
		try {
			Node currNode = ((Node) element);
			if (currNode.hasProperty(propertyName)
					&& currNode.getProperty(propertyName).getValue().getType() == PropertyType.BOOLEAN)
				isPrimary = currNode.getProperty(propertyName).getBoolean();
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to get " + propertyName
					+ " value for node " + element, e);
		}

		if (isPrimary) {
			return imgTrue;
		} else {
			return imgFalse;
		}
	}
}