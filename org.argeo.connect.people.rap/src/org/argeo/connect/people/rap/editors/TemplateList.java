package org.argeo.connect.people.rap.editors;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ResourceService;
import org.argeo.connect.people.rap.PeopleRapPlugin;
import org.argeo.connect.people.rap.PeopleRapUtils;
import org.argeo.connect.people.rap.PeopleWorkbenchService;
import org.argeo.connect.people.rap.listeners.PeopleJcrViewerDClickListener;
import org.argeo.connect.people.rap.providers.BasicNodeListContentProvider;
import org.argeo.connect.people.rap.utils.Refreshable;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
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
public class TemplateList extends EditorPart implements PeopleNames,
		Refreshable {

	public final static String ID = PeopleRapPlugin.PLUGIN_ID + ".templateList";

	/* DEPENDENCY INJECTION */
	private Session session;
	private PeopleService peopleService;
	private ResourceService resourceService;
	private PeopleWorkbenchService peopleWorkbenchService;

	// UI Objects
	private TableViewer tableViewer;

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
		setPartName("Catalogues");
	}

	// MAIN LAYOUT
	public void createPartControl(Composite parent) {
		parent.setLayout(PeopleUiUtils.noSpaceGridLayout());

		Composite buttonCmp = new Composite(parent, SWT.NO_FOCUS);
		populateButtonCmp(buttonCmp);
		buttonCmp.setLayoutData(PeopleUiUtils.horizontalFillData());

		// The table itself
		Composite tableCmp = new Composite(parent, SWT.NO_FOCUS);
		createListPart(tableCmp);
		tableCmp.setLayoutData(PeopleUiUtils.fillGridData());
		refreshList();
	}

	// Header
	protected void populateButtonCmp(Composite parent) {
		parent.setLayout(new GridLayout());
		Button uploadBtn = new Button(parent, SWT.PUSH);
		uploadBtn
				.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
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

		PeopleRapUtils.setTableDefaultStyle(tableViewer, 25);
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		TableViewerColumn col;
		int[] bounds = { 120, 110, 100, 50 };

		// Name
		col = ViewerUtils.createTableViewerColumn(tableViewer, "", SWT.LEFT,
				bounds[0]);
		col.setLabelProvider(new SimpleJcrNodeLabelProvider(
				PeopleNames.PEOPLE_TEMPLATE_ID));
		tableColumnLayout.setColumnData(col.getColumn(), new ColumnWeightData(
				100, 150, true));

		// Providers and listeners
		tableViewer.setContentProvider(new BasicNodeListContentProvider());

		tableViewer.addDoubleClickListener(new PeopleJcrViewerDClickListener(
				peopleWorkbenchService));
		// tableViewer.getTable().addSelectionListener(new HtmlRwtAdapter());
		parent.setLayout(tableColumnLayout);
	}

	/** Refresh the list of registered templates */
	protected void refreshList() {
		try {
			String path = peopleService
					.getBasePath(PeopleConstants.PEOPLE_RESOURCE)
					+ "/"
					+ PeopleNames.PEOPLE_RESOURCE_TEMPLATE;
			Node parent = session.getNode(path);
			List<Node> templates = new ArrayList<Node>();

			NodeIterator nit = parent.getNodes();
			while (nit.hasNext()) {
				Node currNode = nit.nextNode();
				if (currNode.isNodeType(PeopleTypes.PEOPLE_NODE_TEMPLATE))
					templates.add(currNode);
			}

			tableViewer.setInput(templates);
			tableViewer.refresh();
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to list node templates ", e);
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

	/* DEPENDENCY INJECTION */
	public void setRepository(Repository repository) {
		session = CommonsJcrUtils.login(repository);
	}

	public void setPeopleWorkbenchService(
			PeopleWorkbenchService peopleWorkbenchService) {
		this.peopleWorkbenchService = peopleWorkbenchService;
	}

	public void setPeopleService(PeopleService peopleService) {
		this.peopleService = peopleService;
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