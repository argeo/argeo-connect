package org.argeo.connect.e4.parts;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.jcr.Repository;
import javax.jcr.Session;

import org.argeo.cms.CmsUserManager;
import org.argeo.cms.ui.util.CmsUiUtils;
import org.argeo.connect.ConnectConstants;
import org.argeo.connect.e4.ConnectE4Constants;
import org.argeo.connect.e4.resources.parts.TagOrUntagInstancesWizard;
import org.argeo.connect.resources.ResourcesNames;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.ui.ConnectColumnDefinition;
import org.argeo.connect.ui.ConnectUiConstants;
import org.argeo.connect.ui.ConnectUiStyles;
import org.argeo.connect.ui.ConnectUiUtils;
import org.argeo.connect.ui.Refreshable;
import org.argeo.connect.ui.SystemWorkbenchService;
import org.argeo.connect.ui.util.JcrViewerDClickListener;
import org.argeo.connect.ui.util.VirtualJcrTableViewer;
import org.argeo.connect.ui.widgets.DateText;
import org.argeo.connect.ui.widgets.DelayedText;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.rap.rwt.service.ServerPushSession;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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

/**
 * Search the repository with a given entity type at base path. Expect a
 * {@Code SearchNodeEditorInput} editor input.
 */
public abstract class AbstractSearchEntityEditor implements Refreshable {

	/* DEPENDENCY INJECTION */
	@Inject
	private Repository repository;
	@Inject
	private ResourcesService resourcesService;
	@Inject
	private CmsUserManager userAdminService;
	@Inject
	private SystemWorkbenchService systemWorkbenchService;
	@Inject
	private MPart mPart;

	private Session session;
	private String entityType;
	private String basePath;

	// This page widgets
	private VirtualJcrTableViewer tableCmp;
	private Text filterTxt;
	private TraverseListener traverseListener;

	// Locally cache what is displayed in the UI. Enable exports among others.
	private Object[] elements;
	private String filterString;

	public void init() {
		// setSite(site);
		// setInput(input);
		// String label = ((SearchNodeEditorInput) getEditorInput()).getName();
		// setPartName(label);
		session = ConnectJcrUtils.login(repository);
	}

	@PostConstruct
	public void createPartControl(Composite parent) {
		entityType = mPart.getPersistedState().get(ConnectE4Constants.NODE_TYPE);

		init();
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

		if (!systemWorkbenchService.lazyLoadLists())
			// initialize the table
			refreshFilteredList();
	}

	protected abstract void refreshFilteredList();

	/**
	 * Overwrite to false if implementation has no static filter session. Warning:
	 * the refreshStaticFilteredList() must still be implemented
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

	/**
	 * Override this to provide a label that displays the NB of found results
	 */
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
		parent.setLayout(layout);

		final Button selectAllBtn = new Button(parent, SWT.PUSH);
		selectAllBtn.setToolTipText("Select all");
		selectAllBtn.setImage(parent.getDisplay().getSystemImage(SWT.CHECK));
		// PeopleRapImages.CHECK_UNSELECTEDPeopleRapImages.CHECK_UNSELECTED
		RowData rd = new RowData(40, SWT.DEFAULT);
		selectAllBtn.setLayoutData(rd);

