package org.argeo.tracker.internal.workbench;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import org.argeo.connect.ui.ConnectColumnDefinition;
import org.argeo.connect.ui.ConnectImages;
import org.argeo.connect.ui.util.JcrHtmlLabelProvider;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.util.XPathUtils;
import org.argeo.connect.workbench.parts.AbstractSearchEntityEditor;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.tracker.TrackerException;
import org.argeo.tracker.TrackerNames;
import org.argeo.tracker.core.TrackerUtils;
import org.argeo.tracker.ui.TrackerLps;
import org.argeo.tracker.workbench.TrackerUiPlugin;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

/** Search the repository with a given entity type */
public class AllProjectsEditor extends AbstractSearchEntityEditor {

	public final static String ID = TrackerUiPlugin.PLUGIN_ID + ".allProjectsEditor";

	// Default column
	private List<ConnectColumnDefinition> colDefs;

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		setTitleImage(ConnectImages.SEARCH);
		colDefs = new ArrayList<ConnectColumnDefinition>();
		colDefs.add(new ConnectColumnDefinition("Name", new JcrHtmlLabelProvider(Property.JCR_TITLE), 300));
		colDefs.add(new ConnectColumnDefinition("Manager",
				new TrackerLps().new DnLabelProvider(getUserAdminService(), TrackerNames.TRACKER_MANAGER), 
				160));
		colDefs.add(new ConnectColumnDefinition("Description", new JcrHtmlLabelProvider(Property.JCR_DESCRIPTION), 300));
		colDefs.add(new ConnectColumnDefinition("Open Milestones", new CountLp(), 150));
		colDefs.add(new ConnectColumnDefinition("Open / All Tasks", getMSIssuesLP(), 120));

		// colDefs.add(new ColumnDefinition("Account", new AccountLp(), 300));
	}

	protected boolean showStaticFilterSection() {
		return false;
	}

	/** Refresh the table viewer based on the free text search field */
	protected void refreshFilteredList() {
		try {
			// XPath
			StringBuilder builder = new StringBuilder();
			builder.append("//element(*, ").append(getEntityType()).append(")");
			String filter = getFilterText().getText();
			if (EclipseUiUtils.notEmpty(filter))
				builder.append("[").append(XPathUtils.getFreeTextConstraint(filter)).append("]");
			builder.append(" order by @").append(Property.JCR_TITLE).append(" ascending");
			Query query = XPathUtils.createQuery(getSession(), builder.toString());
			QueryResult result = query.execute();
			Node[] nodes = ConnectJcrUtils.nodeIteratorToArray(result.getNodes());
			setViewerInput(nodes);
		} catch (RepositoryException e) {
			throw new TrackerException("Unable to list " + getEntityType() + " entities with static filter ", e);
		}
	}

	/** Overwrite to provide corresponding column definitions */
	@Override
	public List<ConnectColumnDefinition> getColumnDefinition(String extractId) {
		return colDefs;
	}

	private class CountLp extends ColumnLabelProvider {
		private static final long serialVersionUID = -2958762435923332964L;

		@Override
		public String getText(Object element) {
			Node project = (Node) element;
			long currNb = TrackerUtils.getOpenMilestones(project, null).getSize();
			return currNb + "";
		}
	}

	private ColumnLabelProvider getMSIssuesLP() {
		return new ColumnLabelProvider() {
			private static final long serialVersionUID = -998161071505982347L;

			@Override
			public String getText(Object element) {
				Node project = (Node) element;
				NodeIterator nit = TrackerUtils.getIssues(project, null, null, null, true);
				long openNb = nit.getSize();
				nit = TrackerUtils.getIssues(project, null, null, null, false);
				long allNb = nit.getSize();
				return openNb + " / " + allNb;
			}
		};
	}

	// private class AccountLp extends ColumnLabelProvider {
	// private static final long serialVersionUID = 1349855402929072597L;
	//
	// @Override
	// public String getText(Object element) {
	// Node project = (Node) element;
	// try {
	// // TODO this is defined in a client app.
	// if (project.getReferences().hasNext()) {
	// Node account =
	// project.getReferences().nextProperty().getParent().getParent().getParent();
	// return ConnectJcrUtils.get(account, Property.JCR_TITLE);
	// } else
	// return "-";
	// } catch (RepositoryException e) {
	// throw new TrackerException("unable to retrieve related account ", e);
	//
	// }
	// }
	// }
}
