package org.argeo.connect.ui.gps.editors;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.argeo.connect.ui.gps.ConnectUiGpsPlugin;
import org.argeo.connect.ui.gps.commons.ModifiedFieldListener;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

public class MetaDataPage extends AbstractCleanDataEditorPage {

	// local variables
	public final static String ID = "cleanDataEditor.metaDataPage";
	private final static Log log = LogFactory
			.getLog(DefineParamsAndReviewPage.class);

	// Current page widgets
	private Text paramSetLabel;
	private Text paramSetComments;
	private Text defaultSensorName;

	// parameter table
	private TableViewer paramsTableViewer;
	private List<Node> paramNodeList;

	public MetaDataPage(FormEditor editor, String title) {
		super(editor, ID, title);
	}

	protected void createFormContent(IManagedForm managedForm) {
		try {
			ScrolledForm form = managedForm.getForm();

			TableWrapLayout twt = new TableWrapLayout();
			TableWrapData twd = new TableWrapData(SWT.FILL);
			twd.grabHorizontal = true;
			form.getBody().setLayout(twt);
			form.getBody().setLayoutData(twd);

			createFields(form.getBody());
			createParamTable(form.getBody());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Section createFields(Composite parent) {
		FormToolkit tk = getManagedForm().getToolkit();
		GridData gd;

		// Session
		Section section = tk.createSection(parent, Section.TITLE_BAR);
		section.setText(ConnectUiGpsPlugin.getGPSMessage(METADATA_SECTION_TITLE));
		Composite body = tk.createComposite(section, SWT.WRAP);
		section.setClient(body);

		GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		layout.numColumns = 3;
		body.setLayout(layout);

		// Name and comments
		Label label = new Label(body, SWT.NONE);
		label.setText(ConnectUiGpsPlugin.getGPSMessage(PARAM_SET_LABEL_LBL));
		paramSetLabel = new Text(body, SWT.FILL | SWT.BORDER);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.horizontalSpan = 2;
		paramSetLabel.setLayoutData(gd);
		setTextValue(paramSetLabel, CONNECT_NAME);

		label = new Label(body, SWT.NONE);
		label.setText(ConnectUiGpsPlugin.getGPSMessage(PARAM_SET_COMMENTS_LBL));
		paramSetComments = new Text(body, SWT.FILL | SWT.BORDER);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.horizontalSpan = 2;
		paramSetComments.setLayoutData(gd);
		setTextValue(paramSetComments, CONNECT_COMMENTS);

		// Default Sensor name
		label = new Label(body, SWT.NONE);
		label.setText(ConnectUiGpsPlugin.getGPSMessage(DEFAULT_SENSOR_NAME_LBL));
		defaultSensorName = new Text(body, SWT.FILL | SWT.BORDER);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.horizontalSpan = 2;
		defaultSensorName.setLayoutData(gd);
		setTextValue(defaultSensorName, CONNECT_DEFAULT_SENSOR);

		AbstractFormPart part = new SectionPart(section) {
			public void commit(boolean onSave) {
				// implements here what must be done while committing and saving
				// (if onSave = true)
				log.debug("MetadataPage - Fields part doSave");

				Node currentSessionNode = getEditor().getCurrentSessionNode();
				try {
					currentSessionNode.setProperty(CONNECT_NAME,
							paramSetLabel.getText());
					currentSessionNode.setProperty(CONNECT_COMMENTS,
							paramSetComments.getText());
					currentSessionNode.setProperty(CONNECT_DEFAULT_SENSOR,
							defaultSensorName.getText());
					super.commit(onSave);

				} catch (RepositoryException re) {
					throw new ArgeoException(
							"Error while trying to persist Meta Data for Session",
							re);
				}

			}
		};

		paramSetLabel.addModifyListener(new ModifiedFieldListener(part));
		paramSetComments.addModifyListener(new ModifiedFieldListener(part));
		defaultSensorName.addModifyListener(new ModifiedFieldListener(part));

		getManagedForm().addPart(part);
		return section;
	}

	private Section createParamTable(Composite parent) {
		FormToolkit tk = getManagedForm().getToolkit();

		// Section
		Section section = tk.createSection(parent, Section.TITLE_BAR);
		section.setText(ConnectUiGpsPlugin
				.getGPSMessage(METADATA_PARAM_TABLE_TITLE));
		Composite body = tk.createComposite(section, SWT.WRAP);
		body.setLayout(new FillLayout());
		section.setClient(body);

		// Create the parameter table
		final Table table = tk.createTable(body, SWT.NONE | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.BORDER);

		// Handle Life cycle of the table
		final AbstractFormPart part = new SectionPart(section) {
			public void commit(boolean onSave) {
				super.commit(onSave);

				log.debug("MetadataPage - param table doSave");

				// We inform param page that the model has changed.
				IManagedForm imf = getEditor().findPage(
						DefineParamsAndReviewPage.ID).getManagedForm();
				if (imf != null)
					((AbstractFormPart) imf.getParts()[0]).markStale();
			}
		};
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		paramsTableViewer = new TableViewer(table);

		// Check boxes column
		TableViewerColumn column = createTableViewerColumn(paramsTableViewer,
				"", 20);
		column.setLabelProvider(new ColumnLabelProvider() {
			public String getText(Object element) {
				return null;
			}

			public Image getImage(Object element) {
				try {
					Node cnode = (Node) element;
					if (cnode.getProperty(CONNECT_PARAM_IS_USED).getBoolean())
						return CHECKED;
					else
						return UNCHECKED;
				} catch (RepositoryException re) {
					throw new ArgeoException("Problem getting node property ",
							re);
				}

			}
		});
		column.setEditingSupport(new CheckboxEditingSupport(paramsTableViewer,
				part));

		// Param name column
		column = createTableViewerColumn(paramsTableViewer, "Param name", 200);
		column.setLabelProvider(new StyledCellLabelProvider() {
			public void update(final ViewerCell cell) {
				try {
					Node cnode = (Node) cell.getElement();
					String currentText = cnode.getProperty(CONNECT_PARAM_NAME)
							.getString();
					cell.setText(currentText);
				} catch (RepositoryException re) {
					throw new ArgeoException("Problem getting sensor name", re);
				}
			}
		});

		// Min value column
		column = createTableViewerColumn(paramsTableViewer, "Min Value", 100);
		column.setLabelProvider(new CellLabelProvider() {
			public void update(final ViewerCell cell) {
				try {
					Node cnode = (Node) cell.getElement();
					String currentText = Double.toString(cnode.getProperty(
							CONNECT_PARAM_MIN_VALUE).getDouble());
					cell.setText(currentText);
				} catch (RepositoryException re) {
					throw new ArgeoException("Problem getting sensor name", re);
				}
			}
		});
		column.setEditingSupport(new MinValueEditingSupport(paramsTableViewer,
				part));
		// Min value column
		// column = createTableViewerColumn(paramsTableViewer, "Min Value",
		// 100);
		// column.setLabelProvider(new StyledCellLabelProvider() {
		// public void update(final ViewerCell cell) {
		// try {
		// Node cnode = (Node) cell.getElement();
		// String currentText = ""
		// + cnode.getProperty(CONNECT_PARAM_MIN_VALUE)
		// .getDouble();
		// cell.setText(currentText);
		// } catch (RepositoryException re) {
		// throw new ArgeoException("Problem getting sensor name", re);
		// }
		// }
		// });
		// column.setEditingSupport(new
		// MinValueEditingSupport(paramsTableViewer,
		// part));

		// Max value column
		column = createTableViewerColumn(paramsTableViewer, "Max Value", 100);
		column.setLabelProvider(new StyledCellLabelProvider() {
			public void update(final ViewerCell cell) {
				try {
					Node cnode = (Node) cell.getElement();
					String currentText = Double.toString(cnode.getProperty(
							CONNECT_PARAM_MAX_VALUE).getDouble());
					cell.setText(currentText);
				} catch (RepositoryException re) {
					throw new ArgeoException("Problem getting sensor name", re);
				}
			}
		});
		column.setEditingSupport(new MaxValueEditingSupport(paramsTableViewer,
				part));

		// Initialize the table input
		paramNodeList = new ArrayList<Node>();
		try {
			Node session = getEditor().getCurrentSessionNode();
			NodeIterator ni = session.getNodes();
			while (ni.hasNext()) {
				Node node = ni.nextNode();
				if (node.getPrimaryNodeType().isNodeType(
						CONNECT_CLEAN_PARAMETER))
					paramNodeList.add(node);
			}
		} catch (RepositoryException re) {
			throw new ArgeoException(
					"Cannot initialize table list of parameters", re);
		}
		paramsTableViewer.setContentProvider(new ParamsTableContentProvider());
		paramsTableViewer.setInput(paramNodeList);

		getManagedForm().addPart(part);
		return section;
	}

	// Fill with existing values :
	private void setTextValue(Text curText, String jcrPropertyName) {
		try {
			Node curNode = getEditor().getCurrentSessionNode();
			String value = curNode.getProperty(jcrPropertyName).getString();
			if (value != null && !"".equals(value)) {
				curText.setText(value);
			}
		} catch (PathNotFoundException pnfe) {
			// Silent : property has not been initialized yet.
		} catch (RepositoryException re) {
			throw new ArgeoException(
					"Error while getting persisted value for property"
							+ jcrPropertyName, re);
		}
	}

	/**
	 * returns the default sensor name or null if none or an empty string has
	 * been entered
	 */
	public String getDefaultSensoreName() {
		String name = defaultSensorName.getText();

		if (name == null || "".equals(name))
			return null;
		else
			return name;
	}

	// Management of the param table
	private class ParamsTableContentProvider implements
			IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return ((List<Node>) inputElement).toArray();
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			paramsTableViewer.refresh();
		}
	}

