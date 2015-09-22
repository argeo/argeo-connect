package org.argeo.connect.people.rap.wizards;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.Row;
import javax.jcr.version.VersionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoMonitor;
import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.rap.PeopleRapPlugin;
import org.argeo.connect.people.rap.PeopleWorkbenchService;
import org.argeo.connect.people.rap.commands.ForceRefresh;
import org.argeo.connect.people.rap.composites.VirtualJcrTableViewer;
import org.argeo.connect.people.rap.editors.utils.EntityEditorInput;
import org.argeo.connect.people.rap.providers.TitleIconRowLP;
import org.argeo.connect.people.rap.utils.Refreshable;
import org.argeo.connect.people.ui.PeopleColumnDefinition;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.eclipse.ui.EclipseArgeoMonitor;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.eclipse.ui.workbench.CommandUtils;
import org.argeo.jcr.JcrUtils;
import org.argeo.security.ui.PrivilegedJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;

/**
 * Generic wizard to add merge 2 or more entities of the same type * This will
 * return SWT.OK only if the value has been changed, in that case, underlying
 * session is saved
 */

public class MergeEntityWizard extends Wizard implements PeopleNames {
	private final static Log log = LogFactory.getLog(MergeEntityWizard.class);

	// To be cleaned:
	public final static int TYPE_ADD = 1;
	public final static int TYPE_REMOVE = 2;

