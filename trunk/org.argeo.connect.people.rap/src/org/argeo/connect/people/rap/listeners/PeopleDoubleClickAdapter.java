package org.argeo.connect.people.rap.listeners;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Utility double click listener for a people viewer that displays JCR Rows to
 * easily define which action to process on a double click event.
 */
public abstract class PeopleDoubleClickAdapter implements IDoubleClickListener {

	public void doubleClick(DoubleClickEvent event) {
		if (event.getSelection() == null || event.getSelection().isEmpty())
			return;
		Object obj = ((IStructuredSelection) event.getSelection())
				.getFirstElement();
		processDoubleClick(obj);
	}

	protected abstract void processDoubleClick(Object obj);
}