package org.argeo.connect.demo.gr.ui.editors;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.argeo.ArgeoException;
import org.argeo.connect.demo.gr.ui.GrMessages;
import org.argeo.connect.demo.gr.ui.GrUiPlugin;
import org.argeo.gis.ui.MapControlCreator;
import org.argeo.gis.ui.editors.MapFormPage;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

/**
 * Main multitab editor that does the following :
 * <ul>
 * <li>display a network metadata</li>
 * <li>add some documents and sites</li>
 * <li>display a clickable map with corresponding sites</li>
 * </ul>
 */
public class NetworkEditor extends AbstractGrEditor {

	public final static String ID = GrUiPlugin.PLUGIN_ID + ".networkEditor";

	private Node network;

	private MapControlCreator mapControlCreator;

	private MapFormPage mapFormPage;

	// for internationalized messages
	// private final static String MSG_PRE = "grNetworkEditor";

	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);

		NetworkEditorInput nei = (NetworkEditorInput) getEditorInput();
		try {
			network = getSession().getNodeByIdentifier(nei.getUid());
			this.setPartName(network.getProperty(Property.JCR_TITLE)
					.getString());
		} catch (RepositoryException e) {
			throw new ArgeoException("Cannot load network.", e);
		}
	}

	@Override
	protected void addPages() {
		try {

			NetworkDetailsPage networkDetailsPage = new NetworkDetailsPage(
					this, GrMessages.get().networkEditor_detailPage_title);
			addPage(networkDetailsPage);
			// addPage(new MapDisplayPage(this,
			// ClientUiPlugin.getMessage(MSG_PRE
			// + "MapPageTitle")));

			mapFormPage = new MapDisplayPage(this, "map",
					GrMessages.get().networkEditor_mapPage_title, network,
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
			network.getSession().save();
			firePropertyChange(PROP_DIRTY);
		} catch (Exception e) {
			throw new ArgeoException("Error while saving network: " + network,
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

	public void setMapControlCreator(MapControlCreator mapControlCreator) {
		this.mapControlCreator = mapControlCreator;
	}
}
