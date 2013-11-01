package org.argeo.connect.people.rap;

import org.argeo.security.ui.rap.RapWorkbenchAdvisor;
import org.argeo.security.ui.rap.SecureEntryPoint;

/**
 * This class controls all aspects of the application's execution and is
 * contributed through the plugin.xml.
 */
public class PeopleSecureEntryPoint extends SecureEntryPoint {

	/** Override to provide a custom RapWorkbenchAdvisor */
	@Override
	protected RapWorkbenchAdvisor createRapWorkbenchAdvisor(String username) {
		return new PeopleRapWorkbenchAdvisor(username);
	}
}
