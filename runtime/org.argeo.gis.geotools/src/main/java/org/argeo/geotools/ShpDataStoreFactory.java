package org.argeo.geotools;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.argeo.ArgeoException;
import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;

/** Wraps a {@link ShapefileDataStoreFactory}, preparing the directories. */
public class ShpDataStoreFactory {
	private ShapefileDataStoreFactory wrappedFactory = new ShapefileDataStoreFactory();

	private String url;
	private Boolean createSpatialIndex = true;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public DataStore createDataStore() {
		try {
			Map params = new HashMap();
			File dir = DataUtilities.urlToFile(new URL(url));
			if (!dir.exists())
				dir.mkdirs();
			else if (!dir.isDirectory())
				throw new ArgeoException(url + " does not point to a directory");
			params.put(ShapefileDataStoreFactory.URLP.key, url);
			params.put(ShapefileDataStoreFactory.CREATE_SPATIAL_INDEX.key,
					createSpatialIndex);
			return wrappedFactory.createDataStore(params);
		} catch (IOException e) {
			throw new ArgeoException("Cannot create Shapefile data store", e);
		}
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setCreateSpatialIndex(Boolean createSpatialIndex) {
		this.createSpatialIndex = createSpatialIndex;
	}

}
