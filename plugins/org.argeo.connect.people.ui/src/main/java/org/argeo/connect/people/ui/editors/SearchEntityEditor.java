package org.argeo.connect.people.ui.editors;

import java.awt.KeyboardFocusManager;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.DynamicOperand;
import javax.jcr.query.qom.Ordering;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelConstants;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;
import javax.jcr.query.qom.StaticOperand;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.PeopleUiServiceImpl;
import org.argeo.connect.people.ui.composites.PeopleVirtualTableViewer;
import org.argeo.connect.people.ui.editors.utils.SearchEntityEditorInput;
import org.argeo.connect.people.ui.extracts.ITableProvider;
import org.argeo.connect.people.ui.extracts.PeopleColumnDefinition;
import org.argeo.connect.people.ui.listeners.PeopleJcrViewerDClickListener;
import org.argeo.connect.people.ui.utils.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.eclipse.ui.jcr.lists.ColumnDefinition;
import org.argeo.eclipse.ui.jcr.lists.SimpleJcrRowLabelProvider;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.rap.addons.dropdown.DropDown;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.EditorPart;

/**
 * Search the repository with a given entity type
 */
public class SearchEntityEditor extends EditorPart implements PeopleNames,
		ITableProvider {

	public final static String ID = PeopleUiPlugin.PLUGIN_ID
			+ ".searchEntityEditor";

	/* DEPENDENCY INJECTION */
	private Session session;
	private PeopleUiServiceImpl peopleUiService;

	// Business Objects
	private String entityType;

	// This page widgets
	private TableViewer tableViewer;
	private Text filterTxt;

	// Locally cache what is displayed in the UI. Enable among other the report
	// mechanism management
	private Row[] rows;

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
		setSite(site);
		setInput(input);
		SearchEntityEditorInput sei = (SearchEntityEditorInput) getEditorInput();
		entityType = sei.getName();
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(PeopleUiUtils.gridLayoutNoBorder());

		// the generic free search part
		Composite searchCmp = new Composite(parent, SWT.NO_FOCUS);
		populateSearchPanel(searchCmp);
		searchCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		// section with static filters
		Composite filterSection = new Composite(parent, SWT.NO_FOCUS);
		populateStaticFilters(filterSection);
		filterSection
				.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		// The table itself
		Composite tableCmp = new Composite(parent, SWT.NO_FOCUS);
		createListPart(tableCmp);
		tableCmp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// initialize table
		refreshFilteredList();
	}

	private Text tagTxt;
	private Text tagOutTxt;

	/** Override this to provide type specific staic filters */
	protected void populateStaticFilters(Composite parent) {
		parent.setLayout(PeopleUiUtils.gridLayoutNoBorder());

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

		tagTxt = createLT(body, "Tag", "",
				"Select from list to find entities that are categorised with this tag");
		new TagDropDown(tagTxt);

		tagOutTxt = createLT(body, "Omit Tag", "",
				"Select from list to find persons that are NOT categorised with this tag");
		new TagDropDown(tagOutTxt);

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
				tagTxt.setText("");
				tagOutTxt.setText("");
				// WARNING to reset last a text with a drop down
				filterTxt.setText("");
			}
		});
	}

	/** Refresh the table viewer based on the free text search field */
	protected void refreshStaticFilteredList() {
		try {
			QueryManager queryManager = session.getWorkspace()
					.getQueryManager();
			QueryObjectModelFactory factory = queryManager.getQOMFactory();
			Selector source = factory.selector(entityType, entityType);

			Constraint defaultC = getFreeTextContraint(factory, source);

			// Tag
			String currVal = tagTxt.getText();
			if (CommonsJcrUtils.checkNotEmptyString(currVal)) {
				StaticOperand so = factory.literal(session.getValueFactory()
						.createValue(currVal));
				DynamicOperand dyo = factory.propertyValue(
						source.getSelectorName(), PEOPLE_TAGS);
				Constraint currC = factory.comparison(dyo,
						QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO, so);
				defaultC = localAnd(factory, defaultC, currC);
			}

			// Omit Tag
			currVal = tagOutTxt.getText();
			if (CommonsJcrUtils.checkNotEmptyString(currVal)) {
				StaticOperand so = factory.literal(session.getValueFactory()
						.createValue(currVal));
				DynamicOperand dyo = factory.propertyValue(
						source.getSelectorName(), PEOPLE_TAGS);
				Constraint currC = factory.comparison(dyo,
						QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO, so);
				currC = factory.not(currC);
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
			throw new PeopleException("Unable to list " + entityType
					+ " entities with static filter ", e);
		}
	}

	protected Constraint localAnd(QueryObjectModelFactory factory,
			Constraint defaultC, Constraint newC) throws RepositoryException {
		if (defaultC == null)
			return newC;
		else
			return factory.and(defaultC, newC);
	}

	protected Text createLT(Composite parent, String title, String message,
			String tooltip) {
		Label label = new Label(parent, SWT.RIGHT);
		label.setText(title);
		label.setFont(EclipseUiUtils.getBoldFont(parent));
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		Text text = new Text(parent, SWT.BOTTOM | SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		text.setMessage(message);
		text.setToolTipText(tooltip);
		return text;
	}

	protected class TagDropDown extends MyDropDown {

		public TagDropDown(Text text) {
			super(text);
		}

		@Override
		protected List<String> getFilteredValues(String filter) {
			return peopleUiService.getDefinedFilteredTags(session, getText());
		}
	}

	protected void createListPart(Composite parent) {
		parent.setLayout(new GridLayout());
		PeopleVirtualTableViewer tableCmp = new PeopleVirtualTableViewer(
				parent, SWT.MULTI, colDefs);
		tableViewer = tableCmp.getTableViewer();
		tableCmp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tableViewer.addDoubleClickListener(new PeopleJcrViewerDClickListener(
				entityType, peopleUiService));
	}

	protected void populateSearchPanel(Composite parent) {
		parent.setLayout(new GridLayout());
		filterTxt = new Text(parent, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH
				| SWT.ICON_CANCEL);
		filterTxt.setMessage(PeopleUiConstants.FILTER_HELP_MSG);
		filterTxt.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_FILL));
		filterTxt.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 5003010530960334977L;

			public void modifyText(ModifyEvent event) {
				refreshFilteredList();
			}
		});
	}

	/** Use this method to update the result table */
	protected void setViewerInput(Row[] rows) {
		this.rows = rows;
		tableViewer.setInput(rows);
		// we must explicitly set the items count
		tableViewer.setItemCount(rows.length);
		tableViewer.refresh();
	}

	protected Constraint getFreeTextContraint(QueryObjectModelFactory factory,
			Selector source) throws RepositoryException {
		String filter = filterTxt.getText();
		Constraint defaultC = null;
		if (CommonsJcrUtils.checkNotEmptyString(filter)) {
			String[] strs = filter.trim().split(" ");
			for (String token : strs) {
				StaticOperand so = factory.literal(session.getValueFactory()
						.createValue("*" + token + "*"));
				Constraint currC = factory.fullTextSearch(
						source.getSelectorName(), null, so);
				if (defaultC == null)
					defaultC = currC;
				else
					defaultC = factory.and(defaultC, currC);
			}
		}
		return defaultC;
	}

	/** Refresh the table viewer only based on the free text search field */
	protected void refreshFilteredList() {
		try {
			QueryManager queryManager = session.getWorkspace()
					.getQueryManager();
			QueryObjectModelFactory factory = queryManager.getQOMFactory();
			Selector source = factory.selector(entityType, entityType);
			Constraint defaultC = getFreeTextContraint(factory, source);

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
			throw new PeopleException("Unable to list entities with filter ", e);
		}
	}

	/**
	 * Overwrite to set the correct row height
	 * 
	 */
	protected int getCurrRowHeight() {
		return 20;
	}

	/**
	 * Overwrite to false if the table should not be automatically refreshed on
	 * startup, see for instance {@code SearchByTagEditor} to have a relevant
	 * example
	 */
	protected boolean queryOnCreation() {
		return true;
	}

	// Life cycle management
	@Override
	public void setFocus() {
		//filterTxt.forceFocus();
	}

	@Override
	public void dispose() {
		JcrUtils.logoutQuietly(session);
		super.dispose();
	}

	// ///////////////
	// LOCAL CLASSES

	protected abstract class MyDropDown {

		private final Text text;
		private final DropDown dropDown;

		private boolean modifyFromList = false;

		private String[] values;

		protected String getText() {
			return text.getText();
		}

		/** Overwrite to provide specific filtering */
		protected abstract List<String> getFilteredValues(String filter);

		protected void refreshValues() {
			List<String> filteredValues = getFilteredValues(text.getText());
			values = filteredValues.toArray(new String[filteredValues.size()]);
			dropDown.setItems(values);
			dropDown.show();
		}

		public MyDropDown(Text text) {
			dropDown = new DropDown(text);
			this.text = text;
			addListeners();
			refreshValues();
		}

		private void addListeners() {
			text.addFocusListener(new FocusListener() {

				@Override
				public void focusLost(FocusEvent event) {
				}

				@Override
				public void focusGained(FocusEvent event) {
					// Force show on focus in
					//dropDown.show();
				}
			});

			text.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent event) {
					// Avoid reducing suggestion while browsing them
					if (!modifyFromList) {
						refreshValues();
					}
				}
			});

			dropDown.addListener(SWT.Selection, new DDSelectionListener());
			dropDown.addListener(SWT.DefaultSelection,
					new DDSelectionListener());
		}

		private class DDSelectionListener implements Listener {
			@Override
			public void handleEvent(Event event) {
				modifyFromList = true;
				int index = dropDown.getSelectionIndex();
				if (index != -1 && index < values.length)
					text.setText(values[index]);
				modifyFromList = false;
				if (event.type == SWT.DefaultSelection)
					KeyboardFocusManager.getCurrentKeyboardFocusManager()
							.focusNextComponent();
			}
		}
	}

	// /////////////////////////
	// Expose to children classes
	protected Session getSession() {
		return session;
	}

	protected String getEntityType() {
		return entityType;
	}

	@Override
	public RowIterator getRowIterator(String extractId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ColumnDefinition> getColumnDefinition(String extractId) {
		// TODO Auto-generated method stub
		return null;
	}

	// ////////////
	// Helpers
	protected Row[] rowIteratorToArray(RowIterator rit) {
		List<Row> rows = new ArrayList<Row>();
		while (rit.hasNext()) {
			rows.add(rit.nextRow());
		}
		return rows.toArray(new Row[rows.size()]);
	}

	/* DEPENDENCY INJECTION */
	public void setRepository(Repository repository) {
		session = CommonsJcrUtils.login(repository);
	}

	public void setPeopleUiService(PeopleUiServiceImpl peopleUiService) {
		this.peopleUiService = peopleUiService;
	}

	// Compulsory unused methods
	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}
}