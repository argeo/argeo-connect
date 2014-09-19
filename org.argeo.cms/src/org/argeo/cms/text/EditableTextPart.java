package org.argeo.cms.text;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

abstract class EditableTextPart extends Composite {
	private static final long serialVersionUID = -6296987108397319506L;

	public EditableTextPart(Composite parent, int style) {
		super(parent, style);
	}

	protected abstract void startEditing();

	protected abstract void stopEditing();

	protected void clear() {
		for (Control control : getChildren())
			control.dispose();
	}

}
