package org.argeo.cms.text;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.cms.CmsException;
import org.argeo.cms.CmsNames;
import org.argeo.cms.CmsTypes;

/** Based on HTML with a few Wiki-like shortcuts. */
public class IdentityTextInterpreter implements TextInterpreter, CmsNames {

	@Override
	public Node write(Session session, String nodePath, String content) {
		try {
			Node node = session.getNode(nodePath);
			if (node.isNodeType(CmsTypes.CMS_STYLED)) {
				String raw = convertToStorage(node, content);
				node.setProperty(CMS_CONTENT, raw);
			} else {
				throw new CmsException("Don't know how to interpret " + node);
			}
			session.save();
			return node;
		} catch (RepositoryException e) {
			throw new CmsException("Cannot set content on " + nodePath, e);
		}
	}

	@Override
	public String read(Session session, String nodePath) {
		try {
			Node node = session.getNode(nodePath);
			String raw = raw(node);
			return convertFromStorage(node, raw);
		} catch (RepositoryException e) {
			throw new CmsException("Cannot get " + nodePath + " for edit", e);
		}
	}

	@Override
	public String raw(Node node) {
		try {
			if (node.isNodeType(CmsTypes.CMS_STYLED)) {
				return node.getProperty(CMS_CONTENT).getString();
			} else {
				throw new CmsException("Don't know how to interpret " + node);
			}
		} catch (RepositoryException e) {
			throw new CmsException("Cannot get " + node + " content", e);
		}
	}

	protected String convertToStorage(Node node, String content)
			throws RepositoryException {
		return content;

	}

	protected String convertFromStorage(Node node, String content)
			throws RepositoryException {
		return content;

	}
}
