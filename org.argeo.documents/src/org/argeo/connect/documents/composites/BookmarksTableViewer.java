package org.argeo.connect.documents.composites;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;

import org.argeo.connect.documents.DocumentsException;
import org.argeo.connect.documents.DocumentsService;
import org.argeo.connect.documents.DocumentsUiService;
import org.argeo.connect.ui.AppUiService;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class BookmarksTableViewer extends TableViewer {
	private static final long serialVersionUID = -2638259376244326947L;

	private final AppUiService appUiService;
	private final DocumentsService documentsService;
	private final DocumentsUiService documentsUiService = new DocumentsUiService();
	private final Node bookmarkParent;
	private BookmarksObserver bookmarksObserver = new BookmarksObserver();

	public BookmarksTableViewer(Composite parent, int style, Node bookmarkParent, DocumentsService documentsService,
			AppUiService appUiService) {
		super(parent, style | SWT.VIRTUAL);
		this.bookmarkParent = bookmarkParent;
		this.documentsService = documentsService;
		this.appUiService = appUiService;
	}

	public Table configureDefaultSingleColumnTable(int tableWidthHint) {
		Table table = this.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		table.setLinesVisible(false);
		table.setHeaderVisible(false);
		TableViewerColumn column = new TableViewerColumn(this, SWT.NONE);
		TableColumn tcol = column.getColumn();
		tcol.setWidth(tableWidthHint);
		column.setLabelProvider(new BookmarkIconNameLabelProvider());
		this.setContentProvider(new MyLazyCP());
		refreshMyBookmarksList();
		// The context menu
		BookmarksContextMenu contextMenu = new BookmarksContextMenu(bookmarkParent, this, documentsUiService);
		table.addMouseListener(new MouseAdapter() {
			private static final long serialVersionUID = 6737579410648595940L;

			@Override
			public void mouseDown(MouseEvent e) {
				if (e.button == 3) {
					contextMenu.show(table, new Point(e.x, e.y), BookmarksTableViewer.this.getStructuredSelection());
				}
			}
		});
		addBookmarksChangedListenner();
		return table;
	}

	private void refreshMyBookmarksList() {
		Node[] rows = documentsService.getMyBookmarks(ConnectJcrUtils.getSession(bookmarkParent));
		setInput(rows);
		int length = rows == null ? 0 : rows.length;
		this.setItemCount(length);
		this.refresh();
	}

	private void addBookmarksChangedListenner() {
		try {
			Session session = bookmarkParent.getSession();
			String path = bookmarkParent.getPath();
			ObservationManager observationManager = session.getWorkspace().getObservationManager();
			observationManager.addEventListener(bookmarksObserver,
					Event.PROPERTY_ADDED | Event.PROPERTY_CHANGED | Event.NODE_ADDED | Event.NODE_REMOVED, path, true,
					null, null, false);
		} catch (RepositoryException e) {
			throw new DocumentsException("Cannot handle event on " + bookmarkParent, e);
		}

		Table table = this.getTable();
		table.addDisposeListener(new DisposeListener() {
			private static final long serialVersionUID = 7320917131465167200L;

			@Override
			public void widgetDisposed(DisposeEvent event) {
				try {
					ObservationManager observationManager = bookmarkParent.getSession().getWorkspace()
							.getObservationManager();
					observationManager.removeEventListener(bookmarksObserver);
				} catch (RepositoryException e) {
					throw new DocumentsException("Cannot unsuscribe bookmark observer", e);
				}
			}
		});
	}

	private class BookmarksObserver implements EventListener {
		@Override
		public void onEvent(EventIterator events) {
			BookmarksTableViewer.this.getControl().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					refreshMyBookmarksList();
					BookmarksTableViewer.this.getControl().getParent().getParent().layout(true, true);
				}
			});
		}
	}

	private class MyLazyCP implements ILazyContentProvider {
		private static final long serialVersionUID = 9096550041395433128L;
		private Object[] elements;

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// IMPORTANT: don't forget this: an exception will be thrown if
			// a selected object is not part of the results anymore.
			viewer.setSelection(null);
			this.elements = (Object[]) newInput;
		}

		public void updateElement(int index) {
			BookmarksTableViewer.this.replace(elements[index], index);
		}
	}

	private class BookmarkIconNameLabelProvider extends ColumnLabelProvider {
		private static final long serialVersionUID = 8187902187946523148L;

		@Override
		public String getText(Object element) {
			Node node = (Node) element;
			String title = ConnectJcrUtils.get(node, Property.JCR_TITLE);
			// Todo rather diplay name of the target path directory.
			if (EclipseUiUtils.isEmpty(title))
				title = ConnectJcrUtils.getName(node);
			return title;
		}

		@Override
		public Image getImage(Object element) {
			Node node = (Node) element;
			return appUiService.getIconForType(node);
		}
	}
}
