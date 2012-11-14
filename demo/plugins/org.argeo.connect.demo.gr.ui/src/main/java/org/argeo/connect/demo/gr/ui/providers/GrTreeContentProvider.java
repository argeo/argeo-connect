package org.argeo.connect.demo.gr.ui.providers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;

import org.argeo.ArgeoException;
import org.argeo.connect.demo.gr.GrConstants;
import org.argeo.connect.demo.gr.GrException;
import org.argeo.connect.demo.gr.GrNames;
import org.argeo.connect.demo.gr.GrTypes;
import org.argeo.eclipse.ui.jcr.SimpleNodeContentProvider;

// Add specific behaviours to the node provider
public class GrTreeContentProvider extends SimpleNodeContentProvider {

	private Session session;

	public GrTreeContentProvider(Session session, String[] basePaths) {
		super(session, basePaths);
		this.session = session;
	}

	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof Node) {
			try {
				Node node = (Node) parentElement;
				if (node.isNodeType(GrTypes.GR_NETWORK)) {
					String stmt = "SELECT * FROM [" + GrTypes.GR_WATER_SITE
							+ "] AS sites WHERE ISDESCENDANTNODE('"
							+ node.getPath() + "') " + "AND sites.["
							+ GrNames.GR_SITE_TYPE + "]='"
							+ GrConstants.MONITORED + "'";
					Query query = session.getWorkspace().getQueryManager()
							.createQuery(stmt, Query.JCR_SQL2);
					NodeIterator ni = query.execute().getNodes();
					// NodeIterator ni = network.getNodes();
					List<Node> children = new ArrayList<Node>();
					while (ni.hasNext())
						children.add(ni.nextNode());
					return children.toArray();
				} else if (node.isNodeType(GrTypes.GR_SITE))
					return null;
			} catch (RepositoryException e) {
				throw new GrException("Cannot get children for network", e);
			}
		}
		return super.getChildren(parentElement);
	}

	public boolean hasChildren(Object element) {
		if (element instanceof Node) {
			try {
				Node node = (Node) element;
				if (node.isNodeType(GrTypes.GR_SITE))
					return false;
			} catch (RepositoryException e) {
				throw new GrException("Cannot determine if child has nodes", e);
			}
		}
		return super.hasChildren(element);
	}

	@Override
	protected List<Node> filterChildren(List<Node> children)
			throws RepositoryException {
		return super.filterChildren(children);
	}

	@Override
	protected Object[] sort(Object parent, Object[] children) {
		Arrays.sort(children, new Comparator<Object>() {

			public int compare(Object o1, Object o2) {
				Node node1 = (Node) o1;
				Node node2 = (Node) o2;
				try {
					return node1.getPath().compareTo(node2.getPath());
				} catch (RepositoryException e) {
					throw new ArgeoException("Cannot compare " + node1
							+ " and " + node2, e);
				}
			}

		});
		return children;
	}
}