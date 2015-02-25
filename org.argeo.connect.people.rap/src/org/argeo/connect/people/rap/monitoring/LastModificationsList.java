package org.argeo.connect.people.rap.monitoring;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Query;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.rap.utils.Refreshable;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

/**
 * Quick and dirty editor to monitor the last 100 modifications on a repo
 * 
 * TODO work on this to enrich the generic monitoring perspective.
 */
public class LastModificationsList extends NodeTypeList implements Refreshable {

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
		setPartName("Last modif. list");
		setTitleToolTip("Display the list of the most recent "
				+ "modifications done on the system");
	}

	@Override
	public void forceRefresh(Object object) {
		// TODO put this in a job
		try {
			Query query = getSession()
					.getWorkspace()
					.getQueryManager()
					.createQuery(
							"SELECT * FROM [" + NodeType.MIX_LAST_MODIFIED
									+ "] ORDER BY ["
									+ Property.JCR_LAST_MODIFIED + "] DESC ",
							Query.JCR_SQL2);
			query.setLimit(100);
			NodeIterator nit = query.execute().getNodes();

			List<String[]> infos = new ArrayList<String[]>();
			while (nit.hasNext()) {
				Node currNode = nit.nextNode();
				String[] vals = new String[2];
				vals[0] = nameLP.getText(currNode);
				vals[1] = countLP.getText(currNode);
				infos.add(vals);
			}

			elements = infos.toArray(new String[1][2]);
			TableViewer tableViewer = getTableViewer();
			tableViewer.setInput(elements);
			// we must explicitly set the items count
			tableViewer.setItemCount(elements.length);
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
			try {
				Node currNode = (Node) element;
				String modifBy = CommonsJcrUtils.get(currNode,
						Property.JCR_LAST_MODIFIED_BY);
				Calendar modifOn = currNode.getProperty(Property.JCR_LAST_MODIFIED)
						.getDate();
				return modifBy + " - " + dateFormat.format(modifOn.getTime());
			} catch (Exception e) {
				throw new PeopleException("Unable to retrieve and "
						+ "format last modif info for " + element, e);
			}
		}
	}
}