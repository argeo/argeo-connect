package org.argeo.connect.workbench;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;

import org.argeo.cms.ui.workbench.util.CommandUtils;
import org.argeo.connect.ui.AppWorkbenchService;
import org.argeo.connect.ui.ConnectEditor;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.workbench.commands.OpenSearchEntityEditor;

public interface LegacyWorkbenchService extends AppWorkbenchService {

	@Override
	default void openEntityEditor(Node entity) {
		CommandUtils.callCommand(getOpenEntityEditorCmdId(), ConnectEditor.PARAM_JCR_ID,
				ConnectJcrUtils.getIdentifier(entity));
	}

	@Override
	default void openSearchEntityView(String nodeType, String label) {
		Map<String, String> params = new HashMap<String, String>();
		params.put(OpenSearchEntityEditor.PARAM_NODE_TYPE, nodeType);
		params.put(OpenSearchEntityEditor.PARAM_EDITOR_NAME, label);
		String cmdId = getOpenSearchEntityEditorCmdId();
		CommandUtils.callCommand(cmdId, params);
	}

}
