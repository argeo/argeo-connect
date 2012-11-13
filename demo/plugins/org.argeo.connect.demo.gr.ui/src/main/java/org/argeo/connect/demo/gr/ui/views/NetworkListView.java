package org.argeo.connect.demo.gr.ui.views;

import org.argeo.connect.demo.gr.ui.GrMessages;
import org.argeo.connect.demo.gr.ui.GrUiPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

/** List Networks in the current Repository */
public class NetworkListView extends ViewPart {
	// private final static Log log = LogFactory.getLog(NetworkListView.class);

	public static final String ID = GrUiPlugin.PLUGIN_ID + ".networkListView";

	public void createPartControl(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(GrMessages.get().networkListView_msg);
	}

	public void setFocus() {
	}
}
