package org.argeo.people.workbench.rap.util;

import javax.jcr.Repository;
import javax.jcr.Session;

import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.workbench.Refreshable;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

/** Simplify implementation of basic editor in a people context */
public abstract class AbstractPeopleBasicEditor extends EditorPart implements
		Refreshable {

	/* DEPENDENCY INJECTION */
	private Repository repository;
	private Session session;

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
		session = ConnectJcrUtils.login(repository);
	}

	@Override
	public void dispose() {
		JcrUtils.logoutQuietly(session);
		super.dispose();
	}

	protected Session getSession() {
		return session;
	}

	// COMPULSORY UNUSED METHODS
	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public void setFocus() {
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}

	/* DEPENDENCY INJECTION */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}
}