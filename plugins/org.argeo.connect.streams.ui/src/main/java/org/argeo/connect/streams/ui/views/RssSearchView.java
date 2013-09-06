package org.argeo.connect.streams.ui.views;

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
import org.argeo.connect.people.ui.JcrUiUtils;
import org.argeo.connect.people.ui.providers.BasicNodeListContentProvider;
import org.argeo.connect.streams.RssService;
import org.argeo.connect.streams.RssTypes;
import org.argeo.connect.streams.ui.RssUiPlugin;
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

public class RssSearchView extends ViewPart {

	// private final static Log log = LogFactory.getLog(QuickSearchView.class);
	public static final String ID = RssUiPlugin.PLUGIN_ID + ".rssSearchView";

	/* DEPENDENCY INJECTION */
	private Session session;
	private RssService rssService;

	// This page widgets
	private TableViewer entityViewer;
	private Text filterTxt;
	private final static String FILTER_HELP_MSG = "Enter filter criterion";

	@Override
	public void createPartControl(Composite parent) {
		// MainLayout
		parent.setLayout(new GridLayout(1, false));

		addHeaderPanel(parent);
		addFilterPanel(parent);

		entityViewer = createListPart(parent, new RssListLabelProvider());

		// set data
		// refreshFilteredList();
	}

	public void addHeaderPanel(Composite parent) {
		parent.setLayout(new GridLayout());
		// The logo
		// Label image = new Label(parent, SWT.NONE);
		// image.setBackground(parent.getBackground());
		// image.setImage(DemoImages.DEMO_IMG_LOGO);
		// image.setLayoutData(new GridData());

		Link link = new Link(parent, SWT.NONE);
		link.setText("<a>Home</a>");
		link.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				// CommandUtils.callCommand(OpenDefaultEditor.ID);
			}
		});

		link = new Link(parent, SWT.NONE);
		link.setText("<a>A few Chanels</a>");
		link.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				openEditorForId(RssTypes.RSS_CHANNEL);
			}
		});

		link = new Link(parent, SWT.NONE);
		link.setText("<a>23035 Feeds </a>");
		link.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				openEditorForId(RssTypes.RSS_ITEM);
			}
		});
		//
		// link = new Link(parent, SWT.NONE);
		// link.setText("<a>15222 Films </a>");
		// link.addSelectionListener(new SelectionAdapter() {
		// private static final long serialVersionUID = 1L;
		//
		// @Override
		// public void widgetSelected(final SelectionEvent event) {
		// openEditorForId(FilmTypes.FILM);
		// }
		// });

	}

	private void openEditorForId(String entityType) {
		// try {
		// SearchEntityEditorInput eei = new SearchEntityEditorInput(
		// entityType);
		// PeopleUiPlugin.getDefault().getWorkbench()
		// .getActiveWorkbenchWindow().getActivePage()
		// .openEditor(eei, RssSearchEntityEditor.ID);
		// } catch (PartInitException pie) {
		// throw new PeopleException(
		// "Unexpected PartInitException while opening entity editor",
		// pie);
		// }

	}

	public void addFilterPanel(Composite parent) {
		// Text Area for the filter
		filterTxt = new Text(parent, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH
				| SWT.ICON_CANCEL);
		filterTxt.setMessage(FILTER_HELP_MSG);
		filterTxt.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_FILL));
		filterTxt.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 5003010530960334977L;

			public void modifyText(ModifyEvent event) {
				// might be better to use an asynchronous Refresh();
				refreshFilteredList(RssTypes.RSS_ITEM);
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
		table.setData(RWT.CUSTOM_ITEM_HEIGHT, Integer.valueOf(30));
		v.setContentProvider(new BasicNodeListContentProvider());
		v.addDoubleClickListener(new NodeListDoubleClickListener(rssService,
				null));
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
			entityViewer.setInput(JcrUiUtils.nodeIteratorToList(
					result.getNodes(), 30));
		} catch (RepositoryException e) {
			throw new ArgeoException("Unable to list " + nodeType, e);
		}
	}

	// private void asynchronousRefresh() {
	// RefreshJob job = new RefreshJob(filterTxt.getText(), personViewer,
	// getSite().getShell().getDisplay());
	// job.setUser(true);
	// job.schedule();
	// }
	//
	// private class RefreshJob extends PrivilegedJob {
	// private TableViewer viewer;
	// private String filter;
	// private Display display;
	//
	// public RefreshJob(String filter, TableViewer viewer, Display display) {
	// super("Get bundle list");
	// this.filter = filter;
	// this.viewer = viewer;
	// this.display = display;
	// }
	//
	// @Override
	// protected IStatus doRun(IProgressMonitor progressMonitor) {
	// try {
	// ArgeoMonitor monitor = new EclipseArgeoMonitor(progressMonitor);
	// monitor.beginTask("Filtering", -1);
	// final List<Node> result = JcrUiUtils.nodeIteratorToList(
	// listRelevantPersons(session, filter), 5);
	//
	// display.asyncExec(new Runnable() {
	// public void run() {
	// viewer.setInput(result);
	// }
	// });
	// } catch (Exception e) {
	// return new Status(IStatus.ERROR, RssUiPlugin.PLUGIN_ID,
	// "Cannot get filtered list", e);
	// }
	// return Status.OK_STATUS;
	// }
	// }

	/* DEPENDENCY INJECTION */
	public void setRssService(RssService rssService) {
		this.rssService = rssService;
		// try {
		// session = rssService.getRepository().login();
		// } catch (RepositoryException e) {
		// throw new RssException("Unable to initialize "
		// + "session for view " + ID, e);
		// }
	}

}
