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