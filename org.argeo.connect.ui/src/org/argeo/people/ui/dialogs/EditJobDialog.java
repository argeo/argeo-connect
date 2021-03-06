package org.argeo.people.ui.dialogs;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import org.argeo.cms.ui.util.CmsUiUtils;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.ui.ConnectUiConstants;
import org.argeo.connect.ui.ConnectUiStyles;
import org.argeo.connect.ui.SystemWorkbenchService;
import org.argeo.connect.ui.widgets.DelayedText;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.util.XPathUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;
import org.argeo.people.PeopleConstants;
import org.argeo.people.PeopleException;
import org.argeo.people.PeopleNames;
import org.argeo.people.PeopleService;
import org.argeo.people.PeopleTypes;
import org.argeo.people.ui.PeopleMsg;
import org.argeo.people.ui.providers.EntitySingleColumnLabelProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.service.ServerPushSession;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog with a filtered list to create or edit a position for a given person
 * in an organisation. If editing an existing position, note that both
 * referenced and referencing entities must be given in order to eventually
 * remove old reference
 * 
 * <p>
 * It is the dialog duty to correctly initialise what is displayed based on the
 * parameters passed at instantiation time.
 * </p>
 */
public class EditJobDialog extends TrayDialog {
	private static final long serialVersionUID = -3534660152626908662L;

	// Context
	private Session session;
	private final ResourcesService resourcesService;
	private final PeopleService peopleService;
	private final SystemWorkbenchService systemWorkbenchService;

	// The various field
	private Text positionTxt;
	private Text selectedItemTxt;
	private Node selectedItem = null;
	private Text departmentTxt;
	private Button isPrimaryBtn;

	// Labels
	private final String positionLbl = PeopleMsg.position.lead();
	private final String chosenItemLbl = PeopleMsg.chosenItem.lead();
	private final String departmentLbl = PeopleMsg.department.lead();
	private final String primaryLbl = PeopleMsg.isPrimary.lead();

	// The search list
	private Text filterTxt;
	private Button okBtn;
	private TableViewer entityViewer;

	private final String title;

	private boolean isBackward;
	private String toSearchNodeType;
	private Node oldLinkNode;

	// Caches old info to initialize widgets if needed
	private String oldPosition = "";
	private String oldDepartment = "";
	private boolean wasPrimary = false;
	private Node oldReferencing;
	private Node oldReferenced;

	/**
	 * 
	 * @param parentShell
	 * @param title
	 * @param peopleService
	 * @param referencingNode
	 * @param referencedNode
	 * @param toSearchNodeType
	 * @param isBackward
	 *            tells if we must remove referenced (if true) or referencing (if
	 *            false) node
	 */
	public EditJobDialog(Shell parentShell, String title, ResourcesService resourcesService,
			PeopleService peopleService, SystemWorkbenchService systemWorkbenchService, Node oldLink, Node toUpdateNode,
			boolean isBackward) {
		super(parentShell);
		this.title = title;
		this.resourcesService = resourcesService;
		this.peopleService = peopleService;
		this.systemWorkbenchService = systemWorkbenchService;

		this.isBackward = isBackward;
		if (isBackward)
			toSearchNodeType = PeopleTypes.PEOPLE_PERSON;
		else
			toSearchNodeType = PeopleTypes.PEOPLE_ORG;

		if (oldLink == null) { // CREATE
			session = ConnectJcrUtils.getSession(toUpdateNode);
			if (isBackward)
				oldReferenced = toUpdateNode;
			else
				oldReferencing = toUpdateNode;
		} else { // UPDATE
			this.oldLinkNode = oldLink;
			try {
				// Initialize with old values
				session = oldLink.getSession();
				oldPosition = ConnectJcrUtils.get(oldLinkNode, PeopleNames.PEOPLE_ROLE);
				oldDepartment = ConnectJcrUtils.get(oldLinkNode, PeopleNames.PEOPLE_DEPARTMENT);
				Boolean tmp = ConnectJcrUtils.getBooleanValue(oldLink, PeopleNames.PEOPLE_IS_PRIMARY);
				if (tmp != null)
					wasPrimary = tmp;

				oldReferencing = oldLink.getParent().getParent();
				oldReferenced = peopleService.getEntityByUid(session, null,
						oldLink.getProperty(PeopleNames.PEOPLE_REF_UID).getString());
			} catch (RepositoryException e) {
				throw new PeopleException("Unable to initialize link edition", e);
			}
		}
	}

