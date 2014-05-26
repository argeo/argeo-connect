package org.argeo.connect.people.ui.editors;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.DynamicOperand;
import javax.jcr.query.qom.Ordering;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelConstants;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;
import javax.jcr.query.qom.StaticOperand;

import org.argeo.ArgeoException;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.PeopleUiService;
import org.argeo.connect.people.ui.composites.PeopleVirtualTableViewer;
import org.argeo.connect.people.ui.editors.utils.EntityEditorInput;
import org.argeo.connect.people.ui.extracts.PeopleColumnDefinition;
import org.argeo.connect.people.ui.listeners.PeopleJcrViewerDClickListener;
import org.argeo.connect.people.ui.providers.GroupLabelProvider;
import org.argeo.connect.people.ui.utils.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.eclipse.ui.jcr.lists.SimpleJcrRowLabelProvider;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.EditorPart;

/**
 * Editor that display a list of entity that has a given Tag.
 */
public class TagEditor extends EditorPart implements PeopleNames {

	public final static String ID = PeopleUiPlugin.PLUGIN_ID + ".tagEditor";

	private TableViewer membersViewer;
	private Text filterTxt;
	// Keep a local cache of the currently displayed rows.
	@SuppressWarnings("unused")
	private Row[] rows;

	// Default column
	private List<PeopleColumnDefinition> colDefs = new ArrayList<PeopleColumnDefinition>();
	{
		colDefs.add(new PeopleColumnDefinition(PeopleTypes.PEOPLE_ENTITY,
				Property.JCR_TITLE, PropertyType.STRING, "Display Name",
				new TitleWithIconLP(PeopleTypes.PEOPLE_ENTITY,
						Property.JCR_TITLE), 300));
		colDefs.add(new PeopleColumnDefinition(PeopleTypes.PEOPLE_ENTITY,
				PEOPLE_TAGS, PropertyType.STRING, "Tags",
				new SimpleJcrRowLabelProvider(PeopleTypes.PEOPLE_ENTITY,
						PEOPLE_TAGS), 300));
	};

	/* DEPENDENCY INJECTION */
	private PeopleUiService peopleUiService;
	private Repository repository;
	private Session session;

	// Business Objects
	private Node node;
	protected FormToolkit toolkit;

