package org.argeo.cms.text;

import javax.jcr.Property;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

abstract class EditableTextPart extends Composite {
	private static final long serialVersionUID = -6296987108397319506L;

	public EditableTextPart(Composite parent, int style) {
		super(parent, style);
	}

	public abstract void startEditing();

	public abstract void stopEditing();

	public abstract Control getControl();

	public abstract void setStyle(String style);

	public String getNodePath() {
		return (String) getData(Property.JCR_PATH);
	}
}
