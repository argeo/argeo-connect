package org.argeo.connect.people.rap.editors;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
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

import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.rap.PeopleRapPlugin;
import org.argeo.connect.people.rap.composites.dropdowns.SimpleResourceDropDown;
import org.argeo.connect.people.rap.editors.utils.AbstractSearchEntityEditor;
import org.argeo.connect.people.rap.exports.PeopleColumnDefinition;
import org.argeo.connect.people.rap.utils.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.eclipse.ui.jcr.lists.SimpleJcrRowLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.Section;

/**
 * Search the repository with a given entity type
 */
public class DefaultSearchEntityEditor extends AbstractSearchEntityEditor {

	public final static String ID = PeopleRapPlugin.PLUGIN_ID
			+ ".defaultSearchEntityEditor";

	// Default column
	private List<PeopleColumnDefinition> colDefs;
	private SimpleResourceDropDown tagDD;

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		colDefs = new ArrayList<PeopleColumnDefinition>();
		colDefs.add(new PeopleColumnDefinition(getEntityType(),
				Property.JCR_TITLE, PropertyType.STRING, "Display Name",
				new SimpleJcrRowLabelProvider(getEntityType(),
						Property.JCR_TITLE), 300));
		colDefs.add(new PeopleColumnDefinition(getEntityType(), PEOPLE_TAGS,
				PropertyType.STRING, "Tags", new SimpleJcrRowLabelProvider(
						getEntityType(), PEOPLE_TAGS), 300));
	}

	/** Override this to provide type specific static filters */
	protected void populateStaticFilters(Composite parent) {
		parent.setLayout(PeopleUiUtils.noSpaceGridLayout());

		// Configure the Twistie section
		Section headerSection = new Section(parent, Section.TITLE_BAR
				| Section.TWISTIE);
		headerSection.setText("Show more filters");
		headerSection.setExpanded(false);
		headerSection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				false));

		Composite body = new Composite(headerSection, SWT.NONE);
		headerSection.setClient(body);

		body.setLayout(new GridLayout(4, false));

		Text tagTxt = createBoldLT(body, "Tag", "",
				"Select from list to find entities that are categorised with this tag");
		tagDD = new SimpleResourceDropDown(getPeopleUiService(), getSession(),
				getPeopleService().getResourceBasePath(PeopleConstants.RESOURCE_TAG), tagTxt);

		Button goBtn = new Button(body, SWT.PUSH);
		goBtn.setText("Search");
		goBtn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				refreshStaticFilteredList();
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
	protected void refreshStaticFilteredList() {
		try {
			QueryManager queryManager = getSession().getWorkspace()
					.getQueryManager();
			QueryObjectModelFactory factory = queryManager.getQOMFactory();
			Selector source = factory
					.selector(getEntityType(), getEntityType());

			Constraint defaultC = getFreeTextConstraint(factory, source);

			// Tag
			String currVal = tagDD.getText();
			if (CommonsJcrUtils.checkNotEmptyString(currVal)) {
				StaticOperand so = factory.literal(getSession()
						.getValueFactory().createValue(currVal));
				DynamicOperand dyo = factory.propertyValue(
						source.getSelectorName(), PEOPLE_TAGS);
				Constraint currC = factory.comparison(dyo,
						QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO, so);
				defaultC = PeopleUiUtils.localAnd(factory, defaultC, currC);
			}

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

	/** Overwrite to provide corresponding column definitions */
	@Override
	public List<PeopleColumnDefinition> getColumnDefinition(String extractId) {
		return colDefs;
	}
}