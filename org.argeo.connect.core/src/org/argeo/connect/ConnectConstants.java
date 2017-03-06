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
	
	// Various
	String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	String USERNAME_PATTERN = "^[a-zA-Z]+[.a-zA-Z0-9_-]{2,31}$";
}
