package org.argeo.activities.workbench.parts;

import javax.jcr.Node;
import javax.jcr.Property;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.activities.ActivitiesService;
import org.argeo.activities.workbench.ActivitiesUiPlugin;
import org.argeo.connect.ui.util.LazyCTabControl;
import org.argeo.connect.workbench.parts.AbstractConnectCTabEditor;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

/** Default connect task editor */
public class TaskEditor extends AbstractConnectCTabEditor {
	final static Log log = LogFactory.getLog(TaskEditor.class);

	public final static String ID = ActivitiesUiPlugin.PLUGIN_ID + ".taskEditor";

	// Context
	private ActivitiesService activitiesService;
	private Node task;

	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		task = getNode();
	}

	protected String getCurrentTaskType() {
		return activitiesService.getMainNodeType(task);
	}

	protected void populateHeader(Composite parent) {
		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());
		Composite headerCmp = new TaskBasicHeader(this, parent, SWT.NO_FOCUS, getUserAdminService(),
				getResourcesService(), getActivitiesService(), getAppWorkbenchService(), getCurrentTaskType(), task);
		headerCmp.setLayoutData(EclipseUiUtils.fillWidth());
	}

	@Override
	protected void populateTabFolder(CTabFolder folder) {
		// Activities and tasks
		String tooltip = "Activities and tasks related to " + JcrUtils.get(task, Property.JCR_TITLE);
		LazyCTabControl activitiesCmp = new ActivityList(folder, SWT.NO_FOCUS, this, getUserAdminService(),
				getResourcesService(), getActivitiesService(), getAppWorkbenchService(), task);
		activitiesCmp.setLayoutData(EclipseUiUtils.fillAll());
		addLazyTabToFolder(folder, activitiesCmp, "Activity log", ActivityList.CTAB_ID, tooltip);
	}

	protected ActivitiesService getActivitiesService() {
		return activitiesService;
	}

	/* DEPENDENCY INNJECTION */
	public void setActivitiesService(ActivitiesService activitiesService) {
		this.activitiesService = activitiesService;
	}
}
