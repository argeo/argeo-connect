package org.argeo.connect.people.workbench.rap.wizards;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.cms.ui.workbench.util.PrivilegedJob;
import org.argeo.connect.activities.ActivitiesNames;
import org.argeo.connect.activities.ActivitiesService;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.workbench.PeopleWorkbenchService;
import org.argeo.connect.people.workbench.rap.PeopleRapPlugin;
import org.argeo.connect.people.workbench.rap.composites.VirtualJcrTableViewer;
import org.argeo.connect.people.workbench.rap.providers.TitleIconRowLP;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.ui.ConnectColumnDefinition;
import org.argeo.connect.ui.workbench.AppWorkbenchService;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseJcrMonitor;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.eclipse.ui.jcr.lists.JcrColumnDefinition;
import org.argeo.eclipse.ui.utils.ViewerUtils;
import org.argeo.jcr.JcrMonitor;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

/**
 * Update the status of the selected tasks (with only one node type) as batch
 */
public class UpdateStatusWizard extends Wizard implements PeopleNames {
	// private final static Log log = LogFactory.getLog(EditTagWizard.class);

	// Context
	private final ResourcesService resourceService;
	private final ActivitiesService activityService;
	private final AppWorkbenchService peopleUiService;
	private final String taskId;
	private final Node[] nodes;
	// Cache to ease implementation
	private final Session session;

	// The status to impact
	private String chosenStatus;

	// Enable refresh of the calling editor at the end of the job
	// private final Display callingDisplay;
	// does not work should use a server push session

	/**
	 * @param callingDisplay
	 * @param session
	 * @param peopleService
	 * @param peopleWorkbenchService
	 * @param rows
	 * @param selectorName
	 * @param taskId
	 */
	public UpdateStatusWizard(Session session, ResourcesService resourceService, ActivitiesService activityService,
			PeopleWorkbenchService peopleWorkbenchService, Node[] nodes, String selectorName, String taskId) {
		this.session = session;
		this.activityService = activityService;
		this.resourceService = resourceService;
		this.peopleUiService = peopleWorkbenchService;
		this.nodes = nodes;
		this.taskId = taskId;
	}

	@Override
	public void addPages() {
		try {
			// configure container
			String title = "Batch status update";
			setWindowTitle(title);
			MainInfoPage inputPage = new MainInfoPage("Configure");
			addPage(inputPage);
			RecapPage recapPage = new RecapPage("Validate and launch");
			addPage(recapPage);
			// getContainer().updateButtons();
		} catch (Exception e) {
			throw new PeopleException("Cannot add page to wizard", e);
		}
	}

	/**
	 * Called when the user click on 'Finish' in the wizard. The task is then
	 * created and the corresponding session saved.
	 */
	@Override
	public boolean performFinish() {
		// try {
		// Sanity checks
		String errMsg = null;
		if (EclipseUiUtils.isEmpty(chosenStatus))
			errMsg = "Please pick up a new status";

		if (errMsg != null) {
			MessageDialog.openError(getShell(), "Unvalid information", errMsg);
			return false;
		}
		new UpdateStatusesJob(activityService, nodes, taskId, chosenStatus).schedule();
		return true;
	}

	@Override
	public boolean performCancel() {
		return true;
	}

	@Override
	public boolean canFinish() {
		return EclipseUiUtils.notEmpty(chosenStatus) && getContainer().getCurrentPage().getNextPage() == null;
	}

	protected class MainInfoPage extends WizardPage {
		private static final long serialVersionUID = 1L;

		private List<JcrColumnDefinition> colDefs = new ArrayList<JcrColumnDefinition>();
		{ // By default, it displays only title
			colDefs.add(new JcrColumnDefinition(null, Property.JCR_TITLE, PropertyType.STRING, "Nes Status", 420));
		};

		public MainInfoPage(String pageName) {
			super(pageName);
			setTitle("Select a status");
			setMessage("Choose the new status that will be applied " + "to the previously selected tasks.");
		}

		public void createControl(Composite parent) {
			// parent.setLayout(new GridLayout());
			Composite body = new Composite(parent, SWT.NO_FOCUS);
			body.setLayoutData(EclipseUiUtils.fillAll());
			GridLayout layout = new GridLayout();
			layout.marginTop = layout.marginWidth = 10;
			body.setLayout(layout);

			Composite box = new Composite(body, SWT.NO_FOCUS);
			box.setLayoutData(EclipseUiUtils.fillAll());

			int swtStyle = SWT.SINGLE;
			Table table = new Table(box, swtStyle);
			table.setLinesVisible(true);
			TableViewerColumn column;
			TableColumnLayout tableColumnLayout = new TableColumnLayout();
			TableViewer viewer = new TableViewer(table);

			column = ViewerUtils.createTableViewerColumn(viewer, "", SWT.NONE, 100);
			column.setLabelProvider(new ColumnLabelProvider());
			tableColumnLayout.setColumnData(column.getColumn(), new ColumnWeightData(100, 100, true));

			viewer.setContentProvider(new IStructuredContentProvider() {
				private static final long serialVersionUID = 7310636623175577101L;

				@Override
				public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				}

				@Override
				public void dispose() {
				}

				@Override
				public Object[] getElements(Object inputElement) {
					return (String[]) inputElement;
				}
			});

			viewer.addSelectionChangedListener(new MySelectionChangedListener());
			viewer.addDoubleClickListener(new MyDoubleClickListener());

