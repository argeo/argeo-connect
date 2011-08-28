package org.argeo.gis.ui.views;

import java.io.IOException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.ArgeoException;
import org.argeo.eclipse.ui.Error;
import org.argeo.eclipse.ui.jcr.JcrImages;
import org.argeo.eclipse.ui.jcr.SimpleNodeContentProvider;
import org.argeo.gis.GisTypes;
import org.argeo.gis.ui.MapViewer;
import org.argeo.gis.ui.editors.DefaultMapEditor;
import org.argeo.gis.ui.editors.MapEditorInput;
import org.argeo.gis.ui.editors.MapFormPage;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.part.ViewPart;
import org.geotools.geometry.jts.ReferencedEnvelope;

public class FeatureSourcesView extends ViewPart implements
		IDoubleClickListener {
	private String dataStoresBasePath = "/gis/dataStores";

	private Session session;

	private TreeViewer viewer;

	private String editorId="org.argeo.connect.ui.crisis.defaultMapEditor";

	@Override
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		String[] basePaths = { dataStoresBasePath };
		SimpleNodeContentProvider sncp = new SimpleNodeContentProvider(session,
				basePaths);
		sncp.setMkdirs(true);
		viewer.setContentProvider(sncp);
		viewer.setLabelProvider(new MapsLabelProvider());
		viewer.setInput(getViewSite());
		viewer.addDoubleClickListener(this);
	}

	public void doubleClick(DoubleClickEvent event) {
		if (event.getSelection().isEmpty())
			return;
		Object obj = ((IStructuredSelection) event.getSelection())
				.getFirstElement();
		if (obj instanceof Node) {
			Node node = (Node) obj;
			try {
				if (!node.isNodeType(GisTypes.GIS_FEATURE_SOURCE))
					return;

				IWorkbenchPage activePage = getSite().getWorkbenchWindow()
						.getActivePage();
				IEditorPart ed = activePage.getActiveEditor();
				if (ed == null)
					ed = activePage.openEditor(new MapEditorInput(node),
							editorId);

				MapViewer mapViewer = null;
				if (ed instanceof DefaultMapEditor) {
					mapViewer = ((DefaultMapEditor) ed).getMapViewer();
				} else if (ed instanceof FormEditor) {
					IFormPage formPage = ((FormEditor) ed)
							.getActivePageInstance();
					if (formPage instanceof MapFormPage)
						mapViewer = ((MapFormPage) activePage).getMapViewer();
				}

				if (mapViewer == null)
					return;

				mapViewer.addLayer(node, null);
				ReferencedEnvelope areaOfInterest = mapViewer.getGeoJcrMapper()
						.getFeatureSource(node).getBounds();
				mapViewer.setAreaOfInterest(areaOfInterest);
			} catch (Exception e) {
				Error.show("Cannot open " + node, e);
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

	public void setSession(Session session) {
		this.session = session;
	}

	private class MapsLabelProvider extends ColumnLabelProvider {

		@Override
		public String getText(Object element) {
			try {
				if (element instanceof Node) {
					Node node = (Node) element;
					return node.getName();
				}
			} catch (RepositoryException e) {
				throw new ArgeoException("Cannot get text", e);
			}
			return super.getText(element);
		}

		@Override
		public Image getImage(Object element) {
			try {
				if (element instanceof Node) {
					Node node = (Node) element;
					if (node.isNodeType(GisTypes.GIS_FEATURE_SOURCE))
						return JcrImages.BINARY;
					else if (node.isNodeType(GisTypes.GIS_DATA_STORE))
						return JcrImages.NODE;
					return JcrImages.FOLDER;
				}
			} catch (RepositoryException e) {
				throw new ArgeoException("Cannot get text", e);
			}
			return super.getImage(element);
		}

	}
}
