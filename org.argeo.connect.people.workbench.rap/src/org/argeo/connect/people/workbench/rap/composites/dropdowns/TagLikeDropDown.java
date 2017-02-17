package org.argeo.connect.people.workbench.rap.composites.dropdowns;

import java.util.List;

import javax.jcr.Session;

import org.argeo.connect.resources.ResourceService;
import org.eclipse.swt.widgets.Text;

/**
 * Simple DropDown that displays the list of registered values of a tag like
 * resource
 */
public class TagLikeDropDown extends PeopleAbstractDropDown {

	private final Session session;
	private final ResourceService resourceService;
	private final String tagId;

	public TagLikeDropDown(Session session, ResourceService resourceService,
			String tagId, Text text) {
		super(text);
		this.resourceService = resourceService;
		this.session = session;
		this.tagId = tagId;
		init();
	}

	@Override
	protected List<String> getFilteredValues(String filter) {
		List<String> values = resourceService.getRegisteredTagValueList(
				session, tagId, filter);
		return values;
	}
}
