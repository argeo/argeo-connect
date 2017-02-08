package org.argeo.connect.documents.composites;

import static org.argeo.connect.documents.DocumentsUiService.ACTION_ID_BOOKMARK_FOLDER;
import static org.argeo.connect.documents.DocumentsUiService.ACTION_ID_CREATE_FOLDER;
import static org.argeo.connect.documents.DocumentsUiService.ACTION_ID_DELETE;
import static org.argeo.connect.documents.DocumentsUiService.ACTION_ID_DOWNLOAD_FOLDER;
import static org.argeo.connect.documents.DocumentsUiService.ACTION_ID_OPEN;
import static org.argeo.connect.documents.DocumentsUiService.ACTION_ID_RENAME;
import static org.argeo.connect.documents.DocumentsUiService.ACTION_ID_SHARE_FOLDER;
import static org.argeo.connect.documents.DocumentsUiService.ACTION_ID_UPLOAD_FILE;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Repository;

import org.argeo.cms.ui.fs.FsStyles;
import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.documents.DocumentsService;
import org.argeo.connect.documents.DocumentsUiService;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/** Generic popup context menu to manage NIO Path in a Viewer. */
public class DocumentsContextMenu extends Shell {
	private static final long serialVersionUID = -9120261153509855795L;

	// Local context
	private final DocumentsFolderComposite browser;
	private final DocumentsService docService;
	private final DocumentsUiService uiService;
	private final Repository repository;

	// private final Viewer viewer;
	private final static String KEY_ACTION_ID = "actionId";
	private final static String[] DEFAULT_ACTIONS = { ACTION_ID_CREATE_FOLDER, ACTION_ID_BOOKMARK_FOLDER,
			ACTION_ID_SHARE_FOLDER, ACTION_ID_DOWNLOAD_FOLDER, ACTION_ID_UPLOAD_FILE, ACTION_ID_RENAME,
			ACTION_ID_DELETE, ACTION_ID_OPEN };
	private Map<String, Button> actionButtons = new HashMap<String, Button>();

	private Path currFolderPath;

	public DocumentsContextMenu(DocumentsFolderComposite browser, DocumentsService documentsService,
			DocumentsUiService documentsUiService, Repository repository) {
		super(browser.getDisplay(), SWT.NO_TRIM | SWT.BORDER | SWT.ON_TOP);
		this.browser = browser;
		this.docService = documentsService;
		this.uiService = documentsUiService;
		this.repository = repository;
		setLayout(EclipseUiUtils.noSpaceGridLayout());

		Composite boxCmp = new Composite(this, SWT.NO_FOCUS | SWT.BORDER);
		boxCmp.setLayout(EclipseUiUtils.noSpaceGridLayout());
		CmsUtils.style(boxCmp, FsStyles.CONTEXT_MENU_BOX);
		createContextMenu(boxCmp);

		addShellListener(new ActionsShellListener());
	}

	protected void createContextMenu(Composite boxCmp) {
		ActionsSelListener asl = new ActionsSelListener();
		for (String actionId : DEFAULT_ACTIONS) {
			Button btn = new Button(boxCmp, SWT.FLAT | SWT.PUSH | SWT.LEAD);
			btn.setText(uiService.getLabel(actionId));
			btn.setLayoutData(EclipseUiUtils.fillWidth());
			CmsUtils.markup(btn);
			String styleName = actionId + FsStyles.BUTTON_SUFFIX;
			CmsUtils.style(btn, styleName);
			btn.setData(KEY_ACTION_ID, actionId);
			btn.addSelectionListener(asl);
			actionButtons.put(actionId, btn);
		}
	}

