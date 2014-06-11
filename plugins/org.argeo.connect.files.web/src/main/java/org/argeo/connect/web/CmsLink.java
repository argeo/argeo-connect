package org.argeo.connect.web;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.jcr.Node;

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

public class CmsLink implements CmsUiProvider {
	private String label;
	private String custom;
	private String target;
	private String image;

	public CmsLink() {
		super();
	}

	public CmsLink(String label, String target) {
		super();
		this.label = label;
		this.target = target;
	}

	public CmsLink(String label, String target, String custom) {
		super();
		this.label = label;
		this.target = target;
		this.custom = custom;
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
			link.addDisposeListener(new DisposeListener() {

				@Override
				public void widgetDisposed(DisposeEvent event) {
					img.dispose();
				}
			});
		}

		link.setCursor(link.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
		link.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				try {
					URL url = new URL(target);
					JavaScriptExecutor executor = RWT.getClient().getService(
							JavaScriptExecutor.class);
					if (executor != null) {
						executor.execute("window.location.href = '"
								+ url.toString() + "'");
					}
				} catch (MalformedURLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				AbstractCmsEntryPoint entryPoint = (AbstractCmsEntryPoint) parent
						.getDisplay().getData(CmsSession.KEY);
				entryPoint.navigateTo(target);
			}
		});

		return comp;
	}

	private Image loadImage(Display display) {
		// FIXME Deal with multiple bundles
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream(image);
		Image result = null;
		if (inputStream != null) {
			try {
				result = new Image(display, inputStream);
			} finally {
				try {
					inputStream.close();
				} catch (IOException e) {
					// ignore
				}
			}
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
	}

	public void setImage(String image) {
		this.image = image;
	}

}
