package org.argeo.people.workbench.rap.util;

import org.argeo.cms.ui.workbench.util.CommandUtils;
import org.argeo.connect.workbench.SystemWorkbenchService;
import org.argeo.connect.workbench.commands.OpenEntityEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class OpenEditorAdapter extends SelectionAdapter {
	private static final long serialVersionUID = 1638606646060389067L;

	private final String jcrId;
	private final SystemWorkbenchService systemWorkbenchServcice;

	/**
	 * @param jcrId
	 */
	public OpenEditorAdapter(SystemWorkbenchService systemWorkbenchServcice, String jcrId) {
		this.jcrId = jcrId;
		this.systemWorkbenchServcice = systemWorkbenchServcice;
	}

	@Override
	public void widgetSelected(final SelectionEvent event) {
		CommandUtils.callCommand(systemWorkbenchServcice.getOpenEntityEditorCmdId(), OpenEntityEditor.PARAM_JCR_ID,
				jcrId);
	}
}
