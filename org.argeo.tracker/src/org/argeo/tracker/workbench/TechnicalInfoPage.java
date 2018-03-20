package org.argeo.tracker.workbench;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.argeo.connect.ConnectException;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ScrolledForm;

/**
 * Generic editor property page. Lists all properties of current node as a
 * complex tree
 */
public class TechnicalInfoPage extends FormPage {

	// Main business Objects
	private Node currentNode;

	public TechnicalInfoPage(FormEditor editor, String id, Node currentNode) {
		super(editor, id, "Tech. info");
		this.currentNode = currentNode;
	}

	protected void createFormContent(IManagedForm managedForm) {
		ScrolledForm form = managedForm.getForm();
		Composite body = form.getBody();
		FillLayout layout = new FillLayout();
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		body.setLayout(layout);
		createComplexTree(body);
	}

	private TreeViewer createComplexTree(Composite parent) {
		int style = SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION;
		Tree tree = new Tree(parent, style);
		TreeColumnLayout tableColumnLayout = new TreeColumnLayout();

		createColumn(tree, tableColumnLayout, "Property", SWT.LEFT, 200, 30);
		createColumn(tree, tableColumnLayout, "Value(s)", SWT.LEFT, 300, 60);
		createColumn(tree, tableColumnLayout, "Attributes", SWT.LEFT, 75, 0);

		tree.setLinesVisible(true);
		tree.setHeaderVisible(true);

		TreeViewer treeViewer = new TreeViewer(tree);
		treeViewer.setContentProvider(new TreeContentProvider());
		treeViewer.setLabelProvider(new PropertyLabelProvider());
		treeViewer.setInput(currentNode);
		treeViewer.expandAll();
		return treeViewer;
	}

	private static TreeColumn createColumn(Tree parent, TreeColumnLayout tableColumnLayout, String name, int style,
			int width, int weight) {
		TreeColumn column = new TreeColumn(parent, style);
		column.setText(name);
		column.setWidth(width);
		column.setMoveable(true);
		column.setResizable(true);
		tableColumnLayout.setColumnData(column, new ColumnWeightData(weight, width, true));
		return column;
	}

	private class TreeContentProvider implements ITreeContentProvider {
		private static final long serialVersionUID = -6162736530019406214L;

		public Object[] getElements(Object parent) {
			Object[] props = null;
			try {

				if (parent instanceof Node) {
					Node node = (Node) parent;
					PropertyIterator pi;
					pi = node.getProperties();
					List<Property> propList = new ArrayList<Property>();
					while (pi.hasNext()) {
						propList.add(pi.nextProperty());
					}
					props = propList.toArray();
				}
			} catch (RepositoryException e) {
				throw new ConnectException("Unexpected exception while listing node properties", e);
			}
			return props;
		}

		public Object getParent(Object child) {
			return null;
		}

		public Object[] getChildren(Object parent) {
			// Object[] result = null;
			if (parent instanceof Property) {
				Property prop = (Property) parent;
				try {
					if (prop.isMultiple()) {
						Value[] values = prop.getValues();
						// List<MultipleValueItem> list = new
						// ArrayList<MultipleValueItem>();
						// for (int i = 0; i < values.length; i++) {
						// MultipleValueItem mvi = new MultipleValueItem(i,
						// values[i]);
						// list.add(mvi);
						// }
						return values;
					}
				} catch (RepositoryException e) {
					throw new ConnectException("Unexpected error getting multiple values property.", e);
				}
			}
			return null;
		}

		public boolean hasChildren(Object parent) {
			try {
				if (parent instanceof Property && ((Property) parent).isMultiple()) {
					return true;
				}
			} catch (RepositoryException e) {
				throw new ConnectException("Unable to determine if property " + "is multiple", e);
			}
			return false;
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}
	}

	private class PropertyLabelProvider extends ColumnLabelProvider {
		private static final long serialVersionUID = -5405794508731390147L;

		// To be able to change column order easily
		public static final int COLUMN_PROPERTY = 0;
		public static final int COLUMN_VALUE = 1;
		public static final int COLUMN_ATTRIBUTES = 2;

		// Utils
		protected DateFormat timeFormatter = new SimpleDateFormat("dd/MM/yyyy, HH:mm");

		public void update(ViewerCell cell) {
			Object element = cell.getElement();
			cell.setText(getColumnText(element, cell.getColumnIndex()));
		}

		public String getColumnText(Object element, int columnIndex) {
			try {
				if (element instanceof Property) {
					Property prop = (Property) element;
					if (prop.isMultiple()) {
						switch (columnIndex) {
						case COLUMN_PROPERTY:
							return prop.getName();
						case COLUMN_VALUE:
							// Corresponding values are listed on children
							return "";
						case COLUMN_ATTRIBUTES:
							return JcrUtils.getPropertyDefinitionAsString(prop);
						}
					} else {
						switch (columnIndex) {
						case COLUMN_PROPERTY:
							return prop.getName();
						case COLUMN_VALUE:
							return formatValueAsString(prop.getValue());
						case COLUMN_ATTRIBUTES:
							return JcrUtils.getPropertyDefinitionAsString(prop);
						}
					}
				} else if (element instanceof Value) {
					Value val = (Value) element;

					switch (columnIndex) {
					case COLUMN_PROPERTY:
						// Nothing to show
						return "";
					case COLUMN_VALUE:
						return formatValueAsString(val);
					case COLUMN_ATTRIBUTES:
						// Corresponding attributes are listed on the parent
						return "";
					}
				}
			} catch (RepositoryException re) {
				throw new ConnectException("Unexepected error while getting property values", re);
			}
			return null;
		}

		private String formatValueAsString(Value value) {
			// TODO enhance this method
			try {
				String strValue;

				if (value.getType() == PropertyType.BINARY)
					strValue = "<binary>";
				else if (value.getType() == PropertyType.DATE)
					strValue = timeFormatter.format(value.getDate().getTime());
				else
					strValue = value.getString();
				return strValue;
			} catch (RepositoryException e) {
				throw new ConnectException("unexpected error while formatting value", e);
			}
		}
	}
}
