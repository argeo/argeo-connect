package org.argeo.connect.people.rap.wizards;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.ArgeoException;
import org.argeo.ArgeoMonitor;
import org.argeo.cms.auth.AuthConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.UserAdminService;
import org.argeo.connect.people.rap.PeopleRapImages;
import org.argeo.connect.people.rap.PeopleRapPlugin;
import org.argeo.connect.people.rap.PeopleWorkbenchService;
import org.argeo.connect.people.rap.composites.VirtualJcrTableViewer;
import org.argeo.connect.people.rap.providers.TitleIconRowLP;
import org.argeo.connect.people.ui.PeopleColumnDefinition;
import org.argeo.connect.people.util.JcrUiUtils;
import org.argeo.eclipse.ui.EclipseArgeoMonitor;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.eclipse.ui.utils.ViewerUtils;
import org.argeo.jcr.JcrUtils;
import org.argeo.osgi.useradmin.LdifName;
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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;

/** Update the status of the selected tasks (with only one node type) as batch */
public class AssignToWizard extends Wizard implements PeopleNames {
	// private final static Log log = LogFactory.getLog(EditTagWizard.class);

	// Context
	private final PeopleService peopleService;
	private final PeopleWorkbenchService peopleUiService;
	private final UserAdminService userAdminService;

	private final Object[] elements;
	private final String selectorName;

	// The new group to use
	private String chosenGroupId;

	/**
	 * @param session
	 * @param peopleService
	 * @param peopleWorkbenchService
	 * @param elements
	 * @param selectorName
	 * @param taskId
	 */
	public AssignToWizard(PeopleService peopleService,
			PeopleWorkbenchService peopleWorkbenchService, Object[] elements,
			String selectorName) {
		this.peopleService = peopleService;
		this.peopleUiService = peopleWorkbenchService;
		this.elements = elements;
		this.selectorName = selectorName;
		userAdminService = peopleService.getUserAdminService();
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
		// Sanity checks
		String errMsg = null;
		if (EclipseUiUtils.isEmpty(chosenGroupId))
			errMsg = "Please pick up a new group";

		if (errMsg != null) {
			MessageDialog.openError(getShell(), "Unvalid information", errMsg);
			return false;
		}
		new UpdateAssignmentJob(peopleService, elements, selectorName,
				chosenGroupId).schedule();
		return true;
	}

	@Override
	public boolean performCancel() {
		return true;
	}

	@Override
	public boolean canFinish() {
		return EclipseUiUtils.notEmpty(chosenGroupId)
				&& getContainer().getCurrentPage().getNextPage() == null;
	}

	protected class MainInfoPage extends WizardPage {
		private static final long serialVersionUID = 1L;

		private TableViewer viewer;
		private Button showUserBtn;

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

			showUserBtn = new Button(body, SWT.CHECK);
			showUserBtn.setText("Show users");

			Composite box = new Composite(body, SWT.NO_FOCUS);
			box.setLayoutData(EclipseUiUtils.fillAll());

			int swtStyle = SWT.SINGLE;
			Table table = new Table(box, swtStyle);
			table.setLinesVisible(true);
			TableViewerColumn column;
			TableColumnLayout tableColumnLayout = new TableColumnLayout();
			viewer = new TableViewer(table);

