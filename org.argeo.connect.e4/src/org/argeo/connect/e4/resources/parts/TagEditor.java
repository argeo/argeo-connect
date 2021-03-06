package org.argeo.connect.e4.resources.parts;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.cms.auth.CurrentUser;
import org.argeo.cms.ui.eclipse.forms.FormToolkit;
import org.argeo.cms.ui.util.CmsUiUtils;
import org.argeo.connect.ConnectConstants;
import org.argeo.connect.ConnectException;
import org.argeo.connect.ConnectTypes;
import org.argeo.connect.SystemAppService;
import org.argeo.connect.e4.ConnectE4Constants;
import org.argeo.connect.resources.ResourcesNames;
import org.argeo.connect.resources.ResourcesRole;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.resources.core.TagUtils;
import org.argeo.connect.ui.AppWorkbenchService;
import org.argeo.connect.ui.ConnectColumnDefinition;
import org.argeo.connect.ui.ConnectUiConstants;
import org.argeo.connect.ui.IJcrTableViewer;
import org.argeo.connect.ui.Refreshable;
import org.argeo.connect.ui.SystemWorkbenchService;
import org.argeo.connect.ui.util.HtmlListRwtAdapter;
import org.argeo.connect.ui.util.JcrRowLabelProvider;
import org.argeo.connect.ui.util.JcrViewerDClickListener;
import org.argeo.connect.ui.util.MainNodeTypeLabelProvider;
import org.argeo.connect.ui.util.TagLabelProvider;
import org.argeo.connect.ui.util.TitleIconRowLP;
import org.argeo.connect.ui.util.VirtualJcrTableViewer;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.util.XPathUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.BrowserNavigation;
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

/** display a filtered list of entities for a given Tag */
public class TagEditor implements Refreshable, IJcrTableViewer {
	private final static Log log = LogFactory.getLog(TagEditor.class);
	// public final static String ID = ConnectUiPlugin.PLUGIN_ID + ".tagEditor";

	/* DEPENDENCY INJECTION */
	@Inject
	private Repository repository;
	@Inject
	private ResourcesService resourcesService;
	@Inject
	private SystemAppService systemAppService;
	@Inject
	private SystemWorkbenchService systemWorkbenchService;
	@Inject
	private MPart mPart;

	// UI Objects
	private Row[] rows;
	protected FormToolkit toolkit;
	private List<ConnectColumnDefinition> colDefs;
	private TableViewer membersViewer;
	private Text filterTxt;
	private Label titleROLbl;
	private ColumnLabelProvider groupTitleLP;

	// Context
	private Session session;
	private Node node;
	private String partName;

	// RAP specific
	private BrowserNavigation browserNavigation;

	// LIFE CYCLE
	public void init(String entityId) {
		// setSite(site);
		// setInput(input);
		// EntityEditorInput sei = (EntityEditorInput) getEditorInput();

		session = ConnectJcrUtils.login(repository);
		node = ConnectJcrUtils.getNodeByIdentifier(session, entityId);

		colDefs = new ArrayList<ConnectColumnDefinition>();
		colDefs.add(new ConnectColumnDefinition("Display Name",
				new TitleIconRowLP(systemWorkbenchService, null, Property.JCR_TITLE), 300));
		colDefs.add(new ConnectColumnDefinition("Other tags",
				new OtherTagsLabelProvider(resourcesService, systemWorkbenchService, node, null), 300));
	}

	protected void afterNameUpdate(String name) {
		if (EclipseUiUtils.isEmpty(name))
			name = ConnectJcrUtils.get(node, Property.JCR_TITLE);
		if (EclipseUiUtils.notEmpty(name)) {
			setPartName(name);
			// ((EntityEditorInput) getEditorInput()).setTooltipText("List contacts tagged
			// as " + name);
		}
		if (titleROLbl != null && !titleROLbl.isDisposed())
			titleROLbl.setText(groupTitleLP.getText(node));
	}

	protected void setPartName(String name) {
		this.partName = name;
		mPart.setLabel(name);
	}

	/* SPECIFIC CONFIGURATION */
	/** Overwrite to add the batch update features */
	protected boolean enableBatchUpdate() {
		return false;
	}

	/* CONTENT CREATION */
	@PostConstruct
	public void createPartControl(Composite parent) {
		String entityId = mPart.getPersistedState().get(ConnectE4Constants.ENTITY_ID);
		init(entityId);
		// Initialize main UI objects
		toolkit = new FormToolkit(parent.getDisplay());
		// Form form = toolkit.createForm(parent);
		// Composite main = form.getBody();
		// Composite main = toolkit.createComposite(parent);
		Composite main = new Composite(parent, SWT.NONE);
		createMainLayout(main);
		afterNameUpdate(null);

		browserNavigation = RWT.getClient().getService(BrowserNavigation.class);
	}

	protected void createMainLayout(Composite parent) {
		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());
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
		titleROLbl = toolkit.createLabel(parent, "", SWT.WRAP);
		CmsUiUtils.markup(titleROLbl);
		groupTitleLP = new TagLabelProvider(resourcesService, ConnectUiConstants.LIST_TYPE_OVERVIEW_TITLE);
		titleROLbl.setText(groupTitleLP.getText(getNode()));
		titleROLbl.setLayoutData(EclipseUiUtils.fillWidth());

		Link editTitleLink = null;
		if (CurrentUser.isInRole(ResourcesRole.editor.dn())) {
			editTitleLink = new Link(parent, SWT.NONE);
			editTitleLink.setText("<a>Edit Tag</a>");
		} else
			toolkit.createLabel(parent, "");

