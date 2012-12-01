package org.argeo.photo.manager;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class PathConverterResult {
	private final Set<String> paths;
	private Map<String, String> mapping = new TreeMap<String, String>();
	private Map<String, Exception> conversionErrors = new TreeMap<String, Exception>();
	private Set<String> duplicated = new TreeSet<String>();

	public PathConverterResult(Set<String> paths) {
		this.paths = paths;
	}

	public boolean isValid() {
		return conversionErrors.size() == 0 && duplicated.size() == 0;
	}

	public Map<String, String> getMapping() {
		return mapping;
	}

	public Map<String, Exception> getConversionErrors() {
		return conversionErrors;
	}

	public Set<String> getDuplicated() {
		return duplicated;
	}

	public Set<String> getPaths() {
		return paths;
	}

}
