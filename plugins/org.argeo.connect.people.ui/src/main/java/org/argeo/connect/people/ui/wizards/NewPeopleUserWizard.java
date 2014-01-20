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
package org.argeo.connect.people.ui.wizards;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.people.ui.composites.UserGroupTableComposite;
import org.argeo.eclipse.ui.ErrorFeedback;
import org.argeo.jcr.JcrUtils;
import org.argeo.jcr.UserJcrUtils;
import org.argeo.security.UserAdminService;
import org.argeo.security.jcr.JcrSecurityModel;
import org.argeo.security.jcr.JcrUserDetails;
import org.eclipse.jface.dialogs.IPageChangeProvider;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.springframework.security.GrantedAuthority;

/** Wizard to create a new user in a connect People application. */
public class NewPeopleUserWizard extends Wizard {
	private final static Log log = LogFactory.getLog(NewPeopleUserWizard.class);
	private Session session;
	private UserAdminService userAdminService;
	private JcrSecurityModel jcrSecurityModel;

	// pages
	private MainUserInfoWizardPage mainUserInfo;
	private ChooseGroupsPage chooseGroupsPage;

	public NewPeopleUserWizard(Session session,
			UserAdminService userAdminService, JcrSecurityModel jcrSecurityModel) {
		this.session = session;
		this.userAdminService = userAdminService;
		this.jcrSecurityModel = jcrSecurityModel;
	}

	@Override
	public void addPages() {
		mainUserInfo = new MainUserInfoWizardPage(userAdminService);
		addPage(mainUserInfo);

		chooseGroupsPage = new ChooseGroupsPage(session);
		addPage(chooseGroupsPage);
	}

	@Override
	public boolean performFinish() {
		if (!canFinish())
			return false;

		String username = mainUserInfo.getUsername();
		try {
			// Effective creation of the new user
			Node userProfile = jcrSecurityModel.sync(session, username, null);

			session.getWorkspace().getVersionManager()
					.checkout(userProfile.getPath());
			mainUserInfo.mapToProfileNode(userProfile);
			String password = mainUserInfo.getPassword();
			// TODO add roles
			JcrUserDetails jcrUserDetails = new JcrUserDetails(userProfile,
					password, new GrantedAuthority[0]);
			session.save();
			session.getWorkspace().getVersionManager()
					.checkin(userProfile.getPath());
			userAdminService.createUser(jcrUserDetails);
			return true;
		} catch (Exception e) {
			JcrUtils.discardQuietly(session);
			Node userHome = UserJcrUtils.getUserHome(session, username);
			if (userHome != null) {
				try {
					userHome.remove();
					session.save();
				} catch (RepositoryException e1) {
					JcrUtils.discardQuietly(session);
					log.warn("Error when trying to clean up failed new user "
							+ username, e1);
				}
			}
			ErrorFeedback.show("Cannot create new user " + username, e);
			return false;
		}
	}

	public void setSession(Session session) {
		this.session = session;
	}

	/**
	 * Displays a list of groups with a check box to be able to choose some of
	 * them
	 */
	protected class ChooseGroupsPage extends WizardPage implements
			IPageChangedListener {
		private static final long serialVersionUID = 1L;
		private UserGroupTableComposite userTableCmp;
		private Composite container;
		private Session session;

		public ChooseGroupsPage(Session session) {
			super("Choose groups");
			this.session = session;
			setTitle("Assign the new user to some business groups");
		}

		@Override
		public void createControl(Composite parent) {
			container = new Composite(parent, SWT.NONE);
			container.setLayout(new FillLayout());
			userTableCmp = new UserGroupTableComposite(container, SWT.NO_FOCUS,
					session);
			userTableCmp.populate(true, true);
			setControl(container);

			// Add listener to update message when shown
			final IWizardContainer container = this.getContainer();
			if (container instanceof IPageChangeProvider) {
				((IPageChangeProvider) container).addPageChangedListener(this);
			}

		}

		@Override
		public void pageChanged(PageChangedEvent event) {
			if (event.getSelectedPage() == this) {
				String msg = "Add user to group ";
				((WizardPage) event.getSelectedPage()).setMessage(msg);
			}
		}

		protected List<Node> getSelectedUsers() {
			return userTableCmp.getSelectedUsers();
		}

		// private class MyUserTableCmp extends UserTableComposite {
		//
		// private static final long serialVersionUID = 1L;
		//
		// public MyUserTableCmp(Composite parent, int style, Session session) {
		// super(parent, style, session);
		// }
		//
		// @Override
		// protected void refreshFilteredList() {
		// List<Node> nodes = new ArrayList<Node>();
		// try {
		// NodeIterator ni = listFilteredElements(session,
		// getFilterString());
		//
		// users: while (ni.hasNext()) {
		// Node currNode = ni.nextNode();
		// String username = currNode.hasProperty(ARGEO_USER_ID) ? currNode
		// .getProperty(ARGEO_USER_ID).getString() : "";
		// if (username.equals(session.getUserID()))
		// continue users;
		// else
		// nodes.add(currNode);
		// }
		// getTableViewer().setInput(nodes.toArray());
		// } catch (RepositoryException e) {
		// throw new ArgeoException("Unable to list users", e);
		// }
		// }
		// }
	}

}
