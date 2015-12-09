package org.argeo.connect.people.rap.editors.utils;

import java.util.List;

import javax.jcr.Repository;
import javax.jcr.Session;

import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.rap.PeopleRapConstants;
import org.argeo.connect.people.rap.PeopleRapImages;
import org.argeo.connect.people.rap.PeopleRapUtils;
import org.argeo.connect.people.rap.PeopleWorkbenchService;
import org.argeo.connect.people.rap.composites.DateText;
import org.argeo.connect.people.rap.composites.VirtualJcrTableViewer;
import org.argeo.connect.people.rap.listeners.PeopleJcrViewerDClickListener;
import org.argeo.connect.people.rap.utils.Refreshable;
import org.argeo.connect.people.rap.wizards.TagOrUntagInstancesWizard;
import org.argeo.connect.people.ui.PeopleColumnDefinition;
import org.argeo.connect.people.utils.JcrUiUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
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
	private Repository repository;
	private Session session;
	private PeopleWorkbenchService peopleWorkbenchService;
	private PeopleService peopleService;

	// This page widgets
	private VirtualJcrTableViewer tableCmp;
	private Text filterTxt;
	private TraverseListener traverseListener;

	// Locally cache what is displayed in the UI. Enable exports among others.
	private Object[] elements;
	private String filterString;

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
		String label = ((SearchNodeEditorInput) getEditorInput()).getName();
		setPartName(label);
		session = JcrUiUtils.login(repository);
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());

		Composite searchCmp = new Composite(parent, SWT.NO_FOCUS);
		searchCmp.setLayoutData(EclipseUiUtils.fillWidth());
		if (!showStaticFilterSection()) {
			searchCmp.setLayout(new GridLayout());
			Composite filterCmp = new Composite(searchCmp, SWT.NO_FOCUS);
			filterCmp.setLayoutData(EclipseUiUtils.fillWidth());
			populateSearchPanel(filterCmp);
		}
		// a simple generic free search part
		else
			// Add a section with static filters
			populateStaticSearchPanel(searchCmp);

		// A menu with various actions on selected elements
		if (hasCheckBoxes()) {
			Composite menuCmp = new Composite(parent, SWT.NO_FOCUS);
			createCheckBoxMenu(menuCmp);
			menuCmp.setLayoutData(EclipseUiUtils.fillWidth());
		}

		// The table itself
		Composite tableCmp = new Composite(parent, SWT.NO_FOCUS);
		createListPart(tableCmp);
		tableCmp.setLayoutData(EclipseUiUtils.fillAll());

		// initialize table
		// refreshFilteredList();
	}

	protected abstract void refreshFilteredList();

	/**
	 * Overwrite to false if implementation has no static filter session.
	 * Warning: the refreshStaticFilteredList() must still be implemented
	 */
	protected boolean showStaticFilterSection() {
		return true;
	}

	/** Override to provide type specific static filters */
	protected void populateStaticFilters(Composite body) {
	}

	/** Call when a place holder for this info exists */
	protected void setNbOfFoundResultsLbl(Label resultNbLbl) {
		if (elements == null)
			resultNbLbl.setText(" (No result yet) ");
		else if (elements.length == 0)
			resultNbLbl.setText(" (No result found) ");
		else if (elements.length == 1)
			resultNbLbl.setText(" One result found ");
		else
			resultNbLbl.setText(elements.length + " results found ");
		resultNbLbl.getParent().layout(true, true);
	}

	protected void setNbOfFoundResultsLbl(long nbOfResults) {
		Label label = getResultLengthLbl();
		if (label != null && !label.isDisposed())
			setNbOfFoundResultsLbl(label);
	}

	/** Override this to provide a label that displays the NB of found results */
	protected Label getResultLengthLbl() {
		return null;
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
		// layout.pack = false;
		parent.setLayout(layout);

		final Button selectAllBtn = new Button(parent, SWT.PUSH);
		selectAllBtn.setToolTipText("Select all");
		selectAllBtn.setImage(PeopleRapImages.CHECK_UNSELECTED);
		RowData rd = new RowData(40, SWT.DEFAULT);
		selectAllBtn.setLayoutData(rd);

		selectAllBtn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (tableCmp.getSelectedElements().length == 0) {
					tableCmp.setAllChecked(true);
					selectAllBtn.setImage(PeopleRapImages.CHECK_SELECTED);
					selectAllBtn
							.setToolTipText("Unselect all (At least one element is already selected)");
				} else {
					tableCmp.setAllChecked(false);
					selectAllBtn.setImage(PeopleRapImages.CHECK_UNSELECTED);
					selectAllBtn.setToolTipText("Select all");
				}
			}
		});

		// // an empty cell to give some air to the layout
		// Label emptyLbl = new Label(parent, SWT.NONE);
		// emptyLbl.setText("");
		// rd = new RowData(60, SWT.DEFAULT);
		// emptyLbl.setLayoutData(rd);

		Button addTagBtn = new Button(parent, SWT.TOGGLE);
		addTagBtn.setText("Tag");
		addTagBtn.setImage(PeopleRapImages.ICON_TAG);
		addToggleStateTagBtnListener(addTagBtn, PeopleConstants.RESOURCE_TAG,
				PeopleNames.PEOPLE_TAGS);

		if (isOrgOrPerson()) {
			Button addMLBtn = new Button(parent, SWT.TOGGLE);
			addMLBtn.setText(" Mailing List");
			addMLBtn.setImage(PeopleRapImages.ICON_MAILING_LIST);
			addToggleStateTagBtnListener(addMLBtn,
					PeopleTypes.PEOPLE_MAILING_LIST,
					PeopleNames.PEOPLE_MAILING_LISTS);
		}
	}

	private void addToggleStateTagBtnListener(final Button addTagBtn,
			final String tagId, final String taggablePropName) {

		addTagBtn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 9066664395274942610L;
			private DropDownPopup dropDown;

			@Override
			public void widgetSelected(SelectionEvent e) {

				boolean toggled = addTagBtn.getSelection();
				if (toggled) {
					if (dropDown != null && !dropDown.isDisposed())
						// should never happen
						dropDown.setVisible(true);
					else {

						dropDown = new DropDownPopup(addTagBtn, tagId,
								taggablePropName);
						dropDown.open();
					}
				} else {
					if (dropDown != null && !dropDown.isDisposed())
						dropDown.dispose();
				}
			}
		});
	}

	private class DropDownPopup extends Shell {
		private static final long serialVersionUID = 1L;
		private Button toggleBtn;
		private String tagId;
		private String taggablePropName;

		public DropDownPopup(final Control source, String tagId,
				String taggablePropName) {
			super(source.getDisplay(), SWT.NO_TRIM | SWT.BORDER | SWT.ON_TOP);

			toggleBtn = (Button) source;
			this.tagId = tagId;
			this.taggablePropName = taggablePropName;

			populate();
			// Add border and shadow style
			CmsUtils.style(DropDownPopup.this,
					PeopleRapConstants.PEOPLE_CLASS_POPUP_SHELL);
			pack();
			layout();

			// position the popup
			Rectangle parPosition = source.getBounds();
			Point absolute = source.toDisplay(source.getSize().x,
					source.getSize().y);
			absolute.x = absolute.x - parPosition.width;
			absolute.y = absolute.y + 2;
			setLocation(absolute);

			addShellListener(new ShellAdapter() {
				private static final long serialVersionUID = 5178980294808435833L;

				@Override
				public void shellDeactivated(ShellEvent e) {
					close();
					dispose();
					if (!source.isDisposed()) {
						boolean toggled = toggleBtn.getSelection();
						if (toggled)
							toggleBtn.setSelection(false);
						e.doit = false;
					}

				}
			});
			open();
		}

		protected void populate() {

			Composite parent = DropDownPopup.this;
			parent.setLayout(EclipseUiUtils.noSpaceGridLayout());

			final Button addTagBtn = new Button(parent, SWT.LEAD | SWT.FLAT);
			addTagBtn.setText("Add to selected");
			addTagBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false,
					false));
			addBtnListener(toggleBtn.getShell(), addTagBtn, tagId,
					taggablePropName, TagOrUntagInstancesWizard.TYPE_ADD);

			final Button removeTagBtn = new Button(parent, SWT.LEAD | SWT.FLAT);
			removeTagBtn.setText("Remove from selected");
			removeTagBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false,
					false));
			addBtnListener(toggleBtn.getShell(), removeTagBtn, tagId,
					taggablePropName, TagOrUntagInstancesWizard.TYPE_REMOVE);
		}
	}

	protected boolean isOrgOrPerson() {
		SearchNodeEditorInput eei = (SearchNodeEditorInput) getEditorInput();
		String nodeType = eei.getNodeType();

		if (PeopleTypes.PEOPLE_PERSON.equals(nodeType)
				|| PeopleTypes.PEOPLE_ORG.equals(nodeType))
			return true;
		else
			return false;
	}

	private void addBtnListener(final Shell parentShell, final Button button,
			final String tagId, final String taggablePropName,
			final int actionType) {
		button.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 2236384303910015747L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				Object[] rows = tableCmp.getSelectedElements();

				if (rows.length == 0)
					MessageDialog.openInformation(parentShell,
							"Unvalid selection",
							"No item is selected. Nothing has been done.");
				else {
					// We assume here we always use xpath query and thus we have
					// only single node rows
					Wizard wizard = new TagOrUntagInstancesWizard(button
							.getDisplay(), actionType, session, peopleService,
							peopleWorkbenchService, rows, null, tagId,
							taggablePropName);
					WizardDialog dialog = new WizardDialog(parentShell, wizard);
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
	 * (or all if the filter has been reset in the meantime). By default,
	 * returned rows are still *not* linked to the export ID
	 */
	public Object[] getElements(String exportId) {
		return elements;
	}

	/** Generates a pseudo query String that defines the last filter applied */
	public String getFilterAsString() {
		return filterString;
	}

	protected void createListPart(Composite parent) {
		parent.setLayout(new GridLayout());
		tableCmp = new VirtualJcrTableViewer(parent, SWT.MULTI,
				getColumnDefinition(null), hasCheckBoxes());
		TableViewer tableViewer = tableCmp.getTableViewer();
		tableCmp.setLayoutData(EclipseUiUtils.fillAll());

		// Small workaround to enable xpath queries
		String entityType = getEntityType();

		// Xpath queries: only one node by row
		if (getColumnDefinition(null).get(0).getSelectorName() == null)
			entityType = null;

		tableViewer.addDoubleClickListener(new PeopleJcrViewerDClickListener(
				entityType, peopleWorkbenchService));
	}

	/** Refresh the table viewer based on the free text search field */
	protected void populateStaticSearchPanel(final Composite parent) {
		parent.setLayout(new GridLayout(2, false));

		Composite filterCmp = new Composite(parent, SWT.NO_FOCUS);
		filterCmp.setLayoutData(EclipseUiUtils.fillWidth());
		populateSearchPanel(filterCmp);

		// add static filter abilities
		final Link more = new Link(parent, SWT.NONE);
		CmsUtils.markup(more);
		Composite staticFilterCmp = new Composite(parent, SWT.NO_FOCUS);
		staticFilterCmp.setLayoutData(EclipseUiUtils.fillWidth(2));
		populateStaticFilters(staticFilterCmp);

		MoreLinkListener listener = new MoreLinkListener(more, staticFilterCmp,
				false);
		// initialise the layout
		listener.refresh();
		more.addSelectionListener(listener);

		// more.addSelectionListener(new SelectionAdapter() {
		// private static final long serialVersionUID = 2092985883844558754L;
		// private boolean isShown = false;
		// private Composite staticFiltersCmp;
		//
		// @Override
		// public void widgetSelected(SelectionEvent e) {
		// if (isShown) {
		// CmsUtils.clear(staticFiltersCmp);
		// staticFiltersCmp.dispose();
		// more.setText("<a> More... </a>");
		// } else {
		// staticFiltersCmp = new Composite(parent, SWT.NO_FOCUS);
		// staticFiltersCmp.setLayoutData(EclipseUiUtils.fillWidth(2));
		// populateStaticFilters(staticFiltersCmp);
		// more.setText("<a> Less... </a>");
		// }
		// parent.layout();
		// parent.getParent().layout();
		// isShown = !isShown;
		// }
		// });
	}

	private class MoreLinkListener extends SelectionAdapter {
		private static final long serialVersionUID = -524987616510893463L;
		private boolean isShown;
		private final Composite staticFilterCmp;
		private final Link moreLk;

		public MoreLinkListener(Link moreLk, Composite staticFilterCmp,
				boolean isShown) {
			this.moreLk = moreLk;
			this.staticFilterCmp = staticFilterCmp;
			this.isShown = isShown;
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			isShown = !isShown;
			refresh();
		}

		public void refresh() {
			GridData gd = (GridData) staticFilterCmp.getLayoutData();
			if (isShown) {
				moreLk.setText("<a> Less... </a>");
				gd.heightHint = SWT.DEFAULT;
			} else {
				moreLk.setText("<a> More... </a>");
				gd.heightHint = 0;
			}
			forceLayout();
		}

		private void forceLayout() {
			staticFilterCmp.getParent().getParent().layout(true, true);
		}

	}

	/** Refresh the table viewer based on the free text search field */
	protected void populateSearchPanel(Composite parent) {
		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());
		filterTxt = new Text(parent, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH
				| SWT.ICON_CANCEL);
		// TODO internationalize this
		filterTxt.setMessage(PeopleRapConstants.FILTER_HELP_MSG);
		filterTxt.setLayoutData(EclipseUiUtils.fillWidth());

		traverseListener = new TraverseListener() {
			private static final long serialVersionUID = 1914600503113422597L;

			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.keyCode == SWT.CR) {
					e.doit = false;
					refreshFilteredList();
				}
			}
		};
		filterTxt.addTraverseListener(traverseListener);

		// filterTxt.addModifyListener(new ModifyListener() {
		// private static final long serialVersionUID = 5003010530960334977L;
		//
		// public void modifyText(ModifyEvent event) {
		// refreshFilteredList();
		// }
		// });
	}

	protected TraverseListener getTraverseListener() {
		return traverseListener;
	}

	/** Overwrite to customise the filtering widgets */
	protected Text getFilterText() {
		return filterTxt;
	}

	/** Use this method to update the result table */
	protected void setViewerInput(Object[] items) {
		this.elements = items;
		TableViewer tableViewer = tableCmp.getTableViewer();

		tableViewer.setInput(items);
		// we must explicitly set the elements count
		tableViewer.setItemCount(items.length);
		setNbOfFoundResultsLbl(items.length);
		tableViewer.refresh();
	}

	/** Use this method to string representing current applied filter */
	protected void setFilterString(String filterString) {
		this.filterString = filterString;
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

	//
	// Exposes to children classes
	//
	protected Session getSession() {
		return session;
	}

	protected VirtualJcrTableViewer getTableViewer() {
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

	//
	// Local Methods
	//
	protected Text createBoldLT(Composite parent, String title, String message,
			String tooltip) {
		return createBoldLT(parent, title, message, tooltip, 1);
	}

	protected Text createBoldLT(Composite parent, String title, String message,
			String tooltip, int colspan) {
		PeopleRapUtils.createBoldLabel(parent, title);
		Text text = new Text(parent, SWT.BOTTOM | SWT.BORDER);
		text.setLayoutData(EclipseUiUtils.fillAll(colspan, 1));
		text.setMessage(message);
		text.setToolTipText(tooltip);
		return text;
	}

	protected DateText createLD(Composite parent, String title, String tooltip) {
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
		this.repository = repository;
	}

	public void setPeopleService(PeopleService peopleService) {
		this.peopleService = peopleService;
	}

	public void setPeopleWorkbenchService(
			PeopleWorkbenchService peopleWorkbenchService) {
		this.peopleWorkbenchService = peopleWorkbenchService;
	}

}