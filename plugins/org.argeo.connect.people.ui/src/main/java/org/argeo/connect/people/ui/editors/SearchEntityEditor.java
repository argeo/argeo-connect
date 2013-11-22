package org.argeo.connect.people.ui.editors;

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

import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.providers.BasicNodeListContentProvider;
import org.argeo.connect.people.ui.providers.EntitySingleColumnLabelProvider;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

/**
 * Search repository with a given entity type
 */
public class SearchEntityEditor extends EditorPart {
	// private final static Log log =
	// LogFactory.getLog(SearchEntityEditor.class);

	public final static String ID = PeopleUiPlugin.PLUGIN_ID
			+ ".searchEntityEditor";

	/* DEPENDENCY INJECTION */
	private PeopleService peopleService;
	private Session session;

	// This page widgets
	private TableViewer entityViewer;
	private Text filterTxt;

	// Business Objects
	private String entityType;

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
		SearchEntityEditorInput sei = (SearchEntityEditorInput) getEditorInput();
		entityType = sei.getName();
	}

	@Override
	public void dispose() {
		JcrUtils.logoutQuietly(session);
		super.dispose();
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout());
		createFilterPanel(parent);
		createListPart(parent);
		refreshFilteredList();
	}

	/**
	 * Overwrite to provide a specific Label Provider
	 * 
	 * @return
	 */
	protected ILabelProvider getCurrentLabelProvider() {
		return new EntitySingleColumnLabelProvider(peopleService);
	}

	/**
	 * Overwrite to set the correct row height
	 * 
	 */
	protected int getCurrRowHeight() {
		return 20;
	}

	@Override
	public void setFocus() {
	}

	public void createFilterPanel(Composite parent) {
		// Text Area for the filter
		filterTxt = new Text(parent, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH
				| SWT.ICON_CANCEL);
		filterTxt.setMessage(PeopleUiConstants.FILTER_HELP_MSG);
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

	protected void refreshFilteredList() {
		try {
			String filter = filterTxt.getText();
			QueryManager queryManager = session.getWorkspace()
					.getQueryManager();
			QueryObjectModelFactory factory = queryManager.getQOMFactory();

			Selector source = factory.selector(entityType, "selector");

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
			query.setLimit(PeopleConstants.QUERY_DEFAULT_LIMIT);
			QueryResult result = query.execute();
			entityViewer
					.setInput(JcrUtils.nodeIteratorToList(result.getNodes()));
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to list entity", e);
		}
	}

	protected void createListPart(Composite parent) {
		parent.setLayout(new GridLayout());

		Composite tableComposite = new Composite(parent, SWT.NONE);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.GRAB_VERTICAL | GridData.VERTICAL_ALIGN_FILL
				| GridData.GRAB_HORIZONTAL);
		tableComposite.setLayoutData(gd);

		TableViewer v = new TableViewer(tableComposite);
		v.setLabelProvider(getCurrentLabelProvider());

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
		table.setData(RWT.CUSTOM_ITEM_HEIGHT,
				Integer.valueOf(getCurrRowHeight()));
		v.setContentProvider(new BasicNodeListContentProvider());
		// v.addDoubleClickListener(peopleUiService
		// .getNewNodeListDoubleClickListener(peopleService));
		entityViewer = v;
	}

	// Exposes to children classes
	protected String getCurrNodeType() {
		return entityType;
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	/* DEPENDENCY INJECTION */
	public void setPeopleService(PeopleService peopleService) {
		this.peopleService = peopleService;
	}

	public void setRepository(Repository repository) {
		try {
			session = repository.login();
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to initialize "
					+ "session for editor " + ID, e);
		}

	}

}