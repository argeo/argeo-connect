package org.argeo.connect.documents.composites;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.cms.ui.fs.FsStyles;
import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.documents.DocumentsException;
import org.argeo.connect.ui.widgets.SingleQuestion;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
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
public class BookmarksContextMenu extends Shell {
	private static final long serialVersionUID = -9120261153509855795L;

	// Default known actions
	public final static String ACTION_ID_DELETE_BOOKMARK = "deleteBookmark";
	public final static String ACTION_ID_RENAME_BOOKMARK = "renameBookmark";

	// Local context
	private final Node bookmarkParent;
	private final TableViewer viewer;

	private final static String KEY_ACTION_ID = "actionId";
	private final static String[] DEFAULT_ACTIONS = { ACTION_ID_DELETE_BOOKMARK, ACTION_ID_RENAME_BOOKMARK };
	private Map<String, Button> actionButtons = new HashMap<String, Button>();

	public BookmarksContextMenu(Node bookmarkParent, TableViewer viewer) {
		super(viewer.getControl().getDisplay(), SWT.NO_TRIM | SWT.BORDER | SWT.ON_TOP);
		this.bookmarkParent = bookmarkParent;
		this.viewer = viewer;
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
			btn.setText(getLabel(actionId));
			btn.setLayoutData(EclipseUiUtils.fillWidth());
			CmsUtils.markup(btn);
			CmsUtils.style(btn, actionId + FsStyles.BUTTON_SUFFIX);
			btn.setData(KEY_ACTION_ID, actionId);
			btn.addSelectionListener(asl);
			actionButtons.put(actionId, btn);
		}
	}

	protected String getLabel(String actionId) {
		switch (actionId) {
		case ACTION_ID_DELETE_BOOKMARK:
			return "Delete bookmark";
		case ACTION_ID_RENAME_BOOKMARK:
			return "Rename bookmark";
		default:
			throw new IllegalArgumentException("Unknown action ID " + actionId);
		}
	}

	protected void aboutToShow(Control source, Point location) {
		IStructuredSelection selection = ((IStructuredSelection) viewer.getSelection());
		boolean emptySel = selection == null || selection.isEmpty();
		boolean multiSel = !emptySel && selection.size() > 1;

		if (multiSel) {
			setVisible(true, ACTION_ID_DELETE_BOOKMARK);
			setVisible(false, ACTION_ID_RENAME_BOOKMARK);
		} else
			setVisible(!emptySel, ACTION_ID_DELETE_BOOKMARK, ACTION_ID_RENAME_BOOKMARK);
	}

	private void setVisible(boolean visible, String... buttonIds) {
		for (String id : buttonIds) {
			Button button = actionButtons.get(id);
			button.setVisible(visible);
			GridData gd = (GridData) button.getLayoutData();
			gd.heightHint = visible ? SWT.DEFAULT : 0;
		}
	}

	public void show(Control source, Point location) {
		// Do not show when none has been chosen
		IStructuredSelection selection = ((IStructuredSelection) viewer.getSelection());
		boolean emptySel = selection == null || selection.isEmpty();
		if (emptySel)
			return;

		if (isVisible())
			setVisible(false);
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
		}
	}

	class ActionsShellListener extends org.eclipse.swt.events.ShellAdapter {
		private static final long serialVersionUID = -5092341449523150827L;

		@Override
		public void shellDeactivated(ShellEvent e) {
			setVisible(false);
		}
	}

	private void deleteBookmark() {
		IStructuredSelection selection = ((IStructuredSelection) viewer.getSelection());
		if (selection.isEmpty())
			return;

		StringBuilder builder = new StringBuilder();
		@SuppressWarnings("unchecked")
		Iterator<Object> iterator = selection.iterator();
		List<Node> nodes = new ArrayList<>();

		while (iterator.hasNext()) {
			Node node = (Node) iterator.next();
			builder.append(ConnectJcrUtils.get(node, Property.JCR_TITLE) + ", ");
			nodes.add(node);
		}
		String msg = "You are about to delete following bookmark: " + builder.substring(0, builder.length() - 2)
				+ ". Are you sure?";
		if (MessageDialog.openConfirm(this, "Confirm deletion", msg)) {
			Session session = ConnectJcrUtils.getSession(bookmarkParent);
			try {
				if (session.hasPendingChanges())
					throw new DocumentsException("Cannot remove bookmarks, session is not clean");
				for (Node path : nodes)
					path.remove();
				bookmarkParent.getSession().save();
			} catch (RepositoryException e) {
				JcrUtils.discardQuietly(session);
				throw new DocumentsException("Cannot delete bookmarks " + builder.toString(), e);
			}
		}
	}

	private void renameBookmark() {
		IStructuredSelection selection = ((IStructuredSelection) viewer.getSelection());
		if (selection.isEmpty() || selection.size() > 1)
			return;
		Node toRename = (Node) selection.getFirstElement();
		String msg = "Please provide a new name.";
		String name = SingleQuestion.ask("Rename bookmark", msg, ConnectJcrUtils.get(toRename, Property.JCR_TITLE));
		if (EclipseUiUtils.notEmpty(name)
				&& ConnectJcrUtils.setJcrProperty(toRename, Property.JCR_TITLE, PropertyType.STRING, name))
			ConnectJcrUtils.saveIfNecessary(toRename);
	}
}
