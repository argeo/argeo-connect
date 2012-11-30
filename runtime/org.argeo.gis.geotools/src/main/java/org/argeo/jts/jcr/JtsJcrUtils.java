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
package org.argeo.jts.jcr;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.jcr.Binary;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.Session;

import org.apache.commons.io.IOUtils;
import org.argeo.ArgeoException;
import org.argeo.jcr.JcrUtils;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.InputStreamInStream;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKBWriter;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;

/** Utilities depending only from the JTS library. */
public class JtsJcrUtils {
	private static GeometryFactory geometryFactory = new GeometryFactory();
	private static ThreadLocal<WKBWriter> wkbWriters = new ThreadLocal<WKBWriter>() {
		protected WKBWriter initialValue() {
			return new WKBWriter();
		}
	};
	private static ThreadLocal<WKBReader> wkbReaders = new ThreadLocal<WKBReader>() {
		protected WKBReader initialValue() {
			return new WKBReader(getGeometryFactory());
		}
	};

	private static ThreadLocal<WKTWriter> wktWriters = new ThreadLocal<WKTWriter>() {
		protected WKTWriter initialValue() {
			return new WKTWriter();
		}
	};
	private static ThreadLocal<WKTReader> wktReaders = new ThreadLocal<WKTReader>() {
		protected WKTReader initialValue() {
			return new WKTReader(getGeometryFactory());
		}
	};

	public static GeometryFactory getGeometryFactory() {
		return geometryFactory;
	}

	/**
	 * Reads WKB from {@link Binary} or WKT by converting other types to string.
	 */
	public final static Geometry readWkFormat(Property property) {
		try {
			if (property.getType() == PropertyType.BINARY) {
				// WKB
				Binary wkbBinary = null;
				InputStream in = null;
				try {
					wkbBinary = property.getBinary();
					in = wkbBinary.getStream();
					WKBReader wkbReader = wkbReaders.get();
					return wkbReader.read(new InputStreamInStream(in));
				} finally {
					IOUtils.closeQuietly(in);
					JcrUtils.closeQuietly(wkbBinary);
				}
			} else {
				// WKT
				String wkt = property.getString();
				WKTReader wktReader = wktReaders.get();
				return wktReader.read(wkt);
			}
		} catch (Exception e) {
			throw new ArgeoException("Cannot read geometry from " + property, e);
		}

	}

	/** The returned binary should be disposed by the caller */
	public final static Binary writeWkb(Session session, Geometry geometry) {
		Binary wkbBinary = null;
		InputStream in = null;
		try {
			WKBWriter wkbWriter = wkbWriters.get();
			byte[] arr = wkbWriter.write(geometry);
			in = new ByteArrayInputStream(arr);
			wkbBinary = session.getValueFactory().createBinary(in);
			return wkbBinary;
		} catch (Exception e) {
			throw new ArgeoException("Cannot write WKB", e);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	public final static String writeWkt(Geometry geometry) {
		return wktWriters.get().write(geometry);
	}
}
