package org.argeo.connect.people.rap.composites.dropdowns;

import java.util.List;

import javax.jcr.Session;

import org.argeo.connect.people.LabelService;
import org.eclipse.swt.widgets.Text;

/**
 * DropDown that displays the list of possible values for the given property of
 * a given node type business instance
 */
public class InstancePropertyValuesDropDown extends PeopleAbstractDropDown {

	private final Session session;
	private final LabelService labelService;
	private final String resourcePath;
	private final String propertyName;

	/**
	 * @param peopleService
	 * @param session
	 * @param resourcePath
	 * @param propertyName
	 * @param text
	 */
	public InstancePropertyValuesDropDown(LabelService labelService,
			Session session, String resourcePath, String propertyName, Text text) {
		super(text);
		this.labelService = labelService;
		this.session = session;
		this.resourcePath = resourcePath;
		this.propertyName = propertyName;
		init();
	}

	@Override
	protected List<String> getFilteredValues(String filter) {
		List<String> values = labelService.getInstancePropCatalog(session,
				resourcePath, propertyName, filter);
		return values;
	}
}
