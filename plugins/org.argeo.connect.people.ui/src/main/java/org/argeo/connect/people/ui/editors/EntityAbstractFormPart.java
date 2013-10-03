package org.argeo.connect.people.ui.editors;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.AbstractFormPart;

public class EntityAbstractFormPart extends AbstractFormPart {
	final protected Map<String, Composite> contactComposites = new HashMap<String, Composite>();

	public Map<String, Composite> getContactComposites() {
		return contactComposites;
	}
}
