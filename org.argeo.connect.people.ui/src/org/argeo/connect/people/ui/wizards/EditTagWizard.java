package org.argeo.connect.people.ui.wizards;

import java.util.ArrayList;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
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

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.ui.PeopleUiService;
import org.argeo.connect.people.ui.composites.PeopleVirtualTableViewer;
import org.argeo.connect.people.ui.exports.PeopleColumnDefinition;
import org.argeo.connect.people.ui.providers.TitleWithIconLP;
import org.argeo.connect.people.ui.utils.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
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
import org.eclipse.swt.widgets.Text;

/**
 * Generic wizard to edit the value/title of a tag like property
 * 
 * This will return SWT.OK only if the value has been changed, in that case,
 * underlying session is saved and the node checked in to ease life cycle
 * management.
 */

public class EditTagWizard extends Wizard implements PeopleNames {
	// private final static Log log = LogFactory.getLog(EditTagWizard.class);

	// Context
	private PeopleService peopleService;
	private PeopleUiService peopleUiService;

	private Node tagLikeInstanceNode;

	private String resourceNodeType;
	private String resourceInstancesParentPath;
	private String taggableNodeType;
	private String tagPropName;
	private String taggableParentPath;

	// This part widgets
	private Text newTitleTxt;
	private Text newDescTxt;

	/**
	 * 
	 * @param peopleService
	 * @param peopleUiService
	 * @param tagInstanceNode
	 * @param tagNodeType
	 * @param resourceInstancesParentPath
	 * @param taggableNodeType
	 * @param tagPropName
	 * @param taggableParentPath
	 */
	public EditTagWizard(PeopleService peopleService,
			PeopleUiService peopleUiService, Node tagInstanceNode,
			String tagNodeType, String resourceInstancesParentPath,
			String taggableNodeType, String tagPropName,
			String taggableParentPath) {

		this.peopleService = peopleService;
		this.peopleUiService = peopleUiService;
		this.tagLikeInstanceNode = tagInstanceNode;
		this.resourceNodeType = tagNodeType;
		this.resourceInstancesParentPath = resourceInstancesParentPath;
		this.taggableParentPath = taggableParentPath;
		this.tagPropName = tagPropName;
		this.taggableNodeType = taggableNodeType;
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
			String oldTitle = CommonsJcrUtils.get(tagLikeInstanceNode,
					Property.JCR_TITLE);
			String newTitle = newTitleTxt.getText();
			String newDesc = newDescTxt.getText();

			// Sanity checks
			String errMsg = null;
			if (CommonsJcrUtils.isEmptyString(newTitle))
				errMsg = "New value cannot be blank or an empty string";
			else if (oldTitle.equals(newTitle))
				errMsg = "New value is the same as old one.\n"
						+ "Either enter a new one or press cancel.";
			else if (peopleService.getTagService().getRegisteredTag(
					tagLikeInstanceNode.getSession(),
					resourceInstancesParentPath, newTitle) != null)
				errMsg = "The new chosen value is already used.\n"
						+ "Either enter a new one or press cancel.";

			if (errMsg != null) {
				MessageDialog.openError(getShell(), "Unvalid information",
						errMsg);
				return false;
			}

			// TODO use transaction
			boolean isVersionable = tagLikeInstanceNode
					.isNodeType(NodeType.MIX_VERSIONABLE);
			boolean isCheckedIn = isVersionable
					&& !CommonsJcrUtils
							.isNodeCheckedOutByMe(tagLikeInstanceNode);
			if (isCheckedIn)
				CommonsJcrUtils.checkout(tagLikeInstanceNode);
			peopleService.getTagService().updateTagTitle(tagLikeInstanceNode,
					resourceNodeType, resourceInstancesParentPath, newTitle,
					taggableNodeType, tagPropName, taggableParentPath);
			if (CommonsJcrUtils.checkNotEmptyString(newDesc))
				tagLikeInstanceNode.setProperty(Property.JCR_DESCRIPTION,
						newDesc);
			else if (tagLikeInstanceNode.hasProperty(Property.JCR_DESCRIPTION))
				// force reset
				tagLikeInstanceNode.setProperty(Property.JCR_DESCRIPTION, "");
			if (isCheckedIn)
				CommonsJcrUtils.saveAndCheckin(tagLikeInstanceNode);
			else if (isVersionable) // workaround versionnable node should have
				// been commited on last update
				CommonsJcrUtils.saveAndCheckin(tagLikeInstanceNode);
			else
				tagLikeInstanceNode.getSession().save();
			return true;
		} catch (RepositoryException re) {
			throw new PeopleException(
					"unable to update title for tag like resource "
							+ tagLikeInstanceNode, re);
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
			setMessage("As reminder, former value was: "
					+ CommonsJcrUtils.get(tagLikeInstanceNode,
							Property.JCR_TITLE));
		}

