package org.argeo.connect.ui.gps.editors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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
import org.argeo.connect.ConnectNames;
import org.argeo.connect.ConnectTypes;
import org.argeo.connect.ui.gps.ConnectGpsLabels;
import org.argeo.connect.ui.gps.ConnectGpsUiPlugin;
import org.argeo.connect.ui.gps.GpsUiGisServices;
import org.argeo.connect.ui.gps.GpsUiJcrServices;
import org.argeo.connect.ui.gps.views.GpsBrowserView;
import org.argeo.eclipse.ui.ErrorFeedback;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

/**
 * Manages files to process with drag & drop capabilities and ability to set
 * both sensor and device name. Device name are still unused but can be persited
 * here.
 * 
 * Segment ids corresponding to distinct segments of a given GPX file are
 * retrieved and stored in the corresponding JCR Node after the processing.
 * 
 * 
 */
public class GpxFilesProcessingPage extends AbstractCleanDataEditorPage {
	// private final static Log log = LogFactory.getLog(DataSetPage.class);

	public final static String ID = "cleanDataEditor.gpxFilesProcessingPage";

	// This page widgets
	private TableViewer filesViewer;
	private FormToolkit tk;
	private Combo defaultSensorNameCmb;
	private Combo defaultDeviceNameCmb;

	// Shortcuts to increase readability of the code
	private GpsUiJcrServices uiJcrServices;
	private Node currCleanSession;

	// list of all nodes that have been dropped in the editor.
	// WARNING : key is the referenced file node ID, not the id of the
	// corresponding node under currentSessionNode
	private Map<String, Node> droppedNodes = new HashMap<String, Node>();

	// Rendering to differentiate new files from the already processed ones.
	// private final static Color grey = new Color(null, 200, 200, 200);
	// private Styler NOT_EDITABLE = new Styler() {
	// @Override
	// public void applyStyles(TextStyle textStyle) {
	// textStyle.foreground = grey;
	// textStyle.font = JFaceResources.getFontRegistry().getItalic(
	// JFaceResources.DEFAULT_FONT);
	// }
	// };
	// private Styler DEFAULT_FONT = new Styler() {
	// @Override
	// public void applyStyles(TextStyle textStyle) {
	// textStyle.foreground = new Color(null, 0, 0, 0);
	// textStyle.font = JFaceResources
	// .getFont(JFaceResources.DEFAULT_FONT);
	// }
	// };

	/** effective processing of a single GPX file */
	private void importOneNode(String refNodeId, IProgressMonitor monitor,
			Stats stats) {
		Binary binary = null;
		try {
			Session currJcrSession = currCleanSession.getSession();
			Node refNode = currJcrSession.getNodeByIdentifier(refNodeId);
			Node node = currJcrSession.getNodeByIdentifier(refNode.getProperty(
					ConnectNames.CONNECT_LINKED_FILE_REF).getString());
			String name = node.getName();
			monitor.subTask("Importing " + name);
			binary = node.getNode(Property.JCR_CONTENT)
					.getProperty(Property.JCR_DATA).getBinary();
			String cname = refNode
					.getProperty(ConnectNames.CONNECT_SENSOR_NAME).getString();

			GpsUiGisServices uiGisServices = getEditor().getUiGisServices();
			GpsUiJcrServices uiJcrServices = getEditor().getUiJcrServices();

			List<String> segmentUuids = (List<String>) uiGisServices
					.getTrackDao().importRawToCleanSession(
							uiJcrServices.getCleanSessionTechName(getEditor()
									.getCurrentCleanSession()), cname,
							binary.getStream());
			JcrUtils.closeQuietly(binary);

			// Finalization of the import
			String[] uuids = segmentUuids.toArray(new String[0]);
			refNode.setProperty(ConnectNames.CONNECT_SEGMENT_UUID, uuids);
			refNode.setProperty(ConnectNames.CONNECT_ALREADY_PROCESSED, true);
			currJcrSession.save();
			stats.nodeCount++;
			monitor.worked(1);
		} catch (RepositoryException e) {
			throw new ArgeoException("Cannot import GPS from node", e);
		} finally {
			JcrUtils.closeQuietly(binary);
		}
	}

