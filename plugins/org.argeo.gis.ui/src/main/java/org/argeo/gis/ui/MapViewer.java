package org.argeo.gis.ui;

import java.util.Collection;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.argeo.geotools.jcr.GeoJcrMapper;
import org.eclipse.swt.widgets.Composite;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;

/** Viewer for a map, relying on JCR. */
public interface MapViewer {
	public void addLayer(Node layer, Object style);

	public void addLayer(String layerId, NodeIterator layer, Object style);

	public void addLayer(String layerId, Collection<?> collection, Object style);

	public void addLayer(String layerId,
			FeatureIterator<SimpleFeature> featureIterator, Object style);

	public NodeIterator getSelectedFeatures();

	public Composite getControl();

	public void addMapViewerListener(MapViewerListener listener);

	public void removeMapViewerListener(MapViewerListener listener);

	public void setAreaOfInterest(ReferencedEnvelope areaOfInterest);

	// public void setCoordinateReferenceSystem(String crs);

	public void setStyle(String layerId, Object style);

	public GeoJcrMapper getGeoJcrMapper();

	public void setFocus();

	public void removeAllLayers();
}
