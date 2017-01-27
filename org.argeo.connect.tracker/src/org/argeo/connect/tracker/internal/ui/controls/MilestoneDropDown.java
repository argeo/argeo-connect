package org.argeo.connect.tracker.internal.ui.controls;

import java.util.List;

import javax.jcr.Node;

import org.argeo.connect.people.workbench.rap.composites.dropdowns.PeopleAbstractDropDown;
import org.argeo.connect.tracker.core.TrackerUtils;
import org.eclipse.swt.widgets.Text;

/** Simple DropDown that displays the list of future milestones */
public class MilestoneDropDown extends PeopleAbstractDropDown {

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