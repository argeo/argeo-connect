package org.argeo.connect.ui.workbench.parts;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Row;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.cms.ui.workbench.util.PrivilegedJob;
import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.AppService;
import org.argeo.connect.ConnectException;
import org.argeo.connect.ui.ConnectColumnDefinition;
import org.argeo.connect.ui.ConnectUiConstants;
import org.argeo.connect.ui.workbench.AppWorkbenchService;
import org.argeo.connect.ui.workbench.util.TitleIconRowLP;
import org.argeo.connect.ui.workbench.util.VirtualJcrTableViewer;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
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
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPage;

/**
 * Generic wizard to add merge 2 or more entities of the same type * This will
 * return SWT.OK only if the value has been changed, in that case, underlying
 * session is saved
 */

public class MergeEntityWizard extends Wizard {
	private final static Log log = LogFactory.getLog(MergeEntityWizard.class);

	// To be cleaned:
	public final static int TYPE_ADD = 1;
	public final static int TYPE_REMOVE = 2;

	// Context
	private AppService peopleService;
	private AppWorkbenchService peopleWorkbenchService;
	private ColumnLabelProvider overviewLP;

	private Node masterNode;

	// Enable refresh of the calling editor at the end of the job
	private IWorkbenchPage callingPage;

	private Object[] rows;
	private final String selectorName;

	/**
	 * @param actionType
	 * @param session
	 * @param peopleService
	 * @param peopleWorkbenchService
	 * @param rows
	 * @param selectorName
	 * @param tagId
	 * @param tagPropName
	 */
	public MergeEntityWizard(IWorkbenchPage callingPage, AppService peopleService,
			AppWorkbenchService peopleWorkbenchService, Object[] rows, String selectorName,
			ColumnLabelProvider overviewLP) {
		this.callingPage = callingPage;
		this.peopleService = peopleService;
		this.peopleWorkbenchService = peopleWorkbenchService;
		this.rows = rows;
		this.selectorName = selectorName;
		this.overviewLP = overviewLP;
	}

	@Override
	public void addPages() {
		try {
			// configure container
			String title = "Merging";
			setWindowTitle(title);
			ChoosingPage choosingPage = new ChoosingPage("Master definition");
			addPage(choosingPage);
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
		// try {
		// Sanity checks
		String errMsg = null;
		if (masterNode == null)
			errMsg = "Please choose the master to use";

		if (errMsg != null) {
			MessageDialog.openError(getShell(), "Unvalid information", errMsg);
			return false;
		}
		new MergeEntitiesJob(callingPage, peopleService, masterNode, rows, selectorName).schedule();
		return true;
	}

	@Override
	public boolean performCancel() {
		return true;
	}

	@Override
	public boolean canFinish() {
		return getContainer().getCurrentPage().getNextPage() == null;
	}

	protected class ChoosingPage extends WizardPage {
		private static final long serialVersionUID = 1L;
		private Label chosenItemLabel;

		public ChoosingPage(String pageName) {
			super(pageName);
			setTitle("Choose a master");
			setMessage("The choosen entity will retrieve all information "
					+ "from the merged ones that will then be removed.");
		}

		public void createControl(Composite parent) {
			Composite body = new Composite(parent, SWT.NONE);
			body.setLayout(EclipseUiUtils.noSpaceGridLayout());

			// A composite that callingPage chosen entity
			Composite headerCmp = new Composite(body, SWT.NONE);
			headerCmp.setLayoutData(EclipseUiUtils.fillWidth());
			headerCmp.setLayout(EclipseUiUtils.noSpaceGridLayout());
			chosenItemLabel = new Label(headerCmp, SWT.NONE);
			chosenItemLabel.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);

			ArrayList<ConnectColumnDefinition> colDefs = new ArrayList<ConnectColumnDefinition>();
			colDefs.add(new ConnectColumnDefinition("Display Name",
					new TitleIconRowLP(peopleWorkbenchService, selectorName, Property.JCR_TITLE), 300));

			VirtualJcrTableViewer tableCmp = new VirtualJcrTableViewer(body, SWT.SINGLE, colDefs);
			TableViewer membersViewer = tableCmp.getTableViewer();
			membersViewer.setContentProvider(new MyLazyContentProvider(membersViewer));
			setViewerInput(membersViewer, rows);
			membersViewer.addDoubleClickListener(new MyDoubleClickListener());
			membersViewer.addSelectionChangedListener(new ISelectionChangedListener() {

				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					if (chosenItemLabel.isDisposed())
						return;
					Object first = ((IStructuredSelection) event.getSelection()).getFirstElement();
					if (first instanceof Row) {
						masterNode = ConnectJcrUtils.getNode((Row) first, selectorName);
					} else if (first instanceof Node) {
						masterNode = (Node) first;
					}
					if (first == null)
						chosenItemLabel.setText("");
					else
						chosenItemLabel.setText(overviewLP.getText(masterNode));
					chosenItemLabel.getParent().getParent().layout(true, true);
				}
			});

			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
			gd.heightHint = 400;
			tableCmp.setLayoutData(gd);
			setControl(body);
		}

