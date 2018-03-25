package org.argeo.activities.e4.parts;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.Property;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.activities.ActivitiesService;
import org.argeo.activities.ui.ActivityChildrenList;
import org.argeo.activities.ui.RelatedActivityList;
import org.argeo.activities.ui.TaskBasicHeader;
import org.argeo.connect.e4.parts.AbstractConnectCTabEditor;
import org.argeo.connect.ui.util.LazyCTabControl;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Composite;

/** Default connect task editor */
public class TaskEditor extends AbstractConnectCTabEditor {
	final static Log log = LogFactory.getLog(TaskEditor.class);

//	public final static String ID = ActivitiesUiPlugin.PLUGIN_ID + ".taskEditor";

	@Inject
	private ActivitiesService activitiesService;
	private Node task;

	public void init()  {
		super.init();
		task = getNode();
	}

	protected String getCurrentTaskType() {
		return activitiesService.getMainNodeType(task);
	}

	protected void populateHeader(Composite parent) {
		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());
		Composite headerCmp = new TaskBasicHeader(this, parent, SWT.NO_FOCUS, getUserAdminService(),
				getResourcesService(), getActivitiesService(), getSystemWorkbenchService(), getCurrentTaskType(), task);
		headerCmp.setLayoutData(EclipseUiUtils.fillWidth());
	}

	@Override
	protected void populateTabFolder(CTabFolder folder) {
		// Activities and tasks
		String tooltip = "Activities and tasks related to " + JcrUtils.get(task, Property.JCR_TITLE);
		LazyCTabControl activitiesCmp = new ActivityChildrenList(folder, SWT.NO_FOCUS, this, getUserAdminService(),
				getResourcesService(), getActivitiesService(), getSystemAppService(), getSystemWorkbenchService(),
				task);
		activitiesCmp.setLayoutData(EclipseUiUtils.fillAll());
		addLazyTabToFolder(folder, activitiesCmp, "Activity log", RelatedActivityList.CTAB_ID, tooltip);
	}

	protected ActivitiesService getActivitiesService() {
		return activitiesService;
	}

	/* DEPENDENCY INNJECTION */
	public void setActivitiesService(ActivitiesService activitiesService) {
		this.activitiesService = activitiesService;
	}
}
