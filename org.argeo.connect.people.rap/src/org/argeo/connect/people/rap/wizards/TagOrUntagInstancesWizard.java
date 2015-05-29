package org.argeo.connect.people.rap.wizards;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Row;

import org.argeo.ArgeoMonitor;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.ResourceService;
import org.argeo.connect.people.rap.PeopleRapPlugin;
import org.argeo.connect.people.rap.PeopleWorkbenchService;
import org.argeo.connect.people.rap.composites.SimpleJcrTableComposite;
import org.argeo.connect.people.rap.composites.VirtualRowTableViewer;
import org.argeo.connect.people.rap.providers.TitleIconRowLP;
import org.argeo.connect.people.ui.PeopleColumnDefinition;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.eclipse.ui.EclipseArgeoMonitor;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.eclipse.ui.jcr.lists.ColumnDefinition;
import org.argeo.jcr.JcrUtils;
import org.argeo.security.ui.PrivilegedJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * Generic wizard to add a tag like property entities retrieved in the passed
 * Rows
 * 
 * This will return SWT.OK only if the value has been changed, in that case,
 * underlying session is saved
 */

public class TagOrUntagInstancesWizard extends Wizard implements PeopleNames {
	// private final static Log log = LogFactory.getLog(EditTagWizard.class);

	// To be cleaned:
	public final static int TYPE_ADD = 1;
	public final static int TYPE_REMOVE = 2;

	// Context
	private PeopleService peopleService;
	private PeopleWorkbenchService peopleUiService;

	// Enable refresh of the calling editor at the end of the job
	private Display callingDisplay;

	private String tagId;
	private Node tagInstance;

	private String tagPropName;

	private Row[] rows;
	private final String selectorName;
	private final int actionType;

	// Cache to ease implementation
	private Session session;
	private ResourceService resourceService;
	private Node tagParent;
	private String tagInstanceType;

	/**
	 * @param actionType
	 * @param session
	 * @param peopleService
	 * @param peopleUiService
	 * @param rows
	 * @param selectorName
	 * @param tagId
	 * @param tagPropName
	 */
	public TagOrUntagInstancesWizard(Display callingDisplay, int actionType,
			Session session, PeopleService peopleService,
			PeopleWorkbenchService peopleUiService, Row[] rows,
			String selectorName, String tagId, String tagPropName) {

		this.callingDisplay = callingDisplay;
		this.session = session;
		this.peopleService = peopleService;
		this.peopleUiService = peopleUiService;
		this.tagId = tagId;
		this.tagPropName = tagPropName;
		this.rows = rows;
		this.selectorName = selectorName;

		this.actionType = actionType;

		resourceService = peopleService.getResourceService();
		tagParent = resourceService.getTagLikeResourceParent(session, tagId);
		tagInstanceType = CommonsJcrUtils.get(tagParent,
				PEOPLE_TAG_INSTANCE_TYPE);
	}

	@Override
	public void addPages() {
		try {
			// configure container
			String title = "Batch "
					+ (actionType == TYPE_ADD ? "addition" : "remove");
			setWindowTitle(title);
			MainInfoPage inputPage = new MainInfoPage("Configure");
			addPage(inputPage);
			RecapPage recapPage = new RecapPage("Validate and launch");
			addPage(recapPage);
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
		if (tagInstance == null)
			errMsg = "Please choose the tag to use";

		if (errMsg != null) {
			MessageDialog.openError(getShell(), "Unvalid information", errMsg);
			return false;
		}
		new UpdateTagAndInstancesJob(callingDisplay, actionType, peopleService,
				tagInstance, rows, selectorName, tagPropName).schedule();
		return true;
	}

	@Override
	public boolean performCancel() {
		return true;
	}

	@Override
	public boolean canFinish() {
		return tagInstance != null
				&& getContainer().getCurrentPage().getNextPage() == null;
	}

	protected class MainInfoPage extends WizardPage {
		private static final long serialVersionUID = 1L;

		private SimpleJcrTableComposite tableCmp;

		private List<ColumnDefinition> colDefs = new ArrayList<ColumnDefinition>();
		{ // By default, it displays only title
			colDefs.add(new ColumnDefinition(null, Property.JCR_TITLE,
					PropertyType.STRING, "Label", 420));
		};

		public MainInfoPage(String pageName) {
			super(pageName);
			setTitle("Select a tag");
			setMessage("Choose the value that will be "
					+ (actionType == TYPE_ADD ? "added to " : "removed from ")
					+ "the previously selected items.");
		}

		public void createControl(Composite parent) {
			Composite body = new Composite(parent, SWT.NONE);
			body.setLayout(EclipseUiUtils.noSpaceGridLayout());
			Node tagParent = peopleService.getResourceService()
					.getTagLikeResourceParent(session, tagId);
			int style = SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL;
			tableCmp = new SimpleJcrTableComposite(body, style, session,
					CommonsJcrUtils.getPath(tagParent), tagInstanceType,
					colDefs, true, false);
			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
			gd.heightHint = 400;
			tableCmp.setLayoutData(gd);

			// Add listeners
			tableCmp.getTableViewer().addDoubleClickListener(
					new MyDoubleClickListener());
			tableCmp.getTableViewer().addSelectionChangedListener(
					new MySelectionChangedListener());

			setControl(body);
			tableCmp.setFocus();
		}

		class MySelectionChangedListener implements ISelectionChangedListener {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection().isEmpty())
					tagInstance = null;
				else {
					Object obj = ((IStructuredSelection) event.getSelection())
							.getFirstElement();
					if (obj instanceof Node) {
						tagInstance = (Node) obj;
					}
				}
			}
		}

