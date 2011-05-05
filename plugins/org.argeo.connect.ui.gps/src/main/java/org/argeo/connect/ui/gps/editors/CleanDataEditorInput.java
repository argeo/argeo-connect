package org.argeo.connect.ui.gps.editors;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

public class CleanDataEditorInput implements IEditorInput {

	private final int filterId;

	public CleanDataEditorInput(int filterId) {
		this.filterId = filterId;
	}

	public int getfilterId() {
		return filterId;
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
		return String.valueOf(filterId);
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
		final int prime = 31;
		int result = 1;
		result = prime * result + filterId;
		return result;
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
		if (filterId != other.filterId)
			return false;
		return true;
	}
}
