package org.argeo.connect;

import javax.jcr.query.Query;

/** Centralize cross-apps constants */
public interface ConnectConstants {
	// Workaround the JCR deprecation of XPath
	@SuppressWarnings("deprecation")
	String QUERY_XPATH = Query.XPATH;
	String HOME_APP_SYS_RELPARPATH = ".local/argeo:sys";

	// We use a key that look like a node type without declaring it for some
	// concepts
	String RESOURCE_TAG = "connect:tag";
	String RESOURCE_COUNTRY = "connect:country";
	String RESOURCE_LANG = "connect:language";

	// System properties known by the system
	String SYS_PROP_ID_PREVENT_TAG_ADDITION = "org.argeo.connect.resources.preventTagAddition";
}
