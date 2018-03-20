package org.argeo.documents.workbench.parts;

import java.nio.file.spi.FileSystemProvider;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.argeo.cms.ui.CmsEditable;
import org.argeo.connect.ui.SystemWorkbenchService;
import org.argeo.connect.workbench.NodeEditorInput;
import org.argeo.documents.DocumentsException;
import org.argeo.documents.DocumentsService;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

/**
 * Base Editor for a Documents entity. Centralise methods to ease business
 * specific development
 */
public abstract class AbstractDocumentsEditor extends EditorPart implements CmsEditable {

	/* DEPENDENCY INJECTION */
	private Repository repository;
	private SystemWorkbenchService systemWorkbenchService;
	private DocumentsService documentsService;
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
			throw new DocumentsException("Unable to create new session" + " to use with current editor", e);
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
			throw new DocumentsException("Cannot retrieve name for " + node, e);
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

	protected DocumentsService getDocumentsService() {
		return documentsService;
	}

	protected SystemWorkbenchService getSystemWorkbenchService() {
		return systemWorkbenchService;
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

	public void setDocumentsService(DocumentsService documentsService) {
		this.documentsService = documentsService;
	}

	public void setSystemWorkbenchService(SystemWorkbenchService systemWorkbenchService) {
		this.systemWorkbenchService = systemWorkbenchService;
	}

	public void setNodeFileSystemProvider(FileSystemProvider nodeFileSystemProvider) {
		this.nodeFileSystemProvider = nodeFileSystemProvider;
	}
}
