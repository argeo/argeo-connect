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
package org.argeo.connect.people.rap.commands;

import javax.jcr.Repository;
import javax.jcr.Session;

import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.rap.wizards.NewUserGroupWizard;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.handlers.HandlerUtil;

/** Launch a wizard that enables creation of a new user group. */
public class CreateUserGroup extends AbstractHandler {
	private Repository repository;
	private PeopleService peopleService;

	public Object execute(ExecutionEvent event) throws ExecutionException {
		Session session = null;
		try {
			session = repository.login();
			NewUserGroupWizard newUserWizard = new NewUserGroupWizard(session,
					peopleService);
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

	public void setPeopleService(PeopleService peopleService) {
		this.peopleService = peopleService;
	}
}
