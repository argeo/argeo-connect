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
