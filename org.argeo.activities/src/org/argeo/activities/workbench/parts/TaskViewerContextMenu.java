package org.argeo.activities.workbench.parts;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.activities.ActivitiesException;
import org.argeo.activities.ActivitiesService;
import org.argeo.activities.ActivitiesTypes;
import org.argeo.connect.ui.widgets.AbstractConnectContextMenu;
import org.argeo.connect.util.ConnectJcrUtils;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;

public class TaskViewerContextMenu extends AbstractConnectContextMenu {
	private static final long serialVersionUID = 1028389681695028210L;

	// Default known actions
	private final static String ACTION_ID_MARK_AS_DONE = "markAsDone";
	private final static String ACTION_ID_CANCEL = "cancel";

	private final static String[] DEFAULT_ACTIONS = { ACTION_ID_MARK_AS_DONE, ACTION_ID_CANCEL };

	private final Viewer viewer;
	private final ActivitiesService activityService;
	private final Session session;

	public TaskViewerContextMenu(Viewer viewer, Session session, ActivitiesService activityService) {
		super(viewer.getControl().getDisplay(), DEFAULT_ACTIONS);
		this.viewer = viewer;
		this.session = session;
		this.activityService = activityService;
		createControl();
	}

	public boolean performAction(String actionId) {
		boolean hasChanged = false;
		switch (actionId) {
		case ACTION_ID_MARK_AS_DONE:
			hasChanged = markAsDone();
			break;
		case ACTION_ID_CANCEL:
			hasChanged = cancel();
			break;
		default:
			throw new IllegalArgumentException("Unimplemented action " + actionId);
		}

		return hasChanged;
		// if (hasChanged) {
		// refreshFilteredList();
		// tableViewer.getTable().setFocus();
		// }
	}

	protected String getLabel(String actionId) {
		switch (actionId) {
		case ACTION_ID_MARK_AS_DONE:
			return "Mark as done";
		case ACTION_ID_CANCEL:
			return "Cancel";
		default:
			throw new IllegalArgumentException("Unimplemented action " + actionId);
		}
	}

	@Override
	protected boolean aboutToShow(Control source, Point location, IStructuredSelection selection) {
		boolean emptySel = selection == null || selection.isEmpty();
		if (emptySel)
			return false;
		else {
			setVisible(true, ACTION_ID_MARK_AS_DONE, ACTION_ID_CANCEL);
			return true;
		}
	}

	private boolean markAsDone() {
		IStructuredSelection selection = ((IStructuredSelection) viewer.getSelection());
		@SuppressWarnings("unchecked")
		Iterator<Node> it = (Iterator<Node>) selection.iterator();
		List<String> modifiedPaths = new ArrayList<>();
		boolean hasChanged = false;
		try {
			while (it.hasNext()) {
				Node currNode = it.next();
				hasChanged |= activityService.updateStatus(ActivitiesTypes.ACTIVITIES_TASK, currNode, "Done",
						modifiedPaths);
			}
			if (hasChanged) {
				session.save();
				ConnectJcrUtils.checkPoint(session, modifiedPaths, true);
			}
			return hasChanged;
		} catch (RepositoryException e1) {
			throw new ActivitiesException("Cannot mark tasks as done", e1);
		}
	}

	private boolean cancel() {
		IStructuredSelection selection = ((IStructuredSelection) viewer.getSelection());
		@SuppressWarnings("unchecked")
		Iterator<Node> it = (Iterator<Node>) selection.iterator();
		List<String> modifiedPaths = new ArrayList<>();
		boolean hasChanged = false;
		try {
			while (it.hasNext()) {
				Node currNode = it.next();
				hasChanged |= activityService.updateStatus(ActivitiesTypes.ACTIVITIES_TASK, currNode, "Canceled",
						modifiedPaths);
			}
			if (hasChanged) {
				session.save();
				ConnectJcrUtils.checkPoint(session, modifiedPaths, true);
			}
			return hasChanged;
		} catch (RepositoryException e1) {
			throw new ActivitiesException("Cannot mark tasks as done", e1);
		}
	}
}
