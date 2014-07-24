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
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.Ordering;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.editors.utils.AbstractSearchEntityEditor;
import org.argeo.connect.people.ui.exports.PeopleColumnDefinition;
import org.argeo.connect.people.ui.providers.JcrRowHtmlLabelProvider;
import org.argeo.connect.people.ui.utils.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

/** Display a list for all values of a given resource. */
public class ListValuesEditor extends AbstractSearchEntityEditor {

	public final static String ID = PeopleUiPlugin.PLUGIN_ID
			+ ".listValuesEditor";

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
	}

	@Override
	public List<PeopleColumnDefinition> getColumnDefinition(String extractId) {

		List<PeopleColumnDefinition> colDefs = new ArrayList<PeopleColumnDefinition>();
		colDefs.add(new PeopleColumnDefinition(
				getEntityType(),
				Property.JCR_TITLE,
				PropertyType.STRING,
				"Title",
				new JcrRowHtmlLabelProvider(getEntityType(), Property.JCR_TITLE),
				300));
		return colDefs;
	}

	protected boolean showStaticFilterSection() {
		return false;
	}

	// unused compulsory method.
	protected void populateStaticFilters(Composite body) {
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

			Constraint defaultC = getFreeTextConstraint(factory, source);

			defaultC = PeopleUiUtils.localAnd(factory, defaultC, factory
					.descendantNode(source.getSelectorName(), getBasePath()));

			// TODO handle the case where no TITLE prop is available
			Ordering order = factory.ascending(factory.propertyValue(
					source.getSelectorName(), Property.JCR_TITLE));
			Ordering[] orderings = { order };
			QueryObjectModel query = factory.createQuery(source, defaultC,
					orderings, null);
			QueryResult result = query.execute();
			Row[] rows = CommonsJcrUtils.rowIteratorToArray(result.getRows());
			setViewerInput(rows);

		} catch (RepositoryException e) {
			throw new PeopleException("Unable to list " + getEntityType()
					+ " entities with static filter ", e);
		}
	}
}