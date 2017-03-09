package org.argeo.connect.resources.workbench;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.connect.ConnectException;
import org.argeo.connect.resources.ResourcesNames;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.resources.ResourcesTypes;
import org.argeo.connect.ui.util.BasicNodeListContentProvider;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.workbench.ConnectUiPlugin;
import org.argeo.connect.workbench.ConnectWorkbenchUtils;
import org.argeo.connect.workbench.Refreshable;
import org.argeo.connect.workbench.SystemWorkbenchService;
import org.argeo.connect.workbench.util.JcrViewerDClickListener;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.eclipse.ui.jcr.lists.SimpleJcrNodeLabelProvider;
import org.argeo.eclipse.ui.utils.ViewerUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.runtime.IProgressMonitor;
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
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

/**
 * Display a list off all projects and corresponding editions of the current
 * manager instance.
 */
public class TemplateList extends EditorPart implements Refreshable {

	public final static String ID = ConnectUiPlugin.PLUGIN_ID + ".templateList";

	/* DEPENDENCY INJECTION */
	private Repository repository;
	private ResourcesService resourceService;
	private SystemWorkbenchService systemWorkbenchService;

	// Context
	private Session session;

	// UI Objects
	private TableViewer tableViewer;

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
		setPartName("Catalogues");
		session = ConnectJcrUtils.login(repository);
	}

	// MAIN LAYOUT
	public void createPartControl(Composite parent) {
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

		ConnectWorkbenchUtils.setTableDefaultStyle(tableViewer, 25);
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

	@Override
	public void setFocus() {
		// filterTxt.setFocus();
	}

	@Override
	public void dispose() {
		JcrUtils.logoutQuietly(session);
		super.dispose();
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

	// Unused compulsory methods
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
}
