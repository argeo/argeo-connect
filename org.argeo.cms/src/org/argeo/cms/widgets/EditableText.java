package org.argeo.cms.widgets;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.cms.CmsConstants;
import org.argeo.cms.CmsUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/** Editable text part displaying styled text. */
public class EditableText extends StyledControl {
	private static final long serialVersionUID = -6372283442330912755L;

	public EditableText(Composite parent, int swtStyle) {
		super(parent, swtStyle);
	}

	public EditableText(Composite parent, int style, Node node)
			throws RepositoryException {
		this(parent, style, node, false);
	}

	public EditableText(Composite parent, int style, Node node,
			boolean cacheImmediately) throws RepositoryException {
		super(parent, style, node, cacheImmediately);
	}

	@Override
	protected Control createControl(Composite box, String style) {
		if (isEditing())
			return createText(box, style);
		else
			return createLabel(box, style);
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

	protected Text createText(Composite box, String style) {
		final Text text = new Text(box, getStyle() | SWT.MULTI | SWT.WRAP);
		GridData textLayoutData = CmsUtils.fillWidth();
		// textLayoutData.minimumHeight = height;
		text.setLayoutData(textLayoutData);
		text.setData(STYLE, style);
		text.setFocus();
		return text;
	}

	// public void startEditing() {
	// int height = child.getSize().y;
	// String style = (String) child.getData(STYLE);
	// clear(false);
	// child = createText(style, height);
	// }
	//
	// public void stopEditing() {
	// String style = (String) child.getData(STYLE);
	// clear(false);
	// child = createLabel(style);
	// }

	public Text getAsText() {
		return (Text) getControl();
	}

	public Label getAsLabel() {
		return (Label) getControl();
	}

}
