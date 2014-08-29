package org.argeo.connect.people.ui.wizards;

import java.util.ArrayList;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
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
import org.eclipse.swt.layout.FillLayout;
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
	private String tagablebasePath;
	private String tagableNodeType;
	private String propertyName;

	// This page widgets
	private Text newTitleTxt;

	public EditTagWizard(PeopleService peopleService,
			PeopleUiService peopleUiService, String tagablebasePath,
			String tagableNodeType, String propertyName,
			Node tagLikeInstanceNode) {
		this.peopleService = peopleService;
		this.peopleUiService = peopleUiService;
		this.tagablebasePath = tagablebasePath;
		this.tagableNodeType = tagableNodeType;
		this.propertyName = propertyName;
		this.tagLikeInstanceNode = tagLikeInstanceNode;
	}

	@Override
	public void addPages() {
		try {
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

		String oldTitle = CommonsJcrUtils.get(tagLikeInstanceNode,
				Property.JCR_TITLE);
		String newTitle = newTitleTxt.getText();

		if (CommonsJcrUtils.isEmptyString(newTitle)) {
			MessageDialog.openError(getShell(), "Unvalid information",
					"New value cannot be blank or an empty string");
			return false;
		} else if (oldTitle.equals(newTitle)) {
			MessageDialog.openError(getShell(), "Unvalid information",
					"New value is the same as old one.\n"
							+ "Either enter a new one or press cancel.");
			return false;
		}

		// TODO Change title, move Node, update references
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
			PeopleUiUtils.createBoldLabel(body, "New Title");
			newTitleTxt = new Text(body, SWT.BORDER);
			newTitleTxt.setMessage("was: "
					+ CommonsJcrUtils.get(tagLikeInstanceNode,
							Property.JCR_TITLE));
			newTitleTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
					false));
			setControl(body);
			newTitleTxt.setFocus();
		}
	}

	protected class RecapPage extends WizardPage {
		private static final long serialVersionUID = 1L;

		public RecapPage(String pageName) {
			super(pageName);
			setTitle("Check and confirm");
			setMessage("The below listed items will be impacted. Are you sure you want to procede.");
		}

		public void createControl(Composite parent) {
			Composite body = new Composite(parent, SWT.NONE);
			body.setLayout(new FillLayout());

			TableViewer membersViewer = createTableViewer(body);
			membersViewer.setContentProvider(new MyLazyContentProvider(
					membersViewer));
			refreshFilteredList(membersViewer);

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
			Selector source = factory
					.selector(tagableNodeType, tagableNodeType);
			// Selector source =
			// factory.selector(tagLikeInstanceNode.getPrimaryNodeType().getName(),
			// tagLikeInstanceNode.getPrimaryNodeType().getName());

			StaticOperand so = factory.literal(session.getValueFactory()
					.createValue(currVal));
			DynamicOperand dyo = factory.propertyValue(
					source.getSelectorName(), propertyName);
			Constraint constraint = factory.comparison(dyo,
					QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO, so);

			Constraint subTree = factory.descendantNode(
					source.getSelectorName(), tagablebasePath);
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

	private TableViewer createTableViewer(Composite parent) {
		parent.setLayout(new GridLayout());
		ArrayList<PeopleColumnDefinition> colDefs = new ArrayList<PeopleColumnDefinition>();
		colDefs.add(new PeopleColumnDefinition(tagableNodeType,
				Property.JCR_TITLE, PropertyType.STRING, "Display Name",
				new TitleWithIconLP(peopleUiService, tagableNodeType,
						Property.JCR_TITLE), 300));
		PeopleVirtualTableViewer tableCmp = new PeopleVirtualTableViewer(
				parent, SWT.MULTI, colDefs);
		TableViewer tableViewer = tableCmp.getTableViewer();
		return tableViewer;
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