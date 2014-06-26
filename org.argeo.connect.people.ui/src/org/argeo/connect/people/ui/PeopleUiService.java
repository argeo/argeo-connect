package org.argeo.connect.people.ui;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.eclipse.swt.graphics.Image;

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

	public List<String> getValueList(Session session, String basePath,
			String filter);

	public List<String> getValueList(Session session, String nodeType,
			String basePath, String filter);

	public List<String> getInstancePropCatalog(Session session,
			String resourcePath, String propertyName, String filter);

	/** Centralize icon management for a given app */
	public Image getIconForType(Node entity);
}