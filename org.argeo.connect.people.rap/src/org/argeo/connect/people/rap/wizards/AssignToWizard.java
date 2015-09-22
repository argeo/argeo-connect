package org.argeo.connect.people.rap.wizards;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Row;

import org.argeo.ArgeoMonitor;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.rap.PeopleRapPlugin;
import org.argeo.connect.people.rap.PeopleWorkbenchService;
import org.argeo.connect.people.rap.composites.VirtualJcrTableViewer;
import org.argeo.connect.people.rap.providers.TitleIconRowLP;
import org.argeo.connect.people.ui.PeopleColumnDefinition;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.eclipse.ui.EclipseArgeoMonitor;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.eclipse.ui.utils.ViewerUtils;
import org.argeo.jcr.JcrUtils;
import org.argeo.security.ui.PrivilegedJob;
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

/** Update the status of the selected tasks (with only one node type) as batch */
public class AssignToWizard extends Wizard implements PeopleNames {
	// private final static Log log = LogFactory.getLog(EditTagWizard.class);

	// Context
	private final PeopleService peopleService;
	private final PeopleWorkbenchService peopleUiService;
	private final Row[] rows;
	private final String selectorName;
	// Cache to ease implementation
	private final Session session;

	// The status to impact
	private String chosenGroup;

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
	public AssignToWizard(Session session, PeopleService peopleService,
			PeopleWorkbenchService peopleWorkbenchService, Row[] rows,
			String selectorName) {
		this.session = session;
		this.peopleService = peopleService;
		this.peopleUiService = peopleWorkbenchService;
		this.rows = rows;
		this.selectorName = selectorName;
	}

	@Override
	public void addPages() {
		try {
			// configure container
			String title = "Batch assignation";
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
		if (CommonsJcrUtils.isEmptyString(chosenGroup))
			errMsg = "Please pick up a new group";

		if (errMsg != null) {
			MessageDialog.openError(getShell(), "Unvalid information", errMsg);
			return false;
		}
		new UpdateAssignmentJob(peopleService, rows, selectorName, chosenGroup)
				.schedule();
		return true;
	}

	@Override
	public boolean performCancel() {
		return true;
	}

	@Override
	public boolean canFinish() {
		return CommonsJcrUtils.checkNotEmptyString(chosenGroup)
				&& getContainer().getCurrentPage().getNextPage() == null;
	}

	protected class MainInfoPage extends WizardPage {
		private static final long serialVersionUID = 1L;

		public MainInfoPage(String pageName) {
			super(pageName);
			setTitle("Select a group");
			setMessage("Choose the group that must manage "
					+ "the previously selected tasks.");
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

			column = ViewerUtils.createTableViewerColumn(viewer, "", SWT.NONE,
					100);
			column.setLabelProvider(new ColumnLabelProvider());
			tableColumnLayout.setColumnData(column.getColumn(),
					new ColumnWeightData(100, 100, true));

			viewer.setContentProvider(new IStructuredContentProvider() {
				private static final long serialVersionUID = 7310636623175577101L;

				@Override
				public void inputChanged(Viewer viewer, Object oldInput,
						Object newInput) {
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
			List<Node> groups = peopleService.getUserManagementService()
					.getDefinedGroups(session, null, true);
			List<String> values = new ArrayList<String>();
			for (Node group : groups) {
				values.add(CommonsJcrUtils.get(group,
						PeopleNames.PEOPLE_GROUP_ID));
			}
			viewer.setInput(values.toArray(new String[0]));
			setControl(body);
			body.setFocus();
		}

		class MySelectionChangedListener implements ISelectionChangedListener {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection().isEmpty())
					chosenGroup = null;
				else {
					Object obj = ((IStructuredSelection) event.getSelection())
							.getFirstElement();
					if (obj instanceof String) {
						chosenGroup = (String) obj;
					}
				}
				getContainer().updateButtons();
			}
		}

		class MyDoubleClickListener implements IDoubleClickListener {
			public void doubleClick(DoubleClickEvent evt) {
				if (evt.getSelection().isEmpty()) {
					chosenGroup = null;
				} else {
					Object obj = ((IStructuredSelection) evt.getSelection())
							.getFirstElement();
					if (obj instanceof String) {
						chosenGroup = (String) obj;
						getContainer().showPage(getNextPage());
					}
				}
			}
		}

		public boolean canFlipToNextPage() {
			return CommonsJcrUtils.checkNotEmptyString(chosenGroup);
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
				setTitle("Assign to " + chosenGroup + ": check and confirm.");
				setMessage("Your are about to assign the below listed "
						+ rows.length + " tasks to " + chosenGroup
						+ ". Are you sure you want to proceed?");
			}
		}

		public void createControl(Composite parent) {
			Composite body = new Composite(parent, SWT.NONE);
			body.setLayoutData(EclipseUiUtils.fillWidth());
			GridLayout layout = new GridLayout();
			layout.marginTop = layout.marginWidth = 10;
			body.setLayout(layout);
			ArrayList<PeopleColumnDefinition> colDefs = new ArrayList<PeopleColumnDefinition>();
			colDefs.add(new PeopleColumnDefinition(selectorName,
					Property.JCR_TITLE, PropertyType.STRING, "Display Name",
					new TitleIconRowLP(peopleUiService, selectorName,
							Property.JCR_TITLE), 300));

			VirtualJcrTableViewer tableCmp = new VirtualJcrTableViewer(body,
					SWT.READ_ONLY, colDefs);
			TableViewer membersViewer = tableCmp.getTableViewer();
			membersViewer.setContentProvider(new MyLazyContentProvider(
					membersViewer));
			setViewerInput(membersViewer, rows);
			// workaround the issue with fill layout and virtual viewer
			GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false);
			gd.heightHint = 400;
			tableCmp.setLayoutData(gd);
			setControl(body);
		}
	}

