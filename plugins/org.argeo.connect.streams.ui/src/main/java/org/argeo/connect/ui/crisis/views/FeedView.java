/*
 * Argeo Connect - Data management and communications
 * Copyright (C) 2012 Argeo GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or combining it
 * with software covered by the terms of the Eclipse Public License, the
 * licensors of this Program grant you additional permission to convey the
 * resulting work. Corresponding Source for a non-source form of such a
 * combination shall include the source code for the parts of such software
 * which are used as well as that of the covered work.
 */
package org.argeo.connect.ui.crisis.views;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.argeo.ArgeoException;
import org.argeo.connect.ui.crisis.editors.LinkEditorInput;
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
import org.eclipse.ui.PlatformUI;
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

	private String linkEditorId;

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
					PlatformUI
							.getWorkbench()
							.getActiveWorkbenchWindow()
							.getActivePage()
							.openEditor(
									new LinkEditorInput(feedsNode, syndEntry),
									linkEditorId);
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

	public void setLinkEditorId(String linkEditorId) {
		this.linkEditorId = linkEditorId;
	}

	static class FeedContentProvider implements IStructuredContentProvider {

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		@SuppressWarnings("unchecked")
		public Object[] getElements(Object inputElement) {
			try {
				URL feedUrl = new URL(inputElement.toString());

				SyndFeedInput input = new SyndFeedInput();
				SyndFeed feed = input.build(new XmlReader(feedUrl));
				List<SyndEntry> entries = new ArrayList<SyndEntry>();
				entries: for (SyndEntry syndEntry : (List<SyndEntry>) feed
						.getEntries()) {
					if (syndEntry.getTitle() == null
							|| syndEntry.getTitle().trim().equals(""))
						continue entries;
					entries.add(syndEntry);
				}

				return entries.toArray();
			} catch (Exception e) {
				throw new ArgeoException("Cannot read feed", e);
			}
		}
	}
}
