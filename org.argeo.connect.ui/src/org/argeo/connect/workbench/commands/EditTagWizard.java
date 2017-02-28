package org.argeo.connect.workbench.commands;

import java.util.ArrayList;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.cms.ui.workbench.util.CommandUtils;
import org.argeo.cms.ui.workbench.util.PrivilegedJob;
import org.argeo.connect.ConnectException;
import org.argeo.connect.resources.ResourcesNames;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.ui.ConnectColumnDefinition;
import org.argeo.connect.ui.util.VirtualJcrTableViewer;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.workbench.AppWorkbenchService;
import org.argeo.connect.workbench.ConnectUiPlugin;
import org.argeo.connect.workbench.ConnectWorkbenchUtils;
import org.argeo.connect.workbench.Refreshable;
import org.argeo.connect.workbench.util.EntityEditorInput;
import org.argeo.connect.workbench.util.TitleIconRowLP;
import org.argeo.eclipse.ui.EclipseJcrMonitor;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrMonitor;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;

/**
 * Generic wizard to edit the value/title of a tag like property
 * 
 * This will return SWT.OK only if the value has been changed, in that case,
 * underlying session is saved and the node checked in to ease life cycle
 * management.
 */

public class EditTagWizard extends Wizard {
	private final static Log log = LogFactory.getLog(EditTagWizard.class);

	// Context
	private ResourcesService resourceService;
	private AppWorkbenchService appWorkbenchService;

	private String tagId;
	private Node tagInstance;
	private String tagPropName;

	// Cache to ease implementation
	private Session session;
	private Node tagParent;
	private String taggableNodeType;
	private String taggableParentPath;

	// This part widgets
	private Text newTitleTxt;
	private Text newDescTxt;

	/**
	 * 
	 * @param peopleService
	 * @param appWorkbenchService
	 * @param tagInstanceNode
	 * @param tagId
	 * @param tagPropName
	 */
	public EditTagWizard(ResourcesService resourceService, AppWorkbenchService appWorkbenchService,
			Node tagInstanceNode, String tagId, String tagPropName) {
		this.resourceService = resourceService;
		this.appWorkbenchService = appWorkbenchService;
		this.tagId = tagId;
		this.tagInstance = tagInstanceNode;
		this.tagPropName = tagPropName;

		session = ConnectJcrUtils.getSession(tagInstance);
		tagParent = resourceService.getTagLikeResourceParent(session, tagId);
		taggableNodeType = ConnectJcrUtils.get(tagParent, ResourcesNames.RESOURCES_TAGGABLE_NODE_TYPE);
		taggableParentPath = ConnectJcrUtils.get(tagParent, ResourcesNames.RESOURCES_TAGGABLE_PARENT_PATH);
	}

	@Override
	public void addPages() {
		try {
			// configure container
			String title = "Rename [" + ConnectJcrUtils.get(tagInstance, Property.JCR_TITLE)
					+ "] and update existing related contacts";
			setWindowTitle(title);
			MainInfoPage inputPage = new MainInfoPage("Configure");
			addPage(inputPage);
			RecapPage recapPage = new RecapPage("Validate and launch");
			addPage(recapPage);
		} catch (Exception e) {
			throw new ConnectException("Cannot add page to wizard", e);
		}
	}

	/**
	 * Called when the user click on 'Finish' in the wizard. The task is then
	 * created and the corresponding session saved.
	 */
	@Override
	public boolean performFinish() {
		try {
			String oldTitle = ConnectJcrUtils.get(tagInstance, Property.JCR_TITLE);
			String newTitle = newTitleTxt.getText();
			String newDesc = newDescTxt.getText();

			// Sanity checks
			String errMsg = null;
			if (EclipseUiUtils.isEmpty(newTitle))
				errMsg = "New value cannot be blank or an empty string";
			else if (oldTitle.equals(newTitle))
				errMsg = "New value is the same as old one.\n" + "Either enter a new one or press cancel.";
			else if (resourceService.getRegisteredTag(tagInstance.getSession(), tagId, newTitle) != null)
				errMsg = "The new chosen value is already used.\n" + "Either enter a new one or press cancel.";

			if (errMsg != null) {
				MessageDialog.openError(getShell(), "Unvalid information", errMsg);
				return false;
			}

			new UpdateTagAndInstancesJob(resourceService, tagInstance, newTitle, newDesc).schedule();
			return true;
		} catch (RepositoryException re) {
			throw new ConnectException("unable to update title for tag like resource " + tagInstance, re);
		}
	}

