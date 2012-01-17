package org.argeo.geotools.jcr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.argeo.geotools.GeoToolsConstants;
import org.argeo.geotools.GeoToolsUtils;
import org.argeo.gis.GisConstants;
import org.argeo.gis.GisNames;
import org.argeo.gis.GisTypes;
import org.argeo.jcr.JcrUtils;
import org.argeo.jts.jcr.JtsJcrUtils;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/** Maps geographical information meta data in a JCR repository. */
public class SimpleGeoJcrMapper implements GeoJcrMapper, GisNames {
	private final static Log log = LogFactory.getLog(SimpleGeoJcrMapper.class);

	private Map<String, DataStore> registeredDataStores = Collections
			.synchronizedSortedMap(new TreeMap<String, DataStore>());

	private Session systemSession;

	public void init() throws RepositoryException {
	}

	public void dispose() {
		if (systemSession != null)
			systemSession.logout();
	}

	public Map<String, List<FeatureSource<SimpleFeatureType, SimpleFeature>>> getPossibleFeatureSources() {
		Map<String, List<FeatureSource<SimpleFeatureType, SimpleFeature>>> res = new TreeMap<String, List<FeatureSource<SimpleFeatureType, SimpleFeature>>>();
		dataStores: for (String alias : registeredDataStores.keySet()) {
			DataStore dataStore = registeredDataStores.get(alias);
			List<Name> names;
			try {
				names = dataStore.getNames();
			} catch (IOException e) {
				log.warn("Cannot list features sources of data store " + alias,
						e);
				continue dataStores;
			}
			List<FeatureSource<SimpleFeatureType, SimpleFeature>> lst = new ArrayList<FeatureSource<SimpleFeatureType, SimpleFeature>>();
			for (Name name : names) {
				try {
					lst.add(dataStore.getFeatureSource(name));
				} catch (IOException e) {
					if (log.isTraceEnabled())
						log.trace("Skipping " + name + " of data store "
								+ alias + " because it is probably"
								+ " not a feature source", e);
				}
			}
			res.put(alias, lst);
		}
		return res;
	}

	// public Node getNode(String dataStoreAlias,
	// FeatureSource<SimpleFeatureType, SimpleFeature> featureSource,
	// SimpleFeature feature) {
	// StringBuffer pathBuf = new StringBuffer(dataStoresBasePath);
	// pathBuf.append('/').append(dataStoreAlias);
	// pathBuf.append('/').append(featureSource.getName());
	//
	// // TODO: use centroid or bbox to create some depth
	// // Geometry geometry = (Geometry)feature.getDefaultGeometry();
	// // Point centroid = geometry.getCentroid();
	//
	// pathBuf.append('/').append(feature.getID());
	//
	// String path = pathBuf.toString();
	// try {
	// if (session.itemExists(path))
	// return session.getNode(path);
	// else
	// return JcrUtils.mkdirs(session, path);
	// } catch (RepositoryException e) {
	// throw new ArgeoException("Cannot get feature node for " + path, e);
	// }
	// }

	public Node getFeatureNode(Node featureSourceNode, String featureId) {
		try {
			if (!featureSourceNode.hasNode(featureId)) {
				FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = getFeatureSource(featureSourceNode);
				SimpleFeature feature = GeoToolsUtils.querySingleFeature(
						featureSource, featureId);
				Node featureNode = featureSourceNode.addNode(featureId);
				featureNode.addMixin(GisTypes.GIS_FEATURE);
				Geometry geometry = (Geometry) feature.getDefaultGeometry();

				// SRS
				String srs;
				CoordinateReferenceSystem crs = featureSource.getSchema()
						.getCoordinateReferenceSystem();
				try {
					Integer epsgCode = CRS.lookupEpsgCode(crs, false);
					if (epsgCode != null)
						srs = "EPSG:" + epsgCode;
					else
						srs = crs.toWKT();
				} catch (FactoryException e) {
					log.warn("Cannot lookup EPSG code", e);
					srs = crs.toWKT();
				}
				featureNode.setProperty(GIS_SRS, srs);

				Polygon bboxPolygon;
				Geometry envelope = geometry.getEnvelope();
				if (envelope instanceof Point) {
					Point pt = (Point) envelope;
					Coordinate[] coords = new Coordinate[4];
					for (int i = 0; i < coords.length; i++)
						coords[i] = pt.getCoordinate();
					bboxPolygon = JtsJcrUtils.getGeometryFactory()
							.createPolygon(
									JtsJcrUtils.getGeometryFactory()
											.createLinearRing(coords), null);
				} else if (envelope instanceof Polygon) {
					bboxPolygon = (Polygon) envelope;
				} else {
					throw new ArgeoException("Unsupported envelope format "
							+ envelope.getClass());
				}
				featureNode.setProperty(GIS_BBOX,
						JtsJcrUtils.writeWkt(bboxPolygon));
				featureNode.setProperty(GIS_CENTROID,
						JtsJcrUtils.writeWkt(geometry.getCentroid()));
				featureSourceNode.getSession().save();
				return featureNode;
			} else {
				return featureSourceNode.getNode(featureId);
			}
		} catch (RepositoryException e) {
			throw new ArgeoException("Cannot get feature node for feature "
					+ featureId + " from " + featureSourceNode, e);
		}
	}

