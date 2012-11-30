/*
 * Argeo Connect - Data management and communications
 * Copyright (C) 2012 Argeo GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or combining it
 * with software covered by the terms of the Eclipse Public License, the
 * licensors of this Program grant you additional permission to convey the
 * resulting work. Corresponding Source for a non-source form of such a
 * combination shall include the source code for the parts of such software
 * which are used as well as that of the covered work.
 */
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