	@Override
	public boolean performCancel() {
		return true;
	}

	@Override
	public boolean canFinish() {
		return getContainer().getCurrentPage().getNextPage() == null;
	}

	protected class MainInfoPage extends WizardPage {
		private static final long serialVersionUID = 1L;

		public MainInfoPage(String pageName) {
			super(pageName);
			setTitle("Enter a new title");
			setMessage("As reminder, former value was: " + ConnectJcrUtils.get(tagInstance, Property.JCR_TITLE));
		}

		public void createControl(Composite parent) {
			Composite body = new Composite(parent, SWT.NONE);
			body.setLayout(new GridLayout(2, false));

			// New Title Value
			ConnectWorkbenchUtils.createBoldLabel(body, "Title");
			newTitleTxt = new Text(body, SWT.BORDER);
			newTitleTxt.setMessage("was: " + ConnectJcrUtils.get(tagInstance, Property.JCR_TITLE));
			newTitleTxt.setText(ConnectJcrUtils.get(tagInstance, Property.JCR_TITLE));
			newTitleTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			// New Description Value
			ConnectWorkbenchUtils.createBoldLabel(body, "Description", SWT.TOP);
			newDescTxt = new Text(body, SWT.BORDER | SWT.MULTI | SWT.WRAP);
			newDescTxt.setMessage("was: " + ConnectJcrUtils.get(tagInstance, Property.JCR_DESCRIPTION));
			newDescTxt.setText(ConnectJcrUtils.get(tagInstance, Property.JCR_DESCRIPTION));
			newDescTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

			setControl(body);
			newTitleTxt.setFocus();
		}
	}

	protected class RecapPage extends WizardPage {
		private static final long serialVersionUID = 1L;

		public RecapPage(String pageName) {
			super(pageName);
			setTitle("Check and confirm");
			setMessage("The below listed items will be impacted.\nAre you sure you want to procede ?");
		}

		public void createControl(Composite parent) {
			Composite body = new Composite(parent, SWT.NONE);
			body.setLayout(EclipseUiUtils.noSpaceGridLayout());
			ArrayList<ConnectColumnDefinition> colDefs = new ArrayList<ConnectColumnDefinition>();
			colDefs.add(new ConnectColumnDefinition(taggableNodeType, Property.JCR_TITLE, PropertyType.STRING,
					"Display Name", new TitleIconRowLP(appWorkbenchService, taggableNodeType, Property.JCR_TITLE),
					300));

			VirtualJcrTableViewer tableCmp = new VirtualJcrTableViewer(body, SWT.MULTI, colDefs);
			TableViewer membersViewer = tableCmp.getTableViewer();
			membersViewer.setContentProvider(new MyLazyContentProvider(membersViewer));
			refreshFilteredList(membersViewer);
			tableCmp.setLayoutData(EclipseUiUtils.fillAll());
			setControl(body);
		}
	}

