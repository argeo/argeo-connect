package org.argeo.connect.ui.gps.editors;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.argeo.connect.gpx.TrackDao;
import org.argeo.connect.ui.gps.ConnectUiGpsPlugin;
import org.argeo.connect.ui.gps.views.GpsBrowserView;
import org.argeo.eclipse.ui.ErrorFeedback;
import org.argeo.geotools.styling.StylingUtils;
import org.argeo.gis.GisConstants;
import org.argeo.gis.ui.MapControlCreator;
import org.argeo.gis.ui.MapViewer;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;

/**
 * Multitab editor to review clean data and manage the corresponding repository.
 * 
 */

public class LocalRepoEditor extends FormEditor {
	private final static Log log = LogFactory.getLog(LocalRepoEditor.class);

	public static final String ID = "org.argeo.connect.ui.gps.localRepoEditor";

	/* DEPENDENCY INJECTION */
	private Session jcrSession;
	private TrackDao trackDao;
	private MapControlCreator mapControlCreator;
	private List<String> baseLayers;

	// Business objects
	private Node currentLocalRepo;

	public LocalRepoEditor() {
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		if (!(input instanceof LocalRepoEditorInput))
			throw new RuntimeException("Wrong type input");
		setSite(site);
		setInput(input);

		String username = jcrSession.getUserID();
		Node userHomeDirectory = JcrUtils.createUserHomeIfNeeded(jcrSession,
				username);
		Node parentNode = trackDao
				.getLocalRepositoriesParentNode(userHomeDirectory);
		try {
			currentLocalRepo = parentNode.getNode(input.getName());
			this.setPartName(currentLocalRepo.getProperty(Property.JCR_TITLE)
					.getString());
		} catch (RepositoryException re) {
			throw new ArgeoException(
					"unexpected error while getting local repository node");
		}
	}

	protected void addPages() {
		try {
			addPage(new LocalRepoViewerPage(this, "Viewer", mapControlCreator));
			addPage(new LocalRepoMetaDataPage(this, "Meta infos"));
		} catch (PartInitException e) {
			throw new ArgeoException("Not able to add page ", e);
		}
	}

	public void doSave(IProgressMonitor monitor) {
		try {
			// Automatically commit all pages of the editor
			commitPages(true);

			// commit all changes in JCR
			jcrSession.save();

			// clean status.
			firePropertyChange(PROP_DIRTY);

			// Refresh Editor & Jcr Tree.
			// useful when the name has changed.
			this.setPartName(currentLocalRepo.getProperty(Property.JCR_TITLE)
					.getString());
			firePropertyChange(PROP_TITLE);

			GpsBrowserView gbView = (GpsBrowserView) ConnectUiGpsPlugin
					.getDefault().getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().findView(GpsBrowserView.ID);
			gbView.refresh(currentLocalRepo);
		} catch (Exception e) {
			e.printStackTrace();
			ErrorFeedback.show("Cannot save session "
					+ getEditorInput().getName(), e);
		}
	}

	/**
	 * CAUTION : We assume that the setFocus is called after everything has been
	 * correctly initialized.
	 */
	public void setFocus() {
	}

	@Override
	public void doSaveAs() {
		// not implemented, save as is not allowed
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	public void addBaseLayers(MapViewer mapViewer) {
		for (String alias : baseLayers) {
			String layerPath = (GisConstants.DATA_STORES_BASE_PATH + alias)
					.trim();
			try {
				Node layerNode = jcrSession.getNode(layerPath);
				mapViewer.addLayer(layerNode,
						StylingUtils.createLineStyle("LIGHT_GRAY", 1));
			} catch (RepositoryException e) {
				log.warn("Cannot retrieve " + alias + ": " + e);
			}
		}
	}

	protected Node getCurrentRepoNode() {
		return currentLocalRepo;
	}

	/** Returns injected track DAO */
	public TrackDao getTrackDao() {
		return trackDao;
	}

	/* DEPENDENCY INJECTION */
	public void setJcrSession(Session jcrSession) {
		this.jcrSession = jcrSession;
	}

	public void setTrackDao(TrackDao trackDao) {
		this.trackDao = trackDao;
	}

	public void setMapControlCreator(MapControlCreator mapControlCreator) {
		this.mapControlCreator = mapControlCreator;
	}

	public void setBaseLayers(List<String> baseLayers) {
		this.baseLayers = baseLayers;
	}
}