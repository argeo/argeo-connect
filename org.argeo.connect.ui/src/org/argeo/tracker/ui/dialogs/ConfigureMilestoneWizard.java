package org.argeo.tracker.ui.dialogs;

import static javax.jcr.Property.JCR_DESCRIPTION;
import static javax.jcr.Property.JCR_TITLE;
import static org.argeo.connect.util.ConnectJcrUtils.get;
import static org.argeo.eclipse.ui.EclipseUiUtils.isEmpty;

import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.argeo.cms.CmsUserManager;
import org.argeo.connect.core.OfficeRole;
import org.argeo.connect.ui.ConnectUiUtils;
import org.argeo.connect.ui.widgets.DateText;
import org.argeo.connect.ui.widgets.GroupDropDown;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;
import org.argeo.tracker.TrackerException;
import org.argeo.tracker.TrackerNames;
import org.argeo.tracker.TrackerService;
import org.argeo.tracker.TrackerTypes;
import org.argeo.tracker.ui.controls.ProjectDropDown;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/** Dialog to simply configure a milestone */
public class ConfigureMilestoneWizard extends Wizard {

	// Context
	private final CmsUserManager userAdminService;
	private final TrackerService trackerService;
	private final Node project;
	private final Node milestone;

	// Ease implementation
	private Node chosenProject;

	// UI objects
	private ProjectDropDown projectDD;
	private Text titleTxt;
	private GroupDropDown managerDD;
	private GroupDropDown defaultAssigneeDD;
	private DateText targetDateCmp;
	private Label isVersionLbl;
	private Button isVersionBtn;
	private Label versionIdLbl;
	private Text versionIdTxt;
	private Label releaseDateLbl;
	private DateText releaseDateCmp;
	private Text descTxt;

	public ConfigureMilestoneWizard(CmsUserManager userAdminService, TrackerService trackerService, Node milestone) {
		this.userAdminService = userAdminService;
		this.trackerService = trackerService;
		this.milestone = milestone;
		try {
			if (milestone.hasProperty(TrackerNames.TRACKER_PROJECT_UID)) {
				project = trackerService.getEntityByUid(milestone.getSession(), null,
						milestone.getProperty(TrackerNames.TRACKER_PROJECT_UID).getString());
			} else
				project = null;
		} catch (RepositoryException e) {
			throw new TrackerException("Cannot retrieve project for " + milestone, e);
		}
	}

	@Override
	public void addPages() {
		setWindowTitle("Milestone configuration");
		MainPage page = new MainPage("Main page");
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		String msg = null;
		String title = titleTxt.getText();

		boolean isVersion = isVersionBtn.getSelection();
		String versionId = versionIdTxt != null && !versionIdTxt.isDisposed() ? versionIdTxt.getText() : null;
		Calendar releaseDate = releaseDateCmp != null && !releaseDateCmp.isDisposed() ? releaseDateCmp.getCalendar()
				: null;
		if (chosenProject == null)
			msg = "Please pick up a project";
		else if (isEmpty(title))
			msg = "Please give at least a title";
		else if (isVersion && (EclipseUiUtils.isEmpty(versionId) || releaseDate == null)) {
			msg = "You have to provide a version ID *and* a release date to mark this milestone as version";
		}

		if (msg != null) {
			MessageDialog.openError(getShell(), "Uncomplete information", msg);
			return false;
		}

		try {
			trackerService.configureMilestone(milestone, chosenProject, null, title, descTxt.getText(),
					managerDD.getText(), defaultAssigneeDD.getText(), targetDateCmp.getCalendar());

			if (isVersion) {
				if (!milestone.isNodeType(TrackerTypes.TRACKER_VERSION))
					milestone.addMixin(TrackerTypes.TRACKER_VERSION);
				milestone.setProperty(TrackerNames.TRACKER_ID, versionId);
				milestone.setProperty(TrackerNames.TRACKER_RELEASE_DATE, releaseDate);
			}

			if (milestone.getSession().hasPendingChanges())
				JcrUtils.updateLastModified(milestone);
			return true;
		} catch (RepositoryException e1) {
			throw new TrackerException("Unable to create version with ID " + title + " on " + project, e1);
		}
	}

