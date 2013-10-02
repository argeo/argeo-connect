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
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.query.Row;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.utils.ColumnDefinition;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.PeopleJcrUtils;
import org.argeo.connect.people.utils.PersonJcrUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog with a filtered list to add some members in a mailing list
 */
public class AddMLMembersDialog extends AddReferenceDialog {
	private static final long serialVersionUID = 5641280645351822123L;

	// business objects
	private Node referencingNode;
	private PeopleService peopleService;

	public AddMLMembersDialog(Shell parentShell, String title,
			PeopleService peopleService, Node referencingNode,
			String[] toSearchNodeTypes) {
		super(parentShell, title, peopleService, toSearchNodeTypes);
		this.referencingNode = referencingNode;
		this.peopleService = peopleService;
	}

	protected Point getInitialSize() {
		return new Point(800, 600);
	}

	@Override
	protected List<ColumnDefinition> getColumnsDef() {
		List<ColumnDefinition> columnDefs = new ArrayList<ColumnDefinition>();
		columnDefs.add(new ColumnDefinition(PeopleTypes.PEOPLE_PERSON,
				PeopleNames.PEOPLE_LAST_NAME, PropertyType.STRING, "Last name",
				120));
		columnDefs.add(new ColumnDefinition(PeopleTypes.PEOPLE_PERSON,
				PeopleNames.PEOPLE_FIRST_NAME, PropertyType.STRING,
				"First name", 120));
		return columnDefs;
	}
	
	@Override
	protected boolean performFinish() {
		StringBuilder skippedPerson = new StringBuilder();
		StringBuilder duplicates = new StringBuilder();

		try {
			Node members = referencingNode.getNode(PeopleNames.PEOPLE_MEMBERS);

			for (Row personRow : getSelectedItems()) {
				Node person = personRow.getNode(PeopleTypes.PEOPLE_PERSON);
				String defaultMail = PeopleJcrUtils.getDefaultContactValue(
						person, PeopleTypes.PEOPLE_EMAIL);
				if (CommonsJcrUtils.isEmptyString(defaultMail))
					skippedPerson.append(
							PersonJcrUtils.getPersonDisplayName(person))
							.append("; ");
				else if (members.hasNode(defaultMail)) {
					duplicates.append(
							PersonJcrUtils.getPersonDisplayName(person))
							.append("(" + defaultMail + "); ");
				} else {
					// Node createdNode =
					peopleService.createEntityReference(referencingNode,
							person, defaultMail);
				}
			}

			if (skippedPerson.length() > 0) {
				String msg = "Following persons have no defined mail adress, "
						+ "they could not be added: "
						+ skippedPerson
								.substring(0, skippedPerson.length() - 2);
				MessageDialog.openError(getShell(), "Non valid information",
						msg);
			}
			if (duplicates.length() > 0) {
				String msg = "Following persons are already part of the list, "
						+ "they could not be added: \n"
						+ duplicates.substring(0, duplicates.length() - 2);
				MessageDialog.openError(getShell(), "Dupplicates", msg);
			}
			return true;
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to get person node from row", e);
		}

	}
}