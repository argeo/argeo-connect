package org.argeo.tracker.ui.controls;

import java.util.List;

import javax.jcr.Node;

import org.argeo.connect.ui.widgets.ConnectAbstractDropDown;
import org.argeo.tracker.core.TrackerUtils;
import org.eclipse.swt.widgets.Text;

/** Simple DropDown that displays the list of existing versions */
public class VersionDropDown extends ConnectAbstractDropDown {

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
