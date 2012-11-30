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
package org.argeo.connect.demo.gr.testing;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.argeo.ArgeoException;
import org.argeo.connect.demo.gr.GrConstants;
import org.argeo.connect.demo.gr.GrNames;
import org.argeo.connect.demo.gr.GrTypes;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class DataGenerator implements Runnable, GrNames {

	private Integer nationalSitesCount = 10;// = 10;
	private Integer baseSitesCount = 50;// = 50;
	private Integer normalSitesCount = 200;// = 150;

	private String networks = "file://"
			+ System.getProperty("user.home")
			+ "/gis/projects/ignfi_sudan-110826/produced/networks_110826_singleparts.shp";

	private GeometryFactory geometryFactory = JTSFactoryFinder
			.getGeometryFactory(null);

	public void run() {
		// Required to have a consistent coordinate order in GeoTools, see
		// http://sourceforge.net/mailarchive/forum.php?thread_name=BANLkTikXZ%2BNr_2fX4z8z%2BRnLK70SPc5qYw%40mail.gmail.com&forum_name=geotools-gt2-users
		System.setProperty("org.geotools.referencing.forceXY", "true");

		try {
			// open shapefile
			ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

			Map<String, Serializable> params = new HashMap<String, Serializable>();
			params.put("url", networks);

			ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory
					.createNewDataStore(params);
			SimpleFeatureSource featureSource = newDataStore.getFeatureSource();

			StringBuffer buf = new StringBuffer("");

			// CSV header
			buf.append(GrTypes.GR_NETWORK).append(',');
			buf.append(GrTypes.GR_SITE).append(',');
			buf.append(GR_SITE_TYPE).append(',');
			buf.append(GR_WGS84_LONGITUDE).append(',');
			buf.append(GR_WGS84_LATITUDE).append(',');
			buf.append("\n");

			SimpleFeatureIterator it = featureSource.getFeatures().features();
			while (it.hasNext()) {
				SimpleFeature network = it.next();
				String networkName = network.getAttribute("name").toString();
				// normalize
				networkName = networkName.replace('\"', '_');

				Geometry polygon = (Geometry) network.getDefaultGeometry();

				writePoints(buf,
						generateRandomPoints(nationalSitesCount, polygon),
						networkName, GrConstants.MONITORED);
				writePoints(buf, generateRandomPoints(baseSitesCount, polygon),
						networkName, GrConstants.VISITED);
				writePoints(buf,
						generateRandomPoints(normalSitesCount, polygon),
						networkName, GrConstants.REGISTERED);

				System.out.println("Generated test data for network '"
						+ networkName + "'");
			}

			File outputFile = new File("local/ignfi-gr-testdata.csv");
			FileUtils.writeStringToFile(outputFile, buf.toString());
		} catch (IOException e) {
			throw new ArgeoException("Cannot generate data", e);
		}

	}

	protected List<Point> generateRandomPoints(Integer number, Geometry polygon) {
		Coordinate[] bbox = polygon.getEnvelope().getCoordinates();
		double minX = bbox[0].x;
		double minY = bbox[0].y;
		double maxX = bbox[2].x;
		double maxY = bbox[2].y;

		List<Point> points = new ArrayList<Point>();
		// FIXME possible infinite loop? add timeout?
		while (points.size() < number) {
			double randomX = Math.random();
			double randomY = Math.random();
			Double newX = minX + (maxX - minX) * randomX;
			Double newY = minY + (maxY - minY) * randomY;
			Point pt = geometryFactory.createPoint(new Coordinate(newX, newY));
			if (polygon.contains(pt))
				points.add(pt);
		}
		return points;
	}

	protected void writePoints(StringBuffer buf, List<Point> points,
			String networkName, String siteType) {
		for (Point pt : points) {
			buf.append('\"').append(networkName).append("\",");
			String siteName = UUID.randomUUID().toString();
			buf.append('\"').append(siteName).append("\",");
			buf.append('\"').append(siteType).append("\",");
			buf.append(pt.getCoordinate().x).append(',');
			buf.append(pt.getCoordinate().y).append(',');
			buf.append('\n');
		}

	}

	public static void main(String[] args) {
		new DataGenerator().run();
	}
}
