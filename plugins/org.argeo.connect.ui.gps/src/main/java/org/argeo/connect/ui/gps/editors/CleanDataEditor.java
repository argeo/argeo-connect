package org.argeo.connect.ui.gps.editors;

import java.util.List;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.argeo.connect.ConnectNames;
import org.argeo.connect.ConnectTypes;
import org.argeo.connect.gpx.TrackDao;
import org.argeo.connect.ui.gps.ConnectGpsLabels;
import org.argeo.connect.ui.gps.ConnectUiGpsPlugin;
import org.argeo.connect.ui.gps.views.GpsBrowserView;
import org.argeo.eclipse.ui.ErrorFeedback;
import org.argeo.geotools.styling.StylingUtils;
import org.argeo.gis.GisConstants;
import org.argeo.gis.ui.MapControlCreator;
import org.argeo.gis.ui.MapViewer;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;

/**
 * Main multi tab view to handle a session to clean GPS data.
 * 
 */
public class CleanDataEditor extends FormEditor implements ConnectTypes,
		ConnectNames, ConnectGpsLabels {
	public static final String ID = "org.argeo.connect.ui.gps.cleanDataEditor";

	private final static Log log = LogFactory.getLog(CleanDataEditor.class);

	// IoC
	private Session currentSession;
	private TrackDao trackDao;

	private MapControlCreator mapControlCreator;
	private List<String> baseLayers;

	public CleanDataEditor() {
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		if (!(input instanceof CleanDataEditorInput))
			throw new RuntimeException("Wrong input");
		setSite(site);
		setInput(input);

		this.setPartName(getSessionName());
	}

	// protected FormToolkit createToolkit(Display display) {
	// // Create a toolkit that shares colors between editors.
	// return new FormToolkit(ClientUiPlugin.getDefault().getFormColors(
	// display));
	// }

	protected void addPages() {
		try {
			addPage(new MetaDataPage(this,
					ConnectUiGpsPlugin.getGPSMessage(METADATA_PAGE_TITLE)));
			addPage(new DataSetPage(this,
					ConnectUiGpsPlugin.getGPSMessage(DATASET_PAGE_TITLE)));
			addPage(new DefineParamsAndReviewPage(this,
					ConnectUiGpsPlugin.getGPSMessage(PARAMSET_PAGE_TITLE),
					mapControlCreator));
		} catch (PartInitException e) {
			throw new ArgeoException("Not able to add page ", e);
		}
	}

	public void doSave(IProgressMonitor monitor) {
		try {

			log.debug("CleanDataEditor.doSave");

			// Automatically commit all pages of the editor
			commitPages(true);

			// it is thus useless.
			// findPage(DataSetPage.ID).doSave(monitor);
			// findPage(MetaDataPage.ID).doSave(monitor);
			// findPage(DefineParamsAndReviewPage.ID).doSave(monitor);

			// commit all changes in JCR
			currentSession.save();

			// clean status.
			firePropertyChange(PROP_DIRTY);

			// Refresh Editor & Jcr Tree.
			// useful when the name has changed.
			this.setPartName(getSessionName());
			firePropertyChange(PROP_TITLE);

			GpsBrowserView gbView = (GpsBrowserView) ConnectUiGpsPlugin
					.getDefault().getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().findView(GpsBrowserView.ID);
			gbView.refresh(getCurrentSessionNode());

		} catch (Exception e) {
			e.printStackTrace();
			ErrorFeedback.show("Cannot save session "
					+ getEditorInput().getUuid(), e);
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
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * UTILITIES
	 */
	public Node getCurrentSessionNode() {
		Node curNode;
		try {
			curNode = currentSession.getNodeByIdentifier(getEditorInput()
					.getUuid());
		} catch (ItemNotFoundException e) {
			throw new ArgeoException("Node of uuid "
					+ getEditorInput().getUuid() + " has not been found.", e);
		} catch (RepositoryException e) {
			throw new ArgeoException("Node of uuid "
					+ getEditorInput().getUuid() + " has not been found.", e);
		}
		return curNode;
	}

	public void addBaseLayers(MapViewer mapViewer) {
		for (String alias : baseLayers) {
			String layerPath = (GisConstants.DATA_STORES_BASE_PATH + alias)
					.trim();
			try {
				Node layerNode = currentSession.getNode(layerPath);
				mapViewer.addLayer(layerNode,
						StylingUtils.createLineStyle("LIGHT_GRAY", 1));
			} catch (RepositoryException e) {
				log.warn("Cannot retrieve " + alias + ": " + e);
			}
		}
	}

	private String getSessionName() {
		String name = "(New Session)";
		try {
			String tmp = getCurrentSessionNode().getProperty(CONNECT_NAME)
					.getString();
			if (!"".equals(tmp))
				name = tmp;
		} catch (PathNotFoundException pnfe) {
			// Silent : property has not been initialized yet.
		} catch (RepositoryException re) {
			throw new ArgeoException("Error while getting session name", re);
		}
		return name;
	}

	@Override
	public CleanDataEditorInput getEditorInput() {
		return (CleanDataEditorInput) super.getEditorInput();
	}

	/**
	 * returns the default sensor name or null if none or an empty string has
	 * been entered
	 */
	public String getDefaultSensoreName() {
		return ((MetaDataPage) findPage(MetaDataPage.ID))
				.getDefaultSensoreName();
	}

	/** Returns injected track DAO */
	public TrackDao getTrackDao() {
		return trackDao;
	}

	// IoC
	public void setCurrentSession(Session currentSession) {
		this.currentSession = currentSession;
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
