package org.argeo.connect.people.workbench.rap.editors;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

import org.argeo.cms.auth.CurrentUser;
import org.argeo.connect.ConnectConstants;
import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ResourceService;
import org.argeo.connect.people.workbench.rap.PeopleRapConstants;
import org.argeo.connect.people.workbench.rap.PeopleRapPlugin;
import org.argeo.connect.people.workbench.rap.PeopleWorkbenchService;
import org.argeo.connect.people.workbench.rap.composites.VirtualJcrTableViewer;
import org.argeo.connect.people.workbench.rap.dialogs.AskTitleDescriptionDialog;
import org.argeo.connect.people.workbench.rap.editors.util.SearchNodeEditorInput;
import org.argeo.connect.people.workbench.rap.listeners.PeopleJcrViewerDClickListener;
import org.argeo.connect.people.workbench.rap.providers.JcrHtmlLabelProvider;
import org.argeo.connect.people.workbench.rap.util.Refreshable;
import org.argeo.connect.people.workbench.rap.wizards.EditTagWizard;
import org.argeo.connect.ui.ConnectColumnDefinition;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.util.XPathUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

/**
 * Display a list for all values of a given tag like resource (Typically a tag
 * or a mailing list).
 */
public class SearchTagsEditor extends EditorPart implements PeopleNames, Refreshable {

	public final static String ID = PeopleRapPlugin.PLUGIN_ID + ".searchTagsEditor";

	/* DEPENDENCY INJECTION */
	private Repository repository;
	private PeopleService peopleService;
	private PeopleWorkbenchService peopleWorkbenchService;

	// Context
	private Session session;
	private ResourceService resourceService;
	private Node tagParent;
	private String tagId;
	private String tagInstanceType;
	private String propertyName;

	// UI Objects
	private List<ConnectColumnDefinition> colDefs = new ArrayList<ConnectColumnDefinition>();
	private TableViewer tableViewer;
	private Text filterTxt;

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
		String label = ((SearchNodeEditorInput) getEditorInput()).getName();
		setPartName(label);

		this.resourceService = peopleService.getResourceService();
		String basePath = ((SearchNodeEditorInput) getEditorInput()).getBasePath();
		try {
			session = ConnectJcrUtils.login(repository);
			tagParent = session.getNode(basePath);
		} catch (RepositoryException e) {
			throw new PeopleException(
					"Unable to retrieve tag parent with path " + basePath + "\nUnable to open search editor.", e);
		}
		tagInstanceType = ConnectJcrUtils.get(tagParent, PEOPLE_TAG_INSTANCE_TYPE);
		tagId = ConnectJcrUtils.get(tagParent, PEOPLE_TAG_ID);

		// TODO this info should be stored in the parent path
		if (tagId.equals(PeopleConstants.RESOURCE_TAG)) {
			propertyName = PeopleNames.PEOPLE_TAGS;
		} else if (PeopleTypes.PEOPLE_MAILING_LIST.equals(tagId)) {
			propertyName = PeopleNames.PEOPLE_MAILING_LISTS;
		} else
			throw new PeopleException("Unknown tag like property at base path " + basePath);

