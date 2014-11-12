package org.argeo.cms.widgets;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.cms.CmsConstants;
import org.argeo.cms.CmsNames;
import org.argeo.cms.CmsUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/** Editable text part displaying styled text. */
public abstract class StyledControl extends NodeComposite implements
		CmsConstants, CmsNames {
	private static final long serialVersionUID = -6372283442330912755L;
	private Control control;

	private Composite container;
	private Composite box;

	protected MouseListener mouseListener;

	private Boolean editing = Boolean.FALSE;

	public StyledControl(Composite parent, int swtStyle) {
		super(parent, swtStyle);
		setLayout(CmsUtils.noSpaceGridLayout());
	}

	public StyledControl(Composite parent, int style, Node node)
			throws RepositoryException {
		super(parent, style, node);
	}

	public StyledControl(Composite parent, int style, Node node,
			boolean cacheImmediately) throws RepositoryException {
		super(parent, style, node, cacheImmediately);
	}

	protected abstract Control createControl(Composite box, String style);

	protected Composite createBox(Composite parent) {
		Composite container = new Composite(parent, SWT.INHERIT_DEFAULT);
		container.setLayoutData(CmsUtils.fillWidth());
		container.setLayout(CmsUtils.noSpaceGridLayout());
		return container;
	}

	public Control getControl() {
		return control;
	}

	protected synchronized Boolean isEditing() {
		return editing;
	}

	public synchronized void startEditing() {
		assert !isEditing();
		editing = true;
		// int height = child.getSize().y;
		String style = (String) control.getData(STYLE);
		clear(false);
		control = createControl(box, style);
	}

	public synchronized void stopEditing() {
		assert isEditing();
		editing = false;
		String style = (String) control.getData(STYLE);
		clear(false);
		control = createControl(box, style);
	}

	public void setStyle(String style) {
		Object currentStyle = null;
		if (control != null)
			currentStyle = control.getData(STYLE);
		if (currentStyle != null && currentStyle.equals(style))
			return;

		clear(true);
		control = createControl(box, style);
		// if (child == null || child instanceof Label)
		// child = createLabel(style);
		// else if (child instanceof Text)
		// child = createText(style, child.getSize().y);
		control.getParent().setData(STYLE, style + "_box");
		control.getParent().getParent().setData(STYLE, style + "_container");
	}

	protected void clear(boolean deep) {
		if (deep) {
			for (Control control : getChildren())
				control.dispose();
			container = createBox(this);
			box = createBox(container);
		} else {
			control.dispose();
		}
	}

	public void setMouseListener(MouseListener mouseListener) {
		if (this.mouseListener != null && control != null)
			control.removeMouseListener(this.mouseListener);
		this.mouseListener = mouseListener;
		if (control != null && this.mouseListener != null)
			control.addMouseListener(mouseListener);
	}
}
