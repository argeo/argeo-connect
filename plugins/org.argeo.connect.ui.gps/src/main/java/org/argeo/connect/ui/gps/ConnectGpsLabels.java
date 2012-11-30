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
package org.argeo.connect.ui.gps;

public interface ConnectGpsLabels {
	public final static String CONNECT_ = "connect:";

	/** GPS Browser View **/
	public final static String GPS_BROWSER_VIEW = "gpsBrowserView:";

	// labels
	public final static String SESSION_REPOSITORY_LBL = CONNECT_
			+ GPS_BROWSER_VIEW + "sessionRepositoryLbl";

	/** Clean Data Editor **/
	public final static String CLEAN_DATA_EDITOR = "cleanDataEditor:";

	// Metadata page
	public final static String METADATA_SECTION_TITLE = CONNECT_
			+ CLEAN_DATA_EDITOR + "metadataSectionTitle";
	public final static String METADATA_PAGE_TITLE = CONNECT_
			+ CLEAN_DATA_EDITOR + "metadataPageTitle";
	public final static String PARAM_SET_LABEL_LBL = CONNECT_
			+ CLEAN_DATA_EDITOR + "paramSetLabelLbl";
	public final static String PARAM_SET_COMMENTS_LBL = CONNECT_
			+ CLEAN_DATA_EDITOR + "paramSetCommentsLbl";
	public final static String DEFAULT_SENSOR_NAME_LBL = CONNECT_
			+ CLEAN_DATA_EDITOR + "defaultSensorNameLbl";

	public final static String METADATA_PARAM_TABLE_TITLE = CONNECT_
			+ CLEAN_DATA_EDITOR + "paramTableTitle";

	// DataSet page
	public final static String DATASET_PAGE_TITLE = CONNECT_
			+ CLEAN_DATA_EDITOR + "dataSetPageTitle";
	public final static String IMPORT_FILE_SECTION_TITLE = CONNECT_
			+ CLEAN_DATA_EDITOR + "importFileSectionTitle";
	public final static String LAUNCH_IMPORT_BUTTON_LBL = CONNECT_
			+ CLEAN_DATA_EDITOR + "launchImportButtonLbl";

	// ParamSet page
	public final static String PARAMSET_PAGE_TITLE = CONNECT_
			+ CLEAN_DATA_EDITOR + "paramsSetPageTitle";
	public final static String PARAMS_SECTION_TITLE = CONNECT_
			+ CLEAN_DATA_EDITOR + "paramSectionTitle";
	public final static String PARAMS_SECTION_DESC = CONNECT_
			+ CLEAN_DATA_EDITOR + "paramSectionDescription";
	public final static String VISUALIZE_BUTTON_LBL = CONNECT_
			+ CLEAN_DATA_EDITOR + "visualizeButtonLbl";
	public final static String LAUNCH_CLEAN_BUTTON_LBL = CONNECT_
			+ CLEAN_DATA_EDITOR + "launchProcessButtonLbl";
}
