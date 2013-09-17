package org.argeo.connect.streams.ui.views;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
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
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.ui.JcrUiUtils;
import org.argeo.connect.people.ui.editors.EntityEditorInput;
import org.argeo.connect.people.ui.providers.BasicNodeListContentProvider;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.streams.RssManager;
import org.argeo.connect.streams.RssNames;
import org.argeo.connect.streams.RssTypes;
import org.argeo.connect.streams.ui.RssImages;
import org.argeo.connect.streams.ui.RssUiPlugin;
import org.argeo.connect.streams.ui.editors.ChannelEditor;
import org.argeo.connect.streams.ui.listeners.NodeListDoubleClickListener;
import org.argeo.connect.streams.ui.providers.RssListLabelProvider;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

/** Fisrt draft of a user friendly pannel to manage rss channels and feeds */
public class RssSearchView extends ViewPart {

	// private final static Log log = LogFactory.getLog(RssSearchView.class);
	public static final String ID = RssUiPlugin.PLUGIN_ID + ".rssSearchView";

	/* DEPENDENCY INJECTION */
	private Session session;
	private PeopleService peopleService;
	private RssManager rssManager;

	// This page widgets
	private TableViewer entityViewer;
	private Text filterTxt;
	private Text newFeedTxt;
	private final static String FILTER_HELP_MSG = "Enter filter criterion";
	private final static String NEW_FEED_MSG = "Register a Rss link...";

