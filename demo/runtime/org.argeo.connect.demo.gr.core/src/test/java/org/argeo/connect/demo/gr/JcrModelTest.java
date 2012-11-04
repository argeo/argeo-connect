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

package org.argeo.connect.demo.gr;

import java.io.File;
import java.io.InputStreamReader;

import javax.jcr.NodeIterator;
import javax.jcr.Repository;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.commons.cnd.CndImporter;
import org.apache.jackrabbit.core.TransientRepository;
import org.argeo.ArgeoException;
import org.argeo.gis.GisNames;
import org.argeo.jcr.unit.AbstractJcrTestCase;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Tests the basic setup of a JCR repository with current data model without
 * using spring injection.
 */

public class JcrModelTest extends AbstractJcrTestCase implements GrTypes {
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
	}

	public void testQueries() throws Exception {

		// Initialize Repo :
		session().setNamespacePrefix("gr", GrNames.GR_NAMESPACE);
		session().setNamespacePrefix("gis", GisNames.GIS_NAMESPACE);

		InputStreamReader reader = new InputStreamReader(getClass()
				.getResourceAsStream("/org/argeo/gis/argeo_gis.cnd"));
		CndImporter.registerNodeTypes(reader, session());
		reader.close();

		reader = new InputStreamReader(getClass().getResourceAsStream(
				"/org/argeo/connect/demo/gr/gr-0.0.1.cnd"));
		CndImporter.registerNodeTypes(reader, session());
		reader.close();

		executeSqlQuery("SELECT * FROM [nt:base]");
	}

	private NodeIterator executeSqlQuery(String xpathQuery) {
		try {
			if (log.isDebugEnabled())
				log.debug("SQL query string : " + xpathQuery);

			QueryManager queryManager = session().getWorkspace()
					.getQueryManager();
			Query query = queryManager.createQuery(xpathQuery, Query.JCR_SQL2);
			QueryResult queryResult = query.execute();
			NodeIterator ni = queryResult.getNodes();
			if (log.isDebugEnabled())
				log.debug("We have [" + ni.getSize() + "] results");
			return ni;

		} catch (Exception e) {
			throw new ArgeoException("Cannot execute query [" + xpathQuery
					+ "]", e);
		}
	}

	@Override
	protected File getRepositoryFile() throws Exception {
		Resource res = new ClassPathResource(
				"/org/argeo/connect/demo/gr/repository-h2.xml");
		return res.getFile();
	}

	@Override
	protected Repository createRepository() throws Exception {
		Repository repository = new TransientRepository(getRepositoryFile(),
				getHomeDir());
		return repository;
	}
}