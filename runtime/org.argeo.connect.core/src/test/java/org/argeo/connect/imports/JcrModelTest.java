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
package org.argeo.connect.imports;

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
import org.argeo.connect.ConnectNames;
import org.argeo.jcr.ArgeoNames;
import org.argeo.jcr.unit.AbstractJcrTestCase;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Tests the basic setup of a JCR repository with current data model without
 * using spring injection.
 */

public class JcrModelTest extends AbstractJcrTestCase {
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

	public void testModel() throws Exception {

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