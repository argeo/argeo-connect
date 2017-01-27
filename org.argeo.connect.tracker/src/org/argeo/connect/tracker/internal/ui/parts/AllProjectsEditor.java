package org.argeo.connect.tracker.internal.ui.parts;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.ui.PeopleColumnDefinition;
import org.argeo.connect.people.util.JcrUiUtils;
import org.argeo.connect.people.util.XPathUtils;
import org.argeo.connect.people.workbench.rap.editors.util.AbstractSearchEntityEditor;
import org.argeo.connect.people.workbench.rap.providers.JcrHtmlLabelProvider;
import org.argeo.connect.tracker.TrackerException;
import org.argeo.connect.tracker.core.TrackerUtils;
import org.argeo.connect.tracker.ui.TrackerUiPlugin;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

/** Search the repository with a given entity type */
public class AllProjectsEditor extends AbstractSearchEntityEditor {

	public final static String ID = TrackerUiPlugin.PLUGIN_ID + ".allProjectsEditor";

	// Default column
	private List<PeopleColumnDefinition> colDefs;

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		colDefs = new ArrayList<PeopleColumnDefinition>();
		colDefs.add(new PeopleColumnDefinition("Name", new JcrHtmlLabelProvider(Property.JCR_TITLE), 300));
		colDefs.add(new PeopleColumnDefinition("Account", new AccountLp(), 300));
		colDefs.add(new PeopleColumnDefinition("Open Issues", new CountLp(), 150));
	}

	// public void createPartControl(Composite parent) {
	// super.createPartControl(parent);
	// getTableViewer().getTableViewer().addDoubleClickListener(new
	// IDoubleClickListener() {
	//
	// @Override
	// public void doubleClick(DoubleClickEvent event) {
	// System.out.println("Double clicked");
	// }
	// });
	// }

	protected boolean showStaticFilterSection() {
		return false;
	}

	/** Refresh the table viewer based on the free text search field */
	protected void refreshFilteredList() {
		try {
			QueryManager queryManager = getSession().getWorkspace().getQueryManager();
			// XPath
			StringBuilder builder = new StringBuilder();
			builder.append("//element(*, ").append(getEntityType()).append(")");
			String filter = getFilterText().getText();
			if (EclipseUiUtils.notEmpty(filter))
				builder.append("[").append(XPathUtils.getFreeTextConstraint(filter)).append("]");
			builder.append(" order by @").append(PeopleNames.JCR_TITLE).append(" ascending");
			Query query = queryManager.createQuery(builder.toString(), PeopleConstants.QUERY_XPATH);
			QueryResult result = query.execute();
			Node[] nodes = JcrUiUtils.nodeIteratorToArray(result.getNodes());
			setViewerInput(nodes);
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to list " + getEntityType() + " entities with static filter ", e);
		}
	}

	/** Overwrite to provide corresponding column definitions */
	@Override
	public List<PeopleColumnDefinition> getColumnDefinition(String extractId) {
		return colDefs;
	}

	private class CountLp extends ColumnLabelProvider {
		private static final long serialVersionUID = -2958762435923332964L;

		@Override
		public String getText(Object element) {
			Node project = (Node) element;
			long currNb = TrackerUtils.getIssues(project, null, null, null, true).getSize();
			return currNb + "";
		}
	}

	private class AccountLp extends ColumnLabelProvider {
		private static final long serialVersionUID = 1349855402929072597L;

		@Override
		public String getText(Object element) {
			Node project = (Node) element;
			try {
				// TODO this is defined in a client app.
				Node account = project.getReferences().nextProperty().getParent().getParent().getParent();
				return JcrUiUtils.get(account, Property.JCR_TITLE);
			} catch (RepositoryException e) {
				throw new TrackerException("unable to retrieve related account ", e);

			}
			// return "";
		}
	}
}