		class MyDoubleClickListener implements IDoubleClickListener {
			public void doubleClick(DoubleClickEvent evt) {
				if (evt.getSelection().isEmpty()) {
					masterNode = null;
					return;
				} else {
					Object obj = ((IStructuredSelection) evt.getSelection()).getFirstElement();
					if (obj instanceof Row)
						masterNode = ConnectJcrUtils.getNode((Row) obj, selectorName);
					getContainer().showPage(getNextPage());
				}
			}
		}

		public void setVisible(boolean visible) {
			super.setVisible(visible);
			if (visible) {
				if (masterNode == null)
					chosenItemLabel.setText("<br/><big><i> " + ConnectUiConstants.NB_DOUBLE_SPACE + "No master has "
							+ "yet been chosen </i></big><br/> " + ConnectUiConstants.NB_DOUBLE_SPACE);
				else
					chosenItemLabel.setText(overviewLP.getText(masterNode));
				chosenItemLabel.getParent().getParent().layout(true, true);
			}
		}
	}

	protected class RecapPage extends WizardPage {
		private static final long serialVersionUID = 1L;

		private Label chosenItemLabel;

		public RecapPage(String pageName) {
			super(pageName);
			setTitle("Check and confirm");
			setMessage(
					"The below listed items will be impacted.\nOld entities will be definitively removed. Are you sure you want to procede?");
		}

		public void createControl(Composite parent) {
			Composite body = new Composite(parent, SWT.NONE);
			body.setLayout(EclipseUiUtils.noSpaceGridLayout());

			Composite headerCmp = new Composite(body, SWT.NONE);
			headerCmp.setLayoutData(EclipseUiUtils.fillWidth());
			headerCmp.setLayout(EclipseUiUtils.noSpaceGridLayout());
			chosenItemLabel = new Label(headerCmp, SWT.NONE);
			CmsUtils.markup(chosenItemLabel);

			ArrayList<ConnectColumnDefinition> colDefs = new ArrayList<ConnectColumnDefinition>();
			colDefs.add(new ConnectColumnDefinition("Display Name",
					new TitleIconRowLP(peopleWorkbenchService, selectorName, Property.JCR_TITLE), 300));

			VirtualJcrTableViewer tableCmp = new VirtualJcrTableViewer(body, SWT.READ_ONLY, colDefs);
			TableViewer membersViewer = tableCmp.getTableViewer();
			membersViewer.setContentProvider(new MyLazyContentProvider(membersViewer));
			setViewerInput(membersViewer, rows);
			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
			gd.heightHint = 400;
			tableCmp.setLayoutData(gd);
			setControl(body);
		}

		public void setVisible(boolean visible) {
			super.setVisible(visible);
			if (visible) {
				if (masterNode == null)
					chosenItemLabel.setText("");
				else
					chosenItemLabel.setText(overviewLP.getText(masterNode));
				chosenItemLabel.getParent().layout();
				chosenItemLabel.getParent().getParent().layout();
			}
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
	private class MergeEntitiesJob extends PrivilegedJob {

		private Repository repository;
		private AppService appService;
		private String masterPath;
		private Session session;

		private List<String> slavePathes = new ArrayList<String>();
		private List<String> modifiedPathes = new ArrayList<String>();
		private List<String> removedIds = new ArrayList<String>();

		private IWorkbenchPage callingPage;

		public MergeEntitiesJob(IWorkbenchPage callingPage, AppService appService, Node masterNode,
				Object[] toUpdateElements, String selectorName) {
			super("Updating");
			this.callingPage = callingPage;
			this.appService = peopleService;
			try {
				this.masterPath = masterNode.getPath();
				repository = masterNode.getSession().getRepository();
				for (Object element : toUpdateElements) {
					Node currNode = ConnectJcrUtils.getNodeFromElement(element, selectorName);
					slavePathes.add(currNode.getPath());
				}
			} catch (RepositoryException e) {
				throw new ConnectException("Unable to init " + "merge for " + masterNode, e);
			}
		}

		protected IStatus doRun(IProgressMonitor progressMonitor) {

			throw new RuntimeException("Unimplemented in Connect 2.2.");
			// try {
			// JcrMonitor monitor = new EclipseJcrMonitor(progressMonitor);
			// if (monitor != null && !monitor.isCanceled()) {
			// monitor.beginTask("Updating objects",
			// modifiedPathes.size() + 1);
			// session = repository.login();
			// Node masterNode = session.getNode(masterPath);
			//
			// loop: for (String currPath : slavePathes) {
			// if (masterPath.equals(currPath))
			// continue loop;
			// else {
			// Node currSlave = session.getNode(currPath);
			//
			//
			//// peopleService.getImportService().mergeNodes(
			//// masterNode, currSlave);
			// if (log.isDebugEnabled()) {
			// log.debug("About to remove node "
			// + currSlave.getPath()
			// + "\n with title: "
			// + ConnectJcrUtils.get(currSlave,
			// Property.JCR_TITLE));
			// }
			// removedIds.add(currSlave.getIdentifier());
			// currSlave.remove();
			// }
			// monitor.worked(1);
			// }
			//
			// if (session.hasPendingChanges()) {
			// session.save();
			// if (!modifiedPathes.contains(masterPath))
			// modifiedPathes.add(masterPath);
			// }
			// ConnectJcrUtils.checkPoint(session, modifiedPathes, true);
			// monitor.worked(1);
			// }
			//
			// // Update the user interface asynchronously
			// Display currDisplay = callingPage.getWorkbenchWindow()
			// .getShell().getDisplay();
			// currDisplay.asyncExec(new Runnable() {
			// public void run() {
			// try {
			// EntityEditorInput eei;
			// // Close removed node editors
			// for (String jcrId : removedIds) {
			// eei = new EntityEditorInput(jcrId);
			// IEditorPart iep = callingPage.findEditor(eei);
			// if (iep != null)
			// callingPage.closeEditor(iep, false);
			// }
			//
			// // Refresh master editor if opened
			// eei = new EntityEditorInput(masterNode
			// .getIdentifier());
			// IEditorPart iep = callingPage.findEditor(eei);
			// if (iep != null && iep instanceof Refreshable)
			// ((Refreshable) iep).forceRefresh(null);
			//
			// // Refresh list
			// CommandUtils.callCommand(ForceRefresh.ID);
			// } catch (Exception e) {
			// // Fail without notifying the user
			// log.error("Unable to refresh the workbench after merge");
			// e.printStackTrace();
			// }
			// }
			//
			// });
			// } catch (Exception e) {
			// return new Status(IStatus.ERROR, PeopleRapPlugin.PLUGIN_ID,
			// "Unable to perform merge update on " + masterPath
			// + " for " + selectorName + " row list ", e);
			// } finally {
			// JcrUtils.logoutQuietly(session);
			// }
			// return Status.OK_STATUS;
		}
	}
}