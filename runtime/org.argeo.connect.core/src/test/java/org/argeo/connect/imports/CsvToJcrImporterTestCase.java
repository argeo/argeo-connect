/*
 * Copyright (C) 2012 argeo.org
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

package org.argeo.connect.imports;

import java.io.File;
import java.io.InputStreamReader;

import javax.jcr.Repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.commons.cnd.CndImporter;
import org.apache.jackrabbit.core.TransientRepository;
import org.argeo.ArgeoException;
import org.argeo.connect.ConnectNames;
import org.argeo.jcr.ArgeoNames;
import org.argeo.jcr.unit.AbstractJcrTestCase;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Setup of a JCR repository with current data model without using spring
 * injection and then test parsing and import of csv files to JCR.
 */
public class CsvToJcrImporterTestCase extends AbstractJcrTestCase {

	private final static Log log = LogFactory.getLog(JcrModelTest.class);

	protected String pathToRepository = System.getProperty("user.dir")
			+ "/target/jackrabbit-" + System.getProperty("user.name");

	// Decide if the repository must be reset at each start
	private boolean deleteRepoOnStartup = true;

	@Override
	protected void setUp() throws Exception {
		if (deleteRepoOnStartup)
			super.setUp();
		else
			setRepository(createRepository());

		initModel();
	}

	private void initModel() throws Exception {
		// Initialize Repo :
		session().setNamespacePrefix("connect", ConnectNames.CONNECT_NAMESPACE);
		session().setNamespacePrefix("argeo", ArgeoNames.ARGEO_NAMESPACE);

		InputStreamReader reader = new InputStreamReader(getClass()
				.getResourceAsStream("/org/argeo/jcr/argeo.cnd"));
		CndImporter.registerNodeTypes(reader, session());
		reader.close();

		reader = new InputStreamReader(getClass().getResourceAsStream(
				"/org/argeo/connect/connect.cnd"));
		CndImporter.registerNodeTypes(reader, session());
		reader.close();
	}

	public void testImport() throws Exception {
		try {
			// TODO: implement unit test while importing .stf file.
			// This process is not a priority for the time being.

		} catch (Exception e) {
			throw new ArgeoException("Cannot import file ", e);
		}
	}

	@Override
	protected File getRepositoryFile() throws Exception {
		Resource res = new ClassPathResource(
				"org/argeo/connect/imports/repository-h2.xml");
		return res.getFile();
	}

	@Override
	protected Repository createRepository() throws Exception {
		Repository repository = new TransientRepository(getRepositoryFile(),
				getHomeDir());
		return repository;
	}

}
