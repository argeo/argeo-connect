package org.argeo.connect.web;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.jcr.JcrUtils;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.application.AbstractEntryPoint;
import org.eclipse.rap.rwt.client.service.BrowserNavigation;
import org.eclipse.rap.rwt.client.service.BrowserNavigationEvent;
import org.eclipse.rap.rwt.client.service.BrowserNavigationListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public abstract class AbstractCmsEntryPoint extends AbstractEntryPoint
		implements CmsSession {
	private final Log log = LogFactory.getLog(AbstractCmsEntryPoint.class);

	private Session session;
	private Node node;
	private String state;

	private BrowserNavigation history;

	public AbstractCmsEntryPoint(Session session) {
		super();
		this.session = session;

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

	protected abstract void refreshBody();

	public void navigateTo(String state) {
		setState(state);
		refreshBody();
		if (history != null)
			history.pushState(state, state);
	}

	protected void setState(String state) {
		this.state = state;
		if (state.startsWith("/")) {
			try {
				node = session.getNode(state);
			} catch (RepositoryException e) {
				node = null;
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