	/** Use this method to update the result table */
	protected void setViewerInput(TableViewer membersViewer, Row[] rows) {
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
			viewer.setSelection(null);
			this.elements = (Row[]) newInput;
		}

		public void updateElement(int index) {
			viewer.replace(elements[index], index);
		}
	}

	/** Privileged job that performs the update asynchronously */
	private class UpdateAssignmentJob extends PrivilegedJob {

		private Repository repository;

		final private List<String> pathes = new ArrayList<String>();
		private final String chosenGroup;

		// private final String taskTypeId;

		public UpdateAssignmentJob(PeopleService peopleService,
				Row[] toUpdateRows, String selectorName, String chosenGroup) {
			super("Updating");

			// this.taskTypeId = taskTypeId;
			this.chosenGroup = chosenGroup;
			try {
				Node tmpNode = toUpdateRows[0].getNode(selectorName);
				repository = tmpNode.getSession().getRepository();
				for (Row row : toUpdateRows) {
					Node currNode = row.getNode(selectorName);
					pathes.add(currNode.getPath());
				}
			} catch (RepositoryException e) {
				throw new PeopleException("Unable to initialise "
						+ "status batch update ", e);
			}
		}

		protected IStatus doRun(IProgressMonitor progressMonitor) {
			Session session = null;
			try {
				ArgeoMonitor monitor = new EclipseArgeoMonitor(progressMonitor);
				if (monitor != null && !monitor.isCanceled()) {
					monitor.beginTask("Updating objects", -1);

					session = repository.login();

					// TODO use transaction
					for (String currPath : pathes) {
						Node currNode = session.getNode(currPath);
						// Legacy insure the node is checked out before update
						CommonsJcrUtils.checkCOStatusBeforeUpdate(currNode);
						if (CommonsJcrUtils.setJcrProperty(currNode,
								PeopleNames.PEOPLE_ASSIGNED_TO,
								PropertyType.STRING, chosenGroup))
							peopleService.saveEntity(currNode, true);
					}
					monitor.worked(1);
				}
			} catch (Exception e) {
				return new Status(IStatus.ERROR, PeopleRapPlugin.PLUGIN_ID,
						"Unable to perform batch assignment to " + chosenGroup
								+ " for " + selectorName + " row list ", e);
			} finally {
				JcrUtils.logoutQuietly(session);
			}
			return Status.OK_STATUS;
		}
	}
}