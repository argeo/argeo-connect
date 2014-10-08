package org.argeo.connect.people.rap.editors;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
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
import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.UserManagementService;
import org.argeo.connect.people.rap.PeopleRapConstants;
import org.argeo.connect.people.rap.PeopleRapPlugin;
import org.argeo.connect.people.rap.PeopleUiService;
import org.argeo.connect.people.rap.composites.PeopleVirtualTableViewer;
import org.argeo.connect.people.rap.editors.utils.EntityEditorInput;
import org.argeo.connect.people.rap.exports.PeopleColumnDefinition;
import org.argeo.connect.people.rap.listeners.PeopleJcrViewerDClickListener;
import org.argeo.connect.people.rap.providers.TagLabelProvider;
import org.argeo.connect.people.rap.providers.TitleWithIconLP;
import org.argeo.connect.people.rap.utils.PeopleUiUtils;
import org.argeo.connect.people.rap.utils.Refreshable;
import org.argeo.connect.people.rap.wizards.EditTagWizard;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.eclipse.ui.jcr.lists.SimpleJcrRowLabelProvider;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.EditorPart;

/**
 * Editor that display a filtered list of entities that have a given Tag.
 */
public class TagEditor extends EditorPart implements PeopleNames, Refreshable {
	public final static String ID = PeopleRapPlugin.PLUGIN_ID + ".tagEditor";

	/* DEPENDENCY INJECTION */
	private Repository repository;
	private Session session;
	private PeopleService peopleService;
	private PeopleUiService peopleUiService;

	// this page widgets.
	private List<PeopleColumnDefinition> colDefs;
	private TableViewer membersViewer;
	private Text filterTxt;
	// private Row[] rows; // Keep a local cache of the currently displayed
	// rows.

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

		// Initialize column definition
		// Cannot be statically done to have a valid reference to the injected
		// peopleUiService
		colDefs = new ArrayList<PeopleColumnDefinition>();
		colDefs.add(new PeopleColumnDefinition(PeopleTypes.PEOPLE_ENTITY,
				Property.JCR_TITLE, PropertyType.STRING, "Display Name",
				new TitleWithIconLP(peopleUiService, PeopleTypes.PEOPLE_ENTITY,
						Property.JCR_TITLE), 300));
		colDefs.add(new PeopleColumnDefinition(PeopleTypes.PEOPLE_ENTITY,
				PEOPLE_TAGS, PropertyType.STRING, "Tags",
				new SimpleJcrRowLabelProvider(PeopleTypes.PEOPLE_ENTITY,
						PEOPLE_TAGS), 300));
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
		parent.setLayout(PeopleUiUtils.noSpaceGridLayout());
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
		parent.setLayout(new GridLayout(2, false));
		final Label titleROLbl = toolkit.createLabel(parent, "", SWT.WRAP);
		titleROLbl.setData(PeopleRapConstants.MARKUP_ENABLED, Boolean.TRUE);
		final ColumnLabelProvider groupTitleLP = new TagLabelProvider(
				peopleService.getTagService(),
				PeopleRapConstants.LIST_TYPE_OVERVIEW_TITLE,
				peopleService.getBasePath(null), PeopleTypes.PEOPLE_ENTITY,
				PEOPLE_TAGS);
		titleROLbl.setText(groupTitleLP.getText(getNode()));
		titleROLbl
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Link editTitleLink = null;
		UserManagementService userService = peopleService
				.getUserManagementService();
		if (userService.isUserInRole(PeopleConstants.ROLE_BUSINESS_ADMIN)
				|| userService.isUserInRole(PeopleConstants.ROLE_ADMIN)) {
			editTitleLink = new Link(parent, SWT.NONE);
			editTitleLink.setText("<a>Edit Tag</a>");
		} else
			toolkit.createLabel(parent, "");

		if (editTitleLink != null) {
			editTitleLink.addSelectionListener(new SelectionAdapter() {
				private static final long serialVersionUID = 1L;

				@Override
				public void widgetSelected(final SelectionEvent event) {

					Wizard wizard = new EditTagWizard(
							peopleService,
							peopleUiService,
							getNode(),
							NodeType.NT_UNSTRUCTURED,
							peopleService
									.getResourceBasePath(PeopleConstants.RESOURCE_TAG),
							PeopleTypes.PEOPLE_ENTITY, PeopleNames.PEOPLE_TAGS,
							peopleService.getBasePath(null));
					WizardDialog dialog = new WizardDialog(titleROLbl
							.getShell(), wizard);
					int result = dialog.open();
					if (result == WizardDialog.OK) {
						titleROLbl.setText(groupTitleLP.getText(getNode()));
					}
				}
			});
		}

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
		filterTxt.setMessage(PeopleRapConstants.FILTER_HELP_MSG);
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
		// this.rows = rows;
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
	public void forceRefresh(Object object) {
		refreshFilteredList();
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
		this.peopleService = peopleService;
	}

	public void setPeopleUiService(PeopleUiService peopleUiService) {
		this.peopleUiService = peopleUiService;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

}