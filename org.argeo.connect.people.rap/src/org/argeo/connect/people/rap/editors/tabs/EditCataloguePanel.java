package org.argeo.connect.people.rap.editors.tabs;

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
import org.argeo.connect.people.rap.PeopleRapConstants;
import org.argeo.connect.people.rap.PeopleRapSnippets;
import org.argeo.connect.people.rap.PeopleRapUtils;
import org.argeo.connect.people.rap.PeopleWorkbenchService;
import org.argeo.connect.people.rap.commands.OpenEntityEditor;
import org.argeo.connect.people.rap.composites.PeopleVirtualTableViewer;
import org.argeo.connect.people.rap.exports.PeopleColumnDefinition;
import org.argeo.connect.people.rap.listeners.PeopleDoubleClickAdapter;
import org.argeo.connect.people.rap.providers.TitleWithIconLP;
import org.argeo.connect.people.rap.utils.AbstractPanelFormPart;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.eclipse.ui.dialogs.SingleValue;
import org.argeo.eclipse.ui.utils.CommandUtils;
import org.argeo.eclipse.ui.utils.ViewerUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * A composite to include in a form and that enables edition of the values of a
 * catalogue from a given template.
 * 
 * This might be extended to provide a specific request that will limit the
 * perimeter on which the current catalogue might apply (typically, modifying
 * the values of the "Submitted for" categories catalogue of a given edition
 * should only impact the films that are bound to this film edition)
 */
public class EditCataloguePanel extends Composite {
	private static final long serialVersionUID = -5018569293721397600L;

	// Context
	private final FormToolkit toolkit;
	private final PeopleWorkbenchService peopleWorkbenchService;
	private final PeopleService peopleService;
	private final Node templateNode;
	private final String propertyName;

	// this page UI Objects
	private final MyFormPart myFormPart;

	// private TableViewer valuesViewer;

	public EditCataloguePanel(FormToolkit toolkit, Composite parent, int style,
			IManagedForm form, PeopleService peopleService,
			PeopleWorkbenchService peopleWorkbenchService, Node templateNode,
			String propertyName) {
		super(parent, style);
		this.toolkit = toolkit;
		this.peopleService = peopleService;
		this.peopleWorkbenchService = peopleWorkbenchService;
		this.templateNode = templateNode;
		this.propertyName = propertyName;

		myFormPart = new MyFormPart(this);
		myFormPart.initialize(form);
		form.addPart(myFormPart);
	}

	private class MyFormPart extends AbstractPanelFormPart {
		private TableViewer valuesViewer;

		public MyFormPart(Composite parent) {
			super(parent, templateNode);
		}

		protected void reCreateChildComposite(Composite panel, Node editionInfo) {
			// Add button if needed
			Button addBtn = null;
			if (isCurrentlyCheckedOut()) {
				panel.setLayout(new GridLayout());
				addBtn = toolkit.createButton(panel, "Add a value", SWT.PUSH);
				configureAddValueBtn(this, addBtn);
			} else {
				panel.setLayout(PeopleUiUtils.noSpaceGridLayout());
			}

			// Item list
			valuesViewer = createValuesViewer(panel);
			valuesViewer.setContentProvider(new ValuesTableCP());
			// occurrencesViewer.getTable().addSelectionListener(
			// new EditRemoveListener(MyFormPart.this));
			refreshContent(panel, editionInfo);

		}

		protected void refreshContent(Composite parent, Node editionInfo) {
			try {
				valuesViewer.setInput(CommonsJcrUtils.getMultiAsList(
						templateNode, propertyName).toArray(new String[0]));
				valuesViewer.refresh();
				if (editionInfo.getSession().hasPendingChanges())
					MyFormPart.this.markDirty();
			} catch (RepositoryException e) {
				throw new PeopleException("unable to occurrences for "
						+ editionInfo, e);
			}
		}
	}

