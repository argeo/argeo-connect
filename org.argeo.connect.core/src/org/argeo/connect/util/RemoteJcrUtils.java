package org.argeo.connect.util;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.RepositoryFactory;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.nodetype.NodeType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.api.NodeUtils;
import org.argeo.connect.ConnectException;
import org.argeo.jcr.JcrUtils;

/** Centralize convenience methods to manage a remote JCR repository */
public class RemoteJcrUtils {
	private final static Log log = LogFactory.getLog(RemoteJcrUtils.class);

	/**
	 * Copy a node from the remote repository to the local repository, including
	 * all sub nodes. The target "toNode" must already exists in transient
	 * state.
	 * 
	 * @param fromNode
	 *            the node to be copied in the remote repository
	 * @param toNode
	 *            the target node in the local repository
	 */
	public static void copy(Node fromNode, Node toNode, boolean copyChildren) {
		try {
			if (toNode.getDefinition().isProtected())
				return;

			// Mixins
			for (NodeType mixinType : fromNode.getMixinNodeTypes()) {
				toNode.addMixin(mixinType.getName());
			}

			// Properties
			PropertyIterator pit = fromNode.getProperties();
			properties: while (pit.hasNext()) {
				Property fromProperty = pit.nextProperty();
				String propertyName = fromProperty.getName();
				if (toNode.hasProperty(propertyName) && toNode.getProperty(propertyName).getDefinition().isProtected())
					continue properties;

				if (fromProperty.getDefinition().isProtected())
					continue properties;

				if (propertyName.equals("jcr:created") || propertyName.equals("jcr:createdBy")
						|| propertyName.equals("jcr:lastModified") || propertyName.equals("jcr:lastModifiedBy"))
					continue properties;

				if (PropertyType.REFERENCE == fromProperty.getType()
						|| PropertyType.WEAKREFERENCE == fromProperty.getType()) {
					copyReference(fromProperty, toNode);
				} else if (fromProperty.isMultiple())
					toNode.setProperty(propertyName, fromProperty.getValues());
				else
					toNode.setProperty(propertyName, fromProperty.getValue());
			}

			// Child nodes
			if (copyChildren) {
				NodeIterator nit = fromNode.getNodes();
				while (nit.hasNext()) {
					Node fromChild = nit.nextNode();
					Integer index = fromChild.getIndex();
					String nodeRelPath = fromChild.getName() + "[" + index + "]";
					Node toChild;
					if (toNode.hasNode(nodeRelPath))
						toChild = toNode.getNode(nodeRelPath);
					else
						toChild = toNode.addNode(fromChild.getName(), fromChild.getPrimaryNodeType().getName());
					copy(fromChild, toChild, true);
				}
			}
			if (toNode.isNodeType(NodeType.MIX_LAST_MODIFIED))
				JcrUtils.updateLastModified(toNode);
			if (log.isTraceEnabled())
				log.trace("Copied " + toNode);
		} catch (RepositoryException e) {
			throw new ConnectException("Cannot copy " + fromNode + " to " + toNode, e);
		}
	}

	public static void copyReference(Property fromProperty, Node toNode) throws RepositoryException {
		String propertyName = fromProperty.getName();
		if (fromProperty.isMultiple()) {
			Value[] vals = fromProperty.getValues();
			List<Node> toNodes = new ArrayList<Node>();
			values: for (Value val : vals) {
				String fromRefJcrId = val.getString();
				Node oldReferencedNode = null;
				try {
					oldReferencedNode = toNode.getSession().getNodeByIdentifier(fromRefJcrId);
				} catch (ItemNotFoundException e) {
					log.warn("Cannot resolve reference for multi-valued property " + fromProperty + " with ID "
							+ fromRefJcrId + ". Corresponding value has not been copied.");
					continue values;
				}
				String oldRefPath = oldReferencedNode.getPath();
				Session toSession = toNode.getSession();
				if (toSession.nodeExists(oldRefPath))
					toNodes.add(toSession.getNode(oldRefPath));
				else
					log.warn("Node with path: " + oldRefPath + " does not exist in target repository. "
							+ " Reference on this node while copying property " + fromProperty + " has been ignored.");
			}
			if (!toNodes.isEmpty())
				setMultipleReferences(toNode, propertyName, toNodes);
		} else {
			String oldRefPath = fromProperty.getNode().getPath();
			Session toSession = toNode.getSession();
			if (toSession.nodeExists(oldRefPath)) {
				Node newRef = toSession.getNode(oldRefPath);
				// log.warn("Skiping ref prop copy: " + fromProperty.getPath());
				toNode.setProperty(propertyName, newRef);
			} else
				log.warn("Cannot resolve reference for property " + fromProperty.getPath()
						+ ". It has not been copied.");
		}
	}

	public static void setMultipleReferences(Node node, String propertyName, List<Node> nodes)
			throws RepositoryException {
		ValueFactory vFactory = node.getSession().getValueFactory();
		int size = nodes.size();
		Value[] values = new Value[size];
		int i = 0;
		for (Node currNode : nodes) {
			Value val = vFactory.createValue(currNode.getIdentifier(), PropertyType.REFERENCE);
			values[i++] = val;
		}
		node.setProperty(propertyName, values);
	}

	/**
	 * Wraps session opening on a remote repository. The caller must close the
	 * session
	 */
	public static Session getSessionOnRemote(RepositoryFactory repositoryFactory, String repoUrl, String wkspName,
			String login, char[] pwd) throws RepositoryException {
		Repository remoteRepo = NodeUtils.getRepositoryByUri(repositoryFactory, repoUrl);
		SimpleCredentials sc = new SimpleCredentials(login, pwd);
		return remoteRepo.login(sc, wkspName);
	}
}
