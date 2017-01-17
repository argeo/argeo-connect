package org.argeo.cms.ui.fs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.ConnectException;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.eclipse.ui.dialogs.SingleValue;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/** Generic popup context menu to manage NIO Path in a Viewer. */
public class FsContextMenu extends Shell {
	private static final long serialVersionUID = -9120261153509855795L;

	// Default known actions
	public final static String ACTION_ID_CREATE_FOLDER = "createFolder";
	public final static String ACTION_ID_SHARE_FOLDER = "shareFolder";
	public final static String ACTION_ID_DOWNLOAD_FOLDER = "downloadFolder";
	public final static String ACTION_ID_DELETE = "delete";
	public final static String ACTION_ID_UPLOAD_FILE = "uploadFiles";
	public final static String ACTION_ID_OPEN = "open";

	// Local context
	private final Viewer viewer;
	private final static String KEY_ACTION_ID = "actionId";
	private final static String[] DEFAULT_ACTIONS = { ACTION_ID_CREATE_FOLDER, ACTION_ID_SHARE_FOLDER,
			ACTION_ID_DOWNLOAD_FOLDER, ACTION_ID_DELETE, ACTION_ID_UPLOAD_FILE, ACTION_ID_OPEN };
	private Map<String, Button> actionButtons = new HashMap<String, Button>();

	private Path currFolderPath;

	public FsContextMenu(Viewer viewer, Display display) {
		super(display, SWT.NO_TRIM | SWT.BORDER | SWT.ON_TOP);
		this.viewer = viewer;
		setLayout(EclipseUiUtils.noSpaceGridLayout());

		Composite boxCmp = new Composite(this, SWT.NO_FOCUS | SWT.BORDER);
		boxCmp.setLayout(EclipseUiUtils.noSpaceGridLayout());
		CmsUtils.style(boxCmp, "contextMenu_box");
		createContextMenu(boxCmp);

		addShellListener(new ActionsShellListener());
	}

	protected void createContextMenu(Composite boxCmp) {
		ActionsSelListener asl = new ActionsSelListener();
		for (String actionId : DEFAULT_ACTIONS) {
			Button btn = new Button(boxCmp, SWT.FLAT | SWT.PUSH | SWT.LEAD);
			btn.setText(getLabel(actionId));
			btn.setLayoutData(EclipseUiUtils.fillWidth());
			CmsUtils.markup(btn);
			CmsUtils.style(btn, actionId + "_btn");
			btn.setData(KEY_ACTION_ID, actionId);
			btn.addSelectionListener(asl);
			actionButtons.put(actionId, btn);
		}
	}

	protected String getLabel(String actionId) {
		switch (actionId) {
		case ACTION_ID_CREATE_FOLDER:
			return "Create Folder";
		case ACTION_ID_SHARE_FOLDER:
			return "Share Folder";
		case ACTION_ID_DOWNLOAD_FOLDER:
			return "Download as zip archive";
		case ACTION_ID_DELETE:
			return "Delete";
		case ACTION_ID_UPLOAD_FILE:
			return "Upload Files";
		case ACTION_ID_OPEN:
			return "Open";
		default:
			throw new IllegalArgumentException("Unknown action ID " + actionId);
		}
	}

	protected void aboutToShow(Control source, Point location) {
		IStructuredSelection selection = ((IStructuredSelection) viewer.getSelection());
		boolean emptySel = true;
		boolean multiSel = false;
		boolean isFolder = true;
		if (selection != null && !selection.isEmpty()) {
			emptySel = false;
			multiSel = selection.size() > 1;
			if (!multiSel && selection.getFirstElement() instanceof Path) {
				isFolder = Files.isDirectory((Path) selection.getFirstElement());
			}
		}
		if (emptySel) {
			setVisible(true, ACTION_ID_CREATE_FOLDER, ACTION_ID_UPLOAD_FILE);
			setVisible(false, ACTION_ID_SHARE_FOLDER, ACTION_ID_DOWNLOAD_FOLDER, ACTION_ID_DELETE, ACTION_ID_OPEN);
		} else if (multiSel) {
			setVisible(true, ACTION_ID_DELETE);
			setVisible(false, ACTION_ID_SHARE_FOLDER, ACTION_ID_DOWNLOAD_FOLDER, ACTION_ID_CREATE_FOLDER,
					ACTION_ID_UPLOAD_FILE, ACTION_ID_OPEN);
		} else if (isFolder) {
			setVisible(true, ACTION_ID_DELETE, ACTION_ID_SHARE_FOLDER, ACTION_ID_DOWNLOAD_FOLDER);
			setVisible(false, ACTION_ID_CREATE_FOLDER, ACTION_ID_UPLOAD_FILE, ACTION_ID_OPEN);
		} else {
			setVisible(true, ACTION_ID_DELETE, ACTION_ID_OPEN);
			setVisible(false, ACTION_ID_SHARE_FOLDER, ACTION_ID_DOWNLOAD_FOLDER, ACTION_ID_CREATE_FOLDER,
					ACTION_ID_UPLOAD_FILE);
		}
	}