	/** Displays existing submitted for categories */
	private TableViewer createValuesViewer(Composite parent) {
		final Table table = new Table(parent, SWT.SINGLE | SWT.V_SCROLL
				| SWT.H_SCROLL);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(PeopleUiUtils.fillGridData());
		table.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
		table.setData(RWT.CUSTOM_ITEM_HEIGHT, Integer.valueOf(20));

		TableViewer viewer = new TableViewer(table);

		TableViewerColumn col = ViewerUtils.createTableViewerColumn(viewer,
				"Name", SWT.NONE, 200);
		col.setLabelProvider(new ColumnLabelProvider() {
			private static final long serialVersionUID = 1L;

			@Override
			public String getText(Object element) {
				return PeopleUiUtils.replaceAmpersand((String) element);
			}
		});

		if (CommonsJcrUtils.isNodeCheckedOutByMe(templateNode)) {
			col = ViewerUtils.createTableViewerColumn(viewer, "", SWT.NONE, 80);
			col.setLabelProvider(new ColumnLabelProvider() {
				private static final long serialVersionUID = 1L;

				@Override
				public String getText(Object element) {
					String value = PeopleUiUtils
							.replaceAmpersand((String) element);
					String editLink = PeopleRapSnippets
							.getRWTLink(
									PeopleUiConstants.CRUD_EDIT
											+ PeopleRapConstants.HREF_SEPARATOR
											+ value,
									PeopleUiConstants.CRUD_EDIT);
					String removeLink = PeopleRapSnippets
							.getRWTLink(
									PeopleUiConstants.CRUD_DELETE
											+ PeopleRapConstants.HREF_SEPARATOR
											+ value,
									PeopleUiConstants.CRUD_DELETE);
					return editLink + PeopleUiConstants.NB_DOUBLE_SPACE
							+ removeLink;
				}
			});
		}
		return viewer;
	}

	private class ValuesTableCP implements IStructuredContentProvider {
		private static final long serialVersionUID = 1L;

		private String[] elements;

		/** Expects a list of nodes as a new input */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			elements = (String[]) newInput;
		}

		public Object[] getElements(Object arg0) {
			return elements;
		}

