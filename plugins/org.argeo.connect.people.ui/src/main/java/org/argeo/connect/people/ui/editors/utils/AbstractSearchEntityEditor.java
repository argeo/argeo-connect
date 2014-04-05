package org.argeo.connect.people.ui.editors.utils;

import java.util.List;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Row;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;
import javax.jcr.query.qom.StaticOperand;

import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiService;
import org.argeo.connect.people.ui.composites.PeopleVirtualTableViewer;
import org.argeo.connect.people.ui.extracts.PeopleColumnDefinition;
import org.argeo.connect.people.ui.listeners.PeopleJcrViewerDClickListener;
import org.argeo.connect.people.ui.utils.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

/**
 * Search the repository with a given entity type at base path. Expect a {@Code
 *  SearchNodeEditorInput} editor input.
 */
public abstract class AbstractSearchEntityEditor extends EditorPart implements
		PeopleNames {

	/* DEPENDENCY INJECTION */
	private Session session;
	private PeopleUiService peopleUiService;
	private PeopleService peopleService;

	// Business Objects
	// private String entityType;

	// This page widgets
	private TableViewer tableViewer;
	private Text filterTxt;

	// Locally cache what is displayed in the UI. Enable exports among others.
	private Row[] rows;

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
		String label = ((SearchNodeEditorInput) getEditorInput()).getName();
		setPartName(label);
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(PeopleUiUtils.gridLayoutNoBorder());

		// the generic free search part
		Composite searchCmp = new Composite(parent, SWT.NO_FOCUS);
		populateSearchPanel(searchCmp);
		searchCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		// section with static filters
		if (showStaticFilterSection()) {
			Composite filterSection = new Composite(parent, SWT.NO_FOCUS);
			populateStaticFilters(filterSection);
			filterSection.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true,
					false));
		}

		// The table itself
		Composite tableCmp = new Composite(parent, SWT.NO_FOCUS);
		createListPart(tableCmp);
		tableCmp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// initialize table
		refreshStaticFilteredList();
	}

	/**
	 * Overwrite to false if implementation has no static filter session.
	 * Warning: the refreshStaticFilteredList() must still be implemented
	 */
	protected boolean showStaticFilterSection() {
		return true;
	}

	/** Override this to provide type specific static filters */
	protected abstract void populateStaticFilters(Composite body);

	protected abstract void refreshStaticFilteredList();

	/** Overwrite to set the correct row height */
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

	/**
	 * Overwrite to provide corresponding column definitions. Also used for
	 * exports generation
	 */
	public abstract List<PeopleColumnDefinition> getColumnDefinition(
			String extractId);

	/**
	 * Call this when resetting static filters if you also want to reset the
	 * free text search field
	 */
	protected void resetFilterText() {
		filterTxt.setText("");
	}

	public Row[] getRows(String extractId) {
		return rows;
	}

	protected void createListPart(Composite parent) {
		parent.setLayout(new GridLayout());
		PeopleVirtualTableViewer tableCmp = new PeopleVirtualTableViewer(
				parent, SWT.MULTI, getColumnDefinition(null));
		tableViewer = tableCmp.getTableViewer();
		tableCmp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tableViewer.addDoubleClickListener(new PeopleJcrViewerDClickListener(
				getEntityType(), peopleUiService));
	}

	/** Refresh the table viewer based on the free text search field */
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
				refreshStaticFilteredList();
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

	protected Constraint getFreeTextConstraint(QueryObjectModelFactory factory,
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
				defaultC = localAnd(factory, defaultC, currC);
			}
		}
		return defaultC;
	}

	// Life cycle management
	@Override
	public void setFocus() {
		filterTxt.forceFocus();
	}

	@Override
	public void dispose() {
		JcrUtils.logoutQuietly(session);
		super.dispose();
	}

	// ///////////////
	// LOCAL CLASSES

	// protected abstract class MyDropDown {
	//
	// private final Text text;
	// private final DropDown dropDown;
	//
	// private boolean modifyFromList = false;
	//
	// private String[] values;
	//
	// public String getText() {
	// return text.getText();
	// }
	//
	// public void reset(String value) {
	// // Workaround the dropDown Show pb when resetting the text
	// modifyFromList = true;
	// if (CommonsJcrUtils.checkNotEmptyString(value))
	// text.setText(value);
	// else
	// text.setText("");
	// modifyFromList = false;
	//
	// }
	//
	// /** Overwrite to provide specific filtering */
	// protected abstract List<String> getFilteredValues(String filter);
	//
	// protected void refreshValues() {
	// List<String> filteredValues = getFilteredValues(text.getText());
	// values = filteredValues.toArray(new String[filteredValues.size()]);
	// dropDown.setItems(values);
	// dropDown.show();
	// }
	//
	// public MyDropDown(Text text) {
	// dropDown = new DropDown(text);
	// this.text = text;
	// addListeners();
	// refreshValues();
	// }
	//
	// private void addListeners() {
	// text.addFocusListener(new FocusListener() {
	// private static final long serialVersionUID = 1L;
	//
	// // TODO clean this, it is no the best way to force display of
	// // the list when the text is empty, and has some weird side
	// // effects.
	// @Override
	// public void focusLost(FocusEvent event) {
	// }
	//
	// @Override
	// public void focusGained(FocusEvent event) {
	// // Force show on focus in
	// dropDown.show();
	// }
	// });
	//
	// text.addModifyListener(new ModifyListener() {
	// private static final long serialVersionUID = 1L;
	//
	// @Override
	// public void modifyText(ModifyEvent event) {
	// // Avoid reducing suggestion while browsing them
	// if (!modifyFromList) {
	// refreshValues();
	// }
	// }
	// });
	//
	// dropDown.addListener(SWT.Selection, new DDSelectionListener());
	// dropDown.addListener(SWT.DefaultSelection,
	// new DDSelectionListener());
	// }
	//
	// private class DDSelectionListener implements Listener {
	// private static final long serialVersionUID = 1L;
	//
	// @Override
	// public void handleEvent(Event event) {
	// modifyFromList = true;
	// int index = dropDown.getSelectionIndex();
	// if (index != -1 && index < values.length)
	// text.setText(values[index]);
	// modifyFromList = false;
	// if (event.type == SWT.DefaultSelection)
	// KeyboardFocusManager.getCurrentKeyboardFocusManager()
	// .focusNextComponent();
	// }
	// }
	// }

	// /////////////////////////
	// Expose to children classes
	protected Session getSession() {
		return session;
	}

	protected String getEntityType() {
		return ((SearchNodeEditorInput) getEditorInput()).getNodeType();
	}

	protected String getBasePath() {
		return ((SearchNodeEditorInput) getEditorInput()).getBasePath();
	}

	protected PeopleService getPeopleService() {
		return peopleService;
	}

	protected PeopleUiService getPeopleUiService() {
		return peopleUiService;
	}

	// ////////////
	// Helpers
	protected Constraint localAnd(QueryObjectModelFactory factory,
			Constraint defaultC, Constraint newC) throws RepositoryException {
		if (defaultC == null)
			return newC;
		else
			return factory.and(defaultC, newC);
	}

	protected Text createBoldLT(Composite parent, String title, String message,
			String tooltip) {
		return createBoldLT(parent, title, message, tooltip, 1);
	}

	protected Text createBoldLT(Composite parent, String title, String message,
			String tooltip, int colspan) {
		createBoldLabel(parent, title);
		Text text = new Text(parent, SWT.BOTTOM | SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				colspan, 1));
		text.setMessage(message);
		text.setToolTipText(tooltip);
		return text;
	}

	protected DateTime createLDT(Composite parent, String title, String tooltip) {
		Label label = new Label(parent, SWT.RIGHT);
		label.setText(title);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		DateTime dateTime = new DateTime(parent, SWT.RIGHT | SWT.DATE
				| SWT.MEDIUM | SWT.DROP_DOWN);
		dateTime.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		dateTime.setToolTipText(tooltip);
		return dateTime;
	}

	protected Label createBoldLabel(Composite parent, String title) {
		Label label = new Label(parent, SWT.RIGHT);
		label.setText(title);
		label.setFont(EclipseUiUtils.getBoldFont(parent));
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		return label;
	}

	/* DEPENDENCY INJECTION */
	public void setRepository(Repository repository) {
		session = CommonsJcrUtils.login(repository);
	}

	public void setPeopleUiService(PeopleUiService peopleUiService) {
		this.peopleUiService = peopleUiService;
	}

	public void setPeopleService(PeopleService peopleService) {
		this.peopleService = peopleService;
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