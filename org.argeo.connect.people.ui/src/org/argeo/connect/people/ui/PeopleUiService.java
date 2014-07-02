package org.argeo.connect.people.ui;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.eclipse.swt.graphics.Image;

/**
 * Centralizes the definition of context specific parameters (Among other, the
 * name of the command to open editors or default editor
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

	public String getDefaultEditorId();

	public List<String> getValueList(Session session, String basePath,
			String filter);

	public List<String> getValueList(Session session, String nodeType,
			String basePath, String filter);

	public List<String> getInstancePropCatalog(Session session,
			String resourcePath, String propertyName, String filter);

	/** Centralize icon management for a given app */
	public Image getIconForType(Node entity);
}
