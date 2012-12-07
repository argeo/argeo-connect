package org.argeo.connect.demo.gr.ui.exports;

import java.util.List;

import javax.jcr.Node;

import org.argeo.connect.demo.gr.ui.providers.IJcrPropertyLabelProvider;

/**
 * Views and editors can implement this interface so that a call to
 * getCalcExtract command can generate an Openoffice Calc or MS Excel extract
 * corresponding to the displayed node table
 * 
 * Note that headerList and headerLblList lists must be 2 ordered list of the
 * same size where item(i) of the headerLblList is the label for the Jcr
 * property referenced at index i of the headerList
 * 
 */
public interface ICalcExtractProvider {
	/**
	 * Returns the list to display in the spread sheet
	 * 
	 * @param extractId
	 *            enable generation of various extract for the same
	 *            IWorkbenchPart. If null a default value mut be returned
	 */
	public List<Node> getNodeList(String extractId);

	/**
	 * Returns the list of properties to populate each column
	 * 
	 * @param extractId
	 *            enable generation of various extract for the same
	 *            IWorkbenchPart. If null a default value mut be returned
	 */
	public List<String> getHeaderList(String extractId);

	/**
	 * Returns the list of property label for column headers
	 * 
	 * @param extractId
	 *            enable generation of various extract for the same
	 *            IWorkbenchPart. If null a default value mut be returned
	 */
	public List<String> getHeaderLblList(String extractId);

	/**
	 * The Wrapper utility to insure we can have a well formatted string
	 * corresponding to the given Node and current context only with the
	 * property name.
	 * 
	 * @param extractId
	 *            enable generation of various extract for the same
	 *            IWorkbenchPart. If null a default value mut be returned
	 */
	public IJcrPropertyLabelProvider getLabelProvider(String extractId);
}
