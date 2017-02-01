package org.argeo.connect.ui.workbench;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

/** Editor input for generic editor that display info on a given JCR Node */
public class NodeEditorInput implements IEditorInput {

	private String uid;
	private String tooltip = "Display and edit current node informations";

	/** uid must not be null */
	public NodeEditorInput(String uid) {
		this.uid = uid;
	}

	/**
	 * Enable change on the "main" node for this editor after creation *
	 * 
	 * @param uid
	 *            must not be null
	 */
	public void setUid(String uid) {
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
		return tooltip;
	}

	public void setTooltipText(String tooltip) {
		this.tooltip = tooltip;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
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
