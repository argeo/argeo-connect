/*
 * Argeo Connect - Data management and communications
 * Copyright (C) 2012 Argeo GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or combining it
 * with software covered by the terms of the Eclipse Public License, the
 * licensors of this Program grant you additional permission to convey the
 * resulting work. Corresponding Source for a non-source form of such a
 * combination shall include the source code for the parts of such software
 * which are used as well as that of the covered work.
 */
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
	public String networksLbl;

	// Sites
	public String siteLbl;
	public String siteTypeLbl;

	// Point & measures
	public String longitudeLbl;
	public String latitudeLbl;
	public String waterLevelLbl;
	public String eColiRateLbl;
	public String withdrawnWaterLbl;
	public String waterLevelShortLbl;
	public String eColiRateShortLbl;
	public String withdrawnWaterShortLbl;

	// Documents
	public String docNameLbl;

	// Users
	public String userNameLbl;

	// Dates
	public String dateLbl;
	public String lastUpdatedLbl;

	// Miscellaneous
	public String commentTxtLbl;
	public String name_lbl;

	/* Views */

	// NetworkBrowser view
	public String networkBrowserView_title;

	// SiteList view
	public String siteListView_displayFilterLbl;
	public String siteListView_siteTypeCmb;
	public String siteListView_maxWaterLevelLbl;
	public String siteListView_maxWithdrawnWaterLbl;
	public String siteListView_minEColiRateLbl;

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

	// imports 
	public String wizard_importInstances_title;
	
	/* Errors */
	public String forbiddenAction_title;
	public String forbiddenAction_msg;
	public String emptyFileCannotBeUploaded;
	public String existingFileError;

	public static GrMessages get() {
		return nls.get();
	}

}
