package org.argeo.connect.gpx;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.argeo.connect.BeanFeatureTypeBuilder;
import org.argeo.geotools.jcr.GeoJcrMapper;
import org.argeo.gis.GisConstants;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.referencing.CRS;
import org.geotools.referencing.GeodeticCalculator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.xml.sax.InputSource;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

/**
 * Parses a GPX track file and import the data
 * 
 * On PostGIS, a useful index is:
 * <code>CREATE INDEX track_speeds_line ON track_speeds USING GIST (line)</code>
 */
public class GeoToolsTrackDao implements TrackDao {
	private final static Log log = LogFactory.getLog(GeoToolsTrackDao.class);

	private Integer targetSrid = 4326;
	private Float maxSpeed = 200f;

	private GeodeticCalculator geodeticCalculator;
	private FilterFactory filterFactory = CommonFactoryFinder
			.getFilterFactory(null);

	private String dataStoreAlias;
	private GeoJcrMapper geoJcrMapper;

	// private String trackSpeedsToCleanTable = "toclean_track_speeds";
	// private BeanFeatureTypeBuilder<TrackSpeed> trackSpeedType;
	// private FeatureStore<SimpleFeatureType, SimpleFeature> trackSpeedsStore;

	// private String positionsTable = "connect_positions";
	// private BeanFeatureTypeBuilder<TrackPoint> positionType;
	// private FeatureStore<SimpleFeatureType, SimpleFeature> positionStore;

	public GeoToolsTrackDao() {
	}

	private String addGpsCleanTablePrefix(String baseName){
		return "connect_gpsclean_" + baseName;
	}

	private String addPositionTablePrefix(String baseName){
		return "connect_positions_" + baseName;
	}
	// public void init() {
	// trackSpeedType = new BeanFeatureTypeBuilder<TrackSpeed>(
	// trackSpeedsToCleanTable, TrackSpeed.class);
	// positionType = new BeanFeatureTypeBuilder<TrackPoint>(positionsTable,
	// TrackPoint.class);
	//
	// trackSpeedsStore = getFeatureStore(trackSpeedType);
	// positionStore = getFeatureStore(positionType);
	// }

