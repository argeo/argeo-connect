package org.argeo.connect.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.AppService;
import org.argeo.connect.ConnectException;
import org.argeo.connect.SystemAppService;
import org.argeo.eclipse.ui.EclipseUiUtils;

public class DynamicSystemAppService extends AbstractAppService implements SystemAppService {
	private final static Log log = LogFactory.getLog(DynamicSystemAppService.class);

	private List<AppService> knownAppServices = Collections.synchronizedList(new ArrayList<>());

	public DynamicSystemAppService() {
		super();
	}

	@Override
	public Node publishEntity(Node parent, String nodeType, Node srcNode, boolean removeSrcNode)
			throws RepositoryException {
		for (AppService appService : knownAppServices) {
			if (appService.isKnownType(nodeType))
				return appService.publishEntity(parent, nodeType, srcNode, removeSrcNode);
		}
		return null;
	}

	@Override
	public String getAppBaseName() {
		return SuiteConstants.SUITE_APP_BASE_NAME;
	}

	@Override
	public String getBaseRelPath(String nodeType) {
		for (AppService appService : knownAppServices) {
			if (appService.isKnownType(nodeType))
				return appService.getBaseRelPath(nodeType);
		}
		return null;
		// return getAppBaseName();
	}

	@Override
	public String getDefaultRelPath(Node entity) throws RepositoryException {
		for (AppService appService : knownAppServices) {
			if (appService.isKnownType(entity))
				return appService.getDefaultRelPath(entity);
		}
		return null;
	}

	@Override
	public String getMainNodeType(Node node) {
		for (AppService appService : knownAppServices) {
			String foundType = appService.getMainNodeType(node);
			if (EclipseUiUtils.notEmpty(foundType))
				return foundType;
		}
		return null;
	}

	@Override
	public String getDefaultRelPath(Session session, String nodetype, String id) {
		for (AppService appService : knownAppServices) {
			if (appService.isKnownType(nodetype))
				return appService.getDefaultRelPath(session, nodetype, id);
		}
		return null;
	}

	/** Insures the correct service is called on save */
	@Override
	public Node saveEntity(Node entity, boolean publish) {
		for (AppService appService : knownAppServices) {
			if (appService.isKnownType(entity))
				return appService.saveEntity(entity, publish);
		}
		throw new ConnectException("Unknown NodeType for " + entity + ". Cannot save");
		// return AppService.super.saveEntity(entity, publish);
	}

	@Override
	public boolean isKnownType(Node entity) {
		for (AppService appService : knownAppServices) {
			if (appService.isKnownType(entity))
				return true;
		}
		return false;
	}

	@Override
	public boolean isKnownType(String nodeType) {
		for (AppService appService : knownAppServices) {
			if (appService.isKnownType(nodeType))
				return true;
		}
		return false;
	}

	public void addAppService(AppService appService, Map<String, String> properties) {
		knownAppServices.add(appService);
		if (log.isDebugEnabled())
			log.debug("Added app service " + appService);
	}

	public void removeAppService(AppService appService, Map<String, String> properties) {
		knownAppServices.remove(appService);
	}
}
