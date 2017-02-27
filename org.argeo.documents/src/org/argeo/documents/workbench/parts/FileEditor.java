package org.argeo.documents.workbench.parts;

import org.argeo.documents.composites.DocumentsFileComposite;
import org.argeo.documents.workbench.DocumentsUiPlugin;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

/** Default editor to display and edit an issue */
public class FileEditor extends AbstractDocumentsEditor {
	public static final String ID = DocumentsUiPlugin.PLUGIN_ID + ".fileEditor";

	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());
		DocumentsFileComposite dfc = new DocumentsFileComposite(parent, SWT.NO_FOCUS, getNode(), getDocumentsService(),
				getNodeFileSystemProvider());
		dfc.setLayoutData(EclipseUiUtils.fillAll());
		parent.layout(true, true);
	}
}