	// LIFE CYCLE
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
		try {
			session = repository.login();
			EntityEditorInput sei = (EntityEditorInput) getEditorInput();
			node = session.getNodeByIdentifier(sei.getUid());
		} catch (RepositoryException e) {
			throw new ArgeoException("Unable to create new session"
					+ " to use with current editor", e);
		}
		// Name and tooltip
		String name = CommonsJcrUtils.get(node, Property.JCR_TITLE);
		if (CommonsJcrUtils.checkNotEmptyString(name))
			setPartName(name);
		setTitleToolTip("List contacts tagged as " + name);
	}

	/* CONTENT CREATION */
	@Override
	public void createPartControl(Composite parent) {
		// Initialize main UI objects
		toolkit = new FormToolkit(parent.getDisplay());
		Form form = toolkit.createForm(parent);
		Composite main = form.getBody();
		createMainLayout(main);
	}

	protected void createMainLayout(Composite parent) {
		parent.setLayout(PeopleUiUtils.gridLayoutNoBorder());
		// The header
		Composite header = toolkit.createComposite(parent, SWT.NO_FOCUS);
		header.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		populateHeader(header);
		// the body
		Composite body = toolkit.createComposite(parent, SWT.NO_FOCUS);
		body.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createMembersList(body, getNode());
	}

	protected void populateHeader(final Composite parent) {
		parent.setLayout(new GridLayout());
		final Label titleROLbl = toolkit.createLabel(parent, "", SWT.WRAP);
		titleROLbl.setData(PeopleUiConstants.MARKUP_ENABLED, Boolean.TRUE);
		final ColumnLabelProvider groupTitleLP = new GroupLabelProvider(
				PeopleUiConstants.LIST_TYPE_OVERVIEW_TITLE);
		titleROLbl.setText(groupTitleLP.getText(getNode()));
	}

	public void createMembersList(Composite parent, final Node entity) {
		parent.setLayout(new GridLayout());

		// First line: search Text and buttons
		filterTxt = createFilterText(parent);
		filterTxt.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		// Corresponding list
		Composite tableComp = toolkit.createComposite(parent);
		tableComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		membersViewer = createTableViewer(tableComp);
		membersViewer.setContentProvider(new MyLazyContentProvider(
				membersViewer));
		refreshFilteredList();

		// Double click
		PeopleJcrViewerDClickListener ndcl = new PeopleJcrViewerDClickListener(
				PeopleTypes.PEOPLE_ENTITY, peopleUiService);
		membersViewer.addDoubleClickListener(ndcl);
	}

	private Text createFilterText(Composite parent) {
		Text filterTxt = toolkit.createText(parent, "", SWT.BORDER | SWT.SEARCH
				| SWT.ICON_SEARCH | SWT.ICON_CANCEL);
		filterTxt.setMessage(PeopleUiConstants.FILTER_HELP_MSG);
		filterTxt.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		filterTxt.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 5003010530960334977L;

			public void modifyText(ModifyEvent event) {
				refreshFilteredList();
			}
		});
		return filterTxt;
	}

	/** Refresh the table viewer based on the free text search field */
	protected void refreshFilteredList() {
		try {
			QueryManager queryManager = session.getWorkspace()
					.getQueryManager();
			QueryObjectModelFactory factory = queryManager.getQOMFactory();
			Selector source = factory.selector(PeopleTypes.PEOPLE_ENTITY,
					PeopleTypes.PEOPLE_ENTITY);

			String filter = filterTxt.getText();
			String currVal = CommonsJcrUtils.get(getNode(), Property.JCR_TITLE);
			StaticOperand so = factory.literal(session.getValueFactory()
					.createValue(currVal));
			DynamicOperand dyo = factory.propertyValue(
					source.getSelectorName(), PEOPLE_TAGS);
			Constraint constraint = factory.comparison(dyo,
					QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO, so);

			if (CommonsJcrUtils.checkNotEmptyString(filter)) {
				String[] strs = filter.trim().split(" ");
				for (String token : strs) {
					StaticOperand soTmp = factory.literal(session
							.getValueFactory().createValue("*" + token + "*"));
					Constraint currC = factory.fullTextSearch(
							source.getSelectorName(), null, soTmp);
					constraint = PeopleUiUtils.localAnd(factory, constraint,
							currC);
				}
			}

			Ordering order = factory.ascending(factory.propertyValue(
					source.getSelectorName(), Property.JCR_TITLE));
			Ordering[] orderings = { order };
			QueryObjectModel query = factory.createQuery(source, constraint,
					orderings, null);
			QueryResult result = query.execute();
			Row[] rows = CommonsJcrUtils.rowIteratorToArray(result.getRows());
			setViewerInput(rows);

		} catch (RepositoryException e) {
			throw new PeopleException(
					"Unable to list entities with static filter for tag "
							+ getNode(), e);
		}
	}

	private TableViewer createTableViewer(Composite parent) {
		parent.setLayout(new GridLayout());
		PeopleVirtualTableViewer tableCmp = new PeopleVirtualTableViewer(
				parent, SWT.MULTI, colDefs);
		TableViewer tableViewer = tableCmp.getTableViewer();
		tableCmp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tableViewer.addDoubleClickListener(new PeopleJcrViewerDClickListener(
				PeopleTypes.PEOPLE_ENTITY, peopleUiService));
		return tableViewer;
	}

	/** Use this method to update the result table */
	protected void setViewerInput(Row[] rows) {
		this.rows = rows;
		membersViewer.setInput(rows);
		// we must explicitly set the items count
		membersViewer.setItemCount(rows.length);
		membersViewer.refresh();
	}

	private class MyLazyContentProvider implements ILazyContentProvider {
		private static final long serialVersionUID = 1L;
		private TableViewer viewer;
		private Row[] elements;

		public MyLazyContentProvider(TableViewer viewer) {
			this.viewer = viewer;
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// IMPORTANT: don't forget this: an exception will be thrown if a
			// selected object is not part of the results anymore.
			viewer.setSelection(null);
			this.elements = (Row[]) newInput;
		}

		public void updateElement(int index) {
			viewer.replace(elements[index], index);
		}
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	/* EXPOSES TO CHILDREN CLASSES */
	/** Returns the entity Node that is bound to this editor */
	public Node getNode() {
		return node;
	}

	/* Private */

	private class TitleWithIconLP extends SimpleJcrRowLabelProvider {
		private static final long serialVersionUID = 6064779874148619776L;

		public TitleWithIconLP(String selectorName, String propertyName) {
			super(selectorName, propertyName);
		}

		@Override
		public Image getImage(Object element) {
			try {
				return peopleUiService.getIconForType(((Row) element)
						.getNode(PeopleTypes.PEOPLE_ENTITY));
			} catch (RepositoryException e) {
				throw new PeopleException("unable to retrieve image for "
						+ element, e);
			}
		}

	}

	/* UTILITES */
	protected boolean canSave() {
		return false;
	}

	@Override
	public void dispose() {
		JcrUtils.logoutQuietly(session);
		super.dispose();
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void setFocus() {
	}

	/* DEPENDENCY INJECTION */
	public void setPeopleService(PeopleService peopleService) {
		// this.peopleService = peopleService;
	}

	public void setPeopleUiService(PeopleUiService peopleUiService) {
		this.peopleUiService = peopleUiService;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

}