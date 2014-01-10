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
public class TaskEditor extends AbstractPeopleEditor {
	final static Log log = LogFactory.getLog(TaskEditor.class);

	// local constants
	public final static String ID = PeopleUiPlugin.PLUGIN_ID
			+ ".taskEditor";

	// Main business Objects
	private Node task;

	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		task = getNode();
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
		toolkit.createLabel(parent, "Implement the task editor", SWT.NONE);
	}
}