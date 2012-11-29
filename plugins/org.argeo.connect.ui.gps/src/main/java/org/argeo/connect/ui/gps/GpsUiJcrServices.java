package org.argeo.connect.ui.gps;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.ArgeoException;
import org.argeo.connect.ConnectConstants;
import org.argeo.connect.ConnectNames;
import org.argeo.connect.ConnectTypes;
import org.argeo.connect.gpx.JcrSessionUtils;
import org.argeo.jcr.JcrUtils;
import org.argeo.jcr.UserJcrUtils;

/**
 * Centralizes UI specific methods to manage the Connect Gps UI and
 * corresponding model.
 */
public class GpsUiJcrServices {
	// private final static Log log = LogFactory.getLog(GpsUiJcrServices.class);

	/* DEPENDENCY INJECTION */
	private Session jcrSession;

	/* CLEAN GPS SESSION MANAGEMENT */
	/**
	 * Returns the technical name of the given GPS clean session : it is the
	 * corresponding jcr node name
	 */
	public String getCleanSessionTechName(Node currCleanSession) {
		try {
			return currCleanSession.getName();
		} catch (RepositoryException re) {
			throw new ArgeoException(
					"Unexpected error while retrieving GPS clean session name.",
					re);
		}
	}

	/**
	 * Returns the display name of a given GPS clean session or the technical
	 * name if no display name has been defined
	 */
	public String getCleanSessionDisplayName(Node currCleanSession) {
		try {
			if (currCleanSession.hasProperty(Property.JCR_TITLE))
				return currCleanSession.getProperty(Property.JCR_TITLE)
						.getString();
			else
				return currCleanSession.getName();
		} catch (RepositoryException re) {
			throw new ArgeoException("Error while getting the GPS "
					+ "clean session display name", re);
		}
	}

	/**
	 * 
	 * @return the list of all distinct values of the given property that are
	 *         listed in all sessions imported files linked to the given
	 *         referential.
	 */
	public List<String> getCatalogFromRepo(Node currLocalRepo,
			String propertyName) {
		List<String> values = new ArrayList<String>();
		try {
			NodeIterator ni = currLocalRepo.getNodes();
			while (ni.hasNext()) {
				Node currSess = ni.nextNode();
				if (currSess
						.isNodeType(ConnectTypes.CONNECT_CLEAN_TRACK_SESSION)) {
					values.addAll(getCatalogFromSession(currSess, propertyName));
				}
			}
		} catch (RepositoryException e) {
			throw new ArgeoException(
					"unexpected error while retrieving sensor list.", e);
		}
		return values;
	}

	/**
	 * 
	 * @return the list of all distinct values of the given property that are
	 *         listed in the given session's imported files.
	 */
	public List<String> getCatalogFromSession(Node currCleanSession,
			String propertyName) {
		List<String> values = new ArrayList<String>();
		try {
			NodeIterator fi = currCleanSession.getNodes();
			while (fi.hasNext()) {
				Node currFile = fi.nextNode();
				if (currFile.isNodeType(ConnectTypes.CONNECT_FILE_TO_IMPORT)
						&& currFile.hasProperty(propertyName)) {
					String value = currFile.getProperty(propertyName)
							.getString();
					if (value != null && !values.contains(value)) {
						values.add(value);
					}
				}

			}

		} catch (RepositoryException e) {
			throw new ArgeoException(
					"unexpected error while retrieving catalog.", e);
		}
		return values;
	}

	/** returns the read-only status of the given clean data session */
	public boolean isSessionComplete(Node gpsCleanSession) {
		try {
			return gpsCleanSession.getProperty(
					ConnectNames.CONNECT_IS_SESSION_COMPLETE).getBoolean();
		} catch (RepositoryException re) {
			throw new ArgeoException("Error while checking "
					+ "if the session has been completed", re);
		}
	}

	/** sets the read-only status of the given clean data session */
	public void setSessionComplete(Node gpsCleanSession, boolean isComplete) {
		try {
			gpsCleanSession.setProperty(
					ConnectNames.CONNECT_IS_SESSION_COMPLETE, isComplete);
		} catch (RepositoryException re) {
			throw new ArgeoException("Error while updating the read-only "
					+ "status of the session.", re);
		}
	}

	/* LOCAL REPOSITORY MANAGEMENT */
	/**
	 * Returns the node corresponding to the local Repository linked to the
	 * current session
	 * 
	 * Might return null if no referential has already been linked to the
	 * current GPS clean session
	 */
	public Node getLinkedReferential(Node currCleanSession) {
		try {
			String techName = getLinkedReferentialTechName(currCleanSession);
			Node localReposPar = getLocalRepositoriesParentNode();
			if (techName == null || localReposPar == null)
				return null;
			return localReposPar.getNode(techName);
		} catch (RepositoryException re) {
			throw new ArgeoException("Unexpected error while getting the name "
					+ " of local referential that is linked to "
					+ "the current GPS clean session.", re);
		}
	}

	/**
	 * Returns the technical name of the local Repository linked to the current
	 * session
	 * 
	 * Might return null if no referential has already been linked to the
	 * current GPS clean session
	 */
	public String getLinkedReferentialTechName(Node currCleanSession) {
		try {
			if (!currCleanSession
					.hasProperty(ConnectNames.CONNECT_LOCAL_REPO_NAME)
					|| "".equals(currCleanSession
							.getProperty(ConnectNames.CONNECT_LOCAL_REPO_NAME)
							.getString().trim())) {
				return null;
			} else
				return currCleanSession.getProperty(
						ConnectNames.CONNECT_LOCAL_REPO_NAME).getString();
		} catch (RepositoryException re) {
			throw new ArgeoException("Unexpected error while getting the name "
					+ " of local referential that is linked to "
					+ "the current GPS clean session.", re);
		}
	}