	public Object importRawToCleanSession(String cleanSession, String sensor,
			InputStream in) {
		long begin = System.currentTimeMillis();
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			spf.setValidating(false);
			SAXParser sp = spf.newSAXParser();
			InputSource input = new InputSource(in);
			geodeticCalculator = new GeodeticCalculator(CRS.decode("EPSG:"
					+ targetSrid));
			TrackGpxHandler handler = new TrackGpxHandler(sensor, targetSrid,
					cleanSession);
			sp.parse(input, handler);
			return null;
		} catch (Exception e) {
			throw new ArgeoException("Cannot parse GPX stream", e);
		} finally {
			IOUtils.closeQuietly(in);
			long duration = System.currentTimeMillis() - begin;
			if (log.isDebugEnabled())
				log.debug("Gpx file imported to table " + addGpsCleanTablePrefix(cleanSession) + " with sensor '"
						+ sensor + "' in " + (duration) + " ms");
		}
	}

	public void publishCleanPositions(String cleanSession, String referential,
			String toRemoveCql) {
		try {
			String trackSpeedsToCleanTable = addGpsCleanTablePrefix(cleanSession);
			BeanFeatureTypeBuilder<TrackSpeed> trackSpeedType = new BeanFeatureTypeBuilder<TrackSpeed>(
					trackSpeedsToCleanTable, TrackSpeed.class);
			String positionsTable = addPositionTablePrefix(referential);
			BeanFeatureTypeBuilder<TrackPoint> positionType = new BeanFeatureTypeBuilder<TrackPoint>(
					positionsTable, TrackPoint.class);

			SimpleFeatureCollection positions = FeatureCollections
					.newCollection();

			SimpleFeatureStore trackSpeedsStore = getFeatureStore(trackSpeedType);
			Filter filter = filterFactory.not(CQL.toFilter(toRemoveCql));
			FeatureIterator<SimpleFeature> filteredSpeeds = trackSpeedsStore
					.getFeatures(filter).features();
			while (filteredSpeeds.hasNext()) {
				SimpleFeature speed = filteredSpeeds.next();
				SimpleFeature position = positionType.convertFeature(speed);
				positions.add(position);
			}

			// persist
			Transaction transaction = new DefaultTransaction();
			SimpleFeatureStore positionStore = getFeatureStore(positionType);
			positionStore.setTransaction(transaction);
			try {
				positionStore.addFeatures(positions);
				transaction.commit();
			} catch (Exception e) {
				transaction.rollback();
				throw new ArgeoException("Cannot persist changes", e);
			} finally {
				transaction.close();
				positions.clear();
			}
		} catch (Exception e) {
			throw new ArgeoException("Cannot copy speeds to positions", e);
		}
	}

	protected void processTrackSegment(
			BeanFeatureTypeBuilder<TrackSpeed> trackSpeedType,
			TrackSegment trackSegment, GeometryFactory geometryFactory) {
		// FeatureCollection<SimpleFeatureType, SimpleFeature> trackPointsToAdd
		// = FeatureCollections
		// .newCollection();
		// FeatureCollection<SimpleFeatureType, SimpleFeature>
		// trackSegmentsToAdd = FeatureCollections
		// .newCollection();
		FeatureCollection<SimpleFeatureType, SimpleFeature> trackSpeedsToAdd = FeatureCollections
				.newCollection();

		if (trackSegment.getTrackPoints().size() == 0) {
			// no track points
			return;
		} else if (trackSegment.getTrackPoints().size() == 1) {
			// single track points
			// TrackPoint trackPoint = trackSegment.getTrackPoints().get(0);
			// SimpleFeature trackPointFeature = trackPointType
			// .buildFeature(trackPoint);
			// trackPointsToAdd.add(trackPointFeature);
			return;
		}

		// multiple trackpoints
		TrackSpeed currentTrackSpeed = null;
		// List<Coordinate> coords = new ArrayList<Coordinate>();
		trackPoints: for (int i = 0; i < trackSegment.getTrackPoints().size(); i++) {
			TrackPoint trackPoint = trackSegment.getTrackPoints().get(i);

			// map to features
			// trackPointsToAdd.add(trackPointType.buildFeature(trackPoint));

			// coords.add(new Coordinate(trackPoint.getPosition().getX(),
			// trackPoint.getPosition().getY()));

			if (i == 0)
				trackSegment.setStartUtc(trackPoint.getUtcTimestamp());

			if (i == trackSegment.getTrackPoints().size() - 1)
				trackSegment.setEndUtc(trackPoint.getUtcTimestamp());
			else {
				// order 1 coefficients (speed)
				TrackPoint next = trackSegment.getTrackPoints().get(i + 1);

				Coordinate[] crds = { trackPoint.getPosition().getCoordinate(),
						next.getPosition().getCoordinate() };
				LineString line = geometryFactory.createLineString(crds);
				Long duration = next.getUtcTimestamp().getTime()
						- trackPoint.getUtcTimestamp().getTime();
				if (duration < 0) {
					log.warn("Duration " + duration + " is negative between "
							+ trackPoint.getPosition() + " and "
							+ next.getPosition()
							+ ", skipping speed computation");
					currentTrackSpeed = null;
					continue trackPoints;
				}
				TrackSpeed trackSpeed = new TrackSpeed(trackPoint, line,
						duration, geodeticCalculator);
				if (trackSpeed.getSpeed() > maxSpeed) {
					log.warn("Speed " + trackSpeed.getSpeed() + " is above "
							+ maxSpeed + " between " + trackPoint.getPosition()
							+ " and " + next.getPosition()
							+ ", skipping speed computation");
					currentTrackSpeed = null;
					continue trackPoints;
				}

				// order 2 coefficients (acceleration, azimuth variation)
				if (currentTrackSpeed != null) {
					// compute acceleration (in m/s²)
					Double speed1 = trackSpeed.getDistance()
							/ (trackSpeed.getDuration() / 1000);
					Double speed2 = currentTrackSpeed.getDistance()
							/ (currentTrackSpeed.getDuration() / 1000);
					Double acceleration = (speed1 - speed2)
							/ (currentTrackSpeed.getDuration() / 1000);
					trackSpeed.setAcceleration(acceleration);

					Double azimuthVariation = convertAzimuth(trackSpeed
							.getAzimuth())
							- convertAzimuth(currentTrackSpeed.getAzimuth());
					if (azimuthVariation > 180)
						azimuthVariation = -(360 - azimuthVariation);
					else if (azimuthVariation < -180)
						azimuthVariation = (360 + azimuthVariation);
					trackSpeed.setAzimuthVariation(azimuthVariation);
				}
				trackSegment.getTrackSpeeds().add(trackSpeed);
				// map to features
				trackSpeedsToAdd.add(trackSpeedType.buildFeature(trackSpeed));
				currentTrackSpeed = trackSpeed;
			}

		}
		// LineString segment = geometryFactory.createLineString(coords
		// .toArray(new Coordinate[coords.size()]));
		// trackSegment.setSegment(segment);
		// trackSegmentsToAdd.add(trackSegmentType.buildFeature(trackSegment));

		// persist
		SimpleFeatureStore trackSpeedsStore = getFeatureStore(trackSpeedType);
		try {
			Transaction transaction = new DefaultTransaction();
			// trackPointsStore.setTransaction(transaction);
			trackSpeedsStore.setTransaction(transaction);
			// trackSegmentsStore.setTransaction(transaction);
			try {
				// trackPointsStore.addFeatures(trackPointsToAdd);
				trackSpeedsStore.addFeatures(trackSpeedsToAdd);
				// trackSegmentsStore.addFeatures(trackSegmentsToAdd);
				transaction.commit();
			} catch (Exception e) {
				transaction.rollback();
				throw new ArgeoException("Cannot persist changes", e);
			} finally {
				transaction.close();
				// trackPointsToAdd.clear();
				trackSpeedsToAdd.clear();
				// trackSegmentsToAdd.clear();
			}
		} catch (ArgeoException e) {
			throw e;
		} catch (IOException e) {
			throw new ArgeoException("Unexpected issue with the transaction", e);
		}
	}

	protected SimpleFeatureStore getFeatureStore(BeanFeatureTypeBuilder<?> type) {
		SimpleFeatureType featureType = type.getFeatureType();
		return (SimpleFeatureStore) geoJcrMapper.getOrCreateFeatureSource(
				dataStoreAlias, featureType);
		// GeoToolsUtils.createSchemaIfNeeded(dataStore, featureType);
		// return GeoToolsUtils.getFeatureStore(dataStore,
		// featureType.getName());
	}

	/** Normalize from [-180°,180°] to [0°,360°] */
	private Double convertAzimuth(Double azimuth) {
		if (azimuth < 0)
			return 360d + azimuth;
		else
			return azimuth;
	}

	public void setTargetSrid(Integer targetSrid) {
		this.targetSrid = targetSrid;
	}

	public void setMaxSpeed(Float maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	protected String getTrackSpeedsTable(String cleanSession) {
		return "connect_gpsclean_" + cleanSession;
	}

	public String getTrackSpeedsSource(String cleanSession) {
		return GisConstants.DATA_STORES_BASE_PATH + "/" + dataStoreAlias + "/"
				+ getTrackSpeedsTable(cleanSession);
	}

	public void setDataStoreAlias(String dataStoreAlias) {
		this.dataStoreAlias = dataStoreAlias;
	}

	public void setGeoJcrMapper(GeoJcrMapper geoJcrMapper) {
		this.geoJcrMapper = geoJcrMapper;
	}

	public class TrackGpxHandler extends GpxHandler {
		private BeanFeatureTypeBuilder<TrackSpeed> trackSpeedType;

		public TrackGpxHandler(String sensor, Integer srid, String cleanSession) {
			super(sensor, srid);
			//String trackSpeedsToCleanTable = "connect_gpsclean_" + cleanSession;
			trackSpeedType = new BeanFeatureTypeBuilder<TrackSpeed>(
					addGpsCleanTablePrefix(cleanSession), TrackSpeed.class);
		}

		@Override
		protected void processTrackSegment(TrackSegment trackSegment,
				GeometryFactory geometryFactory) {
			GeoToolsTrackDao.this.processTrackSegment(trackSpeedType,
					trackSegment, geometryFactory);

		}
	}

}
