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
package org.argeo.gis.ui.rcp.swing;

import java.awt.Color;
import java.awt.Frame;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.argeo.geotools.StylingUtils;
import org.argeo.geotools.jcr.GeoJcrMapper;
import org.argeo.gis.ui.AbstractMapViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.geotools.data.FeatureSource;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.referencing.CRS;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.Style;
import org.geotools.swing.JMapPane;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/** Map viewer implementation based on GeoTools Swing components. */
@SuppressWarnings("deprecation")
public class SwingMapViewer extends AbstractMapViewer {
	private final static Log log = LogFactory.getLog(SwingMapViewer.class);

	private Composite embedded;
	private JMapPane mapPane;
	private VersatileZoomTool versatileZoomTool;

	private Map<String, MapLayer> mapLayers = Collections
			.synchronizedMap(new HashMap<String, MapLayer>());

	public SwingMapViewer(Node context, GeoJcrMapper geoJcrMapper,
			Composite parent) {
		super(context, geoJcrMapper);

		embedded = new Composite(parent, SWT.EMBEDDED | SWT.NO_BACKGROUND);
		Frame frame = SWT_AWT.new_Frame(embedded);

		MapContext mapContext = new DefaultMapContext();
		// dummy call to make sure that the layers are initialized
		mapContext.layers();
		mapPane = new JMapPane(new StreamingRenderer(), mapContext);
		versatileZoomTool = new VersatileZoomTool();
		mapPane.setCursorTool(versatileZoomTool);
		mapPane.setBackground(Color.WHITE);
		frame.add(mapPane);

		setControl(embedded);

		printDisplayCrs();
	}

	private void printDisplayCrs() {
		if (log.isDebugEnabled()) {
			CoordinateReferenceSystem crs = mapPane.getMapContext()
					.getCoordinateReferenceSystem();
			log.debug("Display CRS: "
					+ (crs != null ? crs.getName() : "<null>"));
		}
	}

	@Override
	protected void addFeatureSource(String layerId,
			FeatureSource<SimpleFeatureType, SimpleFeature> featureSource,
			Object style) {
		if (style == null)
			throw new ArgeoException("Style must be specified");
		if (log.isDebugEnabled()) {
			CoordinateReferenceSystem crs = featureSource.getInfo().getCRS();
			log.debug("Add layer '" + layerId + "', " + crs.getName());
		}
		MapLayer mapLayer = new DefaultMapLayer(featureSource, (Style) style);
		addMapLayer(layerId, mapLayer);
		printDisplayCrs();
	}

	@Override
	public void addLayer(String layerId,
			FeatureIterator<SimpleFeature> featureIterator, Object style) {
		if (!featureIterator.hasNext())
			return;
		SimpleFeature firstFeature = featureIterator.next();
		FeatureCollection<SimpleFeatureType, SimpleFeature> coll = new DefaultFeatureCollection(
				layerId, firstFeature.getFeatureType());
		coll.add(firstFeature);
		while (featureIterator.hasNext())
			coll.add(featureIterator.next());

		// Style geoStyle = StylingUtils.createPointStyle("Square", "RED", 4,
		// "BLACK", 1);
		MapLayer mapLayer = new DefaultMapLayer(coll, (Style) style);
		addMapLayer(layerId, mapLayer);
	}

	protected void addMapLayer(String layerId, MapLayer mapLayer) {
		mapLayers.put(layerId, mapLayer);
		mapPane.getMapContext().addLayer(mapLayer);
	}

	public void addLayer(String layerId, Collection<?> collection, Object style) {
		if (style == null)
			style = StylingUtils.createLineStyle("BLACK", 1);
		MapLayer mapLayer = new DefaultMapLayer(collection, (Style) style);
		addMapLayer(layerId, mapLayer);
	}

	public void setStyle(String layerId, Object style) {
		mapLayers.get(layerId).setStyle((Style) style);
	}

	public void setAreaOfInterest(ReferencedEnvelope areaOfInterest) {
		// mapPane.getMapContext().setAreaOfInterest(areaOfInterest);
		CoordinateReferenceSystem crs = mapPane.getMapContext()
				.getCoordinateReferenceSystem();

		ReferencedEnvelope toDisplay;
		if (crs != null)
			try {
				toDisplay = areaOfInterest.transform(crs, true);
			} catch (Exception e) {
				throw new ArgeoException("Cannot reproject " + areaOfInterest,
						e);
			}
		else
			toDisplay = areaOfInterest;
		mapPane.setDisplayArea(toDisplay);
	}

	public void setCoordinateReferenceSystem(String crs) {
		try {
			CoordinateReferenceSystem crsObj = CRS.decode(crs);
			mapPane.getMapContext().setCoordinateReferenceSystem(crsObj);
			mapPane.repaint();
			printDisplayCrs();
		} catch (Exception e) {
			throw new ArgeoException("Cannot set CRS '" + crs + "'", e);
		}

	}

}
