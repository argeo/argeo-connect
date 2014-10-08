package org.argeo.connect.people.rap.editors.utils;

import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

/**
 * Editor input for an editor that display a filtered list of nodes that have a
 * given JCR Node Type under a given path. A name might be given to display in
 * the corresponding tab of the editor
 */
public class SearchNodeEditorInput implements IEditorInput {

	private final String nodeType;
	private final String basePath;
	private final String name;

	/** Node type cannot be null */
	public SearchNodeEditorInput(String nodeType) {
		this.nodeType = nodeType;
		this.name = nodeType;
		this.basePath = "/";
	}

	/** Node type cannot be null */
	public SearchNodeEditorInput(String nodeType, String basePath, String name) {
		this.nodeType = nodeType;
		if (CommonsJcrUtils.checkNotEmptyString(basePath))
			this.basePath = basePath;
		else
			this.basePath = "/";
		if (CommonsJcrUtils.checkNotEmptyString(name))
			this.name = name;
		else
			this.name = nodeType;
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
		return name;
	}

	public String getBasePath() {
		return basePath;
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
		return "Search among all " + name
				+ " defined in the current repository";
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SearchNodeEditorInput other = (SearchNodeEditorInput) obj;
		if (!nodeType.equals(other.getNodeType())
				|| !name.equals(other.getName())
				|| !basePath.equals(other.getBasePath()))
			return false;

		return true;
	}
}