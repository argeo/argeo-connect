package org.argeo.connect.demo.gr.ui.views;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.demo.gr.ui.GrUiPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

/** List Networks in the current Repository */
public class NetworkListView extends ViewPart {
	private final static Log log = LogFactory.getLog(NetworkListView.class);

	public static final String ID = GrUiPlugin.PLUGIN_ID
			+ ".networkListView";

	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		this.setPartName("Admin only page");
	}

	public void createPartControl(Composite parent) {
		this.setPartName("Admin only page");
		Label label = new Label(parent, SWT.NONE);
		label.setText(GrUiPlugin.getMessage("adminOnlyLbl"));
	}

	public void setFocus() {
	}

}
