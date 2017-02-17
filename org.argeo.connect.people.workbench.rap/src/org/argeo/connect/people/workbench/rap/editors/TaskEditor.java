package org.argeo.connect.people.workbench.rap.editors;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.workbench.rap.PeopleRapConstants;
import org.argeo.connect.people.workbench.rap.PeopleRapPlugin;
import org.argeo.connect.people.workbench.rap.editors.parts.TaskBasicHeader;
import org.argeo.connect.people.workbench.rap.editors.tabs.ActivityList;
import org.argeo.connect.people.workbench.rap.editors.util.AbstractPeopleCTabEditor;
import org.argeo.connect.people.workbench.rap.editors.util.LazyCTabControl;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

/** Default connect task editor */
public class TaskEditor extends AbstractPeopleCTabEditor {
	final static Log log = LogFactory.getLog(TaskEditor.class);

	public final static String ID = PeopleRapPlugin.PLUGIN_ID + ".taskEditor";

	// Context
	private Node task;

	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		task = getNode();
	}

	protected String getCurrentTaskType() {
		try {
			// we rely on the task primary type.
			return task.getPrimaryNodeType().getName();
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to determine task type for " + task, re);
		}
	}

	protected void populateHeader(Composite parent) {
		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());
		Composite headerCmp = new TaskBasicHeader(this, parent, SWT.NO_FOCUS, getUserAdminService(),
				getResourceService(), getActivityService(), getAppWorkbenchService(), getCurrentTaskType(), task);
		headerCmp.setLayoutData(EclipseUiUtils.fillWidth());
	}

	@Override
	protected void populateTabFolder(CTabFolder folder) {
		// Activities and tasks
		String tooltip = "Activities and tasks related to " + JcrUtils.get(task, Property.JCR_TITLE);
		LazyCTabControl activitiesCmp = new ActivityList(folder, SWT.NO_FOCUS, this, getUserAdminService(),
				getResourceService(), getActivityService(), getAppWorkbenchService(), task);
		activitiesCmp.setLayoutData(EclipseUiUtils.fillAll());
		addLazyTabToFolder(folder, activitiesCmp, "Activity log", PeopleRapConstants.CTAB_ACTIVITY_LOG, tooltip);
	}
}