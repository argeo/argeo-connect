package org.argeo.connect.workbench.util;

import org.argeo.connect.workbench.NodeEditorInput;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPersistableElement;

/** Editor input for generic editor that display info on a given JCR Node */
public class EntityEditorInput extends NodeEditorInput {

	private String uid;

	// Workaround to enable setting a dynamic tooltip from the linked editor
	// part
	// instance.
	private String tooltip = "Display and edit current item informations";

	/** uid must not be null */
	public EntityEditorInput(String uid) {
		super(uid);
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

	@SuppressWarnings({ "rawtypes" })
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
		EntityEditorInput other = (EntityEditorInput) obj;
		if (!uid.equals(other.getUid()))
			return false;
		return true;
	}
}