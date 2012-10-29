package org.argeo.connect.demo.gr.ui.utils;

import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;

public abstract class AbstractHyperlinkListener implements IHyperlinkListener {

	public void linkEntered(HyperlinkEvent e) {
	}

	public void linkExited(HyperlinkEvent e) {
	}

	/** Must be overriden **/
	public abstract void linkActivated(HyperlinkEvent e);
}
