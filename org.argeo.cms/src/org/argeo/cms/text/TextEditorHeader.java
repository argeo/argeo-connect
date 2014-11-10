package org.argeo.cms.text;

import org.argeo.cms.CmsEditable;
import org.argeo.cms.CmsUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/** Adds editing capabilities to a page editing text */
public class TextEditorHeader extends Composite implements SelectionListener {
	private static final long serialVersionUID = 4186756396045701253L;

	private final CmsEditable cmsEditable;
	private final Button publish;

	public TextEditorHeader(CmsEditable cmsEditable, Composite parent, int style) {
		super(parent, style);
		this.cmsEditable = cmsEditable;
		setLayout(CmsUtils.noSpaceGridLayout());
		CmsUtils.style(this, TextStyles.TEXT_EDITOR_HEADER);
		publish = new Button(this, SWT.FLAT);
		publish.setText(getPublishButtonLabel());
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
			publish.setText(getPublishButtonLabel());
		}
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
	}

}