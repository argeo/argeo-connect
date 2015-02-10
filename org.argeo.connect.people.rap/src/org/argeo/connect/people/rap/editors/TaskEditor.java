package org.argeo.connect.people.rap.editors;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.rap.PeopleRapConstants;
import org.argeo.connect.people.rap.PeopleRapPlugin;
import org.argeo.connect.people.rap.editors.parts.TaskBasicHeader;
import org.argeo.connect.people.rap.editors.tabs.ActivityList;
import org.argeo.connect.people.rap.editors.utils.AbstractEntityCTabEditor;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

/**
 * Default connect task editor
 */
public class TaskEditor extends AbstractEntityCTabEditor {
	final static Log log = LogFactory.getLog(TaskEditor.class);

	public final static String ID = PeopleRapPlugin.PLUGIN_ID + ".taskEditor";

	// context
	private Node task;

	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		task = getNode();
	}

	protected String getCurrentTaskType() {
		try {
			// we rely on the task primary type.
			return task.getPrimaryNodeType().getName();
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to determine task type for "
					+ task, re);
		}
	}

	protected void populateHeader(Composite parent) {
		parent.setLayout(PeopleUiUtils.noSpaceGridLayout());
		Composite headerCmp = new TaskBasicHeader(getFormToolkit(),
				getManagedForm(), parent, SWT.NO_FOCUS, getPeopleService(),
				getPeopleWorkbenchService(), getCurrentTaskType(), task);
		headerCmp.setLayoutData(PeopleUiUtils.horizontalFillData());
	}

	@Override
	protected void populateTabFolder(CTabFolder tabFolder) {
		// Activities and tasks
		String tooltip = "Activities and tasks related to "
				+ JcrUtils.get(task, Property.JCR_TITLE);
		Composite innerPannel = addTabToFolder(tabFolder, CTAB_COMP_STYLE,
				"Activity log", PeopleRapConstants.CTAB_ACTIVITY_LOG, tooltip);
		innerPannel.setLayout(PeopleUiUtils.noSpaceGridLayout());
		Composite activitiesCmp = new ActivityList(toolkit, getManagedForm(),
				innerPannel, SWT.NONE, getPeopleService(),
				getPeopleWorkbenchService(), task);
		activitiesCmp.setLayoutData(PeopleUiUtils.fillGridData());
	}
}