		colDefs.add(new ConnectColumnDefinition("Title", new JcrHtmlLabelProvider(Property.JCR_TITLE), 300));
		colDefs.add(new ConnectColumnDefinition("Member count", new CountMemberLP(), 85));
		if (canEdit())
			colDefs.add(new ConnectColumnDefinition("", new EditLabelProvider(), 80));
	}

	// MAIN LAYOUT
	public void createPartControl(Composite parent) {
		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());
		// the generic free search part
		Composite searchCmp = new Composite(parent, SWT.NO_FOCUS);
		populateSearchPanel(searchCmp);
		searchCmp.setLayoutData(EclipseUiUtils.fillWidth());

		// The table itself
		Composite tableCmp = new Composite(parent, SWT.NO_FOCUS);
		createListPart(tableCmp);
		tableCmp.setLayoutData(EclipseUiUtils.fillAll());
		refreshStaticFilteredList();
	}

	// Header
	protected void populateSearchPanel(Composite parent) {
		parent.setLayout(new GridLayout(2, false));
		filterTxt = new Text(parent, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH | SWT.ICON_CANCEL);
		filterTxt.setMessage(PeopleRapConstants.FILTER_HELP_MSG);
		filterTxt.setLayoutData(EclipseUiUtils.fillWidth());
		filterTxt.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 5003010530960334977L;

			public void modifyText(ModifyEvent event) {
				refreshStaticFilteredList();
			}
		});

		Button addBtn = new Button(parent, SWT.PUSH);
		addBtn.setText("Create new...");
		addBtn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = -1990521824772413097L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				AskTitleDescriptionDialog dialog = new AskTitleDescriptionDialog(e.display.getActiveShell(), "Create");
				if (Dialog.OK == dialog.open()) {
					try {
						Node tag = resourceService.registerTag(session, tagId, dialog.getTitle());
						String desc = dialog.getDescription();
						if (EclipseUiUtils.notEmpty(desc))
							tag.setProperty(Property.JCR_DESCRIPTION, desc);
						if (tag.isNodeType(NodeType.MIX_VERSIONABLE))
							ConnectJcrUtils.saveAndPublish(tag, true);
						else
							session.save();
						refreshStaticFilteredList();
					} catch (RepositoryException re) {
						throw new PeopleException("Unable to create new " + propertyName, re);
					}
				}
			}
		});
	}

	protected void createListPart(Composite parent) {
		parent.setLayout(new GridLayout());
		VirtualJcrTableViewer tableCmp = new VirtualJcrTableViewer(parent, SWT.MULTI, colDefs);
		tableViewer = tableCmp.getTableViewer();
		tableCmp.setLayoutData(EclipseUiUtils.fillAll());
		tableViewer.addDoubleClickListener(new PeopleJcrViewerDClickListener(null, peopleWorkbenchService));
		tableViewer.getTable().addSelectionListener(new HtmlRwtAdapter());
	}

	/** Refresh the table viewer based on the free text search field */
	protected void refreshStaticFilteredList() {
		try {
			QueryManager queryManager = session.getWorkspace().getQueryManager();
			String queryStr = XPathUtils.descendantFrom(peopleService.getBasePath(PeopleConstants.PEOPLE_RESOURCE))
					+ "//element(*, " + tagInstanceType + ")";
			String attrQuery = XPathUtils.getFreeTextConstraint(filterTxt.getText());
			if (EclipseUiUtils.notEmpty(attrQuery))
				queryStr += "[" + attrQuery + "]";
			// always order ?
			queryStr += " order by @" + PeopleNames.JCR_TITLE;
			Query query = queryManager.createQuery(queryStr, ConnectConstants.QUERY_XPATH);
			NodeIterator nit = query.execute().getNodes();
			Node[] nodes = ConnectJcrUtils.nodeIteratorToArray(nit);
			setViewerInput(nodes);
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to list " + tagInstanceType, e);
		}
	}

	/** Use this method to update the result table */
	protected void setViewerInput(Object[] elements) {
		tableViewer.setInput(elements);
		// we must explicitly set the items count
		tableViewer.setItemCount(elements.length);
		tableViewer.refresh();
	}

	protected class CountMemberLP extends ColumnLabelProvider {
		private static final long serialVersionUID = 1L;

		@Override
		public String getText(Object element) {
			Node currNode = ConnectJcrUtils.getNodeFromElement(element, null);
			long count = resourceService.countMembers(currNode);
			return "" + count;
		}
	}

	protected class EditLabelProvider extends ColumnLabelProvider {
		private static final long serialVersionUID = 1L;

		@Override
		public String getText(Object element) {
			Node currNode = ConnectJcrUtils.getNodeFromElement(element, null);
			try {
				String jcrId = currNode.getIdentifier();
				StringBuilder builder = new StringBuilder();
				if (canEdit())
					builder.append("<a " + PeopleRapConstants.PEOPLE_STYLE_LINK + " href=\"edit/" + jcrId
							+ "\" target=\"_rwt\">Edit</a> ");
				if (canDelete(currNode))
					builder.append("<a " + PeopleRapConstants.PEOPLE_STYLE_LINK + " href=\"delete/" + jcrId
							+ "\" target=\"_rwt\">Delete</a> ");
				return builder.toString();
			} catch (RepositoryException re) {
				throw new PeopleException("Unable to create edit/remove link for " + currNode, re);
			}
		}
	}

	private boolean canEdit() {
		return CurrentUser.isInRole(PeopleConstants.ROLE_BUSINESS_ADMIN);
	}

	private boolean canDelete(Node currNode) {
		long currCount = resourceService.countMembers(currNode);
		return canEdit() && currCount == 0;
	}

	private class HtmlRwtAdapter extends SelectionAdapter {
		private static final long serialVersionUID = 1L;

		public void widgetSelected(SelectionEvent event) {
			if (event.detail == RWT.HYPERLINK) {
				String string = event.text;
				String[] token = string.split("/");
				try {
					Node node = session.getNodeByIdentifier(token[1]);

					if ("edit".equals(token[0])) {
						Wizard wizard = new EditTagWizard(peopleService, peopleWorkbenchService, node, tagId,
								propertyName);
						WizardDialog dialog = new WizardDialog(event.display.getActiveShell(), wizard);
						int result = dialog.open();
						if (result == WizardDialog.OK) {
							refreshStaticFilteredList();
						}
					} else {
						if (canDelete(node)) { // Superstition
							String msg = "Are you sure you want to delete \"" + ConnectJcrUtils.get(node, Property.JCR_TITLE)
									+ "\" ?";
							if (MessageDialog.openConfirm(event.display.getActiveShell(), "Confirm deletion", msg)) {
								ConnectJcrUtils.checkCOStatusBeforeUpdate(node);
								session.removeItem(node.getPath());
								session.save();
								refreshStaticFilteredList();
							}
						}
					}
				} catch (RepositoryException re) {
					throw new PeopleException("unable to retrieve Node with ID " + token[1], re);
				}
			}
		}
	}

	@Override
	public void forceRefresh(Object object) {
		refreshStaticFilteredList();
	}

	@Override
	public void setFocus() {
		filterTxt.setFocus();
	}

	@Override
	public void dispose() {
		JcrUtils.logoutQuietly(session);
		super.dispose();
	}

	/* DEPENDENCY INJECTION */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setPeopleWorkbenchService(PeopleWorkbenchService peopleWorkbenchService) {
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