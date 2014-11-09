package org.argeo.cms.viewers;

import java.util.Observable;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.version.VersionManager;

import org.argeo.cms.CmsEditable;
import org.argeo.cms.CmsEditionEvent;
import org.argeo.cms.CmsException;
import org.eclipse.jface.viewers.ContentViewer;

/** Provides the CmsEditable semantic based on JCR versioning. */
public class JcrVersionCmsEditable extends Observable implements CmsEditable {
	private final String nodePath;// cache
	private final VersionManager versionManager;
	private final Boolean canEdit;

	public JcrVersionCmsEditable(Node node) {
		try {
			// if (node.isNodeType(NodeType.MIX_VERSIONABLE))
			// throw new CmsException(node + " is not versionable");

			this.nodePath = node.getPath();
			if (node.getSession().hasPermission(node.getPath(),
					Session.ACTION_ADD_NODE)) {
				canEdit = true;
				versionManager = node.getSession().getWorkspace()
						.getVersionManager();
			} else {
				canEdit = false;
				versionManager = null;
			}
		} catch (RepositoryException e) {
			throw new CmsException(
					"Cannot initialize CMS editable for " + node, e);
		}
	}

	@Override
	public Boolean canEdit() {
		return canEdit;
	}

	public Boolean isEditing() {
		try {
			if (!canEdit())
				return false;
			return versionManager.isCheckedOut(nodePath);
		} catch (RepositoryException e) {
			throw new CmsException("Cannot check whether " + nodePath
					+ " is editing", e);
		}
	}

	@Override
	public void startEditing() {
		try {
			versionManager.checkout(nodePath);
		} catch (RepositoryException e1) {
			throw new CmsException("Cannot publish " + nodePath);
		}
		notifyObservers(new CmsEditionEvent(nodePath,
				CmsEditionEvent.START_EDITING));
	}

	@Override
	public void stopEditing() {
		try {
			versionManager.checkin(nodePath);
		} catch (RepositoryException e1) {
			throw new CmsException("Cannot publish " + nodePath, e1);
		}
		notifyObservers(new CmsEditionEvent(nodePath,
				CmsEditionEvent.STOP_EDITING));
	}
}
