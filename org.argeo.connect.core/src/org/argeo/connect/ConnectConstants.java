package org.argeo.connect;

import javax.jcr.query.Query;

/** Centralize cross-apps constants */
public interface ConnectConstants {

	// Workaround the JCR deprecation of XPath
	@SuppressWarnings("deprecation")
	String QUERY_XPATH = Query.XPATH;
	String HOME_APP_SYS_RELPARPATH = ".local/argeo:sys";

	// Dirty workaround to avoid plain strings in xpath.request: the jcr
	// references have the complete unique namespace URL as prefix
	// TODO find a cleaner way to replace the URL by the local declared prefix
	String JCR_TITLE = "jcr:title";
	String JCR_DESCRIPTION = "jcr:description";
	String JCR_PRIMARY_TYPE = "jcr:primaryType";
	String JCR_CREATED = "jcr:created";
	String MIX_LAST_MODIFIED = "mix:lastModified";
	String JCR_LAST_MODIFIED = "jcr:lastModified";
	
	
	// We use a key that look like a node type without declaring it for some concepts
	String RESOURCE_TAG = "people:tag";
	String RESOURCE_COUNTRY = "people:country";
	String RESOURCE_LANG = "people:language";
	
	// System properties known by the system
	String SYS_PROP_ID_PREVENT_TAG_ADDITION = "org.argeo.connect.resources.preventTagAddition";
}
