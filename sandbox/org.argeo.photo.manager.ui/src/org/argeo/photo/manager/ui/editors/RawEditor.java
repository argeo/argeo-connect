package org.argeo.photo.manager.ui.editors;

import org.argeo.ArgeoException;
import org.argeo.photo.manager.PictureManager;
import org.argeo.photo.manager.ui.PhotoManagerUiPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;

/**
 * Main multitab editor to handle a session to clean GPS data.
 * 
 */
public class RawEditor extends FormEditor {
	private PictureManager pictureManager;

	public static final String ID = PhotoManagerUiPlugin.PLUGIN_ID
			+ ".rawEditor";

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
	}

	protected void addPages() {
		try {
			addPage(new PictureDisplayPage(this, pictureManager));
			// addPage(new SwingRawDisplayPage(this, "rawDisplayPage", "View"));
		} catch (PartInitException e) {
			throw new ArgeoException("Not able to add page ", e);
		}
	}

	public void doSave(IProgressMonitor monitor) {
	}

	public void setFocus() {
	}

	@Override
	public void doSaveAs() {
		// not implemented, save as is not allowed
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	public void setPictureManager(PictureManager pictureManager) {
		this.pictureManager = pictureManager;
	}

}
