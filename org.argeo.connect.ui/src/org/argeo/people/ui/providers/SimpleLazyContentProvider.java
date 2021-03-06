package org.argeo.people.ui.providers;

import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

/**
 * Canonical implementation of a LazyContentProvider that manage array of
 * objects
 */
public class SimpleLazyContentProvider implements ILazyContentProvider {
	private static final long serialVersionUID = 1L;
	private TableViewer viewer;

	Object[] elements;

	public SimpleLazyContentProvider(TableViewer viewer) {
		this.viewer = viewer;
	}

	public void dispose() {
	}

	public void setElements(Object[] elements) {
		this.elements = elements;
		viewer.setInput(elements);
		viewer.setItemCount(elements == null ? 0 : elements.length);
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// IMPORTANT: don't forget this: an exception will be thrown if a
		// selected object is not part of the results anymore.
		viewer.setSelection(null);
		elements = (Object[]) newInput;
	}

	public void updateElement(int index) {
		viewer.replace(elements[index], index);
	}
}