	@Override
	public boolean canFinish() {
		if (EclipseUiUtils.isEmpty(titleTxt.getText()) || chosenProject == null)
			return false;
		else
			return true;
	}

	@Override
	public boolean performCancel() {
		return true;
	}

	private class MainPage extends WizardPage {
		private static final long serialVersionUID = 3061153468301727903L;

		private Text projectTxt;

		public MainPage(String pageName) {
			super(pageName);
			setMessage("Please complete below information");
		}

		public void createControl(final Composite parent) {
			parent.setLayout(new GridLayout(4, false));
			FocusListener fl = getFocusListener();

			// Project
			ConnectUiUtils.createBoldLabel(parent, "Project");
			projectTxt = new Text(parent, SWT.BORDER);
			projectTxt.setMessage("Choose relevant project");
			GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
			gd.horizontalSpan = 3;
			projectTxt.setLayoutData(gd);

			if (project == null) {
				projectDD = new ProjectDropDown(ConnectJcrUtils.getSession(milestone), projectTxt, false);

				projectTxt.addFocusListener(new FocusAdapter() {
					private static final long serialVersionUID = 1719432159240562984L;

					@Override
					public void focusLost(FocusEvent event) {
						Node project = projectDD.getChosenProject();
						if (project == null)
							setErrorMessage("Choose a valid project");
						else {
							setErrorMessage(null);
							afterProjectSet(project, false);
						}
					}
				});
			} else
				projectTxt.setEditable(false);

			createLabel(parent, "Milestone Name", SWT.CENTER);
			titleTxt = new Text(parent, SWT.BORDER);
			titleTxt.setMessage("A clear name");
			titleTxt.setLayoutData(EclipseUiUtils.fillWidth(3));
			titleTxt.addFocusListener(fl);

			createLabel(parent, "Manager", SWT.CENTER);
			Text managerTxt = new Text(parent, SWT.BORDER);
			managerTxt.setMessage("Choose a group");
			managerTxt.setLayoutData(EclipseUiUtils.fillWidth());
			managerDD = new GroupDropDown(managerTxt, userAdminService, OfficeRole.coworker.dn());

			createLabel(parent, "Default Assignee", SWT.CENTER);
			Text defaultAssigneeTxt = new Text(parent, SWT.BORDER);
			defaultAssigneeTxt.setMessage("Choose a group");
			defaultAssigneeTxt.setLayoutData(EclipseUiUtils.fillWidth());
			defaultAssigneeDD = new GroupDropDown(defaultAssigneeTxt, userAdminService, OfficeRole.coworker.dn());

			createLabel(parent, "Target Date", SWT.CENTER);
			targetDateCmp = new DateText(parent, SWT.NO_FOCUS);
			targetDateCmp.setLayoutData(EclipseUiUtils.fillWidth());
			targetDateCmp.setToolTipText("An optional future due date for this milestone");

			// if (ConnectJcrUtils.isNodeType(project,
			// TrackerTypes.TRACKER_IT_PROJECT)){
			isVersionLbl = createLabel(parent, "Is released version", SWT.CENTER);
			isVersionBtn = new Button(parent, SWT.CHECK);

			versionIdLbl = createLabel(parent, "Version ID", SWT.CENTER);
			versionIdTxt = new Text(parent, SWT.BORDER);
			versionIdTxt.setMessage("Major.Minor.Micro: 2.1.37");
			versionIdTxt.setToolTipText("The version id syntax must be valid, major and minor are compulsory");
			versionIdTxt.setLayoutData(EclipseUiUtils.fillWidth());

			releaseDateLbl = createLabel(parent, "Release date", SWT.CENTER);
			releaseDateCmp = new DateText(parent, SWT.NO_FOCUS);
			releaseDateCmp.setLayoutData(EclipseUiUtils.fillWidth());
			releaseDateCmp.setToolTipText("The version's release date");

			createLabel(parent, "Description", SWT.TOP);
			descTxt = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.WRAP);
			gd = EclipseUiUtils.fillWidth(3);
			gd.heightHint = 150;
			descTxt.setLayoutData(gd);
			descTxt.setMessage("An optional description for this milestone");

