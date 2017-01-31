package org.argeo.connect.people.workbench.rap.views;

import static org.argeo.eclipse.ui.EclipseUiUtils.notEmpty;

import javax.jcr.NodeIterator;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.ConnectConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.workbench.PeopleWorkbenchService;
import org.argeo.connect.people.workbench.rap.PeopleRapPlugin;
import org.argeo.connect.people.workbench.rap.listeners.PeopleJcrViewerDClickListener;
import org.argeo.connect.people.workbench.rap.providers.BasicNodeListContentProvider;
import org.argeo.connect.people.workbench.rap.providers.EntitySingleColumnLabelProvider;
import org.argeo.connect.ui.widgets.DelayedText;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.util.XPathUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.rap.rwt.service.ServerPushSession;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;

/** A Sample of a list display in a table with a quick search field. */
public class QuickSearchView extends ViewPart {
	private final static Log log = LogFactory.getLog(QuickSearchView.class);
	public static final String ID = PeopleRapPlugin.PLUGIN_ID + ".quickSearchView";

	/* DEPENDENCY INJECTION */
	private Repository repository;
	private Session session;
	private PeopleService peopleService;
	private PeopleWorkbenchService peopleWorkbenchService;

	// This page widgets
	private TableViewer entityViewer;
	private DelayedText filterTxt;

	@Override
	public void createPartControl(Composite parent) {
		session = ConnectJcrUtils.login(repository);
		// MainLayout
		parent.setLayout(new GridLayout());
		addFilterPanel(parent);
		entityViewer = createListPart(parent,
				new EntitySingleColumnLabelProvider(peopleService, peopleWorkbenchService));
		refreshFilteredList();
	}

	public void addFilterPanel(Composite parent) {
		// Use a delayed text: the query won't be done until the user stop
		// typing for 800ms
		int style = SWT.BORDER | SWT.SEARCH | SWT.ICON_CANCEL;
		filterTxt = new DelayedText(parent, style, PeopleUiConstants.SEARCH_TEXT_DELAY);
		filterTxt.setLayoutData(EclipseUiUtils.fillWidth());

		final ServerPushSession pushSession = new ServerPushSession();
		filterTxt.addDelayedModifyListener(pushSession, new ModifyListener() {
			private static final long serialVersionUID = 5003010530960334977L;

			public void modifyText(ModifyEvent event) {
				filterTxt.getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						refreshFilteredList();
					}
				});
				pushSession.stop();
			}
		});

		// Jump to the first item of the list using the down arrow
		filterTxt.addKeyListener(new KeyListener() {
			private static final long serialVersionUID = -4523394262771183968L;

			@Override
			public void keyReleased(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// boolean shiftPressed = (e.stateMask & SWT.SHIFT) != 0;
				// boolean altPressed = (e.stateMask & SWT.ALT) != 0;
				if (e.keyCode == SWT.ARROW_DOWN || e.keyCode == SWT.TAB) {
					Object first = entityViewer.getElementAt(0);
					if (first != null) {
						entityViewer.getTable().setFocus();
						entityViewer.setSelection(new StructuredSelection(first), true);
					}
					e.doit = false;
				}
			}
		});
	}

	protected TableViewer createListPart(Composite parent, ILabelProvider labelProvider) {
		parent.setLayout(new GridLayout());

		Composite tableComposite = new Composite(parent, SWT.NONE);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_VERTICAL
				| GridData.VERTICAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
		tableComposite.setLayoutData(gd);

		TableViewer v = new TableViewer(tableComposite);
		v.setLabelProvider(labelProvider);

		TableColumn singleColumn = new TableColumn(v.getTable(), SWT.V_SCROLL);
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		tableColumnLayout.setColumnData(singleColumn, new ColumnWeightData(85));
		tableComposite.setLayout(tableColumnLayout);

		// Corresponding table & style
		Table table = v.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(false);
		CmsUtils.markup(table);
		CmsUtils.setItemHeight(table, 26);

		v.setContentProvider(new BasicNodeListContentProvider());
		v.addDoubleClickListener(new PeopleJcrViewerDClickListener(peopleWorkbenchService));
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

	protected void refreshFilteredList() {
		try {
			String filter = filterTxt.getText();
			// Prevents the query on the full repository
			// if (isEmpty(filter)) {
			// entityViewer.setInput(null);
			// return;
			// }
			QueryManager queryManager = session.getWorkspace().getQueryManager();

			// XPATH Query
			String xpathQueryStr = "//element(*, " + PeopleTypes.PEOPLE_ENTITY + ")";
			String xpathFilter = XPathUtils.getFreeTextConstraint(filter);
			if (notEmpty(xpathFilter))
				xpathQueryStr += "[" + xpathFilter + "]";

			// boolean doOrder = orderResultsBtn != null
			// && !(orderResultsBtn.isDisposed())
			// && orderResultsBtn.getSelection();
			// if (doOrder) {
			// xpathQueryStr += " order by jcr:title";
			// }

			long begin = System.currentTimeMillis();
			Query xpathQuery = queryManager.createQuery(xpathQueryStr, ConnectConstants.QUERY_XPATH);

			// xpathQuery.setLimit(TrackerUiConstants.SEARCH_DEFAULT_LIMIT);
			QueryResult result = xpathQuery.execute();

			NodeIterator nit = result.getNodes();
			entityViewer.setInput(JcrUtils.nodeIteratorToList(nit));

			if (log.isDebugEnabled()) {
				long end = System.currentTimeMillis();
				log.debug("Quick Search - Found: " + nit.getSize() + " in " + (end - begin)
						+ " ms by executing XPath query (" + xpathQueryStr + ").");
			}
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to list persons", e);
		}
	}

	/* DEPENDENCY INJECTION */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setPeopleService(PeopleService peopleService) {
		this.peopleService = peopleService;
	}

	public void setPeopleWorkbenchService(PeopleWorkbenchService peopleWorkbenchService) {
		this.peopleWorkbenchService = peopleWorkbenchService;
	}
}
