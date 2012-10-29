package org.argeo.connect.demo.gr.ui.editors;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

import org.argeo.ArgeoException;
import org.argeo.connect.demo.gr.GrConstants;
import org.argeo.gis.ui.MapControlCreator;
import org.argeo.gis.ui.editors.MapFormPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.editor.FormEditor;


public class MapDisplayPage extends MapFormPage {
	private NetworkEditor networkEditor;

	public MapDisplayPage(FormEditor editor, String id, String title,
			Node context, MapControlCreator mapControlCreator,
			NetworkEditor networkEditor) {
		super(editor, id, title, context, mapControlCreator);
		this.networkEditor = networkEditor;
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		addLayer(GrConstants.NORMAL);
		addLayer(GrConstants.BASE);
		addLayer(GrConstants.NATIONAL);
	}

	protected void addLayer(String type) {
		Node network = networkEditor.getNetwork();
		try {
			QueryManager qm = network.getSession().getWorkspace()
					.getQueryManager();
			NodeIterator l = qm
					.createQuery(
							"select * from [gr:site] as site where ISDESCENDANTNODE(site,'"
									+ network.getPath()
									+ "') and [gr:siteType]='" + type + "'",
							Query.JCR_SQL2).execute().getNodes();
			getMapViewer().addLayer(type, l, type + ".gif");
		} catch (RepositoryException e) {
			throw new ArgeoException("Cannot list sites", e);
		}
	}

}
