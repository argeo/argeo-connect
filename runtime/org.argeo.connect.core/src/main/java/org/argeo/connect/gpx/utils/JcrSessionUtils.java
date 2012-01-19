package org.argeo.connect.gpx.utils;

import javax.jcr.Node;
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
			tmpNode.setProperty(CONNECT_PARAM_MAX_VALUE, 250d);
			// default value
			tmpNode.setProperty(CONNECT_PARAM_VALUE, 100d);

			// Maximal acceleration
			tmpNode = newSession.addNode(
					ConnectConstants.CONNECT_PARAM_ACCELERATION_MAX,
					CONNECT_CLEAN_PARAMETER);
			tmpNode.setProperty(Property.JCR_DESCRIPTION,
					"Maximal acceptable acceleration value ");
			tmpNode.setProperty(CONNECT_PARAM_MIN_VALUE, 0d);
			tmpNode.setProperty(CONNECT_PARAM_MAX_VALUE, 5d);
			tmpNode.setProperty(CONNECT_PARAM_VALUE, 2d);

			// Maximal radial speed
			tmpNode = newSession.addNode(
					ConnectConstants.CONNECT_PARAM_ROTATION_MAX,
					CONNECT_CLEAN_PARAMETER);
			tmpNode.setProperty(Property.JCR_DESCRIPTION,
					"Maximal acceptable rotation speed value ");
			tmpNode.setProperty(CONNECT_PARAM_MIN_VALUE, 0d);
			tmpNode.setProperty(CONNECT_PARAM_MAX_VALUE, 360d);
			tmpNode.setProperty(CONNECT_PARAM_VALUE, 90d);

		} catch (Exception e) {
			throw new ArgeoException(
					"Cannot create new session for node named [" + name + "]",
					e);
		}

		return newSession;
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
