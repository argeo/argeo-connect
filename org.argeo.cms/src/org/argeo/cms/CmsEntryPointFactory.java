package org.argeo.cms;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.application.EntryPoint;
import org.eclipse.rap.rwt.application.EntryPointFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationManager;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.anonymous.AnonymousAuthenticationToken;
import org.springframework.security.userdetails.User;
import org.springframework.security.userdetails.UserDetails;

/** Creates and registers an {@link EntryPoint} */
public class CmsEntryPointFactory implements EntryPointFactory {
	private final static Log log = LogFactory
			.getLog(CmsEntryPointFactory.class);

	private Repository repository;
	private String workspace;
	private AuthenticationManager authenticationManager;
	private String systemKey = null;

	private CmsUiProvider header;
	private CmsUiProvider dynamicPages;
	private Map<String, CmsUiProvider> staticPages;

	@Override
	public EntryPoint create() {
		CmsEntryPoint cmsEntryPoint = new CmsEntryPoint(repository, workspace);
		CmsSession.current.set(cmsEntryPoint);
		return cmsEntryPoint;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setWorkspace(String workspace) {
		this.workspace = workspace;
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
		private Composite headerArea;
		private Composite bodyArea;

		// private ScrolledComposite scrolledArea;

		public CmsEntryPoint(Repository repository, String workspace) {
			super(repository, workspace);
		}

		@Override
		protected void createContents(Composite parent) {
			try {
				getShell().getDisplay().setData(CmsSession.KEY, this);

				parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
						true));
				parent.setLayout(CmsUtils.noSpaceGridLayout());

				headerArea = new Composite(parent, SWT.NONE);
				headerArea.setLayout(new FillLayout());
				GridData headerData = new GridData(SWT.FILL, SWT.FILL, false,
						false);
				headerData.heightHint = 50;
				headerArea.setLayoutData(headerData);
				refreshHeader();

				// scrolledArea = new ScrolledComposite(parent, SWT.V_SCROLL);
				// scrolledArea.setData(RWT.CUSTOM_VARIANT,
				// CmsStyles.CMS_SCROLLED_AREA);
				// // scrolledComp.setMinHeight( CONTENT_MIN_HEIGHT );
				// // scrolledComp.setMinWidth( CENTER_AREA_WIDTH );
				// scrolledArea.setExpandVertical(true);
				// scrolledArea.setExpandHorizontal(true);
				// scrolledArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
				// true, true));
				// // scrolledArea.setLayout(new FillLayout());
				// scrolledArea.setAlwaysShowScrollBars(true);
				// scrolledArea.setExpandVertical(true);
				// scrolledArea.setMinHeight(400);

				bodyArea = new Composite(parent, SWT.NONE);
				bodyArea.setData(RWT.CUSTOM_VARIANT, CmsStyles.CMS_BODY);
				bodyArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
						true));
				bodyArea.setBackgroundMode(SWT.INHERIT_DEFAULT);
				bodyArea.setLayout(CmsUtils.noSpaceGridLayout());
				// scrolledArea.setContent(bodyArea);
				//
				// // cf.
				// //
				// http://www.java2s.com/Code/Java/SWT-JFace-Eclipse/CreateaScrolledCompositewithwrappingcontent.htm
				// scrolledArea.addControlListener(new ControlAdapter() {
				// public void controlResized(ControlEvent e) {
				// Rectangle r = scrolledArea.getClientArea();
				// Point preferredSize = bodyArea.computeSize(SWT.DEFAULT,
				// r.height);
				// scrolledArea.setMinHeight(preferredSize.y);
				// }
				// });
			} catch (Exception e) {
				throw new ArgeoException("Cannot create entrypoint contents", e);
			}
		}

		@Override
		protected void refreshHeader() {
			if (headerArea == null)
				return;
			for (Control child : headerArea.getChildren())
				child.dispose();
			try {
				header.createUi(headerArea, getNode());
			} catch (RepositoryException e) {
				throw new ArgeoException("Cannot refresh header", e);
			}
			headerArea.layout(true, true);
		}

		@Override
		protected void refreshBody() {
			if (bodyArea == null)
				return;
			// clear
			for (Control child : bodyArea.getChildren())
				child.dispose();
			bodyArea.setLayout(CmsUtils.noSpaceGridLayout());

			// Exception
			Throwable exception = getException();
			if (exception != null) {
				new Label(bodyArea, SWT.NONE).setText("Unreachable state : "
						+ getState());
				if (getNode() != null)
					new Label(bodyArea, SWT.NONE).setText("Context : "
							+ getNode());

				Text errorText = new Text(bodyArea, SWT.MULTI | SWT.H_SCROLL
						| SWT.V_SCROLL);
				errorText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
						true));
				StringWriter sw = new StringWriter();
				exception.printStackTrace(new PrintWriter(sw));
				errorText.setText(sw.toString());
				IOUtils.closeQuietly(sw);

				// new CmsLink("Back", getState()).createUi(bodyArea,
				// getNode());
				// TODO report
			} else {
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
			}
			bodyArea.layout(true, true);
			// scrolledArea.setContent(bodyArea);
			// scrolledArea.layout(true, true);
		}

		@Override
		protected void logAsAnonymous() {
			// TODO Better deal with anonymous authentication
			try {
				GrantedAuthority[] anonAuthorities = { new GrantedAuthorityImpl(
						"ROLE_ANONYMOUS") };
				UserDetails anonUser = new User("anonymous", "", true, true,
						true, true, anonAuthorities);
				AnonymousAuthenticationToken anonToken = new AnonymousAuthenticationToken(
						systemKey, anonUser, anonAuthorities);
				Authentication authentication = authenticationManager
						.authenticate(anonToken);
				SecurityContextHolder.getContext().setAuthentication(
						authentication);
			} catch (Exception e) {
				throw new ArgeoException("Cannot authenticate", e);
			}
		}
	}
}
