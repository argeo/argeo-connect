package org.argeo.connect.core;

import static org.osgi.service.component.ComponentConstants.COMPONENT_NAME;

import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.AppService;
import org.argeo.connect.ConnectException;
import org.argeo.connect.ServiceRanking;
import org.argeo.connect.SystemAppService;
import org.argeo.eclipse.ui.EclipseUiUtils;

public class DynamicSystemAppService extends AbstractAppService implements SystemAppService {
	private final static Log log = LogFactory.getLog(DynamicSystemAppService.class);

	private SortedMap<ServiceRanking, AppService> knownAppServices = Collections.synchronizedSortedMap(new TreeMap<>());
	// private List<AppService> knownAppServices = Collections.synchronizedList(new
	// ArrayList<>());

	public DynamicSystemAppService() {
		super();
	}

	@Override
	public Node publishEntity(Node parent, String nodeType, Node srcNode, boolean removeSrcNode)
			throws RepositoryException {
		for (AppService appService : knownAppServices.values()) {
			if (appService.isKnownType(nodeType))
				return appService.publishEntity(parent, nodeType, srcNode, removeSrcNode);
		}
		return null;
	}

	@Override
	public String getAppBaseName() {
		return OfficeConstants.SUITE_APP_BASE_NAME;
	}

	@Override
	public String getBaseRelPath(String nodeType) {
		for (AppService appService : knownAppServices.values()) {
			if (appService.isKnownType(nodeType))
				return appService.getBaseRelPath(nodeType);
		}
		return null;
		// return getAppBaseName();
	}

	@Override
	public String getDefaultRelPath(Node entity) throws RepositoryException {
		for (AppService appService : knownAppServices.values()) {
			if (appService.isKnownType(entity))
				return appService.getDefaultRelPath(entity);
		}
		return null;
	}

	@Override
	public String getMainNodeType(Node node) {
		for (AppService appService : knownAppServices.values()) {
			String foundType = appService.getMainNodeType(node);
			if (EclipseUiUtils.notEmpty(foundType))
				return foundType;
		}
		return null;
	}

	@Override
	public String getDefaultRelPath(Session session, String nodetype, String id) {
		for (AppService appService : knownAppServices.values()) {
			if (appService.isKnownType(nodetype))
				return appService.getDefaultRelPath(session, nodetype, id);
		}
		return null;
	}

	/** Insures the correct service is called on save */
	@Override
	public Node saveEntity(Node entity, boolean publish) {
		for (AppService appService : knownAppServices.values()) {
			if (appService.isKnownType(entity))
				return appService.saveEntity(entity, publish);
		}
		throw new ConnectException("Unknown NodeType for " + entity + ". Cannot save");
		// return AppService.super.saveEntity(entity, publish);
	}

	@Override
	public boolean isKnownType(Node entity) {
		for (AppService appService : knownAppServices.values()) {
			if (appService.isKnownType(entity))
				return true;
		}
		return false;
	}

	@Override
	public boolean isKnownType(String nodeType) {
		for (AppService appService : knownAppServices.values()) {
			if (appService.isKnownType(nodeType))
				return true;
		}
		return false;
	}

	public void addAppService(AppService appService, Map<String, Object> properties) {
		knownAppServices.put(new ServiceRanking(properties), appService);
		if (log.isDebugEnabled())
			log.debug("Added app service "
					+ (properties.containsKey(COMPONENT_NAME) ? properties.get(COMPONENT_NAME) : appService));
	}

	public void removeAppService(AppService appService, Map<String, Object> properties) {
		knownAppServices.remove(new ServiceRanking(properties));
		if (log.isDebugEnabled())
			log.debug("Removed app service "
					+ (properties.containsKey(COMPONENT_NAME) ? properties.get(COMPONENT_NAME) : appService));
	}
}