	public GpxFilesProcessingPage(FormEditor editor, String title) {
		super(editor, ID, title);
	}

	protected void createFormContent(IManagedForm managedForm) {
		// initilize widgets and shortcuts
		ScrolledForm form = managedForm.getForm();
		tk = getManagedForm().getToolkit();
		Composite composite = form.getBody();
		composite.setLayout(new GridLayout(1, false));
		uiJcrServices = getEditor().getUiJcrServices();
		currCleanSession = getEditor().getCurrentCleanSession();

		// Fill the form with the different parts
		addDefaultNamesPart(composite);
		addFilesTablePart(composite);
		addButtonsPart(composite);
		initializePage();
	}

	private void addDefaultNamesPart(Composite composite) {
		Composite parent = tk.createComposite(composite);
		parent.setLayout(new GridLayout(4, false));

		GridData gd;
		DefaultNamesFormPart dnfPart = null;
		DefaultNamesComboListener dncListener;

		// Default sensor name
		tk.createLabel(parent, "Enter a default sensor name:");
		defaultSensorNameCmb = new Combo(parent, SWT.BORDER | SWT.V_SCROLL);
		gd = new GridData(SWT.LEFT, SWT.FILL, true, false);
		gd.widthHint = 200;
		defaultSensorNameCmb.setLayoutData(gd);
		populateCombo(defaultSensorNameCmb,
				ConnectNames.CONNECT_DEFAULT_SENSOR,
				ConnectNames.CONNECT_SENSOR_NAME);

		// Default sensor name
		tk.createLabel(parent, "Enter a default device name:");
		defaultDeviceNameCmb = new Combo(parent, SWT.BORDER | SWT.V_SCROLL);
		gd = new GridData(SWT.LEFT, SWT.FILL, true, false);
		gd.widthHint = 200;
		defaultDeviceNameCmb.setLayoutData(gd);
		populateCombo(defaultDeviceNameCmb,
				ConnectNames.CONNECT_DEFAULT_DEVICE,
				ConnectNames.CONNECT_DEVICE_NAME);

		dnfPart = new DefaultNamesFormPart();
		dncListener = new DefaultNamesComboListener(dnfPart);
		defaultSensorNameCmb.addModifyListener(dncListener);
		defaultDeviceNameCmb.addModifyListener(dncListener);
		getManagedForm().addPart(dnfPart);
	}

	private void populateCombo(Combo combo, String defaultPropertyName,
			String propertyName) {
		// we try to retrieve the list of the existing values to help end user
		try {
			Node currReferential = uiJcrServices
					.getLinkedReferential(currCleanSession);
			List<String> values = null;
			if (currReferential != null) {
				values = uiJcrServices.getCatalogFromRepo(currReferential,
						propertyName);
				values.addAll(uiJcrServices.getCatalogFromSession(
						currCleanSession, propertyName));
			}
			if (values != null && !values.isEmpty())
				combo.setItems(values.toArray(new String[0]));
			String curValue = null;
			if (currCleanSession.hasProperty(defaultPropertyName))
				curValue = currCleanSession.getProperty(defaultPropertyName)
						.getString();
			if (curValue != null) {
				if (!values.contains(curValue))
					combo.add(curValue);
				combo.select(combo.indexOf(curValue));
			}
		} catch (RepositoryException re) {
			throw new ArgeoException(
					"unexpected error retrieving existing default names.", re);
		}
	}

	// Inner classes to handle param changes
	private class DefaultNamesFormPart extends AbstractFormPart {
		public void commit(boolean onSave) {
			if (onSave)
				try {
					if (defaultSensorNameCmb.getText() != null)
						currCleanSession.setProperty(
								ConnectNames.CONNECT_DEFAULT_SENSOR,
								defaultSensorNameCmb.getText());
					if (defaultDeviceNameCmb.getText() != null)
						currCleanSession.setProperty(
								ConnectNames.CONNECT_DEFAULT_DEVICE,
								defaultDeviceNameCmb.getText());
					super.commit(onSave);
				} catch (RepositoryException re) {
					throw new ArgeoException(
							"unexpected error while saving default names.", re);
				}
		}
	}

