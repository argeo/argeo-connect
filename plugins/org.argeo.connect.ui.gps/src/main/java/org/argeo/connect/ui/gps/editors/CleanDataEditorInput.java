package org.argeo.connect.ui.gps.editors;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

public class CleanDataEditorInput implements IEditorInput {

	private final String uuid;
	private String name = "new Session";

	/** uuid must not be null */
	public CleanDataEditorInput(String uuid) {
		this.uuid = uuid;
	}

	public String getUuid() {
		return uuid;
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

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return "Display an editor to clean data for the corresponding parameter set";
	}

	@Override
	public Object getAdapter(Class adapter) {
		return null;
	}

	@Override
	public int hashCode() {
		return uuid.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CleanDataEditorInput other = (CleanDataEditorInput) obj;
		if (!uuid.equals(other.getUuid()))
			return false;
		return true;
	}
}
