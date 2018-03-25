package org.argeo.connect.e4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;

import org.argeo.connect.ui.AppWorkbenchService;
import org.argeo.connect.ui.SystemWorkbenchService;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Image;

public class SystemE4Service implements SystemWorkbenchService, AppE4Service {

	// Injected known AppWorkbenchServices: order is important, first found
	// result will be returned by the various methods.
	private List<AppWorkbenchService> knownAppWbServices = Collections.synchronizedList(new ArrayList<>());
	private String defaultEditorId = null;// DefaultDashboardEditor.ID;

	@Override
	public void callCommand(String commandId, Map<String, String> parameters) {

	}

	//
	// APP SERVICE
	//

	@Override
	public String getDefaultEditorId() {
		String result = null;
		for (AppWorkbenchService appWbService : knownAppWbServices) {
			result = appWbService.getDefaultEditorId();
			if (EclipseUiUtils.notEmpty(result))
				return result;
		}
		return defaultEditorId;
	}

	@Override
	public String getEntityEditorId(Node entity) {
		String result = null;
		for (AppWorkbenchService appWbService : knownAppWbServices) {
			result = appWbService.getEntityEditorId(entity);
			if (EclipseUiUtils.notEmpty(result))
				return result;
		}
		return null;
	}

	@Override
	public void openEntityEditor(Node entity) {
		String result = null;
		for (AppWorkbenchService appWbService : knownAppWbServices) {
			// TODO make it more robust
			result = appWbService.getEntityEditorId(entity);
			if (EclipseUiUtils.notEmpty(result))
				appWbService.openEntityEditor(entity);
		}
	}

	@Override
	public void openSearchEntityView(String nodeType, String label) {
		String result = null;
		for (AppWorkbenchService appWbService : knownAppWbServices) {
			// TODO make it more robust
			result = appWbService.getSearchEntityEditorId(nodeType);
			if (EclipseUiUtils.notEmpty(result))
				appWbService.openSearchEntityView(nodeType, label);
		}
	}

	@Override
	public String getSearchEntityEditorId(String nodeType) {
		String result = null;
		for (AppWorkbenchService appWbService : knownAppWbServices) {
			result = appWbService.getSearchEntityEditorId(nodeType);
			if (EclipseUiUtils.notEmpty(result))
				return result;
		}
		return null;
	}

	@Override
	public Image getIconForType(Node entity) {
		Image result = null;
		for (AppWorkbenchService appWbService : knownAppWbServices) {
			result = appWbService.getIconForType(entity);
			if (result != null)
				return result;
		}
		return null;
	}

	@Override
	public Wizard getCreationWizard(Node node) {
		Wizard result = null;
		for (AppWorkbenchService appWbService : knownAppWbServices) {
			result = appWbService.getCreationWizard(node);
			if (result != null)
				return result;
		}
		return null;
	}

	public void addAppService(AppWorkbenchService appService, Map<String, String> properties) {
		knownAppWbServices.add(appService);
	}

	public void removeAppService(AppWorkbenchService appService, Map<String, String> properties) {
		knownAppWbServices.remove(appService);
	}

}