		if (editTitleLink != null) {
			editTitleLink.addSelectionListener(new SelectionAdapter() {
				private static final long serialVersionUID = 1L;

				@Override
				public void widgetSelected(final SelectionEvent event) {
					EditTagWizard wizard = new EditTagWizard(resourcesService, systemWorkbenchService, getNode(),
							ConnectConstants.RESOURCE_TAG, ResourcesNames.CONNECT_TAGS);
					WizardDialog dialog = new WizardDialog(titleROLbl.getShell(), wizard);
					// NoProgressBarWizardDialog dialog = new
					// NoProgressBarWizardDialog(titleROLbl.getShell(), wizard);
					dialog.open();
				}
			});
		}
	}

	/* Provide extraction ability */
	@Override
	public Row[] getElements(String extractId) {
		return rows;
	}

	@Override
	public List<ConnectColumnDefinition> getColumnDefinition(String extractId) {
		List<ConnectColumnDefinition> columns = new ArrayList<ConnectColumnDefinition>();
		columns.add(new ConnectColumnDefinition("Type", new MainNodeTypeLabelProvider(systemAppService)));
		columns.add(new ConnectColumnDefinition("Name", new JcrRowLabelProvider(Property.JCR_TITLE)));
		columns.add(new ConnectColumnDefinition("Tags", new JcrRowLabelProvider(ResourcesNames.CONNECT_TAGS)));
		columns.add(new ConnectColumnDefinition("Notes", new JcrRowLabelProvider(Property.JCR_DESCRIPTION)));
		return columns;
	}

	public void createMembersList(Composite parent, final Node entity) {
		parent.setLayout(new GridLayout());

		// First line: search Text and buttons
		filterTxt = createFilterText(parent);
		filterTxt.setLayoutData(EclipseUiUtils.fillWidth());

		// Corresponding list
		Composite tableComp = toolkit.createComposite(parent);
		tableComp.setLayoutData(EclipseUiUtils.fillAll());

		membersViewer = createTableViewer(tableComp);
		membersViewer.setContentProvider(new MyLazyContentProvider(membersViewer));
		refreshFilteredList();

		// Double click
		JcrViewerDClickListener ndcl = new JcrViewerDClickListener();
		membersViewer.addDoubleClickListener(ndcl);
	}

	private Text createFilterText(Composite parent) {
		Text filterTxt = toolkit.createText(parent, "", SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH | SWT.ICON_CANCEL);
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
		long begin = System.currentTimeMillis();
		try {

			Node tagParent = TagUtils.retrieveTagParentFromTag(node);
			String parentPath = tagParent.getProperty(ResourcesNames.RESOURCES_TAGGABLE_PARENT_PATH).getString();
			String xpathQueryStr = XPathUtils.descendantFrom(parentPath) + "//element(*, "
					+ ConnectTypes.CONNECT_TAGGABLE + ")";

			String filter = filterTxt.getText();
			String currVal = ConnectJcrUtils.get(getNode(), Property.JCR_TITLE);
			String freeTxtCond = XPathUtils.getFreeTextConstraint(filter);
			String mlNamecond = XPathUtils.getPropertyEquals(ResourcesNames.CONNECT_TAGS, currVal);
			String conditions = XPathUtils.localAnd(freeTxtCond, mlNamecond);
			if (EclipseUiUtils.notEmpty(conditions))
				xpathQueryStr += "[" + conditions + "]";
			Query xpathQuery = XPathUtils.createQuery(session, xpathQueryStr);
			RowIterator xPathRit = xpathQuery.execute().getRows();
			Row[] rows = ConnectJcrUtils.rowIteratorToArray(xPathRit);
			setViewerInput(rows);
			if (log.isDebugEnabled()) {
				long end = System.currentTimeMillis();
				log.debug("Found: " + xPathRit.getSize() + " members for tag " + getNode() + " in " + (end - begin)
						+ " ms by executing XPath query (" + xpathQueryStr + ").");
			}
		} catch (RepositoryException e) {
			throw new ConnectException("Unable to list entities with static filter for tag " + getNode(), e);
		}
	}

	private TableViewer createTableViewer(Composite parent) {
		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());
		VirtualJcrTableViewer tableCmp = new VirtualJcrTableViewer(parent, SWT.MULTI, colDefs, enableBatchUpdate());
		TableViewer tableViewer = tableCmp.getTableViewer();
		tableViewer.getTable().setHeaderVisible(false);
		tableCmp.setLayoutData(EclipseUiUtils.fillAll());
		tableViewer.getTable().addSelectionListener(new HtmlListRwtAdapter(systemWorkbenchService));
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
	public void forceRefresh(Object object) {
		afterNameUpdate(null);
		refreshFilteredList();
	}

	/* Expose to extending classes */
	protected Node getNode() {
		return node;
	}

	protected ResourcesService getResourcesService() {
		return resourcesService;
	}

	protected AppWorkbenchService getAppWorkbenchService() {
		return systemWorkbenchService;
	}

	/* Life Cycle */
	protected boolean canSave() {
		return false;
	}

	@PreDestroy
	public void dispose() {
		JcrUtils.logoutQuietly(session);
		browserNavigation.pushState("~", null);
	}

	@Focus
	public void setFocus() {
		try {
			browserNavigation.pushState(node.getPath(), partName);
		} catch (RepositoryException e) {
			log.error("Cannot set client state", e);
		}
	}

	/* DEPENDENCY INJECTION */
	// public void setRepository(Repository repository) {
	// this.repository = repository;
	// }
	//
	// public void setResourcesService(ResourcesService resourcesService) {
	// this.resourcesService = resourcesService;
	// }
	//
	// public void setSystemAppService(SystemAppService systemAppService) {
	// this.systemAppService = systemAppService;
	// }
	//
	// public void setSystemWorkbenchService(SystemWorkbenchService
	// systemWorkbenchService) {
	// this.systemWorkbenchService = systemWorkbenchService;
	// }
}
