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
import javax.jcr.query.RowIterator;
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
import org.argeo.connect.people.rap.composites.VirtualRowTableViewer;
import org.argeo.connect.people.rap.exports.PeopleColumnDefinition;
import org.argeo.connect.people.rap.listeners.PeopleDoubleClickAdapter;
import org.argeo.connect.people.rap.providers.TitleIconRowLP;
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
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
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
 * perimeter on which the current catalogue might apply (typically, if we use
 * two distinct nomenclature for a same property that are bound to a given
 * projects)
 */
public class TemplateValueCatalogue extends Composite {
	private static final long serialVersionUID = -5018569293721397600L;

	// Context
	private final FormToolkit toolkit;
	private final PeopleWorkbenchService peopleWorkbenchService;
	private final PeopleService peopleService;
	private final Node templateNode;
	private final String propertyName;
	private final String taggableType;

	// this page UI Objects
	private final MyFormPart myFormPart;

	// private TableViewer valuesViewer;

	public TemplateValueCatalogue(FormToolkit toolkit, Composite parent, int style,
			IManagedForm form, PeopleService peopleService,
			PeopleWorkbenchService peopleWorkbenchService, Node templateNode,
			String propertyName, String taggableType) {
		super(parent, style);
		this.toolkit = toolkit;
		this.peopleService = peopleService;
		this.peopleWorkbenchService = peopleWorkbenchService;
		this.templateNode = templateNode;
		this.propertyName = propertyName;
		this.taggableType = taggableType;
		this.setLayout(PeopleUiUtils.noSpaceGridLayout());
		myFormPart = new MyFormPart(this);
		myFormPart.initialize(form);
		form.addPart(myFormPart);
	}

	private class MyFormPart extends AbstractPanelFormPart {
		private TableViewer valuesViewer;
		private TableViewer instancesViewer;

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
				GridLayout gl = PeopleUiUtils.noSpaceGridLayout();
				gl.verticalSpacing = 5;
				panel.setLayout(gl);
			}

			// Item list
			Composite valuesCmp = new Composite(panel, SWT.NO_FOCUS);
			GridData gd = PeopleUiUtils.horizontalFillData();
			gd.heightHint = 150;
			valuesCmp.setLayoutData(gd);
			valuesViewer = createValuesViewer(valuesCmp);
			valuesViewer.setContentProvider(new ValuesTableCP());
			valuesViewer.getTable().addSelectionListener(
					new MyEditRemoveAdapter(MyFormPart.this));

			Composite instancesCmp = new Composite(panel, SWT.NO_FOCUS);
			instancesCmp.setLayoutData(PeopleUiUtils.fillGridData());
			instancesViewer = createInstancesViewer(instancesCmp);
			instancesViewer.setContentProvider(new InstancesTableCP(
					instancesViewer));
			instancesViewer.addDoubleClickListener(new InstanceDClickAdapter());

			// enables update of the bottom table when one of the value is
			// selected
			valuesViewer
					.addSelectionChangedListener(new ISelectionChangedListener() {
						@Override
						public void selectionChanged(SelectionChangedEvent event) {
							IStructuredSelection selection = (IStructuredSelection) event
									.getSelection();
							if (!selection.isEmpty()) {
								String currSelected = (String) selection
										.getFirstElement();
								RowIterator rit = query(currSelected);
								setViewerInput(instancesViewer,
										CommonsJcrUtils.rowIteratorToArray(rit));
							}
						}
					});

