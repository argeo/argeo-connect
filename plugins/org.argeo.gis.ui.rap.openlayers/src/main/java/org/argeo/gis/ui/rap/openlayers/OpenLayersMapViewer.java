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
package org.argeo.gis.ui.rap.openlayers;

import java.awt.Color;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.jcr.Node;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.argeo.geotools.GeoToolsUtils;
import org.argeo.geotools.jcr.GeoJcrMapper;
import org.argeo.gis.ui.AbstractMapViewer;
import org.argeo.gis.ui.MapViewerListener;
import org.argeo.gis.ui.rap.openlayers.custom.GoogleLayer;
import org.eclipse.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.styling.ExternalGraphic;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.SLD;
import org.geotools.styling.Symbolizer;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.style.GraphicalSymbol;
import org.polymap.openlayers.rap.widget.OpenLayersWidget;
import org.polymap.openlayers.rap.widget.base.OpenLayersEventListener;
import org.polymap.openlayers.rap.widget.base.OpenLayersObject;
import org.polymap.openlayers.rap.widget.base_types.Bounds;
import org.polymap.openlayers.rap.widget.base_types.OpenLayersMap;
import org.polymap.openlayers.rap.widget.base_types.Projection;
import org.polymap.openlayers.rap.widget.base_types.Style;
import org.polymap.openlayers.rap.widget.base_types.StyleMap;
import org.polymap.openlayers.rap.widget.controls.KeyboardDefaultsControl;
import org.polymap.openlayers.rap.widget.controls.LayerSwitcherControl;
import org.polymap.openlayers.rap.widget.controls.MousePositionControl;
import org.polymap.openlayers.rap.widget.controls.NavigationControl;
import org.polymap.openlayers.rap.widget.controls.OverviewMapControl;
import org.polymap.openlayers.rap.widget.controls.PanZoomBarControl;
import org.polymap.openlayers.rap.widget.controls.ScaleControl;
import org.polymap.openlayers.rap.widget.controls.SelectFeatureControl;
import org.polymap.openlayers.rap.widget.features.VectorFeature;
import org.polymap.openlayers.rap.widget.geometry.LineStringGeometry;
import org.polymap.openlayers.rap.widget.geometry.PointGeometry;
import org.polymap.openlayers.rap.widget.layers.OSMLayer;
import org.polymap.openlayers.rap.widget.layers.VectorLayer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;

