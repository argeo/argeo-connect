package org.argeo.cms.widgets;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.cms.CmsException;
import org.argeo.cms.CmsUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/** A composite which can (optionally) manage a Node. */
public class NodeComposite extends Composite {
	private static final long serialVersionUID = -1447009015451153367L;

	private final Session session;
	private String nodeId;
	private Node cache;

	/** Regular composite constructor */
	public NodeComposite(Composite parent, int style) {
		super(parent, style);
		session = null;
		nodeId = null;
	}

	public NodeComposite(Composite parent, int style, Node node)
			throws RepositoryException {
		this(parent, style, node, false);
	}

	public NodeComposite(Composite parent, int style, Node node,
			boolean cacheImmediately) throws RepositoryException {
		super(parent, style);
		this.session = node.getSession();
		if (!cacheImmediately && (SWT.READ_ONLY == (style & SWT.READ_ONLY))) {
			this.nodeId = null;
		} else {
			this.nodeId = node.getIdentifier();
			if (cacheImmediately)
				this.cache = node;
		}
		setLayout(CmsUtils.noSpaceGridLayout());
	}

	public synchronized Node getNode() throws RepositoryException {
		if (cache != null)
			return cache;
		else if (session != null)
			if (nodeId != null)
				return session.getNodeByIdentifier(nodeId);
			else
				return null;
		else
			return null;
	}

	/** Set/update the cache or change the node */
	public synchronized void setNode(Node node) throws RepositoryException {
		if (node == null) {// clear cache
			this.cache = null;
			return;
		}

		if (session == null || session != node.getSession())// check session
			throw new CmsException("Uncompatible session");

		if (nodeId == null || !nodeId.equals(node.getIdentifier())) {
			nodeId = node.getIdentifier();
			cache = node;
			nodeUpdated();
		} else {
			cache = node;// set/update cache
		}
	}

	public synchronized String getNodeId() {
		return nodeId;
	}

	/** Change the node, does nothing if same. */
	public synchronized void setNodeId(String nodeId)
			throws RepositoryException {
		if (this.nodeId != null && this.nodeId.equals(nodeId))
			return;
		this.nodeId = nodeId;
		if (cache != null)
			cache = session.getNodeByIdentifier(this.nodeId);
		nodeUpdated();
	}

	protected synchronized void nodeUpdated() {
		layout();
	}

	public Session getSession() {
		return session;
	}

}