		selectAllBtn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (tableCmp.getSelectedElements().length == 0) {
					tableCmp.setAllChecked(true);

					selectAllBtn.setImage(parent.getDisplay().getSystemImage(SWT.CHECK));
					// selectAllBtn.setImage(PeopleRapImages.CHECK_SELECTED);
					selectAllBtn.setToolTipText("Unselect all (At least one element is already selected)");
				} else {
					tableCmp.setAllChecked(false);
					selectAllBtn.setImage(null);
					// selectAllBtn.setImage(PeopleRapImages.CHECK_UNSELECTED);
					selectAllBtn.setToolTipText("Select all");
				}
			}
		});

		Button addTagBtn = new Button(parent, SWT.TOGGLE);
		addTagBtn.setText("Tag");
		// addTagBtn.setImage(PeopleRapImages.ICON_TAG);
		addToggleStateTagBtnListener(addTagBtn, ConnectConstants.RESOURCE_TAG, ResourcesNames.CONNECT_TAGS);

		// if (isOrgOrPerson()) {
		// Button addMLBtn = new Button(parent, SWT.TOGGLE);
		// addMLBtn.setText(" Mailing List");
		// addMLBtn.setImage(PeopleRapImages.ICON_MAILING_LIST);
		// addToggleStateTagBtnListener(addMLBtn,
		// PeopleTypes.PEOPLE_MAILING_LIST, PeopleNames.PEOPLE_MAILING_LISTS);
		// }
	}

	private void addToggleStateTagBtnListener(final Button addTagBtn, final String tagId,
			final String taggablePropName) {

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

						dropDown = new DropDownPopup(addTagBtn, tagId, taggablePropName);
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

		public DropDownPopup(final Control source, String tagId, String taggablePropName) {
			super(source.getDisplay(), SWT.NO_TRIM | SWT.BORDER | SWT.ON_TOP);

			toggleBtn = (Button) source;
			this.tagId = tagId;
			this.taggablePropName = taggablePropName;

			populate();
			// Add border and shadow style
			CmsUiUtils.style(DropDownPopup.this, ConnectUiStyles.POPUP_SHELL);
			pack();
			layout();

			// position the popup
			Rectangle parPosition = source.getBounds();
			Point absolute = source.toDisplay(source.getSize().x, source.getSize().y);
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
			addTagBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
			addBtnListener(toggleBtn.getShell(), addTagBtn, tagId, taggablePropName,
					TagOrUntagInstancesWizard.TYPE_ADD);

			final Button removeTagBtn = new Button(parent, SWT.LEAD | SWT.FLAT);
			removeTagBtn.setText("Remove from selected");
			removeTagBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
			addBtnListener(toggleBtn.getShell(), removeTagBtn, tagId, taggablePropName,
					TagOrUntagInstancesWizard.TYPE_REMOVE);
		}
	}

	protected boolean isOrgOrPerson() {
		// SearchNodeEditorInput eei = (SearchNodeEditorInput) getEditorInput();
		// String nodeType = eei.getNodeType();
		// if (PeopleTypes.PEOPLE_PERSON.equals(nodeType) ||
		// PeopleTypes.PEOPLE_ORG.equals(nodeType))
		// return true;
		// else
		return false;
	}

	private void addBtnListener(final Shell parentShell, final Button button, final String tagId,
			final String taggablePropName, final int actionType) {
		button.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 2236384303910015747L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				Object[] rows = tableCmp.getSelectedElements();

				if (rows.length == 0)
					MessageDialog.openInformation(parentShell, "Unvalid selection",
							"No item is selected. Nothing has been done.");
				else {
					// We assume here we always use xpath query and thus we have
					// only single node rows
					Wizard wizard = new TagOrUntagInstancesWizard(button.getDisplay(), actionType, session,
							resourcesService, systemWorkbenchService, rows, null, tagId, taggablePropName);
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
	 * Overwrite to provide corresponding column definitions. Also used for exports
	 * generation
	 */
	public abstract List<ConnectColumnDefinition> getColumnDefinition(String extractId);

	/**
	 * Call this when resetting static filters if you also want to reset the free
	 * text search field
	 */
	protected void resetFilterText() {
		getFilterText().setText("");
	}

	/**
	 * Returns an array with the rows that where retrieved by the last search (or
	 * all if the filter has been reset in the meantime). By default, returned rows
	 * are still *not* linked to the export ID
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
		tableCmp = new VirtualJcrTableViewer(parent, SWT.MULTI, getColumnDefinition(null), hasCheckBoxes());
		TableViewer tableViewer = tableCmp.getTableViewer();
		tableCmp.setLayoutData(EclipseUiUtils.fillAll());

		// Small workaround to enable xpath queries
		String entityType = getEntityType();
		// Xpath queries: only one node by row
		if (getColumnDefinition(null).get(0).getSelectorName() == null)
			entityType = null;
		tableViewer.addDoubleClickListener(new JcrViewerDClickListener(entityType));
	}

	/** Refresh the table viewer based on the free text search field */
	protected void populateStaticSearchPanel(final Composite parent) {
		parent.setLayout(new GridLayout(2, false));

		Composite filterCmp = new Composite(parent, SWT.NO_FOCUS);
		filterCmp.setLayoutData(EclipseUiUtils.fillWidth());
		populateSearchPanel(filterCmp);

		// add static filter abilities
		final Link more = new Link(parent, SWT.NONE);
		CmsUiUtils.markup(more);
		Composite staticFilterCmp = new Composite(parent, SWT.NO_FOCUS);
		staticFilterCmp.setLayoutData(EclipseUiUtils.fillWidth(2));
		populateStaticFilters(staticFilterCmp);

		MoreLinkListener listener = new MoreLinkListener(more, staticFilterCmp, false);
		// initialise the layout
		listener.refresh();
		more.addSelectionListener(listener);
	}

	private class MoreLinkListener extends SelectionAdapter {
		private static final long serialVersionUID = -524987616510893463L;
		private boolean isShown;
		private final Composite staticFilterCmp;
		private final Link moreLk;

		public MoreLinkListener(Link moreLk, Composite staticFilterCmp, boolean isShown) {
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
		boolean isDyn = systemWorkbenchService.queryWhenTyping();
		int style = SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH | SWT.ICON_CANCEL;
		// if (isDyn)
		// filterTxt = new DelayedText(parent, style,
		// ConnectUiConstants.SEARCH_TEXT_DELAY);
		// else
		// filterTxt = new Text(parent, style);

		if (isDyn) {
			final DelayedText delayedText = new DelayedText(parent, style, ConnectUiConstants.SEARCH_TEXT_DELAY);
			final ServerPushSession pushSession = new ServerPushSession();
			(delayedText).addDelayedModifyListener(pushSession, new ModifyListener() {
				private static final long serialVersionUID = 5003010530960334977L;

				public void modifyText(ModifyEvent event) {
					delayedText.getText().getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							refreshFilteredList();
						}
					});
					pushSession.stop();
				}
			});
			filterTxt = delayedText.getText();
		} else {
			filterTxt = new Text(parent, style);
		}
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
	@Focus
	public void setFocus() {
		getFilterText().forceFocus();
	}

	@PreDestroy
	public void dispose() {
		JcrUtils.logoutQuietly(session);
		// super.dispose();
	}

	@Override
	public void forceRefresh(Object object) {
		refreshFilteredList();
	}

	// Exposes to children classes

	protected String getEntityType() {
		return entityType;
		// return ((SearchNodeEditorInput) getEditorInput()).getNodeType();
	}

	protected String getBasePath() {
		return basePath;
		// return ((SearchNodeEditorInput) getEditorInput()).getBasePath();
	}

	protected VirtualJcrTableViewer getTableViewer() {
		return tableCmp;
	}

	protected Session getSession() {
		return session;
	}

	protected CmsUserManager getUserAdminService() {
		return userAdminService;
	}

	protected ResourcesService getResourceService() {
		return resourcesService;
	}

	protected SystemWorkbenchService getSystemWorkbenchService() {
		return systemWorkbenchService;
	}

	// Helpers to create static filters UI
	protected Text createBoldLT(Composite parent, String title, String message, String tooltip) {
		return createBoldLT(parent, title, message, tooltip, 1);
	}

	protected Text createBoldLT(Composite parent, String title, String message, String tooltip, int colspan) {
		ConnectUiUtils.createBoldLabel(parent, title);
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

	/* DEPENDENCY INJECTION */
	// public void setRepository(Repository repository) {
	// this.repository = repository;
	// }
	//
	// public void setUserAdminService(UserAdminService userAdminService) {
	// this.userAdminService = userAdminService;
	// }
	//
	// public void setResourcesService(ResourcesService resourcesService) {
	// this.resourcesService = resourcesService;
	// }
	//
	// public void setSystemWorkbenchService(SystemWorkbenchService
	// systemWorkbenchService) {
	// this.systemWorkbenchService = systemWorkbenchService;
	// }
}
