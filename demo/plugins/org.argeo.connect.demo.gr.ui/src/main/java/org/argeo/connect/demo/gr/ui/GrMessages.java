package org.argeo.connect.demo.gr.ui;

import org.argeo.eclipse.ui.specific.ThreadNLS;
import org.eclipse.osgi.util.NLS;

/** Localized messages */
public class GrMessages extends NLS {
	public final static ThreadNLS<GrMessages> nls = new ThreadNLS<GrMessages>(
			GrMessages.class);

	public static GrMessages get() {
		return nls.get();
	}
}
