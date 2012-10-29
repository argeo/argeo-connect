package org.argeo.connect.demo.gr.ui.editors;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.ArgeoException;
import org.argeo.connect.demo.gr.GrConstants;
import org.argeo.connect.demo.gr.GrNames;
import org.argeo.connect.demo.gr.ui.GrUiPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

/**
 * Main multi tab editor to display and modify a site.
 */
public class SiteEditor extends AbstractGrEditor implements GrNames {

	public final static String ID = GrUiPlugin.PLUGIN_ID + ".siteEditor";

	private Node network;
	private Node currentSite;

	// for internationalized messages
	private final static String MSG_PRE = "grSiteEditor";

	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);

		SiteEditorInput sei = (SiteEditorInput) getEditorInput();
		try {
			currentSite = getGrBackend().getCurrentSession()
					.getNodeByIdentifier(sei.getUid());
			network = currentSite.getParent();
			this.setPartName(GrUiPlugin.getMessage("siteLbl") + " "
					+ currentSite.getName());
			String siteType = currentSite.getProperty(GR_SITE_TYPE).getString();
			if (GrConstants.NATIONAL.equals(siteType))
				setTitleImage(NetworkDetailsPage.ICON_NATIONAL_TYPE);
			else if (GrConstants.BASE.equals(siteType))
				setTitleImage(NetworkDetailsPage.ICON_BASE_TYPE);
			else if (GrConstants.NORMAL.equals(siteType))
				setTitleImage(NetworkDetailsPage.ICON_NORMAL_TYPE);
		} catch (RepositoryException e) {
			throw new ArgeoException(
					"Error while initialising SiteEditor with JCR information.",
					e);
		}
	}

	@Override
	protected void addPages() {
		try {
			SiteDetailsPage siteDetailsPage = new SiteDetailsPage(this,
					GrUiPlugin.getMessage(MSG_PRE + "DetailPageTitle"));
			addPage(siteDetailsPage);

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
}