			box.setLayout(tableColumnLayout);
			List<String> values = resourceService.getTemplateCatalogue(session, taskId,
					ActivitiesNames.ACTIVITIES_TASK_STATUS, null);
			viewer.setInput(values.toArray(new String[0]));
			setControl(body);
			body.setFocus();
		}

		class MySelectionChangedListener implements ISelectionChangedListener {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection().isEmpty())
					chosenStatus = null;
				else {
					Object obj = ((IStructuredSelection) event.getSelection()).getFirstElement();
					if (obj instanceof String) {
						chosenStatus = (String) obj;
					}
				}
				getContainer().updateButtons();
			}
		}

		class MyDoubleClickListener implements IDoubleClickListener {
			public void doubleClick(DoubleClickEvent evt) {
				if (evt.getSelection().isEmpty()) {
					chosenStatus = null;
				} else {
					Object obj = ((IStructuredSelection) evt.getSelection()).getFirstElement();
					if (obj instanceof String) {
						chosenStatus = (String) obj;
						getContainer().showPage(getNextPage());
					}
				}
			}
		}

		public boolean canFlipToNextPage() {
			return EclipseUiUtils.notEmpty(chosenStatus);
		}
	}

	protected class RecapPage extends WizardPage {
		private static final long serialVersionUID = 1L;

		public RecapPage(String pageName) {
			super(pageName);
		}

		public void setVisible(boolean visible) {
			super.setVisible(visible);
			if (visible == true) {
				setErrorMessage(null);
				setTitle("Update to " + chosenStatus + ": check and confirm.");
				setMessage("Your are about to update the below listed " + nodes.length + " task to status "
						+ chosenStatus + ". Are you sure you want to proceed?");
			}
		}

		public void createControl(Composite parent) {
			Composite body = new Composite(parent, SWT.NONE);
			body.setLayoutData(EclipseUiUtils.fillWidth());
			GridLayout layout = new GridLayout();
			layout.marginTop = layout.marginWidth = 10;
			body.setLayout(layout);
			ArrayList<ConnectColumnDefinition> colDefs = new ArrayList<ConnectColumnDefinition>();
			colDefs.add(new ConnectColumnDefinition("Display Name",
					new TitleIconRowLP(peopleUiService, null, Property.JCR_TITLE), 300));

			VirtualJcrTableViewer tableCmp = new VirtualJcrTableViewer(body, SWT.READ_ONLY, colDefs);
			TableViewer membersViewer = tableCmp.getTableViewer();
			membersViewer.setContentProvider(new MyLazyContentProvider(membersViewer));
			setViewerInput(membersViewer, nodes);
			// workaround the issue with fill layout and virtual viewer
			GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false);
			gd.heightHint = 400;
			tableCmp.setLayoutData(gd);
			setControl(body);
		}
	}

	/** Use this method to update the result table */
	protected void setViewerInput(TableViewer membersViewer, Node[] nodes) {
		membersViewer.setInput(nodes);
		// we must explicitly set the items count
		membersViewer.setItemCount(nodes.length);
		membersViewer.refresh();
	}

	private class MyLazyContentProvider implements ILazyContentProvider {
		private static final long serialVersionUID = 1L;
		private TableViewer viewer;
		private Node[] elements;

		public MyLazyContentProvider(TableViewer viewer) {
			this.viewer = viewer;
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// IMPORTANT: don't forget this: an exception will be thrown if a
			// selected object is not part of the results anymore.
			viewer.setSelection(null);
			this.elements = (Node[]) newInput;
		}

		public void updateElement(int index) {
			viewer.replace(elements[index], index);
		}
	}

	/** Privileged job that performs the update asynchronously */
	private class UpdateStatusesJob extends PrivilegedJob {

		final private Repository repository;
		final private ActivitiesService activityService;
		final private List<String> pathes = new ArrayList<String>();
		private final String chosenStatus;
		private final String taskTypeId;

		public UpdateStatusesJob(ActivitiesService activityService, Node[] toUpdateNodes, String taskTypeId,
				String chosenStatus) {
			super("Updating");

			this.activityService = activityService;
			this.taskTypeId = taskTypeId;
			this.chosenStatus = chosenStatus;
			try {
				repository = toUpdateNodes[0].getSession().getRepository();
				for (Node node : toUpdateNodes)
					pathes.add(node.getPath());
			} catch (RepositoryException e) {
				throw new PeopleException("Unable to initialise " + "status batch update ", e);
			}
		}

		protected IStatus doRun(IProgressMonitor progressMonitor) {
			Session session = null;
			try {
				JcrMonitor monitor = new EclipseJcrMonitor(progressMonitor);
				if (monitor != null && !monitor.isCanceled()) {
					monitor.beginTask("Updating objects", -1);

					session = repository.login();
					List<String> modifiedPaths = new ArrayList<String>();
					// TODO use transaction
					for (String currPath : pathes) {
						Node currNode = session.getNode(currPath);
						ConnectJcrUtils.checkCOStatusBeforeUpdate(currNode);
						boolean changed = activityService.updateStatus(taskTypeId, currNode, chosenStatus,
								modifiedPaths);
						if (changed)
							session.save();
					}
					ConnectJcrUtils.checkPoint(session, modifiedPaths, true);
					monitor.worked(1);
				}
			} catch (Exception e) {
				return new Status(IStatus.ERROR, PeopleRapPlugin.PLUGIN_ID, "Unable to perform batch update to status "
						+ chosenStatus + " on " + taskTypeId + " for node list ", e);
			} finally {
				JcrUtils.logoutQuietly(session);
			}
			return Status.OK_STATUS;
		}
	}
}