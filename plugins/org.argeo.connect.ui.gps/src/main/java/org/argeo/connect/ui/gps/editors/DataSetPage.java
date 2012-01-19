package org.argeo.connect.ui.gps.editors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.ArgeoException;
import org.argeo.connect.ui.gps.ConnectUiGpsPlugin;
import org.argeo.eclipse.ui.ErrorFeedback;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

public class DataSetPage extends AbstractCleanDataEditorPage {
	// private final static Log log = LogFactory.getLog(DataSetPage.class);
	public final static String ID = "cleanDataEditor.dataSetPage";

	// The main table.
	private TableViewer filesViewer;

	private FormToolkit tk;
	// Rendering
	private final static Color grey = new Color(null, 200, 200, 200);
	private Styler NOT_EDITABLE = new Styler() {
		@Override
		public void applyStyles(TextStyle textStyle) {
			textStyle.foreground = grey;
			textStyle.font = JFaceResources.getFontRegistry().getItalic(
					JFaceResources.DEFAULT_FONT);
		}
	};
	private Styler DEFAULT_FONT = new Styler() {
		@Override
		public void applyStyles(TextStyle textStyle) {
			textStyle.foreground = new Color(null, 0, 0, 0);
			textStyle.font = JFaceResources
					.getFont(JFaceResources.DEFAULT_FONT);
		}
	};

	// list of all nodes that have been dropped in the editor.
	// WARNING : key is the referenced file node ID, not the id of the
	// corresponding node under currentSessionNode
	private Map<String, Node> droppedNodes = new HashMap<String, Node>();

	public DataSetPage(FormEditor editor, String title) {
		super(editor, ID, title);
	}

	protected void createFormContent(IManagedForm managedForm) {
		ScrolledForm form = managedForm.getForm();
		tk = getManagedForm().getToolkit();
		Composite composite = form.getBody();
		composite.setLayout(new GridLayout(1, false));

		// Fill the form with the different parts
		addFilesTablePart(composite);
		addButtonsPart(composite);

		// Initialize with persisted values.
		initializePage();
	}

	// Manage the files to import table
	private Section addFilesTablePart(Composite parent) {

		// Corresponding Section
		Section section = tk.createSection(parent, Section.TITLE_BAR);
		section.setText(ConnectUiGpsPlugin
				.getGPSMessage(IMPORT_FILE_SECTION_TITLE));
		Composite body = tk.createComposite(section, SWT.WRAP);
		body.setLayout(new GridLayout(1, false));
		section.setClient(body);

		// Create the table
		Table table = tk.createTable(body, SWT.NONE | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.BORDER);

		GridData gridData = new GridData(SWT.LEFT, SWT.FILL, false, true);
		gridData.heightHint = 400;
		table.setLayoutData(gridData);

		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		filesViewer = new TableViewer(table);

		// Add Drop support
		int operations = DND.DROP_COPY | DND.DROP_MOVE;
		Transfer[] tt = new Transfer[] { TextTransfer.getInstance() };
		filesViewer.addDropSupport(operations, tt, new ViewDropListener(
				filesViewer));

		// Check boxes column
		TableViewerColumn column = createTableViewerColumn(filesViewer, "", 20);
		column.setLabelProvider(new ColumnLabelProvider() {
			public String getText(Object element) {
				return null;
			}

			public Image getImage(Object element) {
				try {
					Node cnode = (Node) element;
					if (!canEditLine(cnode))
						return null;
					if (cnode.getProperty(CONNECT_TO_BE_PROCESSED).getBoolean())
						return CHECKED;
					else
						return UNCHECKED;
				} catch (RepositoryException re) {
					throw new ArgeoException("Problem getting node property ",
							re);
				}

			}
		});
		column.setEditingSupport(new CheckboxEditingSupport(filesViewer));

		// File name column
		column = createTableViewerColumn(filesViewer, "Files", 200);
		column.setLabelProvider(new StyledCellLabelProvider() {
			public void update(final ViewerCell cell) {
				try {
					Node cnode = (Node) cell.getElement();
					String currentText = cnode.getName();
					StyledString styledString;
					if (canEditLine(cnode))
						styledString = new StyledString(currentText,
								DEFAULT_FONT);
					else
						styledString = new StyledString(currentText,
								NOT_EDITABLE);
					cell.setText(styledString.getString());
					cell.setStyleRanges(styledString.getStyleRanges());
				} catch (RepositoryException re) {
					throw new ArgeoException("Problem getting sensor name", re);
				}
			}
		});

		// Sensor name column
		column = createTableViewerColumn(filesViewer, "Sensor", 200);
		column.setLabelProvider(new StyledCellLabelProvider() {
			public void update(final ViewerCell cell) {
				try {
					Node cnode = (Node) cell.getElement();
					String currentText = cnode.getProperty(CONNECT_SENSOR_NAME)
							.getString();
					StyledString styledString;
					if (canEditLine(cnode))
						styledString = new StyledString(currentText,
								DEFAULT_FONT);
					else
						styledString = new StyledString(currentText,
								NOT_EDITABLE);
					cell.setText(styledString.getString());
					cell.setStyleRanges(styledString.getStyleRanges());
				} catch (RepositoryException re) {
					throw new ArgeoException("Problem getting sensor name", re);
				}
			}
		});
		column.setEditingSupport(new SensorNameEditingSupport(filesViewer));

		filesViewer.setContentProvider(new NodesContentProvider());
		filesViewer.setInput(droppedNodes);
		return null;
	}

