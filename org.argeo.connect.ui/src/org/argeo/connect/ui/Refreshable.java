package org.argeo.connect.ui;

/**
 * Implement this interface to enable being called by the generic Connect
 * Workbench ForceRefresh command.
 * 
 * This command is a workaround to manually trigger the refreshment of a part
 * when automatic refreshment does not work.
 */
public interface Refreshable {

	public void forceRefresh(Object object);
}
