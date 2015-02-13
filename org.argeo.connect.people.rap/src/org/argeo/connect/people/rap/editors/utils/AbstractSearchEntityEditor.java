package org.argeo.connect.people.rap.editors.utils;

import java.util.List;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Row;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;
import javax.jcr.query.qom.StaticOperand;

import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.rap.PeopleRapConstants;
import org.argeo.connect.people.rap.PeopleRapUtils;
import org.argeo.connect.people.rap.PeopleWorkbenchService;
import org.argeo.connect.people.rap.composites.DateText;
import org.argeo.connect.people.rap.composites.VirtualRowTableViewer;
import org.argeo.connect.people.rap.exports.PeopleColumnDefinition;
import org.argeo.connect.people.rap.listeners.PeopleJcrViewerDClickListener;
import org.argeo.connect.people.rap.utils.Refreshable;
import org.argeo.connect.people.rap.wizards.TagOrUntagInstancesWizard;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.eclipse.ui.jcr.lists.ColumnDefinition;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
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
		PeopleNames, Refreshable {

	/* DEPENDENCY INJECTION */
	private Session session;
	private PeopleWorkbenchService peopleWorkbenchService;
	private PeopleService peopleService;

	// Business Objects
	// private String entityType;

	// This page widgets
	private VirtualRowTableViewer tableCmp;
	// private TableViewer tableViewer;
	private Text filterTxt;

	// Locally cache what is displayed in the UI. Enable exports among others.
	private Row[] rows;
	private String filterString;

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
		parent.setLayout(PeopleUiUtils.noSpaceGridLayout());

		// the generic free search part
		Composite searchCmp = new Composite(parent, SWT.NO_FOCUS);
		populateSearchPanel(searchCmp);
		searchCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		// section with static filters
		if (showStaticFilterSection()) {
			Composite filterSection = new Composite(parent, SWT.NO_FOCUS);
			populateStaticFilters(filterSection);
			filterSection.setLayoutData(PeopleUiUtils.horizontalFillData());
		}

		// A menu with various actions on selected items
		if (hasCheckBoxes()) {
			Composite menuCmp = new Composite(parent, SWT.NO_FOCUS);
			createCheckBoxMenu(menuCmp);
			menuCmp.setLayoutData(PeopleUiUtils.horizontalFillData());
		}

		// The table itself
		Composite tableCmp = new Composite(parent, SWT.NO_FOCUS);
		createListPart(tableCmp);
		tableCmp.setLayoutData(PeopleUiUtils.fillGridData());

		// initialize table
		refreshFilteredList();
	}

	protected abstract void refreshFilteredList();

	/**
	 * Overwrite to false if implementation has no static filter session.
	 * Warning: the refreshStaticFilteredList() must still be implemented
	 */
	protected boolean showStaticFilterSection() {
		return true;
	}

	/** Override this to provide type specific static filters */
	protected void populateStaticFilters(Composite body) {
	}

	/**
	 * Overwrite to true to use a CheckBoxTableViewer and provide selection
	 * abilities.
	 */
	protected boolean hasCheckBoxes() {
		return false;
	}

	protected void createCheckBoxMenu(Composite parent) {
		RowLayout layout = new RowLayout(SWT.HORIZONTAL);
		layout.pack = false;
		parent.setLayout(layout);

		Button selectAllBtn = new Button(parent, SWT.PUSH);
		selectAllBtn.setText("Select all");
		selectAllBtn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				tableCmp.setAllChecked(true);
			}
		});

		Button unselectAllBtn = new Button(parent, SWT.PUSH);
		unselectAllBtn.setText("Unselect all");
		unselectAllBtn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				tableCmp.setAllChecked(false);
			}
		});

		final Button addTagBtn = new Button(parent, SWT.PUSH);
		addTagBtn.setText("Add tag");
		addBtnListener(addTagBtn, PeopleConstants.RESOURCE_TAG,
				PeopleNames.PEOPLE_TAGS, TagOrUntagInstancesWizard.TYPE_ADD);

		final Button removeTagBtn = new Button(parent, SWT.PUSH);
		removeTagBtn.setText("Remove tag");
		addBtnListener(removeTagBtn, PeopleConstants.RESOURCE_TAG,
				PeopleNames.PEOPLE_TAGS, TagOrUntagInstancesWizard.TYPE_REMOVE);

		if (isOrgOrPerson()) {
			final Button addMLBtn = new Button(parent, SWT.PUSH);
			addMLBtn.setText("Add mailing list");
			addBtnListener(addMLBtn, PeopleTypes.PEOPLE_MAILING_LIST,
					PeopleNames.PEOPLE_MAILING_LISTS,
					TagOrUntagInstancesWizard.TYPE_ADD);

			final Button removeMLBtn = new Button(parent, SWT.PUSH);
			removeMLBtn.setText("Remove mailing list");
			addBtnListener(removeMLBtn, PeopleTypes.PEOPLE_MAILING_LIST,
					PeopleNames.PEOPLE_MAILING_LISTS,
					TagOrUntagInstancesWizard.TYPE_REMOVE);
		}
	}

	private boolean isOrgOrPerson() {
		ColumnDefinition colDef = getColumnDefinition(null).get(0);
		if (PeopleTypes.PEOPLE_PERSON.equals(colDef.getSelectorName())
				|| PeopleTypes.PEOPLE_ORG.equals(colDef.getSelectorName()))
			return true;
		else
			return false;
	}

	private void addBtnListener(final Button button, final String tagId,
			final String taggablePropName, final int actionType) {
		button.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 2236384303910015747L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				Row[] rows = tableCmp.getSelectedElements();
				Shell shell = button.getShell();

				if (rows.length == 0)
					MessageDialog.openInformation(shell, "Unvalid selection",
							"No item is selected. Nothing has been done.");
				else {
					Wizard wizard = new TagOrUntagInstancesWizard(button
							.getDisplay(), actionType, session, peopleService,
							peopleWorkbenchService, rows, getEntityType(),
							tagId, taggablePropName);
					WizardDialog dialog = new WizardDialog(shell, wizard);
					int result = dialog.open();
					if (result == WizardDialog.OK) {
						refreshFilteredList();
					}
				}
			}
		});
	}

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
		getFilterText().setText("");
	}

	/**
	 * Returns an array with the rows that where retrieved by the last search
	 * (or all if the filter has been reset in the mean while). For the time
	 * being, returned rows are still *not* linked to the export ID
	 */
	public Row[] getRows(String exportId) {
		return rows;
	}

	/** Generates a pseudo query String that defines the last filter applied */
	public String getFilterAsString() {
		return filterString;
	}

	protected void createListPart(Composite parent) {
		parent.setLayout(new GridLayout());
		tableCmp = new VirtualRowTableViewer(parent, SWT.MULTI,
				getColumnDefinition(null), hasCheckBoxes());
		TableViewer tableViewer = tableCmp.getTableViewer();
		tableCmp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tableViewer.addDoubleClickListener(new PeopleJcrViewerDClickListener(
				getEntityType(), peopleWorkbenchService));
	}

	/** Refresh the table viewer based on the free text search field */
	protected void populateSearchPanel(Composite parent) {
		parent.setLayout(new GridLayout());
		filterTxt = new Text(parent, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH
				| SWT.ICON_CANCEL);
		filterTxt.setMessage(PeopleRapConstants.FILTER_HELP_MSG);
		filterTxt.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_FILL));
		filterTxt.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 5003010530960334977L;

			public void modifyText(ModifyEvent event) {
				refreshFilteredList();
			}
		});
	}

	/** Overwrite to customise the filtering widgets */
	protected Text getFilterText() {
		return filterTxt;
	}

	/** Use this method to update the result table */
	protected void setViewerInput(Row[] rows) {
		this.rows = rows;
		TableViewer tableViewer = tableCmp.getTableViewer();

		// CheckboxTableViewer ctv = null;
		// if (tableViewer instanceof CheckboxTableViewer) {
		// ctv = (CheckboxTableViewer) tableViewer;
		// ctv.setAllChecked(false);
		// }

		tableViewer.setInput(rows);
		// we must explicitly set the items count
		tableViewer.setItemCount(rows.length);
		tableViewer.refresh();
	}

	/** Use this method to string representing current applied filter */
	protected void setFilterString(String filterString) {
		this.filterString = filterString;
	}

	protected Constraint getFreeTextConstraint(QueryObjectModelFactory factory,
			Selector source) throws RepositoryException {
		String filter = getFilterText().getText();
		Constraint defaultC = null;
		if (CommonsJcrUtils.checkNotEmptyString(filter)) {
			String[] strs = filter.trim().split(" ");
			for (String token : strs) {
				StaticOperand so = factory.literal(session.getValueFactory()
						.createValue("*" + token + "*"));
				Constraint currC = factory.fullTextSearch(
						source.getSelectorName(), null, so);
				defaultC = CommonsJcrUtils.localAnd(factory, defaultC, currC);
			}
		}
		return defaultC;
	}

	// Life cycle management
	@Override
	public void setFocus() {
		getFilterText().forceFocus();
	}

	@Override
	public void dispose() {
		JcrUtils.logoutQuietly(session);
		super.dispose();
	}

	@Override
	public void forceRefresh(Object object) {
		refreshFilteredList();
	}

	// /////////////////////////
	// Expose to children classes
	protected Session getSession() {
		return session;
	}
	
	protected VirtualRowTableViewer getTableViewer(){
		return tableCmp;
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

	protected PeopleWorkbenchService getPeopleUiService() {
		return peopleWorkbenchService;
	}

	// protected TableViewer getTableViewer() {
	// return tableViewer;
	// }

	// Local Methods
	protected Text createBoldLT(Composite parent, String title, String message,
			String tooltip) {
		return createBoldLT(parent, title, message, tooltip, 1);
	}

	protected Text createBoldLT(Composite parent, String title, String message,
			String tooltip, int colspan) {
		PeopleRapUtils.createBoldLabel(parent, title);
		Text text = new Text(parent, SWT.BOTTOM | SWT.BORDER);
		text.setLayoutData(PeopleUiUtils.fillGridData(colspan));
		text.setMessage(message);
		text.setToolTipText(tooltip);
		return text;
	}

	// protected DateTime createLDT(Composite parent, String title, String
	// tooltip) {
	// Label label = new Label(parent, SWT.RIGHT);
	// label.setText(title);
	// label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
	// DateTime dateTime = new DateTime(parent, SWT.RIGHT | SWT.DATE
	// | SWT.MEDIUM | SWT.DROP_DOWN);
	// dateTime.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
	// dateTime.setToolTipText(tooltip);
	// return dateTime;
	// }

	protected DateText createLD(Composite parent, String title, String tooltip) {
		// Label label = new Label(parent, SWT.RIGHT);
		// label.setText(title);
		// label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
		// false));
		DateText dateText = new DateText(parent, SWT.NO_FOCUS);
		dateText.setToolTipText(tooltip);
		dateText.setMessage(title);
		return dateText;
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

	/* DEPENDENCY INJECTION */
	public void setRepository(Repository repository) {
		session = CommonsJcrUtils.login(repository);
	}

	public void setPeopleService(PeopleService peopleService) {
		this.peopleService = peopleService;
	}

	public void setPeopleWorkbenchService(
			PeopleWorkbenchService peopleWorkbenchService) {
		this.peopleWorkbenchService = peopleWorkbenchService;
	}
}