	protected void aboutToShow(Control source, Point location) {
		IStructuredSelection selection = ((IStructuredSelection) browser.getViewer().getSelection());
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
			setVisible(true, ACTION_ID_CREATE_FOLDER, ACTION_ID_UPLOAD_FILE, ACTION_ID_BOOKMARK_FOLDER);
			setVisible(false, ACTION_ID_SHARE_FOLDER, ACTION_ID_DOWNLOAD_FOLDER, ACTION_ID_RENAME, ACTION_ID_DELETE,
					ACTION_ID_OPEN);
		} else if (multiSel) {
			setVisible(true, ACTION_ID_CREATE_FOLDER, ACTION_ID_UPLOAD_FILE, ACTION_ID_DELETE,
					ACTION_ID_BOOKMARK_FOLDER);
			setVisible(false, ACTION_ID_SHARE_FOLDER, ACTION_ID_DOWNLOAD_FOLDER, ACTION_ID_RENAME, ACTION_ID_OPEN);
		} else if (isFolder) {
			setVisible(true, ACTION_ID_CREATE_FOLDER, ACTION_ID_UPLOAD_FILE, ACTION_ID_RENAME, ACTION_ID_DELETE,
					ACTION_ID_BOOKMARK_FOLDER);
			setVisible(false, ACTION_ID_OPEN,
					// to be implemented
					ACTION_ID_SHARE_FOLDER, ACTION_ID_DOWNLOAD_FOLDER);
		} else {
			setVisible(true, ACTION_ID_CREATE_FOLDER, ACTION_ID_UPLOAD_FILE, ACTION_ID_OPEN, ACTION_ID_RENAME,
					ACTION_ID_DELETE);
			setVisible(false, ACTION_ID_SHARE_FOLDER, ACTION_ID_DOWNLOAD_FOLDER, ACTION_ID_BOOKMARK_FOLDER);
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
				case ACTION_ID_BOOKMARK_FOLDER:
					bookmarkFolder();
					break;
				case ACTION_ID_RENAME:
					renameItem();
					break;
				case ACTION_ID_DELETE:
					deleteItems();
					break;
				case ACTION_ID_OPEN:
					openFile();
					break;
				case ACTION_ID_UPLOAD_FILE:
					uploadFiles();
					break;
				default:
					throw new IllegalArgumentException("Unimplemented action " + actionId);
					// case ACTION_ID_SHARE_FOLDER:
					// return "Share Folder";
					// case ACTION_ID_DOWNLOAD_FOLDER:
					// return "Download as zip archive";
				}
			}
			browser.setFocus();
		}
	}

	class ActionsShellListener extends org.eclipse.swt.events.ShellAdapter {
		private static final long serialVersionUID = -5092341449523150827L;

		@Override
		public void shellDeactivated(ShellEvent e) {
			setVisible(false);
		}
	}

	private void openFile() {
		IStructuredSelection selection = ((IStructuredSelection) browser.getViewer().getSelection());
		if (selection.isEmpty() || selection.size() > 1)
			// Should never happen
			return;
		Path toOpenPath = ((Path) selection.getFirstElement());
		uiService.openFile(toOpenPath);
	}

	private void deleteItems() {
		IStructuredSelection selection = ((IStructuredSelection) browser.getViewer().getSelection());
		if (selection.isEmpty())
			return;
		else if (uiService.deleteItems(getShell(), selection))
			browser.refresh();
	}

	private void renameItem() {
		IStructuredSelection selection = ((IStructuredSelection) browser.getViewer().getSelection());
		if (selection.isEmpty() || selection.size() > 1)
			// Should never happen
			return;
		Path toRenamePath = ((Path) selection.getFirstElement());
		if (uiService.renameItem(getShell(), currFolderPath, toRenamePath))
			browser.refresh();
	}

	private void createFolder() {
		if (uiService.createFolder(getShell(), currFolderPath))
			browser.refresh();
	}

	private void bookmarkFolder() {
		Path toBookmarkPath = null;
		IStructuredSelection selection = ((IStructuredSelection) browser.getViewer().getSelection());
		if (selection.isEmpty())
			toBookmarkPath = currFolderPath;
		else if (selection.size() > 1)
			toBookmarkPath = currFolderPath;
		else if (selection.size() == 1) {
			Path currSelected = ((Path) selection.getFirstElement());
			if (Files.isDirectory(currSelected))
				toBookmarkPath = currSelected;
			else
				return;
		}
		uiService.bookmarkFolder(toBookmarkPath, repository, docService);
	}

	private void uploadFiles() {
		if (uiService.uploadFiles(getShell(), currFolderPath))
			browser.refresh();
	}

	public void setCurrFolderPath(Path currFolderPath) {
		this.currFolderPath = currFolderPath;
	}
}
