package org.argeo.connect.people.imports;

import java.io.File;

import org.argeo.support.junit.AbstractSpringTestCase;
import org.springframework.context.ConfigurableApplicationContext;

/** Enable test with imports in the model*/
public abstract class AbstractModelTestCase extends AbstractSpringTestCase {

	protected String pathToRepository = System.getProperty("user.dir")
			+ "/target/jackrabbit-" + System.getProperty("user.name");

	@Override
	protected void setUp() throws Exception {
		// Insure we start the test with a clean environment
		// Warning : it deletes the whole JCR Repository file system.
		ConfigurableApplicationContext cac = getContext();
		cac.close();
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
		cac.close();
		Thread.sleep(1000);
		if (deleteRepoOnShutDown()) {
			File repoDir = new File(pathToRepository);
			boolean success = deleteDir(repoDir);
			if (success)
				log.debug("Repository has been deleted correctly on shut down");

		}
	}

	protected <T> T getBeanWithName(Class<? extends T> clss, String name) {
		ConfigurableApplicationContext cac = getContext();
		// cac.refresh();
		@SuppressWarnings("unchecked")
		T bean = (T) cac.getBean(name, clss);
		return bean;
	}

	/**
	 * Deletes all files and subdirectories under dir. Returns true if all
	 * deletions were successful. If a deletion fails, the method stops
	 * attempting to delete and returns false.
	 */
	protected boolean deleteDir(File dir) {
		boolean success = true;
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean tmpSuccess = deleteDir(new File(dir, children[i]));
				if (!tmpSuccess)
					log.warn("Unable to delete " + dir.getAbsolutePath());
				success = success & tmpSuccess;
			}
		}
		success = success & dir.delete();
		return success;
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