	/**
	 * Returns the display name of a given local Repository or the technical
	 * name if no display name has been defined
	 */
	public String getReferentialDisplayName(Node referential) {
		try {
			if (referential.hasProperty(Property.JCR_TITLE))
				return referential.getProperty(Property.JCR_TITLE).getString();
			else
				return getReferentialTechName(referential);
		} catch (RepositoryException re) {
			throw new ArgeoException("Unexpected error while retrieving "
					+ "local referential display name.", re);
		}
	}

	/** Returns the technical name of a given local Repository */
	public String getReferentialTechName(Node referential) {
		try {
			return referential.getName();
		} catch (RepositoryException re) {
			throw new ArgeoException("Unexpected error while retrieving "
					+ "local referential technical name.", re);
		}
	}

	/* FIRST INITIALIZATION OF THE FRAMEWORK */
	public void initializeLocalRepository() {
		try {
			String username = jcrSession.getUserID();
			Node userHomeDirectory = UserJcrUtils.getUserHome(jcrSession,
					username);
			String userHomePath = userHomeDirectory.getPath();

			// Clean track sessions
			String sessionbasePath = userHomePath
					+ ConnectConstants.TRACK_SESSIONS_PARENT_PATH;
			if (!jcrSession.nodeExists(sessionbasePath)) {
				int lastIndex = sessionbasePath.lastIndexOf("/");
				Node parFolder = JcrUtils.mkdirs(jcrSession,
						sessionbasePath.substring(0, lastIndex));
				parFolder.addNode(sessionbasePath.substring(lastIndex + 1),
						ConnectTypes.CONNECT_SESSION_REPOSITORY);
			}

			// Local repository for clean data with default already created
			// directory
			String localRepoBasePath = userHomePath
					+ ConnectConstants.LOCAL_REPO_PARENT_PATH;
			if (!jcrSession.nodeExists(localRepoBasePath)) {
				int lastIndex = localRepoBasePath.lastIndexOf("/");
				Node parFolder = JcrUtils.mkdirs(jcrSession,
						localRepoBasePath.substring(0, lastIndex));
				Node repos = parFolder.addNode(
						localRepoBasePath.substring(lastIndex + 1),
						ConnectTypes.CONNECT_LOCAL_REPOSITORIES);
				JcrSessionUtils.createLocalRepository(repos, "main", "Default");

			}

			// Gpx base directory
			if (!jcrSession.nodeExists(ConnectConstants.GPX_FILE_DIR_PATH)) {
				int lastIndex = ConnectConstants.GPX_FILE_DIR_PATH
						.lastIndexOf("/");
				Node parFolder = JcrUtils.mkdirs(jcrSession,
						ConnectConstants.GPX_FILE_DIR_PATH.substring(0,
								lastIndex));
				parFolder.addNode(ConnectConstants.GPX_FILE_DIR_PATH
						.substring(lastIndex + 1),
						ConnectTypes.CONNECT_FILE_REPOSITORY);
			}
			jcrSession.save();
		} catch (RepositoryException re) {
			JcrUtils.discardQuietly(jcrSession);
			throw new ArgeoException("Error while initializing jcr repository",
					re);
		}
	}

	public Node getTrackSessionsParentNode() {
		try {
			Node userHomeDirectory = UserJcrUtils.getUserHome(jcrSession);
			if (userHomeDirectory == null)
				return null;
			String sessionbasePath = userHomeDirectory.getPath()
					+ ConnectConstants.TRACK_SESSIONS_PARENT_PATH;
			if (jcrSession.nodeExists(sessionbasePath))
				return jcrSession.getNode(sessionbasePath);
			else
				return null;
		} catch (RepositoryException re) {
			throw new ArgeoException(
					"Error while getting track session parent node.", re);
		}
	}

	public Node getLocalRepositoriesParentNode() {
		try {
			Node userHomeDirectory = UserJcrUtils.getUserHome(jcrSession);
			if (userHomeDirectory == null)
				return null;
			String sessionbasePath = userHomeDirectory.getPath()
					+ ConnectConstants.LOCAL_REPO_PARENT_PATH;
			if (jcrSession.nodeExists(sessionbasePath))
				return jcrSession.getNode(sessionbasePath);
			else
				return null;
		} catch (RepositoryException re) {
			throw new ArgeoException(
					"Error while getting local repositories parent node.", re);
		}
	}

	public Node getGpxFilesDirectory() {
		try {
			if (jcrSession.nodeExists(ConnectConstants.GPX_FILE_DIR_PATH))
				return jcrSession.getNode(ConnectConstants.GPX_FILE_DIR_PATH);
			else
				return null;
		} catch (RepositoryException re) {
			throw new ArgeoException(
					"Error while getting track session parent node.", re);
		}
	}

	/* Exposes injected objects */
	/** exposes injected session */
	public Session getJcrSession() {
		return jcrSession;
	}

	/* DEPENDENCY INJECTION */
	public void setJcrSession(Session jcrSession) {
		this.jcrSession = jcrSession;
	}
}