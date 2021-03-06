package org.argeo.connect.ui.parts;

import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Generic dialog with 2 texts fields to ask the end user for a title and a
 * description
 */
public class AskTitleDescriptionDialog extends TrayDialog {
	private static final long serialVersionUID = -1965274702044063832L;
	// The various field
	private Text titleTxt;
	private Text descTxt;

	private String title;
	private String desc;

	private final String windowTitle;

	/**
	 * @param parentShell
	 * @param windowTitle
	 */
	public AskTitleDescriptionDialog(Shell parentShell, String windowTitle) {
		super(parentShell);
		this.windowTitle = windowTitle;
	}

	public AskTitleDescriptionDialog(Shell parentShell, String windowTitle, String defaultTitle, String defaultDesc) {
		super(parentShell);
		this.windowTitle = windowTitle;
		title = defaultTitle;
		desc = defaultDesc;
	}

	protected Control createDialogArea(Composite parent) {
		// MAIN LAYOUT
		Composite dialogarea = (Composite) super.createDialogArea(parent);
		dialogarea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		dialogarea.setLayout(new GridLayout(2, false));

		// Title
		titleTxt = createLT(dialogarea, "Title");
		if (EclipseUiUtils.notEmpty(title))
			titleTxt.setText(title);
		titleTxt.setMessage("Please provide a title");
		titleTxt.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void modifyText(ModifyEvent event) {
				title = titleTxt.getText();
			}
		});

		// Description
		Label lbl = new Label(dialogarea, SWT.RIGHT);
		lbl.setText("Description");
		lbl.setFont(EclipseUiUtils.getBoldFont(dialogarea));
		lbl.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
		descTxt = new Text(dialogarea, SWT.MULTI | SWT.WRAP | SWT.BORDER);
		descTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		if (EclipseUiUtils.notEmpty(desc))
			descTxt.setText(desc);
		descTxt.setMessage("... and optionally a description");
		descTxt.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void modifyText(ModifyEvent event) {
				desc = descTxt.getText();
			}
		});
		parent.pack();
		return dialogarea;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return desc;
	}

	/**
	 * Override this to perform the real addition
	 * 
	 * @return
	 */
	protected boolean performFinish() {
		// Sanity check
		String msg = null;

		if (EclipseUiUtils.isEmpty(title) || title.length() < 2)
			msg = "Please enter a title that is at list two (2) valid charaters long.";
		if (msg != null) {
			MessageDialog.openError(getShell(), "Non valid information", msg);
			return false;
		}

		return true;
	}

	// This dialog life cycle
	@Override
	protected void okPressed() {
		if (performFinish())
			super.okPressed();
	}

	protected Point getInitialSize() {
		return new Point(400, 250);
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(windowTitle);
	}

	// Specific widgets management
	/** Creates label and text. */
	protected Text createLT(Composite parent, String label) {
		Label lbl = new Label(parent, SWT.RIGHT);
		lbl.setText(label);
		lbl.setFont(EclipseUiUtils.getBoldFont(parent));
		lbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		Text text = new Text(parent, SWT.SINGLE | SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		return text;
	}
}
