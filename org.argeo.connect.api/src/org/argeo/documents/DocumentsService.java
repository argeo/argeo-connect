package org.argeo.documents;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Repository;
import javax.jcr.Session;

import org.argeo.connect.AppService;

public interface DocumentsService extends AppService {
	public Path[] getMyDocumentsPath(FileSystemProvider nodeFileSystemProvider, Session session);

	public Path[] getMyGroupsFilesPath(FileSystemProvider nodeFileSystemProvider, Session session);

	public Path[] getMyBookmarks(FileSystemProvider nodeFileSystemProvider, Session session);

	public Node[] getMyBookmarks(Session session);

	public Node getMyBookmarksParent(Session session);

	public Path getPath(FileSystemProvider nodeFileSystemProvider, String nodePath);

	public NodeIterator getLastUpdatedDocuments(Session session);

	public Path getPath(FileSystemProvider nodeFileSystemProvider, URI uri);

	public Node createFolderBookmark(Path path, String name, Repository repository);
}
