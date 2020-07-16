package org.argeo.documents.core;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.naming.ldap.LdapName;

import org.argeo.api.NodeConstants;
import org.argeo.api.NodeUtils;
import org.argeo.cms.auth.CurrentUser;
import org.argeo.connect.ConnectConstants;
import org.argeo.connect.core.AbstractAppService;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.documents.DocumentsConstants;
import org.argeo.documents.DocumentsException;
import org.argeo.documents.DocumentsNames;
import org.argeo.documents.DocumentsService;
import org.argeo.documents.DocumentsTypes;
import org.argeo.jcr.Jcr;
import org.argeo.jcr.JcrUtils;

/** Default backend for the Documents App */
public class DocumentsServiceImpl extends AbstractAppService implements DocumentsService {
	// private final static String NODE_PREFIX = "node://";

	@Override
	public Node publishEntity(Node parent, String nodeType, Node srcNode, boolean removeSrcNode)
			throws RepositoryException {
		return null;
	}

	@Override
	public String getAppBaseName() {
		return DocumentsConstants.DOCUMENTS_APP_LBL;
	}

	@Override
	public String getDefaultRelPath(Node entity) throws RepositoryException {
		throw new DocumentsException("No default relpath is defined for the Documents App");
	}

	@Override
	public String getDefaultRelPath(Session session, String nodeType, String id) {
		throw new DocumentsException("No default relpath is defined for the Documents App");
	}

	@Override
	public boolean isKnownType(Node entity) {
		if (ConnectJcrUtils.isNodeType(entity, NodeType.NT_FILE)
				|| ConnectJcrUtils.isNodeType(entity, NodeType.NT_FOLDER))
			return true;
		else
			return false;
	}

	@Override
	public boolean isKnownType(String nodeType) {
		if (NodeType.NT_FILE.equals(nodeType) || NodeType.NT_FOLDER.equals(nodeType))
			return true;
		else
			return false;
	}

	/* DOCUMENTS APP SPECIFIC METHODS */

	public Path[] getMyDocumentsPath(FileSystemProvider nodeFileSystemProvider, Session session) {
		Node home = NodeUtils.getUserHome(session);
		Path[] paths = { getPath(nodeFileSystemProvider, home) };
		return paths;
	}

	public Path[] getMyGroupsFilesPath(FileSystemProvider nodeFileSystemProvider, Session session) {
		try {
			List<Path> paths = new ArrayList<>();
			for (String dn : CurrentUser.roles()) {
				LdapName ln = new LdapName(dn);
				String cn = (String) ln.getRdn(ln.size() - 1).getValue();
				Node workgroupHome = NodeUtils.getGroupHome(session, cn);
				if (workgroupHome != null) {
					paths.add(getPath(nodeFileSystemProvider, workgroupHome));
//					Node documents = JcrUtils.mkdirs(workgroupHome, getAppBaseName(), NodeType.NT_FOLDER);
//					documents.addMixin(NodeType.MIX_TITLE);
//					if (session.hasPendingChanges()) {
//						documents.setProperty(Property.JCR_TITLE, cn);
//						session.save();
//					}
//					// Insure the correct subNode is there
//					paths.add(getPath(nodeFileSystemProvider, ConnectJcrUtils.getPath(documents)));
				}
			}
			return paths.toArray(new Path[0]);
		} catch (Exception e) {
			throw new DocumentsException("Cannot retrieve work group home paths", e);
		}
	}

	public Path[] getMyBookmarks(FileSystemProvider nodeFileSystemProvider, Session session) {
		try {
			Node bookmarkParent = getMyBookmarksParent(session);
			List<Path> bookmarks = new ArrayList<>();
			NodeIterator nit = bookmarkParent.getNodes();
			while (nit.hasNext()) {
				Node currBookmark = nit.nextNode();
				String uriStr = ConnectJcrUtils.get(currBookmark, DocumentsNames.DOCUMENTS_URI);
				URI uri = new URI(uriStr);
				bookmarks.add(getPath(nodeFileSystemProvider, uri));
			}
			return bookmarks.toArray(new Path[0]);
		} catch (URISyntaxException | RepositoryException e) {
			throw new DocumentsException("Cannot retrieve CurrentUser bookmarks", e);
		}
	}

	public Node[] getMyBookmarks(Session session) {
		try {
			Node bookmarkParent = getMyBookmarksParent(session);
			List<Node> bookmarks = new ArrayList<>();
			NodeIterator nit = bookmarkParent.getNodes();
			while (nit.hasNext()) {
				Node currBookmark = nit.nextNode();
				if (currBookmark.isNodeType(DocumentsTypes.DOCUMENTS_BOOKMARK)) {
					bookmarks.add(currBookmark);
				}
			}
			return bookmarks.toArray(new Node[0]);
		} catch (RepositoryException e) {
			throw new DocumentsException("Cannot retrieve CurrentUser bookmarks", e);
		}
	}

	public Node getMyBookmarksParent(Session session) {
		try {
			// tryAs is compulsory when not calling from the workbench
			// Repository repo = callingSession.getRepository();
			// session = CurrentUser.tryAs(() -> repo.login());
			// Node home = NodeUtils.getUserHome(session);
			//
			// // Insure the parent node is there.
			// String relPath = DocumentsConstants.SUITE_HOME_SYS_RELPATH + "/"
			// + DocumentsConstants.FS_BASE_NAME + "/"
			// + DocumentsConstants.FS_BOOKMARKS;

			// Insure the parent node is there.
			if (session.hasPendingChanges())
				throw new DocumentsException("Session must be clean to retrieve bookmarks");
			Node home = NodeUtils.getUserHome(session);
			String relPath = ConnectConstants.HOME_APP_SYS_RELPARPATH + "/" + DocumentsConstants.DOCUMENTS_APP_BASE_NAME
					+ "/" + DocumentsConstants.DOCUMENTS_BOOKMARKS;
			Node bookmarkParent = JcrUtils.mkdirs(home, relPath);
			if (session.hasPendingChanges())
				session.save();
			return bookmarkParent;
		} catch (RepositoryException e) {
			throw new DocumentsException("Cannot retrieve bookmark parent for session " + session, e);
		}
	}

