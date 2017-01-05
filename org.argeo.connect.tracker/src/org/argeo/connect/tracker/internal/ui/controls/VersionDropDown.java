package org.argeo.connect.tracker.internal.ui.controls;

import java.util.List;

import javax.jcr.Node;

import org.argeo.connect.people.rap.composites.dropdowns.PeopleAbstractDropDown;
import org.argeo.connect.tracker.core.TrackerUtils;
import org.eclipse.swt.widgets.Text;

/** Simple DropDown that displays the list of existing versions */
public class VersionDropDown extends PeopleAbstractDropDown {

	private final Node project;

	public VersionDropDown(Node project, Text text) {
		super(text);
		this.project = project;
		init();
	}

	@Override
	protected List<String> getFilteredValues(String filter) {
		return TrackerUtils.getVersionIds(project, filter);
	}
}