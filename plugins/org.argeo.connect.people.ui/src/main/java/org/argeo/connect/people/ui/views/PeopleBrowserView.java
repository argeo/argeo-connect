package org.argeo.connect.people.ui.views;

import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class PeopleBrowserView extends ViewPart {
	public final static String ID = PeopleUiPlugin.PLUGIN_ID
			+ ".peopleBrowserView";

	private TreeViewer treeViewer;

	@Override
	public void createPartControl(Composite parent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setFocus() {
		// treeViewer.getTree().setFocus();
	}

}
