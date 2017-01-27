package org.argeo.connect.people.workbench.rap.composites.dropdowns;

import java.util.List;

import javax.jcr.Session;

import org.argeo.connect.people.ResourceService;
import org.eclipse.swt.widgets.Text;

/**
 * DropDown that displays the list of possible values for a property given a
 * template ID and a propertyName a given node type business instance
 */
public class TemplateCatalogueDropDown extends PeopleAbstractDropDown {

	private final Session session;
	private final ResourceService resourceService;
	private final String templateId;
	private final String propertyName;

	/**
	 * @param session
	 * @param templateId
	 * @param propertyName
	 * @param text
	 * @param peopleService
	 */
	public TemplateCatalogueDropDown(Session session,
			ResourceService resourceService, String templateId, String propertyName, Text text) {
		super(text);
		this.resourceService = resourceService;
		this.session = session;
		this.templateId = templateId;
		this.propertyName = propertyName;
		init();
	}

	@Override
	protected List<String> getFilteredValues(String filter) {
		List<String> values = resourceService.getTemplateCatalogue(session,
				templateId, propertyName, filter);
		return values;
	}
}