	private void addButtonsPart(Composite parent) {

		// Launch effective import button
		Button launchImport = tk.createButton(parent,
				ConnectUiGpsPlugin.getGPSMessage(LAUNCH_IMPORT_BUTTON_LBL),
				SWT.PUSH);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.BEGINNING;
		launchImport.setLayoutData(gridData);

		Listener launchListener = new Listener() {
			public void handleEvent(Event event) {
				performEffectiveFileImport();
			}
		};

		launchImport.addListener(SWT.Selection, launchListener);
	}

	private class NodesContentProvider implements IStructuredContentProvider {
		@SuppressWarnings("unchecked")
		public Object[] getElements(Object inputElement) {
			return ((Map<String, Node>) inputElement).values().toArray();
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			filesViewer.refresh();
		}
	}

	/** Modify the sensor Name */
	protected class SensorNameEditingSupport extends EditingSupport {

		private final TableViewer viewer;

		public SensorNameEditingSupport(TableViewer viewer) {
			super(viewer);
			this.viewer = viewer;
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return new TextCellEditor(viewer.getTable());
		}

		@Override
		protected boolean canEdit(Object element) {
			return canEditLine((Node) element);
		}

		@Override
		protected Object getValue(Object element) {
			try {
				Node curNode = (Node) element;
				return curNode.getProperty(CONNECT_SENSOR_NAME).getString();
			} catch (RepositoryException re) {
				throw new ArgeoException("Cannot retrieve sensore name", re);
			}
		}

		@Override
		protected void setValue(Object element, Object value) {
			try {
				Node curNode = (Node) element;
				curNode.setProperty(CONNECT_SENSOR_NAME, (String) value);
				curNode.getSession().save();
				viewer.refresh();
			} catch (RepositoryException re) {
				throw new ArgeoException(
						"Cannot determine if node is already checked", re);
			}
		}
	}

	/** Select which file to import by editing a checkbox */
	protected class CheckboxEditingSupport extends EditingSupport {

		private final TableViewer viewer;

		public CheckboxEditingSupport(TableViewer viewer) {
			super(viewer);
			this.viewer = viewer;
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return new CheckboxCellEditor(null, SWT.CHECK | SWT.READ_ONLY);
		}