		public void createControl(Composite parent) {
			Composite body = new Composite(parent, SWT.NONE);
			body.setLayout(new GridLayout(2, false));

			// New Title Value
			PeopleUiUtils.createBoldLabel(body, "Title");
			newTitleTxt = new Text(body, SWT.BORDER);
			newTitleTxt.setMessage("was: "
					+ CommonsJcrUtils.get(tagLikeInstanceNode,
							Property.JCR_TITLE));
			newTitleTxt.setText(CommonsJcrUtils.get(tagLikeInstanceNode,
					Property.JCR_TITLE));
			newTitleTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
					false));

			// New Description Value
			PeopleUiUtils.createBoldLabel(body, "Description", SWT.TOP);
			newDescTxt = new Text(body, SWT.BORDER | SWT.MULTI | SWT.WRAP);
			newDescTxt.setMessage("was: "
					+ CommonsJcrUtils.get(tagLikeInstanceNode,
							Property.JCR_DESCRIPTION));
			newDescTxt.setText(CommonsJcrUtils.get(tagLikeInstanceNode,
					Property.JCR_DESCRIPTION));
			newDescTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
					true));

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
			body.setLayout(PeopleUiUtils.gridLayoutNoBorder());
			ArrayList<PeopleColumnDefinition> colDefs = new ArrayList<PeopleColumnDefinition>();
			colDefs.add(new PeopleColumnDefinition(taggableNodeType,
					Property.JCR_TITLE, PropertyType.STRING, "Display Name",
					new TitleWithIconLP(peopleUiService, taggableNodeType,
							Property.JCR_TITLE), 300));

			PeopleVirtualTableViewer tableCmp = new PeopleVirtualTableViewer(
					body, SWT.MULTI, colDefs);
			TableViewer membersViewer = tableCmp.getTableViewer();
			membersViewer.setContentProvider(new MyLazyContentProvider(
					membersViewer));
			refreshFilteredList(membersViewer);
			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
			gd.heightHint = 400;
			tableCmp.setLayoutData(gd);
			setControl(body);
		}
	}

	/** Refresh the table viewer based on the free text search field */
	protected void refreshFilteredList(TableViewer membersViewer) {
		String currVal = CommonsJcrUtils.get(tagLikeInstanceNode,
				Property.JCR_TITLE);
		try {
			Session session = tagLikeInstanceNode.getSession();
			QueryManager queryManager = session.getWorkspace()
					.getQueryManager();
			QueryObjectModelFactory factory = queryManager.getQOMFactory();
			Selector source = factory.selector(taggableNodeType,
					taggableNodeType);
			// factory.selector(tagLikeInstanceNode.getPrimaryNodeType().getName(),
			// tagLikeInstanceNode.getPrimaryNodeType().getName());

			StaticOperand so = factory.literal(session.getValueFactory()
					.createValue(currVal));
			DynamicOperand dyo = factory.propertyValue(
					source.getSelectorName(), tagPropName);
			Constraint constraint = factory.comparison(dyo,
					QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO, so);

			Constraint subTree = factory.descendantNode(
					source.getSelectorName(), taggableParentPath);
			constraint = PeopleUiUtils.localAnd(factory, constraint, subTree);

			Ordering order = factory.ascending(factory.propertyValue(
					source.getSelectorName(), Property.JCR_TITLE));
			Ordering[] orderings = { order };
			QueryObjectModel query = factory.createQuery(source, constraint,
					orderings, null);
			QueryResult result = query.execute();
			Row[] rows = CommonsJcrUtils.rowIteratorToArray(result.getRows());
			setViewerInput(membersViewer, rows);

		} catch (RepositoryException e) {
			throw new PeopleException(
					"Unable to list entities for tag like property instance "
							+ currVal, e);
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