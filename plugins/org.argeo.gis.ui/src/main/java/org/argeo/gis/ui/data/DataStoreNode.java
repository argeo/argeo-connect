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
package org.argeo.gis.ui.data;

import java.io.IOException;

import org.argeo.ArgeoException;
import org.argeo.eclipse.ui.TreeParent;
import org.geotools.data.DataStore;
import org.opengis.feature.type.Name;

public class DataStoreNode extends TreeParent {
	private DataStore dataStore;

	public DataStoreNode(DataStore dataStore) {
		super(dataStore.getInfo().getTitle() != null ? dataStore.getInfo()
				.getTitle() : dataStore.toString());
		this.dataStore = dataStore;
		try {
			for (Name name : dataStore.getNames()) {
				addChild(new FeatureNode(dataStore, name));
			}
		} catch (IOException e) {
			throw new ArgeoException("Cannot scan data store", e);
		}
	}

	public DataStore getDataStore() {
		return dataStore;
	}

}