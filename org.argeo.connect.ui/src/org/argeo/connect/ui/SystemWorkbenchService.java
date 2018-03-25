package org.argeo.connect.ui;

import java.util.Map;

/** Provide assembly of the various {@code AppWorkbenchService} of a system */
public interface SystemWorkbenchService extends AppWorkbenchService {
	public void callCommand(String commandId, Map<String, String> parameters);
}
