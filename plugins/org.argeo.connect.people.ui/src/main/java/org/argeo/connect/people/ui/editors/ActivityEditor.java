package org.argeo.connect.people.ui.editors;

import javax.jcr.Node;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

/**
 * Default connect activity editor
 */
public class ActivityEditor extends AbstractPeopleEditor {
	final static Log log = LogFactory.getLog(ActivityEditor.class);

	// local constants
	public final static String ID = PeopleUiPlugin.PLUGIN_ID
			+ ".activityEditor";

	// Main business Objects
	private Node activity;

	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		activity = getNode();
	}

	@Override
	protected void createToolkits() {
	}

	@Override
	protected Boolean deleteParentOnRemove() {
		return new Boolean(false);
	}

	@Override
	protected void createBodyPart(Composite parent) {
		parent.setLayout(new GridLayout());
		toolkit.createLabel(parent, "Mail activity created by ... ", SWT.NONE);
	}
}