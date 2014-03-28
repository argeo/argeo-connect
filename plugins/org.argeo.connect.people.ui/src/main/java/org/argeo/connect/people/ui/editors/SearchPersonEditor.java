package org.argeo.connect.people.ui.editors;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.qom.Ordering;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.editors.utils.AbstractSearchEntityEditor;
import org.argeo.connect.people.ui.extracts.PeopleColumnDefinition;
import org.argeo.eclipse.ui.jcr.lists.SimpleJcrRowLabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

/**
 * Basic search on persons with no static filtering
 */
public class SearchPersonEditor extends AbstractSearchEntityEditor {

	public final static String ID = PeopleUiPlugin.PLUGIN_ID
			+ ".searchPersonEditor";

	// Default column
	private List<PeopleColumnDefinition> colDefs = new ArrayList<PeopleColumnDefinition>();
	{
		colDefs.add(new PeopleColumnDefinition(PeopleTypes.PEOPLE_PERSON,
				Property.JCR_TITLE, PropertyType.STRING, "Display Name",
				new SimpleJcrRowLabelProvider(PeopleTypes.PEOPLE_PERSON,
						Property.JCR_TITLE), 300));
		colDefs.add(new PeopleColumnDefinition(PeopleTypes.PEOPLE_PERSON,
				PEOPLE_LAST_NAME, PropertyType.STRING, "Last name",
				new SimpleJcrRowLabelProvider(PeopleTypes.PEOPLE_PERSON,
						PEOPLE_LAST_NAME), 120));
		colDefs.add(new PeopleColumnDefinition(PeopleTypes.PEOPLE_PERSON,
				PEOPLE_FIRST_NAME, PropertyType.STRING, "First name",
				new SimpleJcrRowLabelProvider(PeopleTypes.PEOPLE_PERSON,
						PEOPLE_FIRST_NAME), 120));
		colDefs.add(new PeopleColumnDefinition(PeopleTypes.PEOPLE_PERSON,
				PEOPLE_TAGS, PropertyType.STRING, "Tags",
				new SimpleJcrRowLabelProvider(PeopleTypes.PEOPLE_PERSON,
						PEOPLE_TAGS), 200));
	};

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
	}

	@Override
	protected boolean showStaticFilterSection() {
		return false;
	}

	@Override
	public List<PeopleColumnDefinition> getColumnDefinition(String extractId) {
		return colDefs;
	}

	/** Override this to provide type specific staic filters */
	protected void populateStaticFilters(Composite body) {
		// unused
	}

	/** Refresh the table viewer based on the free text search field */
	protected void refreshStaticFilteredList() {
		try {
			Session session = getSession();
			QueryManager queryManager = session.getWorkspace()
					.getQueryManager();
			QueryObjectModelFactory factory = queryManager.getQOMFactory();
			Selector source = factory
					.selector(getEntityType(), getEntityType());

			// TODO handle the case where no TITLE prop is available
			Ordering order = factory.ascending(factory.propertyValue(
					source.getSelectorName(), Property.JCR_TITLE));
			Ordering[] orderings = { order };
			QueryObjectModel query = factory.createQuery(source,
					getFreeTextConstraint(factory, source), orderings, null);
			QueryResult result = query.execute();
			Row[] rows = rowIteratorToArray(result.getRows());
			setViewerInput(rows);

		} catch (RepositoryException e) {
			throw new PeopleException("Unable to list " + getEntityType()
					+ " entities with static filter ", e);
		}
	}
}