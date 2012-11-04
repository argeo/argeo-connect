package org.argeo.connect.demo.gr.ui.editors;

import org.argeo.connect.demo.gr.GrBackend;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;


/**
 * Parent Abstract GR multitab editor. Insure the presence of a GrBackend
 */
public abstract class AbstractGrEditor extends FormEditor {
	/** DEPENDENCY INJECTION **/
	private GrBackend grBackend;

	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
	}

	@Override
	public void doSaveAs() {
		// unused compulsory method
	}

	/** expose business objects to same package classes */
	GrBackend getGrBackend() {
		return grBackend;
	}

	/** DEPENDENCY INJECTION **/
	public void setGrBackend(GrBackend grBackend) {
		this.grBackend = grBackend;
	}
}
