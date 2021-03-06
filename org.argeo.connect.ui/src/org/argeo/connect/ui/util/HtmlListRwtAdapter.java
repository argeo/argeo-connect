package org.argeo.connect.ui.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.ui.ConnectUiConstants;
import org.argeo.connect.ui.SystemWorkbenchService;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

/**
 * Listener to add to a tableViewer or a label that displays RWT link that
 * conform to the syntax commandId/param1=value1/param2=value2/.../paramN=valueN
 * 
 * It will call the corresponding command with the given parameters.
 */
public class HtmlListRwtAdapter extends SelectionAdapter {
	private static final long serialVersionUID = -3867410418907732579L;
	private final static Log log = LogFactory.getLog(HtmlListRwtAdapter.class);

	private final SystemWorkbenchService systemWorkbenchService;

	public HtmlListRwtAdapter(SystemWorkbenchService systemWorkbenchService) {
		this.systemWorkbenchService = systemWorkbenchService;
	}

	public void widgetSelected(SelectionEvent event) {
		if (event.detail == ConnectUiConstants.MARKUP_VIEWER_HYPERLINK) {
			String string = event.text;
			String[] token = string.split("/");
			if (token.length > 0) {
				Map<String, String> params = new HashMap<String, String>();
				for (int i = 1; i < token.length; i++) {
					String[] currParam = token[i].split("=");
					params.put(currParam[0], currParam[1]);
				}
				try {
					// CommandUtils.callCommand(token[0], params);
					systemWorkbenchService.callCommand(token[0], params);
				} catch (Exception nde) {
					log.warn("Error while trying to call " + "a command using a RWT.HYPERLINK.\n Retrieved href:"
							+ string + "\n" + nde.getMessage());
					nde.printStackTrace();
				}
			}
		}
	}
}
