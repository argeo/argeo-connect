package org.argeo.connect;

import org.argeo.entity.EntityNames;

/** Connect generic JCR names. */
public interface ConnectNames {

	/** @deprecated Use {@link EntityNames#ENTITY_UID} instead. */
	@Deprecated
	String CONNECT_UID = EntityNames.ENTITY_UID;

	String CONNECT_PHOTO = "photo";

	/* EXTERNAL IDS */
	String CONNECT_EXTERNAL_IDS = "externalIds"; // parent node for external ids
	String CONNECT_SOURCE_URI = "connect:sourceUri";
	String CONNECT_SOURCE_ID = "connect:sourceId";
	String CONNECT_EXTERNAL_UID = "connect:externalUid";

	// Defines various standard property names
	String CONNECT_DATE_BEGIN = "connect:dateBegin";
	String CONNECT_DATE_END = "connect:dateEnd";

	String CONNECT_CLOSE_DATE = "connect:closeDate";
	String CONNECT_CLOSED_BY = "connect:closedBy";

	// Widely used property names
	// String CONNECT_LANG = "connect:lang";
	// Reference an other entity using the business specific UID
	// String CONNECT_REF_UID = "connect:refUid";
	// Primary flag
	// String CONNECT_IS_PRIMARY = "connect:isPrimary";
}
