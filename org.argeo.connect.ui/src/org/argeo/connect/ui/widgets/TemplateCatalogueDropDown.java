package org.argeo.connect.ui.widgets;

import java.util.List;

import javax.jcr.Session;

import org.argeo.connect.resources.ResourcesService;
import org.eclipse.swt.widgets.Text;

/**
 * DropDown that displays the list of possible values for a property given a
 * template ID and a propertyName a given node type business instance
 */
public class TemplateCatalogueDropDown extends ConnectAbstractDropDown {

	private final Session session;
	private final ResourcesService resourcesService;
	private final String templateId;
	private final String propertyName;

	/**
	 * @param session
	 * @param resourceService
	 * @param templateId
	 * @param propertyName
	 * @param text
	 */
	public TemplateCatalogueDropDown(Session session, ResourcesService resourceService, String templateId,
			String propertyName, Text text) {
		super(text);
		this.resourcesService = resourceService;
		this.session = session;
		this.templateId = templateId;
		this.propertyName = propertyName;
		init();
	}

	@Override
	protected List<String> getFilteredValues(String filter) {
		List<String> values = resourcesService.getTemplateCatalogue(session, templateId, propertyName, filter);
		return values;
	}
}
