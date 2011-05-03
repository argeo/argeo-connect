package org.argeo.connect.ui.crisis.editors;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import org.argeo.ArgeoException;
import org.argeo.connect.ConnectNames;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

public class SituationOverviewPage extends FormPage implements ConnectNames {
	private Node situationDefinitionNode;
	private Node featureNode;

	private String currentStatus;

	public SituationOverviewPage(Node featureNode,
			Node situationDefinitionNode, String id, String title) {
		super(id, title);
		this.featureNode = featureNode;
		this.situationDefinitionNode = situationDefinitionNode;
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		Composite parent = managedForm.getForm().getBody();
		parent.setLayout(new GridLayout(2, true));

		FormToolkit tk = managedForm.getToolkit();

		
		// upper left
		Composite upperLeft = tk.createComposite(parent);
		upperLeft.setLayout(new GridLayout(1, true));

		AbstractFormPart statusFormPart = new AbstractFormPart() {
			public void initialize(IManagedForm form) {
				try {
					currentStatus = getSituationNode().getProperty(
							CONNECT_STATUS).getString();
				} catch (RepositoryException e) {
					throw new ArgeoException("Cannot get value", e);
				}
				super.initialize(form);
			}

			public void commit(boolean onSave) {
				try {
					getSituationNode().setProperty(CONNECT_STATUS,
							currentStatus);
					super.commit(onSave);
				} catch (RepositoryException e) {
					throw new ArgeoException("Cannot set value", e);
				}
			}

		};
		createStatusViewer(parent, statusFormPart);
		managedForm.addPart(statusFormPart);

		final Text update = tk
				.createText(upperLeft, "", SWT.MULTI | SWT.BORDER);

		Composite priority = tk.createComposite(upperLeft);
		final Button major = tk.createButton(priority,
				"Highlight (breaking news, etc.)", SWT.CHECK);
		final Button minor = tk.createButton(priority,
				"Do not notify (for minor updates)", SWT.CHECK);

		final Text description = tk.createText(parent, "", SWT.MULTI
				| SWT.BORDER);

		managedForm.addPart(new AbstractFormPart() {
			public void commit(boolean onSave) {
				Node situationNode = getSituationNode();
				try {
					if (!update.getText().trim().equals(""))
						situationNode.setProperty(CONNECT_UPDATE,
								update.getText());
					situationNode.setProperty(CONNECT_TEXT,
							description.getText());
					super.commit(onSave);
				} catch (RepositoryException e) {
					throw new ArgeoException("Cannot commit " + situationNode,
							e);
				}
			}
		});
	}

	/** Create if does not exist */
	protected Node getSituationNode() {
		try {
			String name = situationDefinitionNode.getName();
			Node situationNode;
			if (!featureNode.hasNode(name)) {
				situationNode = featureNode.addNode(name);
				situationNode.addMixin(NodeType.MIX_SIMPLE_VERSIONABLE);
				situationNode.addMixin(NodeType.MIX_CREATED);
				situationNode.addMixin(NodeType.MIX_LAST_MODIFIED);
				situationNode.addMixin(NodeType.MIX_LANGUAGE);
			} else {
				situationNode = featureNode.getNode(name);
			}
			return situationNode;
		} catch (RepositoryException e) {
			throw new ArgeoException("Cannot get situation node for "
					+ featureNode, e);
		}
	}

	protected void createStatusViewer(Composite parent,
			AbstractFormPart formPart) {
		FormToolkit tk = getManagedForm().getToolkit();
		Section section = tk.createSection(parent, Section.DESCRIPTION
				| Section.TITLE_BAR);
		section.setText("Statuses");
		section.setDescription("Current situation");
		Table table = new Table(section, SWT.SINGLE | SWT.H_SCROLL);
		section.setClient(table);

		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.verticalSpan = 20;
		table.setLayoutData(gridData);
		table.setLinesVisible(true);
		table.setHeaderVisible(false);
		TableViewer viewer = new TableViewer(table);

		// check column
		TableViewerColumn column = createTableViewerColumn(viewer, "checked",
				20);
		column.setLabelProvider(new ColumnLabelProvider() {
			public String getText(Object element) {
				return null;
			}

			public Image getImage(Object element) {
				return null;
			}
		});
		column.setEditingSupport(new StatusEditingSupport(viewer, formPart));

		// role column
		column = createTableViewerColumn(viewer, "Role", 200);
		column.setLabelProvider(new ColumnLabelProvider() {
			public String getText(Object element) {
				return element.toString();
			}

			public Image getImage(Object element) {
				return null;
			}
		});
		viewer.setContentProvider(new StatusContentProvider());
		viewer.setInput(getEditorSite());

	}

	protected TableViewerColumn createTableViewerColumn(TableViewer viewer,
			String title, int bound) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer,
				SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(false);
		column.setMoveable(false);
		return viewerColumn;

	}

	protected class StatusContentProvider implements IStructuredContentProvider {

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public Object[] getElements(Object inputElement) {
			try {
				List<Node> lst = new ArrayList<Node>();
				for (NodeIterator nit = situationDefinitionNode
						.getNodes(CONNECT_STATUS); nit.hasNext();) {
					lst.add(nit.nextNode());
				}
				return lst.toArray();
			} catch (RepositoryException e) {
				throw new ArgeoException("Cannot list statuses", e);
			}
		}

	}

	/** Select the columns by editing the checkbox in the first column */
	class StatusEditingSupport extends EditingSupport {

		private final TableViewer viewer;
		private final AbstractFormPart formPart;

		public StatusEditingSupport(TableViewer viewer,
				AbstractFormPart formPart) {
			super(viewer);
			this.viewer = viewer;
			this.formPart = formPart;
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
				String status = ((Node) element).getName();
				return status.equals(currentStatus);
			} catch (RepositoryException e) {
				throw new ArgeoException("Cannot get value", e);
			}
		}

		@Override
		protected void setValue(Object element, Object value) {
			try {
				Boolean hasStatus = (Boolean) value;
				String status = ((Node) element).getName();
				if (hasStatus) {
					currentStatus = status;
					formPart.markDirty();
				}
				viewer.refresh();
			} catch (RepositoryException e) {
				throw new ArgeoException("Cannot set value", e);
			}
		}
	}

}
