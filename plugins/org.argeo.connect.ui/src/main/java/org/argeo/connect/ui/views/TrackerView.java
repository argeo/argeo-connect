package org.argeo.connect.ui.views;

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
import org.argeo.jcr.gis.GisTypes;
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