	protected Node getDataStoreNode(Session session, String dataStoreAlias) {
		try {
			// normalize by starting with a '/'
			String path = dataStoreAlias.startsWith("/") ? GisConstants.DATA_STORES_BASE_PATH
					+ dataStoreAlias
					: GisConstants.DATA_STORES_BASE_PATH + '/' + dataStoreAlias;
			Node dataStoreNode = JcrUtils.mkdirs(session, path,
					GisTypes.GIS_DATA_STORE);
			dataStoreNode.setProperty(GIS_ALIAS, dataStoreAlias);
			if (session.hasPendingChanges())
				session.save();
			return dataStoreNode;
		} catch (RepositoryException e) {
			JcrUtils.discardQuietly(session);
			throw new ArgeoException("Cannot get node for data store "
					+ dataStoreAlias, e);
		}
	}

	public Node getFeatureSourceNode(Session session, String dataStoreAlias,
			FeatureSource<SimpleFeatureType, SimpleFeature> featureSource) {
		try {
			// String name = featureSource.getName().toString();
			Name name = featureSource.getName();
			String nodeName = name.getLocalPart();
			Node dataStoreNode = getDataStoreNode(session, dataStoreAlias);
			if (dataStoreNode.hasNode(nodeName))
				return dataStoreNode.getNode(nodeName);
			else {
				Node featureSourceNode = dataStoreNode.addNode(nodeName);
				featureSourceNode.addMixin(GisTypes.GIS_FEATURE_SOURCE);
				featureSourceNode.getSession().save();
				return featureSourceNode;
			}
		} catch (RepositoryException e) {
			throw new ArgeoException(
					"Cannot get feature source node for data store "
							+ dataStoreAlias + " and feature source "
							+ featureSource.getName(), e);
		}
	}

	public FeatureSource<SimpleFeatureType, SimpleFeature> getFeatureSource(
			Node node) {
		try {
			Node dataStoreNode = node.getParent();
			// TODO: check a dataStore type
			if (!dataStoreNode.hasProperty(GIS_ALIAS))
				throw new ArgeoException("Data store " + dataStoreNode
						+ " is not active.");
			String alias = dataStoreNode.getProperty(GIS_ALIAS).getString();
			if (!registeredDataStores.containsKey(alias))
				throw new ArgeoException("No data store registered under "
						+ dataStoreNode);
			DataStore dataStore = registeredDataStores.get(alias);
			return dataStore.getFeatureSource(node.getName());
		} catch (Exception e) {
			throw new ArgeoException("Cannot find feature source " + node, e);
		}
	}

	/** @return the data store registered under this alias or null if not found */
	public DataStore getDataStore(String dataStoreAlias) {
		if (!registeredDataStores.containsKey(dataStoreAlias))
			return null;
		return registeredDataStores.get(dataStoreAlias);
	}

	public SimpleFeatureSource getOrCreateFeatureSource(String dataStoreAlias,
			SimpleFeatureType featureType) {
		try {
			DataStore dataStore = getDataStore(dataStoreAlias);
			if (dataStore == null)
				throw new ArgeoException("No data store with alias "
						+ dataStoreAlias);
			GeoToolsUtils.createSchemaIfNeeded(dataStore, featureType);
			SimpleFeatureSource featureSource = dataStore
					.getFeatureSource(featureType.getName());
			// make sure the node is registered
			getFeatureSourceNode(systemSession, dataStoreAlias, featureSource);
			return featureSource;
		} catch (IOException e) {
			throw new ArgeoException(
					"Cannot get or create feature source from data store "
							+ dataStoreAlias + " for type " + featureType, e);
		}
	}

	public SimpleFeature getFeature(Node node) {
		// TODO Auto-generated method stub
		return null;
	}

	public synchronized void register(DataStore dataStore,
			Map<String, String> properties) {
		if (!properties.containsKey(GeoToolsConstants.ALIAS_KEY)) {
			log.warn("Cannot register data store " + dataStore
					+ " since it has no '" + GeoToolsConstants.ALIAS_KEY
					+ "' property");
			return;
		}
		String alias = properties.get(GeoToolsConstants.ALIAS_KEY);
		Node dataStoreNode = getDataStoreNode(systemSession, alias);
		try {
			dataStoreNode.setProperty(GIS_ALIAS, alias);

			// TODO synchronize namespace if registered
			for (Name name : dataStore.getNames()) {
				String sourceName = name.getLocalPart();
				if (!dataStoreNode.hasNode(sourceName)) {
					Node featureSourceNode = dataStoreNode.addNode(sourceName);
					featureSourceNode.addMixin(GisTypes.GIS_FEATURE_SOURCE);
				}
			}

			// TODO check feature sources which are registered but not available
			// anymore
			systemSession.save();
			registeredDataStores.put(alias, dataStore);
			JcrUtils.discardQuietly(systemSession);
		} catch (Exception e) {
			throw new ArgeoException("Cannot register data store " + alias
					+ ", " + dataStore, e);
		}
	}

	public synchronized void unregister(DataStore dataStore,
			Map<String, String> properties) {
		if (!properties.containsKey(GeoToolsConstants.ALIAS_KEY)) {
			log.warn("Cannot unregister data store " + dataStore
					+ " since it has no '" + GeoToolsConstants.ALIAS_KEY
					+ "' property");
			return;
		}

		if (!systemSession.isLive())
			return;

		String alias = properties.get(GeoToolsConstants.ALIAS_KEY);
		registeredDataStores.remove(alias);
		Node dataStoreNode = getDataStoreNode(systemSession, alias);
		try {
			dataStoreNode.getProperty(GIS_ALIAS).remove();
			systemSession.save();
		} catch (RepositoryException e) {
			JcrUtils.discardQuietly(systemSession);
			throw new ArgeoException("Cannot unregister data store " + alias
					+ ", " + dataStore, e);
		}
	}

	/** Expects to own this session (will be logged out on dispose) */
	public void setSystemSession(Session systemSession) {
		this.systemSession = systemSession;
	}

}
