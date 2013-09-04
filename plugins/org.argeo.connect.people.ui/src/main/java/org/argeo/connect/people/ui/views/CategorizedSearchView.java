package org.argeo.connect.people.ui.views;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.Ordering;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;
import javax.jcr.query.qom.StaticOperand;

import org.argeo.connect.film.FilmNames;
import org.argeo.connect.film.FilmTypes;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.JcrUiUtils;
import org.argeo.connect.people.ui.PeopleImages;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.PeopleUiService;
import org.argeo.connect.people.ui.providers.BasicNodeListContentProvider;
import org.argeo.connect.people.ui.providers.EntitySingleColumnLabelProvider;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

/** A Sample of a list display in a table with a quick search field. */
public class CategorizedSearchView extends ViewPart {

	// private final static Log log = LogFactory.getLog(QuickSearchView.class);
	public static final String ID = PeopleUiPlugin.PLUGIN_ID
			+ ".categorizedSearchView";

	private final static Integer ROW_LIMIT = 5;
	// private final static Integer ROW_HEIGHT = 20;
	private final static Integer HEIGHT_HINT = 105;

	/* DEPENDENCY INJECTION */
	private Session session;
	private PeopleService peopleService;
	private PeopleUiService peopleUiService;

	// This page widgets
	private TableViewer personViewer;
	private TableViewer orgViewer;
	private TableViewer filmViewer;
	private Text filterTxt;
	private final static String FILTER_HELP_MSG = "Enter filter criterion";

	// enable to set a big padding only on the first subtitle
	private boolean alreadyGotAList = false;

	@Override
	public void createPartControl(Composite parent) {
		// MainLayout
		parent.setLayout(new GridLayout(1, false));

		// Header
		createFilterHeader(parent);

		// short result parts
		personViewer = createListPart(parent, "Persons ",
				new EntitySingleColumnLabelProvider(), false);
		orgViewer = createListPart(parent, "Organisations ",
				new EntitySingleColumnLabelProvider(), false);
		filmViewer = createListPart(parent, "Films ",
				new EntitySingleColumnLabelProvider(), false);
		// set data
		refreshFilteredList();
	}

	public void createFilterHeader(Composite parent) {
		// The logo
		Label image = new Label(parent, SWT.NONE);
		image.setBackground(parent.getBackground());
		image.setImage(PeopleImages.LOGO);
		image.setLayoutData(new GridData());

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
				refreshFilteredList();
			}
		});
	}

	protected TableViewer createListPart(Composite parent, String title,
			ILabelProvider labelProvider, boolean isLastList) {
		// SUBTITLE : name of the current list
		Composite titleComposite = new Composite(parent, SWT.NONE);
		// special padding only for the first list
		if (!alreadyGotAList) {
			titleComposite.setData(RWT.CUSTOM_VARIANT,
					PeopleUiConstants.PEOPLE_CSS_TITLE_COMPOSITE_FIRST);
			alreadyGotAList = true;
		} else
			titleComposite.setData(RWT.CUSTOM_VARIANT,
					PeopleUiConstants.PEOPLE_CSS_TITLE_COMPOSITE);
		titleComposite.setLayoutData(new GridData(
				GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		titleComposite.setLayout(new FillLayout());
		Label lbl = new Label(titleComposite, SWT.NONE);
		lbl.setText(title);
		lbl.setData(RWT.CUSTOM_VARIANT,
				PeopleUiConstants.PEOPLE_CSS_LIST_SUBTITLE);

		// Create an intermediate composite to enable single column to occupy
		// the full width
		// int style = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL
		// | SWT.FULL_SELECTION;
		Composite tableComposite = new Composite(parent, SWT.NONE);

		GridData gd = null;
		if (isLastList)
			gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL
					| GridData.GRAB_VERTICAL | GridData.VERTICAL_ALIGN_FILL
					| GridData.GRAB_HORIZONTAL);
		else {
			gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL
					| GridData.VERTICAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
			gd.heightHint = HEIGHT_HINT;
		}
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
		// Enable markups
		table.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
		table.setData(RWT.CUSTOM_ITEM_HEIGHT, Integer.valueOf(20));

		v.setContentProvider(new BasicNodeListContentProvider());
		v.addDoubleClickListener(peopleUiService
				.getNewNodeListDoubleClickListener(peopleService, null));
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
			List<Node> persons = JcrUiUtils.nodeIteratorToList(
					doSearch(session, filterTxt.getText(),
							PeopleTypes.PEOPLE_PERSON,
							PeopleNames.PEOPLE_LAST_NAME,
							PeopleNames.PEOPLE_PRIMARY_EMAIL), ROW_LIMIT);
			personViewer.setInput(persons);

			List<Node> orgs = JcrUiUtils.nodeIteratorToList(
					doSearch(session, filterTxt.getText(),
							PeopleTypes.PEOPLE_ORGANIZATION,
							PeopleNames.PEOPLE_LEGAL_NAME, null), ROW_LIMIT);
			orgViewer.setInput(orgs);

			List<Node> films = JcrUiUtils.nodeIteratorToList(
					doSearch(session, filterTxt.getText(), FilmTypes.FILM,
							FilmNames.FILM_ORIGINAL_TITLE, null), ROW_LIMIT);
			filmViewer.setInput(films);
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to list persons", e);
		}
	}

	/** Build repository request */
	private NodeIterator doSearch(Session session, String filter,
			String typeName, String orderProperty, String orderProperty2)
			throws RepositoryException {
		QueryManager queryManager = session.getWorkspace().getQueryManager();
		QueryObjectModelFactory factory = queryManager.getQOMFactory();

		Selector source = factory.selector(typeName, "selector");

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
				StaticOperand so = factory.literal(session.getValueFactory()
						.createValue("*" + token + "*"));
				Constraint currC = factory.fullTextSearch(
						source.getSelectorName(), null, so);
				if (defaultC == null)
					defaultC = currC;
				else
					defaultC = factory.and(defaultC, currC);
			}
		}

		Ordering order = null, order2 = null;

		if (orderProperty != null && !"".equals(orderProperty.trim()))
			order = factory.ascending(factory.lowerCase(factory.propertyValue(
					source.getSelectorName(), orderProperty)));
		if (orderProperty2 != null && !"".equals(orderProperty2.trim()))
			order2 = factory.ascending(factory.propertyValue(
					source.getSelectorName(), orderProperty2));

		QueryObjectModel query;
		if (order == null) {
			query = factory.createQuery(source, defaultC, null, null);
		} else {
			if (order2 == null)
				query = factory.createQuery(source, defaultC,
						new Ordering[] { order }, null);
			else
				query = factory.createQuery(source, defaultC, new Ordering[] {
						order, order2 }, null);
		}

		QueryResult result = query.execute();
		return result.getNodes();
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
	// return new Status(IStatus.ERROR, MsmUiPlugin.PLUGIN_ID,
	// "Cannot get filtered list", e);
	// }
	// return Status.OK_STATUS;
	// }
	// }

	/* DEPENDENCY INJECTION */
	public void setPeopleService(PeopleService msmService) {
		this.peopleService = msmService;
		try {
			session = msmService.getRepository().login();
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to initialize "
					+ "session for view " + ID, e);
		}
	}

	public void setMsmUiService(PeopleUiService peopleUiService) {
		this.peopleUiService = peopleUiService;
	}
}