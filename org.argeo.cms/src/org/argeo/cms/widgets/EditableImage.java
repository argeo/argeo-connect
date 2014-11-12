package org.argeo.cms.widgets;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.cms.CmsConstants;
import org.argeo.cms.CmsUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public abstract class EditableImage extends StyledControl {
	private static final long serialVersionUID = -5689145523114022890L;

	private final static Log log = LogFactory.getLog(EditableImage.class);

	private Point preferredSize;
	private Boolean loaded = false;

	public EditableImage(Composite parent, int swtStyle) {
		super(parent, swtStyle);
	}

	public EditableImage(Composite parent, int style, Node node,
			boolean cacheImmediately) throws RepositoryException {
		super(parent, style, node, cacheImmediately);
	}

	protected abstract String createImgTag() throws RepositoryException;

	protected Label createLabel(Composite box, String style) {
		Label lbl = new Label(box, getStyle() | SWT.WRAP);
		lbl.setLayoutData(CmsUtils.fillWidth());
		lbl.setData(CmsConstants.MARKUP, true);
		CmsUtils.style(lbl, style);
		load(lbl);
		getParent().layout();
		if (mouseListener != null)
			lbl.addMouseListener(mouseListener);
		return lbl;
	}

	protected synchronized Boolean load(Label lbl) {
		String imgTag;
		try {
			imgTag = createImgTag();
		} catch (Exception e) {
			// throw new CmsException("Cannot retrieve image", e);
			log.error("Cannot retrieve image", e);
			imgTag = CmsUtils.noImg(preferredSize);
			loaded = false;
		}

		if (imgTag == null) {
			loaded = false;
			imgTag = CmsUtils.noImg(preferredSize);
		} else
			loaded = true;
		if (lbl != null)
			lbl.setText(imgTag);
		else
			loaded = false;
		getParent().layout();
		return loaded;
	}

	public void setPreferredSize(Point size) {
		this.preferredSize = size;
		if (!loaded) {
			setSize(preferredSize);
			load((Label) getControl());
		}
	}

	protected Text createText(Composite box, String style) {
		Text text = new Text(box, getStyle());
		CmsUtils.style(text, style);
		return text;
	}

	public Point getPreferredSize() {
		return preferredSize;
	}

}
