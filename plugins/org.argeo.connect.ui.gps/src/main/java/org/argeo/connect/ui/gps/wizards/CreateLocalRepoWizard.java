package org.argeo.connect.ui.gps.wizards;

import javax.jcr.Node;
import javax.jcr.Session;

import org.argeo.ArgeoException;
import org.argeo.connect.gpx.TrackDao;
import org.argeo.connect.gpx.utils.JcrSessionUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.wizard.Wizard;

public class CreateLocalRepoWizard extends Wizard {

	private Session jcrSession;
	private TrackDao trackDao;

	// This page widget
	private DefineRepositoryModel defineRepositoryModel;

	public CreateLocalRepoWizard(Session jcrSession, TrackDao trackDao) {
		super();
		this.jcrSession = jcrSession;
		this.trackDao = trackDao;
	}

	@Override
	public void addPages() {
		try {
			defineRepositoryModel = new DefineRepositoryModel();
			addPage(defineRepositoryModel);
		} catch (Exception e) {
			throw new ArgeoException("Cannot add page to wizard ", e);
		}
	}

	@Override
	public boolean performFinish() {
		if (!canFinish())
			return false;

		String username = jcrSession.getUserID();
		Node userHomeDirectory = JcrUtils.createUserHomeIfNeeded(jcrSession,
				username);
		Node parentNode = trackDao
				.getLocalRepositoriesParentNode(userHomeDirectory);
		JcrSessionUtils.createLocalRepository(parentNode,
				defineRepositoryModel.getTechName(),
				defineRepositoryModel.getDisplayName());
		// TODO: refresh the tree
		//
		// GpsBrowserView view = (GpsBrowserView)
		// ConnectUiGpsPlugin.getDefault()
		// .getWorkbench().getViewRegistry().find(GpsBrowserView.ID);
		// view.refresh(parentNode);

		return true;
	}
}
