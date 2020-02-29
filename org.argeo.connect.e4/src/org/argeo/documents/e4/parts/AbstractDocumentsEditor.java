package org.argeo.documents.e4.parts;

import java.nio.file.spi.FileSystemProvider;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.cms.ui.CmsEditable;
import org.argeo.connect.e4.ConnectE4Constants;
import org.argeo.connect.ui.SystemWorkbenchService;
import org.argeo.documents.DocumentsException;
import org.argeo.documents.DocumentsService;
import org.argeo.jcr.JcrUtils;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.BrowserNavigation;

/**
 * Base Editor for a Documents entity. Centralise methods to ease business
 * specific development
 */
public abstract class AbstractDocumentsEditor implements CmsEditable {
	private final static Log log = LogFactory.getLog(AbstractDocumentsEditor.class);

	/* DEPENDENCY INJECTION */
	@Inject
	private Repository repository;
	@Inject
	private SystemWorkbenchService systemWorkbenchService;
	@Inject
	private DocumentsService documentsService;
	@Inject
	private FileSystemProvider nodeFileSystemProvider;

	@Inject
	private MPart mPart;

	// Context
	private Session session;
	private Node node;

	// FIXME RAP specific
	private BrowserNavigation browserNavigation;

	public void init() {
		String uid = mPart.getPersistedState().get(ConnectE4Constants.ENTITY_ID);
		String workspace = mPart.getPersistedState().get(ConnectE4Constants.WORKSPACE);
		try {
			session = repository.login(workspace);
			node = session.getNodeByIdentifier(uid);
			// Set a default part name and tooltip
			updatePartName();
		} catch (RepositoryException e) {
			throw new DocumentsException("Unable to create new session" + " to use with current editor", e);
		}
		browserNavigation = RWT.getClient().getService(BrowserNavigation.class);
	}

	protected void updatePartName() {
		try {
			String name;
			if (node.isNodeType(NodeType.MIX_TITLE) && node.hasProperty(Property.JCR_TITLE))
				name = node.getProperty(Property.JCR_TITLE).getString();
			else
				name = node.getName();
			mPart.setLabel(name);
			updateToolTip(name);

		} catch (RepositoryException e) {
			throw new DocumentsException("Cannot retrieve name for " + node, e);
		}
	}

	/** Overwrite to provide a specific part tooltip */
	protected void updateToolTip(String name) {
//		((NodeEditorInput) getEditorInput()).setTooltipText("Display and edit " + name);
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
	@PreDestroy
	public void dispose() {
		JcrUtils.logoutQuietly(session);
	}

	@Focus
	public void setFocus() {
		try {
			browserNavigation.pushState(node.getPath(), mPart.getLabel());
		} catch (RepositoryException e) {
			log.error("Cannot set client state", e);
		}
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
