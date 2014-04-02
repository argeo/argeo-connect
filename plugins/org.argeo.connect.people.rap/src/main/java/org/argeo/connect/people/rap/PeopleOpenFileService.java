/*
 * Copyright (C) 2007-2012 Argeo GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.argeo.connect.people.rap;

import java.io.IOException;
import java.io.InputStream;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.apache.commons.io.IOUtils;
import org.argeo.connect.people.PeopleException;
import org.argeo.eclipse.ui.specific.OpenFileService;
import org.argeo.jcr.JcrUtils;

/**
 * 
 * Extend Basic Default service handler to enable JCR file retrieval. Rap
 * specific.
 * 
 * To open a JCR File, expected URI is jcr:///Path/To/My/File/FileNode where
 * FileNode (the last part of the URI) is the name of a JCR node of type
 * {@code NodeTYpe.NT_FILE}
 * 
 * This simplified version of the URI only enable the opening of files that are
 * in the workspace "main" of the injected repository. Must be generalized.
 * 
 */
public class PeopleOpenFileService extends OpenFileService {

	public final static String JCR_SCHEME = "jcr";

	/* DEPENDENCY INJECTION */
	private Repository repository;

	protected byte[] getFileAsByteArray(String uri) {
		if (uri.startsWith(JCR_SCHEME)) {
			Session session = null;
			InputStream fis = null;
			try {
				session = repository.login();
				if (session.nodeExists(getPathFromUri(uri))) {
					Node nodeFile = session.getNode(getPathFromUri(uri));
					byte[] ba = null;
					fis = (InputStream) getContentBinary(nodeFile).getStream();
					ba = IOUtils.toByteArray(fis);
					return ba;
				}
				return null;
			} catch (RepositoryException re) {
				throw new PeopleException("Error while getting the file at "
						+ uri, re);
			} catch (IOException e) {
				throw new PeopleException("Stream error while opening file at "
						+ uri, e);
			} finally {
				IOUtils.closeQuietly(fis);
				JcrUtils.logoutQuietly(session);
			}
		} else
			return super.getFileAsByteArray(uri);
	}

	protected long getFileLength(String uri) {
		if (uri.startsWith(JCR_SCHEME)) {
			Session session = null;
			try {
				session = repository.login();
				if (session.nodeExists(getPathFromUri(uri))) {
					Node nodeFile = session.getNode(getPathFromUri(uri));
					return getContentBinary(nodeFile).getSize();
				}
				return -1;
			} catch (RepositoryException re) {
				throw new PeopleException("Error while getting the file at "
						+ uri, re);
			} finally {
				JcrUtils.logoutQuietly(session);
			}
		} else
			return super.getFileLength(uri);
	}

	protected String getFileName(String uri) {
		if (uri.startsWith(JCR_SCHEME)) {
			return JcrUtils.lastPathElement(getPathFromUri(uri));
		} else
			return super.getFileName(uri);
	}

	private String getPathFromUri(String uri) {
		return uri.substring((JCR_SCHEME + SCHEME_HOST_SEPARATOR).length());
	}

	private Binary getContentBinary(Node fileNode) throws RepositoryException {
		Node child = null;
		boolean isValid = true;
		if (!fileNode.isNodeType(NodeType.NT_FILE))
			isValid = false;
		else {
			child = fileNode.getNode(Property.JCR_CONTENT);
			if (!(child.isNodeType(NodeType.NT_RESOURCE) || child
					.hasProperty(Property.JCR_DATA)))
				isValid = false;
		}

		if (isValid)
			return child.getProperty(Property.JCR_DATA).getBinary();
		else
			throw new PeopleException(
					"ERROR: In the current implemented model, '"
							+ NodeType.NT_FILE
							+ "' file node must have a child node named jcr:content "
							+ "that has a BINARY Property named jcr:data "
							+ "where the actual data is stored");
		// return child;
	}

	/* DEPENDENCY INJECTION */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

}