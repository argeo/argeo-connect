package org.argeo.connect.crisis.manager.views;

import java.net.URL;

import javax.jcr.Node;
import javax.jcr.Session;

import org.argeo.ArgeoException;
import org.argeo.connect.crisis.manager.CrisisManagerPlugin;
import org.argeo.connect.crisis.manager.editors.LinkEditor;
import org.argeo.connect.crisis.manager.editors.LinkEditorInput;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

public class FeedView extends ViewPart {
	private String feedsBasePath = "/connect/feeds";
	private Session session;
	private TableViewer viewer;

	@Override
	public void createPartControl(Composite parent) {
		final Node feedsNode = JcrUtils.mkdirs(session, feedsBasePath);

		viewer = new TableViewer(parent);
		TableViewerColumn col = createTableViewerColumn(viewer, "Item", 500);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				SyndEntry p = (SyndEntry) element;
				return p.getTitle();
			}

			@Override
			public String getToolTipText(Object element) {
				SyndEntry p = (SyndEntry) element;
				StringBuffer content = new StringBuffer("");
				for (Object ct : p.getContents()) {
					content.append(((SyndContent) ct).getValue());
				}
				return content.toString();
			}

			@Override
			protected void initialize(ColumnViewer viewer, ViewerColumn column) {
				// TODO Auto-generated method stub
				super.initialize(viewer, column);
			}

		});
		viewer.setContentProvider(new FeedContentProvider());
		viewer.addDoubleClickListener(new IDoubleClickListener() {

			public void doubleClick(DoubleClickEvent event) {
				if (event.getSelection().isEmpty())
					return;

				SyndEntry syndEntry = (SyndEntry) ((IStructuredSelection) event
						.getSelection()).getFirstElement();
				try {
					CrisisManagerPlugin
							.getDefault()
							.getWorkbench()
							.getActiveWorkbenchWindow()
							.getActivePage()
							.openEditor(
									new LinkEditorInput(feedsNode, syndEntry),
									LinkEditor.ID);
				} catch (PartInitException e) {
					throw new ArgeoException("Cannot init part", e);
				}

			}
		});
		viewer.setInput("http://actu.voila.fr/Magic/XML/rss-monde.xml");

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

	@Override
	public void setFocus() {
		viewer.getTable().setFocus();
	}

	
	public void setSession(Session session) {
		this.session = session;
	}


	static class FeedContentProvider implements IStructuredContentProvider {

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public Object[] getElements(Object inputElement) {
			try {
				URL feedUrl = new URL(inputElement.toString());

				SyndFeedInput input = new SyndFeedInput();
				SyndFeed feed = input.build(new XmlReader(feedUrl));

				return feed.getEntries().toArray();
			} catch (Exception e) {
				throw new ArgeoException("Cannot read feed", e);
			}
		}

	}
}
