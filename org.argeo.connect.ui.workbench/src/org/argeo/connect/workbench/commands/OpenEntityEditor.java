package org.argeo.connect.workbench.commands;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.cms.ui.CmsEditable;
import org.argeo.connect.ConnectException;
import org.argeo.connect.ui.ConnectEditor;
import org.argeo.connect.ui.SystemWorkbenchService;
import org.argeo.connect.workbench.ConnectUiPlugin;
import org.argeo.connect.workbench.util.EntityEditorInput;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Open the corresponding editor given a node. Centralize here mapping between a
 * node type and an editor Corresponding node can be retrieved either using the
 * JCR ID or a business defined UID
 * 
 * If the parameter param.cTabId is set, if the opened editor is of type
 * {@link AbstractPeopleCTabEditor}, and if a tab with such an id exists, it is
 * opened, otherwise it fails silently and open the default state of the
 * corresponding editor
 */
public class OpenEntityEditor extends AbstractHandler {
	private final static Log log = LogFactory.getLog(OpenEntityEditor.class);

	public final static String ID = ConnectUiPlugin.PLUGIN_ID + ".openEntityEditor";

	/* DEPENDENCY INJECTION */
	private Repository repository;
	private SystemWorkbenchService systemWorkbenchService;

	public Object execute(ExecutionEvent event) throws ExecutionException {
		EntityEditorInput eei = null;
		Node entity = null;
		Session session = null;
		String jcrId = event.getParameter(ConnectEditor.PARAM_JCR_ID);
		try {
			session = repository.login();
			if (jcrId != null) {
				entity = session.getNodeByIdentifier(jcrId);
				eei = new EntityEditorInput(jcrId);
			} else {
				if (log.isTraceEnabled())
					log.warn("Cannot open an editor with no JCR ID");
				return null;
			}

			String editorId = systemWorkbenchService.getEntityEditorId(entity);
			if (editorId != null) {
				// IWorkbenchWindow iww = HandlerUtil.getActiveWorkbenchWindow(event);
				// IWorkbenchPage iwp = iww.getActivePage();

				IWorkbench wb = PlatformUI.getWorkbench();
				IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
				IWorkbenchPage iwp = win.getActivePage();

				IEditorPart editor = iwp.openEditor(eei, editorId);

				String openForEdit = event.getParameter(ConnectEditor.PARAM_OPEN_FOR_EDIT);
				if ("true".equals(openForEdit) && editor instanceof CmsEditable)
					((CmsEditable) editor).startEditing();

				// String tabId = event.getParameter(PARAM_CTAB_ID);
				// if (EclipseUiUtils.notEmpty(tabId) && editor instanceof
				// AbstractPeopleCTabEditor)
				// ((AbstractPeopleCTabEditor) editor).openTabItem(tabId);
			}
		} catch (PartInitException | RepositoryException pie) {
			throw new ConnectException("Connect open editor for " + jcrId, pie);
		} finally {
			JcrUtils.logoutQuietly(session);
		}
		return null;
	}

	/* DEPENDENCY INJECTION */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setSystemWorkbenchService(SystemWorkbenchService systemWorkbenchService) {
		this.systemWorkbenchService = systemWorkbenchService;
	}
}