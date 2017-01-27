package org.argeo.connect.people.workbench.rap.editors;


public class UserGroupEditor {}

// public class UserGroupEditor extends GroupEditor {
//
// public final static String ID = PeopleRapPlugin.PLUGIN_ID
// + ".userGroupEditor";
//
// private JcrUsersTable userTableCmp;
//
// // Main business Objects
// private Node group;
//
// public void init(IEditorSite site, IEditorInput input)
// throws PartInitException {
// super.init(site, input);
// group = getNode();
// }
//
// @Override
// protected void populateTabFolder(CTabFolder folder) {
// // The member list
// String tooltip = "Members of group "
// + JcrUtils.get(group, Property.JCR_TITLE);
// Composite innerPannel = addTabToFolder(folder, CTAB_COMP_STYLE,
// "Members", PeopleRapConstants.CTAB_MEMBERS, tooltip);
// createBottomPart(innerPannel);
// }
//
// protected void createBottomPart(Composite parent) {
// parent.setLayout(new FillLayout());
//
// // Create the composite that displays the list and a filter
// userTableCmp = new MyUsersTable(parent, SWT.NO_FOCUS, getSession());
// userTableCmp.populate(true, false);
//
// // Configure
// getSite().setSelectionProvider(userTableCmp.getTableViewer());
// }
//
// private class MyUsersTable extends JcrUsersTable {
// private static final long serialVersionUID = 1L;
//
// public MyUsersTable(Composite parent, int style, Session session) {
// super(parent, style, session);
// }
//
// protected void refreshFilteredList() {
// List<Node> nodes = new ArrayList<Node>();
// try {
// PropertyIterator pit = group
// .getReferences(PeopleNames.PEOPLE_USER_GROUPS);
// while (pit.hasNext()) {
// Property prop = pit.nextProperty();
// Node parent = prop.getParent().getParent();
// log.debug("Parent Name " + parent.getName());
// nodes.add(parent.getNode(ArgeoNames.ARGEO_PROFILE));
// }
// getTableViewer().setInput(nodes.toArray());
// } catch (RepositoryException e) {
// throw new ArgeoException("Unable to list users", e);
// }
// }
//
// }
//
// }
