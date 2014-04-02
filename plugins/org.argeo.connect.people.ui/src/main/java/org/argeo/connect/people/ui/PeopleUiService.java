package org.argeo.connect.people.ui;

import java.util.List;

import javax.jcr.Session;

/**
 * Centralize here the definition of context specific parameter (for instance
 * the name of the command to open editors so that it is easily extended by
 * specific extensions
 */
public interface PeopleUiService {
	/**
	 * Provide the plugin specific ID of the {@code OpenEntityEditor} command
	 * and thus enable the opening plugin specific editors
	 */
	public String getOpenEntityEditorCmdId();

	public String getOpenSearchEntityEditorCmdId();

	public String getOpenSearchByTagEditorCmdId();

	public String getOpenFileCmdId();

	public List<String> getDefinedFilteredTags(Session session, String filter);
}
