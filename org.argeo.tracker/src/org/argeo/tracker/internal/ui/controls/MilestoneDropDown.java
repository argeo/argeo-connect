package org.argeo.tracker.internal.ui.controls;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;

import org.argeo.connect.ui.widgets.ConnectAbstractDropDown;
import org.argeo.tracker.core.TrackerUtils;
import org.eclipse.swt.widgets.Text;

/** Simple DropDown that displays the list of milestones */
public class MilestoneDropDown extends ConnectAbstractDropDown {

	private Node project;

	public MilestoneDropDown(Node project, Text text) {
		super(text);
		this.project = project;
		init();
	}

	public void setProject(Node project) {
		this.project = project;
	}

	@Override
	protected List<String> getFilteredValues(String filter) {
		if (project == null)
			return new ArrayList<>();
		else
			return TrackerUtils.getMilestoneIds(project, filter);
	}
}
