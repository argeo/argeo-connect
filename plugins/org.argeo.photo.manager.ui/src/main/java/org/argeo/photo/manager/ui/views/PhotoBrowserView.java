package org.argeo.photo.manager.ui.views;

import java.io.File;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.RepositoryFactory;
import javax.jcr.Session;

import org.argeo.ArgeoException;
import org.argeo.jcr.ArgeoJcrUtils;
import org.argeo.jcr.JcrUtils;
import org.argeo.photo.manager.ui.PhotoManagerUiPlugin;
import org.argeo.photo.manager.ui.editors.RawEditor;
import org.argeo.photo.manager.ui.editors.RawEditorInput;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreePathContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

/** Allows to browse the various photo repositories */
public class PhotoBrowserView extends ViewPart {
	public static final String ID = PhotoManagerUiPlugin.PLUGIN_ID
			+ ".photoBrowserView";

	private TreeViewer viewer;

	private final static String JCR_REPO_RELATIVE_PATH = "/metadata/jcr";
	private String picturesBase = System.getProperty("user.home") + "/Pictures";

	private RepositoryFactory repositoryFactory;
	private Session session;

	public void createPartControl(Composite root) {
		String jcrRepoUri = "file://" + picturesBase + JCR_REPO_RELATIVE_PATH;
		try {
			Repository repository = ArgeoJcrUtils.getRepositoryByUri(
					repositoryFactory, jcrRepoUri);
			session = repository.login();
		} catch (RepositoryException e) {
			throw new ArgeoException("Cannot access JCR repository "
					+ jcrRepoUri, e);
		}

		viewer = new TreeViewer(root, SWT.MULTI | SWT.V_SCROLL);

		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setInput(getViewSite());

		try {
			getSite().getPage().openEditor(new RawEditorInput(), RawEditor.ID);
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setFocus() {
		viewer.getControl().setFocus();
	}

	@Override
	public void dispose() {
		JcrUtils.logoutQuietly(session);
	}

	public void setPicturesBase(String picturesBase) {
		this.picturesBase = picturesBase;
	}

	public void setRepositoryFactory(RepositoryFactory repositoryFactory) {
		this.repositoryFactory = repositoryFactory;
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