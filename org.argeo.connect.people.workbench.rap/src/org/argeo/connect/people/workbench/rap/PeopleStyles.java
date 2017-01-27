package org.argeo.connect.people.workbench.rap;

/** Centralize the declaration of RAP Workbench specific CSS Styles */
public interface PeopleStyles {

	// Specific People layouting
	public final static String LOGO = "people-logo";
	public final static String LOGO_TABLE = "people-logoTable";
	public final static String LOGO_BOX = "people-logoComposite";
	public final static String FLAT_BTN = "people_flat_btn";
	public final static String POPUP_SHELL = "people_popup_shell";
	// Overwrite normal behavior: show a border even when a Text is disabled.
	public final static String PEOPLE_CLASS_FORCE_BORDER = "people_force_border";

	// Overview styles
	public final static String PEOPLE_CLASS_ENTITY_HEADER = "people_entity_header";
	public final static String SMALL_TEXT = "people_small_text";

	// Gadgets (typically in the home page)
	public final static String GADGET_HEADER = "people_gadget_header";
	public final static String PEOPLE_CLASS_GADGET = "people_gadget";
}
