package org.argeo.connect.e4.handlers;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.connect.ConnectException;
import org.argeo.connect.ui.ConnectEditor;
import org.argeo.connect.ui.SystemWorkbenchService;
import org.argeo.jcr.JcrUtils;
import org.eclipse.e4.core.di.annotations.Execute;

/**
 * Creates a new entity under draft path of the current repository and opens the
 * corresponding editor. The Node type of the relevant entity must be passed as
 * parameter.
 */
public class OpenEntity {
	public final static String PARAM_TARGET_NODE_TYPE = "targetNodeType";

	@Inject
	private Repository repository;
	@Inject
	private SystemWorkbenchService systemWorkbenchService;

	@Execute
	public void execute(@Named(ConnectEditor.PARAM_JCR_ID) String jcrId,
			@Named(ConnectEditor.PARAM_OPEN_FOR_EDIT) String openForEdit) {
		Session mainSession = null;

		try {
			mainSession = repository.login();
			Node entity = mainSession.getNodeByIdentifier(jcrId);
			systemWorkbenchService.openEntityEditor(entity);
		} catch (RepositoryException e) {
			throw new ConnectException("Cannot get entity #" + jcrId, e);
		} finally {
			JcrUtils.logoutQuietly(mainSession);
		}

	}

}
