package org.argeo.cms.text;

import org.eclipse.swt.widgets.Control;

public interface EditableTextPart {
	public void startEditing();

	public void stopEditing();

	public Control getControl();
}
