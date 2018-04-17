package org.argeo.connect.e4;

import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.cms.CmsException;
import org.argeo.connect.ConnectException;
import org.argeo.connect.ServiceRanking;
import org.argeo.connect.ui.AppWorkbenchService;
import org.argeo.connect.ui.SystemWorkbenchService;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Image;

public class SystemE4Service extends ContextFunction implements SystemWorkbenchService, AppE4Service {
	@Inject
	EPartService partService;

	// Injected known AppWorkbenchServices: order is important, first found
	// result will be returned by the various methods.
	private SortedMap<ServiceRanking, AppWorkbenchService> knownAppWbServices = Collections
			.synchronizedSortedMap(new TreeMap<>());
	private String defaultEditorId = null;// DefaultDashboardEditor.ID;

	@Override
	public void callCommand(String commandId, Map<String, String> parameters) {

	}

	@Override
	public void openEntityEditor(Node entity) {
		String entityEditorId = getEntityEditorId(entity);
		MPart part = partService.createPart(entityEditorId);
		if(part==null)
			throw new CmsException("No entity editor found for id "+entityEditorId);
		try {
			part.setLabel(entity.getName());
			// part.getPersistedState().put("nodeWorkspace",
			// entity.getSession().getWorkspace().getName());
			// part.getPersistedState().put("nodePath", entity.getPath());
			part.getPersistedState().put(ConnectE4Constants.ENTITY_ID, entity.getIdentifier());
		} catch (RepositoryException e) {
			throw new ConnectException("Cannot open " + entity, e);
		}

		// the provided part is be shown
		partService.showPart(part, PartState.ACTIVATE);
	}

	//
	// APP SERVICE
	//

	@Override
	public Object compute(IEclipseContext context, String contextKey) {
		partService = context.get(EPartService.class);
		MApplication app = context.get(MApplication.class);
		IEclipseContext appCtx = app.getContext();
		appCtx.set(SystemWorkbenchService.class, this);
		return this;
	}

	@Override
	public String getDefaultEditorId() {
		String result = null;
		for (AppWorkbenchService appWbService : knownAppWbServices.values()) {
			result = appWbService.getDefaultEditorId();
			if (EclipseUiUtils.notEmpty(result))
				return result;
		}
		return defaultEditorId;
	}

	@Override
	public String getEntityEditorId(Node entity) {
		String result = null;
		for (AppWorkbenchService appWbService : knownAppWbServices.values()) {
			result = appWbService.getEntityEditorId(entity);
			if (EclipseUiUtils.notEmpty(result))
				return result;
		}
		return null;
	}

	// @Override
	// public void openEntityEditor(Node entity) {
	// String result = null;
	// for (AppWorkbenchService appWbService : knownAppWbServices) {
	// // TODO make it more robust
	// result = appWbService.getEntityEditorId(entity);
	// if (EclipseUiUtils.notEmpty(result))
	// appWbService.openEntityEditor(entity);
	// }
	// }

	@Override
	public void openSearchEntityView(String nodeType, String label) {
		String result = null;
		for (AppWorkbenchService appWbService : knownAppWbServices.values()) {
			// TODO make it more robust
			result = appWbService.getSearchEntityEditorId(nodeType);
			if (EclipseUiUtils.notEmpty(result))
				appWbService.openSearchEntityView(nodeType, label);
		}
	}

	@Override
	public String getSearchEntityEditorId(String nodeType) {
		String result = null;
		for (AppWorkbenchService appWbService : knownAppWbServices.values()) {
			result = appWbService.getSearchEntityEditorId(nodeType);
			if (EclipseUiUtils.notEmpty(result))
				return result;
		}
		return null;
	}

	@Override
	public Image getIconForType(Node entity) {
		Image result = null;
		for (AppWorkbenchService appWbService : knownAppWbServices.values()) {
			result = appWbService.getIconForType(entity);
			if (result != null)
				return result;
		}
		return null;
	}

	@Override
	public Wizard getCreationWizard(Node node) {
		Wizard result = null;
		for (AppWorkbenchService appWbService : knownAppWbServices.values()) {
			result = appWbService.getCreationWizard(node);
			if (result != null)
				return result;
		}
		return null;
	}

	public void addAppService(AppWorkbenchService appService, Map<String, Object> properties) {
		// String serviceRankingStr = properties.get(Constants.SERVICE_RANKING);
		// int serviceRanking = serviceRankingStr == null ? 0 :
		// Integer.parseInt(serviceRankingStr);
		// Integer serviceRanking = (Integer) properties.get(Constants.SERVICE_RANKING);
		// System.out.println(serviceRanking + " | " + properties);
		knownAppWbServices.put(new ServiceRanking(properties), appService);
	}

	public void removeAppService(AppWorkbenchService appService, Map<String, Object> properties) {
		knownAppWbServices.remove(new ServiceRanking(properties));
	}

}