	/** Select which parameter to use by editing a checkbox */
	protected class CheckboxEditingSupport extends EditingSupport {

		private final TableViewer viewer;
		private final AbstractFormPart formPart;

		public CheckboxEditingSupport(TableViewer viewer,
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
			return !isSessionAlreadyComplete();
		}

		@Override
		protected Object getValue(Object element) {
			try {
				Node curNode = (Node) element;
				return curNode.getProperty(CONNECT_PARAM_IS_USED).getBoolean();
			} catch (RepositoryException re) {
				throw new ArgeoException(
						"Cannot determine if node is already checked", re);
			}
		}

		@Override
		protected void setValue(Object element, Object value) {
			try {
				Node curNode = (Node) element;
				curNode.setProperty(CONNECT_PARAM_IS_USED, (Boolean) value);
				// curNode.getSession().save();
				formPart.markDirty();
			} catch (RepositoryException re) {
				throw new ArgeoException(
						"Cannot determine if node is already checked", re);
			}
			viewer.refresh();
		}
	}

	private class MinValueEditingSupport extends ParamBoundaryEditingSupport {

		public MinValueEditingSupport(TableViewer viewer,
				AbstractFormPart formPart) {
			super(viewer, formPart);
		}

		@Override
		protected Double getObjectFromNode(Node node)
				throws RepositoryException {
			return node.getProperty(CONNECT_PARAM_MIN_VALUE).getDouble();
		}

