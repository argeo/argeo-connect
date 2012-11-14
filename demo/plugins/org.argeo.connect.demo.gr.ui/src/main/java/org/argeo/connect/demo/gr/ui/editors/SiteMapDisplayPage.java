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
