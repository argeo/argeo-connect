package org.argeo.connect.versioning;

import javax.jcr.Item;

import org.argeo.connect.ConnectException;

/** The result of the comparison of two JCR items. */
public class ItemDiff {

	public final static Integer MODIFIED = 0;
	public final static Integer ADDED = 1;
	public final static Integer REMOVED = 2;

	private final Integer type;
	private final String relPath;
	private final Item referenceItem;
	private final Item observedItem;

	/**
	 * 
	 * @param type
	 * @param relPath
	 * @param referenceItem
	 * @param observedItem
	 */
	public ItemDiff(Integer type, String relPath, Item referenceItem, Item observedItem) {
		if (type == MODIFIED) {
			if (referenceItem == null || observedItem == null)
				throw new ConnectException("Reference and new items must be specified.");
		} else if (type == ADDED) {
			if (referenceItem != null || observedItem == null)
				throw new ConnectException("New item and only it must be specified.");
		} else if (type == REMOVED) {
			if (referenceItem == null || observedItem != null)
				throw new ConnectException("Reference item and only it must be specified.");
		} else {
			throw new ConnectException("Unkown diff type " + type);
		}

		if (relPath == null)
			throw new ConnectException("Relative path must be specified");

		this.type = type;
		this.relPath = relPath;
		this.referenceItem = referenceItem;
		this.observedItem = observedItem;
	}

	public Integer getType() {
		return type;
	}

	public String getRelPath() {
		return relPath;
	}

	public Item getReferenceItem() {
		return referenceItem;
	}

	public Item getObservedItem() {
		return observedItem;
	}

}