		@Override
		protected void setValueOnNode(Node node, Double value)
				throws RepositoryException {
			node.setProperty(CONNECT_PARAM_MIN_VALUE, value);
		}
	}

	private class MaxValueEditingSupport extends ParamBoundaryEditingSupport {

		public MaxValueEditingSupport(TableViewer viewer,
				AbstractFormPart formPart) {
			super(viewer, formPart);
		}

		@Override
		protected Double getObjectFromNode(Node node)
				throws RepositoryException {
			return node.getProperty(CONNECT_PARAM_MAX_VALUE).getDouble();
		}

		@Override
		protected void setValueOnNode(Node node, Double value)
				throws RepositoryException {
			node.setProperty(CONNECT_PARAM_MAX_VALUE, value);
		}
	}

	/** Modify min and max values */
	protected abstract class ParamBoundaryEditingSupport extends EditingSupport {

		protected final TableViewer viewer;
		protected final AbstractFormPart formPart;

		public ParamBoundaryEditingSupport(TableViewer viewer,
				AbstractFormPart formPart) {
			super(viewer);
			this.viewer = viewer;
			this.formPart = formPart;
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return new TextCellEditor(viewer.getTable());
		}

		@Override
		protected boolean canEdit(Object element) {
			return !isSessionAlreadyComplete();
		}

		/**
		 * To be overriden. Handling of repository exception is done on parent
		 * class
		 */
		abstract protected Double getObjectFromNode(Node node)
				throws RepositoryException;

		/**
		 * To be overriden. Handling of repository exception is done on parent
		 * class
		 */
		abstract protected void setValueOnNode(Node node, Double value)
				throws RepositoryException;

		@Override
		protected Object getValue(Object element) {
			try {
				Node curNode = (Node) element;
				return Double.toString(getObjectFromNode(curNode));
			} catch (RepositoryException re) {
				throw new ArgeoException("Cannot retrieve boundary for node",
						re);
			}
		}

		@Override
		protected void setValue(Object element, Object value) {
			try {
				Double dValue = Double.parseDouble((String) value);
				Node curNode = (Node) element;
				setValueOnNode(curNode, dValue);
				curNode.getSession().save();
				formPart.markDirty();
				viewer.refresh();
			} catch (NumberFormatException e) {
				throw new ArgeoException("Invalid number format : " + value, e);
			} catch (RepositoryException re) {
				throw new ArgeoException("Cannot set boudary value", re);
			}
		}
	}

}
