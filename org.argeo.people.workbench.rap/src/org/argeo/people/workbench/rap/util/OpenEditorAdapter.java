package org.argeo.people.workbench.rap.util;

import org.argeo.cms.ui.workbench.util.CommandUtils;
import org.argeo.connect.workbench.commands.OpenEntityEditor;
import org.argeo.people.workbench.PeopleWorkbenchService;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class OpenEditorAdapter extends SelectionAdapter {
	private static final long serialVersionUID = 1638606646060389067L;

	private final String jcrId;
	private final PeopleWorkbenchService peopleWorkbenchServcice;

	/**
	 * @param jcrId
	 */
	public OpenEditorAdapter(PeopleWorkbenchService peopleWorkbenchServcice, String jcrId) {
		this.jcrId = jcrId;
		this.peopleWorkbenchServcice = peopleWorkbenchServcice;
	}

	@Override
	public void widgetSelected(final SelectionEvent event) {
		CommandUtils.callCommand(peopleWorkbenchServcice.getOpenEntityEditorCmdId(), OpenEntityEditor.PARAM_JCR_ID,
				jcrId);
	}
}
