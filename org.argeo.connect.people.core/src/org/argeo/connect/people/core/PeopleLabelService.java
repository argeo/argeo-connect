package org.argeo.connect.people.core;

import java.util.Map;

import javax.jcr.Node;

import org.argeo.connect.people.LabelService;

/** TODO implement this service */
public class PeopleLabelService implements LabelService {

	@Override
	public String getItemDefaultEnLabel(String itemName) {
		return null;
	}

	@Override
	public String getItemLabel(String itemName, String langIso) {
		return null;
	}

	@Override
	public String[] getDefinedValues(Node node, String propertyName) {
		return null;
	}

	@Override
	public Map<String, String> getDefinedValueMap(Node node, String propertyName) {
		return null;
	}
}
