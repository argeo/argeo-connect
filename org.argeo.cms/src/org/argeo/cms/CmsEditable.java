package org.argeo.cms;

/** API NOT STABLE (yet). */
public interface CmsEditable {
	/** Whether the calling thread can edit */
	public Boolean canEdit();

	public Boolean isEditing();

	public void startEditing();

	public void stopEditing();
}
