package org.argeo.connect.documents.composites;

import static org.argeo.connect.documents.DocumentsUiService.ACTION_ID_DELETE_BOOKMARK;
import static org.argeo.connect.documents.DocumentsUiService.ACTION_ID_RENAME_BOOKMARK;

import javax.jcr.Node;

import org.argeo.connect.documents.DocumentsUiService;
import org.argeo.connect.ui.widgets.AbstractConnectContextMenu;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;

/** Generic popup context menu to manage NIO Path in a Viewer. */
public class BookmarksContextMenu extends AbstractConnectContextMenu {
	private static final long serialVersionUID = -7340369674820928400L;

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

	protected void performAction(String actionId) {
		switch (actionId) {
		case ACTION_ID_DELETE_BOOKMARK:
			deleteBookmark();
			break;
		case ACTION_ID_RENAME_BOOKMARK:
			renameBookmark();
			break;
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

	private void deleteBookmark() {
		IStructuredSelection selection = ((IStructuredSelection) viewer.getSelection());
		documentsUiService.deleteBookmark(getShell(), selection, bookmarkParent);
	}

	private void renameBookmark() {
		IStructuredSelection selection = ((IStructuredSelection) viewer.getSelection());
		documentsUiService.renameBookmark(selection);
	}

	@Override
	protected String getLabel(String actionId) {
		return documentsUiService.getLabel(actionId);
	}
}
