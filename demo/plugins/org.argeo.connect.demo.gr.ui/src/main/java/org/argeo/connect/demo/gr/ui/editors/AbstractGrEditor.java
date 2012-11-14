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
	/** DEPENDENCY INJECTION **/
	protected GrBackend grBackend;
	private Repository repository;

	// We use a one session per editor pattern to secure various nodes and
	// changes life cycle
	private Session session;

	// LIFE CYCLE
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		repository = grBackend.getRepository();
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
	}
}