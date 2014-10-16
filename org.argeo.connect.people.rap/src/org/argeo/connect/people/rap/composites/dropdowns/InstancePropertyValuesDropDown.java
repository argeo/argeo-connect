package org.argeo.connect.people.rap.composites.dropdowns;

import java.util.List;

import javax.jcr.Session;

import org.argeo.connect.people.ResourceService;
import org.eclipse.swt.widgets.Text;

/**
 * DropDown that displays the list of possible values for the given property of
 * a given node type business instance
 */
public class InstancePropertyValuesDropDown extends PeopleAbstractDropDown {

	private final Session session;
	private final ResourceService resourceService;
	private final String resourcePath;
	private final String propertyName;

	/**
	 * @param peopleService
	 * @param session
	 * @param resourcePath
	 * @param propertyName
	 * @param text
	 */
	public InstancePropertyValuesDropDown(ResourceService resourceService,
			Session session, String resourcePath, String propertyName, Text text) {
		super(text);
		this.resourceService = resourceService;
		this.session = session;
		this.resourcePath = resourcePath;
		this.propertyName = propertyName;
		init();
	}

	@Override
	protected List<String> getFilteredValues(String filter) {
		List<String> values = resourceService.getPossibleValues(session,
				resourcePath, propertyName, filter);
		return values;
	}
}
