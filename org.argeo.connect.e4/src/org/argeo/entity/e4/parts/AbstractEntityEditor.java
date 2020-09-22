package org.argeo.entity.e4.parts;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.Privilege;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.cms.CmsUserManager;
import org.argeo.cms.ui.CmsEditable;
import org.argeo.cms.ui.util.CmsUiUtils;
import org.argeo.connect.ConnectException;
import org.argeo.connect.e4.ConnectE4Constants;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.BrowserNavigation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/** Base class for all entity related editors. */
public abstract class AbstractEntityEditor implements CmsEditable {
	private final static Log log = LogFactory.getLog(AbstractEntityEditor.class);

	protected final static int SHORT_NAME_LENGTH = 10;

	@Inject
	private Repository repository;
	@Inject
	private CmsUserManager cmsUserManager;

	private Session session;

	@Inject
	private MPart mPart;
	@Inject
	private MDirtyable mDirtyable;
	private boolean isEditing;

	// Context
	private Node node;
	private String partName;

	private Composite main;

	// FIXME RAP specific
	private BrowserNavigation browserNavigation;

	public abstract Control createUi(Composite parent, Node context) throws RepositoryException;

	@PostConstruct
	public void createPartControl(Composite parent) {
		String entityId = mPart.getPersistedState().get(ConnectE4Constants.ENTITY_ID);
		init(entityId);
		init();

		try {
			parent.setLayout(CmsUiUtils.noSpaceGridLayout());
			main = new Composite(parent, SWT.NONE);
			main.setLayoutData(CmsUiUtils.fillAll());
			main.setLayout(new GridLayout());
			createUi(main, node);
		} catch (RepositoryException e) {
			throw new IllegalStateException("Cannot initialise UI", e);
		}
		browserNavigation = RWT.getClient().getService(BrowserNavigation.class);
	}

	@PreDestroy
	public void dispose() {
		try {
			if (node != null)
				JcrUtils.discardUnderlyingSessionQuietly(node);
		} finally {
			JcrUtils.logoutQuietly(session);
		}
		browserNavigation.pushState("~", null);
	}

	private void init(String entityId) {
		try {
			session = repository.login();
			if (entityId != null)
				node = getSession().getNodeByIdentifier(entityId);
			// Set a default part name and tooltip
			updatePartName();
			updateToolTip();
		} catch (RepositoryException e) {
			throw new ConnectException("Unable to create new session" + " to use with current editor", e);
		}
	}

	protected void init() {

	}

	@Focus
	public void setFocus() {
		if (node != null)
			try {
				browserNavigation.pushState('/' + node.getSession().getWorkspace().getName() + node.getPath(),
						partName);
			} catch (RepositoryException e) {
				log.error("Cannot set client state", e);
			}
	}

	/*
	 * EDITING CYCLE
	 */
	@Override
	public synchronized void startEditing() {
		if (!isEditing) {
			isEditing = true;
			notifyEditionStateChange();
			main.layout(true, true);
		}
	}

	@Override
	public synchronized void stopEditing() {
		if (isEditing) {
			isEditing = false;
			notifyEditionStateChange();
			main.layout(true, true);
		}
	}

	/**
	 * Notify the workbench that editable status as changed in order to update
	 * external extension (typically the toolbar buttons)
	 */
	protected void notifyEditionStateChange() {
		mDirtyable.setDirty(isEditing);
	}

	@Override
	public synchronized Boolean isEditing() {
		return isEditing;
	}

	@Override
	public Boolean canEdit() {
		try {
			AccessControlManager acm = session.getAccessControlManager();
			Privilege[] privs = { acm.privilegeFromName(Privilege.JCR_WRITE) };
			return acm.hasPrivileges(node.getPath(), privs);
		} catch (RepositoryException e) {
			throw new ConnectException("Unable to check privilege on " + node, e);
		}
	}

	/*
	 * UTILITIES
	 */
	/** Overwrite to provide a specific part Name */
	protected void updatePartName() {
		if (node != null) {
			String name = ConnectJcrUtils.get(node, Property.JCR_TITLE);
			if (EclipseUiUtils.notEmpty(name)) {
				if (name.length() > SHORT_NAME_LENGTH)
					name = name.substring(0, SHORT_NAME_LENGTH - 1) + "...";
				setPartName(name);
			}
		}
	}

	protected void setPartName(String name) {
		this.partName = name;
		mPart.setLabel(name);
	}

	/** Overwrite to provide a specific part tooltip */
	protected void updateToolTip() {
		if (node != null) {
			String displayName = ConnectJcrUtils.get(node, Property.JCR_TITLE);
			if (EclipseUiUtils.isEmpty(displayName))
				displayName = "current item";
			// EntityEditorInput sei = (EntityEditorInput) getEditorInput();
			// sei.setTooltipText("Display and edit information for " + displayName);
		}
	}

	/*
	 * Expose to children classes
	 */

	protected Session getSession() {
		return session;
	}

	protected Repository getRepository() {
		return repository;
	}

	/** Returns the entity Node that is bound to this editor */
	public Node getNode() {
		return node;
	}

	protected CmsUserManager getCmsUserManager() {
		return cmsUserManager;
	}

}
