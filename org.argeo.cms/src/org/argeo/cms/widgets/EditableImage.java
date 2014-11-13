package org.argeo.cms.widgets;

import static org.argeo.cms.CmsUtils.fillAll;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.cms.CmsUtils;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public abstract class EditableImage extends StyledControl {
	private static final long serialVersionUID = -5689145523114022890L;

	private final static Log log = LogFactory.getLog(EditableImage.class);

	private Point preferredImageSize;
	private Boolean loaded = false;

	public EditableImage(Composite parent, int swtStyle) {
		super(parent, swtStyle);
	}

	@Override
	protected void setContainerLayoutData(Composite composite) {
		composite.setLayoutData(fillAll());
	}

	@Override
	protected void setControlLayoutData(Control control) {
		control.setLayoutData(fillAll());
	}

	public EditableImage(Composite parent, int style, Node node,
			boolean cacheImmediately) throws RepositoryException {
		super(parent, style, node, cacheImmediately);
	}

	/** To be overriden. */
	protected String createImgTag() throws RepositoryException {
		return CmsUtils.noImg(preferredImageSize != null ? preferredImageSize
				: getSize());
	}

	protected Label createLabel(Composite box, String style) {
		Label lbl = new Label(box, getStyle());
		lbl.setLayoutData(CmsUtils.fillWidth());
		CmsUtils.markup(lbl);
		CmsUtils.style(lbl, style);
		if (mouseListener != null)
			lbl.addMouseListener(mouseListener);
		return lbl;
	}

	/** To be overriden. */
	protected synchronized Boolean load(Control control) {
		String imgTag;
		try {
			imgTag = createImgTag();
		} catch (Exception e) {
			// throw new CmsException("Cannot retrieve image", e);
			log.error("Cannot retrieve image", e);
			imgTag = CmsUtils.noImg(preferredImageSize);
			loaded = false;
		}

		if (imgTag == null) {
			loaded = false;
			imgTag = CmsUtils.noImg(preferredImageSize);
		} else
			loaded = true;
		if (control != null) {
			((Label) control).setText(imgTag);
			control.setSize(preferredImageSize != null ? preferredImageSize
					: getSize());
		} else {
			loaded = false;
		}
		getParent().layout();
		return loaded;
	}

	public void setPreferredSize(Point size) {
		this.preferredImageSize = size;
		if (!loaded) {
			setSize(preferredImageSize);
			load((Label) getControl());
		}
	}

	protected Text createText(Composite box, String style) {
		Text text = new Text(box, getStyle());
		CmsUtils.style(text, style);
		return text;
	}

	public Point getPreferredImageSize() {
		return preferredImageSize;
	}

}
