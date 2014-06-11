package org.argeo.connect.web;

import java.util.Map;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.application.EntryPoint;
import org.eclipse.rap.rwt.application.EntryPointFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationManager;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.providers.anonymous.AnonymousAuthenticationToken;
import org.springframework.security.userdetails.User;
import org.springframework.security.userdetails.UserDetails;

public class CmsEntryPointFactory implements EntryPointFactory {
	private final static Log log = LogFactory
			.getLog(CmsEntryPointFactory.class);

	private Repository nodeRepository;
	private AuthenticationManager authenticationManager;
	private String systemKey = null;

	private CmsUiProvider header;
	private CmsUiProvider dynamicPages;
	private Map<String, CmsUiProvider> staticPages;

	@Override
	public EntryPoint create() {
		Authentication authentication = SecurityContextHolder.getContext()
				.getAuthentication();
		// TODO Better deal with authentication
		if (authentication == null) {
			try {
				GrantedAuthority[] anonAuthorities = { new GrantedAuthorityImpl(
						"ROLE_ANONYMOUS") };
				UserDetails anonUser = new User("anonymous", "", true, true,
						true, true, anonAuthorities);
				AnonymousAuthenticationToken anonToken = new AnonymousAuthenticationToken(
						systemKey, anonUser, anonAuthorities);

				UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
						"root", "demo");

				authentication = authenticationManager.authenticate(token);
				SecurityContextHolder.getContext().setAuthentication(
						authentication);
				if (log.isDebugEnabled())
					log.debug("Authenticated as " + authentication);
			} catch (Exception e) {
				throw new ArgeoException("Cannot authenticate", e);
			}
		}

		try {

			// HttpServletRequest req = RWT.getRequest();
			// log.debug(req.getRequestURL().toString());
			// log.debug(req.getQueryString());
			// log.debug(RWT.getUISession().getId());
			// log.debug(req.getPathTranslated());

			// String currentState = navigationListener.getCurrentState();
			// log.debug("Current state : " + currentState);

			Session session = nodeRepository.login();
			return new CmsEntryPoint(session);
		} catch (RepositoryException e) {
			throw new ArgeoException("Cannot create entrypoint", e);
		}
	}

	public void setNodeRepository(Repository nodeRepository) {
		this.nodeRepository = nodeRepository;
	}

	public void setAuthenticationManager(
			AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}

	public void setSystemKey(String systemKey) {
		this.systemKey = systemKey;
	}

	public void setHeader(CmsUiProvider header) {
		this.header = header;
	}

	public void setDynamicPages(CmsUiProvider dynamicPages) {
		this.dynamicPages = dynamicPages;
	}

	public void setStaticPages(Map<String, CmsUiProvider> staticPages) {
		this.staticPages = staticPages;
	}

	private class CmsEntryPoint extends AbstractCmsEntryPoint {
		private Composite bodyArea;

		public CmsEntryPoint(Session session) {
			super(session);
		}

		@Override
		protected void createContents(Composite parent) {
			try {
				getShell().getDisplay().setData(CmsSession.KEY, this);

				parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
						true));
				GridLayout layout = new GridLayout(1, true);
				layout.horizontalSpacing = 0;
				layout.verticalSpacing = 0;
				parent.setLayout(layout);
				
				Control headerArea = header.createUi(parent, getNode());
				GridData headerData = new GridData(SWT.FILL, SWT.FILL, false,
						false);
				headerData.heightHint = 50;
				headerArea.setLayoutData(headerData);

				ScrolledComposite scrolledArea = new ScrolledComposite(parent,
						SWT.H_SCROLL);
				scrolledArea.setData(RWT.CUSTOM_VARIANT,
						CmsStyles.CMS_SCROLLED_AREA);
				// scrolledComp.setMinHeight( CONTENT_MIN_HEIGHT );
				// scrolledComp.setMinWidth( CENTER_AREA_WIDTH );
				scrolledArea.setExpandVertical(true);
				scrolledArea.setExpandHorizontal(true);
				scrolledArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
						true, true));
				scrolledArea.setLayout(new FillLayout());
				bodyArea = new Composite(scrolledArea, SWT.NONE);
				bodyArea.setData(RWT.CUSTOM_VARIANT, CmsStyles.CMS_BODY);
				bodyArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
						true));
				bodyArea.setBackgroundMode(SWT.INHERIT_DEFAULT);
				bodyArea.setLayout(new FillLayout());
				scrolledArea.setContent(bodyArea);
			} catch (Exception e) {
				throw new ArgeoException("Cannot create entrypoint contents", e);
			}
		}

		@Override
		protected void refreshBody() {
			// clear
			for (Control child : bodyArea.getChildren())
				child.dispose();

			String state = getState();
			try {
				if (state == null)
					log.debug("null state");
				else if (state.length() == 0)
					log.debug("empty state");
				else if (state.charAt(0) == '/')
					dynamicPages.createUi(bodyArea, getNode());
				else if (staticPages.containsKey(state))
					staticPages.get(state).createUi(bodyArea, getNode());
				else
					log.error("Unsupported state " + state);
			} catch (RepositoryException e) {
				log.error("Cannot refresh body", e);
			}

			bodyArea.layout(true, true);
		}
	}
}
