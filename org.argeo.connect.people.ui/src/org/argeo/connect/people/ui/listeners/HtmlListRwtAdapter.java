package org.argeo.connect.people.ui.listeners;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.eclipse.ui.utils.CommandUtils;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class HtmlListRwtAdapter extends SelectionAdapter {
	private static final long serialVersionUID = -3867410418907732579L;
	private final static Log log = LogFactory.getLog(HtmlListRwtAdapter.class);

	public void widgetSelected(SelectionEvent event) {
		if (event.detail == RWT.HYPERLINK) {
			String string = event.text;
			String[] token = string.split("/");
			if (token.length > 0) {
				Map<String, String> params = new HashMap<String, String>();
				for (int i = 1; i < token.length; i++) {
					String[] currParam = token[i].split("=");
					params.put(currParam[0], currParam[1]);
				}
				try {
					CommandUtils.callCommand(token[0], params);
				} catch (Exception nde) {
					log.warn("Error while trying to call "
							+ "a command using a RWT.HYPERLINK.\n Retrieved href:"
							+ string + "\n" + nde.getCause().getMessage());
					nde.printStackTrace();
				}
			}
		}
	}
}