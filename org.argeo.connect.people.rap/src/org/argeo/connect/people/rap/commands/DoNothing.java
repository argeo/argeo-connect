package org.argeo.connect.people.ui.commands;

import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

/**
 * Utilitary command used to enable sub menus in various toolbars. Does nothing
 */
public class DoNothing extends AbstractHandler {
	public final static String ID = PeopleUiPlugin.PLUGIN_ID + ".doNothing";

	public Object execute(ExecutionEvent event) throws ExecutionException {
		return null;
	}
}
