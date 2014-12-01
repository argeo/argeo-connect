package org.argeo.photo.manager.ui.editors;

import java.awt.Canvas;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.argeo.ArgeoException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class SwingRawDisplayPage extends FormPage {
	private BufferedImage image;
	private Frame frame;

	public SwingRawDisplayPage(FormEditor editor, String id, String title) {
		super(editor, id, title);
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		String testFile = "/home/mbaudier/dev/work/121201-PhotoManager/116_1224/"
				+ "IMG_0326.JPG";
		try {
			Composite parent = managedForm.getForm().getBody();
			parent.setLayout(new FillLayout());

			FormToolkit tk = managedForm.getToolkit();

			Composite embedded = new Composite(parent, SWT.EMBEDDED);

			frame = SWT_AWT.new_Frame(embedded);

			image = ImageIO.read(new File(testFile));
			Canvas canvas = new Canvas() {
				private static final long serialVersionUID = -672258853622779550L;

				@Override
				public void paint(Graphics g) {
					g.drawImage(image, 0, 0, null);
				}

			};
			frame.add(canvas);
			canvas.repaint();

//			GridLayout layout = new GridLayout();
//			layout.marginHeight = 0;
//			layout.marginWidth = 0;
//			embedded.setLayout(layout);
//
//			tk.adapt(embedded);
		} catch (IOException e) {
			throw new ArgeoException("", e);
		}
	}

	@Override
	public void dispose() {
		if (frame != null)
			frame.dispose();
	}

}
