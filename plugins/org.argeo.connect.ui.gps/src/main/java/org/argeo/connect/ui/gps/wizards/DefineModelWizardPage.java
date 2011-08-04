package org.argeo.connect.ui.gps.wizards;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.argeo.ArgeoException;
import org.argeo.connect.ui.gps.ConnectUiGpsPlugin;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

public class DefineModelWizardPage extends WizardPage implements ModifyListener {
	// sensor
	private Text sensorText;
	private String sensorName;

	// List of the Nodes to import
	private TableViewer nodesViewer;
	private final static Image NODE_CHECKED = ConnectUiGpsPlugin
			.getImageDescriptor("icons/checked.gif").createImage();
	private final static Image NODE_UNCHECKED = ConnectUiGpsPlugin
			.getImageDescriptor("icons/unchecked.gif").createImage();

	private Node baseNode;
	private List<String> nodesToImportPath = new ArrayList<String>();

	public DefineModelWizardPage(Node baseNode) {
		super("Main");
		setTitle("Finalize import");
		this.baseNode = baseNode;
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		Label lbl = new Label(composite, SWT.LEAD);
		lbl.setText("Sensor :");
		lbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		sensorText = new Text(composite, SWT.LEAD | SWT.BORDER);
		sensorText
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		if (sensorText != null)
			sensorText.addModifyListener(this);

		addNodesTablePart(composite);
		addButtons(composite);
		setControl(composite);

		// All nodes are checked by default
		addAllNodes();
		modifyText(null);
	}

	@Override
	public void modifyText(ModifyEvent event) {
		String message = checkComplete();
		if (message != null)
			setMessage(message, WizardPage.ERROR);
		else {
			setMessage("Complete", WizardPage.INFORMATION);
			setPageComplete(true);
		}
	}

	String getSensorName() {
		return sensorName;
	}

	List<String> getNodesToImportPath() {
		return nodesToImportPath;
	}

	private void addAllNodes() {
		try {
			nodesToImportPath.clear();
			for (NodeIterator ni = baseNode.getNodes(); ni.hasNext();) {
				nodesToImportPath.add(ni.nextNode().getPath());
			}
		} catch (RepositoryException re) {
			throw new ArgeoException("Cannot add children nodes to list", re);
		}
		nodesViewer.refresh();
	}

	/** @return error message or null if complete */
	protected String checkComplete() {
		sensorName = sensorText.getText();
		if (sensorName == null || "".equals(sensorName))
			return "Please enter a sensor.";
		return null;
	}

	// Manage the node list table
	private void addNodesTablePart(Composite composite) {
		Table table = new Table(composite, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL);
		table.setLinesVisible(true);
		table.setHeaderVisible(false);
		nodesViewer = new TableViewer(table);

		GridData gridData = new GridData();
		gridData.horizontalSpan = 2;
		table.setLayoutData(gridData);

		// check column
		TableViewerColumn column = createTableViewerColumn(nodesViewer,
				"checked", 20);
		column.setLabelProvider(new ColumnLabelProvider() {
			public String getText(Object element) {
				return null;
			}

			public Image getImage(Object element) {
				String path = null;
				try {
					path = ((Node) element).getPath();
				} catch (RepositoryException re) {
					throw new ArgeoException("Problem getting node name", re);
				}
				if (path == null)
					return null;
				if (nodesToImportPath.contains(path)) {
					return NODE_CHECKED;
				} else {
					return NODE_UNCHECKED;
				}
			}
		});
		column.setEditingSupport(new NodeEditingSupport(nodesViewer));

		// role column
		column = createTableViewerColumn(nodesViewer, "Role", 200);
		column.setLabelProvider(new ColumnLabelProvider() {
			public String getText(Object element) {
				try {
					return ((Node) element).getName();
				} catch (RepositoryException re) {
					throw new ArgeoException("Problem getting node name", re);
				}
			}

			public Image getImage(Object element) {
				return null;
			}
		});
		nodesViewer.setContentProvider(new NodesContentProvider());
		nodesViewer.setInput(baseNode);
	}

	protected TableViewerColumn createTableViewerColumn(TableViewer viewer,
			String title, int bound) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer,
				SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		return viewerColumn;
	}

	private class NodesContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			try {
				Node curNode = (Node) inputElement;
				List<Node> nodes = new ArrayList<Node>();
				for (NodeIterator ni = curNode.getNodes(); ni.hasNext();) {
					nodes.add(ni.nextNode());
				}
				return nodes.toArray();
			} catch (RepositoryException re) {
				throw new ArgeoException("Cannot construct node list", re);
			}
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	/** Select the columns by editing the checkbox in the first column */
	class NodeEditingSupport extends EditingSupport {

		private final TableViewer viewer;

		public NodeEditingSupport(TableViewer viewer) {
			super(viewer);
			this.viewer = viewer;
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return new CheckboxCellEditor(null, SWT.CHECK | SWT.READ_ONLY);
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected Object getValue(Object element) {
			try {
				Node curNode = (Node) element;
				String curPath = curNode.getPath();
				return nodesToImportPath.contains(curPath);
			} catch (RepositoryException re) {
				throw new ArgeoException(
						"Cannot determine if node is already checked", re);
			}
		}

		@Override
		protected void setValue(Object element, Object value) {
			Boolean inNodes = (Boolean) value;
			try {
				Node curNode = (Node) element;
				String curPath = curNode.getPath();
				if (inNodes && !nodesToImportPath.contains(curPath)) {
					nodesToImportPath.add(curPath);
				} else if (!inNodes && nodesToImportPath.contains(curPath)) {
					nodesToImportPath.remove(curPath);
				}

			} catch (RepositoryException re) {
				throw new ArgeoException(
						"Cannot determine if node is already checked", re);
			}
			viewer.refresh();
		}
	}

	// Buttons to select/unselect all
	private void addButtons(Composite composite) {
		// Unselect All Button
		Button execute = new Button(composite, SWT.PUSH);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.BEGINNING;
		execute.setLayoutData(gridData);
		execute.setText("Unselect All");

		Listener executeListener = new Listener() {
			public void handleEvent(Event event) {
				nodesToImportPath.clear();
				nodesViewer.refresh();
			}
		};
		execute.addListener(SWT.Selection, executeListener);

		// select All Button
		Button selectAll = new Button(composite, SWT.PUSH);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.BEGINNING;
		selectAll.setLayoutData(gridData);
		selectAll.setText("Select All");

		Listener selectAllListener = new Listener() {
			public void handleEvent(Event event) {
				addAllNodes();
			}
		};
		selectAll.addListener(SWT.Selection, selectAllListener);
	}
}
