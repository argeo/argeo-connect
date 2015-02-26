package org.argeo.connect.people.rap.monitoring;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.rap.utils.Refreshable;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

/**
 * Quick and dirty editor to monitor number of references that point to a given
 * node: we have noticed issues with PostgreSQL cache management on big data
 * import
 * 
 * TODO work on this to enrich the generic monitoring perspective.
 */
public class ReferencedList extends NodeTypeList implements Refreshable {

	private final int MIN_REF_NB = 5;

	// Business Object
	private String[][] elements;

	// This page widget
	// private TableViewer tableViewer;
	private NameLP nameLP = new NameLP();
	private CountLP countLP = new CountLP();

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		setPartName("Referenced list");
		setTitleToolTip("Display the list of referenceable nodes that "
				+ "have more than 10 references");
	}

	@Override
	public void forceRefresh(Object object) {
		// TODO put this in a job
		try {
			Query query = getSession()
					.getWorkspace()
					.getQueryManager()
					.createQuery(
							"select * from [nt:unstructured] as instances",
							Query.JCR_SQL2);
			NodeIterator nit = query.execute().getNodes();
			TableViewer tableViewer = getTableViewer();
			List<String[]> infos = new ArrayList<String[]>();
			while (nit.hasNext()) {
				Node currNode = nit.nextNode();
				PropertyIterator pit = currNode.getReferences();
				if (pit.getSize() > MIN_REF_NB) {
					String[] vals = new String[2];
					vals[0] = nameLP.getText(currNode);
					vals[1] = countLP.getText(pit.getSize());
					infos.add(vals);
				}
			}
			if (infos.size() > 0) {
				elements = infos.toArray(new String[1][2]);
				tableViewer.setInput(elements);
				tableViewer.setItemCount(elements.length);
			} else {
				elements = null;
				tableViewer.setInput(elements);
				tableViewer.setItemCount(0);
			}
			tableViewer.refresh();
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to refresh node type list table",
					e);
		}
	}

	// PROVIDERS
	protected class NameLP extends ColumnLabelProvider {
		private static final long serialVersionUID = -8179051587196328000L;

		@Override
		public String getText(Object element) {
			try {
				Node node = (Node) element;
				return node.getPath();
			} catch (RepositoryException e) {
				throw new PeopleException("Unable to retrieve name " + "for "
						+ element, e);
			}
		}
	}

	protected class CountLP extends ColumnLabelProvider {
		private static final long serialVersionUID = -4022534826825314784L;

		@Override
		public String getText(Object element) {
			return numberFormat.format((Long) element);
		}
	}
}