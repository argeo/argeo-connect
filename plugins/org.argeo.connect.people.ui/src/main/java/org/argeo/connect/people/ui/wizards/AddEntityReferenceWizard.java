package org.argeo.connect.people.ui.wizards;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.ui.PeopleImages;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.editors.utils.AbstractEntityCTabEditor;
import org.argeo.connect.people.ui.providers.BasicNodeListContentProvider;
import org.argeo.connect.people.ui.providers.EntitySingleColumnLabelProvider;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.eclipse.ui.utils.ViewerUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;

/**
 * Generic one page wizard to add one or more entity to a given Node. Provides a
 * search field to reduce to displayed list. The caller might set a parent node
 * to reduce the scope of the search.
 */

public abstract class AddEntityReferenceWizard extends Wizard {
	// private final static Log log = LogFactory.getLog(AddEntityWizard.class);

	private final Repository repository;
	private Session currSession;
	private PeopleService peopleService;
	private List<Node> selectedItems = new ArrayList<Node>();

	// This page widgets
	protected Text filterTxt;
	protected TableViewer itemsViewer;

	public AddEntityReferenceWizard(Repository repository,
			PeopleService peopleService) {
		this.peopleService = peopleService;
		this.repository = repository;
		this.currSession = CommonsJcrUtils.login(repository);
	}

	// //////////////////////////////////////
	// Generic part to use when Overriding

	/**
	 * Called by the wizard performFinish() method. Overwrite to perform real
	 * addition of new items to a given Node depending on its nature, dealing
	 * with duplicate and check out state among others.
	 */
	protected abstract boolean addChildren(List<Node> newChildren)
			throws RepositoryException;

	/** performs the effective refresh of the list */
	protected abstract void refreshFilteredList();

	/** Define the display message for current Wizard */
	protected abstract String getCurrDescription();

	/**
	 * Overwrite to provide the correct Label provider depending on the
	 * currently being added type of entities
	 */
	protected abstract EntitySingleColumnLabelProvider defineLabelProvider();

	// Exposes to children
	protected Repository getRepository() {
		return repository;
	}

	protected Session getSession() {
		return currSession;
	}

	protected PeopleService getPeopleService() {
		return peopleService;
	}

	protected List<Node> getSelectedItems() {
		return selectedItems;
	}

	@Override
	public void addPages() {
		try {
			SelectChildrenPage page = new SelectChildrenPage("Main page");
			addPage(page);
		} catch (Exception e) {
			throw new PeopleException("Cannot add page to wizard", e);
		}
	}

	/**
	 * Called when the user click on 'Finish' in the wizard. The chosen
	 * entity(ies) is(are) then added to the parent target node
	 */
	@Override
	public boolean performFinish() {
		if (selectedItems.isEmpty())
			return true;
		try {
			addChildren(selectedItems);
		} catch (Exception e) {
			throw new PeopleException("Unable to finish", e);
		} finally {
			// JcrUtils.logoutQuietly(currSession);
		}
		return true;
	}

	@Override
	public boolean performCancel() {
		// rollBack();
		// JcrUtils.logoutQuietly(currSession);
		return true;
	}

	@Override
	public boolean canFinish() {
		return true;
	}

	@Override
	public void dispose() {
		super.dispose();
		JcrUtils.logoutQuietly(currSession);
	}

	protected class SelectChildrenPage extends WizardPage {
		private static final long serialVersionUID = 1L;

		public SelectChildrenPage(String pageName) {
			super(pageName);
			setMessage(getCurrDescription());
			setDescription("HINT: Double clic on a given item "
					+ "directly adds it without closing the current wizard.");
		}

		public void createControl(Composite parent) {
			parent.setLayout(new GridLayout(1, false));
			Composite composite = new Composite(parent, SWT.NONE);
			composite.setLayout(new GridLayout(1, false));
			composite
					.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			addFilterPanel(composite);
			createItemsViewer(composite);
			// Don't forget this.
			setControl(composite);

			// Initialize the list
			refreshFilteredList();
		}

