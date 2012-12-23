/*
 * Argeo Connect - Data management and communications
 * Copyright (C) 2012 Argeo GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or combining it
 * with software covered by the terms of the Eclipse Public License, the
 * licensors of this Program grant you additional permission to convey the
 * resulting work. Corresponding Source for a non-source form of such a
 * combination shall include the source code for the parts of such software
 * which are used as well as that of the covered work.
 */
package org.argeo.connect.gps;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;

import org.argeo.ArgeoException;
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
			// Put it in preferences.
			Node tmpNode;

			// Maximal rotation speed
			tmpNode = newSession.addNode(
					GpsConstants.CONNECT_PARAM_ROTATION_MAX,
					CONNECT_CLEAN_PARAMETER);
			tmpNode.setProperty(Property.JCR_DESCRIPTION,
					"Maximal acceptable rotation speed value ");
			tmpNode.setProperty(CONNECT_PARAM_MIN_VALUE, 0d);
			tmpNode.setProperty(CONNECT_PARAM_MAX_VALUE, 360d);
			tmpNode.setProperty(CONNECT_PARAM_VALUE, 90d);

			// Maximal vertical speed
			tmpNode = newSession.addNode(
					GpsConstants.CONNECT_PARAM_VERTICAL_SPEED_MAX,
					CONNECT_CLEAN_PARAMETER);
			tmpNode.setProperty(Property.JCR_DESCRIPTION,
					"Maximal acceptable vertical speed value ");
			tmpNode.setProperty(CONNECT_PARAM_MIN_VALUE, 0d);
			// cf. http://en.wikipedia.org/wiki/Elevator#Taipei_101
			tmpNode.setProperty(CONNECT_PARAM_MAX_VALUE, 20d);
			tmpNode.setProperty(CONNECT_PARAM_VALUE, 16d);

			// maximal speed
			tmpNode = newSession.addNode(GpsConstants.CONNECT_PARAM_SPEED_MAX,
					CONNECT_CLEAN_PARAMETER);
			tmpNode.setProperty(Property.JCR_DESCRIPTION,
					"Maximal acceptable speed value ");
			tmpNode.setProperty(CONNECT_PARAM_MIN_VALUE, 0d);
			tmpNode.setProperty(CONNECT_PARAM_MAX_VALUE, 500d);
			// default value
			tmpNode.setProperty(CONNECT_PARAM_VALUE, 200d);

			// Maximal acceleration
			tmpNode = newSession.addNode(
					GpsConstants.CONNECT_PARAM_ACCELERATION_MAX,
					CONNECT_CLEAN_PARAMETER);
			tmpNode.setProperty(Property.JCR_DESCRIPTION,
					"Maximal acceptable acceleration value ");
			tmpNode.setProperty(CONNECT_PARAM_MIN_VALUE, 0d);
			tmpNode.setProperty(CONNECT_PARAM_MAX_VALUE, 10d);
			tmpNode.setProperty(CONNECT_PARAM_VALUE, 2d);

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
