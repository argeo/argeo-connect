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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Query;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.argeo.jcr.ArgeoNames;
import org.argeo.jcr.JcrUtils;
import org.argeo.jcr.UserJcrUtils;
import org.argeo.util.CsvParserWithLinesAsMap;
import org.springframework.core.io.Resource;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.context.SecurityContextHolder;

/** Concrete access to the backend */
public class GrBackendImpl implements GrBackend, GrNames, GrConstants, GrTypes {
	private final static Log log = LogFactory.getLog(GrBackendImpl.class);

	/* DEPENDENCY INJECTION */
	private Repository repository;
	private Map<Integer, String> managedRoles;
	private List<String> siteTypes;

	private Resource testData = null;
	private Resource monitoredPic;
	private Resource visitedPic;
	private Resource registeredPic;

	private Session adminSession;

	public Random random = new Random();

	/* Life cycle management */
	/**
	 * Call by each startup in order to make sure the backend is ready to
	 * receive/provide data.
	 */
	public void init() {
		try {
			adminSession = repository.login();
			if (!adminSession.nodeExists(GR_NETWORKS_BASE_PATH)) {
				// Make sure that base directories are available
				JcrUtils.mkdirs(adminSession, GR_NETWORKS_BASE_PATH);
				JcrUtils.mkdirs(adminSession, GR_IMPORTS_BASE_PATH);
				adminSession.save();
			}

			if (testData != null)
				new TestDataParser().parse(testData.getInputStream());
		} catch (Exception e) {
			JcrUtils.logoutQuietly(adminSession);
			throw new ArgeoException("Cannot initialize backend", e);
		}
		log.info("GR backend has been correctly initialized...");
	}

	/** Clean shutdown of the backend. */
	public void destroy() {
		JcrUtils.logoutQuietly(adminSession);
	}

	/* Queries */
	public Query createGenericQuery(String statement) {
		try {
			if (log.isTraceEnabled())
				log.trace("Create JCR-SQL2 query: " + statement);
			return adminSession.getWorkspace().getQueryManager()
					.createQuery(statement, Query.JCR_SQL2);
		} catch (Exception e) {
			throw new ArgeoException("Cannot create query " + statement, e);
		}
	}

	/* File management */

	// public File getSiteReport(String siteUid) {
	// SiteReportPublisher srp = new SiteReportPublisher(this);
	// return srp.createNewReport(siteUid);
	// }

	public static File getFileFromNode(Node node) {
		try {
			InputStream fis = null;
			byte[] ba = null;
			File tmpFile;

			Node child = node.getNodes().nextNode();
			if (child == null || !child.isNodeType(NodeType.NT_RESOURCE))
				throw new ArgeoException(
						"ERROR: IN the current implemented model, "
								+ NodeType.NT_FILE
								+ "  file node must have one and only one child of the nt:ressource, where actual data is stored");
			try {
				fis = (InputStream) child.getProperty(Property.JCR_DATA)
						.getBinary().getStream();
				ba = IOUtils.toByteArray(fis);
				tmpFile = File.createTempFile("GrTmpFile", ".pdf");
				// tmpFile.deleteOnExit();
				FileUtils.writeByteArrayToFile(tmpFile, ba);
			} catch (Exception e) {
				throw new ArgeoException("Stream error while opening file", e);
			} finally {
				IOUtils.closeQuietly(fis);
			}
			return tmpFile;
		} catch (RepositoryException re) {
			throw new ArgeoException(
					"JCR Error while getting file and creating "
							+ "tmp file for download.", re);
		}
	}

	/* Expose injected application wide lists. */
	public List<String> getSiteTypes() {
		return new ArrayList<String>(siteTypes);
	}

	/* Users */
	/** returns true if the current user is in the specified role */
	public boolean isUserInRole(Integer userRole) {
		String role = managedRoles.get(userRole);
		Authentication authen = SecurityContextHolder.getContext()
				.getAuthentication();
		for (GrantedAuthority ga : authen.getAuthorities()) {
			if (ga.getAuthority().equals(role))
				return true;
		}
		return false;
		// return currentUserService.getCurrentUser().getRoles().contains(role);
	}

	/** returns the current user ID **/
	public String getCurrentUserId() {
		Authentication authen = SecurityContextHolder.getContext()
				.getAuthentication();
		return authen.getName();
	}

	/** Returns a human readable display name using the user ID **/
	public String getUserDisplayName(String userId) {
		try {
			Node userProfile = UserJcrUtils
					.getUserProfile(adminSession, userId);
			if (userProfile == null)
				return userId;

			String firstName = userProfile.getProperty(
					ArgeoNames.ARGEO_FIRST_NAME).getString();
			String lastName = userProfile.getProperty(
					ArgeoNames.ARGEO_LAST_NAME).getString();
			return firstName + " " + lastName;
		} catch (RepositoryException e) {
			throw new ArgeoException("Cannot retrieve user catalog", e);
		}
	}