	private class DefaultNamesComboListener implements ModifyListener {
		private DefaultNamesFormPart formPart;

		public DefaultNamesComboListener(DefaultNamesFormPart formPart) {
			this.formPart = formPart;
		}

		@Override
		public void modifyText(ModifyEvent e) {
			formPart.markDirty();
		}
	};

	// Manage the files to import table
	private Section addFilesTablePart(Composite parent) {

		// Corresponding Section
		Section section = tk.createSection(parent, Section.TITLE_BAR);
		section.setText(ConnectGpsUiPlugin
				.getGPSMessage(ConnectGpsLabels.IMPORT_FILE_SECTION_TITLE));
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
					if (cnode.getProperty(ConnectNames.CONNECT_TO_BE_PROCESSED)
							.getBoolean())
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
		column.setLabelProvider(new FilesTableLabelProvider());
		// new StyledCellLabelProvider() {
		// public void update(final ViewerCell cell) {
		// try {
		// Node cnode = (Node) cell.getElement();
		// String currentText = cnode.getName();
		// StyledString styledString;
		// if (canEditLine(cnode))
		// styledString = new StyledString(currentText,
		// DEFAULT_FONT);
		// else
		// styledString = new StyledString(currentText,
		// NOT_EDITABLE);
		// cell.setText(styledString.getString());
		// cell.setStyleRanges(styledString.getStyleRanges());
		// } catch (RepositoryException re) {
		// throw new ArgeoException("Problem getting sensor name", re);
		// }
		// }
		// });

		// Sensor name column
		column = createTableViewerColumn(filesViewer, "Sensor", 200);
		column.setLabelProvider(new FilesTableLabelProvider());
		// new StyledCellLabelProvider() {
		// public void update(final ViewerCell cell) {
		// try {
		// Node cnode = (Node) cell.getElement();
		// String currentText = cnode.getProperty(
		// ConnectNames.CONNECT_SENSOR_NAME).getString();
		// StyledString styledString;
		// if (canEditLine(cnode))
		// styledString = new StyledString(currentText,
		// DEFAULT_FONT);
		// else
		// styledString = new StyledString(currentText,
		// NOT_EDITABLE);
		// cell.setText(styledString.getString());
		// cell.setStyleRanges(styledString.getStyleRanges());
		// } catch (RepositoryException re) {
		// throw new ArgeoException("Problem getting sensor name", re);
		// }
		// }
		// });
		column.setEditingSupport(new SensorNameEditingSupport(filesViewer));

		// Device name column
		column = createTableViewerColumn(filesViewer, "Device", 200);
		column.setLabelProvider(new FilesTableLabelProvider());
		// new StyledCellLabelProvider() {
		// public void update(final ViewerCell cell) {
		// try {
		// Node cnode = (Node) cell.getElement();
		// String currentText = cnode.getProperty(
		// ConnectNames.CONNECT_DEVICE_NAME).getString();
		// StyledString styledString;
		// if (canEditLine(cnode))
		// styledString = new StyledString(currentText,
		// DEFAULT_FONT);
		// else
		// styledString = new StyledString(currentText,
		// NOT_EDITABLE);
		// cell.setText(styledString.getString());
		// cell.setStyleRanges(styledString.getStyleRanges());
		// } catch (RepositoryException re) {
		// throw new ArgeoException("Problem getting sensor name", re);
		// }
		// }
		// });
		column.setEditingSupport(new DeviceNameEditingSupport(filesViewer));

		filesViewer.setContentProvider(new NodesContentProvider());
		filesViewer.setInput(droppedNodes);
		return null;
	}

	private class FilesTableLabelProvider extends CellLabelProvider {

		private static final int COLUMN_CHK_BOX = 0;
		private static final int COLUMN_FILE_NAME = 1;
		private static final int COLUMN_SENSOR = 2;
		private static final int COLUMN_DEVICE = 3;

		private final Device device;
		private final Font normalFont;
		private final Font alreadyImportedFont;
		private final Color grey = new Color(null, 200, 200, 200);
		private final Color black = new Color(null, 0, 0, 0);

