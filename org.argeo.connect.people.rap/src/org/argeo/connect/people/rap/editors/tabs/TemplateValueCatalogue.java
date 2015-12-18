package org.argeo.connect.people.rap.editors.tabs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.argeo.connect.people.ResourceService;
import org.argeo.connect.people.rap.PeopleRapConstants;
import org.argeo.connect.people.rap.PeopleRapUtils;
import org.argeo.connect.people.rap.PeopleWorkbenchService;
import org.argeo.connect.people.rap.commands.ChangeEditingState;
import org.argeo.connect.people.rap.commands.OpenEntityEditor;
import org.argeo.connect.people.rap.composites.VirtualJcrTableViewer;
import org.argeo.connect.people.rap.dialogs.NoProgressBarWizardDialog;
import org.argeo.connect.people.rap.editors.utils.AbstractPeopleEditor;
import org.argeo.connect.people.rap.listeners.PeopleDoubleClickAdapter;
import org.argeo.connect.people.rap.providers.TitleIconRowLP;
import org.argeo.connect.people.rap.util.AbstractPanelFormPart;
import org.argeo.connect.people.rap.util.Refreshable;
import org.argeo.connect.people.ui.PeopleColumnDefinition;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiSnippets;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.util.JcrUiUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.eclipse.ui.dialogs.SingleValue;
import org.argeo.eclipse.ui.utils.ViewerUtils;
import org.argeo.eclipse.ui.workbench.CommandUtils;
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
import org.eclipse.jface.window.Window;
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
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * A composite to include in a form and that enables edition of the values of a
 * catalog from a given template.
 * 
 * This might be extended to provide a specific request that will limit the
 * perimeter on which the current catalog might apply (typically, if we use two
 * distinct nomenclature for a same property that are bound to a given projects)
 */
public class TemplateValueCatalogue extends Composite {
	private static final long serialVersionUID = -5018569293721397600L;

	// Context
	private final PeopleWorkbenchService peopleWorkbenchService;
	private final PeopleService peopleService;
	private final Node templateNode;
	private final String propertyName;
	private final String taggableType;

	// UI Context
	private final IWorkbench workbench;
	private final FormToolkit toolkit;
	private final AbstractPeopleEditor editor;
	private final MyFormPart myFormPart;

