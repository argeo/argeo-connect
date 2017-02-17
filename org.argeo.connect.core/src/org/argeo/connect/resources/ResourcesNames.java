package org.argeo.connect.resources;

/** Property names for the Resources App */
public interface ResourcesNames {

	/* JCR TREE MODEL*/
	String RESOURCES_BASE_NAME = "resources";
	// Subnodes for various types of resources
	String RESOURCES_TEMPLATES = "templates";
	String RESOURCES_TAG_LIKE = "tags";
	
	/* RESOURCES */
	// Generally the corresponding node type. Might be something else.
	String CONNECT_TEMPLATE_ID = "connect:templateId"; // (STRING)
	String CONNECT_TAG_ID = "connect:tagId"; // (STRING)
	String CONNECT_TAG_INSTANCE_TYPE = "connect:tagInstanceType"; // (STRING)
	String CONNECT_TAG_CODE = "connect:code"; // (STRING)
	String CONNECT_TAG_CODE_PROP_NAME = "connect:codePropName"; // (STRING)
	String CONNECT_TAGGABLE_PARENT_PATH = "connect:taggableParentPath"; // (STRING)
	String CONNECT_TAGGABLE_NODE_TYPE = "connect:taggableNodeType"; // (STRING)
	String CONNECT_TAGGABLE_PROP_NAME = "connect:taggablePropNames"; // (STRING)
}
