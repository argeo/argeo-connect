package org.argeo.connect.gpx.utils;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;

import org.argeo.ArgeoException;
import org.argeo.connect.ConnectConstants;
import org.argeo.connect.ConnectNames;
import org.argeo.connect.ConnectTypes;
import org.argeo.jcr.JcrUtils;

public class JcrSessionUtils implements ConnectTypes, ConnectNames {

	public static Node createNewSession(Node parent, String name) {
		Node newSession;

		try {
			newSession = parent.addNode(name, CONNECT_CLEAN_TRACK_SESSION);

			// Add Parameter Nodes

			// TODO : remove hard coding from instantiation of default model.
			Node tmpNode;
			// maximal speed
			tmpNode = newSession.addNode(
					ConnectConstants.CONNECT_PARAM_SPEED_MAX,
					CONNECT_CLEAN_PARAMETER);
			tmpNode.setProperty(Property.JCR_DESCRIPTION,
					"Maximal acceptable speed value ");
			tmpNode.setProperty(CONNECT_PARAM_MIN_VALUE, 0d);
			tmpNode.setProperty(CONNECT_PARAM_MAX_VALUE, 500d);
			// default value
			tmpNode.setProperty(CONNECT_PARAM_VALUE, 200d);

			// Maximal acceleration
			tmpNode = newSession.addNode(
					ConnectConstants.CONNECT_PARAM_ACCELERATION_MAX,
					CONNECT_CLEAN_PARAMETER);
			tmpNode.setProperty(Property.JCR_DESCRIPTION,
					"Maximal acceptable acceleration value ");
			tmpNode.setProperty(CONNECT_PARAM_MIN_VALUE, 0d);
			tmpNode.setProperty(CONNECT_PARAM_MAX_VALUE, 10d);
			tmpNode.setProperty(CONNECT_PARAM_VALUE, 2d);

			// Maximal rotation speed
			tmpNode = newSession.addNode(
					ConnectConstants.CONNECT_PARAM_ROTATION_MAX,
					CONNECT_CLEAN_PARAMETER);
			tmpNode.setProperty(Property.JCR_DESCRIPTION,
					"Maximal acceptable rotation speed value ");
			tmpNode.setProperty(CONNECT_PARAM_MIN_VALUE, 0d);
			tmpNode.setProperty(CONNECT_PARAM_MAX_VALUE, 360d);
			tmpNode.setProperty(CONNECT_PARAM_VALUE, 90d);

			tmpNode.getSession().save();

		} catch (Exception e) {
			throw new ArgeoException(
					"Cannot create new session for node named [" + name + "]",
					e);
		}
		return newSession;
	}

	public static void copyOneParameterNode(Node paramModelNode,
			Node paramTargetNode) throws Exception {
		paramTargetNode.setProperty(Property.JCR_DESCRIPTION, paramModelNode
				.getProperty(Property.JCR_DESCRIPTION).getString());
		paramTargetNode.setProperty(CONNECT_PARAM_MIN_VALUE, paramModelNode
				.getProperty(CONNECT_PARAM_MIN_VALUE).getDouble());
		paramTargetNode.setProperty(CONNECT_PARAM_MAX_VALUE, paramModelNode
				.getProperty(CONNECT_PARAM_MAX_VALUE).getDouble());
		paramTargetNode.setProperty(CONNECT_PARAM_VALUE, paramModelNode
				.getProperty(CONNECT_PARAM_VALUE).getDouble());
	}

	public static void copyDataFromModel(Node modelNode, Node newNode) {
		try {

			// Copy parameter nodes
			NodeIterator ni = modelNode.getNodes();
			while (ni.hasNext()) {
				Node modParamNode = ni.nextNode();
				if (modParamNode
						.isNodeType(ConnectTypes.CONNECT_CLEAN_PARAMETER)) {
					Node newParamNode;
					if (newNode.hasNode(modParamNode.getName())) {
						newParamNode = newNode.getNode(modParamNode.getName());
					} else {
						newParamNode = newNode.addNode(modParamNode.getName(),
								modParamNode.getPrimaryNodeType().toString());
					}
					copyOneParameterNode(modParamNode, newParamNode);
				}
			}

			// Copy default sensor & local repo name if defined
			if (modelNode.hasProperty(CONNECT_DEFAULT_SENSOR))
				newNode.setProperty(CONNECT_DEFAULT_SENSOR, modelNode
						.getProperty(CONNECT_DEFAULT_SENSOR).getString());

			if (modelNode.hasProperty(CONNECT_LOCAL_REPO_NAME))
				newNode.setProperty(CONNECT_LOCAL_REPO_NAME, modelNode
						.getProperty(CONNECT_LOCAL_REPO_NAME).getString());

			newNode.getSession().save();
		} catch (Exception e) {
			throw new ArgeoException(
					"Unexpected error while dupplicating infos from model to newly created node.",
					e);
		}
	}

	public static Node createLocalRepository(Node parent, String techName,
			String title) {
		Node localRepo;
		try {
			localRepo = parent.addNode(techName, CONNECT_LOCAL_REPOSITORY);
			localRepo.setProperty(Property.JCR_TITLE, title);
			JcrUtils.updateLastModified(localRepo);
			localRepo.getSession().save();
		} catch (Exception e) {
			throw new ArgeoException(
					"Cannot create new repository for node named [" + techName
							+ "]", e);
		}
		return localRepo;
	}
}
