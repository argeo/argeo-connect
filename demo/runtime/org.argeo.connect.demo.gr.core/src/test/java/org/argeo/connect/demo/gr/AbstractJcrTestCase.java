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

import java.io.File;

import org.argeo.support.junit.AbstractSpringTestCase;
import org.springframework.context.ConfigurableApplicationContext;

/** Factorizes useful methods to implement test within the JCR framework. */
public abstract class AbstractJcrTestCase extends AbstractSpringTestCase {

	protected String pathToRepository = System.getProperty("user.dir")
			+ "/target/jackrabbit-" + System.getProperty("user.name");

	@Override
	protected void setUp() throws Exception {
		// Insure we start the test with a clean environment
		// Warning : it deletes the whole JCR Repository file system.
		ConfigurableApplicationContext cac = getContext();
		cac.stop();
		if (deleteRepoOnStartup()) {
			File repoDir = new File(pathToRepository);
			boolean success = deleteDir(repoDir);
			if (success)
				log.debug("Repository has been deleted correctly before startup");
		}
		cac = getContext();
		cac.refresh();
	}

	@Override
	protected void tearDown() throws Exception {
		ConfigurableApplicationContext cac = getContext();
		cac.stop();
		if (deleteRepoOnShutDown()) {
			File repoDir = new File(pathToRepository);
			boolean success = deleteDir(repoDir);
			if (success)
				log.debug("Repository has been deleted correctly on shut down");

		}
	}

	@SuppressWarnings("unchecked")
	protected <T> T getBeanWithName(Class<? extends T> clss, String name) {
		ConfigurableApplicationContext cac = getContext();
		// cac.refresh();
		T bean = (T) cac.getBean(name, clss);
		return bean;
	}

	/**
	 * Deletes all files and subdirectories under dir. Returns true if all
	 * deletions were successful. If a deletion fails, the method stops
	 * attempting to delete and returns false.
	 */
	protected static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		return dir.delete();
	}

	/** Override to change this property */
	protected boolean deleteRepoOnStartup() {
		return true;
	}

	/** Override to change this property */
	protected boolean deleteRepoOnShutDown() {
		return true;
	}
}
