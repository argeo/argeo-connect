package org.argeo.connect.people.ui.utils;

/**
 * WorkbenchPart should implement this interface to enable refresh command to
 * refresh them
 */
public interface Refreshable {

	public void forceRefresh(Object object);

}
