package org.argeo.activities.workbench.parts;

import java.nio.file.spi.FileSystemProvider;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.argeo.activities.ActivitiesException;
import org.argeo.activities.ActivitiesService;
import org.argeo.cms.ui.CmsEditable;
import org.argeo.connect.workbench.AppWorkbenchService;
import org.argeo.connect.workbench.NodeEditorInput;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

/**
 * Base Editor for a tracker entity. Centralise some methods to ease business
 * specific development
 */
public abstract class AbstractActivityEditor extends EditorPart implements CmsEditable {

	/* DEPENDENCY INJECTION */
	private Repository repository;
	private AppWorkbenchService appWorkbenchService;
	private ActivitiesService activitiesService;
	private FileSystemProvider nodeFileSystemProvider;

	// Context
	private Session session;
	private Node node;

	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
		try {
			session = repository.login();
			NodeEditorInput sei = (NodeEditorInput) input;
			String uid = sei.getUid();
			node = session.getNodeByIdentifier(uid);
			// Set a default part name and tooltip
			updatePartName();
		} catch (RepositoryException e) {
			throw new ActivitiesException("Unable to create new session" + " to use with current editor", e);
		}
	}

	protected void updatePartName() {
		try {
			String name;
			if (node.isNodeType(NodeType.MIX_TITLE) && node.hasProperty(Property.JCR_TITLE))
				name = node.getProperty(Property.JCR_TITLE).getString();
			else
				name = node.getName();
			setPartName(name);
			updateToolTip(name);

		} catch (RepositoryException e) {
			throw new ActivitiesException("Cannot retrieve name for " + node, e);
		}
	}

	/** Overwrite to provide a specific part tooltip */
	protected void updateToolTip(String name) {
		((NodeEditorInput) getEditorInput()).setTooltipText("Display and edit " + name);
	}

	// Exposes
	protected Repository getRepository() {
		return repository;
	}

	protected Node getNode() {
		return node;
	}

	protected ActivitiesService getActivitiesService() {
		return activitiesService;
	}

	protected AppWorkbenchService getAppWorkbenchService() {
		return appWorkbenchService;
	}

	public FileSystemProvider getNodeFileSystemProvider() {
		return nodeFileSystemProvider;
	}

	// Editor life cycle
	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void dispose() {
		JcrUtils.logoutQuietly(session);
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public void setFocus() {
	}

	// CmsEditable LIFE CYCLE
	@Override
	public Boolean canEdit() {
		return true;
	}

	@Override
	public Boolean isEditing() {
		return true;
	}

	@Override
	public void startEditing() {
	}

	@Override
	public void stopEditing() {
	}

	/* DEPENDENCY INJECTION */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setActivitiesService(ActivitiesService activitiesService) {
		this.activitiesService = activitiesService;
	}

	public void setAppWorkbenchService(AppWorkbenchService appWorkbenchService) {
		this.appWorkbenchService = appWorkbenchService;
	}

	public void setNodeFileSystemProvider(FileSystemProvider nodeFileSystemProvider) {
		this.nodeFileSystemProvider = nodeFileSystemProvider;
	}
}
