package org.argeo.connect.people.rap.commands;

import java.net.URL;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.rap.PeopleRapPlugin;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;

/**
 * Opens various pre-defined URLs on the internet in a new browser window/tab
 * (target ="_blank")
 */
public class OpenUrl extends AbstractHandler {

	public final static String ID = PeopleRapPlugin.PLUGIN_ID + ".openUrl";
	public final static String PARAM_URL_VALUE = "param.urlValue";
	public final static String PARAM_URL_TYPE = "param.urlType";

	public final static String PARAM_VALUE_TRACKER = "tracker";
	public final static String PARAM_VALUE_WIKI = "wiki";

	private final static String TRACKER_URL = "https://www.argeo.org/bugzilla/enter_bug.cgi?product=connect&component=people";
	private final static String WIKI_URL = "https://www.argeo.org/wiki/Connect_People";

	public Object execute(ExecutionEvent event) throws ExecutionException {
		String urlType = event.getParameter(PARAM_URL_TYPE);
		String urlValue = event.getParameter(PARAM_URL_VALUE);
		try {
			URL url = null;

			if (EclipseUiUtils.notEmpty(urlValue)) {
				url = new URL(urlValue);
			} else if (PARAM_VALUE_TRACKER.equals(urlType))
				url = new URL(TRACKER_URL);
			else if (PARAM_VALUE_WIKI.equals(urlType))
				url = new URL(WIKI_URL);
			else
				return null;
			PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser()
					.openURL(url);
		} catch (Exception e) {
			throw new PeopleException("Unable to open browser page", e);
		}
		return null;
	}
}
