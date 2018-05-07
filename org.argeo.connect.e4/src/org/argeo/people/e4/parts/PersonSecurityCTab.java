package org.argeo.people.e4.parts;

import org.argeo.connect.UserAdminService;
import org.argeo.connect.ui.ConnectEditor;
import org.argeo.connect.ui.util.LazyCTabControl;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class PersonSecurityCTab extends LazyCTabControl {
	private static final long serialVersionUID = 4873664608113834820L;

	private final ConnectEditor editor;
	private UserAdminService userAdminService;

	private Text password1;
	private Text password2;

	public PersonSecurityCTab(Composite parent, int style, ConnectEditor editor, UserAdminService userAdminService) {
		super(parent, style);
		this.editor = editor;
		this.userAdminService = userAdminService;
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout());

		Label lbl = new Label(parent, SWT.BORDER);
		lbl.setText("Reset password");
		password1 = new Text(parent, SWT.PASSWORD | SWT.BORDER);
		password2 = new Text(parent, SWT.PASSWORD | SWT.BORDER);
		parent.layout(true, true);
	}

	@Override
	public void refreshPartControl() {
		// TODO Auto-generated method stub

	}

}
