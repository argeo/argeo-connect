package org.argeo.connect.people.ui.editors.utils;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

/** Editor input for generic editor that display info on a given JCR Node */
public class SearchEntityEditorInput implements IEditorInput {

	private final String nodeType;

	/** uid must not be null */
	public SearchEntityEditorInput(String uid) {
		this.nodeType = uid;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		return null;
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
		return nodeType;
	}

	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return "Search view for node type: " + nodeType;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SearchEntityEditorInput other = (SearchEntityEditorInput) obj;
		if (!nodeType.equals(other.getName()))
			return false;
		return true;
	}
}