/*
 * Argeo Connect - Data management and communications
 * Copyright (C) 2012 Argeo GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or combining it
 * with software covered by the terms of the Eclipse Public License, the
 * licensors of this Program grant you additional permission to convey the
 * resulting work. Corresponding Source for a non-source form of such a
 * combination shall include the source code for the parts of such software
 * which are used as well as that of the covered work.
 */
package org.argeo.connect.ui.gps;

import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.gps.TrackDao;
import org.argeo.geotools.StylingUtils;
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
