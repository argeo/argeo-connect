package org.argeo.connect.demo.gr.ui.editors;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

public class SiteEditorInput implements IEditorInput {

	private final String uid;
	private String name = "new Site";

	/** uid must not be null */
	public SiteEditorInput(String uid) {
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
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		return "Display an editor for the choosen site";
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
		SiteEditorInput other = (SiteEditorInput) obj;
		if (!uid.equals(other.getUid()))
			return false;
		return true;
	}
}
