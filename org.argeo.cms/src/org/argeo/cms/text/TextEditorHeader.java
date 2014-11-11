package org.argeo.cms.text;

import java.util.Observable;
import java.util.Observer;

import org.argeo.cms.CmsEditable;
import org.argeo.cms.CmsUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/** Adds editing capabilities to a page editing text */
public class TextEditorHeader extends Composite implements SelectionListener,
		Observer {
	private static final long serialVersionUID = 4186756396045701253L;

	private final CmsEditable cmsEditable;
	private final Button publish;

	public TextEditorHeader(CmsEditable cmsEditable, Composite parent, int style) {
		super(parent, style);
		this.cmsEditable = cmsEditable;
		if (this.cmsEditable instanceof Observable)
			((Observable) this.cmsEditable).addObserver(this);

		setLayout(CmsUtils.noSpaceGridLayout());
		CmsUtils.style(this, TextStyles.TEXT_EDITOR_HEADER);
		publish = new Button(this, SWT.FLAT | SWT.PUSH);
		publish.setText(getPublishButtonLabel());
		CmsUtils.style(publish, TextStyles.TEXT_EDITOR_HEADER);
		publish.addSelectionListener(this);
	}

	private String getPublishButtonLabel() {
		if (cmsEditable.isEditing())
			return "Publish";
		else
			return "Edit";
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if (e.getSource() == publish) {
			if (cmsEditable.isEditing()) {
				cmsEditable.stopEditing();
			} else {
				cmsEditable.startEditing();
			}
			// publish.setText(getPublishButtonLabel());
		}
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
	}

	@Override
	public void update(Observable o, Object arg) {
		if (o == cmsEditable) {
			publish.setText(getPublishButtonLabel());
		}
	}

}