		// private final Color normalColor;
		// private final Color alreadyImportedColor;

		FilesTableLabelProvider() {
			super();
			this.device = ConnectGpsUiPlugin.getDefault().getWorkbench()
					.getDisplay();
			this.normalFont = createFont("Times New Roman", 13, SWT.BOLD);
			// this.normalColor = Graphics.getColor(255, 0, 0);
			this.alreadyImportedFont = createFont("Times New Roman", 13,
					SWT.ITALIC);
			// this.alreadyImportedColor = Graphics.getColor(127, 127, 127);
		}

		@Override
		public void update(ViewerCell cell) {
			try {

				int columnIndex = cell.getColumnIndex();
				Node cnode = (Node) cell.getElement();
				String currText = null;
				switch (columnIndex) {
				case COLUMN_CHK_BOX:
					throw new ArgeoException("This label provider"
							+ " is not intended to be used with the checkbox");
				case COLUMN_FILE_NAME:
					currText = cnode.getName();
					break;
				case COLUMN_SENSOR:
					currText = cnode.getProperty(
							ConnectNames.CONNECT_SENSOR_NAME).getString();
					break;
				case COLUMN_DEVICE:
					currText = cnode.getProperty(
							ConnectNames.CONNECT_DEVICE_NAME).getString();
					break;
				}
				cell.setText(currText);
				cell.setFont(alreadyImportedFont);
				cell.setForeground(grey);

			} catch (RepositoryException re) {
				throw new ArgeoException(
						"Error while retrieving file properties.", re);
			}
		}

		private Font createFont(String name, int size, int style) {
			FontData fontData = new FontData(name, size, style);
			return new Font(device, fontData);
		}
	}

	private void addButtonsPart(Composite parent) {
		// Launch effective import button
		Button launchImport = tk.createButton(parent, ConnectGpsUiPlugin
				.getGPSMessage(ConnectGpsLabels.LAUNCH_IMPORT_BUTTON_LBL),
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
			return sort(((Map<String, Node>) inputElement).values().toArray());
		}

		public void dispose() {
		}

		protected Object[] sort(Object[] elements) {
			Arrays.sort(elements, new Comparator<Object>() {

				@Override
				public int compare(Object o1, Object o2) {
					Node node1 = (Node) o1;
					Node node2 = (Node) o2;
					try {
						return node1.getPath().compareTo(node2.getPath());
					} catch (RepositoryException e) {
						throw new ArgeoException("Cannot compare " + node1
								+ " and " + node2, e);
					}
				}

			});
			return elements;
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			filesViewer.refresh();
		}
	}

	/* EDITING SUPPORT */
	private abstract class NameEditingSupport extends EditingSupport {

		protected final TableViewer viewer;

		public NameEditingSupport(TableViewer viewer) {
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
	}

	/** Modify the sensor Name */
	protected class SensorNameEditingSupport extends NameEditingSupport {

		public SensorNameEditingSupport(TableViewer viewer) {
			super(viewer);
		}

		@Override
		protected Object getValue(Object element) {
			try {
				Node curNode = (Node) element;
				return curNode.getProperty(ConnectNames.CONNECT_SENSOR_NAME)
						.getString();
			} catch (RepositoryException re) {
				throw new ArgeoException("Cannot retrieve sensor name", re);
			}
		}

		@Override
		protected void setValue(Object element, Object value) {
			try {
				Node curNode = (Node) element;
				curNode.setProperty(ConnectNames.CONNECT_SENSOR_NAME,
						(String) value);
				curNode.getSession().save();
				viewer.refresh();
			} catch (RepositoryException re) {
				throw new ArgeoException("Cannot update sensor name", re);
			}
		}
	}

	/** Modify the device name */
	protected class DeviceNameEditingSupport extends NameEditingSupport {

		public DeviceNameEditingSupport(TableViewer viewer) {
			super(viewer);
		}

