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

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.UserManagementService;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.jcr.ArgeoNames;
import org.argeo.security.ui.admin.UserTableComposite;
import org.eclipse.jface.dialogs.IPageChangeProvider;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/** Wizard to create a new group and add users */
public class NewUserGroupWizard extends Wizard {
	// private final static Log log =
	// LogFactory.getLog(NewUserGroupWizard.class);

	private Session session;
	private UserManagementService userManagementService;

	// pages
	private MainInfoPage chooseCommandPage;
	private ChooseUsersWizardPage userListPage;
	private ValidateAndLaunchWizardPage validatePage;

	public NewUserGroupWizard(Session session, PeopleService peopleService) {
		this.userManagementService = peopleService.getUserManagementService();
		this.session = session;
	}

	@Override
	public void addPages() {
		chooseCommandPage = new MainInfoPage();
		addPage(chooseCommandPage);
		userListPage = new ChooseUsersWizardPage(session);
		addPage(userListPage);
		validatePage = new ValidateAndLaunchWizardPage(session);
		addPage(validatePage);

		setWindowTitle("Group creation");
		setNeedsProgressMonitor(true);
	}

	@Override
	public boolean performFinish() {
		if (!canFinish())
			return false;
		Node userGroup = userManagementService.createGroup(session,
				chooseCommandPage.getTitleValue(),
				chooseCommandPage.getTitleValue(),
				chooseCommandPage.getDescValue());

		userManagementService.addUsersToGroup(session, userGroup,
				userListPage.getSelectedUserNames());

		return true;
	}

	public boolean canFinish() {
		if (this.getContainer().getCurrentPage() == validatePage)
			return true;
		return false;
	}

	// //////////////////////
	// Pages definition
	/** Displays a combo box that enables user to choose which action to perform */
	private class MainInfoPage extends WizardPage {
		private static final long serialVersionUID = 1L;

		private Text titleTxt;
		private Text descTxt;

		public MainInfoPage() {
			super("Main Information");
			setTitle("Main Information");
			setMessage("Enter a name and a description for the group to create.");

		}

		@Override
		public void createControl(Composite parent) {
			Composite container = new Composite(parent, SWT.NO_FOCUS);
			container.setLayout(new GridLayout(2, false));
			container
					.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			titleTxt = createLP(container, "Title", "");
			titleTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
					false));

			Label lbl = new Label(container, SWT.NONE);
			lbl.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
			lbl.setText("Description");
			descTxt = new Text(container, SWT.BORDER | SWT.MULTI | SWT.WRAP);
			// descTxt.setText(value);
			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
			gd.heightHint = 100;
			descTxt.setLayoutData(gd);
			setControl(container);
		}

		/** Creates label and text. */
		protected Text createLP(Composite body, String label, String value) {
			Label lbl = new Label(body, SWT.NONE);
			lbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			lbl.setText(label);
			Text text = new Text(body, SWT.BORDER);
			text.setText(value);
			return text;
		}

		protected String getTitleValue() {
			return titleTxt.getText();
		}

		protected String getDescValue() {
			return descTxt.getText();
		}
	}

	/**
	 * Displays a list of users with a check box to be able to choose some of
	 * them
	 */
	private class ChooseUsersWizardPage extends WizardPage implements
			IPageChangedListener {
		private static final long serialVersionUID = 1L;
		private UserTableComposite userTableCmp;
		private Composite container;
		private Session session;

		public ChooseUsersWizardPage(Session session) {
			super("Choose Users");
			this.session = session;
			setTitle("Select users who will be impacted");
		}

		@Override
		public void createControl(Composite parent) {
			container = new Composite(parent, SWT.NONE);
			container.setLayout(new FillLayout());
			userTableCmp = new UserTableComposite(container, SWT.NO_FOCUS,
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
				String msg = "Add user to group "
						+ chooseCommandPage.getTitleValue();
				((WizardPage) event.getSelectedPage()).setMessage(msg);
			}
		}

		protected List<String> getSelectedUserNames() {
			List<Node> selectedNodes = userTableCmp.getSelectedUsers();

			if (selectedNodes.isEmpty())
				return null;

			List<String> usernames = new ArrayList<String>();
			for (Node currNode : selectedNodes) {
				usernames.add(CommonsJcrUtils.get(currNode,
						ArgeoNames.ARGEO_USER_ID));
			}
			return usernames;
		}

		protected List<Node> getSelectedUserNodes() {
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

	/**
	 * Recapitulation of input data before running real update
	 */
	private class ValidateAndLaunchWizardPage extends WizardPage implements
			IPageChangedListener {
		private static final long serialVersionUID = 1L;
		private UserTableComposite userTableCmp;
		private Session session;

		public ValidateAndLaunchWizardPage(Session session) {
			super("Validate and launch");
			this.session = session;
			setTitle("Validate and launch");
		}

		@Override
		public void createControl(Composite parent) {
			Composite mainCmp = new Composite(parent, SWT.NO_FOCUS);
			mainCmp.setLayout(new FillLayout());

			// Add listener to update user list when shown
			final IWizardContainer container = this.getContainer();
			if (container instanceof IPageChangeProvider) {
				((IPageChangeProvider) container).addPageChangedListener(this);
			}

			userTableCmp = new UserTableComposite(mainCmp, SWT.NO_FOCUS,
					session);
			userTableCmp.populate(false, false);
			setControl(mainCmp);
		}

		@Override
		public void pageChanged(PageChangedEvent event) {
			if (event.getSelectedPage() == this) {
				@SuppressWarnings({ "unchecked", "rawtypes" })
				Object[] values = ((ArrayList) userListPage
						.getSelectedUserNodes()).toArray(new Object[0]);
				// .toArray(new Object[userListPage.getSelectedUsers()
				// .size()]);
				userTableCmp.getTableViewer().setInput(values);
				String msg = "Group ["
						+ chooseCommandPage.getTitleValue()
						+ "] will be created and the users listed below will be added.\n"
						+ "Are you sure you want to proceed?";
				((WizardPage) event.getSelectedPage()).setMessage(msg);
			}
		}

		// private class MyUserTableCmp extends UserTableComposite {
		// public MyUserTableCmp(Composite parent, int style, Session session) {
		// super(parent, style, session);
		// }
		//
		// @Override
		// protected void refreshFilteredList() {
		// @SuppressWarnings({ "unchecked", "rawtypes" })
		//
		// setFilteredList(values);
		// }
		//
		// @Override
		// public void setVisible(boolean visible) {
		// super.setVisible(visible);
		// if (visible)
		// refreshFilteredList();
		// }
		// }
	}
}