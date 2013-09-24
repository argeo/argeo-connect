package org.argeo.connect.people.ui.wizards;


public class CreateNewPositionWizard{}

// /**
// * Generic one page wizard to add a new position to a given Node. Provides a
// * search field to reduce to displayed list. The caller might set a parent
// node
// * to reduce the scope of the search.
// */
//
// public class CreateNewPositionWizard extends AddEntityReferenceWizard {
// private Node targetParNode;
//
// public CreateNewPositionWizard(PeopleService peopleService, Node person) {
// super(peopleService);
// this.targetParNode = person;
// setWindowTitle("Add a new position.");
// }
//
// @Override
// protected String getCurrDescription() {
// String desc = "Select some selected films to add to the current program.";
// return desc;
// }
//
// /**
// * Called by the wizard performFinish() method. Overwrite to perform real
// * addition of new items to a given Node depending on its nature, dealing
// * with duplicate and check out state among others.
// */
// @Override
// protected boolean addChildren(List<Node> newChildren)
// throws RepositoryException {
//
// // Specific addition of items to a given program
// // current table display selected items for the given edition
//
// boolean wasCheckedOut = CommonsJcrUtils
// .isNodeCheckedOutByMe(targetParNode);
// if (!wasCheckedOut)
// CommonsJcrUtils.checkout(targetParNode);
// Node itemsPar = targetParNode.getNode(MsmNames.MSM_PROG_ITEMS);
//
// for (Node selectedItem : newChildren) {
// String refUid = selectedItem.getProperty(MsmNames.MSM_FILM_REF_UID)
// .getString();
// Node node = getPeopleService().getEntityByUid(getSession(), refUid);
//
// if (itemsPar.hasNode(node.getName())) {
// // TODO manage duplication, we do nothing for the time being
// } else {
// Node link = itemsPar.addNode(node.getName(), MsmTypes.MSM_ITEM);
// String nodeUid = node.getProperty(PeopleNames.PEOPLE_UID)
// .getString();
// link.setProperty(MsmNames.MSM_REF_TYPE, node
// .getPrimaryNodeType().getName());
// link.setProperty(PeopleNames.PEOPLE_REF_UID, nodeUid);
// }
// }
// if (wasCheckedOut)
// targetParNode.getSession().save();
// else
// CommonsJcrUtils.saveAndCheckin(targetParNode);
// return true;
// }
//
// @Override
// protected void refreshFilteredList() {
// // TODO find a better way to insure all checked items are displayed
// getSelectedItems().clear();
// try {
// String filter = filterTxt.getText();
// QueryManager queryManager = getSession().getWorkspace()
// .getQueryManager();
// QueryObjectModelFactory factory = queryManager.getQOMFactory();
//
// Selector mainSlct = factory.selector(MsmTypes.MSM_SELECTED_FILM,
// "mainSlct");
// // Selector mainSlct = factory.selector(nodeType, "mainSlct");
// Selector refSlct = factory.selector(FilmTypes.FILM, "refSlct");
// EquiJoinCondition joinCond = factory.equiJoinCondition(
// mainSlct.getSelectorName(), MsmNames.MSM_FILM_REF_UID,
// refSlct.getSelectorName(), PeopleNames.PEOPLE_UID);
// Source jointSrc = factory.join(mainSlct, refSlct,
// QueryObjectModelConstants.JCR_JOIN_TYPE_LEFT_OUTER,
// joinCond);
//
// // Only show selected films for current edition
// Constraint defaultC = factory.descendantNode(
// mainSlct.getSelectorName(), selectedFilmsPath);
//
// // Parse the String
// String[] strs = filter.trim().split(" ");
// if (strs.length == 0) {
// StaticOperand so = factory.literal(getSession()
// .getValueFactory().createValue("*"));
// Constraint currC = factory.fullTextSearch(
// refSlct.getSelectorName(), null, so);
// defaultC = factory.and(defaultC, currC);
// } else {
// for (String token : strs) {
// StaticOperand so = factory.literal(getSession()
// .getValueFactory().createValue("*" + token + "*"));
// Constraint currC = factory.fullTextSearch(
// refSlct.getSelectorName(), null, so);
// if (defaultC == null)
// defaultC = currC;
// else
// defaultC = factory.and(defaultC, currC);
// }
// }
// QueryObjectModel query;
// query = factory.createQuery(jointSrc, defaultC, null, null);
// query.setLimit(PeopleConstants.QUERY_DEFAULT_LIMIT);
// QueryResult result = query.execute();
// RowIterator ri = result.getRows();
// itemsViewer.setInput(CommonsJcrUtils.rowIteratorToList(ri,
// mainSlct.getSelectorName()));
// } catch (RepositoryException e) {
// throw new MsmException("Unable to list persons", e);
// }
// }
//
// /**
// * Overwrite to provide the correct Label provider depending on the
// * currently being added type of entities
// */
// @Override
// protected EntitySingleColumnLabelProvider defineLabelProvider() {
// return new MsmEntitySingleColLabelProv(getPeopleService());
// }
// }
