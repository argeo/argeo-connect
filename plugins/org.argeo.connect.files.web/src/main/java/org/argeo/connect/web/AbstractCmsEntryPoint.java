package org.argeo.connect.web;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.argeo.jcr.JcrUtils;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.application.AbstractEntryPoint;
import org.eclipse.rap.rwt.client.service.BrowserNavigation;
import org.eclipse.rap.rwt.client.service.BrowserNavigationEvent;
import org.eclipse.rap.rwt.client.service.BrowserNavigationListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.springframework.security.context.SecurityContextHolder;

/** Manages history and navigation */
public abstract class AbstractCmsEntryPoint extends AbstractEntryPoint
		implements CmsSession {
	private final Log log = LogFactory.getLog(AbstractCmsEntryPoint.class);

	private Repository repository;
	private String workspace;
	private Session session;
	private Node node;
	private String state;

	private BrowserNavigation history;

	public AbstractCmsEntryPoint(Repository repository, String workspace) {
		if (SecurityContextHolder.getContext().getAuthentication() == null)
			logAsAnonymous();

		this.repository = repository;
		this.workspace = workspace;
		authChange();

		history = RWT.getClient().getService(BrowserNavigation.class);
		if (history != null)
			history.addBrowserNavigationListener(new CmsNavigationListener());
	}

	@Override
	protected Shell createShell(Display display) {
		Shell shell = super.createShell(display);
		shell.setData(RWT.CUSTOM_VARIANT, CmsStyles.CMS_SHELL);
		display.disposeExec(new Runnable() {

			@Override
			public void run() {
				if (log.isDebugEnabled())
					log.debug("Logging out " + session);
				JcrUtils.logoutQuietly(session);
			}
		});
		return shell;
	}

	/** Recreate header UI */
	protected abstract void refreshHeader();

	/** Recreate body UI */
	protected abstract void refreshBody();

	/** Log as anonymous */
	protected abstract void logAsAnonymous();

	public void navigateTo(String state) {
		setState(state);
		refreshBody();
		if (history != null)
			history.pushState(state, state);
	}

	@Override
	public void authChange() {
		try {
			String currentPath = null;
			if (node != null)
				currentPath = node.getPath();
			JcrUtils.logoutQuietly(session);

			if (SecurityContextHolder.getContext().getAuthentication() == null)
				logAsAnonymous();
			session = repository.login(workspace);
			if (currentPath != null)
				node = session.getNode(currentPath);

			// refresh UI
			refreshHeader();
			refreshBody();
		} catch (RepositoryException e) {
			throw new ArgeoException("Cannot perform auth change", e);
		}

	}

	protected void setState(String state) {
		this.state = state;
		if (state.startsWith("/")) {
			try {
				if (session.itemExists(state))
					node = session.getNode(state);
				else
					node = null;
			} catch (RepositoryException e) {
				throw new ArgeoException("Cannot retrieve node", e);
			}
		}
	}

	protected Node getNode() {
		return node;
	}

	public String getState() {
		return state;
	}

	protected Session getSession() {
		return session;
	}

	private class CmsNavigationListener implements BrowserNavigationListener {
		private static final long serialVersionUID = -3591018803430389270L;

		@Override
		public void navigated(BrowserNavigationEvent event) {
			setState(event.getState());
			refreshBody();
		}
	}

}