		@Override
		protected Object getValue(Object element) {
			try {
				Node curNode = (Node) element;
				return curNode.getProperty(ConnectNames.CONNECT_DEVICE_NAME)
						.getString();
			} catch (RepositoryException re) {
				throw new ArgeoException("Cannot retrieve device name", re);
			}
		}

		@Override
		protected void setValue(Object element, Object value) {
			try {
				Node curNode = (Node) element;
				curNode.setProperty(ConnectNames.CONNECT_DEVICE_NAME,
						(String) value);
				curNode.getSession().save();
				viewer.refresh();
			} catch (RepositoryException re) {
				throw new ArgeoException("Cannot update device name ", re);
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
				return curNode
						.getProperty(ConnectNames.CONNECT_TO_BE_PROCESSED)
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
				curNode.setProperty(ConnectNames.CONNECT_TO_BE_PROCESSED,
						(Boolean) value);
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
			if (defaultSensorNameCmb.getText() == null
					|| "".equals(defaultSensorNameCmb.getText().trim())) {
				ErrorFeedback.show("Please enter a default sensor name");
				return false;
			}

			// check if the Metadata Page is dirty
			if (getEditor().findPage(ID).isDirty()) {
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
				Session session = getEditor().getCurrentCleanSession()
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
		Node sessionNode = getEditor().getCurrentCleanSession();
		try {
			String fileName = node.getName();

			// we name nodes based on the name of the file they reference
			Node fileNode = sessionNode.addNode(fileName,
					ConnectTypes.CONNECT_FILE_TO_IMPORT);
			fileNode.setProperty(ConnectNames.CONNECT_LINKED_FILE_REF, refId);
			fileNode.setProperty(ConnectNames.CONNECT_SENSOR_NAME, sessionNode
					.getProperty(ConnectNames.CONNECT_DEFAULT_SENSOR)
					.getString());
			fileNode.setProperty(
					ConnectNames.CONNECT_DEVICE_NAME,
					sessionNode
							.hasProperty(ConnectNames.CONNECT_DEFAULT_DEVICE) ? sessionNode
							.getProperty(ConnectNames.CONNECT_DEFAULT_DEVICE)
							.getString() : "");
			sessionNode.getSession().save();
			droppedNodes.put(refId, fileNode);
		} catch (RepositoryException e) {
			throw new ArgeoException(
					"Error creating a new linked file node for session ", e);
		}

	}

	/** Initialize page */
	private void initializePage() {
		Node sessionNode = getEditor().getCurrentCleanSession();

		// Handle existing file references
		try {
			NodeIterator ni = sessionNode.getNodes();

			while (ni.hasNext()) {
				Node curNode = ni.nextNode();
				if (curNode.getPrimaryNodeType().isNodeType(
						ConnectTypes.CONNECT_FILE_TO_IMPORT)) {
					String id = curNode.getProperty(
							ConnectNames.CONNECT_LINKED_FILE_REF).getString();
					droppedNodes.put(id, curNode);
				}
			}
			filesViewer.setInput(droppedNodes);
		} catch (RepositoryException e) {
			throw new ArgeoException("Unexpected error while "
					+ "initializing the GPX files table.", e);
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
				if (node.getProperty(ConnectNames.CONNECT_TO_BE_PROCESSED)
						.getBoolean()
						&& !node.getProperty(
								ConnectNames.CONNECT_ALREADY_PROCESSED)
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
			GpsBrowserView gbView = (GpsBrowserView) ConnectGpsUiPlugin
					.getDefault().getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().findView(GpsBrowserView.ID);
			gbView.refresh(getEditor().getCurrentCleanSession());

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

	private boolean canEditLine(Node node) {
		try {
			// Cannot edit a completed session
			if (isSessionAlreadyComplete())
				return false;
			return !node.getProperty(ConnectNames.CONNECT_ALREADY_PROCESSED)
					.getBoolean();
		} catch (RepositoryException re) {
			throw new ArgeoException(
					"Cannot access node to see if it has already been imported.");
		}
	}

	private Shell getShell() {
		return ConnectGpsUiPlugin.getDefault().getWorkbench()
				.getActiveWorkbenchWindow().getShell();
	}

	static class Stats {
		public Long nodeCount = 0l;
	}
}