			// Initialise values
			String titleStr = get(milestone, JCR_TITLE);
			try {
				titleTxt.setText(titleStr);
				descTxt.setText(get(milestone, JCR_DESCRIPTION));
				targetDateCmp.setText(ConnectJcrUtils.getDateValue(milestone, TrackerNames.TRACKER_TARGET_DATE));
				if (milestone.hasProperty(TrackerNames.TRACKER_MANAGER))
					managerDD.resetDN(milestone.getProperty(TrackerNames.TRACKER_MANAGER).getString());
				if (milestone.hasProperty(TrackerNames.TRACKER_DEFAULT_ASSIGNEE))
					defaultAssigneeDD.resetDN(milestone.getProperty(TrackerNames.TRACKER_DEFAULT_ASSIGNEE).getString());
				if (project != null)
					projectTxt.setText(ConnectJcrUtils.get(project, Property.JCR_TITLE));

				afterProjectSet(project, ConnectJcrUtils.isNodeType(milestone, TrackerTypes.TRACKER_VERSION));

			} catch (RepositoryException e) {
				throw new TrackerException("Cannot initialise widgets with existing data for " + milestone, e);
			}
			if (project == null) {
				setControl(projectTxt);
				projectTxt.setFocus();
			} else if (isEmpty(titleStr)) {
				setControl(titleTxt);
				titleTxt.setFocus();
			} else
				setControl(titleTxt);

			isVersionBtn.addSelectionListener(new SelectionAdapter() {
				private static final long serialVersionUID = -9065523003707104389L;

				@Override
				public void widgetSelected(SelectionEvent e) {
					afterProjectSet(project, isVersionBtn.getSelection());
					parent.layout(true, true);
				}
			});
		}

		private void afterProjectSet(Node project, boolean isVersion) {
			chosenProject = project;
			boolean show = chosenProject != null
					&& ConnectJcrUtils.isNodeType(project, TrackerTypes.TRACKER_IT_PROJECT);
			isVersionLbl.setVisible(show);
			isVersionBtn.setVisible(show);
			show &= isVersion;

			if (ConnectJcrUtils.isNodeType(milestone, TrackerTypes.TRACKER_VERSION)) {
				versionIdTxt.setText(ConnectJcrUtils.get(milestone, TrackerNames.TRACKER_ID));
				releaseDateCmp.setText(ConnectJcrUtils.getDateValue(milestone, TrackerNames.TRACKER_RELEASE_DATE));
			}
			showWidget(versionIdLbl, show);
			showWidget(versionIdTxt, show);
			showWidget(releaseDateLbl, show);
			showWidget(releaseDateCmp, show);
		}

		private void showWidget(Control control, boolean show) {
			GridData gd = (GridData) control.getLayoutData();
			control.setVisible(show);
			if (show)
				gd.heightHint = SWT.DEFAULT;
			else
				gd.heightHint = 0;
		}

	}

	private FocusListener getFocusListener() {
		return new FocusAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void focusLost(FocusEvent event) {
				getContainer().updateButtons();
			}
		};
	}

	private Label createLabel(Composite parent, String label, int verticalAlign) {
		Label lbl = new Label(parent, SWT.NONE);
		lbl.setText(label);
		lbl.setFont(EclipseUiUtils.getBoldFont(parent));
		lbl.setLayoutData(new GridData(SWT.LEAD, verticalAlign, false, false));
		return lbl;
	}
}
