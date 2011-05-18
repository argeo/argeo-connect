package org.argeo.connect.ui.gps.commons;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.ui.forms.AbstractFormPart;

/** Listen to modified fields. */
public class ModifiedFieldListener implements ModifyListener {
	// private static final Log log = LogFactory
	// .getLog(ModifiedFieldListener.class);

	private AbstractFormPart formPart;

	public ModifiedFieldListener(AbstractFormPart generalPart) {
		this.formPart = generalPart;
	}

	@Override
	public void modifyText(ModifyEvent e) {
		formPart.markDirty();
	}
}
