package org.argeo.tracker.e4.parts;

import static org.argeo.eclipse.ui.EclipseUiUtils.notEmpty;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.VersionManager;

import org.argeo.activities.ActivitiesService;
import org.argeo.cms.auth.CurrentUser;
import org.argeo.cms.ui.CmsEditable;
import org.argeo.cms.ui.eclipse.forms.AbstractFormPart;
import org.argeo.cms.ui.eclipse.forms.IFormPart;
import org.argeo.cms.ui.eclipse.forms.IManagedForm;
import org.argeo.connect.AppService;
import org.argeo.connect.ConnectConstants;
import org.argeo.connect.ConnectException;
import org.argeo.connect.UserAdminService;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.ui.AppWorkbenchService;
import org.argeo.connect.ui.IStatusLineProvider;
import org.argeo.connect.ui.Refreshable;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.documents.DocumentsService;
import org.argeo.jcr.JcrUtils;
import org.argeo.tracker.TrackerException;
import org.argeo.tracker.TrackerRole;
import org.argeo.tracker.TrackerService;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;

/**
 * Base Editor for a tracker entity. Centralise some methods to ease business
 * specific development
 */
public abstract class AbstractTrackerEditor implements CmsEditable, Refreshable, IStatusLineProvider {
	// private static final long serialVersionUID = -6765842363698806619L;

	/* DEPENDENCY INJECTION */
	@Inject
	private Repository repository;
	@Inject
	private UserAdminService userAdminService;
	@Inject
	private ResourcesService resourcesService;
	@Inject
	private ActivitiesService activitiesService;
	@Inject
	private DocumentsService documentsService;
	@Inject
	private TrackerService trackerService;

	private AppService appService;
	private AppWorkbenchService appWorkbenchService;

	@Inject
	private MPart mPart;

	// Context
	private Session session;
	private Node node;

	private final static DateFormat df = new SimpleDateFormat(ConnectConstants.DEFAULT_DATE_TIME_FORMAT);

	public void init() {
		try {
			session = repository.login();
			// EntityEditorInput sei = (EntityEditorInput) getEditorInput();
			// FIXME
			String uid = null;
			node = session.getNodeByIdentifier(uid);
			// Set a default part name and tooltip
			updatePartName();
			updateToolTip();
		} catch (RepositoryException e) {
			throw new TrackerException("Unable to create new session" + " to use with current editor", e);
		}
	}

	/** Overwrite to provide a specific part Name */
	protected void updatePartName() {
		String name = ConnectJcrUtils.get(node, Property.JCR_TITLE);
		setPartName(name);
	}

	protected void setPartName(String name) {
		if (notEmpty(name))
			mPart.setLabel(name);
	}

	/** Overwrite to provide a specific part tooltip */
	protected void updateToolTip() {
		// EntityEditorInput sei = (EntityEditorInput) getEditorInput();
		// String displayName = ConnectJcrUtils.get(node, Property.JCR_TITLE);
		// if (isEmpty(displayName))
		// displayName = "current objet";
		// sei.setTooltipText("Display and edit information for " + displayName);
	}

	protected abstract void addPages();

	protected void addPage(AbstractEditorPage page) {
		// TODO implement
	}

	protected void commitPages(boolean b) {
		// TODO implement
	}

	protected AbstractEditorPage getActivePageInstance() {
		// TODO implement
		return null;
	}

	@Override
	public String getStatusLineMessage() {
		Node currNode = getNode();
		StringBuilder builder = new StringBuilder();
		try {
			if (currNode.isNodeType(NodeType.MIX_TITLE)) {
				builder.append(ConnectJcrUtils.get(currNode, Property.JCR_TITLE)).append(" - ");
			}
			if (currNode.isNodeType(NodeType.MIX_LAST_MODIFIED)) {
				builder.append("Last updated on ");
				builder.append(df.format(currNode.getProperty(Property.JCR_LAST_MODIFIED).getDate().getTime()));
				builder.append(", by ");
				String lstModByDn = currNode.getProperty(Property.JCR_LAST_MODIFIED_BY).getString();
				builder.append(userAdminService.getUserDisplayName(lstModByDn));
				builder.append(". ");
			}
			return builder.toString();
		} catch (RepositoryException re) {
			throw new ConnectException("Unable to create last " + "modified message for " + currNode, re);
		}
	}

	// Exposes
	protected Node getNode() {
		return node;
	}

	protected Session getSession() {
		return ConnectJcrUtils.getSession(node);
	}

	protected Repository getRepository() {
		return repository;
	}

	protected UserAdminService getUserAdminService() {
		return userAdminService;
	}

	protected ResourcesService getResourcesService() {
		return resourcesService;
	}

	protected DocumentsService getDocumentsService() {
		return documentsService;
	}

	protected ActivitiesService getActivitiesService() {
		return activitiesService;
	}

	protected TrackerService getTrackerService() {
		return trackerService;
	}

	protected AppWorkbenchService getAppWorkbenchService() {
		return appWorkbenchService;
	}

	protected AppService getAppService() {
		return appService;
	}

	// Editor life cycle
	@Persist
	public void doSave(IProgressMonitor monitor) {
		// Perform pre-saving specific action in each form part
		commitPages(true);
		// Effective save
		try {
			boolean changed = false;
			Session session = getNode().getSession();
			if (session.hasPendingChanges()) {
				JcrUtils.updateLastModified(node);
				session.save();
				changed = true;
			}
			if (changed && ConnectJcrUtils.isVersionable(getNode())) {
				VersionManager vm = session.getWorkspace().getVersionManager();
				String path = getNode().getPath();
				vm.checkpoint(path);
			}
		} catch (RepositoryException re) {
			throw new ConnectException("Unable to perform check point on " + node, re);
		}
		
		//firePropertyChange(PROP_DIRTY);
	}

	@Override
	public void forceRefresh(Object object) {
		// TODO implement a better refresh mechanism
		IManagedForm mf = getActivePageInstance().getManagedForm();
		for (IFormPart part : mf.getParts())
			if (part instanceof AbstractFormPart)
				((AbstractFormPart) part).markStale();

		mf.refresh();
	}

	@PreDestroy
	public void dispose() {
		JcrUtils.logoutQuietly(session);
	}

	// CmsEditable LIFE CYCLE
	@Override
	public Boolean canEdit() {
		// TODO refine this
		String roleStr = TrackerRole.editor.dn();
		return CurrentUser.isInRole(roleStr);
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

	public void setUserAdminService(UserAdminService userAdminService) {
		this.userAdminService = userAdminService;
	}

	public void setResourcesService(ResourcesService resourcesService) {
		this.resourcesService = resourcesService;
	}

	public void setActivitiesService(ActivitiesService activitiesService) {
		this.activitiesService = activitiesService;
	}

	public void setDocumentsService(DocumentsService documentsService) {
		this.documentsService = documentsService;
	}

	public void setTrackerService(TrackerService trackerService) {
		this.trackerService = trackerService;
	}

	public void setAppService(AppService appService) {
		this.appService = appService;
	}

	public void setAppWorkbenchService(AppWorkbenchService appWorkbenchService) {
		this.appWorkbenchService = appWorkbenchService;
	}
}
