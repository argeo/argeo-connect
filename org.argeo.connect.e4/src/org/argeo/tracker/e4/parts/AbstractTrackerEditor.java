package org.argeo.tracker.e4.parts;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.VersionManager;

import org.argeo.activities.ActivitiesService;
import org.argeo.cms.ui.CmsEditable;
import org.argeo.cms.ui.eclipse.forms.FormToolkit;
import org.argeo.cms.ui.eclipse.forms.IManagedForm;
import org.argeo.cms.ui.eclipse.forms.ManagedForm;
import org.argeo.connect.AppService;
import org.argeo.connect.ConnectConstants;
import org.argeo.connect.ConnectException;
import org.argeo.connect.SystemAppService;
import org.argeo.connect.e4.parts.AbstractConnectCTabEditor;
import org.argeo.connect.ui.AppWorkbenchService;
import org.argeo.connect.ui.IStatusLineProvider;
import org.argeo.connect.ui.Refreshable;
import org.argeo.connect.ui.SystemWorkbenchService;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.documents.DocumentsService;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;
import org.argeo.tracker.TrackerService;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Base Editor for a tracker entity. Centralise some methods to ease business
 * specific development
 */
public abstract class AbstractTrackerEditor extends AbstractConnectCTabEditor
		implements CmsEditable, Refreshable, IStatusLineProvider {
	// private static final long serialVersionUID = -6765842363698806619L;

	/* DEPENDENCY INJECTION */
	// @Inject
	// private Repository repository;
	// @Inject
	// private UserAdminService userAdminService;
	// @Inject
	// private ResourcesService resourcesService;
	@Inject
	private ActivitiesService activitiesService;
	@Inject
	private DocumentsService documentsService;
	@Inject
	private TrackerService trackerService;

	@Inject
	private SystemAppService appService;
	@Inject
	private SystemWorkbenchService appWorkbenchService;

	List<AbstractEditorPage> pages = new ArrayList<>();
	// @Inject
	// private MPart mPart;

	// Context
	// private Session session;
	// private Node node;

	private final static DateFormat df = new SimpleDateFormat(ConnectConstants.DEFAULT_DATE_TIME_FORMAT);

	// public void init() {
	// try {
	// session = repository.login();
	// // EntityEditorInput sei = (EntityEditorInput) getEditorInput();
	// // FIXME
	// String uid = null;
	// node = session.getNodeByIdentifier(uid);
	// // Set a default part name and tooltip
	// updatePartName();
	// updateToolTip();
	// } catch (RepositoryException e) {
	// throw new TrackerException("Unable to create new session" + " to use with
	// current editor", e);
	// }
	// }

	/** Overwrite to provide a specific part Name */
	// protected void updatePartName() {
	// String name = ConnectJcrUtils.get(node, Property.JCR_TITLE);
	// setPartName(name);
	// }

	// protected void setPartName(String name) {
	// if (notEmpty(name))
	// mPart.setLabel(name);
	// }

	/** Overwrite to provide a specific part tooltip */
	// protected void updateToolTip() {
	// // EntityEditorInput sei = (EntityEditorInput) getEditorInput();
	// // String displayName = ConnectJcrUtils.get(node, Property.JCR_TITLE);
	// // if (isEmpty(displayName))
	// // displayName = "current objet";
	// // sei.setTooltipText("Display and edit information for " + displayName);
	// }

	protected abstract void addPages();

	@Override
	protected void init() {
		super.init();
		addPages();
	}

	protected void addPage(AbstractEditorPage page) {
		pages.add(page);
	}

	protected void commitPages(boolean b) {
		// TODO implement
	}

	protected AbstractEditorPage getActivePageInstance() {
		return null;
	}

	@Override
	protected void populateTabFolder(CTabFolder tabFolder) {
		for (AbstractEditorPage page : pages) {
			// Composite body = addTabToFolder(tabFolder, SWT.NONE, page.getLabel(),
			// page.getPageId(), "TOOLTIP");
			// body.setLayout(new GridLayout());
			// page.createUi(body);

			ScrolledComposite form = addScrolledTabToFolder(tabFolder, SWT.NONE, page.getLabel(), page.getPageId(),
					"TOOLTIP");
			IManagedForm managedForm = new ManagedForm(getManagedForm().getToolkit(), form);
			// managedForm.getForm().setLayout(new GridLayout());

			// Composite body = getFormToolkit().createComposite(managedForm.getForm());
			// managedForm.getForm().setContent(body);
			// body.setLayout(new GridLayout());
			// for (int i = 0; i < 3; i++)
			// new Label(body, SWT.BORDER).setText("TEST");

			// IManagedForm managedForm = getManagedForm();
			// IManagedForm managedForm = new ManagedForm(body);
			page.createUi(managedForm);
		}

	}

	protected ScrolledComposite addScrolledTabToFolder(CTabFolder tabFolder, int style, String label, String id,
			String tooltip) {
		CTabItem item = new CTabItem(tabFolder, style);
		item.setData(CTAB_INSTANCE_ID, id);
		item.setText(label);
		item.setToolTipText(tooltip);
		ScrolledComposite innerPannel = getFormToolkit().createScrolledForm(tabFolder);
		// must set control
		item.setControl(innerPannel);
		return innerPannel;
	}

	@Override
	protected void populateHeader(Composite parent) {
		// TODO Auto-generated method stub

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
				builder.append(getUserAdminService().getUserDisplayName(lstModByDn));
				builder.append(". ");
			}
			return builder.toString();
		} catch (RepositoryException re) {
			throw new ConnectException("Unable to create last " + "modified message for " + currNode, re);
		}
	}

	// Exposes
	// protected Node getNode() {
	// return node;
	// }

	// protected Session getSession() {
	// return ConnectJcrUtils.getSession(node);
	// }
	//
	// protected Repository getRepository() {
	// return repository;
	// }
	//
	// protected UserAdminService getUserAdminService() {
	// return userAdminService;
	// }
	//
	// protected ResourcesService getResourcesService() {
	// return resourcesService;
	// }

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
				JcrUtils.updateLastModified(getNode());
				session.save();
				changed = true;
			}
			if (changed && ConnectJcrUtils.isVersionable(getNode())) {
				VersionManager vm = session.getWorkspace().getVersionManager();
				String path = getNode().getPath();
				vm.checkpoint(path);
			}
		} catch (RepositoryException re) {
			throw new ConnectException("Unable to perform check point on " + getNode(), re);
		}

		// firePropertyChange(PROP_DIRTY);
	}

	// @Override
	// public void forceRefresh(Object object) {
	// // TODO implement a better refresh mechanism
	// // IManagedForm mf = getActivePageInstance().getManagedForm();
	// IManagedForm mf = getManagedForm();
	// for (IFormPart part : mf.getParts())
	// if (part instanceof AbstractFormPart)
	// ((AbstractFormPart) part).markStale();
	//
	// mf.refresh();
	// }

	// @PreDestroy
	// public void dispose() {
	// JcrUtils.logoutQuietly(session);
	// }
	//
	// // CmsEditable LIFE CYCLE
	// @Override
	// public Boolean canEdit() {
	// // TODO refine this
	// String roleStr = TrackerRole.editor.dn();
	// return CurrentUser.isInRole(roleStr);
	// }

	// @Override
	// public Boolean isEditing() {
	// return true;
	// }
	//
	// @Override
	// public void startEditing() {
	// }
	//
	// @Override
	// public void stopEditing() {
	// }

	/* DEPENDENCY INJECTION */
	// public void setRepository(Repository repository) {
	// this.repository = repository;
	// }
	//
	// public void setUserAdminService(UserAdminService userAdminService) {
	// this.userAdminService = userAdminService;
	// }

	// public void setResourcesService(ResourcesService resourcesService) {
	// this.resourcesService = resourcesService;
	// }

	// public void setActivitiesService(ActivitiesService activitiesService) {
	// this.activitiesService = activitiesService;
	// }
	//
	// public void setDocumentsService(DocumentsService documentsService) {
	// this.documentsService = documentsService;
	// }
	//
	// public void setTrackerService(TrackerService trackerService) {
	// this.trackerService = trackerService;
	// }

	// public void setAppService(AppService appService) {
	// this.appService = appService;
	// }
	//
	// public void setAppWorkbenchService(AppWorkbenchService appWorkbenchService) {
	// this.appWorkbenchService = appWorkbenchService;
	// }
	/** @deprecated Use {@link #createFormBoldLabel(Composite, String)} */
	@Deprecated
	protected Label createFormBoldLabel(FormToolkit toolkit, Composite parent, String value) {
		return createFormBoldLabel(parent, value);
	}

	protected Label createFormBoldLabel(Composite parent, String value) {
		Label label = new Label(parent, SWT.END);
		label.setText(" " + value);
		label.setFont(EclipseUiUtils.getBoldFont(parent));
		GridData twd = new GridData(SWT.END, SWT.FILL, false, false);
		label.setLayoutData(twd);
		return label;
	}

}
