/*
 * Copyright (C) 2010 Mathieu Baudier <mbaudier@argeo.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
