package org.argeo.connect.people.ui.toolkits;

import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.ui.PeopleUiService;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class MailingListToolkit {

	private final FormToolkit toolkit;
	private final IManagedForm form;
	private final PeopleService peopleService;
	private final PeopleUiService peopleUiService;

	public MailingListToolkit(FormToolkit toolkit, IManagedForm form,
			PeopleService peopleService, PeopleUiService peopleUiService) {
		this.toolkit = toolkit;
		this.form = form;
		this.peopleService = peopleService;
		this.peopleUiService = peopleUiService;
	}

	// public void createControl(Composite parent) {
	// parent.setLayout(new GridLayout(1, false));
	// Composite composite = new Composite(parent, SWT.NONE);
	// composite.setLayout(new GridLayout(1, false));
	// composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	// addFilterPanel(composite);
	// createItemsViewer(composite);
	//
	// // Initialize the list
	// refreshFilteredList();
	// }
	//
	// private TableViewer createItemsViewer(Composite parent) {
	// int style = SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL;
	// Table table = new Table(parent, style);
	// table.setLinesVisible(true);
	//
	// GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
	// // gd.heightHint = 250;
	// table.setLayoutData(gd);
	// table.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
	// table.setData(RWT.CUSTOM_ITEM_HEIGHT, Integer.valueOf(20));
	//
	// TableViewer itemsViewer = new TableViewer(table);
	// itemsViewer.setContentProvider(new BasicNodeListContentProvider());
	//
	// itemsViewer.addDoubleClickListener(new IDoubleClickListener() {
	// @Override
	// public void doubleClick(DoubleClickEvent event) {
	// // same as itemsViewer
	// Node selectedNode = (Node) ((IStructuredSelection) ((TableViewer) event
	// .getSource()).getSelection()).getFirstElement();
	// List<Node> nodes = new ArrayList<Node>();
	// nodes.add(selectedNode);
	// try {
	// addChildren(nodes);
	// } catch (RepositoryException re) {
	// throw new PeopleException(
	// "Unable to add node by double click");
	// }
	// IEditorPart iep = PeopleUiPlugin.getDefault().getWorkbench()
	// .getActiveWorkbenchWindow().getActivePage()
	// .getActiveEditor();
	// if (iep != null && iep instanceof AbstractEntityCTabEditor_old)
	// ((AbstractEntityCTabEditor_old) iep).forceRefresh();
	// }
	// });
	//
	// // The columns
	// TableViewerColumn col = ViewerUtils.createTableViewerColumn(
	// itemsViewer, "selected", SWT.NONE, 25);
	// col.setEditingSupport(new SelectedEditingSupport(itemsViewer));
	//
	// col.setLabelProvider(new ColumnLabelProvider() {
	// private static final long serialVersionUID = 1L;
	//
	// @Override
	// public String getText(Object element) {
	// return null;
	// }
	//
	// @Override
	// public Image getImage(Object element) {
	// if (selectedItems.contains(element)) {
	// return PeopleImages.CHECKED;
	// } else {
	// return PeopleImages.UNCHECKED;
	// }
	// }
	// });
	//
	// col = ViewerUtils.createTableViewerColumn(itemsViewer, "Entities",
	// SWT.NONE, 400);
	// col.setLabelProvider(getCurrentLabelProvider());
	//
	// return itemsViewer;
	// }
	//
	// private CellLabelProvider getCurrentLabelProvider() {
	// return new ColumnLabelProvider() {
	// private static final long serialVersionUID = 1L;
	// private EntitySingleColumnLabelProvider entityLP = defineLabelProvider();
	//
	// @Override
	// public String getText(Object element) {
	// return entityLP.getText(element);
	// }
	//
	// @Override
	// public void dispose() {
	// super.dispose();
	// entityLP.dispose();
	// }
	// };
	// }
	//
	// private class SelectedEditingSupport extends EditingSupport {
	// private static final long serialVersionUID = 1L;
	// private final TableViewer viewer;
	//
	// public SelectedEditingSupport(TableViewer viewer) {
	// super(viewer);
	// this.viewer = viewer;
	// }
	//
	// @Override
	// protected CellEditor getCellEditor(Object element) {
	// return new CheckboxCellEditor(viewer.getTable());
	// }
	//
	// @Override
	// protected boolean canEdit(Object element) {
	// return true;
	// }
	//
	// @Override
	// protected Object getValue(Object element) {
	// return selectedItems.contains(element);
	// }
	//
	// @Override
	// protected void setValue(Object element, Object value) {
	// if ((Boolean) value && !selectedItems.contains(element))
	// selectedItems.add((Node) element);
	// else if (!(Boolean) value && selectedItems.contains(element))
	// selectedItems.remove((Node) element);
	// viewer.update(element, null);
	// }
	// }
	//
	// public Text addFilterPanel(Composite parent) {
	// // Text Area for the filter
	// Text filterTxt = new Text(parent, SWT.BORDER | SWT.SEARCH
	// | SWT.ICON_SEARCH | SWT.ICON_CANCEL);
	// filterTxt.setMessage(PeopleUiConstants.FILTER_HELP_MSG);
	// filterTxt.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
	// | GridData.HORIZONTAL_ALIGN_FILL));
	// filterTxt.addModifyListener(new ModifyListener() {
	// private static final long serialVersionUID = 5003010530960334977L;
	//
	// public void modifyText(ModifyEvent event) {
	// // might be better to use an asynchronous Refresh();
	// refreshFilteredList();
	// }
	// });
	// return filterTxt;
	// }
	//
	// /**
	// * Do the real addition
	// */
	// @Override
	// protected boolean addChildren(List<Node> newChildren, Node entity)
	// throws RepositoryException {
	// boolean wasCheckedOut = CommonsJcrUtils
	// .isNodeCheckedOutByMe(entity);
	// if (!wasCheckedOut)
	// CommonsJcrUtils.checkout(entity);
	//
	// for (Node selectedItem : newChildren) {
	// if (targetParNode.hasNode(selectedItem.getName())) {
	// // TODO manage duplication, we do nothing for the time being
	// } else {
	// Node selectedVenue = targetParNode.addNode(
	// selectedItem.getName(), MsmTypes.MSM_SELECTED_VENUE);
	// String entityUid = selectedItem.getProperty(
	// PeopleNames.PEOPLE_UID).getString();
	// selectedVenue
	// .setProperty(PeopleNames.PEOPLE_REF_UID, entityUid);
	// JcrUtils.updateLastModified(selectedVenue);
	// CommonsJcrUtils.saveAndCheckin(selectedVenue);
	// }
	// }
	//
	// if (wasCheckedOut)
	// entity.getSession().save(); // useless
	// else
	// CommonsJcrUtils.saveAndCheckin(entity);
	// return true;
	// }
	//
	// @Override
	// protected void refreshFilteredList(String filter, String nodeType) {
	// // TODO find a better way to insure all checked items are displayed
	// getSelectedItems().clear();
	// }
}
