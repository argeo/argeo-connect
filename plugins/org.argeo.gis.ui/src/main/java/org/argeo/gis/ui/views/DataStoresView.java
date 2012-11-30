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
package org.argeo.gis.ui.views;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.argeo.eclipse.ui.AbstractTreeContentProvider;
import org.argeo.gis.ui.data.DataStoreNode;
import org.argeo.gis.ui.data.FeatureNode;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.geotools.data.DataStore;

public class DataStoresView extends ViewPart implements IDoubleClickListener {
	private TreeViewer viewer;

	private List<DataStore> dataStores;

	@Override
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new DataStoreContentProvider());
		viewer.setLabelProvider(new DataStoreLabelProvider());
		viewer.setInput(getViewSite());
		viewer.addDoubleClickListener(this);
	}

	public void doubleClick(DoubleClickEvent event) {
		if (!event.getSelection().isEmpty()) {
			Iterator<?> it = ((IStructuredSelection) event.getSelection())
					.iterator();
			while (it.hasNext()) {
				Object obj = it.next();
				if (obj instanceof FeatureNode) {
//					FeatureNode featureNode = (FeatureNode) obj;
//					FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = featureNode
//							.getFeatureSource();
//					IEditorPart ed = getSite().getWorkbenchWindow().getActivePage().getActiveEditor();
//					if(ed instanceof DefaultMapEditor){
////						((DefaultMapEditor)ed).addLayer(featureSource);
//					}
				}
			}
		}

	}

	@Override
	public void setFocus() {
		viewer.getTree().setFocus();
	}

	public void refresh() {
		viewer.refresh();
	}

	public void setDataStores(List<DataStore> dataStores) {
		this.dataStores = dataStores;
	}

	private class DataStoreContentProvider extends AbstractTreeContentProvider {

		public Object[] getElements(Object inputElement) {
			List<DataStoreNode> dataStoreNodes = new ArrayList<DataStoreNode>();
			// it is better to deal with OSGi reference using and iterator
			Iterator<DataStore> it = dataStores.iterator();
			while (it.hasNext())
				dataStoreNodes.add(new DataStoreNode(it.next()));
			return dataStoreNodes.toArray();
		}

	}

	private class DataStoreLabelProvider extends LabelProvider {

		@Override
		public String getText(Object element) {
			return super.getText(element);
		}

	}
}
