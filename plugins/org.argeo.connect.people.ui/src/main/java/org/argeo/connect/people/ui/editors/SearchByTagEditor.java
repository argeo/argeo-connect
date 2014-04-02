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
import javax.jcr.query.qom.DynamicOperand;
import javax.jcr.query.qom.Ordering;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelConstants;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;
import javax.jcr.query.qom.StaticOperand;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.editors.utils.AbstractSearchEntityEditor;
import org.argeo.connect.people.ui.extracts.PeopleColumnDefinition;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.eclipse.ui.jcr.lists.SimpleJcrRowLabelProvider;
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

/**
 * Search the repository with a given entity type
 */
public class SearchByTagEditor extends AbstractSearchEntityEditor {

	public final static String ID = PeopleUiPlugin.PLUGIN_ID
			+ ".searchByTagEditor";

	private TagDropDown tagDD;
	private Button goBtn;

	// Default column
	private List<PeopleColumnDefinition> colDefs = new ArrayList<PeopleColumnDefinition>();
	{
		colDefs.add(new PeopleColumnDefinition(PeopleTypes.PEOPLE_ENTITY,
				Property.JCR_TITLE, PropertyType.STRING, "Display Name",
				new SimpleJcrRowLabelProvider(PeopleTypes.PEOPLE_ENTITY,
						Property.JCR_TITLE), 300));
		colDefs.add(new PeopleColumnDefinition(PeopleTypes.PEOPLE_ENTITY,
				PEOPLE_TAGS, PropertyType.STRING, "Tags",
				new SimpleJcrRowLabelProvider(PeopleTypes.PEOPLE_ENTITY,
						PEOPLE_TAGS), 300));
	};

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
	}

	public void setTagValue(String tag) {
		tagDD.reset(tag);
		refreshStaticFilteredList();
		// TODO solve the drop down problem when setting the text
		goBtn.setFocus();
	}

	@Override
	protected boolean queryOnCreation() {
		// the refresh will be done by the method above once the tag value has
		// been set.
		return false;
	}

	@Override
	public List<PeopleColumnDefinition> getColumnDefinition(String extractId) {
		return colDefs;
	}

	/** Override this to provide type specific staic filters */
	protected void populateStaticFilters(Composite body) {

		body.setLayout(new GridLayout(3, false));

		Text tagTxt = createBoldLT(body, "List entities for tag", "",
				"Select from list to find entities that are categorised with this tag");
		tagDD = new TagDropDown(tagTxt);

		goBtn = new Button(body, SWT.PUSH);
		goBtn.setText("Refresh list");
		goBtn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				refreshStaticFilteredList();
			}
		});
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

			// Tag
			String currVal = tagDD.getText();
			if (CommonsJcrUtils.checkNotEmptyString(currVal)) {
				StaticOperand so = factory.literal(session.getValueFactory()
						.createValue(currVal));
				DynamicOperand dyo = factory.propertyValue(
						source.getSelectorName(), PEOPLE_TAGS);
				Constraint currC = factory.comparison(dyo,
						QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO, so);
				defaultC = localAnd(factory, defaultC, currC);
			}

			// TODO handle the case where no TITLE prop is available
			Ordering order = factory.ascending(factory.propertyValue(
					source.getSelectorName(), Property.JCR_TITLE));
			Ordering[] orderings = { order };
			QueryObjectModel query = factory.createQuery(source, defaultC,
					orderings, null);
			QueryResult result = query.execute();
			Row[] rows = rowIteratorToArray(result.getRows());
			setViewerInput(rows);

		} catch (RepositoryException e) {
			throw new PeopleException("Unable to list " + getEntityType()
					+ " entities with static filter ", e);
		}
	}
}