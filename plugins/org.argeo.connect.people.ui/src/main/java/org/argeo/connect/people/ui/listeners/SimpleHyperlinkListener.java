package org.argeo.connect.people.ui.listeners;

import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;

/** Helper class to reduce verbosity while adding a new simple HyperlinkListener */
public abstract class SimpleHyperlinkListener implements IHyperlinkListener {
	public void linkEntered(HyperlinkEvent e) {
	}

	public void linkExited(HyperlinkEvent e) {
	}

	/** Must be overriden **/
	public abstract void linkActivated(HyperlinkEvent e);
}