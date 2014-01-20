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
package org.argeo.connect.people.ui.editors;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.ArgeoException;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.jcr.JcrUtils;
import org.argeo.jcr.UserJcrUtils;
import org.argeo.security.UserAdminService;
import org.argeo.security.jcr.JcrUserDetails;
import org.argeo.security.ui.admin.editors.ArgeoUserEditorInput;
import org.argeo.security.ui.admin.editors.DefaultUserMainPage;
import org.argeo.security.ui.admin.editors.UserRolesPage;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.springframework.security.GrantedAuthority;

/** People specific user Editor. Adds among other group management. */
public class UserEditor extends FormEditor {
	// private final static Log log = LogFactory.getLog(UserEditor.class);
	private static final long serialVersionUID = 270486756895365730L;

	public final static String ID = PeopleUiPlugin.PLUGIN_ID + ".userEditor";

	private JcrUserDetails userDetails;
	private Node userProfile;
	private UserAdminService userAdminService;
	private Session session;

	// Pages
	private DefaultUserMainPage defaultUserMainPage;
	private UserRolesPage userRolesPage;

	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		String username = "";

		username = ((ArgeoUserEditorInput) getEditorInput()).getUsername();
		userProfile = UserJcrUtils.getUserProfile(session, username);

		if (userAdminService.userExists(username)) {
			userDetails = (JcrUserDetails) userAdminService
					.loadUserByUsername(username);
		} else {
			GrantedAuthority[] authorities = {};
			try {
				userDetails = new JcrUserDetails(session, username, null,
						authorities);
			} catch (RepositoryException e) {
				throw new ArgeoException("Cannot retrieve disabled JCR profile");
			}
		}

		this.setPartProperty("name", username != null ? username : "<new user>");
		setPartName(username != null ? username : "<new user>");
	}

	protected void addPages() {
		try {
			defaultUserMainPage = new DefaultUserMainPage(this, userProfile);
			addPage(defaultUserMainPage);
			userRolesPage = new UserRolesPage(this, userDetails,
					userAdminService);
			addPage(userRolesPage);
		} catch (Exception e) {
			throw new ArgeoException("Cannot add pages", e);
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		// list pages

		if (defaultUserMainPage.isDirty()) {
			defaultUserMainPage.doSave(monitor);
			String newPassword = defaultUserMainPage.getNewPassword();
			defaultUserMainPage.resetNewPassword();
			if (newPassword != null)
				userDetails = userDetails.cloneWithNewPassword(newPassword);
		}

		if (userRolesPage.isDirty()) {
			userRolesPage.doSave(monitor);
			userDetails = userDetails.cloneWithNewRoles(userRolesPage
					.getRoles());
		}

		userAdminService.updateUser(userDetails);
		firePropertyChange(PROP_DIRTY);

		userRolesPage.setUserDetails(userDetails);
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	public void refresh() {
		userRolesPage.refresh();
	}

	@Override
	public void dispose() {
		JcrUtils.logoutQuietly(session);
		super.dispose();
	}

	/* DEPENDENCY INJECTION */
	public void setUserAdminService(UserAdminService userAdminService) {
		this.userAdminService = userAdminService;
	}

	public void setRepository(Repository repository) {
		this.session = CommonsJcrUtils.login(repository);
	}
}