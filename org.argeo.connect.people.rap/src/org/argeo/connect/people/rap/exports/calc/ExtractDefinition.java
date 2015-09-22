package org.argeo.connect.people.rap.exports.calc;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Property;
import javax.jcr.PropertyType;

import org.argeo.connect.people.PeopleTypes;
import org.argeo.eclipse.ui.jcr.lists.ColumnDefinition;

public interface ExtractDefinition {

	public final static List<ColumnDefinition> EXTRACT_SIMPLE_MAILING_LIST = new ArrayList<ColumnDefinition>() {
		private static final long serialVersionUID = 1L;
		{
			add(new ColumnDefinition(PeopleTypes.PEOPLE_ENTITY,
					Property.JCR_TITLE, PropertyType.STRING, "Display name"));
		}
	};

	// TIP: create a List in one line
	// List<String> list3 = new ArrayList<String>(
	// Arrays.asList("String A", "String B", "String C")
	// );

}
