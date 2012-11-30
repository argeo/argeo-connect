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

import java.util.ArrayList;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.connect.demo.gr.GrException;
import org.argeo.connect.demo.gr.GrNames;
import org.argeo.gis.ui.MapControlCreator;
import org.argeo.gis.ui.editors.MapFormPage;
import org.argeo.jcr.CollectionNodeIterator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.editor.FormEditor;

/** Display a map for a whole network */
public class SiteMapDisplayPage extends MapFormPage {
	private Node site;

	public SiteMapDisplayPage(FormEditor editor, String id, String title,
			Node context, MapControlCreator mapControlCreator) {
		super(editor, id, title, context, mapControlCreator);
		this.site = context;
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		try {
			String type = site.getProperty(GrNames.GR_SITE_TYPE).getString();
			ArrayList<Node> lst = new ArrayList<Node>();
			lst.add(site);
			getMapViewer().addLayer(type, new CollectionNodeIterator(lst),
					type + ".gif");
		} catch (RepositoryException e) {
			throw new GrException("Caqnnot display site", e);
		}
	}
}
