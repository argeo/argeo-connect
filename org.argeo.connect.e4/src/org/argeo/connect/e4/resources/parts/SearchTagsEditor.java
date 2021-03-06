package org.argeo.connect.e4.resources.parts;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Query;

import org.argeo.cms.auth.CurrentUser;
import org.argeo.connect.ConnectException;
import org.argeo.connect.resources.ResourcesNames;
import org.argeo.connect.resources.ResourcesRole;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.ui.ConnectColumnDefinition;
import org.argeo.connect.ui.ConnectUiSnippets;
import org.argeo.connect.ui.Refreshable;
import org.argeo.connect.ui.SystemWorkbenchService;
import org.argeo.connect.ui.parts.AskTitleDescriptionDialog;
import org.argeo.connect.ui.util.JcrHtmlLabelProvider;
import org.argeo.connect.ui.util.JcrViewerDClickListener;
import org.argeo.connect.ui.util.VirtualJcrTableViewer;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.util.XPathUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.e4.ui.di.Focus;
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

/**
 * Display a list for all values of a given tag like resource (Typically a tag
 * or a mailing list).
 */
public class SearchTagsEditor implements Refreshable {

//	public final static String ID = ConnectUiPlugin.PLUGIN_ID + ".searchTagsEditor";

	/* DEPENDENCY INJECTION */
	@Inject
	private Repository repository;
	@Inject
	private ResourcesService resourcesService;
	@Inject
	private SystemWorkbenchService systemWorkbenchService;

	// Context
	private Session session;
	private Node tagParent;
	private String tagId;
	private String tagInstanceType;
	private String propertyName;

	// UI Objects
	private List<ConnectColumnDefinition> colDefs = new ArrayList<ConnectColumnDefinition>();
	private TableViewer tableViewer;
	private Text filterTxt;

	public void init() {
//		setSite(site);
//		setInput(input);
//		String label = ((SearchNodeEditorInput) getEditorInput()).getName();
//		setPartName(label);

		tagInstanceType = null;// ((SearchNodeEditorInput) getEditorInput()).getNodeType();
		session = ConnectJcrUtils.login(repository);
		tagParent = resourcesService.getTagLikeResourceParent(session, tagInstanceType);
		tagId = ConnectJcrUtils.get(tagParent, ResourcesNames.RESOURCES_TAG_ID);

		colDefs.add(new ConnectColumnDefinition("Title", new JcrHtmlLabelProvider(Property.JCR_TITLE), 300));
		colDefs.add(new ConnectColumnDefinition("Member count", new CountMemberLP(), 85));
		if (canEdit())
			colDefs.add(new ConnectColumnDefinition("", new EditLabelProvider(), 80));
	}

	// MAIN LAYOUT
	@PostConstruct
	public void createPartControl(Composite parent) {
		init();
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
						Node tag = resourcesService.registerTag(session, tagId, dialog.getTitle());
						String desc = dialog.getDescription();
						if (EclipseUiUtils.notEmpty(desc))
							tag.setProperty(Property.JCR_DESCRIPTION, desc);
						if (tag.isNodeType(NodeType.MIX_VERSIONABLE))
							ConnectJcrUtils.saveAndPublish(tag, true);
						else
							session.save();
						refreshStaticFilteredList();
					} catch (RepositoryException re) {
						throw new ConnectException("Unable to create new " + propertyName, re);
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
		tableViewer.addDoubleClickListener(new JcrViewerDClickListener());
		tableViewer.getTable().addSelectionListener(new HtmlRwtAdapter());
	}

	/** Refresh the table viewer based on the free text search field */
	protected void refreshStaticFilteredList() {
		try {
			String queryStr = XPathUtils.descendantFrom(tagParent.getPath()) + "//element(*, " + tagInstanceType + ")";
			String attrQuery = XPathUtils.getFreeTextConstraint(filterTxt.getText());
			if (EclipseUiUtils.notEmpty(attrQuery))
				queryStr += "[" + attrQuery + "]";
			// always order ?
			queryStr += " order by @" + Property.JCR_TITLE;
			Query query = XPathUtils.createQuery(session, queryStr);
			NodeIterator nit = query.execute().getNodes();
			Node[] nodes = ConnectJcrUtils.nodeIteratorToArray(nit);
			setViewerInput(nodes);
		} catch (RepositoryException e) {
			throw new ConnectException("Unable to list " + tagInstanceType, e);
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
			long count = resourcesService.countMembers(currNode);
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
					builder.append(ConnectUiSnippets.getRWTLink("edit/" + jcrId, "Edit")).append(" ");
				// builder.append("<a " + PeopleRapConstants.PEOPLE_STYLE_LINK +
				// " href=\"edit/" + jcrId
				// + "\" target=\"_rwt\"></a> ");
				if (canDelete(currNode))
					builder.append(ConnectUiSnippets.getRWTLink("delete/" + jcrId, "Delete"));
				return builder.toString();
			} catch (RepositoryException re) {
				throw new ConnectException("Unable to create edit/remove link for " + currNode, re);
			}
		}
	}

	private boolean canEdit() {
		return CurrentUser.isInRole(ResourcesRole.editor.dn());
	}

	private boolean canDelete(Node currNode) {
		long currCount = resourcesService.countMembers(currNode);
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
						Wizard wizard = new EditTagWizard(resourcesService, systemWorkbenchService, node, tagId,
								propertyName);
						WizardDialog dialog = new WizardDialog(event.display.getActiveShell(), wizard);
						int result = dialog.open();
						if (result == WizardDialog.OK) {
							refreshStaticFilteredList();
						}
					} else {
						if (canDelete(node)) { // Superstition
							String msg = "Are you sure you want to delete \""
									+ ConnectJcrUtils.get(node, Property.JCR_TITLE) + "\" ?";
							if (MessageDialog.openConfirm(event.display.getActiveShell(), "Confirm deletion", msg)) {
								ConnectJcrUtils.checkCOStatusBeforeUpdate(node);
								session.removeItem(node.getPath());
								session.save();
								refreshStaticFilteredList();
							}
						}
					}
				} catch (RepositoryException re) {
					throw new ConnectException("unable to retrieve Node with ID " + token[1], re);
				}
			}
		}
	}

	@Override
	public void forceRefresh(Object object) {
		refreshStaticFilteredList();
	}

	@Focus
	public void setFocus() {
		filterTxt.setFocus();
	}

	@PreDestroy
	public void dispose() {
		JcrUtils.logoutQuietly(session);
	}

	/* DEPENDENCY INJECTION */
//	public void setRepository(Repository repository) {
//		this.repository = repository;
//	}
//
//	public void setResourcesService(ResourcesService resourcesService) {
//		this.resourcesService = resourcesService;
//	}
//
//	public void setSystemWorkbenchService(SystemWorkbenchService systemWorkbenchService) {
//		this.systemWorkbenchService = systemWorkbenchService;
//	}
}