		@Override
		public void dispose() {
		}
	}

	private void configureAddValueBtn(final AbstractFormPart formPart,
			final Button button) {
		String tooltip = "Create a new value for this catalogue";
		button.setToolTipText(tooltip);
		button.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));

		button.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				String value = SingleValue
						.ask("New value",
								"Provide a value to be addded in the current catalogue.");
				if (CommonsJcrUtils.checkNotEmptyString(value)) {
					String errMsg = CommonsJcrUtils.addMultiPropertyValue(
							templateNode, propertyName, value);
					if (CommonsJcrUtils.isEmptyString(errMsg)) {
						formPart.markDirty();
						formPart.refresh();
					} else
						MessageDialog.openError(button.getShell(),
								"Dupplicate value", errMsg);
				}

			}
		});
	}

	// TODO implement this
	@SuppressWarnings("unused")
	private class OccurrenceTableCP implements IStructuredContentProvider {
		private static final long serialVersionUID = 1L;

		private Node[] nodes;

		/** Expects a list of nodes as a new input */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			nodes = (Node[]) newInput;
		}

		public Object[] getElements(Object arg0) {
			return nodes;
		}

		@Override
		public void dispose() {
		}
	}

	@SuppressWarnings("unused")
	private class OccurrenceDClickAdapter extends PeopleDoubleClickAdapter {
		@Override
		protected void processDoubleClick(Object obj) {
			Node occurrence = ((Node) obj);
			CommandUtils.callCommand(
					peopleWorkbenchService.getOpenEntityEditorCmdId(),
					OpenEntityEditor.PARAM_JCR_ID,
					CommonsJcrUtils.getIdentifier(occurrence));
		}
	}

	public class EditTagWizard extends Wizard implements PeopleNames {

		// Context
		private PeopleService peopleService;
		private PeopleWorkbenchService peopleUiService;

		private Node templateNode;
		private String propertyName;
		private String oldValue;

		// private String resourceInstancesParentPath;
		private String taggableNodeType;
		private String taggableParentPath;

		// This part widgets
		private Text newValueTxt;

		public EditTagWizard(PeopleService peopleService,
				PeopleWorkbenchService peopleUiService, Node templateNode,
				String propertyName, String oldValue, String taggableNodeType,
				String taggableParentPath) {
			// String resourceInstancesParentPath,
			// ) {

			this.peopleService = peopleService;
			this.peopleUiService = peopleUiService;
			this.templateNode = templateNode;
			this.oldValue = oldValue;
			this.propertyName = propertyName;
			this.taggableNodeType = taggableNodeType;
			// this.resourceInstancesParentPath = resourceInstancesParentPath;
			this.taggableParentPath = taggableParentPath;
		}

		@Override
		public void addPages() {
			try {
				setWindowTitle("Update wizard");
				MainInfoPage inputPage = new MainInfoPage("Configure");
				addPage(inputPage);
				RecapPage recapPage = new RecapPage("Validate and launch");
				addPage(recapPage);
			} catch (Exception e) {
				throw new PeopleException("Cannot add page to wizard", e);
			}
		}

		/**
		 * Called when the user click on 'Finish' in the wizard. The task is
		 * then created and the corresponding session saved.
		 */
		@Override
		public boolean performFinish() {
			// try {
			String newTitle = newValueTxt.getText();

			// Sanity checks
			String errMsg = null;
			if (CommonsJcrUtils.isEmptyString(newTitle))
				errMsg = "New value cannot be blank or an empty string";
			else if (oldValue.equals(newTitle))
				errMsg = "New value is the same as old one.\n"
						+ "Either enter a new one or press cancel.";
			// TODO check for duplicates
			// else if (peopleService.getTagService().getRegisteredTag(
			// templateNode.getSession(),
			// resourceInstancesParentPath, newTitle) != null)
			// errMsg = "The new chosen value is already used.\n"
			// + "Either enter a new one or press cancel.";

			if (errMsg != null) {
				MessageDialog.openError(getShell(), "Unvalid information",
						errMsg);
				return false;
			}

			MessageDialog.openWarning(getShell(), "Implement This", errMsg);

			// // TODO use transaction
			// boolean isVersionable = templateNode
			// .isNodeType(NodeType.MIX_VERSIONABLE);
			// boolean isCheckedIn = isVersionable
			// && !CommonsJcrUtils
			// .isNodeCheckedOutByMe(templateNode);
			// if (isCheckedIn)
			// CommonsJcrUtils.checkout(templateNode);
			// peopleService.getTagService().updateTagTitle(
			// templateNode, resourceNodeType,
			// resourceInstancesParentPath, newTitle,
			// taggableNodeType, propertyName, taggableParentPath);
			// if (CommonsJcrUtils.checkNotEmptyString(newDesc))
			// templateNode.setProperty(Property.JCR_DESCRIPTION,
			// newDesc);
			// else if (templateNode
			// .hasProperty(Property.JCR_DESCRIPTION))
			// // force reset
			// templateNode.setProperty(Property.JCR_DESCRIPTION,
			// "");
			// if (isCheckedIn)
			// CommonsJcrUtils.saveAndCheckin(templateNode);
			// else if (isVersionable) // workaround versionnable node should
			// // have
			// // been commited on last update
			// CommonsJcrUtils.saveAndCheckin(templateNode);
			// else
			// templateNode.getSession().save();
			return true;
			// } catch (RepositoryException re) {
			// throw new PeopleException(
			// "unable to update title for tag like resource "
			// + templateNode, re);
			// }
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
				setMessage("As reminder, former value was: " + oldValue);
			}

			public void createControl(Composite parent) {
				Composite body = new Composite(parent, SWT.NONE);
				body.setLayout(new GridLayout(2, false));

				// New Title Value
				PeopleRapUtils.createBoldLabel(body, "New Value");
				newValueTxt = new Text(body, SWT.BORDER);
				newValueTxt.setMessage("was: " + oldValue);
				newValueTxt.setText(oldValue);
				newValueTxt.setLayoutData(PeopleUiUtils.horizontalFillData());
				setControl(body);
				newValueTxt.setFocus();
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
						Property.JCR_TITLE, PropertyType.STRING,
						"Display Name", new TitleWithIconLP(peopleUiService,
								taggableNodeType, Property.JCR_TITLE), 300));

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
			String currVal = CommonsJcrUtils.get(templateNode,
					Property.JCR_TITLE);
			try {
				Session session = templateNode.getSession();
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
						source.getSelectorName(), propertyName);
				Constraint constraint = factory.comparison(dyo,
						QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO, so);

				Constraint subTree = factory.descendantNode(
						source.getSelectorName(), taggableParentPath);
				constraint = CommonsJcrUtils.localAnd(factory, constraint,
						subTree);

				Ordering order = factory.ascending(factory.propertyValue(
						source.getSelectorName(), Property.JCR_TITLE));
				Ordering[] orderings = { order };
				QueryObjectModel query = factory.createQuery(source,
						constraint, orderings, null);
				QueryResult result = query.execute();
				Row[] rows = CommonsJcrUtils.rowIteratorToArray(result
						.getRows());
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

			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
				// IMPORTANT: don't forget this: an exception will be thrown if
				// a
				// selected object is not part of the results anymore.
				viewer.setSelection(null);
				this.elements = (Row[]) newInput;
			}

			public void updateElement(int index) {
				viewer.replace(elements[index], index);
			}
		}
	}
}