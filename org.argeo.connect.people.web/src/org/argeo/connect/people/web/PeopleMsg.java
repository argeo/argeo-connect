package org.argeo.connect.people.web;

import org.argeo.cms.DefaultsResourceBundle;
import org.argeo.cms.Msg;

/** Humane readable messages used by the Argeo People framework. */
public class PeopleMsg extends DefaultsResourceBundle {
	public final static Msg searchEntities = new Msg("search entities");

	static {
		Msg.init(PeopleMsg.class);
	}
}
