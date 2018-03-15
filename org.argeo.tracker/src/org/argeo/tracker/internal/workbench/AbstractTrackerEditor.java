package org.argeo.tracker.internal.workbench;

import static org.argeo.eclipse.ui.EclipseUiUtils.isEmpty;
import static org.argeo.eclipse.ui.EclipseUiUtils.notEmpty;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

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
import org.argeo.connect.AppService;
import org.argeo.connect.ConnectConstants;
import org.argeo.connect.ConnectException;
import org.argeo.connect.UserAdminService;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.workbench.AppWorkbenchService;
import org.argeo.connect.workbench.Refreshable;
import org.argeo.connect.workbench.parts.IStatusLineProvider;
import org.argeo.connect.workbench.util.EntityEditorInput;
import org.argeo.documents.DocumentsService;
import org.argeo.jcr.JcrUtils;
import org.argeo.tracker.TrackerException;
import org.argeo.tracker.TrackerRole;
import org.argeo.tracker.TrackerService;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;

/**
 * Base Editor for a tracker entity. Centralise some methods to ease business
 * specific development
 */
public abstract class AbstractTrackerEditor extends FormEditor
		implements CmsEditable, Refreshable, IStatusLineProvider {
	private static final long serialVersionUID = -6765842363698806619L;

	/* DEPENDENCY INJECTION */
	private Repository repository;
	private UserAdminService userAdminService;
	private ResourcesService resourcesService;
	private ActivitiesService activitiesService;
	private DocumentsService documentsService;
	private TrackerService trackerService;
	private AppService appService;
	private AppWorkbenchService appWorkbenchService;

	// Context
	private Session session;
	private Node node;

	private final static DateFormat df = new SimpleDateFormat(ConnectConstants.DEFAULT_DATE_TIME_FORMAT);

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
			throw new ConnectException("Unable to perform check point on " + node, re);
		}
		firePropertyChange(PROP_DIRTY);
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
