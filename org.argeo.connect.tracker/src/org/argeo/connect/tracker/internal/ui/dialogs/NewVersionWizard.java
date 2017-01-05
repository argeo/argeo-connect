package org.argeo.connect.tracker.internal.ui.dialogs;

import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.connect.people.rap.composites.DateText;
import org.argeo.connect.tracker.TrackerException;
import org.argeo.connect.tracker.TrackerService;
import org.argeo.connect.tracker.core.TrackerUtils;
import org.argeo.connect.tracker.internal.ui.dialogs.NewVersionWizard;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/** Dialog to simply configure a new milestone */
public class NewVersionWizard extends Wizard implements ModifyListener {
	private static final long serialVersionUID = -8365425809976445458L;

	// Context
	final private TrackerService issueService;
	final private Node project;

	// this page widgets and UI objects
	private Text idTxt;
	private DateText targetDateCmp;
	private DateText releaseDateCmp;
	private Text descTxt;

	public NewVersionWizard(TrackerService issueService, Node project) {
		this.project = project;
		this.issueService = issueService;
	}

	@Override
	public void addPages() {
		setWindowTitle("Create a new version");
		ConfigureVersionPage page = new ConfigureVersionPage("Main page");
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		// TODO rather use error messages than an error popup
		Calendar now = new GregorianCalendar();
		if (EclipseUiUtils.isEmpty(getId())) {
			MessageDialog.openError(getShell(), "Compulsory ID",
					"Please define the version ID");
			return false;
		} else if (TrackerUtils.getVersionById(project, getId()) != null) {
			MessageDialog.openError(getShell(), "Already existing version",
					"A version with ID " + getId()
							+ " already exists, cannot create");
			return false;
		} else if (getReleaseDate() != null && getReleaseDate().after(now)) {
			MessageDialog.openError(getShell(), "Non-valid release date",
					"A release date can only be defined when the release "
							+ "has already been done, and thus must be "
							+ "in the past.");
			return false;

		} else if (getTargetDate() != null && getTargetDate().before(now)) {
			MessageDialog.openError(getShell(), "Non-valid target date",
					"A target date must be in the future:\n we cannot change "
							+ "the past and must not plan at a past date a "
							+ "release that has not already been done.");
			return false;
		}

		try {
			issueService.createVersion(project, getId(), getDescription(),
					getTargetDate(), getReleaseDate());
		} catch (RepositoryException e1) {
			throw new TrackerException("Unable to create" + "version with ID "
					+ getId() + " on " + project, e1);
		}
		return true;
	}

	@Override
	public boolean canFinish() {
		if (EclipseUiUtils.isEmpty(getId()))
			return false;
		else
			return true;
	}

	@Override
	public boolean performCancel() {
		return true;
	}

	protected class ConfigureVersionPage extends WizardPage {
		private static final long serialVersionUID = 3061153468301727903L;

		public ConfigureVersionPage(String pageName) {
			super(pageName);
			setTitle("Create a new version");
			setMessage("Please fill out following information.");
		}

		public void createControl(Composite parent) {
			parent.setLayout(new GridLayout(4, false));

			createLabel(parent, "ID", SWT.CENTER);
			idTxt = new Text(parent, SWT.BORDER);
			idTxt.setMessage("Major.Minor.Micro: 2.1.37");
			GridData gd = EclipseUiUtils.fillWidth();
			gd.horizontalSpan = 3;
			idTxt.setLayoutData(gd);
			idTxt.addModifyListener(NewVersionWizard.this);

			createLabel(parent, "Target Date", SWT.CENTER);
			targetDateCmp = new DateText(parent, SWT.NO_FOCUS);
			targetDateCmp.setToolTipText("An optional "
					+ "future date for this milestone");

			Label lbl = createLabel(parent, "Release Date ", SWT.CENTER);
			gd = new GridData();
			gd.horizontalIndent = 15;
			lbl.setLayoutData(gd);
			releaseDateCmp = new DateText(parent, SWT.NO_FOCUS);
			releaseDateCmp
					.setToolTipText("Define a past date when "
							+ "creating a milestone for an already released version,\n "
							+ "typically in the case of reporting a bug for a version "
							+ "that was not yet listed.");

			createLabel(parent, "Description", SWT.TOP);
			descTxt = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.WRAP);
			gd = EclipseUiUtils.fillAll();
			gd.horizontalSpan = 3;
			descTxt.setLayoutData(gd);
			descTxt.setMessage("An optional description for this milestone");

			// Don't forget this.
			setControl(idTxt);
		}
	}

	public String getId() {
		return idTxt.getText();
	}

	public Calendar getTargetDate() {
		return targetDateCmp.getCalendar();
	}

	public Calendar getReleaseDate() {
		return releaseDateCmp.getCalendar();
	}

	public String getDescription() {
		return descTxt.getText();
	}

	private Label createLabel(Composite parent, String label, int verticalAlign) {
		Label lbl = new Label(parent, SWT.NONE);
		lbl.setText(label);
		lbl.setFont(EclipseUiUtils.getBoldFont(parent));
		lbl.setLayoutData(new GridData(SWT.RIGHT, verticalAlign, false, false));
		return lbl;
	}

	@Override
	public void modifyText(ModifyEvent event) {
		getContainer().updateButtons();
	}
}