/** Map viewer implementation based on open layers. */
public class OpenLayersMapViewer extends AbstractMapViewer implements
		OpenLayersEventListener {
	private final static Log log = LogFactory.getLog(OpenLayersMapViewer.class);

	private final OpenLayersMap map;

	private Map<String, VectorLayer> vectorLayers = Collections
			.synchronizedMap(new HashMap<String, VectorLayer>());
	private Map<String, FeatureSource<SimpleFeatureType, SimpleFeature>> featureSources = Collections
			.synchronizedMap(new HashMap<String, FeatureSource<SimpleFeatureType, SimpleFeature>>());

	public OpenLayersMapViewer(Node context, GeoJcrMapper geoJcrMapper,
			Composite parent) {
		super(context, geoJcrMapper);
		createControl(parent);

		this.map = ((OpenLayersWidget) getControl()).getMap();
		// TODO: make dependent of the base layer
		map.zoomTo(2);

		// mapContextProvider.getMapContext().addMapLayerListListener(this);

		HashMap<String, String> payloadMap = new HashMap<String, String>();
		payloadMap.put("layername", "event.layer.name");
		this.map.events.register(this, "changebaselayer", payloadMap);
		payloadMap.put("property", "event.property");
		payloadMap.put("visibility", "event.layer.visibility");
		this.map.events.register(this, "changelayer", payloadMap);

		// WARNING: registering click events on the map hides other events!!
		// HashMap<String, String> mapPayload = new HashMap<String, String>();
		// mapPayload.put("bbox", map.getJSObjRef() + ".getExtent().toBBOX()");
		// mapPayload.put("lonlat", map.getJSObjRef()
		// + ".getLonLatFromViewPortPx(event.xy)");
		// mapPayload.put("x", "event.xy.x");
		// mapPayload.put("y", "event.xy.y");
		// mapPayload.put("button", "event.button");
		// map.events.register(this, "click", mapPayload);
	}

	protected void createControl(Composite parent) {
//		OpenLayersWidget openLayersWidget = new OpenLayersWidget(parent,
//				SWT.MULTI | SWT.WRAP);
		OpenLayersWidget openLayersWidget = new OpenLayersWidget(parent,
				SWT.MULTI | SWT.WRAP, "OpenLayers/OpenLayers.js");
		openLayersWidget.setLayoutData(new GridData(GridData.FILL_BOTH));

		String srs = "EPSG:3857";
		setMapProjection(srs);
		Projection projection = new Projection(srs);
		Projection displayProjection = new Projection("EPSG:4326");
		String units = "m";
		Bounds bounds = new Bounds(Double.parseDouble("2427766.775"),
				Double.parseDouble("970195.764"),
				Double.parseDouble("4298713.456"),
				Double.parseDouble("2538801.203"));
		Float maxResolution = 7308.385472656251f;
		openLayersWidget.createMap(projection, displayProjection, units,
				bounds, maxResolution);
		OpenLayersMap map = openLayersWidget.getMap();

		map.addControl(new LayerSwitcherControl());
		NavigationControl navigationControl = new NavigationControl();
		navigationControl.setObjAttr("handleRightClicks", true);
		navigationControl.setObjAttr("zoomBoxEnabled", true);
		map.addControl(navigationControl);
		map.addControl(new KeyboardDefaultsControl());
		map.addControl(new PanZoomBarControl());
		map.addControl(new ScaleControl());

		// WMSLayer baseLayer = new WMSLayer("argeo_dev",
		// "https://dev.argeo.org/geoserver/wms?",
		// "naturalearth:10m_admin_0_countries");

		// map.setDisplayProjection(new Projection("EPSG:4326"));

		// String srs = "EPSG:4326";

		// map.setProjection(new Projection("EPSG:900913"));

		// WMSLayer wmsLayer = new WMSLayer("Sudan Basemap",
		// "https://gis.argeo.org:443/geoserver/wms", "sudan_basemap");
		// wmsLayer.setFormat("image/png");
		// map.addLayer(wmsLayer);

		// map.zoomToExtent(bounds, true);
		OSMLayer osmLayer = new OSMLayer("OSM",
				"http://tile.openstreetmap.org/${z}/${x}/${y}.png", 19);
		map.addLayer(osmLayer);

		// VirtualEarthLayer virtualEarthLayer = new VirtualEarthLayer(
		// "Virtual Earth Aerial", VirtualEarthLayer.AERIAL, true);
		// map.addLayer(virtualEarthLayer);

		map.addLayer(new GoogleLayer("Google Satellite",
				GoogleLayer.G_SATELLITE_MAP));
		// map.addLayer(new GoogleLayer("Google Physical",
		// GoogleLayer.G_PHYSICAL_MAP));
		// map.addLayer(new GoogleLayer("Google Default", null));
		// map.addLayer(new GoogleLayer("Google Hybrid",
		// GoogleLayer.G_HYBRID_MAP));

		map.addControl(new OverviewMapControl());

		map.addControl(new MousePositionControl());
		setControl(openLayersWidget);
	}

	/*
	 * OPENLAYERS MAP
	 */

	public void process_event(OpenLayersObject source, String eventName,
			HashMap<String, String> payload) {
		if (eventName.equals("beforefeatureadded")) {
			if (log.isDebugEnabled())
				log.debug("before feature added on layer '"
						+ payload.get("layername") + "' x=" + payload.get("x")
						+ "' y=" + payload.get("y"));
		} else if (eventName.equals("afterfeatureadded")) {
			if (log.isDebugEnabled())
				log.debug("after feature added on layer '"
						+ payload.get("layername") + "' x=" + payload.get("x")
						+ "' y=" + payload.get("y"));
		} else if (eventName.equals("featureselected")) {
			if (log.isDebugEnabled())
				log.debug("feature selected " + payload);
			String layerId = payload.get("layerId");
			String featureId = payload.get("featureId");
			if (!getSelected().containsKey(layerId))
				getSelected().put(layerId, new TreeSet<String>());
			getSelected().get(layerId).add(featureId);

			for (MapViewerListener listener : getListeners())
				listener.featureSelected(layerId, featureId);
		} else if (eventName.equals("featureunselected")) {
			if (log.isDebugEnabled())
				log.debug("feature unselected " + payload);
			String layerId = payload.get("layerId");
			String featureId = payload.get("featureId");
			if (getSelected().containsKey(layerId))
				getSelected().get(layerId).remove(featureId);
			for (MapViewerListener listener : getListeners())
				listener.featureUnselected(layerId, featureId);

		} else if (log.isDebugEnabled())
			log.debug("Unknown event '" + eventName + "' from "
					+ source.getClass().getName() + " (" + source.getJSObjRef()
					+ ")" + " : " + payload);

	}

	@Override
	protected void addFeatureSource(String layerId,
			FeatureSource<SimpleFeatureType, SimpleFeature> featureSource,
			Object style) {
		try {
			featureSources.put(layerId, featureSource);
			FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = featureSource
					.getFeatures();
			addLayer(layerId, featureCollection.features(), style);
		} catch (IOException e) {
			log.error("Cannot add layer " + featureSource.getName(), e);
		}

	}

	@Override
	public void addLayer(String layerId,
			FeatureIterator<SimpleFeature> featureIterator, Object style) {
		try {
			Style st = null;

			if (style instanceof String) {
				st = new Style();
				st.setAttribute("externalGraphic", style.toString());
				st.setAttribute("graphicOpacity", "1");
				st.setAttribute("pointRadius", "8");
			} else if (style instanceof org.geotools.styling.Style) {
				// FIXME make it more generic
				org.geotools.styling.Style gtStyle = (org.geotools.styling.Style) style;
				Symbolizer symb = gtStyle.featureTypeStyles().get(0).rules()
						.get(0).getSymbolizers()[0];
				if (symb instanceof LineSymbolizer) {
					LineSymbolizer lSymb = (LineSymbolizer) symb;
					Color color = SLD.color(lSymb);
					int width = SLD.width(lSymb);
					String hexColor = "#" + hexString2Chars(color.getRed())
							+ hexString2Chars(color.getGreen())
							+ hexString2Chars(color.getBlue());
					// FIXME ugly hack because there seem to be a bug with black
					// and blue
					// String hexColor = SLD.colorToHex(color);
					// if (hexColor.length() == 3)
					// hexColor = hexColor + "0000";
					st = new Style();
					st.setAttribute("strokeColor", hexColor);
					st.setAttribute("strokeWidth", width);
					if (log.isDebugEnabled())
						log.debug("Stroke color=" + hexColor + ", width="
								+ width);

				} else if (symb instanceof PointSymbolizer) {
					ExternalGraphic externalGraphic = null;
					for (GraphicalSymbol symbol : ((PointSymbolizer) symb)
							.getGraphic().graphicalSymbols()) {
						if (symbol instanceof ExternalGraphic) {
							externalGraphic = (ExternalGraphic) symbol;
							break;
						}
					}

					String resourceName;
					if (externalGraphic != null) {
						URL osgiUrl = externalGraphic.getLocation();
						resourceName = osgiUrl.getHost() + osgiUrl.getPath();
					} else
						resourceName = gtStyle.getName();
					// String gtStyleName = gtStyle.getName();
					String imgLocation = null;
					if (resourceName != null) {
						imgLocation = RWT.getResourceManager().getLocation(
								resourceName);
					}

					st = new Style();
					st.setAttribute("externalGraphic", imgLocation);
					st.setAttribute("graphicOpacity", "1");
					st.setAttribute("pointRadius", "8");
				}
			}
			// AdvancedStyleMap styleMap = new AdvancedStyleMap();
			// Map<String, String> lookup = new HashMap<String, String>();
			// lookup.put("national", "national.gif");
			// lookup.put("base", "base.gif");
			// lookup.put("normal", "normal.gif");
			// styleMap.addUniqueValueRules("default", "gr_siteType", lookup);

			VectorLayer vectorLayer;
			if (st != null)
				vectorLayer = new VectorLayer(layerId.toString(), new StyleMap(
						st));
			else
				vectorLayer = new VectorLayer(layerId.toString());
			vectorLayer.setObjAttr("id", layerId);
			vectorLayers.put(layerId, vectorLayer);

			// selection
			HashMap<String, String> selectPayload = new HashMap<String, String>();
			selectPayload.put("featureId", "event.feature.id");
			selectPayload.put("geometry", "event.feature.geometry");
			selectPayload.put("layerId", "event.feature.layer.id");
			vectorLayer.events.register(this, "featureselected", selectPayload);

			HashMap<String, String> unselectPayload = new HashMap<String, String>();
			unselectPayload.put("featureId", "event.feature.id");
			unselectPayload.put("geometry", "event.feature.geometry");
			unselectPayload.put("layerId", "event.feature.layer.id");
			vectorLayer.events.register(this, "featureunselected",
					unselectPayload);
			SelectFeatureControl mfc = new SelectFeatureControl(vectorLayer, 0);
			// mfc.events.register(this, SelectFeatureControl.EVENT_HIGHLIGHTED,
			// unselectPayload);
			// mfc.events.register(this,
			// SelectFeatureControl.EVENT_UNHIGHLIGHTED,
			// unselectPayload);
			map.addControl(mfc);
			mfc.setMultiple(true);
			// mfc.setRenderIntent("temporary");
			mfc.activate();

			// TODO make this interruptible since it can easily block with huge
			// data
			while (featureIterator.hasNext()) {
				SimpleFeature feature = featureIterator.next();

				Geometry geom = getReprojectedGeometry(feature);

				if (log.isTraceEnabled())
					log.trace("Feature " + feature.getID() + ", "
							+ geom.getClass().getName());
				// log.debug("  Geom: " + geom.getClass() + ", centroid="
				// + geom.getCentroid());
				VectorFeature vf;
				if (geom instanceof Point) {
					Point mp = (Point) geom;
					PointGeometry pg = new PointGeometry(mp.getX(), mp.getY());
					vf = new VectorFeature(pg);
					vf.setObjAttr("id", feature.getID());
				} else if (geom instanceof MultiPolygon) {
					MultiPolygon mp = (MultiPolygon) geom;
					List<PointGeometry> points = new ArrayList<PointGeometry>();
					for (Coordinate coo : mp.getCoordinates()) {
						points.add(new PointGeometry(coo.x, coo.y));
					}
					vf = new VectorFeature(new LineStringGeometry(
							points.toArray(new PointGeometry[points.size()])));
				} else if (geom instanceof LineString) {
					LineString lineString = (LineString) geom;
					List<PointGeometry> points = new ArrayList<PointGeometry>();
					for (Coordinate coo : lineString.getCoordinates()) {
						points.add(new PointGeometry(coo.x, coo.y));
					}
					vf = new VectorFeature(new LineStringGeometry(
							points.toArray(new PointGeometry[points.size()])));
				} else {
					throw new ArgeoException("Unsupported geometry type "
							+ geom);
				}

				attributes: for (AttributeDescriptor desc : feature.getType()
						.getAttributeDescriptors()) {
					Object obj = feature.getAttribute(desc.getName());

					if (obj instanceof Geometry)
						continue attributes;
					else if (obj == null)
						vf.setObjAttr(desc.getLocalName(), (String) null);
					else if (obj instanceof Integer)
						vf.setObjAttr(desc.getLocalName(), (Integer) obj);
					else if (obj instanceof Double)
						vf.setObjAttr(desc.getLocalName(), (Double) obj);
					else if (obj instanceof Boolean)
						vf.setObjAttr(desc.getLocalName(), (Double) obj);
					else
						vf.setObjAttr(desc.getLocalName(), obj.toString());
				}
				vectorLayer.addFeatures(vf);
			}
			map.addLayer(vectorLayer);

			map.addObjModCode(map.getJSObjRef() + ".zoomToExtent("
					+ vectorLayer.getJSObjRef() + ".getDataExtent());");
		} catch (Exception e) {
			throw new ArgeoException("Cannot add layer " + layerId, e);
		} finally {
			GeoToolsUtils.closeQuietly(featureIterator);
		}
	}

	/** Normalize to 2 characters */
	private String hexString2Chars(int i) {
		String str = Integer.toHexString(i);
		if (str.length() == 1)
			str = "0" + str;
		return str;
	}

	public void addLayer(String layerId, Collection<?> collection, Object style) {
		// TODO Auto-generated method stub

	}

	public void setAreaOfInterest(ReferencedEnvelope areaOfInterest) {

	}

	public void setStyle(String layerId, Object style) {
		// TODO Auto-generated method stub

	}

	public void removeAllLayers() {
		for (String layerId : vectorLayers.keySet()) {
			map.removeLayer(vectorLayers.get(layerId));
		}
		vectorLayers.clear();
	}

	@Override
	public void setFocus() {
		// TODO make sure dispolay is properly refreshed
		super.setFocus();
	}

}
