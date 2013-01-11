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
package org.argeo.geotools;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.FeatureIterator;
import org.geotools.filter.FilterFactoryImpl;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.identity.FeatureId;

/** Utilities related to the GeoTools framework */
public class GeoToolsUtils {

	private final static Log log = LogFactory.getLog(GeoToolsUtils.class);

	// TODO: use common factory finder?
	private static FilterFactory2 filterFactory = new FilterFactoryImpl();

	/** Opens a read/write feature store */
	public static SimpleFeatureStore getFeatureStore(DataStore dataStore,
			Name name) {
		SimpleFeatureSource featureSource;
		try {
			featureSource = dataStore.getFeatureSource(name);
		} catch (IOException e) {
			throw new ArgeoException("Cannot open feature source " + name
					+ " in data store " + dataStore, e);
		}
		if (!(featureSource instanceof SimpleFeatureStore)) {
			throw new ArgeoException("Feature source " + name
					+ " is not writable.");
		}
		return (SimpleFeatureStore) featureSource;
	}

	/** Creates the provided schema in the data store. */
	public static void createSchemaIfNeeded(DataStore dataStore,
			SimpleFeatureType featureType) {
		try {
			dataStore.getSchema(featureType.getName());
		} catch (Exception e) {
			// assume it does not exist
			try {
				dataStore.createSchema(featureType);
			} catch (IOException e1) {
				throw new ArgeoException("Cannot create schema " + featureType,
						e1);
			}
		}
	}

	public static FilterFactory2 ff() {
		return filterFactory;
	}

	public static SimpleFeature querySingleFeature(
			FeatureSource<SimpleFeatureType, SimpleFeature> featureSource,
			String featureId) {
		Set<FeatureId> ids = new HashSet<FeatureId>();
		ids.add(ff().featureId(featureId));
		Filter filter = ff().id(ids);
		FeatureIterator<SimpleFeature> it = null;
		try {
			it = featureSource.getFeatures(filter).features();
			if (!it.hasNext())
				return null;
			else {
				SimpleFeature feature = it.next();
				if (it.hasNext())
					log.warn("More than one feature for feature id "
							+ featureId + " in feature source "
							+ featureSource.getName());
				return feature;
			}
		} catch (Exception e) {
			throw new ArgeoException("Cannot extract single feature "
					+ featureId + " from feature source "
					+ featureSource.getName(), e);
		} finally {
			closeQuietly(it);
		}
	}

	public static void closeQuietly(FeatureIterator<?> featureIterator) {
		if (featureIterator != null)
			try {
				featureIterator.close();
			} catch (Exception e) {
				// silent
			}
	}
}
