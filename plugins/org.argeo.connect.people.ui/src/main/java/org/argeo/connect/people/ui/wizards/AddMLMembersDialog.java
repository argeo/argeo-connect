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
import javax.jcr.Session;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.Ordering;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelConstants;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.SameNodeJoinCondition;
import javax.jcr.query.qom.Selector;
import javax.jcr.query.qom.Source;
import javax.jcr.query.qom.StaticOperand;

import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.extracts.ColumnDefinition;
import org.argeo.connect.people.utils.ContactJcrUtils;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog with a filtered list to add some members in a mailing list
 */
public class AddMLMembersDialog extends AddReferenceDialog {
	private static final long serialVersionUID = 5641280645351822123L;

	// business objects
	private Node referencingNode;

	// private PeopleService peopleService;

	public AddMLMembersDialog(Shell parentShell, String title,
			PeopleService peopleService, Node referencingNode,
			String[] toSearchNodeTypes) {
		super(parentShell, title, peopleService, toSearchNodeTypes);
		this.referencingNode = referencingNode;
		// this.peopleService = peopleService;
	}

	protected Point getInitialSize() {
		return new Point(800, 600);
	}

	@Override
	protected List<ColumnDefinition> getColumnsDef() {
		List<ColumnDefinition> columnDefs = new ArrayList<ColumnDefinition>();
		columnDefs.add(new ColumnDefinition(PeopleTypes.PEOPLE_ENTITY,
				Property.JCR_TITLE, PropertyType.STRING, "Display name", 400));
		// columnDefs.add(new ColumnDefinition(PeopleTypes.PEOPLE_PERSON,
		// PeopleNames.PEOPLE_FIRST_NAME, PropertyType.STRING,
		// "First name", 120));
		return columnDefs;
	}

	@Override
	protected boolean performAddition(List<Row> selectedItems) {
		// StringBuilder skippedPerson = new StringBuilder();
		// StringBuilder duplicates = new StringBuilder();

		try {
			for (Row personRow : selectedItems) {
				Node contactable = personRow.getNode(PeopleTypes.PEOPLE_ENTITY);
				ContactJcrUtils.addToMailingList(referencingNode, contactable);
			}

			// TODO implement sanity checks

			// if (skippedPerson.length() > 0) {
			// String msg = "Following persons have no defined mail adress, "
			// + "they could not be added: "
			// + skippedPerson
			// .substring(0, skippedPerson.length() - 2);
			// MessageDialog.openError(getShell(), "Non valid information",
			// msg);
			// }
			// if (duplicates.length() > 0) {
			// String msg = "Following persons are already part of the list, "
			// + "they could not be added: \n"
			// + duplicates.substring(0, duplicates.length() - 2);
			// MessageDialog.openError(getShell(), "Dupplicates", msg);
			// }
			return true;
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to get person node from row", e);
		}
	}

	protected RowIterator refreshFilteredList(String filter, String nodeType) {
		try {
			Session session = getSession();

			QueryManager queryManager = session.getWorkspace()
					.getQueryManager();
			QueryObjectModelFactory factory = queryManager.getQOMFactory();
			Selector source = factory.selector(PeopleTypes.PEOPLE_ENTITY,
					PeopleTypes.PEOPLE_ENTITY);
			Selector contactable = factory.selector(
					PeopleTypes.PEOPLE_CONTACTABLE,
					PeopleTypes.PEOPLE_CONTACTABLE);

			SameNodeJoinCondition joinCond = factory.sameNodeJoinCondition(
					source.getSelectorName(), contactable.getSelectorName(),
					".");
			Source jointSrc = factory.join(source, contactable,
					QueryObjectModelConstants.JCR_JOIN_TYPE_INNER, joinCond);

			// no Default Constraint
			Constraint defaultC = null;

			// Parse the String
			String[] strs = filter.trim().split(" ");
			if (strs.length == 0) {
				StaticOperand so = factory.literal(session.getValueFactory()
						.createValue("*"));
				defaultC = factory.fullTextSearch(source.getSelectorName(),
						null, so);
			} else {
				for (String token : strs) {
					StaticOperand so = factory.literal(session
							.getValueFactory().createValue("*" + token + "*"));
					Constraint currC = factory.fullTextSearch(
							source.getSelectorName(), null, so);
					if (defaultC == null)
						defaultC = currC;
					else
						defaultC = factory.and(defaultC, currC);
				}
			}

			// Order by default by JCR TITLE
			Ordering order = factory
					.ascending(factory.upperCase(factory.propertyValue(
							source.getSelectorName(), Property.JCR_TITLE)));
			QueryObjectModel query;
			query = factory.createQuery(jointSrc, defaultC,
					new Ordering[] { order }, null);
			// query = factory.createQuery(jointSrc, null, null, null);
			query.setLimit(PeopleConstants.QUERY_DEFAULT_LIMIT);

			QueryResult result = query.execute();
			return result.getRows();
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to list entities", e);
		}
	}

}