package org.argeo.connect.gpx;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.argeo.connect.BeanFeatureTypeBuilder;
import org.argeo.geotools.GeoToolsUtils;
import org.geotools.data.DataStore;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.referencing.CRS;
import org.geotools.referencing.GeodeticCalculator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
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

	private DataStore dataStore;

	private String trackPointsToCleanTable = "toclean_track_points";
	private String trackSpeedsToCleanTable = "toclean_track_speeds";
	private String trackSegmentsToCleanTable = "toclean_track_segments";

	private BeanFeatureTypeBuilder<TrackPoint> trackPointType;
	private BeanFeatureTypeBuilder<TrackSegment> trackSegmentType;
	private BeanFeatureTypeBuilder<TrackSpeed> trackSpeedType;

	private FeatureStore<SimpleFeatureType, SimpleFeature> trackPointsStore;
	private FeatureStore<SimpleFeatureType, SimpleFeature> trackSegmentsStore;
	private FeatureStore<SimpleFeatureType, SimpleFeature> trackSpeedsStore;

	public GeoToolsTrackDao() {
	}

	public void init() {
		trackPointType = new BeanFeatureTypeBuilder<TrackPoint>(
				trackPointsToCleanTable, TrackPoint.class);
		trackSegmentType = new BeanFeatureTypeBuilder<TrackSegment>(
				trackSpeedsToCleanTable, TrackSegment.class);
		trackSpeedType = new BeanFeatureTypeBuilder<TrackSpeed>(
				trackSegmentsToCleanTable, TrackSpeed.class);

		trackPointsStore = getFeatureStore(trackPointType);
		trackSegmentsStore = getFeatureStore(trackSegmentType);
		trackSpeedsStore = getFeatureStore(trackSpeedType);
	}

	protected FeatureStore<SimpleFeatureType, SimpleFeature> getFeatureStore(
			BeanFeatureTypeBuilder<?> type) {
		SimpleFeatureType featureType = type.getFeatureType();
		GeoToolsUtils.createSchemaIfNeeded(dataStore, featureType);
		return GeoToolsUtils.getFeatureStore(dataStore, featureType.getName());
	}

	public Object importTrackPoints(String source, String sensor, InputStream in) {
		long begin = System.currentTimeMillis();
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			spf.setValidating(false);
			SAXParser sp = spf.newSAXParser();
			InputSource input = new InputSource(in);
			geodeticCalculator = new GeodeticCalculator(CRS.decode("EPSG:"
					+ targetSrid));
			TrackGpxHandler handler = new TrackGpxHandler(sensor, targetSrid);
			sp.parse(input, handler);
			return null;
		} catch (Exception e) {
			throw new ArgeoException("Cannot parse GPX stream", e);
		} finally {
			IOUtils.closeQuietly(in);
			long duration = System.currentTimeMillis() - begin;
			if (log.isDebugEnabled())
				log.debug("Imported " + source + " from sensor '" + sensor
						+ "' in " + (duration) + " ms");
		}
	}

	// protected SimpleFeatureType createTrackPointType() {
	// SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
	// // builder.setNamespaceURI(GIS_NAMESPACE);
	// builder.setName("TRACK_POINTS");
	//
	// builder.setDefaultGeometry("location");
	// builder.add("location", Point.class);
	//
	// builder.add("segmentUuid", String.class);
	//
	// return builder.buildFeatureType();
	// }

	protected void processTrackSegment(TrackSegment trackSegment,
			GeometryFactory geometryFactory) {

		FeatureCollection<SimpleFeatureType, SimpleFeature> trackPointsToAdd = FeatureCollections
				.newCollection();
		FeatureCollection<SimpleFeatureType, SimpleFeature> trackSegmentsToAdd = FeatureCollections
				.newCollection();
		FeatureCollection<SimpleFeatureType, SimpleFeature> trackSpeedsToAdd = FeatureCollections
				.newCollection();

		if (trackSegment.getTrackPoints().size() == 0) {
			// no track points
			return;
		} else if (trackSegment.getTrackPoints().size() == 1) {
			// single track points
			TrackPoint trackPoint = trackSegment.getTrackPoints().get(0);
			SimpleFeature trackPointFeature = trackPointType
					.buildFeature(trackPoint);
			trackPointsToAdd.add(trackPointFeature);
			return;
		}

		// multiple trackpoints
		TrackSpeed currentTrackSpeed = null;
		List<Coordinate> coords = new ArrayList<Coordinate>();
		trackPoints: for (int i = 0; i < trackSegment.getTrackPoints().size(); i++) {
			TrackPoint trackPoint = trackSegment.getTrackPoints().get(i);

			// map to features
			trackPointsToAdd.add(trackPointType.buildFeature(trackPoint));

			coords.add(new Coordinate(trackPoint.getLocation().getX(),
					trackPoint.getLocation().getY()));

			if (i == 0)
				trackSegment.setStartUtc(trackPoint.getUtcTimestamp());

			if (i == trackSegment.getTrackPoints().size() - 1)
				trackSegment.setEndUtc(trackPoint.getUtcTimestamp());
			else {
				// order 1 coefficients (speed)
				TrackPoint next = trackSegment.getTrackPoints().get(i + 1);

				Coordinate[] crds = { trackPoint.getLocation().getCoordinate(),
						next.getLocation().getCoordinate() };
				LineString line = geometryFactory.createLineString(crds);
				Long duration = next.getUtcTimestamp().getTime()
						- trackPoint.getUtcTimestamp().getTime();
				if (duration < 0) {
					log.warn("Duration " + duration + " is negative between "
							+ trackPoint.getLocation() + " and "
							+ next.getLocation()
							+ ", skipping speed computation");
					currentTrackSpeed = null;
					continue trackPoints;
				}
				TrackSpeed trackSpeed = new TrackSpeed(trackPoint, line,
						duration, geodeticCalculator);
				if (trackSpeed.getSpeed() > maxSpeed) {
					log.warn("Speed " + trackSpeed.getSpeed() + " is above "
							+ maxSpeed + " between " + trackPoint.getLocation()
							+ " and " + next.getLocation()
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
		LineString segment = geometryFactory.createLineString(coords
				.toArray(new Coordinate[coords.size()]));
		trackSegment.setSegment(segment);
		trackSegmentsToAdd.add(trackSegmentType.buildFeature(trackSegment));

		// persist
		try {
			Transaction transaction = new DefaultTransaction();
			trackPointsStore.setTransaction(transaction);
			trackSpeedsStore.setTransaction(transaction);
			trackSegmentsStore.setTransaction(transaction);
			try {
				trackPointsStore.addFeatures(trackPointsToAdd);
				trackSpeedsStore.addFeatures(trackSpeedsToAdd);
				trackSegmentsStore.addFeatures(trackSegmentsToAdd);
				transaction.commit();
			} catch (Exception e) {
				transaction.rollback();
				throw new ArgeoException("Cannot persist changes", e);
			} finally {
				transaction.close();
				trackPointsToAdd.clear();
				trackSpeedsToAdd.clear();
				trackSegmentsToAdd.clear();
			}
		} catch (ArgeoException e) {
			throw e;
		} catch (IOException e) {
			throw new ArgeoException("Unexpected issue with the transaction", e);
		}
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

	public void setDataStore(DataStore dataStore) {
		this.dataStore = dataStore;
	}

	public class TrackGpxHandler extends GpxHandler {

		public TrackGpxHandler(String sensor, Integer srid) {
			super(sensor, srid);
		}

		@Override
		protected void processTrackSegment(TrackSegment trackSegment,
				GeometryFactory geometryFactory) {
			GeoToolsTrackDao.this.processTrackSegment(trackSegment,
					geometryFactory);

		}
	}

}
