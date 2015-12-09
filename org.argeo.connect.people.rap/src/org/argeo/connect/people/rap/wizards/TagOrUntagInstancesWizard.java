package org.argeo.connect.people.rap.wizards;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.ArgeoMonitor;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.ResourceService;
import org.argeo.connect.people.rap.PeopleRapPlugin;
import org.argeo.connect.people.rap.PeopleWorkbenchService;
import org.argeo.connect.people.rap.composites.SimpleJcrTableComposite;
import org.argeo.connect.people.rap.composites.VirtualJcrTableViewer;
import org.argeo.connect.people.rap.providers.TitleIconRowLP;
import org.argeo.connect.people.ui.PeopleColumnDefinition;
import org.argeo.connect.people.util.JcrUiUtils;
import org.argeo.eclipse.ui.EclipseArgeoMonitor;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.eclipse.ui.jcr.lists.JcrColumnDefinition;
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

	private Object[] elements;
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
	 * @param elements
	 * @param selectorName
	 * @param tagId
	 * @param tagPropName
	 */
	public TagOrUntagInstancesWizard(Display callingDisplay, int actionType,
			Session session, PeopleService peopleService,
			PeopleWorkbenchService peopleUiService, Object[] elements,
			String selectorName, String tagId, String tagPropName) {

		this.callingDisplay = callingDisplay;
		this.session = session;
		this.peopleService = peopleService;
		this.peopleUiService = peopleUiService;
		this.tagId = tagId;
		this.tagPropName = tagPropName;

		this.elements = elements;
		this.selectorName = selectorName;

		this.actionType = actionType;

		resourceService = peopleService.getResourceService();
		tagParent = resourceService.getTagLikeResourceParent(session, tagId);
		tagInstanceType = JcrUiUtils.get(tagParent,
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
				tagInstance, elements, selectorName, tagPropName).schedule();
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

		private List<JcrColumnDefinition> colDefs = new ArrayList<JcrColumnDefinition>();
		{ // By default, it displays only title
			colDefs.add(new JcrColumnDefinition(null, Property.JCR_TITLE,
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
					JcrUiUtils.getPath(tagParent), tagInstanceType,
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
					String name = JcrUiUtils.get(tagInstance,
							Property.JCR_TITLE);
					if (actionType == TYPE_ADD)
						setMessage("Your are about to add [" + name
								+ "] to the below listed " + elements.length
								+ " items. "
								+ "Are you sure you want to proceed ?");
					else
						setMessage("Your are about to remove [" + name
								+ "] from the below listed " + elements.length
								+ " items. "
								+ "Are you sure you want to proceed ?");
				}
			}
		}

		public void createControl(Composite parent) {
			Composite body = new Composite(parent, SWT.NONE);
			body.setLayout(EclipseUiUtils.noSpaceGridLayout());
			ArrayList<PeopleColumnDefinition> colDefs = new ArrayList<PeopleColumnDefinition>();
			colDefs.add(new PeopleColumnDefinition("Display Name",
					new TitleIconRowLP(peopleUiService, selectorName,
							Property.JCR_TITLE), 300));

			VirtualJcrTableViewer tableCmp = new VirtualJcrTableViewer(body,
					SWT.READ_ONLY, colDefs);
			TableViewer membersViewer = tableCmp.getTableViewer();
			membersViewer.setContentProvider(new MyLazyContentProvider(
					membersViewer));
			setViewerInput(membersViewer, elements);
			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
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
			// IMPORTANT: don't forget this: an exception will be thrown if a
			// selected object is not part of the results anymore.
			viewer.setSelection(null);
			this.elements = (Object[]) newInput;
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
				Object[] toUpdateElements, String selectorName,
				String tagPropName) {
			super("Updating");

			this.display = display;
			this.actionType = actionType;
			this.tagPropName = tagPropName;

			try {
				this.tagPath = tagInstance.getPath();
				repository = tagInstance.getSession().getRepository();
				for (Object element : toUpdateElements) {
					Node currNode = JcrUiUtils.getNodeFromElement(element,
							selectorName);
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
					// Legacy insure the node is checked out before update
					JcrUiUtils.checkCOStatusBeforeUpdate(tagInstance);

					// TODO hardcoded prop name
					String value = targetTagInstance.getProperty(
							Property.JCR_TITLE).getString();

					for (String currPath : pathes) {
						Node currNode = session.getNode(currPath);
						JcrUiUtils.checkCOStatusBeforeUpdate(currNode);
						if (actionType == TYPE_ADD) {
							// Duplication will return an error message that we
							// ignore
							JcrUiUtils.addStringToMultiValuedProp(
									currNode, tagPropName, value);
						} else if (actionType == TYPE_REMOVE) {
							// Duplication will return an error message that we
							// ignore
							JcrUiUtils.removeStringFromMultiValuedProp(
									currNode, tagPropName, value);
						}
						JcrUiUtils.save(currNode, true);
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
						"Unable to perform batch update on "
								+ tagPath
								+ " for "
								+ (selectorName == null ? "single node "
										: selectorName) + " row list with "
								+ tagInstanceType, e);
			} finally {
				JcrUtils.logoutQuietly(session);
			}
			return Status.OK_STATUS;
		}
	}
}