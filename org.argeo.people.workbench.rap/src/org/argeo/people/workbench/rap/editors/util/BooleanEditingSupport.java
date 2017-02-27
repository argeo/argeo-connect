package org.argeo.people.workbench.rap.editors.util;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.argeo.people.PeopleException;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;

/** Enable flag like editing support for various BOOLEAN properties */
public abstract class BooleanEditingSupport extends EditingSupport {
	private static final long serialVersionUID = 1L;
	private final TableViewer viewer;
	private final String propertyName;

	public BooleanEditingSupport(TableViewer viewer, String propertyName) {
		super(viewer);
		this.viewer = viewer;
		this.propertyName = propertyName;
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		return new CheckboxCellEditor(viewer.getTable());
	}

	/** Define this depending on the context */
	protected abstract boolean canEdit(Object element);

	// protected boolean canEdit(Object element) {
	// return ConnectJcrUtils.isNodeCheckedOutByMe(film);
	// }

	@Override
	protected Object getValue(Object element) {
		// check if current row display a primary title
		try {
			Node currNode = (Node) element;
			if (currNode.hasProperty(propertyName)
					&& currNode.getProperty(propertyName).getValue().getType() == PropertyType.BOOLEAN)
				return currNode.getProperty(propertyName).getBoolean();
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to get " + propertyName
					+ " value for node " + element, e);
		}
		return false;
	}
}