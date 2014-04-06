package org.argeo.connect.people.ui.composites.dropdowns;

import java.util.List;

import javax.jcr.Session;

import org.argeo.connect.people.ui.PeopleUiService;
import org.eclipse.swt.widgets.Text;

/**
 * DropDown that displays the list of possible values for the given property of
 * a given node type business instance
 */
public class InstancePropertyValuesDropDown extends PeopleAbstractDropDown {

	private final Session session;
	private final PeopleUiService peopleUiService;
	private final String resourcePath;
	private final String propertyName;

	public InstancePropertyValuesDropDown(PeopleUiService peopleUiService,
			Session session, String resourcePath, String propertyName, Text text) {
		super(text);
		this.peopleUiService = peopleUiService;
		this.session = session;
		this.resourcePath = resourcePath;
		this.propertyName = propertyName;
		init();
	}

	@Override
	protected List<String> getFilteredValues(String filter) {
		List<String> values = peopleUiService.getInstancePropCatalog(session,
				resourcePath, propertyName, filter);
		return values;
	}
}
