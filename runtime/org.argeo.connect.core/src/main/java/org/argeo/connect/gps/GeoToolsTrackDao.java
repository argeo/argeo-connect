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
package org.argeo.connect.gps;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.bind.DatatypeConverter;
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
import org.geotools.data.Query;
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
import org.opengis.filter.sort.SortBy;
import org.opengis.filter.sort.SortOrder;
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

	private Boolean shapefileBackend;

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
					positionsDisplayTable, TrackSegment.class, popularMercator,
					"position");

			SimpleFeatureCollection positions = FeatureCollections
					.newCollection();
			SimpleFeatureCollection segments = FeatureCollections
					.newCollection();

			SimpleFeatureStore trackSpeedsStore = getFeatureStore(trackSpeedType);
			Filter filter = filterFactory.not(CQL.toFilter(toRemoveCql));
			Query query = new Query(trackSpeedsToCleanTable, filter);
			query.setSortBy(new SortBy[] { filterFactory.sort("utcTimestamp",
					SortOrder.ASCENDING) });
			FeatureIterator<SimpleFeature> filteredSpeeds = trackSpeedsStore
					.getFeatures(query).features();

			List<Coordinate> currSegmentCoords = new ArrayList<Coordinate>();
			TrackSegment currSegment = null;
			while (filteredSpeeds.hasNext()) {
				SimpleFeature speed = filteredSpeeds.next();
				SimpleFeature position = positionType.convertFeature(speed);
				positions.add(position);

				// segment
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

	public void exportAsGpx(String cleanSession, String referential,
			String toRemoveCql, OutputStream out) {
		try {
			String trackSpeedsToCleanTable = addGpsCleanTablePrefix(cleanSession);
			BeanFeatureTypeBuilder<TrackSpeed> trackSpeedType = new BeanFeatureTypeBuilder<TrackSpeed>(
					trackSpeedsToCleanTable, TrackSpeed.class);
			SimpleFeatureStore trackSpeedsStore = getFeatureStore(trackSpeedType);

			PrintWriter writer = new PrintWriter(out);
			writer.append("<gpx"
					+ " xmlns=\"http://www.topografix.com/GPX/1/1\""
					+ " creator=\"\""
					+ " version=\"1.1\""
					+ " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
					+ " xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\""
					+ ">\n");

			Filter filter = filterFactory.not(CQL.toFilter(toRemoveCql));

			// sorted query
			Query query = new Query(trackSpeedsToCleanTable, filter);
			query.setSortBy(new SortBy[] { filterFactory.sort("utcTimestamp",
					SortOrder.ASCENDING) });
			FeatureIterator<SimpleFeature> filteredSpeeds = trackSpeedsStore
					.getFeatures(query).features();

			String currSegmentUuid = null;
			while (filteredSpeeds.hasNext()) {
				SimpleFeature speed = filteredSpeeds.next();
				String segmentUuid = speed.getAttribute("segmentUuid")
						.toString();
				Point position = (Point) speed.getAttribute("position");
				Double elevation = (Double) speed.getAttribute("elevation");
				Date utcTimestamp = (Date) speed.getAttribute("utcTimestamp");

				// write
				if (currSegmentUuid == null
						|| !currSegmentUuid.equals(segmentUuid)) {
					if (currSegmentUuid != null)
						writer.append("</trkseg></trk>\n");
					writer.append("<trk>\n");
					writer.append("<name>Session ").append(cleanSession)
							.append(" ").append(segmentUuid)
							.append("</name>\n");
					writer.append("<trkseg>\n");
					currSegmentUuid = segmentUuid;
				}

				writer.append(" <trkpt lat=\"")
						.append(Double.toString(position.getCoordinate().y))
						.append("\" lon=\"")
						.append(Double.toString(position.getCoordinate().x))
						.append("\">");
				writer.append("<ele>").append(Double.toString(elevation))
						.append("</ele>");

				// GeoTools 2.7 returns timestamps relative to local timezone
				Calendar localCalendar = Calendar.getInstance();
				localCalendar.setTime(utcTimestamp);
				String timeStr = DatatypeConverter.printDateTime(localCalendar);

				writer.append("<time>").append(timeStr).append("</time>");
				writer.append("</trkpt>\n");
			}
			if (currSegmentUuid != null)
				writer.append("</trkseg></trk>\n");
			writer.append("</gpx>\n");
			writer.flush();
		} catch (Exception e) {
			throw new ArgeoException(
					"Cannot write " + cleanSession + " as GPX", e);
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
		FeatureCollection<SimpleFeatureType, SimpleFeature> trackSpeedsToAdd = FeatureCollections
				.newCollection();

		if (trackSegment.getTrackPoints().size() == 0) {
			// no track points
			return;
		} else if (trackSegment.getTrackPoints().size() == 1) {
			// single track points
			return;
		}

		// persist
		SimpleFeatureStore trackSpeedsStore = getFeatureStore(trackSpeedType);

		// multiple trackpoints
		TrackSpeed currentTrackSpeed = null;
		trackPoints: for (int i = 0; i < trackSegment.getTrackPoints().size(); i++) {
			TrackPoint trackPoint = trackSegment.getTrackPoints().get(i);

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
				if (duration <= 0) {
					log.warn("Duration " + duration
							+ " is negative or nil between "
							+ trackPoint.getPosition() + " and "
							+ next.getPosition()
							+ ", skipping speed computation");
					currentTrackSpeed = null;
					continue trackPoints;
				}
				Double ascent = next.getElevation() - trackPoint.getElevation();
				TrackSpeed trackSpeed = new TrackSpeed(trackPoint, line,
						duration, ascent, geodeticCalculator);
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
					// acceleration (in m/s²)
					Double speed1 = trackSpeed.getDistance()
							/ (trackSpeed.getDuration() / 1000);
					Double speed2 = currentTrackSpeed.getDistance()
							/ (currentTrackSpeed.getDuration() / 1000);
					Double acceleration = (speed1 - speed2)
							/ (currentTrackSpeed.getDuration() / 1000);
					trackSpeed.setAcceleration(acceleration);

					// azimut variation
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
				SimpleFeature trackSpeedFeature = trackSpeedType.buildFeature(
						trackSpeedsStore.getSchema(), trackSpeed);
				trackSpeedsToAdd.add(trackSpeedFeature);
				currentTrackSpeed = trackSpeed;
			}

		}

		try {
			Transaction transaction = new DefaultTransaction();
			trackSpeedsStore.setTransaction(transaction);
			try {
				trackSpeedsStore.addFeatures(trackSpeedsToAdd);
				transaction.commit();
			} catch (Exception e) {
				transaction.rollback();
				throw new ArgeoException("Cannot persist changes", e);
			} finally {
				transaction.close();
				trackSpeedsToAdd.clear();
			}
		} catch (ArgeoException e) {
			throw e;
		} catch (IOException e) {
			throw new ArgeoException("Unexpected issue with the transaction", e);
		}
	}

	protected SimpleFeatureStore getFeatureStore(BeanFeatureTypeBuilder<?> type) {
		SimpleFeatureType featureType = type.getFeatureType();
		SimpleFeatureStore featureStore = (SimpleFeatureStore) geoJcrMapper
				.getOrCreateFeatureSource(dataStoreAlias, featureType);
		return featureStore;
	}

	/** Normalize from [-180°,180°] to [0°,360°] */
	private Double convertAzimuth(Double azimuth) {
		if (azimuth < 0)
			return 360d + azimuth;
		else
			return azimuth;
	}

	public void deleteCleanPositions(String referential,
			List<String> segmentUuids) {
		try {
			String positionsTable = addPositionsTablePrefix(referential);
			BeanFeatureTypeBuilder<TrackPoint> positionType = new BeanFeatureTypeBuilder<TrackPoint>(
					positionsTable, TrackPoint.class);
			String positionsDisplayTable = addPositionsDisplayTablePrefix(referential);
			BeanFeatureTypeBuilder<TrackSegment> positionDisplayType = new BeanFeatureTypeBuilder<TrackSegment>(
					positionsDisplayTable, TrackSegment.class, null, "segment");
			Transaction transaction = new DefaultTransaction();
			SimpleFeatureStore positionStore = getFeatureStore(positionType);
			SimpleFeatureStore positionDisplayStore = getFeatureStore(positionDisplayType);
			positionStore.setTransaction(transaction);
			positionDisplayStore.setTransaction(transaction);
			Filter filter;
			try {
				for (String segmentUuid : segmentUuids) {
					filter = CQL
							.toFilter("segmentUuid = '" + segmentUuid + "'");
					positionStore.removeFeatures(filter);
					filter = CQL.toFilter("uuid = '" + segmentUuid + "'");
					positionDisplayStore.removeFeatures(filter);
				}

				transaction.commit();
			} catch (Exception e) {
				transaction.rollback();
				throw new ArgeoException("Cannot remove segments", e);
			} finally {
				transaction.close();
			}
		} catch (Exception e) {
			throw new ArgeoException(
					"Cannot remove segments from local referential", e);
		}
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

	public void setShapefileBackend(Boolean shapefile) {
		this.shapefileBackend = shapefile;
	}

	public Boolean isShapefileBackend() {
		return shapefileBackend;
	}

	public class TrackGpxHandler extends GpxHandler {
		private BeanFeatureTypeBuilder<TrackSpeed> trackSpeedType;
		private List<String> segmentUuids = new ArrayList<String>();

		public TrackGpxHandler(String sensor, Integer srid, String cleanSession) {
			super(sensor, srid);
			// String trackSpeedsToCleanTable = "connect_gpsclean_" +
			// cleanSession;
			trackSpeedType = new BeanFeatureTypeBuilder<TrackSpeed>(
					addGpsCleanTablePrefix(cleanSession), TrackSpeed.class,
					null, "line");
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