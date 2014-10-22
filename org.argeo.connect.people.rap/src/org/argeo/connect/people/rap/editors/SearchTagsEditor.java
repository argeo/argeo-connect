package org.argeo.connect.people.rap.editors;

import java.util.ArrayList;
import java.util.List;

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
import javax.jcr.query.qom.Ordering;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;
import javax.jcr.query.qom.StaticOperand;

import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ResourceService;
import org.argeo.connect.people.rap.PeopleRapConstants;
import org.argeo.connect.people.rap.PeopleRapPlugin;
import org.argeo.connect.people.rap.PeopleWorkbenchService;
import org.argeo.connect.people.rap.composites.VirtualRowTableViewer;
import org.argeo.connect.people.rap.dialogs.AskTitleDescriptionDialog;
import org.argeo.connect.people.rap.editors.utils.SearchNodeEditorInput;
import org.argeo.connect.people.rap.exports.PeopleColumnDefinition;
import org.argeo.connect.people.rap.listeners.PeopleJcrViewerDClickListener;
import org.argeo.connect.people.rap.providers.JcrRowHtmlLabelProvider;
import org.argeo.connect.people.rap.utils.Refreshable;
import org.argeo.connect.people.rap.wizards.EditTagWizard;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
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
import org.eclipse.swt.layout.GridData;
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
public class SearchTagsEditor extends EditorPart implements PeopleNames,
		Refreshable {

	public final static String ID = PeopleRapPlugin.PLUGIN_ID
			+ ".searchTagsEditor";

	/* DEPENDENCY INJECTION */
	private Session session;
	private PeopleService peopleService;
	private ResourceService resourceService;
	private PeopleWorkbenchService peopleWorkbenchService;

	// Context
	private Node tagParent;
	private String tagId;
	// private String basePath;
	private String tagInstanceType;
	private String propertyName;
	// private String resourceType;
	private List<PeopleColumnDefinition> colDefs = new ArrayList<PeopleColumnDefinition>();

	// This page widget
	private TableViewer tableViewer;
	private Text filterTxt;

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
		String label = ((SearchNodeEditorInput) getEditorInput()).getName();
		setPartName(label);

		this.resourceService = peopleService.getResourceService();
		String basePath = ((SearchNodeEditorInput) getEditorInput())
				.getBasePath();
		try {
			tagParent = session.getNode(basePath);
		} catch (RepositoryException e) {
			throw new PeopleException(
					"Unable to retrieve tag parent with path " + basePath
							+ "\nUnable to open search editor.", e);
		}
		// tagInstanceType = ((SearchNodeEditorInput)
		// getEditorInput()).getNodeType();
		tagInstanceType = CommonsJcrUtils.get(tagParent,
				PEOPLE_TAG_INSTANCE_TYPE);
		tagId = CommonsJcrUtils.get(tagParent, PEOPLE_TAG_ID);

		// TODO this info should be stored in the parent path
		if (tagId.equals(PeopleConstants.RESOURCE_TAG)) {
			propertyName = PeopleNames.PEOPLE_TAGS;
		} else if (PeopleTypes.PEOPLE_MAILING_LIST.equals(tagId)) {
			propertyName = PeopleNames.PEOPLE_MAILING_LISTS;
		} else
			throw new PeopleException("Unknown tag like property at base path "
					+ basePath);

		colDefs.add(new PeopleColumnDefinition(
				tagInstanceType,
				Property.JCR_TITLE,
				PropertyType.STRING,
				"Title",
				new JcrRowHtmlLabelProvider(tagInstanceType, Property.JCR_TITLE),
				300));
		colDefs.add(new PeopleColumnDefinition(tagInstanceType,
				Property.JCR_TITLE, PropertyType.STRING, "Member count",
				new CountMemberLP(), 85));
		if (canEdit())
			colDefs.add(new PeopleColumnDefinition(tagInstanceType,
					Property.JCR_TITLE, PropertyType.STRING, "",
					new EditLabelProvider(), 200));
	}

	// MAIN LAYOUT
	public void createPartControl(Composite parent) {
		parent.setLayout(PeopleUiUtils.noSpaceGridLayout());
		// the generic free search part
		Composite searchCmp = new Composite(parent, SWT.NO_FOCUS);
		populateSearchPanel(searchCmp);
		searchCmp.setLayoutData(PeopleUiUtils.horizontalFillData());

		// The table itself
		Composite tableCmp = new Composite(parent, SWT.NO_FOCUS);
		createListPart(tableCmp);
		tableCmp.setLayoutData(PeopleUiUtils.fillGridData());
		refreshStaticFilteredList();
	}

	// Header
	protected void populateSearchPanel(Composite parent) {
		parent.setLayout(new GridLayout(2, false));
		filterTxt = new Text(parent, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH
				| SWT.ICON_CANCEL);
		filterTxt.setMessage(PeopleRapConstants.FILTER_HELP_MSG);
		filterTxt.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		filterTxt.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 5003010530960334977L;

			public void modifyText(ModifyEvent event) {
				refreshStaticFilteredList();
			}
		});

		Button addBtn = new Button(parent, SWT.PUSH);
		addBtn.setText("Create new...");
		addBtn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				AskTitleDescriptionDialog dialog = new AskTitleDescriptionDialog(
						e.display.getActiveShell(), "Create");
				if (Dialog.OK == dialog.open()) {
					try {
						Node tag = resourceService.registerTag(session, tagId,
								dialog.getTitle());
						if (tag.isNodeType(NodeType.MIX_VERSIONABLE))
							CommonsJcrUtils.saveAndCheckin(tag);
						else
							session.save();
						refreshStaticFilteredList();
					} catch (RepositoryException re) {
						throw new PeopleException("Unable to create new "
								+ propertyName, re);
					}
				}
			}
		});
	}

	protected void createListPart(Composite parent) {
		parent.setLayout(new GridLayout());
		VirtualRowTableViewer tableCmp = new VirtualRowTableViewer(parent,
				SWT.MULTI, colDefs);
		tableViewer = tableCmp.getTableViewer();
		tableCmp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tableViewer.addDoubleClickListener(new PeopleJcrViewerDClickListener(
				tagInstanceType, peopleWorkbenchService));
		tableViewer.getTable().addSelectionListener(new HtmlRwtAdapter());
	}

	/** Refresh the table viewer based on the free text search field */
	protected void refreshStaticFilteredList() {
		try {
			QueryManager queryManager = session.getWorkspace()
					.getQueryManager();
			QueryObjectModelFactory factory = queryManager.getQOMFactory();
			Selector source = factory
					.selector(tagInstanceType, tagInstanceType);

			String filter = filterTxt.getText();
			Constraint defaultC = null;
			if (CommonsJcrUtils.checkNotEmptyString(filter)) {
				String[] strs = filter.trim().split(" ");
				for (String token : strs) {
					StaticOperand so = factory.literal(session
							.getValueFactory().createValue("*" + token + "*"));
					Constraint currC = factory.fullTextSearch(
							source.getSelectorName(), null, so);
					defaultC = CommonsJcrUtils.localAnd(factory, defaultC,
							currC);
				}
			}

			defaultC = CommonsJcrUtils.localAnd(
					factory,
					defaultC,
					factory.descendantNode(source.getSelectorName(),
							tagParent.getPath()));

			Ordering order = factory.ascending(factory.propertyValue(
					source.getSelectorName(), Property.JCR_TITLE));
			Ordering[] orderings = { order };
			QueryObjectModel query = factory.createQuery(source, defaultC,
					orderings, null);
			QueryResult result = query.execute();
			Row[] rows = CommonsJcrUtils.rowIteratorToArray(result.getRows());
			setViewerInput(rows);

		} catch (RepositoryException e) {
			throw new PeopleException("Unable to list " + tagInstanceType, e);
		}
	}

	/** Use this method to update the result table */
	protected void setViewerInput(Row[] rows) {
		tableViewer.setInput(rows);
		// we must explicitly set the items count
		tableViewer.setItemCount(rows.length);
		tableViewer.refresh();
	}

	protected class CountMemberLP extends ColumnLabelProvider {
		private static final long serialVersionUID = 1L;

		@Override
		public String getText(Object element) {
			Node currNode = CommonsJcrUtils.getNode((Row) element,
					tagInstanceType);

			long count = resourceService.countMembers(currNode);
			// peopleService.getResourceService().countMembers(currNode,
			// peopleService.getBasePath(null), PeopleTypes.PEOPLE_ENTITY,
			// propertyName);
			return "" + count;
		}
	}

	protected class EditLabelProvider extends ColumnLabelProvider {
		private static final long serialVersionUID = 1L;

		@Override
		public String getText(Object element) {
			Node currNode = CommonsJcrUtils.getNode((Row) element,
					tagInstanceType);
			try {
				String jcrId = currNode.getIdentifier();
				StringBuilder builder = new StringBuilder();
				if (canEdit())
					builder.append("<a " + PeopleRapConstants.PEOPLE_STYLE_LINK
							+ " href=\"edit/" + jcrId
							+ "\" target=\"_rwt\">Edit</a> ");
				if (canDelete(currNode))
					builder.append("<a " + PeopleRapConstants.PEOPLE_STYLE_LINK
							+ " href=\"delete/" + jcrId
							+ "\" target=\"_rwt\">Delete</a> ");
				return builder.toString();
			} catch (RepositoryException re) {
				throw new PeopleException(
						"Unable to create edit/remove link for " + currNode, re);
			}
		}
	}

	private boolean canEdit() {
		return peopleService.getUserManagementService().isUserInRole(
				PeopleConstants.ROLE_BUSINESS_ADMIN)
				|| peopleService.getUserManagementService().isUserInRole(
						PeopleConstants.ROLE_ADMIN);
	}

	private boolean canDelete(Node currNode) {
		long currCount = resourceService.countMembers(currNode);
		// currNode, peopleService.getBasePath(null),
		// PeopleTypes.PEOPLE_ENTITY, propertyName);
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
						Wizard wizard = new EditTagWizard(peopleService,
								peopleWorkbenchService, node, tagId,
								propertyName);
						WizardDialog dialog = new WizardDialog(
								event.display.getActiveShell(), wizard);
						int result = dialog.open();
						if (result == WizardDialog.OK) {
							refreshStaticFilteredList();
						}
					} else {
						if (canDelete(node)) { // Superstition
							String msg = "Are you sure you want to delete \""
									+ CommonsJcrUtils.get(node,
											Property.JCR_TITLE) + "\" ?";
							if (MessageDialog.openConfirm(
									event.display.getActiveShell(),
									"Confirm deletion", msg)) {
								if (!CommonsJcrUtils.isNodeCheckedOutByMe(node))
									CommonsJcrUtils.checkout(node);
								session.removeItem(node.getPath());
								session.save();
								refreshStaticFilteredList();
							}
						}
					}
				} catch (RepositoryException re) {
					throw new PeopleException(
							"unable to retrieve Node with ID " + token[1], re);
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
		session = CommonsJcrUtils.login(repository);
	}

	public void setPeopleWorkbenchService(
			PeopleWorkbenchService peopleWorkbenchService) {
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