package org.argeo.connect.people.workbench.rap;

/** Centralize the declaration of RAP Workbench specific CSS Styles */
public interface PeopleStyles {

	// Specific People layouting
	String LOGO = "people-logo";
	String LOGO_TABLE = "people-logoTable";
	String LOGO_BOX = "people-logoComposite";
	String FLAT_BTN = "people_flat_btn";
	String POPUP_SHELL = "people_popup_shell";
	// Overwrite normal behavior: show a border even when a Text is disabled.
	String PEOPLE_CLASS_FORCE_BORDER = "people_force_border";

	// Overview styles
	String PEOPLE_CLASS_ENTITY_HEADER = "people_entity_header";
	String SMALL_TEXT = "people_small_text";

	// Gadgets (typically in the home page)
	String GADGET_HEADER = "people_gadget_header";
	String PEOPLE_CLASS_GADGET = "people_gadget";
}