	/** Override to provide business specific addition behavior */
	protected boolean performFinish() {
		// Sanity check
		String msg = null;
		if (selectedItem == null && oldLinkNode == null)
			msg = "Please select an entity.";
		if (msg != null) {
			MessageDialog.openError(getShell(), "Non valid information", msg);
			return false;
		}

		// Retrieve values
		String position = positionTxt.getText();
		String department = departmentTxt.getText();
		boolean isPrimary = wasPrimary;
		if (isPrimaryBtn != null)
			isPrimary = isPrimaryBtn.getSelection();
		Node person, organisation;

		if (isBackward) {
			organisation = oldReferenced;
			if (selectedItem == null)
				person = oldReferencing;
			else
				person = selectedItem;
		} else {
			person = oldReferencing;
			if (selectedItem == null)
				organisation = oldReferenced;
			else
				organisation = selectedItem;
		}

		// Real update
		peopleService.getPersonService().createOrUpdateJob(oldLinkNode, person, organisation, position, department,
				isPrimary);
		return true;
	}

	protected Control createDialogArea(Composite parent) {
		// MAIN LAYOUT
		Composite dialogarea = (Composite) super.createDialogArea(parent);
		dialogarea.setLayout(new GridLayout(2, false));

		// The filter
		Composite filterCmp = new Composite(dialogarea, SWT.NONE);
		filterCmp.setLayoutData(EclipseUiUtils.fillWidth(2));
		addFilterPanel(filterCmp);

		// The list
		Composite listCmp = new Composite(dialogarea, SWT.NONE);
		GridData gd = EclipseUiUtils.fillWidth(2);
		gd.heightHint = 290;
		listCmp.setLayoutData(gd);
		entityViewer = createListPart(listCmp,
				new EntitySingleColumnLabelProvider(resourcesService, peopleService, systemWorkbenchService));
		refreshFilteredList(toSearchNodeType);

		// An empty line to give some air to the dialog
		Label dummyLbl = new Label(dialogarea, SWT.NONE);
		dummyLbl.setText("");
		dummyLbl.setLayoutData(EclipseUiUtils.fillWidth(2));

		// Display chosen org or person
		selectedItemTxt = createLT(dialogarea, chosenItemLbl);
		selectedItemTxt.setEnabled(false);
		CmsUiUtils.style(selectedItemTxt, ConnectUiStyles.FORCE_BORDER);

		if (isBackward) {
			if (oldReferencing != null)
				selectedItemTxt.setText(ConnectJcrUtils.get(oldReferencing, Property.JCR_TITLE));
		} else {
			if (oldReferenced != null)
				selectedItemTxt.setText(ConnectJcrUtils.get(oldReferenced, Property.JCR_TITLE));
		}
		// Role
		positionTxt = createLT(dialogarea, positionLbl);
		positionTxt.setText(oldPosition);

		// Department
		departmentTxt = createLT(dialogarea, departmentLbl);
		departmentTxt.setText(oldDepartment);

		// Is primary
		// Display primary check box only when editing a position from a person
		// perspective
		if (!isBackward) {
			isPrimaryBtn = createLC(dialogarea, primaryLbl);
			isPrimaryBtn.setSelection(wasPrimary);
		}

		dialogarea.layout();
		// Set the focus on the first field.
		filterTxt.setFocus();
		if (!systemWorkbenchService.lazyLoadLists())
			refreshFilteredList(toSearchNodeType);
		return dialogarea;
	}

	// This dialog life cycle
	@Override
	protected void okPressed() {
		if (performFinish())
			super.okPressed();
	}

	protected Point getInitialSize() {
		return new Point(400, 580);
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(title);
	}

	@Override
	public void create() {
		super.create();
		// prevent calling OK Pressed on filtering
		getShell().setDefaultButton(okBtn);
	}

	/** Overwrite to close session */
	public boolean close() {
		JcrUtils.logoutQuietly(session);
		return super.close();
	}

	// Specific widgets management

