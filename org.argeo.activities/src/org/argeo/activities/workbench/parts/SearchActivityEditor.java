package org.argeo.activities.workbench.parts;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;

import org.argeo.activities.ActivitiesException;
import org.argeo.activities.ActivitiesNames;
import org.argeo.activities.ActivitiesService;
import org.argeo.activities.ActivitiesTypes;
import org.argeo.activities.ui.AssignedToLP;
import org.argeo.activities.workbench.ActivitiesUiPlugin;
import org.argeo.connect.ConnectNames;
import org.argeo.connect.ui.ConnectColumnDefinition;
import org.argeo.connect.ui.IJcrTableViewer;
import org.argeo.connect.ui.util.JcrRowLabelProvider;
import org.argeo.connect.ui.util.UserNameLP;
import org.argeo.connect.ui.workbench.parts.DefaultSearchEntityEditor;
import org.argeo.connect.ui.workbench.util.JcrHtmlLabelProvider;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.util.XPathUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

/** Search the repository with a given entity type */
public class SearchActivityEditor extends DefaultSearchEntityEditor implements IJcrTableViewer {

	public final static String ID = ActivitiesUiPlugin.PLUGIN_ID + ".searchActivityEditor";

	// Context
	private ActivitiesService activitiesService;

	// Default column
	private List<ConnectColumnDefinition> colDefs;

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		colDefs = new ArrayList<ConnectColumnDefinition>();
		colDefs.add(new ConnectColumnDefinition("Display Name", new JcrHtmlLabelProvider(Property.JCR_TITLE), 300));
		colDefs.add(new ConnectColumnDefinition("Tags", new JcrHtmlLabelProvider(ConnectNames.CONNECT_TAGS), 300));
	}

	/** Override this to provide type specific static filters */
	protected void populateStaticFilters(Composite body) {
	}

	/** Refresh the table viewer based on the free text search field */
	protected void refreshFilteredList() {
		try {
			// XPath
			StringBuilder builder = new StringBuilder();
			builder.append("//element(*, ").append(getEntityType()).append(")");
			String attrQuery = XPathUtils.getFreeTextConstraint(getFilterText().getText());
			if (EclipseUiUtils.notEmpty(attrQuery))
				builder.append("[").append(attrQuery).append("]");
			builder.append(" order by @").append(Property.JCR_TITLE).append(" ascending");
			Query query = XPathUtils.createQuery(getSession(), builder.toString());
			QueryResult result = query.execute();
			Row[] rows = ConnectJcrUtils.rowIteratorToArray(result.getRows());
			setViewerInput(rows);
		} catch (RepositoryException e) {
			throw new ActivitiesException("Unable to list " + getEntityType() + " entities with static filter ", e);
		}
	}

	/** Overwrite to provide corresponding column definitions */
	@Override
	public List<ConnectColumnDefinition> getColumnDefinition(String extractId) {

		String currType = getEntityType();
		List<ConnectColumnDefinition> columns = new ArrayList<ConnectColumnDefinition>();

		// The editor table sends a null ID whereas the JxlExport mechanism
		// always passes an ID
		if (EclipseUiUtils.isEmpty(extractId)) {
			if (ActivitiesTypes.ACTIVITIES_TASK.equals(currType)) {
				columns.add(new ConnectColumnDefinition("Status",
						new JcrRowLabelProvider(ActivitiesNames.ACTIVITIES_TASK_STATUS), 80));
				columns.add(new ConnectColumnDefinition("Title", new JcrRowLabelProvider(Property.JCR_TITLE), 200));
				columns.add(new ConnectColumnDefinition("Assigned To",
						new AssignedToLP(activitiesService, null, Property.JCR_DESCRIPTION), 150));
				columns.add(new ConnectColumnDefinition("Due Date",
						new JcrRowLabelProvider(ActivitiesNames.ACTIVITIES_DUE_DATE), 100));
				columns.add(new ConnectColumnDefinition("Close Date",
						new JcrRowLabelProvider(ActivitiesNames.ACTIVITIES_CLOSE_DATE), 100));
				columns.add(new ConnectColumnDefinition("Closed by",
						new UserNameLP(getUserAdminService(), null, ActivitiesNames.ACTIVITIES_CLOSED_BY), 120));
				return columns;
			} else
				return colDefs;

		}

		if (ActivitiesTypes.ACTIVITIES_TASK.equals(currType)) {
			columns.add(new ConnectColumnDefinition("Status",
					new JcrRowLabelProvider(ActivitiesNames.ACTIVITIES_TASK_STATUS)));
			columns.add(new ConnectColumnDefinition("Title", new JcrRowLabelProvider(Property.JCR_TITLE)));
			columns.add(new ConnectColumnDefinition("Description", new JcrRowLabelProvider(Property.JCR_DESCRIPTION)));
			columns.add(new ConnectColumnDefinition("Assigned To",
					new AssignedToLP(activitiesService, null, Property.JCR_DESCRIPTION)));
			columns.add(new ConnectColumnDefinition("Due Date",
					new JcrRowLabelProvider(ActivitiesNames.ACTIVITIES_DUE_DATE)));
			columns.add(new ConnectColumnDefinition("Wake-Up Date",
					new JcrRowLabelProvider(ActivitiesNames.ACTIVITIES_WAKE_UP_DATE)));
			columns.add(new ConnectColumnDefinition("Close Date",
					new JcrRowLabelProvider(ActivitiesNames.ACTIVITIES_CLOSE_DATE)));
			columns.add(new ConnectColumnDefinition("Closed by",
					new UserNameLP(getUserAdminService(), null, ActivitiesNames.ACTIVITIES_CLOSED_BY)));
		} else
			return null;

		return columns;
	}

	public void setActivitiesService(ActivitiesService activitiesService) {
		this.activitiesService = activitiesService;
	}
}
