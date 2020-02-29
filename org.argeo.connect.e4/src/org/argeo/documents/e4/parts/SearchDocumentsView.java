package org.argeo.documents.e4.parts;

import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.ui.SystemWorkbenchService;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.documents.DocumentsService;
import org.argeo.eclipse.ui.specific.EclipseUiSpecificUtils;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class SearchDocumentsView {
	private final static Log log = LogFactory.getLog(SearchDocumentsView.class);

	@Inject
	private Repository repository;

	private Session session;

	@PostConstruct
	public void createPartControl(Composite parent) {
		session = ConnectJcrUtils.login(repository);
		// MainLayout
		parent.setLayout(new GridLayout());

		Text searchTxt = new Text(parent, SWT.BORDER | SWT.SEARCH | SWT.ICON_CANCEL);
		searchTxt.setLayoutData(CmsUtils.fillWidth());

		TableViewer viewer = new TableViewer(parent);
		viewer.getTable().setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
		viewer.getTable().setData(RWT.TOOLTIP_MARKUP_ENABLED, Boolean.TRUE);
		EclipseUiSpecificUtils.enableToolTipSupport(viewer);
		viewer.getTable().setLayoutData(CmsUtils.fillAll());
		viewer.setContentProvider(new ArrayContentProvider());
		TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
		column.getColumn().setWidth(400);
		column.getColumn().setText("Name");
		column.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				try {
					Row row = (Row) element;
					return row.getNode().getParent().getName();
				} catch (RepositoryException e) {
					return e.getMessage();
				}
			}

			@Override
			public Image getImage(Object element) {
//				String mimeType = ConnectJcrUtils.get((Node) element, Property.JCR_MIMETYPE);
				// TODO icons
				return super.getImage(element);
			}

			@Override
			public String getToolTipText(Object element) {
//				try {
//					Row row = (Row) element;
//					String excerpt = getExcerpt(row.getNode().getIdentifier(), "*software*");
//					return "... " + excerpt + " ...";
//				} catch (RepositoryException e) {
//					return e.getMessage();
//				}
				try {
					return ConnectJcrUtils.getPath(((Row) element).getNode().getParent());
				} catch (RepositoryException e) {
					return e.getMessage();
				}
			}

		});

		Browser browser = new Browser(parent, SWT.NONE);
		GridData gd = CmsUtils.fillWidth();
		gd.heightHint = 200;
		browser.setLayoutData(gd);

		searchTxt.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				String searchTerm = searchTxt.getText().trim();
				if ("".equals(searchTerm)) {
					viewer.setInput(new ArrayList<>());
					return;
				}
				String searchPattern = "*" + searchTerm + "*";
				ArrayList<Row> lst = new ArrayList<>();
				try {
//					Query query = session.getWorkspace().getQueryManager().createQuery(
//							"select * from [nt:resource] as s" + " where contains(s.*, '" + searchPattern + "')",
//							Query.JCR_SQL2);
					// we have to use SQL for excerpt
					Query query = session.getWorkspace().getQueryManager().createQuery(
							"select excerpt(.) from nt:resource" + " where contains(., '" + searchPattern + "')",
							Query.SQL);
					QueryResult result = query.execute();
//					String[] columnNames = result.getColumnNames();
//					for (int i = 0; i < columnNames.length; i++) {
//						System.out.println(i + "\t" + columnNames[i]);
//					}

					for (RowIterator it = result.getRows(); it.hasNext();) {
						Row r = it.nextRow();
						// Value excerpt = r.getValue("rep:excerpt(.)");
						// log.debug(excerpt.getString());
//						log.debug(r.getScore());
//						log.debug(r.getNode());
//						for (int i = 0; i < columnNames.length; i++) {
//							log.debug(columnNames[i] + "=" + r.getValue(columnNames[i]).getString());
//						}
						lst.add(r);
					}

//					NodeIterator nit = query.execute().getNodes();
//					while (nit.hasNext()) {
//						lst.add(nit.nextNode().getParent());
//					}
				} catch (RepositoryException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				viewer.setInput(lst);
				if (lst.size() > 0)
					viewer.setSelection(new StructuredSelection(lst.get(0)));
				// viewer.refresh();
			}

		});

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				Object element = ((IStructuredSelection) event.getSelection()).getFirstElement();
				if (element == null) {
					browser.setText("");
					return;
				}

				try {
					Row row = (Row) element;
					String excerpt = row.getValue("rep:excerpt(.)").getString();
					excerpt = "<div style='font-family: sans-serif'>" + excerpt + "</div>";
					// String excerpt = "... " + getExcerpt(row.getNode().getIdentifier(),
					// "*software*") ;
					browser.setText(excerpt);
				} catch (RepositoryException e) {
					e.printStackTrace();
				}

			}
		});

		viewer.setInput(new ArrayList<>());
	}

	private String getExcerpt(String uuid, String searchTerm) {
		try {
			Query query = session.getWorkspace().getQueryManager().createQuery("select excerpt(.) from nt:resource"
					+ " where jcr:uuid = '" + uuid + "' AND contains(., '" + searchTerm + "')", Query.SQL);
			String excerpt = query.execute().getRows().nextRow().getValue("rep:excerpt(.)").getString();
			// Remove div tag
			excerpt = excerpt.substring(5, excerpt.length() - 6);
			System.out.print(excerpt);
			return excerpt;
		} catch (RepositoryException e) {
			e.printStackTrace();
			return "";
		}
	}
}