	public TemplateValueCatalogue(IWorkbench workbench,
			AbstractPeopleEditor editor, Composite parent, int style,
			PeopleService peopleService,
			PeopleWorkbenchService peopleWorkbenchService, Node templateNode,
			String propertyName, String taggableType) {
		super(parent, style);
		this.workbench = workbench;
		this.editor = editor;
		this.toolkit = editor.getFormToolkit();
		this.peopleService = peopleService;
		this.peopleWorkbenchService = peopleWorkbenchService;
		this.templateNode = templateNode;
		this.propertyName = propertyName;
		this.taggableType = taggableType;
		this.setLayout(EclipseUiUtils.noSpaceGridLayout());
		myFormPart = new MyFormPart(this);
		myFormPart.initialize(editor.getManagedForm());
		editor.getManagedForm().addPart(myFormPart);
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
			if (isEditing()) {
				panel.setLayout(new GridLayout());
				addBtn = toolkit.createButton(panel, "Add a value", SWT.PUSH);
				configureAddValueBtn(this, addBtn);
			} else {
				GridLayout gl = EclipseUiUtils.noSpaceGridLayout();
				gl.verticalSpacing = 5;
				panel.setLayout(gl);
			}

			// Item list
			Composite valuesCmp = new Composite(panel, SWT.NO_FOCUS);
			GridData gd = EclipseUiUtils.fillWidth();
			gd.heightHint = 150;
			valuesCmp.setLayoutData(gd);
			valuesViewer = createValuesViewer(valuesCmp);
			valuesViewer.setContentProvider(new ValuesTableCP());
			valuesViewer.getTable().addSelectionListener(
					new MyEditRemoveAdapter());

			Composite instancesCmp = new Composite(panel, SWT.NO_FOCUS);
			instancesCmp.setLayoutData(EclipseUiUtils.fillAll());
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
										JcrUiUtils.rowIteratorToArray(rit));
							}
						}
					});

			refreshContent(panel, editionInfo);
		}

		protected void refreshContent(Composite parent, Node editionInfo) {
			try {
				valuesViewer.setInput(JcrUiUtils.getMultiAsList(templateNode,
						propertyName).toArray(new String[0]));
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

			Ordering order = factory
					.ascending(factory.upperCase(factory.propertyValue(
							source.getSelectorName(), Property.JCR_TITLE)));
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
		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());
		final Table table = new Table(parent, SWT.SINGLE | SWT.V_SCROLL
				| SWT.H_SCROLL);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(EclipseUiUtils.fillAll());
		table.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
		table.setData(RWT.CUSTOM_ITEM_HEIGHT, Integer.valueOf(24));

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

		if (editor.isEditing()) {
			col = ViewerUtils.createTableViewerColumn(viewer, "", SWT.NONE, 80);
			col.setLabelProvider(new ColumnLabelProvider() {
				private static final long serialVersionUID = 1L;

				@Override
				public String getText(Object element) {
					String value = PeopleUiUtils
							.replaceAmpersand((String) element);
					String editLink = PeopleUiSnippets
							.getRWTLink(
									PeopleUiConstants.CRUD_EDIT
											+ PeopleRapConstants.HREF_SEPARATOR
											+ value,
									PeopleUiConstants.CRUD_EDIT);
					String removeLink = PeopleUiSnippets
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

	private class MyEditRemoveAdapter extends SelectionAdapter {
		private static final long serialVersionUID = 1L;

		public MyEditRemoveAdapter() {
		}

		public void widgetSelected(SelectionEvent event) {
			if (event.detail == RWT.HYPERLINK) {
				String href = event.text;
				String[] val = href.split(PeopleRapConstants.HREF_SEPARATOR);
				EditValueWizard wizard = new EditValueWizard(val[0], val[1]);
				NoProgressBarWizardDialog dialog = new NoProgressBarWizardDialog(
						TemplateValueCatalogue.this.getShell(), wizard);

				if (Window.OK == dialog.open()) {
					try {
						// Session is not saved when no object is linked to this
						// catalogue value.
						if (templateNode.getSession().hasPendingChanges())
							templateNode.getSession().save();
					} catch (RepositoryException re) {
						throw new PeopleException("Unable to save session for "
								+ templateNode, re);
					}

//					// Small workaround to keep the calling editor in a clean a
//					// logical state regarding its check out status
//					IWorkbench wb = PlatformUI.getWorkbench();
//					IEditorPart editor = wb.getActiveWorkbenchWindow()
//							.getActivePage().getActiveEditor();
//					if (editor != null && editor instanceof Refreshable) {
//						// Cancel and Check In
//						Map<String, String> params = new HashMap<String, String>();
//						params.put(ChangeEditingState.PARAM_NEW_STATE,
//								ChangeEditingState.NOT_EDITING);
//						params.put(ChangeEditingState.PARAM_PRIOR_ACTION,
//								ChangeEditingState.PRIOR_ACTION_CANCEL);
//						CommandUtils.callCommand(ChangeEditingState.ID);
//						((Refreshable) editor).forceRefresh(null);
//
//					}
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
		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());
		ArrayList<PeopleColumnDefinition> colDefs = new ArrayList<PeopleColumnDefinition>();
		colDefs.add(new PeopleColumnDefinition(taggableType,
				Property.JCR_TITLE, PropertyType.STRING, "Instances",
				new TitleIconRowLP(peopleWorkbenchService, taggableType,
						Property.JCR_TITLE), 350));
		VirtualJcrTableViewer tableCmp = new VirtualJcrTableViewer(parent,
				SWT.MULTI, colDefs);
		tableCmp.setLayoutData(EclipseUiUtils.fillAll());
		TableViewer viewer = tableCmp.getTableViewer();
		viewer.setContentProvider(new InstancesTableCP(viewer));
		return viewer;
	}

	private class InstanceDClickAdapter extends PeopleDoubleClickAdapter {
		@Override
		protected void processDoubleClick(Object obj) {
			Node occurrence = JcrUiUtils.getNode((Row) obj, taggableType);
			CommandUtils.callCommand(
					peopleWorkbenchService.getOpenEntityEditorCmdId(),
					OpenEntityEditor.PARAM_JCR_ID,
					JcrUiUtils.getIdentifier(occurrence));
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
				String value = SingleValue.ask("New Option",
						"Enrich current catalogue with a new option");
				if (EclipseUiUtils.notEmpty(value)) {
					String errMsg = JcrUiUtils.addMultiPropertyValue(
							templateNode, propertyName, value);
					if (EclipseUiUtils.isEmpty(errMsg)) {
						formPart.markDirty();
						formPart.refresh();
					} else
						MessageDialog.openError(button.getShell(),
								"Duplicate value", errMsg);
				}

			}
		});
	}

	public class EditValueWizard extends Wizard implements PeopleNames {

		// Context
		private final String actionType;
		private final String oldValue;

		// This part widgets
		private Text newValueTxt;

		public EditValueWizard(String actionType, String oldValue) {
			this.actionType = actionType;
			this.oldValue = oldValue;
		}

		@Override
		public void addPages() {
			try {
				setWindowTitle("Update wizard");
				if (PeopleUiConstants.CRUD_EDIT.equals(actionType)) {
					MainInfoPage inputPage = new MainInfoPage("Configure");
					addPage(inputPage);
				}
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
			String errMsg = null;
			String newValue = null;
			ResourceService rs = peopleService.getResourceService();

			List<String> existingValues = rs.getTemplateCatalogue(templateNode,
					propertyName, null);

			// Sanity checks for update only
			if (PeopleUiConstants.CRUD_EDIT.equals(actionType)) {
				newValue = newValueTxt.getText();

				if (EclipseUiUtils.isEmpty(newValue))
					errMsg = "New value cannot be blank or an empty string";
				else if (oldValue.equals(newValue))
					errMsg = "New value is the same as old one.\n"
							+ "Either enter a new one or press cancel.";
				else if (existingValues.contains(newValue))
					errMsg = "The new chosen value is already used.\n"
							+ "Either enter a new one or press cancel.";
			}
			if (errMsg != null) {
				MessageDialog.openError(getShell(), "Unvalid information",
						errMsg);
				return false;
			}

			rs.updateCatalogueValue(templateNode, taggableType, propertyName,
					oldValue, newValue);
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
				setTitle("Enter a new value for this catalogue's item");
				setMessage("As reminder, former value was: \"" + oldValue
						+ "\"");
			}

			public void createControl(Composite parent) {
				Composite body = new Composite(parent, SWT.NONE);
				body.setLayout(new GridLayout(2, false));

				// New Title Value
				PeopleRapUtils.createBoldLabel(body, "New Value");
				newValueTxt = new Text(body, SWT.BORDER);
				newValueTxt.setMessage("was: " + oldValue);
				newValueTxt.setText(oldValue);
				newValueTxt.setLayoutData(EclipseUiUtils.fillWidth());
				setControl(body);
				newValueTxt.setFocus();
			}
		}

		protected class RecapPage extends WizardPage {
			private static final long serialVersionUID = 1L;
			private TableViewer membersViewer;
			private Composite body;

			public RecapPage(String pageName) {
				super(pageName);
				setTitle("Check and confirm");
				setMessage("The below listed items will be impacted.\nAre you sure you want to proceed?");
			}

			public void createControl(Composite parent) {
				body = new Composite(parent, SWT.NONE);
				membersViewer = createInstancesViewer(body);
				setControl(body);
			}

			@Override
			public void setVisible(boolean visible) {
				super.setVisible(visible);

				// In the newer approach, all nodes are always checked out.
				// TODO We should rather rely on another mechanism to
				// investigate on potential blockers before launching the batch
				// update

				RowIterator rit = query(oldValue);
				List<Row> rows = new ArrayList<Row>();
				while (rit.hasNext())
					rows.add(rit.nextRow());
				setViewerInput(membersViewer, rows.toArray(new Row[0]));
			}
		}
	}

	// Add a decorator to the checked out instances
	// private class MyTitleIconRowLP extends TitleIconRowLP {
	// private static final long serialVersionUID = 1L;
	// private final Map<Image, Image> images = new HashMap<Image, Image>();
	// private final ImageDescriptor failedDesc;
	//
	// public MyTitleIconRowLP(PeopleWorkbenchService peopleUiService,
	// String selectorName, String propertyName) {
	// super(peopleUiService, selectorName, propertyName);
	// // this.selectorName = selectorName;
	// failedDesc = workbench.getSharedImages().getImageDescriptor(
	// ISharedImages.IMG_DEC_FIELD_ERROR);
	// }
	//
	// @Override
	// public Image getImage(Object element) {
	// Image image = super.getImage(element);
	// // Node currEntity = JcrUiUtils.getNode((Row) element,
	// // selectorName);
	// // JcrUiUtils.isNodeCheckedOut(currEntity)
	// if (editor.isEditing() && image != null) {
	// if (images.containsKey(image)) {
	// image = images.get(image);
	// } else {
	// Image descImage = new DecorationOverlayIcon(image,
	// failedDesc, IDecoration.BOTTOM_RIGHT).createImage();
	// images.put(image, descImage);
	// image = descImage;
	// }
	// }
	// return image;
	// }
	//
	// @Override
	// public void dispose() {
	// // Free created image resources
	// for (Image image : images.values())
	// image.dispose();
	// super.dispose();
	// }
	// }
}