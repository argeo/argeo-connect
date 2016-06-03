package org.argeo.photo.manager.ui.editors;

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

public class RawDisplayPage extends FormPage {
	private ImageData imageData;
	private Image image;

	public RawDisplayPage(FormEditor editor, String id, String title) {
		super(editor, id, title);
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		Composite parent = managedForm.getForm().getBody();
		parent.setLayout(new FillLayout());

		FormToolkit tk = managedForm.getToolkit();

		Display display = getSite().getShell().getDisplay();

		String testFile = "/home/mbaudier/dev/work/121201-PhotoManager/s100/raw/116_1224/"
				+ "IMG_0326.tiff";
		imageData = new ImageData(testFile);
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
