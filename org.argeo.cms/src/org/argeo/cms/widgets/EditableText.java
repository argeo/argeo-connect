package org.argeo.cms.widgets;

import org.argeo.cms.CmsConstants;
import org.argeo.cms.CmsNames;
import org.argeo.cms.CmsUtils;
import org.argeo.cms.text.TextStyles;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/** Editable text part displaying styled text. */
public class EditableText extends Composite implements CmsConstants,
		CmsNames, TextStyles {
	private static final long serialVersionUID = -6372283442330912755L;
	private Control child;

	private Composite container;
	private Composite box;

	private MouseListener mouseListener;

	public EditableText(Composite parent, int swtStyle) {
		super(parent, swtStyle);
		setLayout(CmsUtils.noSpaceGridLayout());
		setData(STYLE, TEXT_STYLED_COMPOSITE);
	}

	protected Label createLabel(String style) {
		Label lbl = new Label(box, getStyle() | SWT.WRAP);
		lbl.setLayoutData(CmsUtils.fillWidth());
		lbl.setData(CmsConstants.MARKUP, true);
		lbl.setData(STYLE, style);
		if (mouseListener != null)
			lbl.addMouseListener(mouseListener);
		return lbl;
	}

	protected Text createText(String style, int height) {
		final Text text = new Text(box, getStyle() | SWT.MULTI | SWT.WRAP);
		GridData textLayoutData = CmsUtils.fillWidth();
		textLayoutData.minimumHeight = height;
		text.setLayoutData(textLayoutData);
		text.setData(STYLE, style);
		text.setFocus();
		return text;
	}

	protected Composite createBox(Composite parent) {
		Composite container = new Composite(parent, SWT.INHERIT_DEFAULT);
		container.setLayoutData(CmsUtils.fillWidth());
		container.setLayout(CmsUtils.noSpaceGridLayout());
		return container;
	}

	public void startEditing() {
		int height = child.getSize().y;
		String style = (String) child.getData(STYLE);
		clear(false);
		child = createText(style, height);
	}

	public void stopEditing() {
		String style = (String) child.getData(STYLE);
		clear(false);
		child = createLabel(style);
	}

	public Control getControl() {
		return child;
	}

	public Text getAsText() {
		return (Text) getControl();
	}

	public Label getAsLabel() {
		return (Label) getControl();
	}

	public void setStyle(String style) {
		Object currentStyle = null;
		if (child != null)
			currentStyle = child.getData(STYLE);
		if (currentStyle != null && currentStyle.equals(style))
			return;

		clear(true);
		if (child == null || child instanceof Label)
			child = createLabel(style);
		else if (child instanceof Text)
			child = createText(style, child.getSize().y);
		child.getParent().setData(STYLE, style + "_box");
		child.getParent().getParent().setData(STYLE, style + "_container");
	}

	protected void clear(boolean deep) {
		if (deep) {
			for (Control control : getChildren())
				control.dispose();
			container = createBox(this);
			box = createBox(container);
		} else {
			child.dispose();
		}
	}

	public void setMouseListener(MouseListener mouseListener) {
		if (this.mouseListener != null && child != null)
			child.removeMouseListener(this.mouseListener);
		this.mouseListener = mouseListener;
		if (child != null && this.mouseListener != null)
			child.addMouseListener(mouseListener);
	}
}
