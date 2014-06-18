package org.argeo.connect.streams.web.views;

/** First draft of a user friendly pannel to manage RSS channels and feeds */
public class RssSearchView {
}
// extends ViewPart {
// // private final static Log log = LogFactory.getLog(RssSearchView.class);
// public static final String ID = StreamsActivator.PLUGIN_ID +
// ".rssSearchView";
//
// /* DEPENDENCY INJECTION */
// private Session session;
// private RssManager rssManager;
//
// // This page widgets
// private TableViewer sourcesViewer;
// private Text srcFilterTxt;
// private Text newSourceTxt;
// private Composite sourcesCmp;
//
// private TableViewer postsViewer;
// @SuppressWarnings("unused")
// private Text postsFilterTxt;
// private Composite postsCmp;
//
// private final static String CMD_SHOW_SOURCES = "showSources";
// private final static String CMD_SHOW_POSTS = "showPosts";
// private final static String CMD_OPEN_ALL_POSTS_EDITOR = "openAllPostsEditor";
// private final static String CMD_LOGOUT = "logout";
//
// private final static String FILTER_HELP_MSG = "Enter filter criterion";
// private final static String NEW_FEED_MSG = "Register a new RSS source";
//
// private final static int QUERY_LIMIT = 30;
//
// @Override
// public void createPartControl(Composite parent) {
// parent.setLayout(gridLayoutNoBorder());
//
// Composite logoPannel = new Composite(parent, SWT.NO_FOCUS);
// logoPannel.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
// | GridData.HORIZONTAL_ALIGN_FILL));
// addHeaderPanel(logoPannel);
//
// Composite bodyPannel = new Composite(parent, SWT.NO_FOCUS);
// bodyPannel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
// createBodyPanel(bodyPannel);
// }
//
// private void addHeaderPanel(Composite parent) {
// parent.setLayout(new FormLayout());
//
// // The BackGround
// Composite logoCmp = new Composite(parent, SWT.NO_FOCUS);
// FormData fdBg = createformData(0, 20, 100, 75);
// // fdBg.height = 56; // superstition ?
// logoCmp.setLayoutData(fdBg);
// logoCmp.setData(RWT.CUSTOM_VARIANT, "RSS-logoComposite");
//
// // The Image
// Label logoLbl = new Label(parent, SWT.NO_FOCUS);
// logoLbl.setImage(RssImages.LOGO_SMALL);
// logoLbl.setData(RWT.CUSTOM_VARIANT, "RSS-logo");
// logoLbl.setSize(130, 131);
// FormData fdImg = new FormData();
// fdImg.top = new FormAttachment(0, 0);
// fdImg.bottom = new FormAttachment(100, 0);
// fdImg.left = new FormAttachment(0, 0);
// logoLbl.setLayoutData(fdImg);
//
// // The links - we use a table as a workaround to enable customizable
// // links
// int style = SWT.NO_SCROLL;
// Table table = new Table(parent, style);
// table.setLinesVisible(false);
// table.setHeaderVisible(false);
// table.setData(RWT.CUSTOM_VARIANT, "RSS-logoTable");
// table.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
// table.setData(RWT.CUSTOM_ITEM_HEIGHT, Integer.valueOf(70));
//
// table.setLayoutData(createformData(75, 25, 98, 73));
// table.addSelectionListener(new SelectionAdapter() {
// private static final long serialVersionUID = 1L;
//
// public void widgetSelected(SelectionEvent event) {
// if (event.detail == RWT.HYPERLINK) {
// // here is the magic for the menu links
// if (CMD_SHOW_POSTS.equals(event.text))
// postsCmp.moveAbove(sourcesCmp);
// else if (CMD_SHOW_SOURCES.equals(event.text))
// sourcesCmp.moveAbove(postsCmp);
// else if (CMD_OPEN_ALL_POSTS_EDITOR.equals(event.text)) {
// try {
// SearchNodeEditorInput eei = new SearchNodeEditorInput(
// RssTypes.RSS_ITEM);
// PeopleUiPlugin.getDefault().getWorkbench()
// .getActiveWorkbenchWindow().getActivePage()
// .openEditor(eei, RssSearchPostEditor.ID);
// } catch (PartInitException pie) {
// throw new PeopleException(
// "Unexpected PartInitException while opening entity editor",
// pie);
// }
//
// }
//
// else if (CMD_LOGOUT.equals(event.text))
// CommandUtils.callCommand("org.eclipse.ui.file.exit");
//
// postsCmp.getParent().layout();
// sourcesCmp.getParent().layout();
// }
// }
// });
//
// // ViewerUtils.createColumn(table, "", SWT.RIGHT, 60);
// TableItem item = new TableItem(table, SWT.RIGHT);
// item.setData(RWT.CUSTOM_VARIANT, "RSS-logoTable");
// String styleStr = "style=\"font: 12px; color:e7eff4; "
// + "font-decoration:none;\"";
//
// StringBuilder builder = new StringBuilder();
// // builder.append("<a href=\"").append(CMD_SHOW_SOURCES).append("\" ");
// // builder.append(styleStr).append(" target=\"_rwt\">My Feeds</a>");
// // builder.append("<br/>");
// builder.append("<a href=\"").append(CMD_OPEN_ALL_POSTS_EDITOR);
// builder.append("\" ").append(styleStr);
// builder.append(" target=\"_rwt\">All Posts</a>");
// builder.append("<br/>");
// // builder.append("<a href=\"").append(CMD_SHOW_POSTS);
// // builder.append("\" ").append(styleStr);
// // builder.append(" target=\"_rwt\">Search Posts</a>");
// // builder.append("<br/>");
// builder.append("<a href=\"").append(CMD_LOGOUT);
// builder.append("\" ").append(styleStr);
// builder.append(" target=\"_rwt\">Logout</a>");
//
// item.setText(builder.toString());
// // Order layouts
// logoLbl.moveAbove(logoCmp);
// table.moveAbove(logoLbl);
// }
//
// public void createBodyPanel(Composite parent) {
// parent.setLayout(new FormLayout());
// // sources panel
// sourcesCmp = new Composite(parent, SWT.NO_FOCUS);
// sourcesCmp.setLayoutData(createformData(0, 0, 100, 100));
// populateSourcesPanel(sourcesCmp);
// // posts panel
// postsCmp = new Composite(parent, SWT.NO_FOCUS);
// postsCmp.setLayoutData(createformData(0, 0, 100, 100));
// populatePostsPanel(postsCmp);
//
// parent.layout();
// }
//
// public void populateSourcesPanel(Composite parent) {
// parent.setLayout(gridLayoutNoBorder());
//
// Composite addFeedCmp = new Composite(parent, SWT.NO_FOCUS);
// addFeedCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
// addChannelPanel(addFeedCmp);
//
// srcFilterTxt = new Text(parent, SWT.BORDER | SWT.SEARCH
// | SWT.ICON_SEARCH | SWT.ICON_CANCEL);
// sourcesViewer = createListPartWithRemove(parent);
//
// initializeFilterPanel(srcFilterTxt, sourcesViewer,
// RssTypes.RSS_CHANNEL_INFO);
// sourcesViewer.setInput(JcrUtils.nodeIteratorToList(doSearch(
// RssTypes.RSS_CHANNEL_INFO, "")));
// }
//
// public void populatePostsPanel(Composite parent) {
// parent.setLayout(gridLayoutNoBorder());
// Text txt = new Text(parent, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH
// | SWT.ICON_CANCEL);
//
// postsViewer = createListPart(parent, new RssListLblProvider());
// initializeFilterPanel(txt, postsViewer, RssTypes.RSS_ITEM);
// postsViewer.setInput(JcrUtils.nodeIteratorToList(doSearch(
// RssTypes.RSS_ITEM, "")));
//
// }
//
// public void addChannelPanel(Composite body) {
// // parent.setLayout(gridLayoutNoBorder());
// // Section section = new Section(parent, Section.TITLE_BAR
// // | Section.TWISTIE);
// // section.setText("Add a new channel");
// // section.setExpanded(false);
// // Composite body = new Composite(section, SWT.NONE);
// // section.setClient(body);
//
// GridLayout gl = new GridLayout(2, false);
// gl.marginHeight = gl.marginWidth = gl.verticalSpacing = 0;
// gl.horizontalSpacing = 5;
// body.setLayout(gl);
// // Text Area for the filter
// newSourceTxt = new Text(body, SWT.BORDER | SWT.ICON_CANCEL | SWT.SINGLE);
// newSourceTxt.setMessage(NEW_FEED_MSG);
// newSourceTxt
// .setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
//
// newSourceTxt.addTraverseListener(new TraverseListener() {
// private static final long serialVersionUID = 3928817201882691948L;
//
// public void keyTraversed(TraverseEvent e) {
// if (e.keyCode == SWT.CR) {
// registerRssLink(newSourceTxt.getText());
// e.doit = false;
// }
// }
// });
//
// Button okBtn = new Button(body, SWT.PUSH);
// okBtn.setText(" Add ");
//
// okBtn.addSelectionListener(new SelectionListener() {
// private static final long serialVersionUID = 5003010530960334977L;
//
// @Override
// public void widgetSelected(SelectionEvent e) {
// registerRssLink(newSourceTxt.getText());
// }
//
// @Override
// public void widgetDefaultSelected(SelectionEvent e) {
// }
// });
// }
//
// private boolean registerRssLink(String sourceStr) {
// try {
// Node channelInfo = rssManager
// .getOrCreateChannel(session, sourceStr).getNode(
// RssNames.RSS_CHANNEL_INFO);
// // TODO do it asynchroneously
// rssManager.retrieveItems();
// CommonsJcrUtils.saveAndCheckin(channelInfo);
// openEditorForId(channelInfo.getIdentifier());
// String filter = srcFilterTxt.getText();
// sourcesViewer.setInput(JcrUtils.nodeIteratorToList(doSearch(
// RssTypes.RSS_CHANNEL_INFO, filter)));
// newSourceTxt.setText("");
// } catch (RepositoryException e) {
// throw new ArgeoException("Unable to create a Stream", e);
// }
// return true;
// }
//
// public Text initializeFilterPanel(final Text txt, final TableViewer viewer,
// final String nodeType) {
// txt.setMessage(FILTER_HELP_MSG);
// txt.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
// | GridData.HORIZONTAL_ALIGN_FILL));
//
// txt.addModifyListener(new ModifyListener() {
// private static final long serialVersionUID = 5003010530960334977L;
//
// public void modifyText(ModifyEvent event) {
// String filter = txt.getText();
// viewer.setInput(JcrUtils.nodeIteratorToList(doSearch(nodeType,
// filter)));
// }
// });
// return txt;
// }
//
// protected TableViewer createListPartWithRemove(Composite parent) {
// parent.setLayout(new GridLayout());
//
// Composite tableComposite = new Composite(parent, SWT.NONE);
// GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL
// | GridData.GRAB_VERTICAL | GridData.VERTICAL_ALIGN_FILL
// | GridData.GRAB_HORIZONTAL);
// tableComposite.setLayoutData(gd);
//
// TableViewer v = new TableViewer(tableComposite);
// v.setLabelProvider(new RssListLblProvider());
//
// TableColumn singleColumn = new TableColumn(v.getTable(), SWT.LEFT);
// TableColumnLayout tableColumnLayout = new TableColumnLayout();
// tableColumnLayout.setColumnData(singleColumn, new ColumnWeightData(85));
// tableComposite.setLayout(tableColumnLayout);
//
// // Remove links
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
//
// TableViewerColumn col = ViewerUtils.createTableViewerColumn(v,
// "Edit/Remove links", SWT.NONE, 60);
// tableColumnLayout.setColumnData(col.getColumn(), new ColumnWeightData(
// 20, 50, true));
// col.setLabelProvider(new ColumnLabelProvider() {
// private static final long serialVersionUID = 1L;
//
// @Override
// public String getText(Object element) {
// return PeopleHtmlUtils.getRemoveSnippetForLists((Node) element,
// true);
// }
// });
//
// // Corresponding table & style
// Table table = v.getTable();
// table.setLinesVisible(false);
// table.setHeaderVisible(false);
//
// tableComposite.setLayout(tableColumnLayout);
//
// // Enable markups
// table.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
// table.setData(RWT.CUSTOM_ITEM_HEIGHT, Integer.valueOf(50));
// v.setContentProvider(new BasicNodeListContentProvider());
// v.addDoubleClickListener(new NodeListDoubleClickListener());
// return v;
// }
//
// protected TableViewer createListPart(Composite parent,
// ILabelProvider labelProvider) {
// parent.setLayout(new GridLayout());
//
// Composite tableComposite = new Composite(parent, SWT.NONE);
// GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL
// | GridData.GRAB_VERTICAL | GridData.VERTICAL_ALIGN_FILL
// | GridData.GRAB_HORIZONTAL);
// tableComposite.setLayoutData(gd);
//
// TableViewer v = new TableViewer(tableComposite);
// v.setLabelProvider(labelProvider);
//
// TableColumn singleColumn = new TableColumn(v.getTable(), SWT.V_SCROLL);
// TableColumnLayout tableColumnLayout = new TableColumnLayout();
// tableColumnLayout.setColumnData(singleColumn, new ColumnWeightData(85));
// tableComposite.setLayout(tableColumnLayout);
//
// // Corresponding table & style
// Table table = v.getTable();
// table.setLinesVisible(false);
// table.setHeaderVisible(false);
// // Enable markups
// table.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
// table.setData(RWT.CUSTOM_ITEM_HEIGHT, Integer.valueOf(50));
// v.setContentProvider(new BasicNodeListContentProvider());
// v.addDoubleClickListener(new NodeListDoubleClickListener());
// return v;
// }
//
// /* Helpers */
// private void openEditorForId(String uid) {
// try {
// EntityEditorInput eei = new EntityEditorInput(uid);
// IWorkbenchWindow iww = StreamsActivator.getDefault().getWorkbench()
// .getActiveWorkbenchWindow();
// iww.getActivePage().openEditor(eei, ChannelEditor.ID);
// } catch (PartInitException pie) {
// throw new ArgeoException(
// "Unexpected PartInitException while opening entity editor",
// pie);
// }
//
// }
//
// private GridLayout gridLayoutNoBorder() {
// GridLayout gl = new GridLayout(1, false);
// gl.marginWidth = gl.marginHeight = gl.horizontalSpacing = gl.verticalSpacing
// = 0;
// return gl;
// }
//
// private FormData createformData(int left, int top, int right, int bottom) {
// FormData formData = new FormData();
// formData.left = new FormAttachment(left, 0);
// formData.top = new FormAttachment(top, 0);
// formData.right = new FormAttachment(right, 0);
// formData.bottom = new FormAttachment(bottom, 0);
// return formData;
// }
//
// // protected void refreshFilteredList(String nodeType) {
// // }
//
// protected NodeIterator doSearch(String nodeType, String filter) {
// try {
// QueryManager queryManager = session.getWorkspace()
// .getQueryManager();
// QueryObjectModelFactory factory = queryManager.getQOMFactory();
//
// Selector source = factory.selector(nodeType, "selector");
//
// Constraint defaultC = null;
// // Parse the String
// String[] strs = filter.trim().split(" ");
// if (strs.length == 0) {
// StaticOperand so = factory.literal(session.getValueFactory()
// .createValue("*"));
// defaultC = factory.fullTextSearch(source.getSelectorName(),
// null, so);
// } else {
// for (String token : strs) {
// StaticOperand so = factory.literal(session
// .getValueFactory().createValue("*" + token + "*"));
// Constraint currC = factory.fullTextSearch(
// source.getSelectorName(), null, so);
// if (defaultC == null)
// defaultC = currC;
// else
// defaultC = factory.and(defaultC, currC);
// }
// }
// QueryObjectModel query = factory.createQuery(source, defaultC,
// null, null);
// query.setLimit(QUERY_LIMIT);
// QueryResult result = query.execute();
// NodeIterator ni = result.getNodes();
// return ni;
// } catch (RepositoryException e) {
// throw new ArgeoException("Unable to list " + nodeType, e);
// }
// }
//
// /* Life cycle management */
// @Override
// public void dispose() {
// JcrUtils.logoutQuietly(session);
// super.dispose();
// }
//
// @Override
// public void setFocus() {
// }
//
// /* DEPENDENCY INJECTION */
// public void setRepository(Repository repository) {
// try {
// session = repository.login();
// } catch (RepositoryException e) {
// throw new ArgeoException("Unable to initialize "
// + "session for view " + ID, e);
// }
// }
//
// public void setRssManager(RssManager rssManager) {
// this.rssManager = rssManager;
// }
// }