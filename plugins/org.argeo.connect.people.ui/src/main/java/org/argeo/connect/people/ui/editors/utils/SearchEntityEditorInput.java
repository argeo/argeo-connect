package org.argeo.connect.people.ui.editors.utils;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

/**
 * Editor input for an editor that display a filtered list of nodes that have a
 * given JCR Node Type
 */
public class SearchEntityEditorInput implements IEditorInput {

	private final String nodeType;

	/** Node type cannot be null */
	public SearchEntityEditorInput(String nodeType) {
		this.nodeType = nodeType;
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

	public String getNodeType() {
		return nodeType;
	}

	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return "Search among all " + nodeType
				+ " defined in the current repository";
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SearchEntityEditorInput other = (SearchEntityEditorInput) obj;
		if (!nodeType.equals(other.getNodeType()))
			return false;
		return true;
	}
}