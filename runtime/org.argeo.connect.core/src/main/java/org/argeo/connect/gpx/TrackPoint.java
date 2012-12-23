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
package org.argeo.connect.gpx;

import java.util.Date;

import com.vividsolutions.jts.geom.Point;

/** A 4D position of a given sensor */
public class TrackPoint implements Cloneable {
	/** Name of the related geometry field */
	public final static String POSITION = "position";
	
	private Integer tid;
	private String segmentUuid;
	private String sensor;
	private Date utcTimestamp;
	private Point position;
	private Double elevation;

	/** Empty constructor */
	public TrackPoint() {
	}

	/** Cloning constructor */
	public TrackPoint(TrackPoint trackPoint) {
		this.tid = trackPoint.tid;
		this.segmentUuid = trackPoint.segmentUuid;
		this.sensor = trackPoint.sensor;
		this.utcTimestamp = trackPoint.utcTimestamp;
		this.position = trackPoint.position;
		this.elevation = trackPoint.elevation;
	}

	public Integer getTid() {
		return tid;
	}

	public void setTid(Integer tid) {
		this.tid = tid;
	}

	public String getSegmentUuid() {
		return segmentUuid;
	}

	public void setSegmentUuid(String segmentUuid) {
		this.segmentUuid = segmentUuid;
	}

	public String getSensor() {
		return sensor;
	}

	public void setSensor(String sensor) {
		this.sensor = sensor;
	}

	public Date getUtcTimestamp() {
		return utcTimestamp;
	}

	public void setUtcTimestamp(Date ts) {
		this.utcTimestamp = ts;
	}

	public Point getPosition() {
		return position;
	}

	public void setPosition(Point location) {
		this.position = location;
	}

	public Double getElevation() {
		return elevation;
	}

	public void setElevation(Double elevation) {
		this.elevation = elevation;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new TrackPoint(this);
	}

}
