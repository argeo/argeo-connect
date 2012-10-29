package org.argeo.connect.demo.gr.ui.utils;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.demo.gr.GrTypes;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.ui.forms.AbstractFormPart;


/** Listen to modified selections. */
public class ModifiedSelectionListener implements SelectionListener, GrTypes {
	private final static Log log = LogFactory
			.getLog(ModifiedSelectionListener.class);
	private Map<String, Object> controls;
	private AbstractFormPart formPart;

	public ModifiedSelectionListener(AbstractFormPart generalPart) {
		this.controls = controls;
		this.formPart = generalPart;
	}

	@SuppressWarnings("unchecked")
	public void widgetSelected(SelectionEvent e) {

		if (e.getSource() instanceof Combo) {
			Combo combo = (Combo) e.getSource();
			formPart.markDirty();
		} else if (log.isWarnEnabled())
			log.warn("Unimplemented listener for widget of class: "
					+ e.getSource().getClass());
	}

	public void widgetDefaultSelected(SelectionEvent e) {
		// TODO Auto-generated method stub

	}

}
