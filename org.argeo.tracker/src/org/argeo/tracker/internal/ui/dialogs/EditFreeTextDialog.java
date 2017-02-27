package org.argeo.tracker.internal.ui.dialogs;

import javax.jcr.Node;

import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/** Edit a free text value */
public class EditFreeTextDialog extends TrayDialog {
	private static final long serialVersionUID = -2526572299370624808L;

	// Business objects
	private final Node node;
	private final String propName;
	private String updatedText;

	// UI objects
	private Text text;
	private final String title;

	public EditFreeTextDialog(Shell parentShell, String title, Node node,
			String propName) {
		super(parentShell);
		this.title = title;
		this.node = node;
		this.propName = propName;
	}

	protected Point getInitialSize() {
		return new Point(400, 400);
	}

	@Override
	protected void okPressed() {
		updatedText = text.getText();
		super.okPressed();
	}

	protected Control createDialogArea(Composite parent) {
		Composite dialogArea = (Composite) super.createDialogArea(parent);
		dialogArea.setLayout(new GridLayout());
		new Label(dialogArea, SWT.WRAP).setText("Please modify the below text");
		text = new Text(dialogArea, SWT.WRAP | SWT.MULTI | SWT.BORDER);
		text.setLayoutData(EclipseUiUtils.fillAll());
		text.setText(ConnectJcrUtils.get(node, propName));
		parent.pack();
		return dialogArea;
	}

	public String getEditedText() {
		return updatedText;
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(title);
	}
}
