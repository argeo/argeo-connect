/*
 * Copyright (C) 2007-2012 Argeo GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.argeo.connect.people.core.versioning;

import javax.jcr.Item;

import org.argeo.ArgeoException;

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
	public ItemDiff(Integer type, String relPath, Item referenceItem,
			Item observedItem) {
		if (type == MODIFIED) {
			if (referenceItem == null || observedItem == null)
				throw new ArgeoException(
						"Reference and new items must be specified.");
		} else if (type == ADDED) {
			if (referenceItem != null || observedItem == null)
				throw new ArgeoException(
						"New item and only it must be specified.");
		} else if (type == REMOVED) {
			if (referenceItem == null || observedItem != null)
				throw new ArgeoException(
						"Reference item and only it must be specified.");
		} else {
			throw new ArgeoException("Unkown diff type " + type);
		}

		if (relPath == null)
			throw new ArgeoException("Relative path must be specified");

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