			column = ViewerUtils.createTableViewerColumn(viewer, "", SWT.NONE,
					100);
			column.setLabelProvider(new ColumnLabelProvider() {
				private static final long serialVersionUID = -3677453559279606328L;

				@Override
				public Image getImage(Object element) {
					User user = (User) element;
					String dn = (String) user.getProperties().get(
							LdifName.dn.name());
					if (dn.endsWith(AuthConstants.ROLES_BASEDN))
						return PeopleRapImages.ICON_ROLE;
					else if (user.getType() == Role.GROUP)
						return PeopleRapImages.ICON_GROUP;
					else
						return PeopleRapImages.ICON_USER;
				}

				@Override
				public String getText(Object element) {
					User user = (User) element;
					return userAdminService.getUserDisplayName(user.getName());
				}
			});
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
					return (Role[]) inputElement;
				}
			});

			refreshList(null);
			viewer.addSelectionChangedListener(new MySelectionChangedListener());
			viewer.addDoubleClickListener(new MyDoubleClickListener());
			showUserBtn.addSelectionListener(new SelectionListener() {
				private static final long serialVersionUID = -6302066420365139940L;

				@Override
				public void widgetSelected(SelectionEvent e) {
					refreshList(null);
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					// TODO Auto-generated method stub

				}
			});

			box.setLayout(tableColumnLayout);
			setControl(body);
			body.setFocus();
		}

		private final String[] knownProps = { LdifName.uid.name(),
				LdifName.cn.name(), LdifName.dn.name() };

		private void refreshList(String filter) {
			// List<Role> groups = userAdminService.listGroups(null);
			Role[] roles;
			try {
				StringBuilder builder = new StringBuilder();

				StringBuilder filterBuilder = new StringBuilder();
				if (EclipseUiUtils.notEmpty(filter))
					for (String prop : knownProps) {
						filterBuilder.append("(");
						filterBuilder.append(prop);
						filterBuilder.append("=*");
						filterBuilder.append(filter);
						filterBuilder.append("*)");
					}

				String typeStr = "(" + LdifName.objectClass.name() + "="
						+ LdifName.groupOfNames.name() + ")";
				if ((showUserBtn.getSelection()))
					typeStr = "(|(" + LdifName.objectClass.name() + "="
							+ LdifName.inetOrgPerson.name() + ")" + typeStr
							+ ")";

				// if (!showSystemRoleBtn.getSelection())
				typeStr = "(& " + typeStr + "(!(" + LdifName.dn.name() + "=*"
						+ AuthConstants.ROLES_BASEDN + ")))";

				if (filterBuilder.length() > 1) {
					builder.append("(&" + typeStr);
					builder.append("(|");
					builder.append(filterBuilder.toString());
					builder.append("))");
				} else {
					builder.append(typeStr);
				}
				roles = userAdminService.getUserAdmin().getRoles(
						builder.toString());
			} catch (InvalidSyntaxException e) {
				throw new ArgeoException("Unable to get roles with filter: "
						+ filter, e);
			}

			// // List<String> values = new ArrayList<String>();
			// for (Group group : groups) {
			// values.add();
			// }
			viewer.setInput(roles);
			viewer.refresh();

		}

		class MySelectionChangedListener implements ISelectionChangedListener {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection().isEmpty())
					chosenGroupId = null;
				else {
					Object obj = ((IStructuredSelection) event.getSelection())
							.getFirstElement();
					if (obj instanceof User) {
						chosenGroupId = ((User) obj).getName();
					}
				}
				getContainer().updateButtons();
			}
		}

		class MyDoubleClickListener implements IDoubleClickListener {
			public void doubleClick(DoubleClickEvent evt) {
				if (evt.getSelection().isEmpty()) {
					chosenGroupId = null;
				} else {
					Object obj = ((IStructuredSelection) evt.getSelection())
							.getFirstElement();
					if (obj instanceof User) {
						chosenGroupId = ((User) obj).getName();
						getContainer().showPage(getNextPage());
					}
				}
			}
		}

		public boolean canFlipToNextPage() {
			return EclipseUiUtils.notEmpty(chosenGroupId);
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
				String dName = userAdminService
						.getUserDisplayName(chosenGroupId);
				setTitle("Assign to " + dName + ": check and confirm.");
				setMessage("Your are about to assign the below listed "
						+ elements.length + " tasks to " + dName
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
			setViewerInput(membersViewer, elements);
			// workaround the issue with fill layout and virtual viewer
			GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false);
			gd.heightHint = 400;
			tableCmp.setLayoutData(gd);
			setControl(body);
		}
	}

	/** Use this method to update the result table */
	protected void setViewerInput(TableViewer membersViewer, Object[] elements) {
		membersViewer.setInput(elements);
		// we must explicitly set the items count
		membersViewer.setItemCount(elements.length);
		membersViewer.refresh();
	}

	private class MyLazyContentProvider implements ILazyContentProvider {
		private static final long serialVersionUID = 1L;
		private TableViewer viewer;
		private Object[] elements;

		public MyLazyContentProvider(TableViewer viewer) {
			this.viewer = viewer;
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			viewer.setSelection(null);
			this.elements = (Object[]) newInput;
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
				Object[] toUpdateItems, String selectorName, String chosenGroup) {
			super("Updating");

			// this.taskTypeId = taskTypeId;
			this.chosenGroup = chosenGroup;
			try {
				Node tmpNode = JcrUiUtils.getNodeFromElement(toUpdateItems[0],
						selectorName);
				repository = tmpNode.getSession().getRepository();
				for (Object element : toUpdateItems) {
					Node currNode = JcrUiUtils.getNodeFromElement(element,
							selectorName);
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
						JcrUiUtils.checkCOStatusBeforeUpdate(currNode);
						if (JcrUiUtils.setJcrProperty(currNode,
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