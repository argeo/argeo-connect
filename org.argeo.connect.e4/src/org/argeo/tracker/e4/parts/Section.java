package org.argeo.tracker.e4.parts;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

class Section extends Composite {
	private static final long serialVersionUID = -7682652336964238982L;
	private Composite client;

	public Section(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout());
	}

	public void setClient(Composite client) {
		this.client = client;
	}

	public Composite getClient() {
		return client;
	}

	public void setText(String text) {

	}
	
	public void setTextClient(Composite composite) {
		
	}
}
