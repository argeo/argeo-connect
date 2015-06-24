package org.argeo.connect.people.rap.listeners;

import org.argeo.connect.people.rap.PeopleWorkbenchService;
import org.argeo.connect.people.rap.commands.OpenEntityEditor;
import org.argeo.eclipse.ui.workbench.CommandUtils;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class OpenEditorAdapter extends SelectionAdapter {
	private static final long serialVersionUID = 1638606646060389067L;

	private final String jcrId;
	private final PeopleWorkbenchService peopleWorkbenchServcice;

	/**
	 * @param jcrId
	 */
	public OpenEditorAdapter(PeopleWorkbenchService peopleWorkbenchServcice,
			String jcrId) {
		this.jcrId = jcrId;
		this.peopleWorkbenchServcice = peopleWorkbenchServcice;
	}

	@Override
	public void widgetSelected(final SelectionEvent event) {
		CommandUtils.callCommand(
				peopleWorkbenchServcice.getOpenEntityEditorCmdId(),
				OpenEntityEditor.PARAM_JCR_ID, jcrId);
	}
}