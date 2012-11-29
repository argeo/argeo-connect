package org.argeo.connect.ui.gps.wizards;

import javax.jcr.Node;

import org.argeo.ArgeoException;
import org.argeo.connect.gpx.JcrSessionUtils;
import org.argeo.connect.ui.gps.ConnectGpsUiPlugin;
import org.argeo.connect.ui.gps.GpsUiJcrServices;
import org.argeo.connect.ui.gps.views.GpsBrowserView;
import org.eclipse.jface.wizard.Wizard;

public class CreateLocalRepoWizard extends Wizard {

	// private Session jcrSession;
	private GpsUiJcrServices uiJcrServices;

	// This page widget
	private DefineRepositoryModel defineRepositoryModel;

	public CreateLocalRepoWizard(GpsUiJcrServices uiJcrServices) {
		super();
		this.uiJcrServices = uiJcrServices;
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
		Node parentNode = uiJcrServices.getLocalRepositoriesParentNode();
		JcrSessionUtils.createLocalRepository(parentNode,
				defineRepositoryModel.getTechName(),
				defineRepositoryModel.getDisplayName());

		// refresh the tree
		GpsBrowserView gbView = (GpsBrowserView) ConnectGpsUiPlugin
				.getDefault().getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().findView(GpsBrowserView.ID);
		gbView.refresh(parentNode);
		return true;
	}
}
