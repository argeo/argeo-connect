package org.argeo.people.e4.parts;

import org.argeo.connect.ConnectException;
import org.argeo.connect.UserAdminService;
import org.argeo.connect.ui.ConnectEditor;
import org.argeo.connect.ui.util.LazyCTabControl;
import org.argeo.eclipse.ui.Selected;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.osgi.service.useradmin.User;

public class PersonSecurityCTab extends LazyCTabControl {
	private static final long serialVersionUID = 4873664608113834820L;

	private final ConnectEditor editor;
	private UserAdminService userAdminService;

	private Text password1;
	private Text password2;

	private String dn;

	public PersonSecurityCTab(Composite parent, int style, ConnectEditor editor, UserAdminService userAdminService,
			String dn) {
		super(parent, style);
		this.editor = editor;
		this.userAdminService = userAdminService;
		this.dn = dn;
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(3, false));

		password1 = new Text(parent, SWT.PASSWORD | SWT.BORDER);
		password2 = new Text(parent, SWT.PASSWORD | SWT.BORDER);
		Button lbl = new Button(parent, SWT.PUSH);
		lbl.setText("Reset password");
		lbl.addSelectionListener(new Selected() {
			private static final long serialVersionUID = -3679890149990208064L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				String msg = null;
				if (password1.getText().equals(""))
					msg = "Password cannot be empty";
				else if (password1.getText().equals(password2.getText())) {
					char[] newPassword = password1.getText().toCharArray();
					// userAdminWrapper.beginTransactionIfNeeded();
					User user = userAdminService.getUser(dn);
					try {
						userAdminService.getUserTransaction().begin();
						user.getCredentials().put(null, newPassword);
						userAdminService.getUserTransaction().commit();
					} catch (Exception e1) {
						try {
							userAdminService.getUserTransaction().rollback();
						} catch (Exception e2) {
							e2.printStackTrace();
						}
					}
				} else {
					msg = "Passwords are not equals";
				}
				if (msg != null)// TODO make it more user friendly
					throw new ConnectException("Cannot change password: " + msg);
			}
		});
		parent.layout(true, true);
	}

	@Override
	public void refreshPartControl() {
		// TODO Auto-generated method stub

	}

}
