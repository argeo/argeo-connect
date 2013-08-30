package org.argeo.connect.people.ui.editors;

import java.util.Map;

import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.AbstractFormPart;

public class EntityAbstractFormPart extends AbstractFormPart {
	protected Map<String, Text> controls = null;

	public Map<String, Text> getTextControls() {
		return controls;
	}

	public void setTextControls(Map<String, Text> controls) {
		this.controls = controls;
	}

	// @Override
	// public boolean isStale() {
	// // Always consider form part as stale to ease enable state management
	// // TODO find a cleaner way
	// return true;
	// }
}