	// A dummy label to test string cleaner
	// private Label toremove;

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(1, false));

		addHeaderPanel(parent);
		addFilterPanel(parent);
		entityViewer = createListPart(parent, new RssListLabelProvider());

		// set data
		refreshFilteredList(RssTypes.RSS_CHANNEL_INFO);
	}

	public void addHeaderPanel(Composite parent) {
		Composite mainPannel = new Composite(parent, SWT.NO_FOCUS);
		GridData gd = new GridData();
		// gd.widthHint = 180;
		gd.grabExcessHorizontalSpace = true;
		mainPannel.setLayoutData(gd);

		mainPannel.setLayout(new GridLayout(1, false));
		// The logo
		Label image = new Label(mainPannel, SWT.NO_FOCUS);
		image.setBackground(parent.getBackground());
		image.setImage(RssImages.LOGO);
		image.setSize(120, 100);

		// TODO remove this
		// a dummy label to test string cleaner
		// toremove = new Label(mainPannel, SWT.NO_FOCUS);
		// toremove.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);

		Composite bottomPannel = new Composite(mainPannel, SWT.NONE);
		bottomPannel.setLayout(new GridLayout(2, false));
		gd = new GridData(SWT.RIGHT, SWT.TOP, true, true);
		// gd.widthHint = 180;
		gd.grabExcessHorizontalSpace = true;
		bottomPannel.setLayoutData(gd);

		bottomPannel.setLayout(new GridLayout(2, false));
		Link link = new Link(bottomPannel, SWT.RIGHT);
		link.setText("<a>My Feeds...</a>");
		link.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				refreshFilteredList(RssTypes.RSS_CHANNEL_INFO);
				// CommandUtils.callCommand(OpenDefaultEditor.ID);
			}
		});
		// gd = new GridData(SWT.RIGHT, SWT.TOP, false, false);
		// link.setLayoutData(gd);

		addNewFeedPanel(bottomPannel);

		// link = new Link(parent, SWT.NONE);
		// link.setText("<a>A few Chanels</a>");
		// link.addSelectionListener(new SelectionAdapter() {
		// private static final long serialVersionUID = 1L;
		//
		// @Override
		// public void widgetSelected(final SelectionEvent event) {
		// openEditorForId(RssTypes.RSS_CHANNEL);
		// }
		// });
		//
		// link = new Link(parent, SWT.NONE);
		// link.setText("<a>23035 Feeds </a>");
		// link.addSelectionListener(new SelectionAdapter() {
		// private static final long serialVersionUID = 1L;
		//
		// @Override
		// public void widgetSelected(final SelectionEvent event) {
		// openEditorForId(RssTypes.RSS_ITEM);
		// }
		// });
	}

	private void openEditorForId(String uid) {
		try {
			EntityEditorInput eei = new EntityEditorInput(uid);
			IWorkbenchWindow iww = RssUiPlugin.getDefault().getWorkbench()
					.getActiveWorkbenchWindow();
			iww.getActivePage().openEditor(eei, ChannelEditor.ID);
		} catch (PartInitException pie) {
			throw new ArgeoException(
					"Unexpected PartInitException while opening entity editor",
					pie);
		}

	}

	public void addNewFeedPanel(Composite parent) {
		Composite panel = new Composite(parent, SWT.FILL);
		panel.setLayout(new GridLayout(2, false));
		// Text Area for the filter
		newFeedTxt = new Text(panel, SWT.BORDER | SWT.ICON_CANCEL | SWT.SINGLE);
		newFeedTxt.setMessage(NEW_FEED_MSG);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.widthHint = 180;
		gd.grabExcessHorizontalSpace = true;
		newFeedTxt.setLayoutData(gd);

		Button okBtn = new Button(panel, SWT.PUSH);
		okBtn.setText("Add");

		okBtn.addSelectionListener(new SelectionListener() {
			private static final long serialVersionUID = 5003010530960334977L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				registerRssLink(newFeedTxt.getText());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	private boolean registerRssLink(String sourceStr) {
		try {
			Node channelInfo = rssManager
					.getOrCreateChannel(session, sourceStr).getNode(
							RssNames.RSS_CHANNEL_INFO);
			rssManager.retrieveItems();
			CommonsJcrUtils.saveAndCheckin(channelInfo);
			channelInfo.getSession().getWorkspace().getVersionManager()
					.checkin(channelInfo.getPath());
			openEditorForId(channelInfo.getIdentifier());

		} catch (RepositoryException e) {
			throw new ArgeoException(
					"You loose... Not so easy to create a Stream", e);
		}

		return true;
	}

	public void addFilterPanel(Composite parent) {
		// Text Area for the filter
		filterTxt = new Text(parent, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH
				| SWT.ICON_CANCEL);
		filterTxt.setMessage(FILTER_HELP_MSG);
		filterTxt.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_FILL));

		// final DropDown dd = new DropDown(filterTxt);
		// dd.setVisibleItemCount(5);
		// dd.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);

		// dd.show();
		// dd.setVisible(true);
		filterTxt.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 5003010530960334977L;

			public void modifyText(ModifyEvent event) {
				// might be better to use an asynchronous Refresh();
				refreshFilteredList(RssTypes.RSS_ITEM);

				// NodeIterator ni = doSearch(RssTypes.RSS_ITEM);
				// int i = 0;
				// List<String> results = new ArrayList<String>();
				// while (ni.hasNext() && i < 5) {
				// Node currNode = ni.nextNode();
				// String currStr = RssHtmlProvider.getItemShort(currNode);
				// // String currStr = CommonsJcrUtils.getStringValue(currNode,
				// // Property.JCR_TITLE);
				// if (CommonsJcrUtils.checkNotEmptyString(currStr)) {
				// results.add(currStr);
				// }
				// i++;
				// }
				//
				// int lenght = results.size();
				// if (lenght == 0) {
				// dd.hide();
				// return;
				// } else {
				// dd.setVisibleItemCount(lenght);
				// dd.setItems(results.toArray(new String[lenght]));
				// dd.show();
				// }

			}
		});

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
		v.setContentProvider(new BasicNodeListContentProvider());
		v.addDoubleClickListener(new NodeListDoubleClickListener(peopleService));
		return v;
	}

	@Override
	public void dispose() {
		JcrUtils.logoutQuietly(session);
		super.dispose();
	}

	@Override
	public void setFocus() {
	}

	protected void refreshFilteredList(String nodeType) {
		entityViewer.setInput(JcrUiUtils.nodeIteratorToList(doSearch(nodeType),
				30));
		// TODO rmove this. Was used to test html cleaning
		// try {
		// NodeIterator ni = doSearch(nodeType);
		// while (ni.hasNext()) {
		// Node currNode = ni.nextNode();
		// String name = currNode.getName();
		// try {
		// String desc = CommonsJcrUtils.getStringValue(currNode,
		// Property.JCR_DESCRIPTION);
		// String cdesc = RssHtmlProvider.cleanString(desc);
		// toremove.setText(cdesc);
		// // toremove.layout();
		// } catch (Exception e) {
		// log.error("Error while rendering node: " + name, e);
		//
		// }
		//
		// }
		// } catch (RepositoryException e1) {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// }

	}

	protected NodeIterator doSearch(String nodeType) {
		try {
			String filter = filterTxt.getText();
			QueryManager queryManager = session.getWorkspace()
					.getQueryManager();
			QueryObjectModelFactory factory = queryManager.getQOMFactory();

			Selector source = factory.selector(nodeType, "selector");

			// no Default Constraint
			Constraint defaultC = null;

			// Parse the String
			String[] strs = filter.trim().split(" ");
			if (strs.length == 0) {
				StaticOperand so = factory.literal(session.getValueFactory()
						.createValue("*"));
				defaultC = factory.fullTextSearch("selector", null, so);
			} else {
				for (String token : strs) {
					StaticOperand so = factory.literal(session
							.getValueFactory().createValue("*" + token + "*"));
					Constraint currC = factory.fullTextSearch(
							source.getSelectorName(), null, so);
					if (defaultC == null)
						defaultC = currC;
					else
						defaultC = factory.and(defaultC, currC);
				}
			}
			QueryObjectModel query;
			query = factory.createQuery(source, defaultC, null, null);

			QueryResult result = query.execute();
			NodeIterator ni = result.getNodes();
			return ni;
		} catch (RepositoryException e) {
			throw new ArgeoException("Unable to list " + nodeType, e);
		}
	}

	/* DEPENDENCY INJECTION */
	public void setPeopleService(PeopleService peopleService) {
		this.peopleService = peopleService;
		try {
			session = peopleService.getRepository().login();
		} catch (RepositoryException e) {
			throw new ArgeoException("Unable to initialize "
					+ "session for view " + ID, e);
		}
	}

	public void setRssManager(RssManager rssManager) {
		this.rssManager = rssManager;
	}

}
