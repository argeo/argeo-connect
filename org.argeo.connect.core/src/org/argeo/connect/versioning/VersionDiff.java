package org.argeo.connect.versioning;

import java.util.Calendar;
import java.util.Map;

/**
 * Generic Object that enables the creation of history reports based on a JCR
 * versionable node. userId and creation date are added to the map of
 * PropertyDiff, together with the reference version ID
 * 
 * These two fields might be null
 * 
 */
public class VersionDiff {

	private String refVersionId;
	private String userId;
	private Map<String, ItemDiff> diffs;
	private Calendar updateTime;

	/**
	 * 
	 * @param referenceVersionId
	 * @param userId
	 * @param updateTime
	 * @param diffs
	 */
	public VersionDiff(String referenceVersionId, String userId,
			Calendar updateTime, Map<String, ItemDiff> diffs) {
		this.refVersionId = referenceVersionId;
		this.userId = userId;
		this.updateTime = updateTime;
		this.diffs = diffs;
	}

	public String getReferenceVersionId() {
		return refVersionId;
	}

	public String getUserId() {
		return userId;
	}

	public Map<String, ItemDiff> getDiffs() {
		return diffs;
	}

	public Calendar getUpdateTime() {
		return updateTime;
	}
}