package org.argeo.connect.people.ui.toolkits;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.query.Row;

import org.argeo.connect.people.ui.PeopleImages;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.editors.AbstractEntityEditor;
import org.argeo.connect.people.ui.providers.SimpleJcrRowLabelProvider;
import org.argeo.connect.people.ui.utils.MailListComparator;
import org.argeo.connect.people.ui.utils.PeopleUiUtils;
import org.argeo.eclipse.ui.jcr.lists.ColumnDefinition;
import org.argeo.eclipse.ui.utils.ViewerUtils;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;

public class MailingListToolkit {

	// private final FormToolkit toolkit;
	// private final IManagedForm form;
	// private final PeopleService peopleService;
	// private final PeopleUiService peopleUiService;

	public MailingListToolkit() {
		// (FormToolkit toolkit, IManagedForm form,
		// PeopleService peopleService, PeopleUiService peopleUiService) {
		// this.toolkit = toolkit;
		// this.form = form;
		// this.peopleService = peopleService;
		// this.peopleUiService = peopleUiService;
	}

	/**
	 * create a table viewer with a column for selected items. Note that it is
	 * caller responsability to set a contentprovider
	 **/
	public TableViewer createItemsViewerWithCheckBox(Composite parent,
			List<Row> selectedItems, List<ColumnDefinition> columns) {
		parent.setLayout(PeopleUiUtils.gridLayoutNoBorder());

		// TODO clean this: we must 1st create the composite for the filter,
		// then the table, then the filter itself
		Composite filterCmp = new Composite(parent, SWT.NO_FOCUS);
		filterCmp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		TableViewer viewer = createItemsViewer(parent, selectedItems, columns);
		addFilterPanel(filterCmp, viewer);
		parent.layout();
		return viewer;
	}

	private TableViewer createItemsViewer(Composite parent,
			final List<Row> selectedItems, List<ColumnDefinition> columns) {
		int style = SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL;
		Table table = new Table(parent, style);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		MailListComparator comparator = new MailListComparator();

		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		table.setLayoutData(gd);

		TableViewer itemsViewer = new TableViewer(table);

		// The columns
		TableViewerColumn col = ViewerUtils.createTableViewerColumn(
				itemsViewer, "", SWT.NONE, 25);
		col.setEditingSupport(new SelectedEditingSupport(itemsViewer,
				selectedItems));
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

		int i = 1;
		for (ColumnDefinition colDef : columns) {
			col = ViewerUtils.createTableViewerColumn(itemsViewer,
					colDef.getHeaderLabel(), SWT.NONE, colDef.getColumnSize());
			col.setLabelProvider(new SimpleJcrRowLabelProvider(colDef
					.getSelectorName(), colDef.getPropertyName()));
			col.getColumn().addSelectionListener(
					getSelectionAdapter(i, colDef.getPropertyType(),
							colDef.getSelectorName(), colDef.getPropertyName(),
							comparator, itemsViewer));
			i++;
		}

		ColumnDefinition firstCol = columns.get(0);
		// IMPORTANT: initialize comparator before setting it
		comparator.setColumn(firstCol.getPropertyType(),
				firstCol.getSelectorName(), firstCol.getPropertyName());
		itemsViewer.setComparator(comparator);

		return itemsViewer;
	}

	/** Extends to provide the correct add call back method */
	public abstract class AddDoubleClickListener implements
			IDoubleClickListener {
		@Override
		public void doubleClick(DoubleClickEvent event) {
			// same as itemsViewer
			Node selectedNode = (Node) ((IStructuredSelection) ((TableViewer) event
					.getSource()).getSelection()).getFirstElement();
			List<Node> nodes = new ArrayList<Node>();
			nodes.add(selectedNode);
			add(nodes);
			IEditorPart iep = PeopleUiPlugin.getDefault().getWorkbench()
					.getActiveWorkbenchWindow().getActivePage()
					.getActiveEditor();
			if (iep != null && iep instanceof AbstractEntityEditor)
				((AbstractEntityEditor) iep).forceRefresh();
		}

		protected abstract void add(List<Node> nodes);
	}

	private class SelectedEditingSupport extends EditingSupport {
		private static final long serialVersionUID = 1L;
		private final TableViewer viewer;
		private List<Row> selectedItems;

		public SelectedEditingSupport(TableViewer viewer,
				List<Row> selectedItems) {
			super(viewer);
			this.viewer = viewer;
			this.selectedItems = selectedItems;
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
				selectedItems.add((Row) element);
			else if (!(Boolean) value && selectedItems.contains(element))
				selectedItems.remove((Row) element);
			viewer.update(element, null);
		}
	}

	public Text addFilterPanel(Composite parent, final TableViewer viewer) {
		parent.setLayout(PeopleUiUtils.gridLayoutNoBorder());
		// Text Area for the filter
		final Text filterTxt = new Text(parent, SWT.BORDER | SWT.SEARCH
				| SWT.ICON_SEARCH | SWT.ICON_CANCEL);
		filterTxt.setMessage(PeopleUiConstants.FILTER_HELP_MSG);
		filterTxt.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_FILL));
		filterTxt.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 5003010530960334977L;

			public void modifyText(ModifyEvent event) {
				// might be better to use an asynchronous Refresh();
				viewer.setInput(filterTxt.getText());
			}
		});
		return filterTxt;
	}

	private SelectionAdapter getSelectionAdapter(final int index,
			final int propertyType, final String selectorName,
			final String propertyName, final MailListComparator comparator,
			final TableViewer viewer) {
		SelectionAdapter selectionAdapter = new SelectionAdapter() {
			private static final long serialVersionUID = -3452356616673385039L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				Table table = viewer.getTable();
				comparator.setColumn(propertyType, selectorName, propertyName);
				int dir = table.getSortDirection();
				if (table.getSortColumn() == table.getColumn(index)) {
					dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
				} else {
					dir = SWT.DOWN;
				}
				table.setSortDirection(dir);
				table.setSortColumn(table.getColumn(index));
				viewer.refresh();
			}
		};
		return selectionAdapter;
	}
}
