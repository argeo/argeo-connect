package org.argeo.photo.manager.ui.editors;

import java.io.InputStream;

import org.argeo.photo.manager.PictureManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;

/** Display any picture. Base class for editors manipulating pictures. */
public class PictureDisplayPage extends FormPage {
	private ImageData imageData;
	private Image image;
	private final PictureManager pictureManager;

	public PictureDisplayPage(FormEditor editor, PictureManager pictureManager) {
		super(editor, "pictureDisplayPage", "View");
		this.pictureManager = pictureManager;
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		PictureEditorInput ei = (PictureEditorInput) getEditorInput();

		Composite parent = managedForm.getForm().getBody();
		parent.setLayout(new FillLayout());

		FormToolkit tk = managedForm.getToolkit();

		Display display = getSite().getShell().getDisplay();

		InputStream in = pictureManager
				.getPictureAsStream(ei.getRelativePath());
		imageData = new ImageData(in);
		image = new Image(display, imageData);
		final Canvas canvas = new Canvas(parent, SWT.NO_REDRAW_RESIZE);
		canvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				// e.gc.drawImage(image, 0, 0);

				// quick and dirty resize keeping proportions
				// to be improved with only rational numbers
				Float factor = ((float) imageData.width)
						/ ((float) imageData.height);
				Float targetWidth = canvas.getSize().y * factor;
				if (targetWidth > canvas.getSize().x) {
					Float targetHeight = canvas.getSize().x / factor;
					e.gc.drawImage(image, 0, 0, imageData.width,
							imageData.height, 0, 0, canvas.getSize().x,
							targetHeight.intValue());
				} else {
					e.gc.drawImage(image, 0, 0, imageData.width,
							imageData.height, 0, 0, targetWidth.intValue(),
							canvas.getSize().y);
				}

				// distort
				// e.gc.drawImage(image, 0, 0, imageData.width,
				// imageData.height,
				// 0, 0, canvas.getSize().x, canvas.getSize().y);
			}
		});

		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		canvas.setLayout(layout);

		tk.adapt(canvas);
	}

	@Override
	public void dispose() {
		if (image != null)
			image.dispose();
	}

}
