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
package org.argeo.connect.demo.gr.ui.editors;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.connect.demo.gr.GrBackend;
import org.argeo.connect.demo.gr.GrException;
import org.argeo.jcr.JcrUtils;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;

/**
 * Parent Abstract GR multipage editor. Insure the presence of a GrBackend and
 * manage a life cycle of the JCR session that is bound to it
 */
public abstract class AbstractGrEditor extends FormEditor {

	/* DEPENDENCY INJECTION * */
	private GrBackend grBackend;

	private Repository repository;

	// We use a one session per editor pattern to secure various nodes and
	// changes life cycle
	private Session session;

	// LIFE CYCLE
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		try {
			session = repository.login();
		} catch (RepositoryException e) {
			throw new GrException("Unable to create new session"
					+ " to use with current editor", e);
		}
	}

	@Override
	public void dispose() {
		JcrUtils.logoutQuietly(session);
		super.dispose();
	}

	@Override
	public void doSaveAs() {
		// unused compulsory method
	}

	// EXPOSES TO CHILDREN CLASSES
	protected GrBackend getGrBackend() {
		return grBackend;
	}

	protected Session getSession() {
		return session;
	}

	/** DEPENDENCY INJECTION **/
	public void setGrBackend(GrBackend grBackend) {
		this.grBackend = grBackend;
		repository = grBackend.getRepository();
	}
}