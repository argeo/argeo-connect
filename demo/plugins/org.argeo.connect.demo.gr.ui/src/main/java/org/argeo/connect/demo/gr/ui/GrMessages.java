package org.argeo.connect.demo.gr.ui;

import org.argeo.eclipse.ui.specific.ThreadNLS;
import org.eclipse.osgi.util.NLS;

/** Localized messages */
public class GrMessages extends NLS {
	public final static ThreadNLS<GrMessages> nls = new ThreadNLS<GrMessages>(
			GrMessages.class);

	/* GENERIC LABELS */

	// Networks
	public String networkLbl;

	// Sites
	public String siteLbl;
	public String siteTypeLbl;

	// Point & measures
	public String longitudeLbl;
	public String latitudeLbl;
	public String waterLevelLbl;
	public String eColiRateLbl;
	public String withdrawnWaterLbl;

	// Documents
	public String docNameLbl;

	// Users
	public String userNameLbl;

	// Dates
	public String dateLbl;
	public String lastUpdatedLbl;

	// Miscellaneous
	public String commentTxtLbl;

	/* Views */

	// NetworkBrowser view
	public String networkBrowserView_title;

	// NetworkList view
	public String networkListView_title;
	public String networkListView_msg;

	/* Editors */
	// Generic
	public String editor_docSection_title;

	// Network editor
	public String networkEditor_detailPage_title;
	public String networkEditor_mapPage_title;
	public String networkEditor_mainSection_title;
	public String networkEditor_sitesSection_title;

	// Site editor
	public String siteEditor_title_pre;
	public String siteEditor_detailPage_title;
	public String siteEditor_mapPage_title;
	public String siteEditor_metadataSection_title_pre;
	public String siteEditor_lastUpdatedInfoSection_title;
	public String siteEditor_commentsSection_title;

	/* Commands, links and buttons */

	public String createNetwork_lbl;
	public String createSite_lbl;
	public String generateSiteReport_lbl;
	public String addComment_lbl;
	public String addDocument_lbl;

	public String browseButtonLbl;

	/* Dialogs and wizards */
	// Comments
	public String dialog_createComment_title;
	public String dialog_createComment_msg;

	// Network
	public String dialog_createNetwork_title;
	public String dialog_createNetwork_msg;

	// Site
	public String dialog_createSite_title;
	public String dialog_createSite_msg;

	// documents
	public String wizard_attachDoc_title;
	public String wizard_attachDoc_msg;
	public String wizard_attachDoc_lbl;

	/* Errors */
	public String forbiddenAction_title;
	public String forbiddenAction_msg;
	public String emptyFileCannotBeUploaded;
	public String existingFileError;

	public static GrMessages get() {
		return nls.get();
	}

}
