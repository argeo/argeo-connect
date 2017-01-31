package org.argeo.connect.documents;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.spi.FileSystemProvider;
import java.security.PrivilegedActionException;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.argeo.cms.auth.CurrentUser;
import org.argeo.jcr.JcrUtils;
import org.argeo.node.NodeUtils;

/** Default backend for the Argeo Documents App */
public class DocumentsService {
	private final static String NODE_PREFIX = "node://";

	public Path[] getMyDocumentsPath(FileSystemProvider nodeFileSystemProvider, Session session) {
		Path[] paths = { getPath(nodeFileSystemProvider, getMyDocumentsNodePath(session)) };
		return paths;
	}

	public Path[] getMyGroupsFilesPath(FileSystemProvider nodeFileSystemProvider, Session callingSession) {
		Session session = null;
		try {
			// tryAs is compulsory when not calling from the workbench
			Repository repo = callingSession.getRepository();
			session = CurrentUser.tryAs(() -> repo.login());
			Node home = NodeUtils.getUserHome(session);

			// Insure the parent node is there.
			if (!home.hasNode(DocumentsConstants.SUITE_DOCUMENTS_LBL))
				home.addNode(DocumentsConstants.SUITE_DOCUMENTS_LBL, NodeType.NT_FOLDER);

			// return home.getPath();
		} catch (Exception e) {
			throw new DocumentsException("Cannot retrieve Current User Home Path", e);
		} finally {
			JcrUtils.logoutQuietly(session);
		}

		// TODO
		Path[] paths = { Paths.get(System.getProperty("user.dir")), Paths.get("/tmp") };
		return paths;
	}

	public Path[] getMyBookmarks(FileSystemProvider nodeFileSystemProvider, Session callingSession) {

		Session session = null;
		try {
			// tryAs is compulsory when not calling from the workbench
			Repository repo = callingSession.getRepository();
			session = CurrentUser.tryAs(() -> repo.login());
			Node home = NodeUtils.getUserHome(session);

			// Insure the parent node is there.
			String relPath = DocumentsConstants.SUITE_HOME_SYS_RELPATH + "/" + DocumentsConstants.FS_BASE_NAME + "/"
					+ DocumentsConstants.FS_BOOKMARKS;
			Node bookmarkParent = JcrUtils.mkdirs(home, relPath);
			if (session.hasPendingChanges())
				session.save();

			List<Path> bookmarks = new ArrayList<>();
			NodeIterator nit = bookmarkParent.getNodes();
			while (nit.hasNext())

				if (!home.hasNode(DocumentsConstants.SUITE_DOCUMENTS_LBL))
					home.addNode(DocumentsConstants.SUITE_DOCUMENTS_LBL, NodeType.NT_FOLDER);

			// TODO
			Path[] paths = { Paths.get(System.getProperty("user.dir")), Paths.get("/tmp") };
			return paths;

		} catch (Exception e) {
			throw new DocumentsException("Cannot retrieve Current User Home Path", e);
		} finally {
			JcrUtils.logoutQuietly(session);
		}

	}

	private String getCurrentHomePath(Session callingSession) {
		Session session = null;
		try {
			// tryAs is compulsory when not calling from the workbench
			Repository repo = callingSession.getRepository();
			session = CurrentUser.tryAs(() -> repo.login());
			String homepath = NodeUtils.getUserHome(session).getPath();
			return homepath;
		} catch (Exception e) {
			throw new DocumentsException("Cannot retrieve Current User Home Path", e);
		} finally {
			JcrUtils.logoutQuietly(session);
		}
	}

	private Path getPath(FileSystemProvider nodeFileSystemProvider, String nodePath) {
		try {
			URI uri = new URI(NODE_PREFIX + nodePath);
			FileSystem fileSystem = nodeFileSystemProvider.getFileSystem(uri);
			if (fileSystem == null)
				// Note that tryAs() is useless in the workbench but compulsory
				// in the CMS.
				fileSystem = CurrentUser.tryAs(() -> nodeFileSystemProvider.newFileSystem(uri, null));
			return fileSystem.getPath(nodePath);
		} catch (URISyntaxException | PrivilegedActionException e) {
			throw new RuntimeException("Unable to initialise file system for " + nodePath, e);
		}
	}

	private String getMyDocumentsNodePath(Session callingSession) {
		Session session = null;
		try {
			Repository repo = callingSession.getRepository();
			session = CurrentUser.tryAs(() -> repo.login());
			Node home = NodeUtils.getUserHome(session);
			// Insure the parent node is there.
			if (!home.hasNode(DocumentsConstants.SUITE_DOCUMENTS_LBL))
				home.addNode(DocumentsConstants.SUITE_DOCUMENTS_LBL, NodeType.NT_FOLDER);
			return home.getPath() + "/" + DocumentsConstants.SUITE_DOCUMENTS_LBL;
		} catch (Exception e) {
			throw new DocumentsException("Cannot retrieve Current User Home Path", e);
		} finally {
			JcrUtils.logoutQuietly(session);
		}
	}
}
