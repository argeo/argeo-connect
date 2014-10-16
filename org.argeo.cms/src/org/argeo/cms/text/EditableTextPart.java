package org.argeo.cms.text;

import javax.jcr.Property;

import org.argeo.cms.CmsException;
import org.argeo.cms.widgets.ScrolledPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

abstract class EditableTextPart extends Composite {
	private static final long serialVersionUID = -6296987108397319506L;

	private Boolean selected = false;

	public EditableTextPart(Composite parent, int style) {
		super(parent, style);
	}

	public abstract void startEditing();

	public abstract void stopEditing();

	public abstract Control getControl();

	public abstract void setStyle(String style);

	public Boolean isSelected() {
		return selected;
	}

	public void selected(Boolean selected) {
		if (!this.selected && !selected)
			throw new CmsException("Was not selected");
		this.selected = selected;
	}

	public String getNodePath() {
		return (String) getData(Property.JCR_PATH);
	}

	public String toString() {
		return getClass().getName() + "#" + getNodePath();
	}

}
