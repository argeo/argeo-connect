package org.argeo.eclipse.ui.files;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/** Draft for some UI upon Java 7 nio files api */
public class SimpleBrowser extends Composite {
	private static final long serialVersionUID = -40347919096946585L;

	public SimpleBrowser(Composite parent, int style) {
		super(parent, style);
		createContent();
		parent.layout(true, true);
	}
	
	private void createContent(){		
		SashForm form = new SashForm(this,SWT.VERTICAL);
		form.setLayout(new FillLayout());

		Composite child1 = new Composite(form,SWT.NONE);
		child1.setLayout(new FillLayout());
		new Label(child1,SWT.NONE).setText("Label in pane 1");

		Composite child2 = new Composite(form,SWT.NONE);
		child2.setLayout(new FillLayout());
		new Button(child2,SWT.PUSH).setText("Button in pane2");

		Composite child3 = new Composite(form,SWT.NONE);
		child3.setLayout(new FillLayout());
		new Label(child3,SWT.PUSH).setText("Label in pane3");
	}
}