		@Override
		protected boolean canEdit(Object element) {
			return canEditLine((Node) element);
		}

		@Override
		protected Object getValue(Object element) {
			try {
				Node curNode = (Node) element;
				return curNode.getProperty(CONNECT_TO_BE_PROCESSED)
						.getBoolean();
			} catch (RepositoryException re) {
				throw new ArgeoException(
						"Cannot determine if node is already checked", re);
			}
		}

		@Override
		protected void setValue(Object element, Object value) {
			try {
				Node curNode = (Node) element;
				curNode.setProperty(CONNECT_TO_BE_PROCESSED, (Boolean) value);
				curNode.getSession().save();
			} catch (RepositoryException re) {
				throw new ArgeoException(
						"Cannot determine if node is already checked", re);
			}
			viewer.refresh();
		}
	}

	// Implementation of the Drop Listener
	protected class ViewDropListener extends ViewerDropAdapter {

		public ViewDropListener(Viewer viewer) {
			super(viewer);
		}

		@Override
		public boolean performDrop(Object data) {
			// Check if a default sensor name has already been entered.
			if (getEditor().getDefaultSensorName() == null) {
				ErrorFeedback.show("Please enter a default sensor name");
				getEditor().setActivePage(SessionMetaDataPage.ID);
				return false;
			}

			// check if the Metadata Page is dirty
			if (getEditor().findPage(SessionMetaDataPage.ID).isDirty()) {
				ErrorFeedback
						.show("Please save Metadata before starting file import process.");
				return false;
			}

			// check if the session is read only
			FormPage page = (FormPage) getManagedForm().getContainer();
			boolean enabled = page.getPartControl().getEnabled();
			if (!enabled) {
				ErrorFeedback
						.show("This session has already been commited and closed. No more file can be added.");
				return false;
			}
			try {

				// parse data :
				String[] ids = ((String) data).split(";");
				Session session = getEditor().getCurrentSessionNode()
						.getSession();

				for (int i = 0; i < ids.length; i++) {
					if (!droppedNodes.containsKey(ids[i])) {
						Node node = session.getNodeByIdentifier(ids[i]);
						addFileNode(node, ids[i]);

					}
				}
				getViewer().refresh();
				// resize the table if needed.
				getViewer().getControl().getParent().layout();
			} catch (RepositoryException e) {
				throw new ArgeoException("Error while parsing Dropped data", e);
			}

			return true;
		}

		@Override
		public boolean validateDrop(Object target, int operation,
				TransferData transferType) {
			return true;
		}
	}

	/** Add a new file node to current Session */
	private void addFileNode(Node node, String refId) {
		Node sessionNode = getEditor().getCurrentSessionNode();

		try {
			String fileName = node.getName();

			// we name nodes based on the name of the file they reference
			Node fileNode = sessionNode.addNode(fileName,
					CONNECT_FILE_TO_IMPORT);
			fileNode.setProperty(CONNECT_LINKED_FILE_REF, refId);
			fileNode.setProperty(CONNECT_SENSOR_NAME,
					sessionNode.getProperty(CONNECT_DEFAULT_SENSOR).getString());
			sessionNode.getSession().save();
			droppedNodes.put(refId, fileNode);
		} catch (RepositoryException e) {
			throw new ArgeoException(
					"Error creating a new linked file node for session ", e);
		}

	}

