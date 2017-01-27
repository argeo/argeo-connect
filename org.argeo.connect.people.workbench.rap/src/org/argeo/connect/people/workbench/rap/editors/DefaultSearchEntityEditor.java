package org.argeo.connect.people.workbench.rap.editors;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;

import org.argeo.connect.ConnectConstants;
import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.workbench.rap.PeopleRapPlugin;
import org.argeo.connect.people.workbench.rap.composites.dropdowns.TagLikeDropDown;
import org.argeo.connect.people.workbench.rap.editors.util.AbstractSearchEntityEditor;
import org.argeo.connect.people.workbench.rap.providers.JcrHtmlLabelProvider;
import org.argeo.connect.ui.ConnectColumnDefinition;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.util.XPathUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

/** Search the repository with a given entity type */
public class DefaultSearchEntityEditor extends AbstractSearchEntityEditor {

	public final static String ID = PeopleRapPlugin.PLUGIN_ID + ".defaultSearchEntityEditor";

	// Default column
	private List<ConnectColumnDefinition> colDefs;
	private TagLikeDropDown tagDD;

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		colDefs = new ArrayList<ConnectColumnDefinition>();
		colDefs.add(new ConnectColumnDefinition("Display Name", new JcrHtmlLabelProvider(Property.JCR_TITLE), 300));
		colDefs.add(new ConnectColumnDefinition("Tags", new JcrHtmlLabelProvider(PEOPLE_TAGS), 300));
	}

	/** Override this to provide type specific static filters */
	protected void populateStaticFilters(Composite body) {
		body.setLayout(new GridLayout(4, false));

		Text tagTxt = createBoldLT(body, "Tag", "",
				"Select from list to find entities that are categorised with this tag");
		tagDD = new TagLikeDropDown(getSession(), getPeopleService().getResourceService(), PeopleConstants.RESOURCE_TAG,
				tagTxt);

		Button goBtn = new Button(body, SWT.PUSH);
		goBtn.setText("Search");
		goBtn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				refreshFilteredList();
			}
		});

		Button resetBtn = new Button(body, SWT.PUSH);
		resetBtn.setText("Reset");
		resetBtn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				tagDD.reset(null);
			}
		});
	}

	/** Refresh the table viewer based on the free text search field */
	protected void refreshFilteredList() {
		try {
			QueryManager queryManager = getSession().getWorkspace().getQueryManager();

			// XPath
			StringBuilder builder = new StringBuilder();
			builder.append("//element(*, ").append(getEntityType()).append(")");
			String attrQuery = XPathUtils.localAnd(XPathUtils.getFreeTextConstraint(getFilterText().getText()),
					XPathUtils.getPropertyEquals(PEOPLE_TAGS, tagDD.getText()));
			if (EclipseUiUtils.notEmpty(attrQuery))
				builder.append("[").append(attrQuery).append("]");
			builder.append(" order by @").append(PeopleNames.JCR_TITLE).append(" ascending");
			Query query = queryManager.createQuery(builder.toString(), ConnectConstants.QUERY_XPATH);

			QueryResult result = query.execute();
			Row[] rows = ConnectJcrUtils.rowIteratorToArray(result.getRows());
			setViewerInput(rows);
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to list " + getEntityType() + " entities with static filter ", e);
		}
	}

	/** Overwrite to provide corresponding column definitions */
	@Override
	public List<ConnectColumnDefinition> getColumnDefinition(String extractId) {
		return colDefs;
	}
}