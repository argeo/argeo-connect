package org.argeo.connect.demo.gr.ui.providers;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.argeo.connect.demo.gr.GrException;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;

public class GrTableLabelProvider extends ColumnLabelProvider {

	private List<String> propertiesList;

	private GrJcrPropertyValueProvider propLabelProvider;

	/**
	 * 
	 * @param propertiesList
	 *            an ordered list where {@code propertiesList.get(index)}
	 *            returns a key (generally the corresponding JCR property name)
	 *            that enable the provider to send back a string corresponding
	 *            to the data that must be displayed in the table for the
	 *            specified column index.
	 */
	public GrTableLabelProvider(List<String> propertiesList) {
		super();
		this.propertiesList = propertiesList;
		propLabelProvider = new GrJcrPropertyValueProvider();
	}

	public void update(ViewerCell cell) {
		int colIndex = cell.getColumnIndex();
		Object element = cell.getElement();
		if (element instanceof Node) {
			Node node = (Node) element;
			if (Property.JCR_TITLE.equals(propertiesList.get(colIndex))) {
				cell.setText(GrNodeLabelProvider.getName(node));
				cell.setImage(GrNodeLabelProvider.getIcon(node));
			} else
				cell.setText(getColumnText((Node) element, colIndex));
		} else
			throw new GrException("Label provider for element of type "
					+ element.getClass().getName() + " is not implemented");
	}

	/** Extend to provide specific behaviour */
	public String getColumnText(Node node, int columnIndex) {
		try {
			if (node.hasProperty(propertiesList.get(columnIndex))
					&& !node.getProperty(propertiesList.get(columnIndex))
							.isMultiple()) {
				return propLabelProvider.getFormattedPropertyValue(node,
						propertiesList.get(columnIndex));
			} else
				return "";
		} catch (RepositoryException re) {
			throw new GrException(
					"Unexpected error while getting property values", re);
		}
	}

}