	/** Initialize page */
	private void initializePage() {
		Node sessionNode = getEditor().getCurrentSessionNode();

		// Handle existing file references
		try {
			NodeIterator ni = sessionNode.getNodes();

			while (ni.hasNext()) {
				Node curNode = ni.nextNode();
				if (curNode.getPrimaryNodeType().isNodeType(
						CONNECT_FILE_TO_IMPORT)) {
					String id = curNode.getProperty(CONNECT_LINKED_FILE_REF)
							.getString();
					droppedNodes.put(id, curNode);
				}
			}
			filesViewer.setInput(droppedNodes);
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Return ID of the nodes under Session node that contains the reference to
	 * the file to import node
	 */
	private List<String> getIdsToImport() {
		List<String> ids = new ArrayList<String>();
		for (Node node : droppedNodes.values()) {
			try {
				if (node.getProperty(CONNECT_TO_BE_PROCESSED).getBoolean()
						&& !node.getProperty(CONNECT_ALREADY_PROCESSED)
								.getBoolean()) {
					ids.add(node.getIdentifier());
				}
			} catch (RepositoryException e) {
				throw new ArgeoException(
						"Error while getting list of Ebi to import ids");
			}
		}
		return ids;
	}

	private boolean performEffectiveFileImport() {

		Boolean failed = false;
		final List<String> ids = getIdsToImport();
		final int nodeSize = ids.size();
		// final Map<String, Node> nodesToImport = constructNodeToImport(ids);

		final Stats stats = new Stats();
		Long begin = System.currentTimeMillis();
		try {

			ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());

			dialog.run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) {
					monitor.beginTask("Importing GPX nodes", nodeSize);
					Iterator<String> it = ids.iterator();
					while (it.hasNext()) {
						importOneNode(it.next(), monitor, stats);
					}
					monitor.done();
				}
			});
			filesViewer.refresh();

		} catch (Exception e) {
			ErrorFeedback.show("Cannot import GPX nodes", e);
			failed = true;
		}

		Long duration = System.currentTimeMillis() - begin;
		Long durationS = duration / 1000l;
		String durationStr = (durationS / 60) + " min " + (durationS % 60)
				+ " s";
		StringBuffer message = new StringBuffer("Imported\n");
		message.append(stats.nodeCount).append(" nodes\n");
		if (failed)
			message.append(" of planned ").append(nodeSize);
		message.append("\n");
		message.append("in ").append(durationStr).append("\n");
		if (failed)
			MessageDialog.openError(getShell(), "Import failed",
					message.toString());
		else
			MessageDialog.openInformation(getShell(), "Import successful",
					message.toString());
		return true;
	}

	private boolean importOneNode(String refNodeId, IProgressMonitor monitor,
			Stats stats) {
		Binary binary = null;
		try {

			Session curSession = getEditor().getCurrentSessionNode()
					.getSession();

			Node refNode = curSession.getNodeByIdentifier(refNodeId);
			Node node = curSession.getNodeByIdentifier(refNode.getProperty(
					CONNECT_LINKED_FILE_REF).getString());
			String name = node.getName();
			monitor.subTask("Importing " + name);
			binary = node.getNode(Property.JCR_CONTENT)
					.getProperty(Property.JCR_DATA).getBinary();

			String cname = refNode.getProperty(CONNECT_SENSOR_NAME).getString();

			getEditor().getTrackDao().importRawToCleanSession(
					getCleanSession(), cname, binary.getStream());
			JcrUtils.closeQuietly(binary);

			// Finalization of the import / UI updates
			refNode.setProperty(CONNECT_ALREADY_PROCESSED, true);
			curSession.save();

			stats.nodeCount++;
			monitor.worked(1);
		} catch (RepositoryException e) {
			throw new ArgeoException("Cannot import GPS from node", e);
		} finally {
			JcrUtils.closeQuietly(binary);
		}
		return true;
	}

	private boolean canEditLine(Node node) {
		try {
			// Cannot edit a completed session
			if (isSessionAlreadyComplete())
				return false;
			return !node.getProperty(CONNECT_ALREADY_PROCESSED).getBoolean();
		} catch (RepositoryException re) {
			throw new ArgeoException(
					"Cannot access node to see if it has already been imported.");
		}
	}

	private Shell getShell() {
		return ConnectUiGpsPlugin.getDefault().getWorkbench()
				.getActiveWorkbenchWindow().getShell();
	}

	static class Stats {
		public Long nodeCount = 0l;
	}

}