	// private String getCurrentHomePath(Session callingSession) {
	// Session session = null;
	// try {
	// // tryAs is compulsory when not calling from the workbench
	// Repository repo = callingSession.getRepository();
	// session = CurrentUser.tryAs(() -> repo.login());
	// String homepath = NodeUtils.getUserHome(session).getPath();
	// return homepath;
	// } catch (Exception e) {
	// throw new DocumentsException("Cannot retrieve Current User Home Path",
	// e);
	// } finally {
	// JcrUtils.logoutQuietly(session);
	// }
	// }

	public Path getPath(FileSystemProvider nodeFileSystemProvider, Node node) {
		// try {
		// URI uri = new URI(NODE_PREFIX + nodePath);
		String fullPath = '/' + Jcr.getWorkspaceName(node) + Jcr.getPath(node);
		URI uri;
		try {
			uri = new URI(NodeConstants.SCHEME_NODE, null, fullPath, null);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("Cannot interpret " + fullPath + " as an URI", e);
		}
		return getPath(nodeFileSystemProvider, uri);

		// FileSystem fileSystem = nodeFileSystemProvider.getFileSystem(uri);
		// if (fileSystem == null)
		// fileSystem = nodeFileSystemProvider.newFileSystem(uri, null);
		// FileSystem fileSystem = nodeFileSystemProvider.getFileSystem(uri);
		// if (fileSystem == null)
		// FileSystem fileSystem = nodeFileSystemProvider.newFileSystem(uri, null);
		// Note that tryAs() is compulsory in the CMS.
		// fileSystem = CurrentUser.tryAs(() ->
		// nodeFileSystemProvider.newFileSystem(uri, null));
		// return fileSystem.getPath(nodePath);
		// } catch (IOException e) {
		// throw new RuntimeException("Unable to initialise file system for " +
		// nodePath, e);
		// }
	}

	public Node getNode(Session session, Path path) {
		assert path.getNameCount() > 0;
		String workspaceName = path.getName(0).toString();
		String jcrPath = '/' + path.subpath(1, path.getNameCount()).toString();
		try {
			if (workspaceName.equals(session.getWorkspace().getName())) {
				return session.getNode(jcrPath);
			} else {
				Session newSession = session.getRepository().login(workspaceName);
				// FIXME close it
				return newSession.getNode(jcrPath);
			}
		} catch (RepositoryException e) {
			throw new DocumentsException("Cannot get node from path " + path, e);
		}
	}

	public NodeIterator getLastUpdatedDocuments(Session session) {
		try {
			String qStr = "//element(*, " + ConnectJcrUtils.getLocalJcrItemName(NodeType.NT_FILE) + ")";
			qStr += " order by @" + ConnectJcrUtils.getLocalJcrItemName(Property.JCR_LAST_MODIFIED) + " descending";
			QueryManager queryManager = session.getWorkspace().getQueryManager();
			Query xpathQuery = queryManager.createQuery(qStr, ConnectConstants.QUERY_XPATH);
			xpathQuery.setLimit(8);
			NodeIterator nit = xpathQuery.execute().getNodes();
			return nit;
		} catch (RepositoryException e) {
			throw new DocumentsException("Unable to retrieve last updated documents", e);
		}
	}

	public Path getPath(FileSystemProvider nodeFileSystemProvider, URI uri) {
		try {
			FileSystem fileSystem = nodeFileSystemProvider.getFileSystem(uri);
			if (fileSystem == null)
				fileSystem = nodeFileSystemProvider.newFileSystem(uri, null);
			// TODO clean this
			// String path = uri.toString().substring(NODE_PREFIX.length());
			String path = uri.getPath();
			return fileSystem.getPath(path);
		} catch (IOException e) {
			throw new DocumentsException("Unable to initialise file system for " + uri, e);
		}
	}

	public Node createFolderBookmark(Path path, String name, Repository repository) {
		Session session = null;
		try {
			session = repository.login(NodeConstants.HOME_WORKSPACE);
			Node bookmarkParent = getMyBookmarksParent(session);
			// String uriStr = NODE_PREFIX + path.toString();
			// uriStr = uriStr.replaceAll(" ", "%20");
			String nodeName = path.getFileName().toString();
			Node bookmark = bookmarkParent.addNode(nodeName);
			bookmark.addMixin(DocumentsTypes.DOCUMENTS_BOOKMARK);
			bookmark.setProperty(DocumentsNames.DOCUMENTS_URI, path.toUri().toString());
			bookmark.setProperty(Property.JCR_TITLE, name);
			session.save();
			return bookmark;
		} catch (RepositoryException e) {
			throw new DocumentsException("Cannot create bookmark for " + path + " with name " + name, e);
		} finally {
			JcrUtils.logoutQuietly(session);
		}
	}

//	private static URI nodePathToURI(String nodePath) {
//		try {
//			return new URI(NodeConstants.SCHEME_NODE, null, nodePath, null);
//		} catch (URISyntaxException e) {
//			throw new DocumentsException("Badly formatted path " + nodePath, e);
//		}
//	}
}
