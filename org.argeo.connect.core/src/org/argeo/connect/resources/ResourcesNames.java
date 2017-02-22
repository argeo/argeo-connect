package org.argeo.connect.resources;

/** Property names for the Resources App */
public interface ResourcesNames {

	/* JCR TREE MODEL */
	String RESOURCES_BASE_NAME = "resources";
	// Subnodes for various types of resources
	String RESOURCES_TEMPLATES = "templates";
	String RESOURCES_TAG_LIKE = "tags";

	/* RESOURCES */
	// Generally the corresponding node type. Might be something else.
	String RESOURCES_TEMPLATE_ID = "connect:templateId";

	String RESOURCES_TAG_ID = "connect:tagId";
	String RESOURCES_TAG_INSTANCE_TYPE = "connect:tagInstanceType";
	String RESOURCES_TAG_CODE = "connect:code";
	String RESOURCES_TAG_CODE_PROP_NAME = "connect:codePropName";
	String RESOURCES_TAGGABLE_PARENT_PATH = "connect:taggableParentPath";
	String RESOURCES_TAGGABLE_NODE_TYPE = "connect:taggableNodeType";
	String RESOURCES_TAGGABLE_PROP_NAME = "connect:taggablePropNames";
}
