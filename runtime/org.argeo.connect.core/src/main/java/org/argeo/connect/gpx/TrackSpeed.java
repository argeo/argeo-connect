package org.argeo.connect.gpx;

import org.geotools.referencing.GeodeticCalculator;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

/**
 * A 4D position enriched with order 1 and 2 informations (speed, acceleration,
 * rotation) useful for cleaning GPS tracks.
 */
public class TrackSpeed extends TrackPoint {
	private LineString line;
	/** Orthodromic distance */
	private Double distance;
	private Double azimuth;
	private Long duration;
	// orthodromicDistance(line) in km/(duration in h)
	private Double speed;
	// can be null
	private Double acceleration;
	// can be null
	private Double azimuthVariation;

	public TrackSpeed() {
	}

	public TrackSpeed(TrackPoint ref, LineString line, Long duration,
			GeodeticCalculator geodeticCalculator) {
		super(ref);
		this.line = line;
		this.duration = duration;

		Point startPoint = line.getStartPoint();
		Point endPoint = line.getEndPoint();
		geodeticCalculator.setStartingGeographicPoint(startPoint.getX(),
				startPoint.getY());
		geodeticCalculator.setDestinationGeographicPoint(endPoint.getX(),
				endPoint.getY());
		this.distance = geodeticCalculator.getOrthodromicDistance();
		this.azimuth = geodeticCalculator.getAzimuth();
		// in km/h
		this.speed = (this.distance * 60 * 60) / this.duration;
	}

	public LineString getLine() {
		return line;
	}

	public void setLine(LineString line) {
		this.line = line;
	}

	public Long getDuration() {
		return duration;
	}

	public void setDuration(Long duration) {
		this.duration = duration;
	}

	public Double getSpeed() {
		return speed;
	}

	public void setSpeed(Double speed) {
		this.speed = speed;
	}

	public Double getDistance() {
		return distance;
	}

	public void setDistance(Double length) {
		this.distance = length;
	}

	public Double getAcceleration() {
		return acceleration;
	}

	public void setAcceleration(Double acceleration) {
		this.acceleration = acceleration;
	}

	public Double getAzimuth() {
		return azimuth;
	}

	public void setAzimuth(Double azimut) {
		this.azimuth = azimut;
	}

	public Double getAzimuthVariation() {
		return azimuthVariation;
	}

	public void setAzimuthVariation(Double azimuthVariation) {
		this.azimuthVariation = azimuthVariation;
	}

}
