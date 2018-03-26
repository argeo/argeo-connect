package org.argeo.documents.e4.parts;

import javax.annotation.PostConstruct;

import org.argeo.documents.composites.DocumentsFileComposite;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/** Default editor to display and edit a file */
public class FileEditor extends AbstractDocumentsEditor {
	// public static final String ID = DocumentsUiPlugin.PLUGIN_ID + ".fileEditor";

	@PostConstruct
	public void createPartControl(Composite parent) {
		init();

		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());
		DocumentsFileComposite dfc = new DocumentsFileComposite(parent, SWT.NO_FOCUS, getNode(), getDocumentsService(),
				getNodeFileSystemProvider());
		dfc.setLayoutData(EclipseUiUtils.fillAll());
		parent.layout(true, true);
	}
}
