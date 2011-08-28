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
