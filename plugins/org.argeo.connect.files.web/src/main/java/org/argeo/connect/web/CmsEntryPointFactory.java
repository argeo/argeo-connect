package org.argeo.connect.web;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.ArgeoException;
import org.eclipse.rap.rwt.application.EntryPoint;
import org.eclipse.rap.rwt.application.EntryPointFactory;
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
	private Repository nodeRepository;
	private AuthenticationManager authenticationManager;
	private String systemKey = null;

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
			} catch (Exception e) {
				throw new ArgeoException("Cannot authenticate", e);
			}
		}

		try {
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

}
