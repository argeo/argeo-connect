package org.argeo.connect.people.ui.editors;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

/** Editor input for generic editor that display info on a given JCR Node */
public class NodeEditorInput implements IEditorInput {

	private final String uid;

	/** uid must not be null */
	public NodeEditorInput(String uid) {
		this.uid = uid;
	}

	public String getUid() {
		return uid;
	}

	public boolean exists() {
		return true;
	}

	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	public String getName() {
		return uid;
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		return "Display and edit current item informations";
	}

	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		return null;
	}

	public int hashCode() {
		return uid.hashCode();
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NodeEditorInput other = (NodeEditorInput) obj;
		if (!uid.equals(other.getUid()))
			return false;
		return true;
	}
}