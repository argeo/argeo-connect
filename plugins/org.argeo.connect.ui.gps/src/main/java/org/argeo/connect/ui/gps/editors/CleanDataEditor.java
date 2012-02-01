package org.argeo.connect.ui.gps.editors;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.ArgeoException;
import org.argeo.connect.ConnectNames;
import org.argeo.connect.ConnectTypes;
import org.argeo.connect.ui.gps.ConnectGpsLabels;
import org.argeo.connect.ui.gps.ConnectGpsUiPlugin;
import org.argeo.connect.ui.gps.GpsUiGisServices;
import org.argeo.connect.ui.gps.GpsUiJcrServices;
import org.argeo.connect.ui.gps.providers.GpsNodeLabelProvider;
import org.argeo.connect.ui.gps.views.GpsBrowserView;
import org.argeo.eclipse.ui.ErrorFeedback;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;

/**
 * Main multitab editor to handle a session to clean GPS data.
 * 
 */
public class CleanDataEditor extends FormEditor implements ConnectTypes,
		ConnectNames, ConnectGpsLabels {
	// private final static Log log = LogFactory.getLog(CleanDataEditor.class);

	public static final String ID = "org.argeo.connect.ui.gps.cleanDataEditor";

	/* DEPENDENCY INJECTION */
	private GpsUiJcrServices uiJcrServices;
	private GpsUiGisServices uiGisServices;

	// The session we are currently editing
	private Node currCleanSession;

	@Override
	public CleanDataEditorInput getEditorInput() {
		return (CleanDataEditorInput) super.getEditorInput();
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		if (!(input instanceof CleanDataEditorInput))
			throw new RuntimeException("Wrong input");
		setSite(site);
		setInput(input);
		try {
			currCleanSession = uiJcrServices.getJcrSession()
					.getNodeByIdentifier(getEditorInput().getUuid());
		} catch (RepositoryException e) {
			throw new ArgeoException("unable to retrieve current session", e);
		}
		this.setPartName(uiJcrServices
				.getCleanSessionDisplayName(currCleanSession));
	}

	protected void addPages() {
		try {
			addPage(new CleanSessionInfoPage(this,
					ConnectGpsUiPlugin.getGPSMessage(METADATA_PAGE_TITLE)));
			addPage(new GpxFilesProcessingPage(this,
					ConnectGpsUiPlugin.getGPSMessage(DATASET_PAGE_TITLE)));
			addPage(new DefineParamsAndReviewPage(this,
					ConnectGpsUiPlugin.getGPSMessage(PARAMSET_PAGE_TITLE)));
		} catch (PartInitException e) {
			throw new ArgeoException("Not able to add page ", e);
		}
	}

	public void doSave(IProgressMonitor monitor) {
		try {
			// Automatically commit all pages of the editor
			commitPages(true);
			// commit all changes in JCR
			uiJcrServices.getJcrSession().save();
			// clean status.
			firePropertyChange(PROP_DIRTY);
			// Refresh Editor & Jcr Tree.
			// useful when the name has changed.
			this.setPartName(uiJcrServices
					.getCleanSessionDisplayName(currCleanSession));
			firePropertyChange(PROP_TITLE);
			GpsBrowserView gbView = (GpsBrowserView) ConnectGpsUiPlugin
					.getDefault().getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().findView(GpsBrowserView.ID);
			gbView.refresh(currCleanSession.getParent());
		} catch (Exception e) {
			e.printStackTrace();
			ErrorFeedback.show("Cannot save session "
					+ getEditorInput().getUuid(), e);
		}
	}

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

	// change enable state of all children control to readOnly
	public void refreshReadOnlyState() {
		if (uiJcrServices.isSessionComplete(currCleanSession)) {
			for (int i = 0; i < getPageCount(); i++) {
				Control curPage = getControl(i);
				if (curPage != null)
					curPage.setEnabled(false);
			}
			this.setTitleImage(GpsNodeLabelProvider.sessionDone);
			firePropertyChange(PROP_TITLE);
		} else {
			for (int i = 0; i < getPageCount(); i++) {
				Control curPage = getControl(i);
				if (curPage != null)
					curPage.setEnabled(true);
			}
			this.setTitleImage(GpsNodeLabelProvider.session);
			firePropertyChange(PROP_TITLE);
		}
	}

	/** exposes the current GPS Clean session to its FormPart */
	protected Node getCurrentCleanSession() {
		return currCleanSession;
	}

	/** exposes injected uiJcrServices to its FormPart */
	protected GpsUiJcrServices getUiJcrServices() {
		return uiJcrServices;
	}

	/** exposes injected uiGisServices to its FormPart */
	protected GpsUiGisServices getUiGisServices() {
		return uiGisServices;
	}

	/* DEPENDENCY INJECTION */
	public void setUiGisServices(GpsUiGisServices uiGisServices) {
		this.uiGisServices = uiGisServices;
		this.uiJcrServices = uiGisServices.getUiJcrServices();
	}

}