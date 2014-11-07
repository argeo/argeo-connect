package org.argeo.connect.people.rap.editors.utils;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

/**
 * Editor input for editor which are designed to be instantiated only once in
 * the current workbench. The equals method relies on the corresponding editor
 * ID
 */
public class SingletonEditorInput implements IEditorInput {

	private final String id;

	/** the ID of the corresponding editor class. Must not be null */
	public SingletonEditorInput(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public boolean exists() {
		return true;
	}

	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	public String getName() {
		return id;
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		return "Simple editor";
	}

	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		return null;
	}

	public int hashCode() {
		return id.hashCode();
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SingletonEditorInput other = (SingletonEditorInput) obj;
		if (!id.equals(other.getId()))
			return false;
		return true;
	}
}