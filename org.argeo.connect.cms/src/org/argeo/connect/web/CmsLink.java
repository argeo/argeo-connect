package org.argeo.connect.web;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.jcr.Node;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.JavaScriptExecutor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.osgi.framework.BundleContext;
import org.springframework.osgi.context.BundleContextAware;

/** A link to an internal or external location. */
public class CmsLink implements CmsUiProvider, BundleContextAware {
	private final static Log log = LogFactory.getLog(CmsLink.class);

	private String label;
	private String custom;
	private String target;
	private String image;

	// internal
	private Boolean isUrl = false;

	private BundleContext bundleContext;

	public CmsLink() {
		super();
	}

	public CmsLink(String label, String target) {
		this(label, target, null);
	}

	public CmsLink(String label, String target, String custom) {
		super();
		this.label = label;
		this.target = target;
		this.custom = custom;

		try {
			new URL(target);
			isUrl = true;
		} catch (MalformedURLException e1) {
			isUrl = false;
		}
	}

	@Override
	public Control createUi(final Composite parent, Node context) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(1, true));

		Label link = new Label(comp, SWT.NONE);
		link.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));
		if (custom != null) {
			comp.setData(RWT.CUSTOM_VARIANT, custom);
			link.setData(RWT.CUSTOM_VARIANT, custom);
		} else {
			comp.setData(RWT.CUSTOM_VARIANT, CmsStyles.CMS_LINK);
			link.setData(RWT.CUSTOM_VARIANT, CmsStyles.CMS_LINK);
		}

		if (label != null)
			link.setText(label);
		if (image != null) {
			final Image img = loadImage(parent.getDisplay());
			link.setImage(img);
			link.addDisposeListener(new DListener(img));
		}

		link.setCursor(link.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
		CmsSession cmsSession = (CmsSession) parent.getDisplay().getData(
				CmsSession.KEY);
		link.addMouseListener(new MListener(cmsSession));

		return comp;
	}

	private Image loadImage(Display display) {
		if (bundleContext == null)
			return null;

		URL res = bundleContext.getBundle().getResource(image);

		Image result = null;
		InputStream inputStream = null;
		try {
			inputStream = res.openStream();
			result = new Image(display, inputStream);
			if (log.isDebugEnabled())
				log.debug("Loaded image " + image);
		} catch (Exception e) {
			throw new ArgeoException("Cannot load image " + image, e);
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
		return result;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setCustom(String custom) {
		this.custom = custom;
	}

	public void setTarget(String target) {
		this.target = target;
		try {
			new URL(target);
			isUrl = true;
		} catch (MalformedURLException e1) {
			isUrl = false;
		}
	}

	public void setImage(String image) {
		this.image = image;
	}

	@Override
	public void setBundleContext(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}

	/** Mouse listener */
	private class MListener extends MouseAdapter {
		private static final long serialVersionUID = 3634864186295639792L;
		private final CmsSession cmsSession;

		public MListener(CmsSession cmsSession) {
			super();
			if (cmsSession == null)
				throw new ArgeoException("CMS Session cannot be null");
			this.cmsSession = cmsSession;
		}

		@Override
		public void mouseDown(MouseEvent e) {
			if (isUrl) {
				JavaScriptExecutor executor = RWT.getClient().getService(
						JavaScriptExecutor.class);
				if (executor != null)
					executor.execute("window.location.href = '" + target + "'");
			} else
				cmsSession.navigateTo(target);
		}
	}

	/** Dispose listener */
	private class DListener implements DisposeListener {
		private static final long serialVersionUID = -3808587499269394812L;
		private final Image img;

		public DListener(Image img) {
			super();
			this.img = img;
		}

		@Override
		public void widgetDisposed(DisposeEvent event) {
			img.dispose();
		}

	}
}
