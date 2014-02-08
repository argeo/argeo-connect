package org.argeo.photo.manager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteResultHandler;
import org.apache.commons.exec.ExecuteStreamHandler;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.argeo.jcr.JcrUtils;

/** JCR based picture manager. */
public class JcrPictureManager implements PictureManager {
	private Log log = LogFactory.getLog(JcrPictureManager.class);

	private static Set<String> swtImageExts = new HashSet<String>();
	static {
		swtImageExts.add("jpg");
		swtImageExts.add("jpeg");
		swtImageExts.add("gif");
		swtImageExts.add("png");
		swtImageExts.add("tif");
		swtImageExts.add("tiff");
	}

	private final static String JCR_REPO_RELATIVE_PATH = "/metadata/jcr";
	private String picturesBase = System.getProperty("user.home") + "/Pictures";
	private Repository repository;

	private String photoWorkspace = null;

	private Executor executor = new DefaultExecutor();

	// after initialization
	private Session session;

	public void init() {
		picturesBase = picturesBase.trim();
		String jcrRepoUri = "file://" + picturesBase + JCR_REPO_RELATIVE_PATH;
		try {
			session = repository.login(photoWorkspace);
		} catch (RepositoryException e) {
			throw new ArgeoException("Cannot access JCR repository "
					+ jcrRepoUri, e);
		}

		// dcraw

	}

	public void destroy() {
		JcrUtils.logoutQuietly(session);
	}

	public InputStream getPictureAsStream(String relativePath) {
		try {
			String fileName = JcrUtils.lastPathElement(relativePath);
			String ext = FilenameUtils.getExtension(fileName).toLowerCase();
			if (swtImageExts.contains(ext)) {
				Node content = session.getNode(relativePath).getNode(
						Node.JCR_CONTENT);
				if (content.isNodeType(NodeType.NT_ADDRESS))
					return new FileInputStream(pathToFile(relativePath));
				else if (content.isNodeType(NodeType.NT_RESOURCE))
					return content.getProperty(Property.JCR_DATA).getBinary()
							.getStream();
				else
					throw new ArgeoException("Don't know how to read "
							+ relativePath);
			} else {
				// extract thumbnail

				InputStream in = dcraw(relativePath, "-T -w");
				return new ByteArrayInputStream(IOUtils.toByteArray(in));
			}
		} catch (ArgeoException e) {
			throw e;
		} catch (Exception e) {
			throw new ArgeoException("Cannot get " + relativePath, e);
		}
	}

	protected File pathToFile(String relativePath) {
		try {
			return new File(picturesBase + '/' + relativePath)
					.getCanonicalFile();
		} catch (IOException e) {
			throw new ArgeoException("Cannot get file for " + relativePath, e);
		}
	}

	protected InputStream dcraw(String relativePath, String args) {

		final CommandLine commandLine = CommandLine.parse("dcraw -c " + args
				+ " " + pathToFile(relativePath).getPath());
		final ByteArrayOutputStream errBos = new ByteArrayOutputStream();
		if (log.isDebugEnabled())
			log.debug(commandLine.toString());

		PipedInputStream pipeIn = new PipedInputStream();
		try {
			// stdout
			final PipedOutputStream pipeOut = new PipedOutputStream(pipeIn);
			// stderr
			ExecuteStreamHandler streamHandler = new PumpStreamHandler(pipeOut,
					errBos) {
				protected Thread createPump(final InputStream is,
						final OutputStream os) {
					return createPump(is, os, true);
				}
			};
			executor.setStreamHandler(streamHandler);
			// executor.execute(commandLine);
			executor.execute(commandLine, new ExecuteResultHandler() {

				public void onProcessFailed(ExecuteException e) {
					IOUtils.closeQuietly(pipeOut);
					byte[] err = errBos.toByteArray();
					String errStr = new String(err);
					throw new ArgeoException("Process " + commandLine
							+ " failed (" + e.getExitValue() + "): " + errStr,
							e);
				}

				public void onProcessComplete(int exitValue) {
					IOUtils.closeQuietly(pipeOut);
					if (log.isTraceEnabled())
						log.trace("'" + commandLine
								+ "' executed withotu error");
				}
			});
			return pipeIn;
		} catch (ExecuteException e) {
			byte[] err = errBos.toByteArray();
			String errStr = new String(err);
			throw new ArgeoException("Process " + commandLine + " failed ("
					+ e.getExitValue() + "): " + errStr, e);
		} catch (Exception e) {
			byte[] err = errBos.toByteArray();
			String errStr = new String(err);
			throw new ArgeoException("Process " + commandLine + " failed: "
					+ errStr, e);
		} finally {
			IOUtils.closeQuietly(errBos);
		}
	}

	public List<Node> getChildren(String relativePath) {
		try {
			File dir = new File(picturesBase + "/" + relativePath);
			Node baseNode = JcrUtils.mkfolders(session, relativePath);
			SortedMap<String, Node> dirs = new TreeMap<String, Node>();
			SortedMap<String, Node> files = new TreeMap<String, Node>();
			for (File file : dir.listFiles()) {
				Node node = null;
				if (baseNode.hasNode(file.getName())) {
					node = baseNode.getNode(file.getName());
					if (file.isDirectory()
							&& !node.isNodeType(NodeType.NT_FOLDER)) {
						node.remove();
						node = null;
					} else if (!file.isDirectory()
							&& !node.isNodeType(NodeType.NT_FILE)) {
						node.remove();
						node = null;
					}
				}
				if (node == null)
					if (file.isDirectory()) {
						node = addDirectory(baseNode, file);
					} else {
						node = addFileAddress(baseNode, file);
					}

				if (file.isDirectory())
					dirs.put(file.getName(), node);
				else
					files.put(file.getName(), node);
			}
			List<Node> children = new ArrayList<Node>();
			children.addAll(dirs.values());
			children.addAll(files.values());
			return children;
		} catch (RepositoryException e) {
			throw new ArgeoException("Cannot get children of " + relativePath,
					e);
		}
	}

	public Boolean hasChildren(String relativePath) {
		File file = new File(picturesBase + "/" + relativePath);
		if (file.exists() && file.isDirectory()) {
			if (file.list().length > 0)
				return true;
		}
		return false;
	}

	protected Node addFileAddress(Node baseDir, File file)
			throws RepositoryException {
		Node addressFile = baseDir.addNode(file.getName(), NodeType.NT_FILE);
		Node address = addressFile.addNode(Node.JCR_CONTENT,
				NodeType.NT_ADDRESS);
		address.setProperty(Property.JCR_PROTOCOL, "file");
		addressFile.getSession().save();
		return addressFile;
	}

	protected Node addDirectory(Node baseDir, File file)
			throws RepositoryException {
		return baseDir.addNode(file.getName(), NodeType.NT_FOLDER);
	}

	public void setPicturesBase(String picturesBase) {
		this.picturesBase = picturesBase;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setPhotoWorkspace(String photoWorkspace) {
		this.photoWorkspace = photoWorkspace;
	}

	class DcrawToStdout implements Runnable {
		public void run() {

		}
	}
}
