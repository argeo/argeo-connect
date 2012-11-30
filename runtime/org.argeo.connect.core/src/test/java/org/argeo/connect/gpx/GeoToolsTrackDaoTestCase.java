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
package org.argeo.connect.gpx;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;
import org.argeo.support.junit.AbstractSpringTestCase;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class GeoToolsTrackDaoTestCase extends AbstractSpringTestCase {
	public void testImport() throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Begin GPX import");
		}
		// Integration test
		forDeployment();

		// DEV ONLY : To test import an a big directory
		// WARNING: file configuration is hard coded
		// forBigImport();
	}

	private void forDeployment() throws Exception {
		String sensor = "mbaudier";
		Resource gpx1 = new ClassPathResource(
				"/org/argeo/connect/gpx/test/20100123.gpx");
		Resource gpx2 = new ClassPathResource(
				"/org/argeo/connect/gpx/test/20100124.gpx");
		Resource gpx3 = new ClassPathResource(
				"/org/argeo/connect/gpx/test/20100125.gpx");

		TrackDao trackDao = getBean(TrackDao.class);
		long begin = System.currentTimeMillis();

		InputStream in = null;
		InputStream in2 = null;
		InputStream in3 = null;

		String file = "";
		try {
			file = gpx1.getFilename();
			in = gpx1.getInputStream();
			trackDao.importRawToCleanSession(file, sensor, in);

			file = gpx2.getFilename();
			in2 = gpx2.getInputStream();
			trackDao.importRawToCleanSession(file, sensor, in2);

			file = gpx3.getFilename();
			in3 = gpx3.getInputStream();
			trackDao.importRawToCleanSession(file, sensor, in3);
		} catch (Exception e) {
			log.warn("Could not import " + file + ": " + e.getMessage());
			throw e;
		} finally {
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(in2);
			IOUtils.closeQuietly(in3);
		}

		long duration = System.currentTimeMillis() - begin;
		if (log.isDebugEnabled())
			log.debug("Imported 3 tests files in " + ((duration / 1000) / 60)
					+ "min " + ((duration / 1000) % 60) + "s");

	}

	@SuppressWarnings("unused")
	private void forBigImport() throws Exception {
		String sensor = "mbaudier";

		File dir = new File("/home/bsinou/dev/work/connect-gps/mbaudier-2010");

		SortedSet<File> files = new TreeSet<File>(new Comparator<File>() {
			public int compare(File o1, File o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});

		TrackDao trackDao = getBean(TrackDao.class);
		long begin = System.currentTimeMillis();

		// order files
		for (File file : dir.listFiles()) {
			files.add(file);
		}

		for (File file : files) {
			if (!file.getName().endsWith(".gpx"))
				continue;
			FileInputStream in = null;
			try {
				in = new FileInputStream(file);
				trackDao.importRawToCleanSession(file.getName(), sensor, in);
			} catch (Exception e) {
				log.warn("Could not import " + file + ": " + e.getMessage());
				throw e;
			} finally {
				IOUtils.closeQuietly(in);
			}
		}
		long duration = System.currentTimeMillis() - begin;
		if (log.isDebugEnabled())
			log.debug("Imported files from " + dir + " in "
					+ ((duration / 1000) / 60) + "min "
					+ ((duration / 1000) % 60) + "s");
	}
}
