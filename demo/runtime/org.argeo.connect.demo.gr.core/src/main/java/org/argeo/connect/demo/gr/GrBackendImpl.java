package org.argeo.connect.demo.gr;

import java.io.File;
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
import org.argeo.connect.demo.gr.pdf.SiteReportPublisher;
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
				adminSession.save();
			}

			if (testData != null)
				new TestDataParser().parse(testData.getInputStream());
		} catch (Exception e) {
			JcrUtils.logoutQuietly(adminSession);
			throw new ArgeoException("Cannot initialize backend", e);
		}
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

	public File getSiteReport(String siteUid) {
		SiteReportPublisher srp = new SiteReportPublisher(this);
		return srp.createNewReport(siteUid);
	}

	public File getFileFromNode(Node node) {
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

	public Session getCurrentSession() {
		return adminSession;
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
				site.setProperty(GR_SITE_TYPE, line.get(GR_SITE_TYPE));

				// values
				site.setProperty(GR_WATER_LEVEL, generateRandomData(1d, 15d));
				site.setProperty(GR_ECOLI_RATE, generateRandomData(10d, 500d));
				site.setProperty(GR_WITHDRAWN_WATER,
						generateRandomData(0d, 10d));

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

	private Double generateRandomData(Double min, Double max) {
		return min + random.nextDouble() * (max - min);
	}
}
