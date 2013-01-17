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
package org.argeo.connect.demo.gr.ui.commands;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.ArgeoException;
import org.argeo.connect.demo.gr.GrConstants;
import org.argeo.connect.demo.gr.ui.GrImages;
import org.argeo.connect.demo.gr.ui.GrUiPlugin;
import org.argeo.connect.demo.gr.ui.wizards.ImportFromDeviceWizard;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Recursively upload files and directory from the user file system to JCR TODO:
 * filter unwanted files and directories
 * 
 */
public class ImportInstancesFromDevice extends AbstractHandler {
	// private static Log log = LogFactory.getLog(ImportDirectoryContent.class);

	/* DEPENDENCY INJECTION */
	private Session session;

	public final static String ID = GrUiPlugin.PLUGIN_ID
			+ ".importInstancesFromDevice";
	public final static Image DEFAULT_ICON = GrImages.ICON_IMPORT_INSTANCES;
	public final static String DEFAULT_LABEL = "Upload data from a mounted device";

	public Object execute(ExecutionEvent event) throws ExecutionException {

		try {
			Node importParentNode = session
					.getNode(GrConstants.GR_IMPORTS_BASE_PATH);
			ImportFromDeviceWizard wizard = new ImportFromDeviceWizard(
					importParentNode);
			WizardDialog dialog = new WizardDialog(
					HandlerUtil.getActiveShell(event), wizard);
			dialog.open();
		} catch (RepositoryException e) {
			throw new ArgeoException("Unexpected exception "
					+ "while importing new instances from a device ", e);
		}
		return null;
	}

	@Override
	public void dispose() {
		JcrUtils.logoutQuietly(session);
	}

	/* DEPENDENCY INJECTION */
	public void setRepository(Repository repository) {
		try {
			session = repository.login();
		} catch (RepositoryException e) {
			throw new ArgeoException("Unexpected exception "
					+ "while creating a session to "
					+ "import new instances from a device ", e);
		}
	}
}