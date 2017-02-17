package org.argeo.connect.resources;

/** Property names for the Resources App */
public interface ResourcesNames {

	String RESOURCES_BASE_NAME = "resources";
	// Subnodes for various types of resources 
	String RESOURCES_TEMPLATES = "templates";
	String RESOURCES_TAG_LIKE = "tags";
	/* RESOURCES */
	// Generally the corresponding node type. Might be something else.
	String PEOPLE_TEMPLATE_ID = "people:templateId"; // (STRING)
	String PEOPLE_TAG_ID = "people:tagId"; // (STRING)
	String PEOPLE_TAG_INSTANCE_TYPE = "people:tagInstanceType"; // (STRING)
	String PEOPLE_TAG_CODE_PROP_NAME = "people:codePropName"; // (STRING)
	String PEOPLE_TAGGABLE_PARENT_PATH = "people:taggableParentPath"; // (STRING)
	String PEOPLE_TAGGABLE_NODE_TYPE = "people:taggableNodeType"; // (STRING)
	String PEOPLE_TAGGABLE_PROP_NAME = "people:taggablePropNames"; // (STRING)
	String PEOPLE_CODE = "people:code"; // (STRING)
}
