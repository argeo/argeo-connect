package org.argeo.connect.people.rap.wizards;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Row;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.ResourceService;
import org.argeo.connect.people.rap.PeopleWorkbenchService;
import org.argeo.connect.people.rap.composites.SimpleJcrTableComposite;
import org.argeo.connect.people.rap.composites.VirtualRowTableViewer;
import org.argeo.connect.people.rap.exports.PeopleColumnDefinition;
import org.argeo.connect.people.rap.providers.TitleIconRowLP;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.eclipse.ui.jcr.lists.ColumnDefinition;
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
import org.eclipse.swt.widgets.Text;

/**
 * Generic wizard to add a tag like property entities retrieved in the passed
 * Rows
 * 
 * This will return SWT.OK only if the value has been changed, in that case,
 * underlying session is saved
 */

public class AddTagWizard extends Wizard implements PeopleNames {
	// private final static Log log = LogFactory.getLog(EditTagWizard.class);

	// Context
	private PeopleService peopleService;
	private PeopleWorkbenchService peopleUiService;

	private String tagId;
	private Node tagInstance;

	private String tagPropName;

	private Row[] rows;

	// Cache to ease implementation
	private Session session;
	private ResourceService resourceService;
	private Node tagParent;
	private String taggableNodeType;
	private String tagInstanceType;
	private String taggableParentPath;

	// This part widgets
	private Text newTitleTxt;
	private Text newDescTxt;

	/**
	 * 
	 * @param peopleService
	 * @param peopleUiService
	 * @param tagInstanceNode
	 * @param tagId
	 * @param tagPropName
	 */
	public AddTagWizard(PeopleService peopleService,
			PeopleWorkbenchService peopleUiService, Session session,
			Row[] rows, String tagId, String tagPropName) {

		this.peopleService = peopleService;
		this.peopleUiService = peopleUiService;
		this.tagId = tagId;
		this.tagPropName = tagPropName;

		this.session = session;
		this.rows = rows;
		resourceService = peopleService.getResourceService();
		tagParent = resourceService.getTagLikeResourceParent(session, tagId);
		taggableNodeType = CommonsJcrUtils.get(tagParent,
				PEOPLE_TAGGABLE_NODE_TYPE);
		tagInstanceType = CommonsJcrUtils.get(tagParent,
				PEOPLE_TAG_INSTANCE_TYPE);

		taggableParentPath = CommonsJcrUtils.get(tagParent,
				PEOPLE_TAGGABLE_PARENT_PATH);
	}

