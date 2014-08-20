package org.argeo.connect.streams.web.pages;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;
import javax.jcr.query.qom.StaticOperand;

import org.argeo.ArgeoException;
import org.argeo.connect.cms.CmsSession;
import org.argeo.connect.cms.CmsUiProvider;
import org.argeo.connect.cms.CmsUtils;
import org.argeo.connect.streams.RssManager;
import org.argeo.connect.streams.RssNames;
import org.argeo.connect.streams.RssTypes;
import org.argeo.connect.streams.web.StreamsWebStyles;
import org.argeo.connect.streams.web.listeners.NodeListDoubleClickListener;
import org.argeo.connect.streams.web.providers.RssListLblProvider;
import org.argeo.connect.streams.web.providers.SimpleNodeListContentProvider;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

/** Simple page to manage RSS channels and feeds */
public class RssHomePage implements CmsUiProvider {
	// private final static Log log = LogFactory.getLog(RssHomePage.class);

	/* DEPENDENCY INJECTION */
	private RssManager rssManager;
	private Session session;

	// This page widgets
	private TableViewer sourcesViewer;
	private Text srcFilterTxt;
	private Text newSourceTxt;

	private final static String FILTER_HELP_MSG = "Enter filter criterion";
	private final static String NEW_FEED_MSG = "Register a new RSS source";

	@Override
	public Control createUi(Composite parent, Node context) {
		Composite sourcesCmp = new Composite(parent, SWT.NO_FOCUS);
		sourcesCmp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		sourcesCmp.setData(RWT.CUSTOM_VARIANT,
				StreamsWebStyles.STREAMS_MAIN_COMPOSITE);
		populateSourcesPanel(sourcesCmp);
		parent.layout();
		return null;
	}

	public void populateSourcesPanel(Composite parent) {
		parent.setLayout(CmsUtils.noSpaceGridLayout());

		Composite addFeedCmp = new Composite(parent, SWT.NO_FOCUS);
		addFeedCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		addChannelPanel(addFeedCmp);

		srcFilterTxt = new Text(parent, SWT.BORDER | SWT.SEARCH
				| SWT.ICON_SEARCH | SWT.ICON_CANCEL);
		sourcesViewer = createListPartWithRemove(parent);

		initializeFilterPanel(srcFilterTxt, sourcesViewer,
				RssTypes.RSS_CHANNEL_INFO);
		sourcesViewer.setInput(JcrUtils.nodeIteratorToList(doSearch(
				RssTypes.RSS_CHANNEL_INFO, "")));
	}

