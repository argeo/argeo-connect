package org.argeo.connect.people.ui.views;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.argeo.ArgeoException;
import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.PeopleImages;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.utils.PeopleUtils;
import org.argeo.eclipse.ui.jcr.JcrImages;
import org.argeo.jcr.ArgeoNames;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.part.ViewPart;

/** General people browser. */
public class PeopleBrowserView extends ViewPart implements ArgeoNames,
		PeopleNames {
	public final static String ID = PeopleUiPlugin.PLUGIN_ID
			+ ".peopleBrowserView";

	private TreeViewer treeViewer;
	private Repository repository;

	private Session session;

	@Override
	public void createPartControl(Composite parent) {
		try {
			session = repository.login();
		} catch (RepositoryException e) {
			throw new ArgeoException("Cannot log to repository", e);
		}

		Tree tree = new Tree(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
		treeViewer = new TreeViewer(tree);

		TreeColumn column1 = new TreeColumn(tree, SWT.LEFT);
		column1.setAlignment(SWT.LEFT);
		column1.setText("Name");
		column1.setWidth(300);
		TreeColumn column2 = new TreeColumn(tree, SWT.RIGHT);
		column2.setAlignment(SWT.LEFT);
		column2.setText("First Name");
		column2.setWidth(200);
		TreeColumn column3 = new TreeColumn(tree, SWT.RIGHT);
		column3.setAlignment(SWT.LEFT);
		column3.setText("Age");
		column3.setWidth(35);

		treeViewer.setContentProvider(new PeopleBrowserContentProvider());
		treeViewer.setLabelProvider(new PeopleBrowserLabelProvider());
		treeViewer.setSorter(new ViewerSorter());
		treeViewer.setInput(session);
		treeViewer.expandToLevel(3);
	}

	@Override
	public void dispose() {
		JcrUtils.logoutQuietly(session);
		super.dispose();
	}

	@Override
	public void setFocus() {
		treeViewer.getTree().setFocus();
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	class PeopleBrowserContentProvider implements ITreeContentProvider {
		public Object[] getChildren(Object parentElement) {
			try {
				Node node = (Node) parentElement;
				return JcrUtils.nodeIteratorToList(node.getNodes()).toArray();
			} catch (RepositoryException e) {
				throw new ArgeoException("Cannot get children for "
						+ parentElement, e);
			}
		}

		public Object getParent(Object element) {
			try {
				Node node = (Node) element;
				return node.getParent();
			} catch (ItemNotFoundException e) {// root node
				return null;
			} catch (RepositoryException e) {
				throw new ArgeoException("Cannot get parent for " + element, e);
			}
		}

		public boolean hasChildren(Object element) {
			try {
				Node node = (Node) element;
				return node.hasNodes();
			} catch (RepositoryException e) {
				throw new ArgeoException("Cannot check has children for "
						+ element, e);
			}
		}

		public Object[] getElements(Object input) {
			try {
				Session session = (Session) input;
				if (!session.itemExists(PeopleConstants.PEOPLE_BASE_PATH))
					return new Object[0];
				return JcrUtils.nodeIteratorToList(
						session.getNode(PeopleConstants.PEOPLE_BASE_PATH)
								.getNodes()).toArray();
			} catch (RepositoryException e) {
				throw new ArgeoException("Cannot get root elements", e);
			}
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	class PeopleBrowserLabelProvider implements ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			try {
				Node node = (Node) element;
				if (columnIndex == 0)
					if (node.isNodeType(PeopleTypes.PEOPLE_PERSON))
						return PeopleImages.PERSON;
					else
						return JcrImages.FOLDER;
				return null;
			} catch (RepositoryException e) {
				throw new ArgeoException("Cannot get label for " + element, e);
			}
		}

		public String getColumnText(Object element, int columnIndex) {
			try {
				Node node = (Node) element;
				if (node.isNodeType(PeopleTypes.PEOPLE_PERSON)) {
					switch (columnIndex) {
					case 0:
						return node.getProperty(ARGEO_LAST_NAME).getString()
								.toUpperCase();
					case 1:
						return node.getProperty(ARGEO_FIRST_NAME).getString();
					case 2:
						return PeopleUtils.computeAge(
								node.getProperty(PEOPLE_DATE_OF_BIRTH)
										.getDate()).toString();
					}
				} else if (columnIndex == 0) {
					String label = node.isNodeType(NodeType.MIX_TITLE) ? node
							.getProperty(Property.JCR_TITLE).getString() : node
							.getName();
					if (node.hasProperty(PEOPLE_COUNT)) {
						Long count = node.getProperty(PEOPLE_COUNT).getLong();
						String countStr;
						if (count / 1000 > 0)
							countStr = (count / 1000) + " "
									+ String.format("%03d", count % 1000);
						else
							countStr = count.toString();
						label = label + " (" + countStr + ")";
					}
					return label;
				}
				return null;
			} catch (RepositoryException e) {
				throw new ArgeoException("Cannot get label for " + element, e);
			}
		}

		public void addListener(ILabelProviderListener listener) {
		}

		public void dispose() {
		}

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
		}
	}

}
