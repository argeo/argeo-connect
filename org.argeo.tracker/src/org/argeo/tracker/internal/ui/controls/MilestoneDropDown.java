package org.argeo.tracker.internal.ui.controls;

import java.util.List;

import javax.jcr.Node;

import org.argeo.connect.ui.widgets.ConnectAbstractDropDown;
import org.argeo.tracker.core.TrackerUtils;
import org.eclipse.swt.widgets.Text;

/** Simple DropDown that displays the list of future milestones */
public class MilestoneDropDown extends ConnectAbstractDropDown {

	private final Node project;

	public MilestoneDropDown(Node project, Text text) {
		super(text);
		this.project = project;
		init();
	}

	@Override
	protected List<String> getFilteredValues(String filter) {
		return TrackerUtils.getMilestoneIds(project, filter);
	}
}