package org.argeo.photo.manager.ui.views;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import org.argeo.ArgeoException;
import org.argeo.eclipse.ui.workbench.ErrorFeedback;
import org.argeo.photo.manager.PictureManager;
import org.argeo.photo.manager.ui.PhotoManagerImages;
import org.argeo.photo.manager.ui.PhotoManagerUiPlugin;
import org.argeo.photo.manager.ui.editors.PictureEditorInput;
import org.argeo.photo.manager.ui.editors.RawEditor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreePathContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

/** Allows to browse the various photo repositories */
public class PhotoBrowserView extends ViewPart {
	public static final String ID = PhotoManagerUiPlugin.PLUGIN_ID
			+ ".photoBrowserView";

	private TreeViewer viewer;

	private PictureManager pictureManager;

	public void createPartControl(Composite root) {
		viewer = new TreeViewer(root, SWT.MULTI | SWT.V_SCROLL);

		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.addDoubleClickListener(new ViewDoubleClickListener());
		viewer.setInput(getViewSite());

	}

	public void setFocus() {
		viewer.getControl().setFocus();
	}

	public void setPictureManager(PictureManager pictureManager) {
		this.pictureManager = pictureManager;
	}

	class ViewContentProvider implements ITreePathContentProvider {

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public Object[] getChildren(TreePath parentPath) {
			return pictureManager.getChildren(
					treePathToRelativePath(parentPath)).toArray();
		}

		public boolean hasChildren(TreePath path) {
			return pictureManager.hasChildren(treePathToRelativePath(path));
		}

		public TreePath[] getParents(Object element) {
			return new TreePath[0];
		}

		public Object[] getElements(Object parent) {
			return pictureManager.getChildren(treePathToRelativePath(null))
					.toArray();
		}

		private String treePathToRelativePath(TreePath path) {
			if (path == null || path.getSegmentCount() == 0)
				return "/";

			Node node = (Node) path.getLastSegment();
			try {
				String nodePath = node.getPath();
				return nodePath;
			} catch (RepositoryException e) {
				throw new ArgeoException("Cannot return relative path", e);
			}
			//
			// StringBuilder sb = new StringBuilder("");
			// for (int i = 0; i < path.getSegmentCount(); i++) {
			//
			// sb.append('/').append(path.getSegment(i).toString());
			// }
			// return sb.toString();
		}
	}

	class ViewLabelProvider extends LabelProvider {
		public String getText(Object obj) {
			if (obj instanceof Node) {
				Node node = (Node) obj;
				try {
					if (node.isNodeType(NodeType.MIX_TITLE))
						return node.getProperty(Property.JCR_TITLE).getString();
					else
						return node.getName();
				} catch (RepositoryException e) {
					throw new ArgeoException("Cannot get label", e);
				}
			}
			return null;
		}

		@Override
		public Image getImage(Object obj) {
			if (obj instanceof Node) {
				Node node = (Node) obj;
				try {
					if (node.isNodeType(NodeType.NT_FILE))
						return PhotoManagerImages.FILE;
					else if (node.isNodeType(NodeType.NT_FOLDER))
						return PhotoManagerImages.FOLDER;
				} catch (RepositoryException e) {
					throw new ArgeoException("Cannot get image", e);
				}
			}
			return null;
		}

	}

	public class ViewDoubleClickListener implements IDoubleClickListener {

		public void doubleClick(DoubleClickEvent event) {
			Object obj = ((IStructuredSelection) event.getSelection())
					.getFirstElement();
			if (obj instanceof Node) {
				Node node = (Node) obj;
				try {
					getSite().getPage().openEditor(
							new PictureEditorInput(node.getPath()),
							RawEditor.ID);
				} catch (Exception e) {
					ErrorFeedback.show("Cannot open editor", e);
				}
			}
		}

	}
}