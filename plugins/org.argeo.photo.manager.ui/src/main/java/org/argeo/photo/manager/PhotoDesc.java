package org.argeo.photo.manager;

import java.util.Date;

public class PhotoDesc {
	public final static int FIELD_SRCID = 0;
	public final static int FIELD_PHOTOID = 1;
	public final static int FIELD_LABEL = 2;
	public final static int FIELD_DATE = 3;

	private Long srcId;
	private Long photoId;
	private String label;
	private Date date;

	public PhotoDesc(Object[] objs) {
		this.srcId = (Long) objs[FIELD_SRCID];
		this.photoId = (Long) objs[FIELD_PHOTOID];
		this.label = (String) objs[FIELD_LABEL];
		this.date = (Date) objs[FIELD_DATE];
	}

	public Object[] getValues() {
		Object[] objs = new Object[4];
		objs[FIELD_SRCID] = srcId;
		objs[FIELD_PHOTOID] = photoId;
		objs[FIELD_LABEL] = label;
		objs[FIELD_DATE] = date;
		return objs;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Long getPhotoId() {
		return photoId;
	}

	public void setPhotoId(Long photoId) {
		this.photoId = photoId;
	}

	public Long getSrcId() {
		return srcId;
	}

	public void setSrcId(Long srcId) {
		this.srcId = srcId;
	}

}
