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
package org.argeo.connect.ui.crisis.views;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.qom.Ordering;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Source;

import org.argeo.ArgeoException;
import org.argeo.gis.GisTypes;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class TrackerView extends ViewPart {
	private TableViewer viewer;

	private Session session;

	@Override
	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent);

	}

	@Override
	public void setFocus() {
		viewer.getTable().setFocus();
	}

	protected Query createQuery() {
		try {

			QueryObjectModelFactory qomf = session.getWorkspace()
					.getQueryManager().getQOMFactory();

			String nodeSelector = "n";
			Source source = qomf.selector(GisTypes.GIS_INDEXED, nodeSelector);
			Ordering byLastModified = qomf.descending(qomf.propertyValue(
					nodeSelector, Property.JCR_LAST_MODIFIED));
			qomf.createQuery(source, null, new Ordering[] { byLastModified },
					null);

			return null;
		} catch (RepositoryException e) {
			throw new ArgeoException("Cannot create query", e);
		}
	}

	static class TrackerContentProvider implements IStructuredContentProvider {

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public Object[] getElements(Object inputElement) {
			try {
				Query query = (Query) inputElement;
				NodeIterator nit = query.execute().getNodes();
				List<Node> nodes = new ArrayList<Node>();
				while (nit.hasNext()) {
					nodes.add(nit.nextNode());
				}
				return nodes.toArray();
			} catch (RepositoryException e) {
				throw new ArgeoException("Cannot retrieve tracked items", e);
			}
		}

	}

}