	/** Creates label and text. */
	protected Text createLT(Composite parent, String label) {
		Label lbl = new Label(parent, SWT.RIGHT);
		lbl.setText(label);
		lbl.setFont(EclipseUiUtils.getBoldFont(parent));
		lbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		Text text = new Text(parent, SWT.SINGLE | SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		return text;
	}

	/** Creates label and check box. */
	protected Button createLC(Composite parent, String label) {
		Label lbl = new Label(parent, SWT.RIGHT);
		lbl.setText(label);
		lbl.setFont(EclipseUiUtils.getBoldFont(parent));
		lbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		Button btn = new Button(parent, SWT.CHECK);
		btn.setText("");
		btn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		return btn;
	}

	protected void addFilterPanel(Composite parent) {
		GridLayout layout = EclipseUiUtils.noSpaceGridLayout(new GridLayout(2, false));
		layout.horizontalSpacing = 5;
		parent.setLayout(layout);

		boolean isDyn = systemWorkbenchService.queryWhenTyping();
		int style = SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH | SWT.ICON_CANCEL;
		if (isDyn) {
			DelayedText delayedText = new DelayedText(parent, style, ConnectUiConstants.SEARCH_TEXT_DELAY);
			final ServerPushSession pushSession = new ServerPushSession();
			delayedText.addDelayedModifyListener(pushSession, new ModifyListener() {
				private static final long serialVersionUID = 5003010530960334977L;

				public void modifyText(ModifyEvent event) {
					delayedText.getText().getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							refreshFilteredList(toSearchNodeType);
						}
					});
					pushSession.stop();
				}
			});
			filterTxt = delayedText.getText();
		} else
			filterTxt = new Text(parent, style);
		filterTxt.setMessage(PeopleMsg.searchAndChooseEntity.lead());
		filterTxt.setLayoutData(EclipseUiUtils.fillWidth());

		okBtn = new Button(parent, SWT.FLAT);
		okBtn.setText("Find");

		filterTxt.addTraverseListener(new TraverseListener() {
			private static final long serialVersionUID = 3886722799404099828L;

			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.keyCode == SWT.CR) {
					e.doit = false;
					refreshFilteredList(toSearchNodeType);
				}
			}
		});

		okBtn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 4305076157959928315L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				refreshFilteredList(toSearchNodeType);
			}
		});
	}

	protected TableViewer createListPart(Composite tableComposite, ILabelProvider labelProvider) {
		TableViewer v = new TableViewer(tableComposite, SWT.VIRTUAL | SWT.V_SCROLL | SWT.SINGLE);
		v.setLabelProvider(labelProvider);

		TableColumn singleColumn = new TableColumn(v.getTable(), SWT.LEFT);
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		tableColumnLayout.setColumnData(singleColumn, new ColumnWeightData(85));
		tableComposite.setLayout(tableColumnLayout);

		// Corresponding table & style
		Table table = v.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(false);
		// Enable markups
		table.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
		table.setData(RWT.CUSTOM_ITEM_HEIGHT, Integer.valueOf(24));

		// Providers and listeners
		v.setContentProvider(new MyLazyContentProvider(v));
		v.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				// Avoid NPE on filter reset
				Object element = ((IStructuredSelection) event.getSelection()).getFirstElement();
				if (element == null) {
					selectedItem = null;
					return;
				}

				// Only single selection is enabled
				Node selectedEntity = (Node) ((IStructuredSelection) event.getSelection()).getFirstElement();
				selectedItem = selectedEntity;

				try {
					if (selectedEntity.isNodeType(NodeType.MIX_TITLE))
						selectedItemTxt.setText(ConnectJcrUtils.get(selectedEntity, Property.JCR_TITLE));
				} catch (RepositoryException e) {
					throw new PeopleException("Unable to update " + "selected item", e);
				}
				// Sets the focus to next usefull field
				positionTxt.setFocus();
			}
		});
		return v;
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

	protected void refreshFilteredList(String nodeType) {
		List<Node> nodes = JcrUtils.nodeIteratorToList(query(nodeType));
		entityViewer.setInput(nodes.toArray());
		entityViewer.setItemCount(nodes.size());
		entityViewer.refresh();
	}

	protected NodeIterator query(String nodeType) {
		String filter = filterTxt.getText();
		try {
			String xpathQueryStr = "//element(*, " + nodeType + ")";
			String attrQuery = XPathUtils.getFreeTextConstraint(filter);
			if (EclipseUiUtils.notEmpty(attrQuery))
				xpathQueryStr += "[" + attrQuery + "]";
			Query xpathQuery = XPathUtils.createQuery(session, xpathQueryStr);
			xpathQuery.setLimit(PeopleConstants.QUERY_DEFAULT_LIMIT);
			QueryResult result = xpathQuery.execute();
			return result.getNodes();
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to list entities", e);
		}
	}
}