	public void addChannelPanel(Composite body) {
		GridLayout gl = new GridLayout(2, false);
		gl.marginHeight = gl.marginWidth = gl.verticalSpacing = 0;
		gl.horizontalSpacing = 5;
		body.setLayout(gl);

		// text area to enter a new link
		newSourceTxt = new Text(body, SWT.BORDER | SWT.ICON_CANCEL | SWT.SINGLE);
		newSourceTxt.setMessage(NEW_FEED_MSG);
		newSourceTxt
				.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		newSourceTxt.addTraverseListener(new TraverseListener() {
			private static final long serialVersionUID = 3928817201882691948L;

			public void keyTraversed(TraverseEvent e) {
				if (e.keyCode == SWT.CR) {
					registerRssLink(newSourceTxt.getText());
					e.doit = false;
				}
			}
		});

		Button okBtn = new Button(body, SWT.PUSH);
		okBtn.setText(" Add ");

		okBtn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 5003010530960334977L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				registerRssLink(newSourceTxt.getText());
			}
		});
	}

	public Text initializeFilterPanel(final Text txt, final TableViewer viewer,
			final String nodeType) {
		txt.setMessage(FILTER_HELP_MSG);
		txt.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_FILL));

		txt.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 5003010530960334977L;

			public void modifyText(ModifyEvent event) {
				String filter = txt.getText();
				viewer.setInput(JcrUtils.nodeIteratorToList(doSearch(nodeType,
						filter)));
			}
		});
		return txt;
	}

	protected TableViewer createListPartWithRemove(Composite parent) {
		parent.setLayout(new GridLayout());

		Composite tableComposite = new Composite(parent, SWT.NONE);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.GRAB_VERTICAL | GridData.VERTICAL_ALIGN_FILL
				| GridData.GRAB_HORIZONTAL);
		tableComposite.setLayoutData(gd);

		final TableViewer v = new TableViewer(tableComposite);
		v.setLabelProvider(new RssListLblProvider());

		TableColumn singleColumn = new TableColumn(v.getTable(), SWT.LEFT);
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		tableColumnLayout.setColumnData(singleColumn, new ColumnWeightData(85));
		tableComposite.setLayout(tableColumnLayout);

		// Remove links
		// v.getTable().addSelectionListener(new HtmlListRwtAdapter() {
		// private static final long serialVersionUID = -1633530879653353403L;
		//
		// @Override
		// public void widgetSelected(SelectionEvent e) {
		// if (e.detail == RWT.HYPERLINK) {
		// super.widgetSelected(e);
		// String filter = srcFilterTxt.getText();
		// sourcesViewer.setInput(JcrUtils
		// .nodeIteratorToList(doSearch(
		// RssTypes.RSS_CHANNEL_INFO, filter)));
		// }
		// }
		//
		// });

		TableViewerColumn col = createTableViewerColumn(v, "Edit/Remove links",
				SWT.NONE, 60);
		tableColumnLayout.setColumnData(col.getColumn(), new ColumnWeightData(
				20, 50, true));
		col.setLabelProvider(new ColumnLabelProvider() {
			private static final long serialVersionUID = 1L;

			@Override
			public String getText(Object element) {
				return null;
				// TODO implement this.
				// return PeopleHtmlUtils.getRemoveSnippetForLists((Node)
				// element,
				// true);
			}
		});

		// Corresponding table & style
		Table table = v.getTable();
		table.setLinesVisible(false);
		table.setHeaderVisible(false);
		tableComposite.setLayout(tableColumnLayout);
		table.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
		table.setData(RWT.CUSTOM_ITEM_HEIGHT, Integer.valueOf(50));

		v.setContentProvider(new SimpleNodeListContentProvider());
		v.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				CmsSession cmsSession = (CmsSession) v.getTable().getDisplay()
						.getData(CmsSession.KEY);
				IStructuredSelection selection = (IStructuredSelection) event
						.getSelection();
				try {
					cmsSession.navigateTo(((Node) selection.getFirstElement())
							.getPath());
				} catch (RepositoryException e) {
					throw new ArgeoException(
							"unable to get path for node in the RssHomePage", e);
				}
			}
		});
		return v;
	}

	protected TableViewer createListPart(Composite parent,
			ILabelProvider labelProvider) {
		parent.setLayout(new GridLayout());

		Composite tableComposite = new Composite(parent, SWT.NONE);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.GRAB_VERTICAL | GridData.VERTICAL_ALIGN_FILL
				| GridData.GRAB_HORIZONTAL);
		tableComposite.setLayoutData(gd);

		TableViewer v = new TableViewer(tableComposite);
		v.setLabelProvider(labelProvider);

		TableColumn singleColumn = new TableColumn(v.getTable(), SWT.V_SCROLL);
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		tableColumnLayout.setColumnData(singleColumn, new ColumnWeightData(85));
		tableComposite.setLayout(tableColumnLayout);

		// Corresponding table & style
		Table table = v.getTable();
		table.setLinesVisible(false);
		table.setHeaderVisible(false);
		// Enable markups
		table.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
		table.setData(RWT.CUSTOM_ITEM_HEIGHT, Integer.valueOf(50));
		v.setContentProvider(new SimpleNodeListContentProvider());
		v.addDoubleClickListener(new NodeListDoubleClickListener());
		return v;
	}

	private NodeIterator doSearch(String nodeType, String filter) {
		try {
			QueryManager queryManager = session.getWorkspace()
					.getQueryManager();
			QueryObjectModelFactory factory = queryManager.getQOMFactory();
			Selector source = factory.selector(nodeType, nodeType);
			Constraint defaultC = null;
			String[] strs = filter.trim().split(" ");
			for (String token : strs) {
				StaticOperand so = factory.literal(session.getValueFactory()
						.createValue("*" + token + "*"));
				Constraint currC = factory.fullTextSearch(
						source.getSelectorName(), null, so);
				if (defaultC == null)
					defaultC = currC;
				else
					defaultC = factory.and(defaultC, currC);
			}
			QueryObjectModel query = factory.createQuery(source, defaultC,
					null, null);
			QueryResult result = query.execute();
			return result.getNodes();
		} catch (RepositoryException e) {
			throw new ArgeoException("Unable to list " + nodeType, e);
		}
	}

	private static TableViewerColumn createTableViewerColumn(
			TableViewer parent, String name, int style, int width) {
		TableViewerColumn tvc = new TableViewerColumn(parent, style);
		final TableColumn column = tvc.getColumn();
		column.setText(name);
		column.setWidth(width);
		column.setResizable(true);
		return tvc;
	}

	private boolean registerRssLink(String sourceStr) {
		try {
			Node channelInfo = rssManager
					.getOrCreateChannel(session, sourceStr).getNode(
							RssNames.RSS_CHANNEL_INFO);
			// TODO do it asynchroneously
			rssManager.retrieveItems();

			// Save and check in
			JcrUtils.updateLastModified(channelInfo);
			channelInfo.getSession().save();
			channelInfo.getSession().getWorkspace().getVersionManager()
					.checkin(channelInfo.getPath());

			openChannelEditor(channelInfo);

			String filter = srcFilterTxt.getText();
			sourcesViewer.setInput(JcrUtils.nodeIteratorToList(doSearch(
					RssTypes.RSS_CHANNEL_INFO, filter)));
			newSourceTxt.setText("");
		} catch (RepositoryException e) {
			throw new ArgeoException("Unable to create a Stream", e);
		}
		return true;
	}

	private void openChannelEditor(Node channelInfo) throws RepositoryException {
		CmsSession cmsSession = (CmsSession) this.newSourceTxt.getDisplay()
				.getData(CmsSession.KEY);
		cmsSession.navigateTo(channelInfo.getPath());
	}

	/* DEPENDENCY INJECTION */
	public void setRepository(Repository repository) {
		// TO MANAGE THIS: session stay unclosed.
		try {
			this.session = repository.login();
		} catch (RepositoryException e) {
			throw new ArgeoException("Unable to log in the repository", e);
		}
	}

	public void setRssManager(RssManager rssManager) {
		this.rssManager = rssManager;
	}
}