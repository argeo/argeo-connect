package org.argeo.connect.e4;

import java.util.Map;
import java.util.SortedMap;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.cms.CmsException;
import org.argeo.connect.ConnectException;
import org.argeo.connect.ServiceRanking;
import org.argeo.connect.ui.AppWorkbenchService;
import org.argeo.connect.ui.SystemWorkbenchService;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Image;

@SuppressWarnings("restriction")
public class SystemE4Service implements SystemWorkbenchService, AppE4Service {
	IEclipseContext eclipseContext;
	// EPartService partService;
	// ECommandService commandService;
	// EHandlerService handlerService;

	private String defaultEditorId = null;// DefaultDashboardEditor.ID;
	private SortedMap<ServiceRanking, AppWorkbenchService> knownAppWbServices;

	// public SystemE4Service(SortedMap<ServiceRanking, AppWorkbenchService>
	// knownAppWbServices) {
	// super();
	// this.knownAppWbServices = knownAppWbServices;
	// }

	public SystemE4Service(SortedMap<ServiceRanking, AppWorkbenchService> knownAppWbServices,
			IEclipseContext eclipseContext) {
		super();
		this.knownAppWbServices = knownAppWbServices;
		this.eclipseContext = eclipseContext;
		// this.partService = partService;
		// this.commandService = commandService;
		// this.handlerService = handlerService;
	}

	@Override
	public void callCommand(String commandId, Map<String, String> parameters) {
		final Command command = eclipseContext.get(ECommandService.class).getCommand(commandId);
		final ParameterizedCommand pcmd = ParameterizedCommand.generateCommand(command, parameters);
		if (pcmd == null)
			throw new ConnectException("No command found for id " + commandId + " and parameters " + parameters);
		eclipseContext.get(EHandlerService.class).executeHandler(pcmd);
	}

	@Override
	public String getOpenEntityEditorCmdId() {
		return "org.argeo.suite.e4.command.openEntity";
	}

	@Override
	public void openEntityEditor(Node entity) {
		EPartService partService = eclipseContext.get(EPartService.class);
		try {
			String entityId = entity.getIdentifier();
			String entityEditorId = getEntityEditorId(entity);
			parts: for (MPart part : partService.getParts()) {
				String elementId = part.getElementId();
				if (!elementId.equals(entityEditorId))
					continue parts;
				String id = part.getPersistedState().get(ConnectE4Constants.ENTITY_ID);
				if (id != null && entityId.equals(id)) {
					partService.showPart(part, PartState.ACTIVATE);
					return;
				}
			}

			// new part
			MPart part = partService.createPart(entityEditorId);
			if (part == null)
				throw new CmsException("No entity editor found for id " + entityEditorId);
			part.setLabel(entity.getName());
			// part.getPersistedState().put("nodeWorkspace",
			// entity.getSession().getWorkspace().getName());
			// part.getPersistedState().put("nodePath", entity.getPath());
			part.getPersistedState().put(ConnectE4Constants.ENTITY_ID, entityId);
			partService.showPart(part, PartState.ACTIVATE);
		} catch (RepositoryException e) {
			throw new ConnectException("Cannot open " + entity, e);
		}

	}

	//
	// APP SERVICE
	//

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
		EPartService partService = eclipseContext.get(EPartService.class);
		// try {
		String entityEditorId = getSearchEntityEditorId(nodeType);
		for (MPart part : partService.getParts()) {
			String id = part.getPersistedState().get(ConnectE4Constants.NODE_TYPE);
			if (id != null && nodeType.equals(id)) {
				partService.showPart(part, PartState.ACTIVATE);
				return;
			}
		}

		// new part
		MPart part = partService.createPart(entityEditorId);
		if (part == null)
			throw new CmsException("No entity editor found for id " + entityEditorId);
		part.setLabel(label);
		part.getPersistedState().put(ConnectE4Constants.NODE_TYPE, nodeType);
		partService.showPart(part, PartState.ACTIVATE);
		// } catch (RepositoryException e) {
		// throw new ConnectException("Cannot open search entity for type " + nodeType,
		// e);
		// }
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

	// void setKnownAppWbServices(SortedMap<ServiceRanking, AppWorkbenchService>
	// knownAppWbServices) {
	// this.knownAppWbServices = knownAppWbServices;
	// }

}
