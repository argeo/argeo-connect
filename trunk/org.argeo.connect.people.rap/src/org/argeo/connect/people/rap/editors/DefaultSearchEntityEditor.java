package org.argeo.connect.people.rap.editors;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;

import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.rap.PeopleRapPlugin;
import org.argeo.connect.people.rap.composites.dropdowns.TagLikeDropDown;
import org.argeo.connect.people.rap.editors.util.AbstractSearchEntityEditor;
import org.argeo.connect.people.rap.providers.JcrHtmlLabelProvider;
import org.argeo.connect.people.ui.PeopleColumnDefinition;
import org.argeo.connect.people.util.JcrUiUtils;
import org.argeo.connect.people.util.XPathUtils;
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

	public final static String ID = PeopleRapPlugin.PLUGIN_ID
			+ ".defaultSearchEntityEditor";

	// Default column
	private List<PeopleColumnDefinition> colDefs;
	private TagLikeDropDown tagDD;

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		colDefs = new ArrayList<PeopleColumnDefinition>();
		colDefs.add(new PeopleColumnDefinition("Display Name",
				new JcrHtmlLabelProvider(Property.JCR_TITLE), 300));
		colDefs.add(new PeopleColumnDefinition("Tags",
				new JcrHtmlLabelProvider(PEOPLE_TAGS), 300));
	}

	/** Override this to provide type specific static filters */
	protected void populateStaticFilters(Composite body) {
		body.setLayout(new GridLayout(4, false));

		Text tagTxt = createBoldLT(body, "Tag", "",
				"Select from list to find entities that are categorised with this tag");
		tagDD = new TagLikeDropDown(getSession(), getPeopleService()
				.getResourceService(), PeopleConstants.RESOURCE_TAG, tagTxt);

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
			QueryManager queryManager = getSession().getWorkspace()
					.getQueryManager();

			// XPath
			StringBuilder builder = new StringBuilder();
			builder.append("//element(*, ").append(getEntityType()).append(")");
			String attrQuery = XPathUtils
					.localAnd(XPathUtils.getFreeTextConstraint(getFilterText()
							.getText()), XPathUtils.getPropertyEquals(
							PEOPLE_TAGS, tagDD.getText()));
			if (EclipseUiUtils.notEmpty(attrQuery))
				builder.append("[").append(attrQuery).append("]");
			builder.append(" order by @").append(PeopleNames.JCR_TITLE)
					.append(" ascending");
			Query query = queryManager.createQuery(builder.toString(),
					PeopleConstants.QUERY_XPATH);

			// boolean showAll = showAllResultsBtn != null
			// && !(showAllResultsBtn.isDisposed())
			// && showAllResultsBtn.getSelection();
			//
			// if (!showAll)
			// xpathQuery.setLimit(MsmUiConstants.SEARCH_DEFAULT_LIMIT);
			//
			// RowIterator xPathRit = xpathQuery.execute().getRows();
			// if (log.isDebugEnabled()) {
			// long end = System.currentTimeMillis();
			// log.debug("Found: " + xPathRit.getSize() + " persons in "
			// + (end - begin) + " ms by executing XPath query ("
			// + xpathQueryStr + ").");
			// }
			//
			//
			// QueryObjectModelFactory factory = queryManager.getQOMFactory();
			// Selector source = factory
			// .selector(getEntityType(), getEntityType());
			//
			// Constraint defaultC = getFreeTextConstraint(factory, source);
			//
			// // Tag
			// String currVal = tagDD.getText();
			// if (JcrUiUtils.checkNotEmptyString(currVal)) {
			// StaticOperand so = factory.literal(getSession()
			// .getValueFactory().createValue(currVal));
			// DynamicOperand dyo = factory.propertyValue(
			// source.getSelectorName(), );
			// Constraint currC = factory.comparison(dyo,
			// QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO, so);
			// defaultC = JcrUiUtils.localAnd(factory, defaultC, currC);
			// }
			//
			// // TODO handle the case where no TITLE prop is available
			// Ordering order = factory.ascending(factory.propertyValue(
			// source.getSelectorName(), Property.JCR_TITLE));
			// Ordering[] orderings = { order };
			// QueryObjectModel query = factory.createQuery(source, defaultC,
			// orderings, null);
			QueryResult result = query.execute();
			Row[] rows = JcrUiUtils.rowIteratorToArray(result.getRows());
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