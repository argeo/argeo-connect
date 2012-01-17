package org.argeo.connect.gpx;

import java.util.Date;

import com.vividsolutions.jts.geom.Point;

/** A 4D position of a given sensor */
public class TrackPoint implements Cloneable {
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
