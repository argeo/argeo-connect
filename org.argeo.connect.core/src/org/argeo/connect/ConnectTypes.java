package org.argeo.connect;

/** JCR node types managed by Connect */
public interface ConnectTypes {

	/* COMMON CONCEPTS */
	String CONNECT_BASE = "connect:base";
	String CONNECT_ENTITY = "connect:entity";
	String CONNECT_EXTERNAL_ID = "connect:externalId";
	String CONNECT_TAGGABLE = "connect:taggable";

	// Minimal persons and contact model based on standard LDAP properties
	String CONNECT_CONTACTABLE = "connect:contactable";
	String CONNECT_LDAP_PERSON = "connect:person";
	String CONNECT_LDAP_ORG = "connect:org";
}
