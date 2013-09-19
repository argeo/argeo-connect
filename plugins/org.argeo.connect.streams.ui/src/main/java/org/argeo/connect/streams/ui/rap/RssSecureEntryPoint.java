package org.argeo.connect.streams.ui.rap;

import org.argeo.security.ui.rap.RapWorkbenchAdvisor;
import org.argeo.security.ui.rap.SecureEntryPoint;

/**
 * Enables definition of a custom entry point to call extension of the
 * RapWindoAdvisor. FIXME Is it really usefull ?
 */

public class RssSecureEntryPoint extends SecureEntryPoint {

	/** Override to provide a custom RapWorkbenchAdvisor */
	@Override
	protected RapWorkbenchAdvisor createRapWorkbenchAdvisor(String username) {
		return new RssRapWorkbenchAdvisor(username);
	}
}
