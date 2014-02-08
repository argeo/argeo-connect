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
package org.argeo.geotools.jdbc;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.argeo.ArgeoException;
import org.argeo.geotools.ShpDataStoreFactory;
import org.geotools.data.DataStore;
import org.geotools.data.postgis.PostgisNGDataStoreFactory;

/**
 * Simplified data store to avoid issues with Spring and OSGi when Springs scans
 * for all available factory methods.
 */
public class PostgisDataStoreFactory {
	private PostgisNGDataStoreFactory wrappedFactory = new PostgisNGDataStoreFactory();
	private Boolean shapefile = false;
	private ShpDataStoreFactory shpDataStoreFactory;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public DataStore createDataStore(DataSource dataSource) {
		if (shapefile)
			return shpDataStoreFactory.createDataStore();
		else
			try {
				Map params = new HashMap();
				params.put(PostgisNGDataStoreFactory.DATASOURCE.key, dataSource);
				return wrappedFactory.createDataStore(params);
			} catch (IOException e) {
				throw new ArgeoException("Cannot create PostGIS data store", e);
			}
	}

	public void setShapefile(Boolean shapefile) {
		this.shapefile = shapefile;
	}

	public void setShpDataStoreFactory(ShpDataStoreFactory shpDataStoreFactory) {
		this.shpDataStoreFactory = shpDataStoreFactory;
	}

}
