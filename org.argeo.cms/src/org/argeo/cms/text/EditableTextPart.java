package org.argeo.cms.text;

import org.eclipse.swt.widgets.Control;

public interface EditableTextPart {
	public void startEditing();

	public void stopEditing();

	public Control getControl();

	// public void updateContent() throws RepositoryException;
	//
	// public void save(Item item);

	// public Section getSection();

	// public abstract void setStyle(String style);
}