			refreshContent(panel, editionInfo);
		}

		protected void refreshContent(Composite parent, Node editionInfo) {
			try {
				valuesViewer.setInput(CommonsJcrUtils.getMultiAsList(
						templateNode, propertyName).toArray(new String[0]));
				valuesViewer.refresh();
				setViewerInput(instancesViewer, null);
				if (editionInfo.getSession().hasPendingChanges())
					MyFormPart.this.markDirty();
			} catch (RepositoryException e) {
				throw new PeopleException("unable to occurrences for "
						+ editionInfo, e);
			}
		}
	}

	/**
	 * Retrieves all instances of the repository that have this value, overwrite
	 * to provide a more relevant request
	 */
	protected RowIterator query(String currVal) {
		try {
			Session session = templateNode.getSession();
			QueryManager queryManager = session.getWorkspace()
					.getQueryManager();
			QueryObjectModelFactory factory = queryManager.getQOMFactory();
			Selector source = factory.selector(taggableType, taggableType);

			StaticOperand so = factory.literal(session.getValueFactory()
					.createValue(currVal));
			DynamicOperand dyo = factory.propertyValue(
					source.getSelectorName(), propertyName);
			Constraint constraint = factory.comparison(dyo,
					QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO, so);

			Ordering order = factory.ascending(factory.propertyValue(
					source.getSelectorName(), Property.JCR_TITLE));
			Ordering[] orderings = { order };
			QueryObjectModel query = factory.createQuery(source, constraint,
					orderings, null);
			QueryResult result = query.execute();
			return result.getRows();
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to list entities with property "
					+ currVal + " for property " + propertyName + " of "
					+ templateNode, e);
		}
	}

	/** Displays existing values for this property */
	private TableViewer createValuesViewer(Composite parent) {
		parent.setLayout(PeopleUiUtils.noSpaceGridLayout());
		final Table table = new Table(parent, SWT.SINGLE | SWT.V_SCROLL
				| SWT.H_SCROLL);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(PeopleUiUtils.fillGridData());
		table.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
		table.setData(RWT.CUSTOM_ITEM_HEIGHT, Integer.valueOf(20));

		TableViewer viewer = new TableViewer(table);

		TableViewerColumn col = ViewerUtils.createTableViewerColumn(viewer,
				"Name", SWT.NONE, 400);
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
					// TODO implement edition and uncomment this
					return removeLink;
					// return editLink + PeopleUiConstants.NB_DOUBLE_SPACE
					// + removeLink;
				}
			});
		}
		return viewer;
	}

	private class MyEditRemoveAdapter extends SelectionAdapter {
		private static final long serialVersionUID = 1L;
		private final AbstractFormPart part;

		public MyEditRemoveAdapter(AbstractFormPart part) {
			this.part = part;
		}

		public void widgetSelected(SelectionEvent event) {
			if (event.detail == RWT.HYPERLINK) {
				String href = event.text;
				String[] val = href.split(PeopleRapConstants.HREF_SEPARATOR);
				if (PeopleUiConstants.CRUD_DELETE.equals(val[0])) {
					String msg = "Are you sure you want to remove \"" + val[1]
							+ "\" from this template catalogue? ";
					// TODO also manage removing various corresponding
					// references from the business entities
					boolean result = MessageDialog.openConfirm(
							TemplateValueCatalogue.this.getShell(),
							"Confirm value removal", msg);
					if (result) {
						CommonsJcrUtils.removeMultiPropertyValue(templateNode,
								propertyName, val[1]);
						part.markDirty();
						part.refresh();
					}
				} else if (PeopleUiConstants.CRUD_EDIT.equals(val[0])) {
					MessageDialog.openWarning(
							TemplateValueCatalogue.this.getShell(), "Boom",
							"implement this");
					// part.markDirty();
					// part.refresh();
				}
			}
		}
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

	private TableViewer createInstancesViewer(Composite parent) {
		parent.setLayout(PeopleUiUtils.noSpaceGridLayout());
		ArrayList<PeopleColumnDefinition> colDefs = new ArrayList<PeopleColumnDefinition>();
		colDefs.add(new PeopleColumnDefinition(taggableType,
				Property.JCR_TITLE, PropertyType.STRING, "Instances",
				new TitleIconRowLP(peopleWorkbenchService, taggableType,
						Property.JCR_TITLE), 400));
		VirtualRowTableViewer tableCmp = new VirtualRowTableViewer(parent,
				SWT.MULTI, colDefs);
		tableCmp.setLayoutData(PeopleUiUtils.fillGridData());
		TableViewer viewer = tableCmp.getTableViewer();
		viewer.setContentProvider(new InstancesTableCP(viewer));
		return viewer;
	}

	private class InstanceDClickAdapter extends PeopleDoubleClickAdapter {
		@Override
		protected void processDoubleClick(Object obj) {
			Node occurrence = CommonsJcrUtils.getNode((Row) obj, taggableType);
			CommandUtils.callCommand(
					peopleWorkbenchService.getOpenEntityEditorCmdId(),
					OpenEntityEditor.PARAM_JCR_ID,
					CommonsJcrUtils.getIdentifier(occurrence));
		}
	}

	private class InstancesTableCP implements ILazyContentProvider {
		private static final long serialVersionUID = 1L;
		private TableViewer viewer;
		private Row[] elements;

		public InstancesTableCP(TableViewer viewer) {
			this.viewer = viewer;
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// IMPORTANT: don't forget this: an exception will be thrown if
			// a selected object is not part of the results anymore.
			viewer.setSelection(null);
			if (newInput == null)
				elements = null;
			else
				this.elements = (Row[]) newInput;
		}

		public void updateElement(int index) {
			viewer.replace(elements[index], index);
		}
	}

	/** Use this method to update the instances tables */
	private void setViewerInput(TableViewer viewer, Row[] rows) {
		viewer.setInput(rows);
		// we must explicitly set the items count
		int count = 0;
		if (rows != null)
			count = rows.length;
		viewer.setItemCount(count);
		viewer.refresh();
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

	public class EditValueWizard extends Wizard implements PeopleNames {

		// Context
		private final String oldValue;

		// This part widgets
		private Text newValueTxt;

		public EditValueWizard(String oldValue) {
			this.oldValue = oldValue;
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
				TableViewer membersViewer = createInstancesViewer(body);
				RowIterator rit = query(oldValue);
				GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
				// gd.heightHint = 400;
				// tableCmp.setLayoutData(gd);
				setControl(body);
			}
		}

		// /** Refresh the table viewer based on the free text search field */
		// protected void refreshFilteredList(TableViewer membersViewer) {
		// String currVal = CommonsJcrUtils.get(templateNode,
		// Property.JCR_TITLE);
		// try {
		// Session session = templateNode.getSession();
		// QueryManager queryManager = session.getWorkspace()
		// .getQueryManager();
		// QueryObjectModelFactory factory = queryManager.getQOMFactory();
		// Selector source = factory.selector(taggableNodeType,
		// taggableNodeType);
		// //
		// factory.selector(tagLikeInstanceNode.getPrimaryNodeType().getName(),
		// // tagLikeInstanceNode.getPrimaryNodeType().getName());
		//
		// StaticOperand so = factory.literal(session.getValueFactory()
		// .createValue(currVal));
		// DynamicOperand dyo = factory.propertyValue(
		// source.getSelectorName(), propertyName);
		// Constraint constraint = factory.comparison(dyo,
		// QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO, so);
		//
		// Constraint subTree = factory.descendantNode(
		// source.getSelectorName(), taggableParentPath);
		// constraint = CommonsJcrUtils.localAnd(factory, constraint,
		// subTree);
		//
		// Ordering order = factory.ascending(factory.propertyValue(
		// source.getSelectorName(), Property.JCR_TITLE));
		// Ordering[] orderings = { order };
		// QueryObjectModel query = factory.createQuery(source,
		// constraint, orderings, null);
		// QueryResult result = query.execute();
		// Row[] rows = CommonsJcrUtils.rowIteratorToArray(result
		// .getRows());
		// setViewerInput(membersViewer, rows);
		//
		// } catch (RepositoryException e) {
		// throw new PeopleException(
		// "Unable to list entities for tag like property instance "
		// + currVal, e);
		// }
		// }
	}
}