	// Context
	private PeopleService peopleService;
	private PeopleWorkbenchService peopleWorkbenchService;
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
	public MergeEntityWizard(IWorkbenchPage callingPage,
			PeopleService peopleService,
			PeopleWorkbenchService peopleWorkbenchService, Object[] rows,
			String selectorName, ColumnLabelProvider overviewLP) {
		this.callingPage = callingPage;
		this.peopleService = peopleService;
		this.peopleWorkbenchService = peopleWorkbenchService;
		this.rows = rows;
		this.selectorName = selectorName;
		// might be refined.
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
		if (masterNode == null)
			errMsg = "Please choose the master to use";

		if (errMsg != null) {
			MessageDialog.openError(getShell(), "Unvalid information", errMsg);
			return false;
		}
		new MergeEntitiesJob(callingPage, peopleService, masterNode, rows,
				selectorName).schedule();
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

			ArrayList<PeopleColumnDefinition> colDefs = new ArrayList<PeopleColumnDefinition>();
			colDefs.add(new PeopleColumnDefinition("Display Name",
					new TitleIconRowLP(peopleWorkbenchService, selectorName,
							Property.JCR_TITLE), 300));

			VirtualJcrTableViewer tableCmp = new VirtualJcrTableViewer(body,
					SWT.SINGLE, colDefs);
			TableViewer membersViewer = tableCmp.getTableViewer();
			membersViewer.setContentProvider(new MyLazyContentProvider(
					membersViewer));
			setViewerInput(membersViewer, rows);
			membersViewer.addDoubleClickListener(new MyDoubleClickListener());
			membersViewer
					.addSelectionChangedListener(new ISelectionChangedListener() {

						@Override
						public void selectionChanged(SelectionChangedEvent event) {
							if (chosenItemLabel.isDisposed())
								return;
							Object first = ((IStructuredSelection) event
									.getSelection()).getFirstElement();
							// try {
							if (first instanceof Row) {
								masterNode = CommonsJcrUtils.getNode(
										(Row) first, selectorName);
							}
							if (first == null)
								chosenItemLabel.setText("");
							else
								chosenItemLabel.setText(overviewLP
										.getText(masterNode));
							chosenItemLabel.getParent().layout();
							chosenItemLabel.getParent().getParent().layout();
							// } catch (RepositoryException e) {
							// throw new PeopleException(
							// "unable to change master with " + first,
							// e);
							// }
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
					Object obj = ((IStructuredSelection) evt.getSelection())
							.getFirstElement();
					// try {
					if (obj instanceof Row)
						masterNode = CommonsJcrUtils.getNode((Row) obj,
								selectorName);

					// masterNode = ((Row) obj).getNode(selectorName);
					getContainer().showPage(getNextPage());

					// } catch (RepositoryException e) {
					// throw new PeopleException(
					// "unable to change master with " + obj, e);
					// }
				}
			}
		}

		public void setVisible(boolean visible) {
			super.setVisible(visible);
			if (visible) {
				if (masterNode == null)
					chosenItemLabel.setText("<br/><big><i> "
							+ PeopleUiConstants.NB_DOUBLE_SPACE
							+ "No master has "
							+ "yet been chosen </i></big><br/> "
							+ PeopleUiConstants.NB_DOUBLE_SPACE);
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
			setMessage("The below listed items will be impacted.\nOld entities will be definitively removed. Are you sure you want to procede?");
		}

		public void createControl(Composite parent) {
			Composite body = new Composite(parent, SWT.NONE);
			body.setLayout(EclipseUiUtils.noSpaceGridLayout());

			Composite headerCmp = new Composite(body, SWT.NONE);
			headerCmp.setLayoutData(EclipseUiUtils.fillWidth());
			headerCmp.setLayout(EclipseUiUtils.noSpaceGridLayout());
			chosenItemLabel = new Label(headerCmp, SWT.NONE);
			CmsUtils.markup(chosenItemLabel);

			ArrayList<PeopleColumnDefinition> colDefs = new ArrayList<PeopleColumnDefinition>();
			colDefs.add(new PeopleColumnDefinition("Display Name",
					new TitleIconRowLP(peopleWorkbenchService, selectorName,
							Property.JCR_TITLE), 300));

			VirtualJcrTableViewer tableCmp = new VirtualJcrTableViewer(body,
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
		private String masterPath;

		private Session session;

		private List<String> slavePathes = new ArrayList<String>();
		private List<String> modifiedPathes = new ArrayList<String>();

		private List<String> removedIds = new ArrayList<String>();

		private IWorkbenchPage callingPage;

		public MergeEntitiesJob(IWorkbenchPage callingPage,
				PeopleService peopleService, Node masterNode,
				Object[] toUpdateElements, String selectorName) {
			super("Updating");
			this.callingPage = callingPage;

			try {
				this.masterPath = masterNode.getPath();
				repository = masterNode.getSession().getRepository();
				for (Object element : toUpdateElements) {
					Node currNode = CommonsJcrUtils.getNodeFromElement(element,
							selectorName);
					// Node currNode = row.getNode(selectorName);
					slavePathes.add(currNode.getPath());
				}
			} catch (RepositoryException e) {
				throw new PeopleException("Unable to init " + "merge for "
						+ masterNode, e);
			}
		}

		protected IStatus doRun(IProgressMonitor progressMonitor) {
			try {
				ArgeoMonitor monitor = new EclipseArgeoMonitor(progressMonitor);
				if (monitor != null && !monitor.isCanceled()) {
					monitor.beginTask("Updating objects",
							modifiedPathes.size() * 2);
					session = repository.login();
					Node masterNode = session.getNode(masterPath);
					checkCOStatusBeforeUpdate(masterNode);

					loop: for (String currPath : slavePathes) {
						if (masterPath.equals(currPath))
							continue loop;
						else {
							Node currSlave = masterNode.getSession().getNode(
									currPath);
							mergeNodes(null, masterNode, currSlave);

							if (log.isDebugEnabled()) {
								log.debug("About to remove node "
										+ currSlave.getPath()
										+ "\n with title: "
										+ CommonsJcrUtils.get(currSlave,
												Property.JCR_TITLE));
							}

							removedIds.add(currSlave.getIdentifier());
							currSlave.remove();
						}
						monitor.worked(1);
					}

					if (session.hasPendingChanges())
						session.save();

					VersionManager vManager = session.getWorkspace()
							.getVersionManager();
					for (String currPath : modifiedPathes) {
						if (session.nodeExists(currPath))
							vManager.checkin(currPath);
						monitor.worked(1);
					}
				}

				// Update the user interface asynchronously
				// wait one second so that the monitor & the dialog are
				// disposed.
				// Do something later in the UI thread
				// callingPage.asyncExec(new Runnable() {
				// public void run() {
				// callingPage.timerExec(1000, new Runnable() {
				// @Override
				// public void run() {
				// // Refresh list
				// CommandUtils.callCommand(ForceRefresh.ID);
				// }});}});
				Display currDisplay = callingPage.getWorkbenchWindow()
						.getShell().getDisplay();
				currDisplay.asyncExec(new Runnable() {
					public void run() {
						try {
							EntityEditorInput eei;
							// Close removed node editors
							for (String jcrId : removedIds) {
								eei = new EntityEditorInput(jcrId);
								IEditorPart iep = callingPage.findEditor(eei);
								if (iep != null)
									callingPage.closeEditor(iep, false);
							}

							// Refresh master editor if opened
							eei = new EntityEditorInput(masterNode
									.getIdentifier());
							IEditorPart iep = callingPage.findEditor(eei);
							if (iep != null && iep instanceof Refreshable)
								((Refreshable) iep).forceRefresh(null);

							// Refresh list
							CommandUtils.callCommand(ForceRefresh.ID);
						} catch (Exception e) {
							// Fail without notifying the user
							log.error("Unable to refresh the workbench after merge");
							e.printStackTrace();
						}
					}

				});
			} catch (Exception e) {
				return new Status(IStatus.ERROR, PeopleRapPlugin.PLUGIN_ID,
						"Unable to perform merge update on " + masterPath
								+ " for " + selectorName + " row list ", e);
			} finally {
				JcrUtils.logoutQuietly(session);
			}
			return Status.OK_STATUS;
		}

		private boolean checkCOStatusBeforeUpdate(Node node) {
			// Look for the parent versionable;
			Node parentV = CommonsJcrUtils.getVersionableAncestor(node);
			if (parentV == null)
				return true;
			boolean wasCo = CommonsJcrUtils.checkCOStatusBeforeUpdate(parentV);
			if (!wasCo)
				modifiedPathes.add(CommonsJcrUtils.getPath(parentV));
			return wasCo;
		}

		// Filtered properties
		private final List<String> TECHNICAL_PROPERTIES = asList("jcr:uuid",
				"jcr:baseVersion", "jcr:isCheckedOut", "jcr:predecessors",
				"jcr:frozenUuid", "jcr:versionHistory",
				"jcr:frozenPrimaryType", "jcr:primaryType", "jcr:mixinTypes",
				"jcr:created", "jcr:createdBy", "jcr:lastModified",
				"jcr:lastModifiedBy");

		private final List<String> TECHNICAL_NODES = asList("rep:policy");

		private void mergeNodes(Node parentNode, Node masterNode, Node slaveNode)
				throws RepositoryException {
			checkCOStatusBeforeUpdate(slaveNode);
			// particular case for child nodes
			if (masterNode == null) {
				String slavePath = slaveNode.getPath();
				slaveNode.isCheckedOut();
				slaveNode.isNodeType("mix:versionable");
				String destPath = parentNode.getPath() + "/"
						+ JcrUtils.lastPathElement(slavePath);
				session.move(slavePath, destPath);
				return;
			}

			// current nodes
			PropertyIterator pit = slaveNode.getProperties();
			props: while (pit.hasNext()) {
				Property currProp = pit.nextProperty();
				if (TECHNICAL_PROPERTIES.contains(currProp.getName()))
					continue props;

				Property masterCurrProp = null;
				if (masterNode.hasProperty(currProp.getName()))
					masterCurrProp = masterNode.getProperty(currProp.getName());
				mergeProperty(masterNode, masterCurrProp, currProp);
			}

			NodeIterator nit = slaveNode.getNodes();
			nodes: while (nit.hasNext()) {
				Node currNode = nit.nextNode();
				if (TECHNICAL_NODES.contains(currNode.getName()))
					continue nodes;
				Node masterCurrChild = null;
				if (masterNode.hasNode(currNode.getName()))
					masterCurrChild = masterNode.getNode(currNode.getName());
				mergeNodes(masterNode, masterCurrChild, currNode);
			}

			if (slaveNode.hasProperty(PeopleNames.PEOPLE_UID))
				mergeInternalReferences(masterNode, slaveNode);

			if (slaveNode.isNodeType("mix:referenceable"))
				mergeJcrReferences(masterNode, slaveNode);
			// current property
		}

		private void mergeJcrReferences(Node masterNode, Node slaveNode)
				throws RepositoryException {
			PropertyIterator pit = slaveNode.getReferences();
			while (pit.hasNext()) {
				Property ref = pit.nextProperty();
				Node referencing = ref.getParent();
				checkCOStatusBeforeUpdate(referencing);
				if (ref.isMultiple()) {
					CommonsJcrUtils.removeRefFromMultiValuedProp(referencing,
							ref.getName(), slaveNode.getIdentifier());
					CommonsJcrUtils.addRefToMultiValuedProp(referencing,
							ref.getName(), masterNode);
				} else
					referencing.setProperty(ref.getName(), masterNode);
			}
		}

		private void mergeInternalReferences(Node masterNode, Node slaveNode)
				throws RepositoryException {
			NodeIterator nit = internalReferencing(slaveNode);
			String peopleUId = masterNode.getProperty(PEOPLE_UID).getString();
			while (nit.hasNext()) {
				Node referencing = nit.nextNode();
				checkCOStatusBeforeUpdate(referencing);
				referencing.setProperty(PEOPLE_REF_UID, peopleUId);
			}
		}

		private NodeIterator internalReferencing(Node slaveNode)
				throws RepositoryException {
			String peopleUId = slaveNode.getProperty(PEOPLE_UID).getString();
			QueryManager qm = session.getWorkspace().getQueryManager();
			Query query = qm.createQuery(
					"select * from [nt:base] as nodes where ISDESCENDANTNODE('"
							+ peopleService.getBasePath(null) + "') AND ["
							+ PEOPLE_REF_UID + "]='" + peopleUId + "'" + " ",
					Query.JCR_SQL2);
			return query.execute().getNodes();
		}

		private void mergeProperty(Node masterNode, Property masterProp,
				Property slaveProp) throws RepositoryException {
			if (slaveProp.isMultiple())
				mergeMultipleProperty(masterNode, masterProp, slaveProp);
			else if (masterProp == null) {
				masterNode.setProperty(slaveProp.getName(),
						slaveProp.getValue());
			}
			// TODO won't merge properties with empty values.
		}

		private void mergeMultipleProperty(Node masterNode,
				Property masterProp, Property slaveProp)
				throws RepositoryException {
			Value[] slaveVals = slaveProp.getValues();
			if (masterProp == null) {
				masterNode.setProperty(slaveProp.getName(), slaveVals);
			} else {
				Value[] vals = masterProp.getValues();
				if (vals[0].getType() == PropertyType.STRING) {
					List<String> res = new ArrayList<String>();
					for (Value val : vals)
						res.add(val.getString());
					for (Value val : slaveVals) {
						String currStr = val.getString();
						if (!res.contains(currStr))
							res.add(currStr);
					}
					masterProp.setValue(res.toArray(new String[0]));
				} else if (vals[0].getType() == PropertyType.REFERENCE) {
					List<String> res = new ArrayList<String>();
					for (Value val : vals)
						res.add(val.getString());
					for (Value val : slaveVals) {
						String currStr = val.getString();
						if (!res.contains(currStr))
							res.add(currStr);
					}
					ValueFactory vFactory = session.getValueFactory();
					int size = res.size();
					Value[] values = new Value[size];
					int i = 0;
					for (String id : res) {
						Value val = vFactory.createValue(id,
								PropertyType.REFERENCE);
						values[i++] = val;
					}
					masterNode.setProperty(slaveProp.getName(), values);
				} else {
					throw new PeopleException(
							"Unsupported multiple property type on property "
									+ masterProp + "for node " + masterNode);
				}
			}
		}
	}
}