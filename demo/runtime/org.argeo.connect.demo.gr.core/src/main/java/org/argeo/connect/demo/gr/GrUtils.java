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
			return uuid.substring(index);
		else
			return uuid;
	}

	private GrUtils() {
	}
}
