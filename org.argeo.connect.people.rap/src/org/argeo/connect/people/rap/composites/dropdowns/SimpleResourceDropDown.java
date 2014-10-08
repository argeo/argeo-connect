package org.argeo.connect.people.rap.composites.dropdowns;

import java.util.List;

import javax.jcr.Session;

import org.argeo.connect.people.LabelService;
import org.eclipse.swt.widgets.Text;

/**
 * Simple DropDown that displays the list of possible resource values at the
 * given base bath
 */
public class SimpleResourceDropDown extends PeopleAbstractDropDown {

	private final Session session;
	private final LabelService labelService;
	private final String resourceBasePath;

	public SimpleResourceDropDown(LabelService labelService, Session session,
			String resourceBasePath, Text text) {
		super(text);
		this.labelService = labelService;
		this.session = session;
		this.resourceBasePath = resourceBasePath;
		init();
	}

	@Override
	protected List<String> getFilteredValues(String filter) {
		List<String> values = labelService.getValueList(session,
				resourceBasePath, filter);
		return values;
	}
}
