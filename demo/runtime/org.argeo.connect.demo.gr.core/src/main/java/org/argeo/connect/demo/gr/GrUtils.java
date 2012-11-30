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
package org.argeo.connect.demo.gr;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.ArgeoException;
import org.argeo.gis.GisConstants;
import org.argeo.gis.GisNames;
import org.argeo.jts.jcr.JtsJcrUtils;
import org.geotools.geometry.jts.JTSFactoryFinder;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

/** Various utilities */
public class GrUtils implements GrNames {
	// GIS components
	private final static GeometryFactory geometryFactory = JTSFactoryFinder
			.getGeometryFactory(null);

	/** Synchronizes the gis:geometry properties of a gr:point */
	public static void syncPointGeometry(Node pointNode) {
		try {
			if (pointNode.hasProperty(GR_WGS84_LATITUDE)
					&& pointNode.hasProperty(GR_WGS84_LONGITUDE)) {
				Coordinate coordinate = new Coordinate(pointNode.getProperty(
						GR_WGS84_LONGITUDE).getDouble(), pointNode.getProperty(
						GR_WGS84_LATITUDE).getDouble());
				Point point = geometryFactory.createPoint(coordinate);
				pointNode.setProperty(GisNames.GIS_WKT,
						JtsJcrUtils.writeWkt(point));
				pointNode.setProperty(GisNames.GIS_SRS, GisConstants.WGS84);
			}
		} catch (RepositoryException e) {
			throw new ArgeoException("Cannot sync geometry of " + pointNode, e);
		}

	}

	/**
	 * Extracts the last part of an uuid (e.g. 'bdaa90111466' for
	 * '5eddb136-f906-401b-8d8d-bdaa90111466')
	 */
	public static String shortenUuid(String uuid) {
		int index = uuid.lastIndexOf('-');
		if (index > 0)
			return uuid.substring(index + 1);
		else
			return uuid;
	}

	private GrUtils() {
	}
}
