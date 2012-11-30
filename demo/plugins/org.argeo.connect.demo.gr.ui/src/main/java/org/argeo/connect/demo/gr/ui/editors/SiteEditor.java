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
package org.argeo.connect.demo.gr.ui.editors;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.ArgeoException;
import org.argeo.connect.demo.gr.GrException;
import org.argeo.connect.demo.gr.GrNames;
import org.argeo.connect.demo.gr.ui.GrMessages;
import org.argeo.connect.demo.gr.ui.GrUiPlugin;
import org.argeo.connect.demo.gr.ui.providers.GrNodeLabelProvider;
import org.argeo.gis.ui.MapControlCreator;
import org.argeo.gis.ui.editors.MapFormPage;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

/**
 * Main multi tab editor to display and modify a site.
 */
public class SiteEditor extends AbstractGrEditor implements GrNames {

	public final static String ID = GrUiPlugin.PLUGIN_ID + ".siteEditor";

	private MapControlCreator mapControlCreator;

	private Node network;
	private Node currentSite;

	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);

		SiteEditorInput sei = (SiteEditorInput) getEditorInput();
		try {
			currentSite = getSession().getNodeByIdentifier(sei.getUid());
			network = currentSite.getParent();
			this.setPartName(GrNodeLabelProvider.getName(currentSite));
			setTitleImage(GrNodeLabelProvider.getIcon(currentSite));
		} catch (RepositoryException e) {
			throw new GrException(
					"Error while initialising SiteEditor with JCR information.",
					e);
		}
	}

	@Override
	protected void addPages() {
		try {
			addPage(new SiteDetailsPage(this,
					GrMessages.get().siteEditor_detailPage_title));
			MapFormPage mapFormPage = new SiteMapDisplayPage(this, "map",
					GrMessages.get().siteEditor_mapPage_title, currentSite,
					mapControlCreator);
			addPage(mapFormPage);
		} catch (PartInitException e) {
			throw new ArgeoException("Not able to add page ", e);
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		try {
			// Automatically commit all pages of the editor
			commitPages(true);
			currentSite.getSession().save();
			firePropertyChange(PROP_DIRTY);
		} catch (Exception e) {
			throw new ArgeoException("Error while saving site: " + currentSite,
					e);
		}
	}

	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}

	Node getNetwork() {
		return network;
	}

	Node getCurrentSite() {
		return currentSite;
	}

	public void setMapControlCreator(MapControlCreator mapControlCreator) {
		this.mapControlCreator = mapControlCreator;
	}

}