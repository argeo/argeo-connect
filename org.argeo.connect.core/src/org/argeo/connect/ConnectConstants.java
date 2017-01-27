package org.argeo.connect;

import javax.jcr.query.Query;

/** Centralize cross-apps constants */
public interface ConnectConstants {

	// Workaround the JCR deprecation of XPath
	@SuppressWarnings("deprecation")
	String QUERY_XPATH = Query.XPATH;
	
}
