/*
 * Argeo Connect - Data management and communications
 * Copyright (C) 2012 Argeo GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or combining it
 * with software covered by the terms of the Eclipse Public License, the
 * licensors of this Program grant you additional permission to convey the
 * resulting work. Corresponding Source for a non-source form of such a
 * combination shall include the source code for the parts of such software
 * which are used as well as that of the covered work.
 */
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