package org.argeo.cms.text;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.cms.CmsException;
import org.argeo.cms.CmsNames;
import org.argeo.cms.CmsTypes;

/** Based on HTML with a few Wiki-like shortcuts. */
public class WikiTextInterpreter implements TextInterpreter, CmsNames {

	@Override
	public String read(Session session, String nodePath) {
		try {
			Node node = session.getNode(nodePath);
			if (node.isNodeType(CmsTypes.CMS_STYLED)) {
				return node.getProperty(CMS_CONTENT).getString();
			} else {
				throw new CmsException("Don't know how to interpret "
						+ nodePath);
			}
		} catch (RepositoryException e) {
			throw new CmsException("Cannot set content on " + nodePath, e);
		}
	}

	@Override
	public void write(Session session, String nodePath, String content) {
		try {
			Node node = session.getNode(nodePath);
			if (node.isNodeType(CmsTypes.CMS_STYLED)) {
				node.setProperty(CMS_CONTENT, content);
			} else {
				throw new CmsException("Don't know how to interpret "
						+ nodePath);
			}
			session.save();
		} catch (RepositoryException e) {
			throw new CmsException("Cannot set content on " + nodePath, e);
		}
	}

	protected String convertToStorage(Node node, String content) {
		return content;

	}

	protected String convertFromStorage(Node node, String content) {
		return content;

	}
}
