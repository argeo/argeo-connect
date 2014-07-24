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
package org.argeo.connect.people.ui.dialogs;

/**
 * Dialog with a filtered list to add some members in a mailing list
 */
public class AddMLMembersDialog {
	// extends AddReferenceDialog {
	// }
	// private static final long serialVersionUID = 5641280645351822123L;

	// // business objects
	// private Node referencingNode;
	//
	// public AddMLMembersDialog(Shell parentShell, String title,
	// Repository repository, Node referencingNode,
	// String[] toSearchNodeTypes) {
	// super(parentShell, title, repository, toSearchNodeTypes);
	// this.referencingNode = referencingNode;
	// }
	//
	// protected Point getInitialSize() {
	// return new Point(450, 600);
	// }
	//
	// @Override
	// protected List<ColumnDefinition> getColumnsDef() {
	// List<ColumnDefinition> columnDefs = new ArrayList<ColumnDefinition>();
	// columnDefs.add(new ColumnDefinition(PeopleTypes.PEOPLE_ENTITY,
	// Property.JCR_TITLE, PropertyType.STRING, "Display name", 380));
	// return columnDefs;
	// }
	//
	// @Override
	// protected boolean performAddition(List<Row> selectedItems) {
	// // TODO implement sanity checks
	// // if (duplicates.length() > 0) {
	// // String msg = "Following persons are already part of the list, "
	// // + "they could not be added: \n"
	// // + duplicates.substring(0, duplicates.length() - 2);
	// // MessageDialog.openError(getShell(), "Dupplicates", msg);
	// // }
	// try {
	// for (Row personRow : selectedItems) {
	// Node contactable = personRow.getNode(PeopleTypes.PEOPLE_ENTITY);
	// ContactJcrUtils.addToMailingList(referencingNode, contactable);
	// }
	// return true;
	// } catch (RepositoryException e) {
	// throw new PeopleException("Error while trying to add members to "
	// + referencingNode, e);
	// }
	// }
	//
	// protected RowIterator refreshFilteredList(String filter, String nodeType)
	// {
	// try {
	// Session session = getSession();
	//
	// QueryManager queryManager = session.getWorkspace()
	// .getQueryManager();
	// QueryObjectModelFactory factory = queryManager.getQOMFactory();
	// Selector source = factory.selector(PeopleTypes.PEOPLE_ENTITY,
	// PeopleTypes.PEOPLE_ENTITY);
	// Selector contactable = factory.selector(
	// PeopleTypes.PEOPLE_CONTACTABLE,
	// PeopleTypes.PEOPLE_CONTACTABLE);
	//
	// SameNodeJoinCondition joinCond = factory.sameNodeJoinCondition(
	// source.getSelectorName(), contactable.getSelectorName(),
	// ".");
	// Source jointSrc = factory.join(source, contactable,
	// QueryObjectModelConstants.JCR_JOIN_TYPE_INNER, joinCond);
	//
	// // no Default Constraint
	// Constraint defaultC = null;
	//
	// // Parse the String
	// String[] strs = filter.trim().split(" ");
	// if (strs.length == 0) {
	// StaticOperand so = factory.literal(session.getValueFactory()
	// .createValue("*"));
	// defaultC = factory.fullTextSearch(source.getSelectorName(),
	// null, so);
	// } else {
	// for (String token : strs) {
	// StaticOperand so = factory.literal(session
	// .getValueFactory().createValue("*" + token + "*"));
	// Constraint currC = factory.fullTextSearch(
	// source.getSelectorName(), null, so);
	// if (defaultC == null)
	// defaultC = currC;
	// else
	// defaultC = factory.and(defaultC, currC);
	// }
	// }
	//
	// // Order by default by JCR TITLE
	// Ordering order = factory
	// .ascending(factory.upperCase(factory.propertyValue(
	// source.getSelectorName(), Property.JCR_TITLE)));
	// QueryObjectModel query;
	// query = factory.createQuery(jointSrc, defaultC,
	// new Ordering[] { order }, null);
	// // query = factory.createQuery(jointSrc, null, null, null);
	// query.setLimit(PeopleConstants.QUERY_DEFAULT_LIMIT);
	//
	// QueryResult result = query.execute();
	// return result.getRows();
	// } catch (RepositoryException e) {
	// throw new PeopleException("Unable to list entities", e);
	// }
	// }

}