	/** Refresh the table viewer based on the free text search field */
	protected void refreshFilteredList(TableViewer membersViewer) {
		String currVal = ConnectJcrUtils.get(tagInstance, Property.JCR_TITLE);
		try {
			QueryManager queryManager = session.getWorkspace().getQueryManager();
			QueryObjectModelFactory factory = queryManager.getQOMFactory();
			Selector source = factory.selector(taggableNodeType, taggableNodeType);

			StaticOperand so = factory.literal(session.getValueFactory().createValue(currVal));
			DynamicOperand dyo = factory.propertyValue(source.getSelectorName(), tagPropName);
			Constraint constraint = factory.comparison(dyo, QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO, so);

			Constraint subTree = factory.descendantNode(source.getSelectorName(), taggableParentPath);
			constraint = ConnectJcrUtils.localAnd(factory, constraint, subTree);

			Ordering order = factory.ascending(factory.propertyValue(source.getSelectorName(), Property.JCR_TITLE));
			Ordering[] orderings = { order };
			QueryObjectModel query = factory.createQuery(source, constraint, orderings, null);
			QueryResult result = query.execute();
			Row[] rows = ConnectJcrUtils.rowIteratorToArray(result.getRows());
			setViewerInput(membersViewer, rows);

		} catch (RepositoryException e) {
			throw new ConnectException("Unable to list entities for tag like property instance " + currVal, e);
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

	/** Privileged job that asynchronously performs the update */
	private class UpdateTagAndInstancesJob extends PrivilegedJob {

		private Repository repository;
		private ResourcesService resourceService;
		private String tagPath;
		private String newTitle, newDesc;

		// To refresh the calling editor after refresh
		private String tagJcrId;
		private IWorkbenchPage callingPage;

		public UpdateTagAndInstancesJob(ResourcesService resourceService, Node tagInstance, String newTitle,
				String newDesc) {
			super("Updating");
			this.resourceService = resourceService;
			this.newTitle = newTitle;
			this.newDesc = newDesc;
			try {
				repository = tagInstance.getSession().getRepository();
				tagPath = tagInstance.getPath();
			} catch (RepositoryException e) {
				throw new ConnectException("Unable to init " + "tag instance batch update for " + tagId, e);
			}
			callingPage = ConnectUiPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
		}

		protected IStatus doRun(IProgressMonitor progressMonitor) {
			Session session = null;
			try {
				JcrMonitor monitor = new EclipseJcrMonitor(progressMonitor);
				if (monitor != null && !monitor.isCanceled()) {
					monitor.beginTask("Updating objects", -1);

					session = repository.login();
					Node tagInstance = session.getNode(tagPath);

					// TODO use transaction
					// Legacy insure the node is checked out before update
					ConnectJcrUtils.checkCOStatusBeforeUpdate(tagInstance);

					resourceService.updateTag(tagInstance, newTitle);

					if (EclipseUiUtils.notEmpty(newDesc))
						tagInstance.setProperty(Property.JCR_DESCRIPTION, newDesc);
					else if (tagInstance.hasProperty(Property.JCR_DESCRIPTION))
						tagInstance.getProperty(Property.JCR_DESCRIPTION).remove();

					// Do we really want a new version at each and every time
					if (tagInstance.isNodeType(NodeType.MIX_VERSIONABLE))
						ConnectJcrUtils.saveAndPublish(tagInstance, true);
					else
						tagInstance.getSession().save();
					monitor.worked(1);
					tagJcrId = tagInstance.getIdentifier();
					doRefresh();
				}
			} catch (Exception e) {
				e.printStackTrace();
				return new Status(IStatus.ERROR, ConnectUiPlugin.PLUGIN_ID,
						"Cannot edit tag and corresponding instances", e);
			} finally {
				JcrUtils.logoutQuietly(session);
			}
			return Status.OK_STATUS;
		}

		private void doRefresh() {
			// Refresh asynchronously the corresponfding tag editor if it is
			// still opened
			Display currDisplay = callingPage.getWorkbenchWindow().getShell().getDisplay();
			currDisplay.asyncExec(new Runnable() {
				public void run() {
					try {
						EntityEditorInput eei = new EntityEditorInput(tagJcrId);
						IEditorPart iep = callingPage.findEditor(eei);
						if (iep != null && iep instanceof Refreshable)
							((Refreshable) iep).forceRefresh(null);
						CommandUtils.callCommand(ForceRefresh.ID);
					} catch (Exception e) {
						// Fail without notifying the user
						log.error("Unable to refresh the workbench after merge");
						e.printStackTrace();
					}
				}
			});
		}
	}
}
