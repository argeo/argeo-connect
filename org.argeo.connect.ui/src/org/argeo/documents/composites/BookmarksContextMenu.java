package org.argeo.documents.composites;

import static org.argeo.documents.ui.DocumentsUiService.ACTION_ID_DELETE_BOOKMARK;
import static org.argeo.documents.ui.DocumentsUiService.ACTION_ID_RENAME_BOOKMARK;

import javax.jcr.Node;

import org.argeo.connect.ui.widgets.AbstractConnectContextMenu;
import org.argeo.documents.ui.DocumentsUiService;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;

/** Generic popup context menu to manage NIO Path in a Viewer. */
public class BookmarksContextMenu extends AbstractConnectContextMenu {
	// Local context
	private final DocumentsUiService documentsUiService;
	private final Node bookmarkParent;
	private final TableViewer viewer;

	private final static String[] DEFAULT_ACTIONS = { ACTION_ID_DELETE_BOOKMARK, ACTION_ID_RENAME_BOOKMARK };

	public BookmarksContextMenu(Node bookmarkParent, TableViewer viewer, DocumentsUiService documentsUiService) {
		super(viewer.getControl().getDisplay(), DEFAULT_ACTIONS);
		this.documentsUiService = documentsUiService;
		this.bookmarkParent = bookmarkParent;
		this.viewer = viewer;
		createControl();
	}

	protected boolean performAction(String actionId) {
		switch (actionId) {
		case ACTION_ID_DELETE_BOOKMARK:
			return deleteBookmark();
		// break;
		case ACTION_ID_RENAME_BOOKMARK:
			return renameBookmark();
		// break;
		default:
			throw new IllegalArgumentException("Unimplemented action " + actionId);
		}
	}

	@Override
	protected boolean aboutToShow(Control source, Point location, IStructuredSelection selection) {
		boolean emptySel = selection == null || selection.isEmpty();
		if (emptySel)
			return false;
		boolean multiSel = !emptySel && selection.size() > 1;
		if (multiSel) {
			setVisible(true, ACTION_ID_DELETE_BOOKMARK);
			setVisible(false, ACTION_ID_RENAME_BOOKMARK);
		} else
			setVisible(true, ACTION_ID_DELETE_BOOKMARK, ACTION_ID_RENAME_BOOKMARK);
		return true;
	}

	private boolean deleteBookmark() {
		IStructuredSelection selection = ((IStructuredSelection) viewer.getSelection());
		return documentsUiService.deleteBookmark(getParentShell(), selection, bookmarkParent);
	}

	private boolean renameBookmark() {
		IStructuredSelection selection = ((IStructuredSelection) viewer.getSelection());
		return documentsUiService.renameBookmark(selection);
	}

	@Override
	protected String getLabel(String actionId) {
		return documentsUiService.getLabel(actionId);
	}
}
