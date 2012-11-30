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
package org.argeo.gis.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.argeo.ArgeoException;
import org.argeo.geotools.jcr.GeoJcrMapper;
import org.argeo.geotools.jcr.GeoJcrUtils;
import org.argeo.gis.GisNames;
import org.argeo.gis.GisTypes;
import org.argeo.jcr.CollectionNodeIterator;
import org.argeo.jts.jcr.JtsJcrUtils;
import org.eclipse.swt.widgets.Composite;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Logic of a map viewer which is independent from a particular map display
 * implementation.
 */
public abstract class AbstractMapViewer implements MapViewer {
	private final Node context;
	private final GeoJcrMapper geoJcrMapper;

	private Composite control;
	private Map<String, Set<String>> selected = new HashMap<String, Set<String>>();

	private CoordinateReferenceSystem mapProjection;
	// private GeometryFactory geometryFactory = JTSFactoryFinder
	// .getGeometryFactory(null);
	// private MathTransform reprojection;

	private Set<MapViewerListener> listeners = Collections
			.synchronizedSet(new HashSet<MapViewerListener>());

	protected abstract void addFeatureSource(String layerId,
			FeatureSource<SimpleFeatureType, SimpleFeature> featureSource,
			Object style);

	public abstract void addLayer(String layerId,
			FeatureIterator<SimpleFeature> featureIterator, Object style);

	public AbstractMapViewer(Node context, GeoJcrMapper geoJcrMapper) {
		super();
		this.context = context;
		this.geoJcrMapper = geoJcrMapper;

		// default is WGS84
		setMapProjection("EPSG:4326");
	}

	public void addLayer(Node layer, Object style) {
		try {
			if (layer.isNodeType(GisTypes.GIS_FEATURE_SOURCE)) {
				addFeatureSource(layer.getPath(),
						geoJcrMapper.getFeatureSource(layer), style);
			} else {
				throw new ArgeoException("Unsupported layer " + layer);
			}
		} catch (Exception e) {
			throw new ArgeoException("Cannot add layer " + layer, e);
		}

	}

	public void addLayer(String layerId, final NodeIterator layer, Object style) {
		FeatureIterator<SimpleFeature> featureIterator = new FeatureIterator<SimpleFeature>() {

			public boolean hasNext() {
				return layer.hasNext();
			}

			public SimpleFeature next() throws NoSuchElementException {
				Node node = layer.nextNode();
				return nodeToFeature(node);
			}

			public void close() {
			}

		};
		addLayer(layerId, featureIterator, style);
	}

	protected SimpleFeature nodeToFeature(Node node) {
		try {
			SimpleFeatureTypeBuilder sftb = new SimpleFeatureTypeBuilder();
			sftb.setName(node.getName().replace(':', '_'));

			List<Object> values = new ArrayList<Object>();

			Geometry geom = null;
			NodeIterator children = node.getNodes();
			while (children.hasNext()) {
				Node child = children.nextNode();
				if (child.isNodeType(GisTypes.GIS_GEOMETRY)) {
					geom = JtsJcrUtils.readWkFormat(child
							.getProperty(GisNames.GIS_WKT));
					sftb.add(child.getName().replace(':', '_'),
							geom.getClass(),
							GeoJcrUtils.getCoordinateReferenceSystem(child));
					break;
				}
			}

			if (geom == null)
				throw new ArgeoException("No geometry under " + node);
			values.add(geom);

			sftb.add("path", String.class);
			values.add(node.getPath());

			// PropertyIterator pit = parent.getProperties();
			// while (pit.hasNext()) {
			// Property p = pit.nextProperty();
			// // TODO: typing
			// sftb.add(p.getName().replace(':', '_'), String.class);
			// values.add(p.getString());
			// }

			// sftb.add("thumbnail", String.class);
			// values.add(parent.getProperty("gr:siteType").getString()
			// + ".gif");

			SimpleFeature sf = SimpleFeatureBuilder.build(
					sftb.buildFeatureType(), values.toArray(),
					node.getIdentifier());
			sf.setDefaultGeometry(geom);
			return sf;

		} catch (RepositoryException e) {
			throw new ArgeoException("Cannot convert node " + node
					+ " to feature", e);
		}
	}

