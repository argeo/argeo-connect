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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.vividsolutions.jts.geom.LineString;

public class TrackSegment {
	private Integer tid;
	private String uuid;
	private String sensor;
	private Date startUtc;
	private Date endUtc;
	private LineString segment;
	private List<TrackPoint> trackPoints = new ArrayList<TrackPoint>();
	private List<TrackSpeed> trackSpeeds = new ArrayList<TrackSpeed>();

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getSensor() {
		return sensor;
	}

	public void setSensor(String sensor) {
		this.sensor = sensor;
	}

	public List<TrackPoint> getTrackPoints() {
		return trackPoints;
	}

	public void setTrackPoints(List<TrackPoint> trackPoints) {
		this.trackPoints = trackPoints;
	}

	public Date getStartUtc() {
		return startUtc;
	}

	public void setStartUtc(Date start) {
		this.startUtc = start;
	}

	public Date getEndUtc() {
		return endUtc;
	}

	public void setEndUtc(Date end) {
		this.endUtc = end;
	}

	public LineString getSegment() {
		return segment;
	}

	public void setSegment(LineString segment) {
		this.segment = segment;
	}

	public Integer getTid() {
		return tid;
	}

	public void setTid(Integer tid) {
		this.tid = tid;
	}

	public List<TrackSpeed> getTrackSpeeds() {
		return trackSpeeds;
	}

	public void setTrackSpeeds(List<TrackSpeed> trackSpeeds) {
		this.trackSpeeds = trackSpeeds;
	}

}
