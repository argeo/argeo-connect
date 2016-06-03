package org.argeo.photo.manager.ui.editors;

import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

public class PictureEditorInput implements IEditorInput {
	private String relativePath;

	public PictureEditorInput(String relativePath) {
		this.relativePath = relativePath;
	}

	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		return null;
	}

	public boolean exists() {
		return false;
	}

	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	public String getName() {
		return JcrUtils.lastPathElement(relativePath);
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		return relativePath;
	}

	public String getRelativePath() {
		return relativePath;
	}

}
