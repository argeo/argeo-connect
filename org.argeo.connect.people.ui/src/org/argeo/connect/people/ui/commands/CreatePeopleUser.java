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
package org.argeo.connect.people.ui.commands;

import javax.jcr.Repository;
import javax.jcr.Session;

import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.UserManagementService;
import org.argeo.connect.people.ui.wizards.NewPeopleUserWizard;
import org.argeo.jcr.JcrUtils;
import org.argeo.security.UserAdminService;
import org.argeo.security.jcr.JcrSecurityModel;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Launch a wizard that enables creation of a new People user, enable among
 * other group assignation.
 */

public class CreatePeopleUser extends AbstractHandler {
	/* DEPENDENCY INJECTION */
	private Repository repository;
	private UserAdminService userAdminService;
	private JcrSecurityModel jcrSecurityModel;
	// People specific service
	private UserManagementService userManagementService;

	public Object execute(ExecutionEvent event) throws ExecutionException {
		Session session = null;
		try {
			session = repository.login();
			NewPeopleUserWizard newUserWizard = new NewPeopleUserWizard(
					session, userAdminService, jcrSecurityModel,
					userManagementService);
			WizardDialog dialog = new WizardDialog(
					HandlerUtil.getActiveShell(event), newUserWizard);
			dialog.open();
		} catch (Exception e) {
			throw new ExecutionException("Cannot open wizard", e);
		} finally {
			JcrUtils.logoutQuietly(session);
		}
		return null;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setUserAdminService(UserAdminService userAdminService) {
		this.userAdminService = userAdminService;
	}

	public void setJcrSecurityModel(JcrSecurityModel jcrSecurityModel) {
		this.jcrSecurityModel = jcrSecurityModel;
	}

	public void setPeopleService(PeopleService peopleService) {
		this.userManagementService = peopleService.getUserManagementService();
	}
}