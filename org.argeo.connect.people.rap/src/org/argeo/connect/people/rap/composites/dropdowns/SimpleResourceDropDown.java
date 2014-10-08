package org.argeo.connect.people.rap.composites.dropdowns;

import java.util.List;

import javax.jcr.Session;

import org.argeo.connect.people.rap.PeopleWorkbenchService;
import org.eclipse.swt.widgets.Text;

/**
 * Simple DropDown that displays the list of possible resource values at the
 * given base bath
 */
public class SimpleResourceDropDown extends PeopleAbstractDropDown {

	private final Session session;
	private final PeopleWorkbenchService peopleUiService;
	private final String resourceBasePath;

	public SimpleResourceDropDown(PeopleWorkbenchService peopleUiService,
			Session session, String resourceBasePath, Text text) {
		super(text);
		this.peopleUiService = peopleUiService;
		this.session = session;
		this.resourceBasePath = resourceBasePath;
		init();
	}

	@Override
	protected List<String> getFilteredValues(String filter) {
		List<String> values = peopleUiService.getValueList(session,
				resourceBasePath, filter);
		return values;
	}
}
