package org.argeo.people.workbench.rap.util;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.connect.ConnectException;
import org.argeo.connect.ui.SystemWorkbenchService;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

@Deprecated
class OpenEditorAdapter extends SelectionAdapter {
	private static final long serialVersionUID = 1638606646060389067L;

	private final String jcrId;
	private final Session session;
	private final SystemWorkbenchService systemWorkbenchService;

	/**
	 * @param jcrId
	 */
	public OpenEditorAdapter(SystemWorkbenchService systemWorkbenchServcice, Session session, String jcrId) {
		this.session = session;
		this.jcrId = jcrId;
		this.systemWorkbenchService = systemWorkbenchServcice;
	}

	@Override
	public void widgetSelected(final SelectionEvent event) {
		try {
			Node node = session.getNodeByIdentifier(jcrId);
			systemWorkbenchService.openEntityEditor(node);
		} catch (RepositoryException e) {
			throw new ConnectException("Cannot open editor for entity with id " + jcrId, e);
		}
		// CommandUtils.callCommand(systemWorkbenchService.getOpenEntityEditorCmdId(),
		// ConnectEditor.PARAM_JCR_ID,
		// jcrId);
	}
}
