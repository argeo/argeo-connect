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
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.query.Row;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.extracts.ColumnDefinition;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.PeopleJcrUtils;
import org.argeo.connect.people.utils.PersonJcrUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog with a filtered list to add some members in a mailing list
 */
public class AddMLMembershipDialog extends AddReferenceDialog {

	private static final long serialVersionUID = 7681423439719164047L;
	// business objects
	private Node referencedNode;
	private PeopleService peopleService;

	public AddMLMembershipDialog(Shell parentShell, String title,
			PeopleService peopleService, Node referencedNode,
			String[] toSearchNodeTypes) {
		super(parentShell, title, peopleService, toSearchNodeTypes);
		this.referencedNode = referencedNode;
		this.peopleService = peopleService;
	}

	protected List<ColumnDefinition> getColumnsDef() {
		List<ColumnDefinition> columnDefs = new ArrayList<ColumnDefinition>();
		columnDefs.add(new ColumnDefinition(PeopleTypes.PEOPLE_MAILING_LIST,
				Property.JCR_TITLE, PropertyType.STRING, "Title", 100));
		columnDefs.add(new ColumnDefinition(PeopleTypes.PEOPLE_MAILING_LIST,
				Property.JCR_DESCRIPTION, PropertyType.STRING, "Description",
				300));
		return columnDefs;
	}

	protected boolean performAddition(List<Row> selectedItems) {
		StringBuilder duplicates = new StringBuilder();

		String defaultMail = PeopleJcrUtils.getDefaultContactValue(
				referencedNode, PeopleTypes.PEOPLE_EMAIL);

		if (CommonsJcrUtils.isEmptyString(defaultMail)) {
			String msg = "Current person has no defined primary mail adress, "
					+ "he could not be added to any mailing list";
			MessageDialog.openError(getShell(), "Non valid information", msg);
			return true;
		}

		try {
			for (Row mlRow : selectedItems) {
				Node mailingList = mlRow
						.getNode(PeopleTypes.PEOPLE_MAILING_LIST);
				Node members = mailingList.getNode(PeopleNames.PEOPLE_MEMBERS);

				if (members.hasNode(defaultMail)) {
					duplicates
							.append(PersonJcrUtils
									.getPersonDisplayName(referencedNode))
							.append("(" + defaultMail + "); ");
				} else {
					// Node createdNode =
					peopleService.createEntityReference(mailingList,
							referencedNode, defaultMail);
				}
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