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
package org.argeo.gis.ui.editors;

import javax.jcr.Node;

import org.argeo.gis.ui.MapControlCreator;
import org.argeo.gis.ui.MapViewer;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

/** A generic map editor */
public class DefaultMapEditor extends EditorPart {
	private Node context;
	private MapViewer mapViewer;
	private MapControlCreator mapControlCreator;

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		if (input instanceof MapEditorInput) {
			// mapContext = ((MapEditorInput) input).getMapContext();
			context = ((MapEditorInput) input).getContext();
			setSite(site);
			setInput(input);
			setPartName(input.getName());
		} else {
			throw new PartInitException("Support only " + MapEditorInput.class
					+ " inputs");
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		Composite mapArea = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		mapArea.setLayout(layout);
		mapViewer = mapControlCreator.createMapControl(context, mapArea);
		mapViewer.getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true));
	}

	public MapViewer getMapViewer() {
		return mapViewer;
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void setFocus() {
		// LayersView layersView = (LayersView) getEditorSite()
		// .getWorkbenchWindow().getActivePage().findView(LayersView.ID);
		// layersView.setMapContext(getMapContext());
		mapViewer.getControl().setFocus();
	}

	public void featureSelected(String layerId, String featureId) {
		// TODO Auto-generated method stub

	}

	public void featureUnselected(String layerId, String featureId) {
		// TODO Auto-generated method stub

	}

	public void setMapControlCreator(MapControlCreator mapControlCreator) {
		this.mapControlCreator = mapControlCreator;
	}

}
