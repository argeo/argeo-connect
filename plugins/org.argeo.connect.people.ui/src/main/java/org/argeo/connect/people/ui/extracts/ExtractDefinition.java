package org.argeo.connect.people.ui.extracts;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Property;
import javax.jcr.PropertyType;

import org.argeo.connect.people.PeopleTypes;

public interface ExtractDefinition {

	public final static List<ColumnDefinition> EXTRACT_SIMPLE_MAILING_LIST = new ArrayList<ColumnDefinition>() {
		private static final long serialVersionUID = 1L;

		{
			add(new ColumnDefinition(PeopleTypes.PEOPLE_ENTITY,
					Property.JCR_TITLE, PropertyType.STRING, "Display name"));

			// add(new ColumnDefinition(PeopleTypes.PEOPLE_PERSON,
			// PeopleNames.PEOPLE_LAST_NAME, PropertyType.STRING,
			// "Last name"));
			// add(new ColumnDefinition(PeopleTypes.PEOPLE_PERSON,
			// PeopleNames.PEOPLE_FIRST_NAME, PropertyType.STRING,
			// "First name"));
		}
	};

	// Reminder create a List in one line
	// List<String> list3 = new ArrayList<String>(
	// Arrays.asList("String A", "String B", "String C")
	// );

}
