package org.argeo.photo.manager.ui.views;

import java.io.File;

import org.argeo.photo.manager.ui.PhotoManagerUiPlugin;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreePathContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.part.ViewPart;

/** Allows to browse the various photo repositories */
public class PhotoBrowserView extends ViewPart {
	public static final String ID = PhotoManagerUiPlugin.PLUGIN_ID
			+ ".photoBrowserView";

	private TreeViewer viewer;

	public void createPartControl(Composite root) {
		viewer = new TreeViewer(root, SWT.MULTI | SWT.V_SCROLL);

		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setInput(getViewSite());
	}

	public void setFocus() {
		viewer.getControl().setFocus();
	}

	class ViewContentProvider implements ITreePathContentProvider {

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public Object[] getChildren(TreePath parentPath) {
			return null;
		}

		public boolean hasChildren(TreePath path) {
			return false;
		}

		public TreePath[] getParents(Object element) {
			return new TreePath[0];
		}

		public Object[] getElements(Object parent) {
			return new Object[0];
		}
	}

	class ViewLabelProvider extends LabelProvider implements
			ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			if (!(obj instanceof File)) {
				if (index == 0)
					return obj != null ? obj.toString() : "null";
				else
					return "";
			}

			File file = (File) obj;

			if (index == 0) {
				return file.getName();
			} else {
				return null;
			}
		}

		public Image getColumnImage(Object obj, int index) {
			return null;
		}

	}
}