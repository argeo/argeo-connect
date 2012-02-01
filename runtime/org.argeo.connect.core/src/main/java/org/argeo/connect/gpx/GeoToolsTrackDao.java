package org.argeo.connect.gpx;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.GeodeticCalculator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.xml.sax.InputSource;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;

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

	private String addGpsCleanTablePrefix(String baseName) {
		return "connect_gpsclean_" + baseName;
	}

	private String addPositionsTablePrefix(String baseName) {
		return "connect_positions_" + baseName;
	}

	private String addPositionsDisplayTablePrefix(String baseName) {
		return "connect_positions_display_" + baseName;
	}

	public GeoToolsTrackDao() {
	}

	public List<String> importRawToCleanSession(String cleanSession,
			String sensor, InputStream in) {
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
			return handler.getSegmentUuids();
		} catch (Exception e) {
			throw new ArgeoException("Cannot parse GPX stream", e);
		} finally {
			IOUtils.closeQuietly(in);
			long duration = System.currentTimeMillis() - begin;
			if (log.isDebugEnabled())
				log.debug("Gpx file imported to table "
						+ addGpsCleanTablePrefix(cleanSession)
						+ " with sensor '" + sensor + "' in " + (duration)
						+ " ms");
		}
	}

	public void publishCleanPositions(String cleanSession, String referential,
			String toRemoveCql) {
		try {
			int srid = 3857;
			// set up the math transform used to process the data
			CoordinateReferenceSystem wgs84 = CRS.decode("EPSG:4326");
			CoordinateReferenceSystem popularMercator = CRS.decode("EPSG:"
					+ srid);
			MathTransform transform = CRS.findMathTransform(wgs84,
					popularMercator);
			PrecisionModel precisionModel = new PrecisionModel();
			GeometryFactory targetGF = new GeometryFactory(precisionModel, srid);

			String trackSpeedsToCleanTable = addGpsCleanTablePrefix(cleanSession);
			BeanFeatureTypeBuilder<TrackSpeed> trackSpeedType = new BeanFeatureTypeBuilder<TrackSpeed>(
					trackSpeedsToCleanTable, TrackSpeed.class);
			String positionsTable = addPositionsTablePrefix(referential);
			BeanFeatureTypeBuilder<TrackPoint> positionType = new BeanFeatureTypeBuilder<TrackPoint>(
					positionsTable, TrackPoint.class);
			String positionsDisplayTable = addPositionsDisplayTablePrefix(referential);
			BeanFeatureTypeBuilder<TrackSegment> positionDisplayType = new BeanFeatureTypeBuilder<TrackSegment>(
					positionsDisplayTable, TrackSegment.class, popularMercator);

			SimpleFeatureCollection positions = FeatureCollections
					.newCollection();
			SimpleFeatureCollection segments = FeatureCollections
					.newCollection();

			SimpleFeatureStore trackSpeedsStore = getFeatureStore(trackSpeedType);
			Filter filter = filterFactory.not(CQL.toFilter(toRemoveCql));
			FeatureIterator<SimpleFeature> filteredSpeeds = trackSpeedsStore
					.getFeatures(filter).features();

			List<Coordinate> currSegmentCoords = new ArrayList<Coordinate>();
			TrackSegment currSegment = null;
			while (filteredSpeeds.hasNext()) {
				SimpleFeature speed = filteredSpeeds.next();
				SimpleFeature position = positionType.convertFeature(speed);
				positions.add(position);

				// segmet
				String segmentUuid = (String) position
						.getAttribute("segmentUuid");
				if (currSegment == null) {
					currSegment = startNewSegment(position);
				}

				if (!currSegment.getUuid().equals(segmentUuid)) {
					if (currSegmentCoords.size() >= 2) {// skip single point
						Coordinate[] line = currSegmentCoords
								.toArray(new Coordinate[currSegmentCoords
										.size()]);
						LineString segment = targetGF.createLineString(line);
						currSegment.setSegment(segment);
						Date end = (Date) position.getAttribute("utcTimestamp");
						currSegment.setEndUtc(end);
						segments.add(positionDisplayType
								.buildFeature(currSegment));
					}
					currSegment = startNewSegment(position);
					currSegmentCoords.clear();
				}

				// reproject point
				Point point = (Point) position.getDefaultGeometry();
				Coordinate coor = new Coordinate(point.getX(), point.getY());
				Coordinate targetCoor = new Coordinate();
				JTS.transform(coor, targetCoor, transform);
				currSegmentCoords.add(targetCoor);

			}

			// persist
			Transaction transaction = new DefaultTransaction();
			SimpleFeatureStore positionStore = getFeatureStore(positionType);
			SimpleFeatureStore positionDisplayStore = getFeatureStore(positionDisplayType);
			positionStore.setTransaction(transaction);
			positionDisplayStore.setTransaction(transaction);
			try {
				positionStore.addFeatures(positions);
				positionDisplayStore.addFeatures(segments);
				transaction.commit();
			} catch (Exception e) {
				transaction.rollback();
				throw new ArgeoException("Cannot persist changes", e);
			} finally {
				transaction.close();
				positions.clear();
				segments.clear();
			}
		} catch (Exception e) {
			throw new ArgeoException("Cannot copy speeds to positions", e);
		}
	}

	private TrackSegment startNewSegment(SimpleFeature position) {
		String sensor = (String) position.getAttribute("sensor");
		String segmentUuid = (String) position.getAttribute("segmentUuid");
		Date utcTimestamp = (Date) position.getAttribute("utcTimestamp");
		TrackSegment currSegment = new TrackSegment();
		currSegment.setSensor(sensor);
		currSegment.setUuid(segmentUuid);
		currSegment.setStartUtc(utcTimestamp);
		return currSegment;
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

	public void deleteCleanPositions(String referential,
			List<String> segmentUuuids) {
		// TODO Auto-generated method stub

	}

	public void setTargetSrid(Integer targetSrid) {
		this.targetSrid = targetSrid;
	}

	public void setMaxSpeed(Float maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	public String getTrackSpeedsSource(String cleanSession) {
		return GisConstants.DATA_STORES_BASE_PATH + "/" + dataStoreAlias + "/"
				+ addGpsCleanTablePrefix(cleanSession);
	}

	public String getPositionsSource(String positionsRepositoryName) {
		return GisConstants.DATA_STORES_BASE_PATH + "/" + dataStoreAlias + "/"
				+ addPositionsTablePrefix(positionsRepositoryName);
	}

	public String getPositionsDisplaySource(String positionsRepositoryName) {
		return GisConstants.DATA_STORES_BASE_PATH + "/" + dataStoreAlias + "/"
				+ addPositionsDisplayTablePrefix(positionsRepositoryName);
	}

	public void setDataStoreAlias(String dataStoreAlias) {
		this.dataStoreAlias = dataStoreAlias;
	}

	public void setGeoJcrMapper(GeoJcrMapper geoJcrMapper) {
		this.geoJcrMapper = geoJcrMapper;
	}

	public class TrackGpxHandler extends GpxHandler {
		private BeanFeatureTypeBuilder<TrackSpeed> trackSpeedType;
		private List<String> segmentUuids = new ArrayList<String>();

		public TrackGpxHandler(String sensor, Integer srid, String cleanSession) {
			super(sensor, srid);
			// String trackSpeedsToCleanTable = "connect_gpsclean_" +
			// cleanSession;
			trackSpeedType = new BeanFeatureTypeBuilder<TrackSpeed>(
					addGpsCleanTablePrefix(cleanSession), TrackSpeed.class);
		}

		@Override
		protected void processTrackSegment(TrackSegment trackSegment,
				GeometryFactory geometryFactory) {
			GeoToolsTrackDao.this.processTrackSegment(trackSpeedType,
					trackSegment, geometryFactory);
			segmentUuids.add(trackSegment.getUuid());
		}

		public List<String> getSegmentUuids() {
			return segmentUuids;
		}

	}
}