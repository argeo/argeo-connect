package org.argeo.connect.ui.crisis.editors;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.eclipse.ui.dialogs.Error;
import org.argeo.eclipse.ui.jcr.editors.NodeEditorInput;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;

public class SituationEditor extends FormEditor {
	private Session session;

	private Node node;

	@Override
	protected void addPages() {
		NodeEditorInput nei = (NodeEditorInput) getEditorInput();
		String path = nei.getPath();
		try {
			if (!session.itemExists(path)) {
				// new node
				String parentPath = JcrUtils.parentPath(path);
				JcrUtils.mkdirs(session, parentPath);

			}
		} catch (RepositoryException e) {
			Error.show("Cannot add pages for " + path, e);
		}

		// addPage(new SituationOverviewPage(featureNode,
		// situationDefinitionNode, id, title));

	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub

	}

	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return false;
	}

}