	@Override
	public void addPages() {
		try {
			// configure container
			setWindowTitle("Update Title");
			// setNeedsProgressMonitor(false);

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
		try {
			String oldTitle = CommonsJcrUtils.get(tagInstance,
					Property.JCR_TITLE);
			String newTitle = newTitleTxt.getText();
			String newDesc = newDescTxt.getText();

			if (true)
				return false;

			// Sanity checks
			String errMsg = null;
			if (CommonsJcrUtils.isEmptyString(newTitle))
				errMsg = "New value cannot be blank or an empty string";
			else if (oldTitle.equals(newTitle))
				errMsg = "New value is the same as old one.\n"
						+ "Either enter a new one or press cancel.";
			else if (peopleService.getResourceService().getRegisteredTag(
					tagInstance.getSession(), tagId, newTitle) != null)
				errMsg = "The new chosen value is already used.\n"
						+ "Either enter a new one or press cancel.";

			if (errMsg != null) {
				MessageDialog.openError(getShell(), "Unvalid information",
						errMsg);
				return false;
			}

			// TODO use transaction
			boolean isVersionable = tagInstance
					.isNodeType(NodeType.MIX_VERSIONABLE);
			boolean isCheckedIn = isVersionable
					&& !CommonsJcrUtils.isNodeCheckedOutByMe(tagInstance);
			if (isCheckedIn)
				CommonsJcrUtils.checkout(tagInstance);

			resourceService.updateTag(tagInstance, newTitle);

			if (CommonsJcrUtils.checkNotEmptyString(newDesc))
				tagInstance.setProperty(Property.JCR_DESCRIPTION, newDesc);
			else if (tagInstance.hasProperty(Property.JCR_DESCRIPTION))
				// force reset
				tagInstance.setProperty(Property.JCR_DESCRIPTION, "");

			if (isCheckedIn)
				CommonsJcrUtils.saveAndCheckin(tagInstance);
			else if (isVersionable) // workaround versionable node should have
				// been commited on last update
				CommonsJcrUtils.saveAndCheckin(tagInstance);
			else
				tagInstance.getSession().save();
			return true;
		} catch (RepositoryException re) {
			throw new PeopleException(
					"unable to update title for tag like resource "
							+ tagInstance, re);
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

		private SimpleJcrTableComposite tableCmp;

		private List<ColumnDefinition> colDefs = new ArrayList<ColumnDefinition>();
		{ // By default, it displays only title
			colDefs.add(new ColumnDefinition(null, Property.JCR_TITLE,
					PropertyType.STRING, "Label", 300));
		};

		public MainInfoPage(String pageName) {
			super(pageName);
			setTitle("Select a tag");
		}

		public void createControl(Composite parent) {
			parent.setLayout(PeopleUiUtils.noSpaceGridLayout());
			Node tagParent = peopleService.getResourceService()
					.getTagLikeResourceParent(session, tagId);
			int style = SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL;
			tableCmp = new SimpleJcrTableComposite(parent, style, session,
					CommonsJcrUtils.getPath(tagParent), tagInstanceType,
					colDefs, true, false);
			tableCmp.setLayoutData(PeopleUiUtils.fillGridData());

			// Add listeners
			tableCmp.getTableViewer().addDoubleClickListener(
					new MyDoubleClickListener());
			tableCmp.getTableViewer().addSelectionChangedListener(
					new MySelectionChangedListener());

			//
			//
			// FilteredVirtualEntityTable fvet = new
			// FilteredVirtualEntityTable(body, SWT.SINGLE, session);
			//
			// // New Title Value
			// PeopleRapUtils.createBoldLabel(body, "Title");
			// newTitleTxt = new Text(body, SWT.BORDER);
			// newTitleTxt.setMessage("was: "
			// + CommonsJcrUtils.get(tagInstance, Property.JCR_TITLE));
			// newTitleTxt.setText(CommonsJcrUtils.get(tagInstance,
			// Property.JCR_TITLE));
			// newTitleTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
			// true,
			// false));
			//
			//
			//
			// // New Description Value
			// PeopleRapUtils.createBoldLabel(body, "Description", SWT.TOP);
			// newDescTxt = new Text(body, SWT.BORDER | SWT.MULTI | SWT.WRAP);
			// newDescTxt.setMessage("was: "
			// + CommonsJcrUtils
			// .get(tagInstance, Property.JCR_DESCRIPTION));
			// newDescTxt.setText(CommonsJcrUtils.get(tagInstance,
			// Property.JCR_DESCRIPTION));
			// newDescTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
			// true));

			setControl(tableCmp);
			tableCmp.setFocus();
			// newTitleTxt.setFocus();
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
			setMessage("The below listed items will be impacted.\nAre you sure you want to procede ?");
		}

		public void createControl(Composite parent) {
			Composite body = new Composite(parent, SWT.NONE);
			body.setLayout(PeopleUiUtils.noSpaceGridLayout());
			ArrayList<PeopleColumnDefinition> colDefs = new ArrayList<PeopleColumnDefinition>();
			colDefs.add(new PeopleColumnDefinition(taggableNodeType,
					Property.JCR_TITLE, PropertyType.STRING, "Display Name",
					new TitleIconRowLP(peopleUiService, taggableNodeType,
							Property.JCR_TITLE), 300));

			VirtualRowTableViewer tableCmp = new VirtualRowTableViewer(body,
					SWT.MULTI, colDefs);
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
}