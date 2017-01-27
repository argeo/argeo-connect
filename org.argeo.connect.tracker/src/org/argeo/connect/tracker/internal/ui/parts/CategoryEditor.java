package org.argeo.connect.tracker.internal.ui.parts;

import static org.argeo.eclipse.ui.EclipseUiUtils.notEmpty;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.argeo.cms.ui.workbench.util.CommandUtils;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.util.JcrUiUtils;
import org.argeo.connect.people.workbench.rap.commands.OpenEntityEditor;
import org.argeo.connect.tracker.TrackerException;
import org.argeo.connect.tracker.TrackerNames;
import org.argeo.connect.tracker.core.TrackerUtils;
import org.argeo.connect.tracker.internal.ui.AbstractTrackerEditor;
import org.argeo.connect.tracker.internal.ui.TrackerImages;
import org.argeo.connect.tracker.internal.ui.TrackerLps;
import org.argeo.connect.tracker.internal.ui.TrackerUiUtils;
import org.argeo.connect.tracker.internal.ui.dialogs.NewIssueWizard;
import org.argeo.connect.tracker.ui.TrackerUiPlugin;
import org.argeo.connect.ui.TechnicalInfoPage;
import org.argeo.eclipse.ui.ColumnDefinition;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.eclipse.ui.jcr.lists.SimpleJcrNodeLabelProvider;
import org.argeo.jcr.JcrUtils;
import org.argeo.node.ArgeoNames;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
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
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ScrolledForm;

/**
 * Basic editor to display a list of all issues that are related to a given
 * category (a milestone, a version, a component ...) within a project. It also
 * enable business admin to edit the main info of this category (typically :
 * title & description)
 */
public class CategoryEditor extends AbstractTrackerEditor {
	private static final long serialVersionUID = -6492660981141107302L;
	// private final static Log log = LogFactory.getLog(CategoryEditor.class);
	public static final String ID = TrackerUiPlugin.PLUGIN_ID + ".categoryEditor";

	// Context
	private Node project;
	private Node category;
	private String relevantPropName;
	private String officeID;

	// Ease implementation

	@Override
	protected void addPages() {
		// Initialise local cache
		category = getNode();
		project = TrackerUtils.getProjectFromChild(category);
		officeID = JcrUiUtils.get(category, TrackerNames.TRACKER_ID);
		relevantPropName = TrackerUtils.getRelevantPropName(category);
		try {
			MainPage mainPage = new MainPage(this);
			addPage(mainPage);

			TechnicalInfoPage techInfoPage = new TechnicalInfoPage(this,
					TrackerUiPlugin.PLUGIN_ID + ".projectEditor.techInfoPage", getNode());
			addPage(techInfoPage);
		} catch (PartInitException e) {
			throw new TrackerException("Cannot add pages for editor of " + getNode(), e);
		}
	}

	protected void updatePartName() {
		String name = getCategoryTitle();
		if (notEmpty(name))
			setPartName(name);
		else
			super.updatePartName();
	}

	private class MainPage extends FormPage implements ArgeoNames {
		public final static String ID = TrackerUiPlugin.PLUGIN_ID + ".projectEditor.componentsPage";

		private TableViewer tableViewer;

		public MainPage(FormEditor editor) {
			super(editor, ID, "Overview");
		}

		protected void createFormContent(final IManagedForm mf) {
			ScrolledForm form = mf.getForm();
			Composite body = form.getBody();
			GridLayout mainLayout = new GridLayout();
			body.setLayout(mainLayout);
			Composite filterCmp = new Composite(body, SWT.NO_FOCUS);
			createFilterPart(filterCmp);
			filterCmp.setLayoutData(EclipseUiUtils.fillWidth());
			Composite tableCmp = new Composite(body, SWT.NO_FOCUS);
			appendIssuesPart(tableCmp);
			tableCmp.setLayoutData(EclipseUiUtils.fillAll());
			form.reflow(true);
		}

		/** Creates the answers for one group */
		private void appendIssuesPart(Composite parent) {
			List<ColumnDefinition> columnDefs = new ArrayList<ColumnDefinition>();
			columnDefs.add(new ColumnDefinition(new SimpleJcrNodeLabelProvider(TrackerNames.TRACKER_ID), "ID", 40));
			columnDefs.add(new ColumnDefinition(new SimpleJcrNodeLabelProvider(Property.JCR_TITLE), "Title", 300));
			columnDefs.add(new ColumnDefinition(new SimpleJcrNodeLabelProvider(PeopleNames.PEOPLE_TASK_STATUS),
					"Status", 100));
			columnDefs.add(new ColumnDefinition(new TrackerLps().new ImportanceLabelProvider(), "Importance", 100));
			columnDefs.add(new ColumnDefinition(new TrackerLps().new PriorityLabelProvider(), "Priority", 100));

			// Create and configure the table
			tableViewer = TrackerUiUtils.createTableViewer(parent, SWT.SINGLE, columnDefs);
			tableViewer.addDoubleClickListener(new IDoubleClickListener() {

				@Override
				public void doubleClick(DoubleClickEvent event) {
					Object element = ((IStructuredSelection) event.getSelection()).getFirstElement();
					String jcrId = JcrUiUtils.getIdentifier((Node) element);
					CommandUtils.callCommand(getAoWbService().getOpenEntityEditorCmdId(), OpenEntityEditor.PARAM_JCR_ID,
							jcrId);
				}
			});
			refreshViewer(null);
		}

		private void refreshViewer(String filter) {
			NodeIterator nit = TrackerUtils.getIssues(project, filter, relevantPropName, officeID);
			tableViewer.setInput(JcrUtils.nodeIteratorToList(nit).toArray(new Node[0]));
		}

		// Add the filter ability
		private void createFilterPart(Composite parent) {
			GridLayout layout = EclipseUiUtils.noSpaceGridLayout(new GridLayout(2, false));
			layout.horizontalSpacing = 5;
			parent.setLayout(layout);
			parent.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			final Text filterTxt = new Text(parent, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH | SWT.ICON_CANCEL);
			filterTxt.setLayoutData(EclipseUiUtils.fillWidth());

			final Button addBtn = new Button(parent, SWT.PUSH);
			addBtn.setToolTipText("Create a new issue");
			addBtn.setImage(TrackerImages.ICON_ADD);

			filterTxt.addModifyListener(new ModifyListener() {
				private static final long serialVersionUID = 8130545587125370689L;

				public void modifyText(ModifyEvent event) {
					refreshViewer(filterTxt.getText());
				}
			});

			addBtn.addSelectionListener(new SelectionAdapter() {
				private static final long serialVersionUID = -7249705366574519524L;

				@Override
				public void widgetSelected(SelectionEvent e) {

					NewIssueWizard wizard = new NewIssueWizard(getPeopleService(), project);
					WizardDialog dialog = new WizardDialog(addBtn.getShell(), wizard);
					if (dialog.open() == Window.OK) {
						try {
							project.getSession().save();
							refreshViewer(filterTxt.getText());
							// TODO link to current category
						} catch (RepositoryException e1) {
							throw new TrackerException("Unable to create issue on " + project, e1);
						}
					}
				}
			});
		}
	}

	private String getCategoryTitle() {
		String name = JcrUiUtils.get(getNode(), Property.JCR_TITLE);
		if (notEmpty(name)) {
			Node project = TrackerUtils.getProjectFromChild(getNode());
			String pname = JcrUiUtils.get(project, Property.JCR_TITLE);
			name = name + (notEmpty(pname) ? " (" + pname + ")" : "");
		}
		return name;
	}
}
