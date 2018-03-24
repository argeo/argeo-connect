package org.argeo.documents.workbench.parts;

import java.nio.file.Path;

import javax.jcr.Node;
import javax.jcr.Session;

import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.documents.composites.DocumentsFolderComposite;
import org.argeo.documents.workbench.DocumentsUiPlugin;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

/** Default editor to display and edit a document folder */
public class FolderEditor extends AbstractDocumentsEditor {
	public static final String ID = DocumentsUiPlugin.PLUGIN_ID + ".folderEditor";

	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());
		DocumentsFolderComposite dfc = new DocumentsFolderComposite(parent, SWT.NO_FOCUS, getNode(),
				getDocumentsService()) {
			private static final long serialVersionUID = 4880354934421639754L;

			@Override
			protected void externalNavigateTo(Path path) {
				// TODO rather directly use the jcrPath / an URI?
				Session session = ConnectJcrUtils.getSession(getNode());
				Node currNode = ConnectJcrUtils.getNode(session, path.toString());
				// String nodeId = ConnectJcrUtils.getIdentifier(currNode);
				// CommandUtils.callCommand(getSystemWorkbenchService().getOpenEntityEditorCmdId(),
				// ConnectEditor.PARAM_JCR_ID, nodeId);
				getSystemWorkbenchService().openEntityEditor(currNode);
			}
		};
		dfc.setLayoutData(EclipseUiUtils.fillAll());
		String jcrPath = ConnectJcrUtils.getPath(getNode());
		Path path = getDocumentsService().getPath(getNodeFileSystemProvider(), jcrPath);
		dfc.populate(path);
		parent.layout(true, true);
	}
}
