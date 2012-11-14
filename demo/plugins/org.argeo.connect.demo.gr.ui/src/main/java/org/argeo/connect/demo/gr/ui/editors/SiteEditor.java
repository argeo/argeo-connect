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
			currentSite = getGrBackend().getCurrentSession()
					.getNodeByIdentifier(sei.getUid());
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
			getGrBackend().getCurrentSession().save();
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