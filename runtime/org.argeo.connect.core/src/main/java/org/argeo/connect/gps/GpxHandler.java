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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.argeo.ArgeoException;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.CRS;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;

/** Parse the GPX format. */
class GpxHandler extends DefaultHandler {
	public static final String TAG_TRKSEG = "trkseg";
	public static final String TAG_TRKPT = "trkpt";
	public static final String TAG_TIME = "time";
	public static final String TAG_ELE = "ele";
	public static final String ATTR_LAT = "lat";
	public static final String ATTR_LON = "lon";

	private final static DateFormat ISO8601 = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss");

	private final CoordinateReferenceSystem wgs84;
	private final GeometryFactory targetGF;

	private final MathTransform reprojection;

	private String sensor;

	private StringBuffer accumulator = new StringBuffer();

	// private Long segmentCount = 0l;
	private Long pointCount = 0l;
	private Long currentSegmentPointCount = 0l;
	private String currentSegmentUuid = null;
	private TrackPoint currentTrackPoint = null;

	private TrackSegment currentTrackSegment = null;

	/**
	 * Called when a track segment has been parsed track segment. Does nothing
	 * by default.
	 */
	protected void processTrackSegment(TrackSegment trackSegment,
			GeometryFactory geometryFactory) {
		// does nothing by default
	}

	public GpxHandler(String sensor, Integer srid) {
		this.sensor = sensor;
		PrecisionModel precisionModel = new PrecisionModel();
		targetGF = new GeometryFactory(precisionModel, srid);
		if (srid != 4326) {
			try {
				wgs84 = CRS.decode("EPSG:4326");
				reprojection = CRS.findMathTransform(wgs84,
						CRS.decode("EPSG:" + srid));
			} catch (Exception e) {
				throw new ArgeoException("Cannot find reprojection", e);
			}
		} else {
			reprojection = null;
			wgs84 = null;
		}
	}

	public void characters(char[] buffer, int start, int length) {
		accumulator.append(buffer, start, length);
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		accumulator.setLength(0); // Ready to accumulate new text
		if (qName.equals(TAG_TRKSEG)) {
			currentSegmentUuid = UUID.randomUUID().toString();
			currentTrackSegment = new TrackSegment();
			currentTrackSegment.setSensor(sensor);
			currentTrackSegment.setUuid(currentSegmentUuid);
		} else if (qName.equals(TAG_TRKPT)) {
			currentTrackPoint = new TrackPoint();
			currentTrackPoint.setSensor(sensor);
			currentTrackPoint.setSegmentUuid(currentSegmentUuid);
			String latStr = attributes.getValue(ATTR_LAT);
			String lonStr = attributes.getValue(ATTR_LON);
			Coordinate coordinate = new Coordinate(Double.parseDouble(lonStr),
					Double.parseDouble(latStr));
			Point location = reproject(coordinate);
			currentTrackPoint.setPosition(location);
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (qName.equals(TAG_ELE)) {
			Double elevation = Double
					.parseDouble(accumulator.toString().trim());
			currentTrackPoint.setElevation(elevation);
		} else if (qName.equals(TAG_TIME) && currentTrackPoint != null) {
			String timeStr = accumulator.toString().trim();
			try {
				Date time = ISO8601.parse(timeStr);
				currentTrackPoint.setUtcTimestamp(time);
			} catch (ParseException e) {
				throw new ArgeoException("Cannot parse date " + timeStr);
			}
		} else if (qName.equals(TAG_TRKPT)) {
			// trackPoints.add(currentTrackPoint);
			pointCount++;
			currentSegmentPointCount++;

			currentTrackSegment.getTrackPoints().add(currentTrackPoint);

			// getHibernateTemplate().save(currentTrackPoint);

			currentTrackPoint = null;
		} else if (qName.equals(TAG_TRKSEG)) {
			processTrackSegment(currentTrackSegment, targetGF);
			currentSegmentPointCount = 0l;
			currentSegmentUuid = null;
		}

	}

	protected Point reproject(Coordinate coordinate) {
		if (reprojection != null) {
			try {
				// invert order
				DirectPosition2D pos = new DirectPosition2D(wgs84,
						coordinate.y, coordinate.x);
				DirectPosition targetPos = reprojection.transform(pos, null);
				Coordinate targetCoordinate = new Coordinate(
						targetPos.getOrdinate(0), targetPos.getOrdinate(1));
				return targetGF.createPoint(targetCoordinate);
			} catch (Exception e) {
				throw new ArgeoException("Cannot reproject " + coordinate, e);
			}
		} else {
			return targetGF.createPoint(coordinate);
		}
	}
}