	private void setVisible(boolean visible, String... buttonIds) {
		for (String id : buttonIds) {
			Button button = actionButtons.get(id);
			button.setVisible(visible);
			GridData gd = (GridData) button.getLayoutData();
			gd.heightHint = visible ? SWT.DEFAULT : 0;
		}
	}

	public void show(Control source, Point location, Path currFolderPath) {
		if (isVisible())
			setVisible(false);
		// TODO find a better way to retrieve the parent path (cannot be deduced
		// from table content because it will fail on an empty folder)
		this.currFolderPath = currFolderPath;
		aboutToShow(source, location);
		pack();
		layout();
		if (source instanceof Control)
			setLocation(((Control) source).toDisplay(location.x, location.y));
		open();
	}

	class StyleButton extends Label {
		private static final long serialVersionUID = 7731102609123946115L;

		public StyleButton(Composite parent, int swtStyle) {
			super(parent, swtStyle);
		}

	}

	// class ActionsMouseListener extends MouseAdapter {
	// private static final long serialVersionUID = -1041871937815812149L;
	//
	// @Override
	// public void mouseDown(MouseEvent e) {
	// Object eventSource = e.getSource();
	// if (e.button == 1) {
	// if (eventSource instanceof Button) {
	// Button pressedBtn = (Button) eventSource;
	// String actionId = (String) pressedBtn.getData(KEY_ACTION_ID);
	// switch (actionId) {
	// case ACTION_ID_CREATE_FOLDER:
	// createFolder();
	// break;
	// case ACTION_ID_DELETE:
	// deleteItems();
	// break;
	// default:
	// throw new IllegalArgumentException("Unimplemented action " + actionId);
	// // case ACTION_ID_SHARE_FOLDER:
	// // return "Share Folder";
	// // case ACTION_ID_DOWNLOAD_FOLDER:
	// // return "Download as zip archive";
	// // case ACTION_ID_UPLOAD_FILE:
	// // return "Upload Files";
	// // case ACTION_ID_OPEN:
	// // return "Open";
	// }
	// }
	// }
	// viewer.getControl().setFocus();
	// // setVisible(false);
	// }
	// }

	class ActionsSelListener extends SelectionAdapter {
		private static final long serialVersionUID = -1041871937815812149L;

		@Override
		public void widgetSelected(SelectionEvent e) {
			Object eventSource = e.getSource();
			if (eventSource instanceof Button) {
				Button pressedBtn = (Button) eventSource;
				String actionId = (String) pressedBtn.getData(KEY_ACTION_ID);
				switch (actionId) {
				case ACTION_ID_CREATE_FOLDER:
					createFolder();
					break;
				case ACTION_ID_DELETE:
					deleteItems();
					break;
				default:
					throw new IllegalArgumentException("Unimplemented action " + actionId);
					// case ACTION_ID_SHARE_FOLDER:
					// return "Share Folder";
					// case ACTION_ID_DOWNLOAD_FOLDER:
					// return "Download as zip archive";
					// case ACTION_ID_UPLOAD_FILE:
					// return "Upload Files";
					// case ACTION_ID_OPEN:
					// return "Open";
				}
			}
			viewer.getControl().setFocus();
			// setVisible(false);

		}
	}

	class ActionsShellListener extends org.eclipse.swt.events.ShellAdapter {
		private static final long serialVersionUID = -5092341449523150827L;

		// @Override
		// public void shellActivated(ShellEvent e) {
		// }

		@Override
		public void shellDeactivated(ShellEvent e) {
			setVisible(false);
		}
	}

	private void deleteItems() {
		IStructuredSelection selection = ((IStructuredSelection) viewer.getSelection());
		if (selection.isEmpty())
			return;

		StringBuilder builder = new StringBuilder();
		@SuppressWarnings("unchecked")
		Iterator<Object> iterator = selection.iterator();
		List<Path> paths = new ArrayList<>();

		while (iterator.hasNext()) {
			Path path = (Path) iterator.next();
			builder.append(path.getFileName() + ", ");
			paths.add(path);
		}
		String msg = "You are about to delete following elements: " + builder.substring(0, builder.length() - 2)
				+ ". Are you sure?";
		if (MessageDialog.openConfirm(this, "Confirm deletion", msg)) {
			for (Path path : paths) {
				try {
					// Might have already been deleted if we are in a tree
					Files.deleteIfExists(path);
				} catch (IOException e) {
					throw new ConnectException("Cannot delete path " + path, e);
				}
			}
			viewer.refresh();
		}
	}

	private void createFolder() {
		String msg = "Please provide a name.";
		String name = SingleValue.ask("Create folder", msg);
		// TODO enhance check of name validity
		if (EclipseUiUtils.notEmpty(name)) {
			try {
				Path child = currFolderPath.resolve(name);
				if (Files.exists(child))
					throw new ConnectException("An item with name " + name + " already exists at "
							+ currFolderPath.toString() + ", cannot create");
				else
					Files.createDirectories(child);
				viewer.refresh();
			} catch (IOException e) {
				throw new ConnectException("Cannot create folder " + name + " at " + currFolderPath.toString(), e);
			}
		}
	}
}
