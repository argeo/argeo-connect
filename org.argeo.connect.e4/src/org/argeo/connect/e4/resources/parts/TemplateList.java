package org.argeo.connect.e4.resources.parts;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.connect.ConnectException;
import org.argeo.connect.resources.ResourcesNames;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.resources.ResourcesTypes;
import org.argeo.connect.ui.ConnectUiUtils;
import org.argeo.connect.ui.Refreshable;
import org.argeo.connect.ui.SystemWorkbenchService;
import org.argeo.connect.ui.util.BasicNodeListContentProvider;
import org.argeo.connect.ui.util.JcrViewerDClickListener;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.eclipse.ui.jcr.lists.SimpleJcrNodeLabelProvider;
import org.argeo.eclipse.ui.util.ViewerUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * Display a list off all projects and corresponding editions of the current
 * manager instance.
 */
public class TemplateList implements Refreshable {

	// public final static String ID = ConnectUiPlugin.PLUGIN_ID + ".templateList";

	/* DEPENDENCY INJECTION */
	@Inject
	private Repository repository;
	@Inject
	private ResourcesService resourceService;
	@Inject
	private SystemWorkbenchService systemWorkbenchService;

	// Context
	private Session session;

	// UI Objects
	private TableViewer tableViewer;

	public void init() {
		// setSite(site);
		// setInput(input);

		// setPartName("Catalogues");
		session = ConnectJcrUtils.login(repository);
	}

	// MAIN LAYOUT
	public void createPartControl(Composite parent) {
		init();
		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());

		Composite buttonCmp = new Composite(parent, SWT.NO_FOCUS);
		populateButtonCmp(buttonCmp);
		buttonCmp.setLayoutData(EclipseUiUtils.fillWidth());

		// The table itself
		Composite tableCmp = new Composite(parent, SWT.NO_FOCUS);
		createListPart(tableCmp);
		tableCmp.setLayoutData(EclipseUiUtils.fillAll());
		refreshList();
	}

	// Header
	protected void populateButtonCmp(Composite parent) {
		parent.setLayout(new GridLayout());
		Button uploadBtn = new Button(parent, SWT.PUSH);
		uploadBtn.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		uploadBtn.setText("Upload a Csv File");
		uploadBtn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
	}

	protected void createListPart(Composite parent) {
		tableViewer = new TableViewer(parent, SWT.V_SCROLL);

		ConnectUiUtils.setTableDefaultStyle(tableViewer, 25);
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		TableViewerColumn col;
		int[] bounds = { 120, 110, 100, 50 };

		// Name
		col = ViewerUtils.createTableViewerColumn(tableViewer, "", SWT.LEFT, bounds[0]);
		col.setLabelProvider(new NodeTypeLabelProvider(ResourcesNames.RESOURCES_TEMPLATE_ID));
		tableColumnLayout.setColumnData(col.getColumn(), new ColumnWeightData(100, 150, true));

		// Providers and listeners
		tableViewer.setContentProvider(new BasicNodeListContentProvider());

		tableViewer.addDoubleClickListener(new JcrViewerDClickListener());
		// tableViewer.getTable().addSelectionListener(new HtmlRwtAdapter());
		parent.setLayout(tableColumnLayout);
	}

	/** Refresh the list of registered templates */
	protected void refreshList() {
		try {
			String path = "/" + ResourcesNames.RESOURCES_BASE_NAME + "/" + ResourcesNames.RESOURCES_TEMPLATES;
			Node parent = session.getNode(path);
			List<Node> templates = new ArrayList<Node>();

			NodeIterator nit = parent.getNodes();
			while (nit.hasNext()) {
				Node currNode = nit.nextNode();
				if (currNode.isNodeType(ResourcesTypes.RESOURCES_NODE_TEMPLATE))
					templates.add(currNode);
			}

			tableViewer.setInput(templates);
			tableViewer.refresh();
		} catch (RepositoryException e) {
			throw new ConnectException("Unable to list node templates ", e);
		}
	}

	@Override
	public void forceRefresh(Object object) {
		refreshList();
	}

	@Focus
	public void setFocus() {
		// filterTxt.setFocus();
	}

	@PreDestroy
	public void dispose() {
		JcrUtils.logoutQuietly(session);
	}

	/**
	 * Returns the assigned to display name given a row that contains a Task
	 * selector
	 */
	private class NodeTypeLabelProvider extends SimpleJcrNodeLabelProvider {
		private static final long serialVersionUID = 1L;

		public NodeTypeLabelProvider(String propName) {
			super(propName);
		}

		@Override
		public String getText(Object element) {
			return resourceService.getItemDefaultEnLabel(super.getText(element));
		}
	}

	/* DEPENDENCY INJECTION */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setResourceService(ResourcesService resourceService) {
		this.resourceService = resourceService;
	}

	public void setSystemWorkbenchService(SystemWorkbenchService systemWorkbenchService) {
		this.systemWorkbenchService = systemWorkbenchService;
	}
}