	public Node createNetwork(Node parent, String name) {
		try {
			Node node = parent.addNode(name, GrTypes.GR_NETWORK);
			parent.getSession().save();
			return node;
		} catch (RepositoryException re) {
			throw new ArgeoException(
					"Cannot create network node named " + name, re);
		}
	}

	public Repository getRepository() {
		return repository;
	}

	/* DEPENDENCY INJECTION */

	public void setSiteTypes(List<String> siteTypes) {
		this.siteTypes = siteTypes;
	}

	public void setManagedRoles(Map<Integer, String> managedRoles) {
		this.managedRoles = managedRoles;
	}

	public void setTestData(Resource testData) {
		this.testData = testData;
	}

	public void setMonitoredPic(Resource monitoredPic) {
		this.monitoredPic = monitoredPic;
	}

	public void setVisitedPic(Resource visitedPic) {
		this.visitedPic = visitedPic;
	}

	public void setRegisteredPic(Resource registeredPic) {
		this.registeredPic = registeredPic;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	/** Loads test data */
	protected class TestDataParser extends CsvParserWithLinesAsMap {
		@Override
		protected void processLine(Integer lineNumber, Map<String, String> line) {
			try {
				Node networks = adminSession.getNode(GR_NETWORKS_BASE_PATH);
				String networkName = line.get(GrTypes.GR_NETWORK);
				String networkUuid = line.get(GR_UUID);
				String networkNodeName = GrUtils.shortenUuid(networkUuid);
				Node network;
				if (!networks.hasNode(networkNodeName)) {
					network = networks.addNode(networkNodeName,
							GrTypes.GR_NETWORK);
					network.setProperty(GR_UUID, networkUuid);
					network.setProperty(Property.JCR_TITLE, networkName);
					network.getSession().save();
				} else {
					network = networks.getNode(networkNodeName);
				}

				String siteUuid = line.get(GrTypes.GR_SITE);
				String siteRelativePath = siteUuid.substring(0, 1) + '/'
						+ siteUuid;
				Node site;
				if (network.hasNode(siteRelativePath))
					return;// skip if present

				site = JcrUtils.mkdirs(network, siteRelativePath,
						GrTypes.GR_WATER_SITE);
				site.setProperty(GR_UUID, siteUuid);
				site.setProperty(Property.JCR_TITLE,
						GrUtils.shortenUuid(siteUuid));
				site.addNode(GR_SITE_COMMENTS, NodeType.NT_UNSTRUCTURED);
				String type = line.get(GR_SITE_TYPE);
				site.setProperty(GR_SITE_TYPE, type);

				// values
				site.setProperty(GR_WATER_LEVEL, generateRandomData(1d, 15d));
				site.setProperty(GR_ECOLI_RATE, generateRandomData(10d, 500d));
				site.setProperty(GR_WITHDRAWN_WATER,
						generateRandomData(0d, 10d));

				// pic
				if (GrConstants.MONITORED.equals(type))
					addPic(site, monitoredPic);
				else if (GrConstants.VISITED.equals(type))
					addPic(site, visitedPic);
				else if (GrConstants.REGISTERED.equals(type))
					addPic(site, registeredPic);

				Node mainPoint = site.hasNode(GR_SITE_MAIN_POINT) ? site
						.getNode(GR_SITE_MAIN_POINT) : site.addNode(
						GR_SITE_MAIN_POINT, GrTypes.GR_POINT);
				mainPoint.setProperty(GR_WGS84_LATITUDE,
						line.get(GR_WGS84_LATITUDE));
				mainPoint.setProperty(GR_WGS84_LONGITUDE,
						line.get(GR_WGS84_LONGITUDE));
				GrUtils.syncPointGeometry(mainPoint);

				site.getSession().save();

				if (log.isDebugEnabled())
					log.debug("Test data: loaded " + site);

			} catch (RepositoryException e) {
				throw new ArgeoException("Cannot process line " + lineNumber
						+ " " + line, e);
			}
		}
	}

	private void addPic(Node site, Resource pic) {
		InputStream in = null;
		try {
			in = pic.getInputStream();
			JcrUtils.copyStreamAsFile(site, "SitePicture.jpg", in);
		} catch (IOException e) {
			throw new GrException("Cannot upload " + pic + " to " + site, e);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	private Double generateRandomData(Double min, Double max) {
		return min + random.nextDouble() * (max - min);
	}
}
