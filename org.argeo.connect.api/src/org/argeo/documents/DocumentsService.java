package org.argeo.documents;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Repository;
import javax.jcr.Session;

import org.argeo.connect.AppService;

/** Document backend. */
public interface DocumentsService extends AppService {
	public Path[] getMyDocumentsPath(FileSystemProvider nodeFileSystemProvider, Session session);

	public Path[] getMyGroupsFilesPath(FileSystemProvider nodeFileSystemProvider, Session session);

	public Path[] getMyBookmarks(FileSystemProvider nodeFileSystemProvider, Session session);

	public Node[] getMyBookmarks(Session session);

	public Node getMyBookmarksParent(Session session);

	public Path getPath(FileSystemProvider nodeFileSystemProvider, Node context);

	/**
	 * Returns the node corresponding to this path. This will always open a new
	 * session, which should be closed bay the caller.
	 */
	public Node getNode(Repository repository, Path path);

	public NodeIterator getLastUpdatedDocuments(Session session);

	public Path getPath(FileSystemProvider nodeFileSystemProvider, URI uri);

	public Node createFolderBookmark(Path path, String name, Repository repository);
}
