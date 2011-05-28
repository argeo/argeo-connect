package org.argeo.connect.ui.gps.commons;

import javax.jcr.Node;

import org.argeo.ArgeoException;
import org.argeo.connect.ConnectConstants;
import org.argeo.connect.ConnectNames;
import org.argeo.connect.ConnectTypes;

public class JcrSessionUtils implements ConnectTypes, ConnectNames {

	public static Node createNewSession(Node parent, String name) {
		Node newSession;

		try {
			newSession = parent.addNode(name, CONNECT_CLEAN_TRACK_SESSION);

			// Add Parameter Nodes

			// TODO : remove hard coding from instantiation of default model.
			Node tmpNode;
			// minimal speed
			tmpNode = newSession.addNode(ConnectConstants.CONNECT_PARAM_SPEED_MIN,
					CONNECT_CLEAN_PARAMETER);
			tmpNode.setProperty(CONNECT_PARAM_NAME, "Minimal speed");
			tmpNode.setProperty(CONNECT_PARAM_LABEL,
					"Enter minimal acceptable speed value ");
			tmpNode.setProperty(CONNECT_PARAM_MIN_VALUE, -1000d);
			tmpNode.setProperty(CONNECT_PARAM_MAX_VALUE, 0d);
			tmpNode.setProperty(CONNECT_PARAM_VALUE, -500);

			// maximal speed
			tmpNode = newSession.addNode(ConnectConstants.CONNECT_PARAM_SPEED_MAX,
					CONNECT_CLEAN_PARAMETER);
			tmpNode.setProperty(CONNECT_PARAM_NAME, "Maximal speed");
			tmpNode.setProperty(CONNECT_PARAM_LABEL,
					"Enter maximal acceptable speed value ");
			tmpNode.setProperty(CONNECT_PARAM_MIN_VALUE, 0d);
			tmpNode.setProperty(CONNECT_PARAM_MAX_VALUE, 2000d);
			tmpNode.setProperty(CONNECT_PARAM_VALUE, 1000);

			// Minimal acceleration
			tmpNode = newSession.addNode(ConnectConstants.CONNECT_PARAM_ACCELERATION_MIN,
					CONNECT_CLEAN_PARAMETER);
			tmpNode.setProperty(CONNECT_PARAM_NAME, "Minimal acceleration");
			tmpNode.setProperty(CONNECT_PARAM_LABEL,
					"Enter minimal acceptable acceleration value ");
			tmpNode.setProperty(CONNECT_PARAM_MIN_VALUE, -1000d);
			tmpNode.setProperty(CONNECT_PARAM_MAX_VALUE, 1000d);
			tmpNode.setProperty(CONNECT_PARAM_VALUE, 0d);

			// Maximal acceleration
			tmpNode = newSession.addNode(ConnectConstants.CONNECT_PARAM_ACCELERATION_MAX,
					CONNECT_CLEAN_PARAMETER);
			tmpNode.setProperty(CONNECT_PARAM_NAME, "Maximal acceleration");
			tmpNode.setProperty(CONNECT_PARAM_LABEL,
					"Enter maximal acceptable acceleration value ");
			tmpNode.setProperty(CONNECT_PARAM_MIN_VALUE, 0d);
			tmpNode.setProperty(CONNECT_PARAM_MAX_VALUE, 2000d);
			tmpNode.setProperty(CONNECT_PARAM_VALUE, 1000d);

			// Minimal radial speed
			tmpNode = newSession.addNode(ConnectConstants.CONNECT_PARAM_RADIAL_SPEED_MIN,
					CONNECT_CLEAN_PARAMETER);
			tmpNode.setProperty(CONNECT_PARAM_NAME, "Minimal radial speed");
			tmpNode.setProperty(CONNECT_PARAM_LABEL,
					"Enter minimal acceptable radial speed value ");
			tmpNode.setProperty(CONNECT_PARAM_MIN_VALUE, -1000d);
			tmpNode.setProperty(CONNECT_PARAM_MAX_VALUE, 0d);
			tmpNode.setProperty(CONNECT_PARAM_VALUE, -500d);
			tmpNode.setProperty(CONNECT_PARAM_IS_USED, false);

			// Maximal radial speed
			tmpNode = newSession.addNode(ConnectConstants.CONNECT_PARAM_RADIAL_SPEED_MAX,
					CONNECT_CLEAN_PARAMETER);
			tmpNode.setProperty(CONNECT_PARAM_NAME, "Maximal radial speed");
			tmpNode.setProperty(CONNECT_PARAM_LABEL,
					"Enter maximal acceptable radial value ");
			tmpNode.setProperty(CONNECT_PARAM_MIN_VALUE, 0d);
			tmpNode.setProperty(CONNECT_PARAM_MAX_VALUE, 10000d);
			tmpNode.setProperty(CONNECT_PARAM_VALUE, 5000d);

		} catch (Exception e) {
			throw new ArgeoException(
					"Cannot create new session for node named [" + name + "]",
					e);
		}

		return newSession;
	}
}
