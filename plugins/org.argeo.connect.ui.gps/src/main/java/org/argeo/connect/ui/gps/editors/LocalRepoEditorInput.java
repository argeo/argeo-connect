package org.argeo.connect.ui.gps.editors;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

public class LocalRepoEditorInput implements IEditorInput {

	private final String name;

	/** uuid must not be null */
	public LocalRepoEditorInput(String name) {
		this.name = name;
	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return "Display an editor to review clean data and manage the corresponding repository.";
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		return null;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LocalRepoEditorInput other = (LocalRepoEditorInput) obj;
		if (!name.equals(other.getName()))
			return false;
		return true;
	}
}