	protected Geometry getReprojectedGeometry(SimpleFeature feature) {
		CoordinateReferenceSystem crs = feature.getDefaultGeometryProperty()
				.getDescriptor().getCoordinateReferenceSystem();
		if (!crs.equals(mapProjection)) {
			try {
				MathTransform reprojection = CRS.findMathTransform(crs,
						mapProjection);
				return JTS.transform((Geometry) feature.getDefaultGeometry(),
						reprojection);
			} catch (Exception e) {
				throw new ArgeoException("Cannot reproject " + feature
						+ " from " + crs + " to " + mapProjection, e);
			}

		} else {
			return (Geometry) feature.getDefaultGeometry();
		}
	}

	protected Envelope getReprojectedEnvelope(CoordinateReferenceSystem crs,
			Envelope envelope) {
		if (!crs.equals(mapProjection)) {
			try {
				MathTransform reprojection = CRS.findMathTransform(crs,
						mapProjection);
				return JTS.transform(envelope, reprojection);
			} catch (Exception e) {
				throw new ArgeoException("Cannot reproject " + envelope
						+ " from " + crs + " to " + mapProjection, e);
			}

		} else {
			return envelope;
		}
	}

	// protected Point reproject(Coordinate coordinate) {
	// if (reprojection != null) {
	// try {
	// // invert order
	// DirectPosition2D pos = new DirectPosition2D(wgs84,
	// coordinate.y, coordinate.x);
	// DirectPosition targetPos = reprojection.transform(pos, null);
	// Coordinate targetCoordinate = new Coordinate(
	// targetPos.getOrdinate(0), targetPos.getOrdinate(1));
	// return geometryFactory.createPoint(targetCoordinate);
	// } catch (Exception e) {
	// throw new ArgeoException("Cannot reproject " + coordinate, e);
	// }
	// } else {
	// return geometryFactory.createPoint(coordinate);
	// }
	// }

	public NodeIterator getSelectedFeatures() {
		try {
			List<Node> nodes = new ArrayList<Node>();
			for (String layerId : selected.keySet()) {
				Set<String> featureIds = selected.get(layerId);
				Node featureSource = context.getSession().getNode(layerId);
				for (String featureId : featureIds) {
					Node featureNode = geoJcrMapper.getFeatureNode(
							featureSource, featureId);
					nodes.add(featureNode);
				}
			}
			return new CollectionNodeIterator(nodes);
		} catch (RepositoryException e) {
			throw new ArgeoException("Cannot get selected features from "
					+ context, e);
		}
	}

	public void addMapViewerListener(MapViewerListener listener) {
		listeners.add(listener);
	}

	public void removeMapViewerListener(MapViewerListener listener) {
		listeners.remove(listener);
	}

	protected Node getContext() {
		return context;
	}

	protected Map<String, Set<String>> getSelected() {
		return selected;
	}

	protected Set<MapViewerListener> getListeners() {
		return listeners;
	}

	protected void setControl(Composite control) {
		this.control = control;
	}

	public Composite getControl() {
		return control;
	}

	public GeoJcrMapper getGeoJcrMapper() {
		return geoJcrMapper;
	}

	public void setFocus() {
		getControl().setFocus();
	}

	public void setMapProjection(String srs) {
		try {
			// mapProjection = CRS.decode("EPSG:3857");
			mapProjection = CRS.decode(srs);
		} catch (Exception e) {
			throw new ArgeoException("Cannot define default map projection", e);
		}
	}

	public void removeAllLayers() {
		// FIXME implement in RCP
	}
}