		private void createItemsViewer(Composite parent) {
			int style = SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL;
			Table table = new Table(parent, style);
			table.setLinesVisible(true);

			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
			gd.heightHint = 250;
			table.setLayoutData(gd);
			table.setData(PeopleUiConstants.MARKUP_ENABLED, Boolean.TRUE);
			table.setData(PeopleUiConstants.CUSTOM_ITEM_HEIGHT,
					Integer.valueOf(20));

			itemsViewer = new TableViewer(table);
			itemsViewer.setContentProvider(new BasicNodeListContentProvider());

			itemsViewer.addDoubleClickListener(new IDoubleClickListener() {
				@Override
				public void doubleClick(DoubleClickEvent event) {
					// same as itemsViewer
					Node selectedNode = (Node) ((IStructuredSelection) ((TableViewer) event
							.getSource()).getSelection()).getFirstElement();
					List<Node> nodes = new ArrayList<Node>();
					nodes.add(selectedNode);
					try {
						addChildren(nodes);
					} catch (RepositoryException re) {
						throw new PeopleException(
								"Unable to add node by double click");
					}
					IEditorPart iep = PeopleUiPlugin.getDefault()
							.getWorkbench().getActiveWorkbenchWindow()
							.getActivePage().getActiveEditor();
					if (iep != null && iep instanceof AbstractEntityCTabEditor)
						((AbstractEntityCTabEditor) iep).forceRefresh();
				}
			});

			// The columns
			TableViewerColumn col = ViewerUtils.createTableViewerColumn(
					itemsViewer, "selected", SWT.NONE, 25);
			col.setEditingSupport(new SelectedEditingSupport(itemsViewer));

			col.setLabelProvider(new ColumnLabelProvider() {
				private static final long serialVersionUID = 1L;

				@Override
				public String getText(Object element) {
					return null;
				}

				@Override
				public Image getImage(Object element) {
					if (selectedItems.contains(element)) {
						return PeopleImages.CHECKED;
					} else {
						return PeopleImages.UNCHECKED;
					}
				}
			});

			col = ViewerUtils.createTableViewerColumn(itemsViewer, "Entities",
					SWT.NONE, 400);
			col.setLabelProvider(getCurrentLabelProvider());
		}

		private CellLabelProvider getCurrentLabelProvider() {
			return new ColumnLabelProvider() {
				private static final long serialVersionUID = 1L;
				private EntitySingleColumnLabelProvider msmLP = defineLabelProvider();

				@Override
				public String getText(Object element) {
					return msmLP.getText(element);
				}

				@Override
				public void dispose() {
					super.dispose();
					msmLP.dispose();
				}
			};
		}

		private class SelectedEditingSupport extends EditingSupport {
			private static final long serialVersionUID = 1L;
			private final TableViewer viewer;

			public SelectedEditingSupport(TableViewer viewer) {
				super(viewer);
				this.viewer = viewer;
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return new CheckboxCellEditor(viewer.getTable());
			}

			@Override
			protected boolean canEdit(Object element) {
				return true;
			}

			@Override
			protected Object getValue(Object element) {
				return selectedItems.contains(element);
			}

			@Override
			protected void setValue(Object element, Object value) {
				if ((Boolean) value && !selectedItems.contains(element))
					selectedItems.add((Node) element);
				else if (!(Boolean) value && selectedItems.contains(element))
					selectedItems.remove((Node) element);
				viewer.update(element, null);
			}
		}

		public void addFilterPanel(Composite parent) {
			// Text Area for the filter
			filterTxt = new Text(parent, SWT.BORDER | SWT.SEARCH
					| SWT.ICON_SEARCH | SWT.ICON_CANCEL);
			filterTxt.setMessage(PeopleUiConstants.FILTER_HELP_MSG);
			filterTxt.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
					| GridData.HORIZONTAL_ALIGN_FILL));
			filterTxt.addModifyListener(new ModifyListener() {
				private static final long serialVersionUID = 5003010530960334977L;

				public void modifyText(ModifyEvent event) {
					// might be better to use an asynchronous Refresh();
					refreshFilteredList();
				}
			});
		}
	}
}
