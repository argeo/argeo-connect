package org.argeo.connect.files.web;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.connect.cms.CmsUiProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Displays a form to send a message
 * 
 * TODO internationalise, sanity check, send message.
 * 
 */
public class SimpleContactPage implements CmsUiProvider {
	private String title = "Contact";
	private String description = "You can use this form to request an invitation to "
			+ "the Argeo infrastructure. This will allow you "
			+ "to add comments, participate in forums and gain "
			+ "access to more features. Please tell us briefly why "
			+ "you want to join and your request will be reviewed "
			+ "by a real person.";
	private Text nameTxt;
	private Text addressTxt;
	private Text subjectTxt;
	private Text messageTxt;

	@Override
	public Control createUi(Composite parent2, Node context)
			throws RepositoryException {
		Composite parent = new Composite(parent2, SWT.NONE);
		parent.setLayout(new GridLayout(1, true));

		createLabel(parent, title, FormStyles.FORM_TITLE);
		createLabel(parent, description, FormStyles.FORM_DESCRIPTION);
		createLabel(parent, "Your name", FormStyles.FORM_LABEL);
		nameTxt = createText(parent, "John Herry", "Some help message",
				FormStyles.FORM_TEXT);
		createLabel(parent, "Your e-mail address", FormStyles.FORM_LABEL);
		addressTxt = createText(parent, "john@herry.de", "Some help message",
				FormStyles.FORM_TEXT);
		createLabel(parent, "Subject", FormStyles.FORM_LABEL);
		subjectTxt = createText(parent, "A quick extract", "Some help message",
				FormStyles.FORM_TEXT);
		createLabel(parent, "Message", FormStyles.FORM_LABEL);
		messageTxt = createTextArea(parent, "What you want to say",
				"Some help message", FormStyles.FORM_TEXT_AREA);

		Button validBtn = new Button(parent, SWT.PUSH);
		validBtn.setText("Send message");
		validBtn.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
		validBtn.setData(RWT.CUSTOM_VARIANT, FormStyles.FORM_BUTTON);

		validBtn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 4281219663091503680L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				String message = "Message sent to " + nameTxt.getText() + "( "
						+ addressTxt.getText() + " )\n" + "Title: "
						+ subjectTxt.getText() + "\nMessage:"
						+ messageTxt.getText();
				MessageDialog.openInformation(nameTxt.getShell(),
						"Message sent", message);
			}
		});

		return parent;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	private Composite createLabel(Composite parent, String value, String custom) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(1, true));
		comp.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
		comp.setData(RWT.CUSTOM_VARIANT, custom);

		Label label = new Label(comp, SWT.WRAP);
		label.setText(value);
		label.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, false));
		label.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
		label.setData(RWT.CUSTOM_VARIANT, custom);
		return comp;
	}

	private Text createText(Composite parent, String message, String tooltip,
			String custom) {
		Text text = new Text(parent, SWT.BORDER);
		if (message != null)
			text.setMessage(message);
		text.setToolTipText(tooltip);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		text.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
		text.setData(RWT.CUSTOM_VARIANT, custom);
		return text;
	}

	private Text createTextArea(Composite parent, String message,
			String tooltip, String custom) {
		Text text = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.WRAP);
		if (message != null)
			text.setMessage(message);
		text.setToolTipText(tooltip);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		text.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
		text.setData(RWT.CUSTOM_VARIANT, custom);
		return text;
	}

}
