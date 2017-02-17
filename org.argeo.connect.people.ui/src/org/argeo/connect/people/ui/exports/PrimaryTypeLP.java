package org.argeo.connect.people.ui.exports;

import javax.jcr.Property;

import org.argeo.connect.resources.ResourceService;
import org.argeo.connect.ui.JcrRowLabelProvider;

/**
 * Returns a human friendly label for the primary type of the node that is
 * returned with the given selector name, if such a label is known by the
 * resource service. Returns the node type otherwise.
 */
public class PrimaryTypeLP extends JcrRowLabelProvider {
	private static final long serialVersionUID = 1L;

	private ResourceService resourceService;

	public PrimaryTypeLP(ResourceService resourceService, String selectorName) {
		super(selectorName, Property.JCR_PRIMARY_TYPE);
		this.resourceService = resourceService;
	}

	@Override
	public String getText(Object element) {
		return resourceService.getItemDefaultEnLabel(super.getText(element));
	}
}
