package org.argeo.connect.ui.gps.editors;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.argeo.connect.ui.gps.ConnectGpsUiPlugin;
import org.argeo.connect.ui.gps.GpsUiGisServices;
import org.argeo.connect.ui.gps.GpsUiJcrServices;
import org.argeo.connect.ui.gps.views.GpsBrowserView;
import org.argeo.eclipse.ui.ErrorFeedback;
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
	private GpsUiJcrServices uiJcrServices;
	private GpsUiGisServices uiGisServices;

	// Business objects
	private Node currentLocalRepo;
	private LocalRepoViewerPage viewerPage;

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		if (!(input instanceof LocalRepoEditorInput))
			throw new RuntimeException("Wrong type input");
		setSite(site);
		setInput(input);

		Node parentNode = uiJcrServices.getLocalRepositoriesParentNode();
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
			viewerPage = new LocalRepoViewerPage(this, "Viewer");
			addPage(viewerPage);
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
			currentLocalRepo.getSession().save();

			// clean status.
			firePropertyChange(PROP_DIRTY);

			// Refresh Editor & Jcr Tree.
			// useful when the name has changed.
			this.setPartName(currentLocalRepo.getProperty(Property.JCR_TITLE)
					.getString());
			firePropertyChange(PROP_TITLE);

			GpsBrowserView gbView = (GpsBrowserView) ConnectGpsUiPlugin
					.getDefault().getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().findView(GpsBrowserView.ID);
			gbView.refresh(currentLocalRepo);
		} catch (Exception e) {
			e.printStackTrace();
			ErrorFeedback.show("Cannot save session "
					+ getEditorInput().getName(), e);
		}
	}

	public void setFocus() {
	}

	public void refresh() {
		viewerPage.refresh();
	}

	@Override
	public void doSaveAs() {
		// save as is not allowed
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	protected Node getCurrentRepoNode() {
		return currentLocalRepo;
	}

	/*
	 * Centralize management of business objects for both the editor and its
	 * children pages
	 */

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