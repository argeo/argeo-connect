package org.argeo.connect;

/** Connect generic JCR names. */
public interface ConnectNames {

	/**
	 * An implementation specific UID, might be a JCR node Identifier but it is
	 * not compulsory We personally use the type 4 (pseudo randomly generated)
	 * UUID - we retrieve them simply in java with this method
	 * <code>UUID.randomUUID().toString()</code> Class 3 UUID of the
	 * distinguished name in UTF-8
	 */
	String CONNECT_UID = "conect:uid";

	String CONNECT_TAGS = "connect:tags";
	String CONNECT_EXTERNAL_IDS = "connect:externalIds";

	// Widely used property names
	String CONNECT_LANG = "connect:lang";
	// Reference an other entity using the business specific UID
	String CONNECT_REF_UID = "connect:refUid";
	// Primary flag
	String CONNECT_IS_PRIMARY = "connect:isPrimary";

	/* EXTERNAL IDS */
	String CONNECT_SOURCE_ID = "connect:sourceId";
	String CONNECT_EXTERNAL_UID = "connect:externalUid";
	
	/* RESOURCES */
	// Generally the corresponding node type. Might be something else.
	String CONNECT_TEMPLATE_ID = "connect:templateId"; 
	String CONNECT_TAG_ID = "connect:tagId";
	String CONNECT_TAG_INSTANCE_TYPE = "connect:tagInstanceType";
	String CONNECT_TAG_CODE_PROP_NAME = "connect:codePropName";
	String CONNECT_CODE = "connect:code";
	String CONNECT_TAGGABLE_PARENT_PATH = "connect:taggableParentPath";
	String CONNECT_TAGGABLE_NODE_TYPE = "connect:taggableNodeType";
	String CONNECT_TAGGABLE_PROP_NAME = "connect:taggablePropNames";

	
	// Various
	String CONNECT_DATE_BEGIN = "connect:dateBegin";
	String CONNECT_DATE_END = "connect:dateEnd";
	

}