		class MyDoubleClickListener implements IDoubleClickListener {
			public void doubleClick(DoubleClickEvent evt) {
				if (evt.getSelection().isEmpty()) {
					tagInstance = null;
					return;
				} else {
					Object obj = ((IStructuredSelection) evt.getSelection())
							.getFirstElement();
					if (obj instanceof Node) {
						tagInstance = (Node) obj;
						getContainer().showPage(getNextPage());
					}
				}
			}
		}
	}

	protected class RecapPage extends WizardPage {
		private static final long serialVersionUID = 1L;

		public RecapPage(String pageName) {
			super(pageName);
			setTitle("Check and confirm");
		}

		public void setVisible(boolean visible) {
			super.setVisible(visible);

			if (visible == true) {
				if (tagInstance == null)
					setErrorMessage("Please choose a tag value to be used");
				else {
					setErrorMessage(null);
					String name = CommonsJcrUtils.get(tagInstance,
							Property.JCR_TITLE);
					if (actionType == TYPE_ADD)
						setMessage("Your are about to add [" + name
								+ "] to the below listed " + rows.length
								+ " items. "
								+ "Are you sure you want to procede ?");
					else
						setMessage("Your are about to remove [" + name
								+ "] from the below listed " + rows.length
								+ " items. "
								+ "Are you sure you want to procede ?");
				}
			}
		}

		public void createControl(Composite parent) {
			Composite body = new Composite(parent, SWT.NONE);
			body.setLayout(EclipseUiUtils.noSpaceGridLayout());
			ArrayList<PeopleColumnDefinition> colDefs = new ArrayList<PeopleColumnDefinition>();
			colDefs.add(new PeopleColumnDefinition(selectorName,
					Property.JCR_TITLE, PropertyType.STRING, "Display Name",
					new TitleIconRowLP(peopleUiService, selectorName,
							Property.JCR_TITLE), 300));

			VirtualRowTableViewer tableCmp = new VirtualRowTableViewer(body,
					SWT.READ_ONLY, colDefs);
			TableViewer membersViewer = tableCmp.getTableViewer();
			membersViewer.setContentProvider(new MyLazyContentProvider(
					membersViewer));
			setViewerInput(membersViewer, rows);
			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
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
			// IMPORTANT: don't forget this: an exception will be thrown if a
			// selected object is not part of the results anymore.
			viewer.setSelection(null);
			this.elements = (Row[]) newInput;
		}

		public void updateElement(int index) {
			viewer.replace(elements[index], index);
		}
	}

	/** Privileged job that performs the update asynchronously */
	private class UpdateTagAndInstancesJob extends PrivilegedJob {

		private int actionType;
		private Repository repository;
		private String tagPath;
		private String tagPropName;
		private List<String> pathes = new ArrayList<String>();
		private Display display;

		public UpdateTagAndInstancesJob(Display display, int actionType,
				PeopleService peopleService, Node taginstance,
				Row[] toUpdateRows, String selectorName, String tagPropName) {
			super("Updating");

			this.display = display;
			this.actionType = actionType;
			this.tagPropName = tagPropName;

			try {
				this.tagPath = tagInstance.getPath();
				repository = tagInstance.getSession().getRepository();
				for (Row row : toUpdateRows) {
					Node currNode = row.getNode(selectorName);
					pathes.add(currNode.getPath());
				}
			} catch (RepositoryException e) {
				throw new PeopleException("Unable to init "
						+ "tag instance batch update for " + tagInstance, e);
			}
		}

		protected IStatus doRun(IProgressMonitor progressMonitor) {
			Session session = null;
			try {
				ArgeoMonitor monitor = new EclipseArgeoMonitor(progressMonitor);
				if (monitor != null && !monitor.isCanceled()) {
					monitor.beginTask("Updating objects", -1);

					session = repository.login();
					Node targetTagInstance = session.getNode(tagPath);

					// TODO use transaction
					boolean isVersionable = targetTagInstance
							.isNodeType(NodeType.MIX_VERSIONABLE);
					boolean isCheckedIn = isVersionable
							&& !CommonsJcrUtils
									.isNodeCheckedOutByMe(targetTagInstance);
					if (isCheckedIn)
						CommonsJcrUtils.checkout(targetTagInstance);

					// TODO hardcoded prop name
					String value = targetTagInstance.getProperty(
							Property.JCR_TITLE).getString();

					for (String currPath : pathes) {
						Node currNode = session.getNode(currPath);
						boolean wasCO = CommonsJcrUtils
								.isNodeCheckedOutByMe(currNode);
						if (!wasCO)
							CommonsJcrUtils.checkout(currNode);
						if (actionType == TYPE_ADD) {
							// Duplication will return an error message that we
							// ignore
							CommonsJcrUtils.addStringToMultiValuedProp(
									currNode, tagPropName, value);
						} else if (actionType == TYPE_REMOVE) {
							// Duplication will return an error message that we
							// ignore
							CommonsJcrUtils.removeStringFromMultiValuedProp(
									currNode, tagPropName, value);
						}
						if (!wasCO)
							CommonsJcrUtils.saveAndCheckin(currNode);
						else
							currNode.getSession().save();
					}
					monitor.worked(1);

					// FIXME asynchronous refresh does not yet work
					display.asyncExec(new Runnable() {
						@Override
						public void run() {
							// CommandUtils.callCommand(ForceRefresh.ID);
						}
					});
				}
			} catch (Exception e) {
				return new Status(IStatus.ERROR, PeopleRapPlugin.PLUGIN_ID,
						"Unable to perform batch update on " + tagPath
								+ " for " + selectorName + " row list with "
								+ tagInstanceType, e);
			} finally {
				JcrUtils.logoutQuietly(session);
			}
			return Status.OK_STATUS;
		}
	}
}