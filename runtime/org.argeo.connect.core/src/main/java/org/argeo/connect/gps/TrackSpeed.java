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

import org.geotools.referencing.GeodeticCalculator;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

/**
 * A 4D position enriched with order 1 and 2 informations (speed, acceleration,
 * rotation) useful for cleaning GPS tracks.
 */
public class TrackSpeed extends TrackPoint {
	/** Name of the related geometry field */
	public final static String LINE = "line";

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
