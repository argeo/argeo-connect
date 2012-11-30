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

import java.io.File;

import javax.jcr.Repository;

import org.argeo.ArgeoException;
import org.argeo.connect.demo.gr.pdf.SiteReportPublisher;
import org.argeo.connect.demo.gr.ui.GrUiPlugin;
import org.argeo.connect.demo.gr.ui.providers.TmpFileProvider;
import org.argeo.eclipse.ui.specific.FileHandler;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

/** Generates a site report in PDF */
public class GenerateSiteReport extends AbstractHandler {
	public final static String ID = GrUiPlugin.PLUGIN_ID
			+ ".generateSiteReport";
	public final static String PARAM_UID = GrUiPlugin.PLUGIN_ID + ".siteUid";

	/* DEPENDENCY INJECTION */
	private Repository repository;

	public Object execute(ExecutionEvent event) throws ExecutionException {
		String uid = event.getParameter(PARAM_UID);
		try {
			SiteReportPublisher srp = new SiteReportPublisher(repository);

			File file = srp.createNewReport(uid);
			TmpFileProvider ssfp = new TmpFileProvider();
			FileHandler fileHandler = new FileHandler(ssfp);
			StringBuffer fileName = new StringBuffer();
			fileName.append("SiteReport-");
			// TODO put site name instead of site UID
			fileName.append(uid);
			fileName.append(".pdf");
			fileHandler.openFile(fileName.toString(), file.getAbsolutePath());
			// try {
			// file.delete();
			// } catch (Exception e) {
			// // silent: we also have the delete on exit set.
			// }

		} catch (Exception e) {
			throw new ArgeoException("Cannot generate site report", e);
		}
		return null;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	/* DEPENDENCY INJECTION */

}
