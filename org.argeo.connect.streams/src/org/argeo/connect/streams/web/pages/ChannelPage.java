package org.argeo.connect.streams.web.pages;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.qom.DescendantNode;
import javax.jcr.query.qom.Ordering;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.argeo.connect.cms.CmsUiProvider;
import org.argeo.connect.streams.RssNames;
import org.argeo.connect.streams.RssTypes;
import org.argeo.connect.streams.web.providers.RssHtmlProvider;
import org.argeo.connect.streams.web.providers.RssListLblProvider;
import org.argeo.connect.streams.web.providers.SimpleNodeListContentProvider;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

/**
 * Manage a single channel
 * <ul>
 * <li>enrich meta data for a given source.</li>
 * <li>display corresponding items</li>
 * </ul>
 */
public class ChannelPage implements CmsUiProvider, RssNames {
	final static Log log = LogFactory.getLog(ChannelPage.class);

	// Main business Objects
	private Session session;
	private Node channel;
	private TableViewer feeds;

	@Override
	public Control createUi(Composite parent, Node context)
			throws RepositoryException {
		this.channel = context;
		try {
			session = channel.getSession();
		} catch (RepositoryException re) {
			throw new ArgeoException("Unable to get session for node "
					+ context, re);
		}
		populate(parent);
		return null;
	}

	protected void populate(Composite parent) {
		parent.setLayout(new GridLayout());

		Label readOnlyInfoLbl = new Label(parent, SWT.WRAP);
		readOnlyInfoLbl.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
		readOnlyInfoLbl.setText(RssHtmlProvider.getChannelTitle(channel));

		// 1st line: Search + Buttons.
		// Composite buttons = new Composite(parent, SWT.NO_FOCUS);
		// buttons.setLayout(new GridLayout());
		// buttons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		// new Label(parent, SWT.NONE).setText("Add here search and buttons");

		// 2nd line: the list
		Composite list = new Composite(parent, SWT.NO_FOCUS);
		list.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createListPart(list, new RssListLblProvider(false));

	}

	protected void createListPart(Composite parent, ILabelProvider labelProvider) {
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		parent.setLayout(tableColumnLayout);

		Table table = new Table(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		table.setLinesVisible(false);
		table.setHeaderVisible(false);
		// Enable markups
		table.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
		table.setData(RWT.CUSTOM_ITEM_HEIGHT, Integer.valueOf(80));

		TableViewer v = new TableViewer(table);
		v.setLabelProvider(labelProvider);
		v.setContentProvider(new SimpleNodeListContentProvider());
		feeds = v;

		TableColumn singleColumn = new TableColumn(table, SWT.LEFT);
		tableColumnLayout.setColumnData(singleColumn, new ColumnWeightData(90));

		refreshFilteredList(RssTypes.RSS_CHANNEL);
		// table.setLayout(tableColumnLayout);
	}

	protected void refreshFilteredList(String nodeType) {
		try {
			QueryManager queryManager = session.getWorkspace()
					.getQueryManager();
			QueryObjectModelFactory factory = queryManager.getQOMFactory();
			final String typeSelector = "items";
			Selector source = factory.selector(RssTypes.RSS_ITEM, typeSelector);
			DescendantNode statOp = factory.descendantNode(
					source.getSelectorName(), channel.getParent().getPath());

			Ordering order = factory.descending(factory.lowerCase(factory
					.propertyValue(source.getSelectorName(), RSS_PUB_DATE)));

			QueryObjectModel query = factory.createQuery(source, statOp,
					new Ordering[] { order }, null);
			QueryResult result = query.execute();
			NodeIterator ni = result.getNodes();
			feeds.setInput(JcrUtils.nodeIteratorToList(ni));
		} catch (RepositoryException e) {
			throw new ArgeoException("Unable to list " + nodeType, e);
		}
	}

	// //////////////
	// Helpers

	/** Creates a text widget with RowData already set */
	protected Text createRDText(Composite parent, String msg, String toolTip,
			int width) {
		Text text = new Text(parent, SWT.BORDER | SWT.SINGLE | SWT.LEFT);
		text.setMessage(msg);
		text.setToolTipText(toolTip);
		text.setLayoutData(new RowData(width, SWT.DEFAULT));
		return text;
	}

	/** Creates a text widget with GridData already set */
	protected Text createGDText(Composite parent, String msg, String toolTip,
			int width, int colSpan) {
		Text text = new Text(parent, SWT.BORDER | SWT.SINGLE | SWT.LEFT);
		text.setMessage(msg);
		text.setToolTipText(toolTip);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.widthHint = width;
		gd.horizontalSpan = colSpan;
		text.setLayoutData(gd);
		return text;
	}

	public String getlastUpdateMessage() {
		return "";
	}
}