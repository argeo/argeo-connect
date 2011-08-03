package org.argeo.gis.ui;

import javax.jcr.Node;

public interface MapContextProvider {
	//public MapContext getMapContext();
	
	public void addLayer(Node layer);
	
	public void featureSelected(String layerId, String featureId);
	public void featureUnselected(String layerId, String featureId);
}
