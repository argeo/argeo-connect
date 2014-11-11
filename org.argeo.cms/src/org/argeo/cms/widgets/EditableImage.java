package org.argeo.cms.widgets;

import org.argeo.cms.CmsConstants;
import org.argeo.cms.CmsUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class EditableImage extends StyledControl {
	private static final long serialVersionUID = -5689145523114022890L;

	public EditableImage(Composite parent, int swtStyle) {
		super(parent, swtStyle);
	}

	@Override
	protected Control createControl(Composite box, String style) {
		if (isEditing()) {
			return createImageChooser(box, style);
		} else {
			return createLabel(box, style);
		}
	}

	protected Label createLabel(Composite box, String style) {
		Label lbl = new Label(box, getStyle() | SWT.WRAP);
		lbl.setLayoutData(CmsUtils.fillWidth());
		lbl.setData(CmsConstants.MARKUP, true);
		lbl.setData(STYLE, style);
		if (mouseListener != null)
			lbl.addMouseListener(mouseListener);
		return lbl;
	}

	protected Control createImageChooser(Composite box, String style) {
		final Text text = new Text(box, getStyle() | SWT.MULTI | SWT.WRAP);
		GridData textLayoutData = CmsUtils.fillWidth();
		// textLayoutData.minimumHeight = height;
		text.setLayoutData(textLayoutData);
		text.setData(STYLE, style);
		text.setFocus();
		return text;
	}

}
