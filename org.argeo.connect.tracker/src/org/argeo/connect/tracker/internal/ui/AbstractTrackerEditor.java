package org.argeo.connect.tracker.internal.ui;

import static org.argeo.eclipse.ui.EclipseUiUtils.isEmpty;
import static org.argeo.eclipse.ui.EclipseUiUtils.notEmpty;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.version.VersionManager;

import org.argeo.cms.ui.CmsEditable;
import org.argeo.connect.UserAdminService;
import org.argeo.connect.activities.ActivitiesService;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.tracker.TrackerException;
import org.argeo.connect.tracker.TrackerService;
import org.argeo.connect.ui.workbench.AppWorkbenchService;
import org.argeo.connect.ui.workbench.parts.EntityEditorInput;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;

/**
 * Base Editor for a tracker entity. Centralise some methods to ease business
 * specific development
 */
public abstract class AbstractTrackerEditor extends FormEditor implements CmsEditable {
	private static final long serialVersionUID = -6765842363698806619L;

	/* DEPENDENCY INJECTION */
	private Repository repository;
	private UserAdminService userAdminService;
	private ResourcesService resourceService;
	private ActivitiesService activityService;
	private TrackerService trackerService;
	private AppWorkbenchService appWorkbenchService;

	// Context
	private Session session;
	private Node node;

	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		try {
			session = repository.login();
			EntityEditorInput sei = (EntityEditorInput) getEditorInput();
			node = session.getNodeByIdentifier(sei.getUid());
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
		if (notEmpty(name))
			setPartName(name);
	}

	/** Overwrite to provide a specific part tooltip */
	protected void updateToolTip() {
		EntityEditorInput sei = (EntityEditorInput) getEditorInput();
		String displayName = ConnectJcrUtils.get(node, Property.JCR_TITLE);
		if (isEmpty(displayName))
			displayName = "current objet";
		sei.setTooltipText("Display and edit information for " + displayName);
	}

	protected abstract void addPages();

	// Exposes
	protected Node getNode() {
		return node;
	}

	protected Repository getRepository() {
		return repository;
	}

	protected UserAdminService getUserAdminService() {
		return userAdminService;
	}

	protected ResourcesService getResourceService() {
		return resourceService;
	}

	protected ActivitiesService getActivityService() {
		return activityService;
	}

	protected TrackerService getTrackerService() {
		return trackerService;
	}

	protected AppWorkbenchService getAppWorkbenchService() {
		return appWorkbenchService;
	}

	// Editor life cycle
	@Override
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
			throw new PeopleException("Unable to perform check point on " + node, re);
		}
		firePropertyChange(PROP_DIRTY);
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
		super.dispose();
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

	public void setUserAdminService(UserAdminService userAdminService) {
		this.userAdminService = userAdminService;
	}

	public void setResourceService(ResourcesService resourceService) {
		this.resourceService = resourceService;
	}

	public void setActivityService(ActivitiesService activityService) {
		this.activityService = activityService;
	}

	public void setTrackerService(TrackerService trackerService) {
		this.trackerService = trackerService;
	}

	public void setAppWorkbenchService(AppWorkbenchService appWorkbenchService) {
		this.appWorkbenchService = appWorkbenchService;
	}
}
