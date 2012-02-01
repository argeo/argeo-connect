package org.argeo.connect.ui.gps;

import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.gpx.TrackDao;
import org.argeo.geotools.styling.StylingUtils;
import org.argeo.gis.GisConstants;
import org.argeo.gis.ui.MapControlCreator;
import org.argeo.gis.ui.MapViewer;

/**
 * Centralizes UI specific methods to manage the Connect Gps UI and
 * corresponding model.
 */
public class GpsUiGisServices {
	private final static Log log = LogFactory.getLog(GpsUiGisServices.class);

	/* DEPENDENCY INJECTION */
	private TrackDao trackDao;
	private MapControlCreator mapControlCreator;
	private Map<String, String> baseLayers;
	private GpsUiJcrServices uiJcrServices;

	/* LAYERS MANAGEMENT */
	/** Add all defined base layers to the viewer */
	public void addBaseLayers(MapViewer mapViewer) {
		for (String name : baseLayers.keySet()) {
			addBaseLayer(mapViewer, name);
		}
	}

	/** Add a specific layer to the viewer */
	public void addBaseLayer(MapViewer mapViewer, String layerName) {
		String layerPath = (GisConstants.DATA_STORES_BASE_PATH + baseLayers
				.get(layerName)).trim();
		try {
			Node layerNode = uiJcrServices.getJcrSession().getNode(layerPath);
			mapViewer.addLayer(layerNode,
					StylingUtils.createLineStyle("LIGHT_GRAY", 1));
		} catch (RepositoryException e) {
			log.warn("Cannot retrieve " + layerName + ": " + e);
		}
	}

	/* Exposes injected objects */
	/** exposes injected track DAO */
	public TrackDao getTrackDao() {
		return trackDao;
	}

	/** exposes injected GpsUiJcrServices */
	public GpsUiJcrServices getUiJcrServices() {
		return uiJcrServices;
	}

	/** exposes injected MapControlCreator */
	public MapControlCreator getMapControlCreator() {
		return mapControlCreator;
	}

	/* DEPENDENCY INJECTION */
	public void setUiJcrServices(GpsUiJcrServices uiJcrServices) {
		this.uiJcrServices = uiJcrServices;
	}

	public void setTrackDao(TrackDao trackDao) {
		this.trackDao = trackDao;
	}

	public void setMapControlCreator(MapControlCreator mapControlCreator) {
		this.mapControlCreator = mapControlCreator;
	}

	public void setBaseLayers(Map<String, String> baseLayers) {
		this.baseLayers = baseLayers;
	}

}
