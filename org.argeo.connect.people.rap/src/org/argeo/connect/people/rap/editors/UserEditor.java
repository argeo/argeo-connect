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
package org.argeo.connect.people.rap.editors;

public class UserEditor{}

// import java.util.ArrayList;
// import java.util.List;
//
// import javax.jcr.Node;
// import javax.jcr.NodeIterator;
// import javax.jcr.Property;
// import javax.jcr.Repository;
// import javax.jcr.RepositoryException;
// import javax.jcr.Session;
//
// import org.apache.commons.logging.Log;
// import org.apache.commons.logging.LogFactory;
// import org.argeo.ArgeoException;
// import org.argeo.connect.people.PeopleService;
// import org.argeo.connect.people.UserManagementService;
// import org.argeo.connect.people.rap.PeopleRapImages;
// import org.argeo.connect.people.rap.PeopleRapPlugin;
// import org.argeo.connect.people.rap.composites.UserGroupTableComposite;
// import org.argeo.connect.people.utils.CommonsJcrUtils;
// import org.argeo.eclipse.ui.EclipseUiUtils;
// import org.argeo.jcr.ArgeoNames;
// import org.argeo.jcr.JcrUtils;
// import org.argeo.jcr.UserJcrUtils;
// import org.argeo.security.UserAdminService;
// import org.argeo.security.jcr.JcrUserDetails;
// import org.argeo.security.ui.admin.editors.ArgeoUserEditorInput;
// import org.argeo.security.ui.admin.editors.DefaultUserMainPage;
// import org.argeo.security.ui.admin.editors.UserRolesPage;
// import org.eclipse.core.runtime.IProgressMonitor;
// import org.eclipse.jface.viewers.CellEditor;
// import org.eclipse.jface.viewers.CheckboxCellEditor;
// import org.eclipse.jface.viewers.ColumnLabelProvider;
// import org.eclipse.jface.viewers.DoubleClickEvent;
// import org.eclipse.jface.viewers.EditingSupport;
// import org.eclipse.jface.viewers.IDoubleClickListener;
// import org.eclipse.jface.viewers.IStructuredContentProvider;
// import org.eclipse.jface.viewers.IStructuredSelection;
// import org.eclipse.jface.viewers.TableViewer;
// import org.eclipse.jface.viewers.TableViewerColumn;
// import org.eclipse.jface.viewers.Viewer;
// import org.eclipse.swt.SWT;
// import org.eclipse.swt.graphics.Image;
// import org.eclipse.swt.layout.FillLayout;
// import org.eclipse.swt.layout.GridData;
// import org.eclipse.swt.widgets.Composite;
// import org.eclipse.swt.widgets.Label;
// import org.eclipse.swt.widgets.Table;
// import org.eclipse.swt.widgets.TableColumn;
// import org.eclipse.ui.IEditorInput;
// import org.eclipse.ui.IEditorSite;
// import org.eclipse.ui.PartInitException;
// import org.eclipse.ui.forms.AbstractFormPart;
// import org.eclipse.ui.forms.IManagedForm;
// import org.eclipse.ui.forms.editor.FormEditor;
// import org.eclipse.ui.forms.editor.FormPage;
// import org.eclipse.ui.forms.widgets.ScrolledForm;
// import org.springframework.security.core.GrantedAuthority;
//
// /** People specific user Editor. Adds among other group management. */
// public class UserEditor extends FormEditor {
// // private final static Log log = LogFactory.getLog(UserEditor.class);
// private static final long serialVersionUID = 270486756895365730L;
//
// public final static String ID = PeopleRapPlugin.PLUGIN_ID + ".userEditor";
//
// /* DEPENDENCY INJECTION */
// private UserAdminService userAdminService;
// private UserManagementService userManagementService;
// private Session session;
//
// // Local instance of the user model
// private Node userProfile;
// private JcrUserDetails userDetails;
// private String username;
//
// // Pages
// private DefaultUserMainPage defaultUserMainPage;
// private UserGroupsPage userGroupsPage;
// private UserRolesPage userRolesPage;
//
// public void init(IEditorSite site, IEditorInput input)
// throws PartInitException {
// super.init(site, input);
// username = "";
//
// // TODO simplify this: user is created using a wizard and always exists
// // on editor opening.
// username = ((ArgeoUserEditorInput) getEditorInput()).getUsername();
// userProfile = UserJcrUtils.getUserProfile(session, username);
//
// if (userAdminService.userExists(username)) {
// userDetails = (JcrUserDetails) userAdminService
// .loadUserByUsername(username);
// } else {
// List<GrantedAuthority> authoritiesList = new ArrayList<GrantedAuthority>();
// try {
// userDetails = new JcrUserDetails(session, username, null,
// authoritiesList);
// } catch (RepositoryException e) {
// throw new ArgeoException("Cannot retrieve disabled JCR profile");
// }
// }
// this.setPartProperty("name", username != null ? username : "<new user>");
// setPartName(username != null ? username : "<new user>");
// }
//
// protected void addPages() {
// try {
// defaultUserMainPage = new DefaultUserMainPage(this, userProfile);
// addPage(defaultUserMainPage);
//
// userGroupsPage = new UserGroupsPage(this, userManagementService,
// userProfile);
// addPage(userGroupsPage);
//
// userRolesPage = new UserRolesPage(this, userDetails,
// userAdminService);
// addPage(userRolesPage);
//
// } catch (Exception e) {
// throw new ArgeoException("Cannot add pages", e);
// }
// }
//
// @Override
// public void doSave(IProgressMonitor monitor) {
// // list pages
// if (defaultUserMainPage.isDirty()) {
// defaultUserMainPage.doSave(monitor);
// String newPassword = defaultUserMainPage.getNewPassword();
// defaultUserMainPage.resetNewPassword();
// if (newPassword != null)
// userDetails = userDetails.cloneWithNewPassword(newPassword);
// }
//
// if (userRolesPage.isDirty()) {
// userRolesPage.doSave(monitor);
// }
//
// userDetails = userDetails.cloneWithNewRoles(userRolesPage.getRoles());
// userAdminService.updateUser(userDetails);
//
// if (userGroupsPage.isDirty()) {
// userGroupsPage.doSave(monitor);
// userManagementService.addGroupsToUser(session, username,
// userGroupsPage.selectedGroups);
// }
//
// firePropertyChange(PROP_DIRTY);
// userRolesPage.setUserDetails(userDetails);
//
// }
//
// @Override
// public void doSaveAs() {
// }
//
// @Override
// public boolean isSaveAsAllowed() {
// return false;
// }
//
// public void refresh() {
// userRolesPage.refresh();
// }
//
// /* Local classes */
// private class UserGroupsPage extends FormPage implements ArgeoNames {
// private final Log log = LogFactory.getLog(UserGroupsPage.class);
//
// // This UI Objects
// private TableViewer groupsViewer;
// private AbstractFormPart part;
// private UserManagementService userManagementService;
//
// // Business objects
// // private Node argeoProfile;
// private List<Node> selectedGroups;
// // Keep a local cache for upper table
// private List<Node> displayedGroups = new ArrayList<Node>();
//
// public UserGroupsPage(FormEditor editor,
// UserManagementService userManagementService, Node argeoProfile) {
// super(editor, ID, "Groups");
// this.userManagementService = userManagementService;
// // this.argeoProfile = argeoProfile;
// }
//
// protected void createFormContent(final IManagedForm mf) {
// ScrolledForm form = mf.getForm();
// form.setText("Group Management");
// Composite body = form.getBody();
// body.setLayout(EclipseUiUtils.noSpaceGridLayout());
//
// Label lbl = new Label(body, SWT.NONE);
// lbl.setFont(EclipseUiUtils.getBoldFont(body));
// lbl.setText("Already assigned group");
// GridData gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
// gd.horizontalIndent = 20;
// lbl.setLayoutData(gd);
//
// Composite top = new Composite(body, SWT.NO_FOCUS);
// top.setLayout(new FillLayout());
// createSelectedGroupsPart(top);
// refresh();
// top.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
//
// lbl = new Label(body, SWT.NONE);
// lbl.setFont(EclipseUiUtils.getBoldFont(body));
// lbl.setText("Pick up some new group (by double click)");
// gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
// gd.verticalIndent = 10;
// gd.horizontalIndent = 20;
// lbl.setLayoutData(gd);
//
// final UserGroupTableComposite allGroupsCmp = new MyGroupTableComposite(
// body, SWT.NO_FOCUS, session);
// allGroupsCmp.populate(true, false);
// gd = new GridData(SWT.FILL, SWT.BOTTOM, true, false);
// gd.heightHint = 250;
// allGroupsCmp.setLayoutData(gd);
//
// allGroupsCmp.getTableViewer().addDoubleClickListener(
// new IDoubleClickListener() {
//
// @Override
// public void doubleClick(DoubleClickEvent event) {
// if (event.getSelection().isEmpty())
// return;
// Object obj = ((IStructuredSelection) event
// .getSelection()).getFirstElement();
// if (obj instanceof Node) {
// selectNewGroup((Node) obj);
// allGroupsCmp.refresh();
// }
// }
// });
// }
//
// private class MyGroupTableComposite extends UserGroupTableComposite {
// private static final long serialVersionUID = 1L;
//
// public MyGroupTableComposite(Composite parent, int style,
// Session session) {
// super(parent, style, session);
// }
//
// protected void refreshFilteredList() {
// List<Node> nodes = new ArrayList<Node>();
// try {
// NodeIterator nit = listFilteredElements(session,
// getFilterString());
//
// while (nit.hasNext()) {
// Node currNode = nit.nextNode();
// if (!contain(selectedGroups, currNode))
// nodes.add(currNode);
// }
// getTableViewer().setInput(nodes.toArray());
// } catch (RepositoryException e) {
// throw new ArgeoException("Unable to list users", e);
// }
// }
// }
//
// /** Creates the role section */
// protected void createSelectedGroupsPart(Composite parent) {
// Table table = new Table(parent, SWT.MULTI | SWT.H_SCROLL
// | SWT.V_SCROLL);
// part = new AbstractFormPart() {
// public void commit(boolean onSave) {
// super.commit(onSave);
// if (log.isTraceEnabled())
// log.trace("Group part committed");
// }
// };
// getManagedForm().addPart(part);
//
// table.setLinesVisible(true);
// table.setHeaderVisible(false);
// groupsViewer = new TableViewer(table);
//
// // check column
// TableViewerColumn column = createTableViewerColumn(groupsViewer,
// "checked", 20);
// column.setLabelProvider(new ColumnLabelProvider() {
// private static final long serialVersionUID = 1L;
//
// public String getText(Object element) {
// return null;
// }
//
// public Image getImage(Object element) {
// Node currNode = (Node) element;
//
// if (selectedGroups.contains(currNode))
// return PeopleRapImages.ROLE_CHECKED;
// else
// return null;
// }
// });
// column.setEditingSupport(new GroupEditingSupport(groupsViewer, part));
//
// // role column
// column = createTableViewerColumn(groupsViewer, "Role", 200);
// column.setLabelProvider(new ColumnLabelProvider() {
// private static final long serialVersionUID = 1L;
//
// public String getText(Object element) {
// return CommonsJcrUtils.get((Node) element,
// Property.JCR_TITLE);
// }
//
// public Image getImage(Object element) {
// return null;
// }
// });
// groupsViewer.setContentProvider(new BasicContentProvider());
// }
//
// protected TableViewerColumn createTableViewerColumn(TableViewer viewer,
// String title, int bound) {
// final TableViewerColumn viewerColumn = new TableViewerColumn(
// viewer, SWT.NONE);
// final TableColumn column = viewerColumn.getColumn();
// column.setText(title);
// column.setWidth(bound);
// column.setResizable(true);
// column.setMoveable(true);
// return viewerColumn;
//
// }
//
// // public List<Node> getSelectedGroups() {
// // return selectedGroups;
// // }
//
// public void refresh() {
// selectedGroups = userManagementService.getUserGroups(session,
// username);
// displayedGroups.clear();
// displayedGroups.addAll(selectedGroups);
// groupsViewer.setInput(displayedGroups.toArray());
// groupsViewer.refresh();
// }
//
// public void selectNewGroup(Node newGroup) {
// if (!contain(selectedGroups, newGroup)) {
// selectedGroups.add(newGroup);
// part.markDirty();
// }
// if (!contain(displayedGroups, newGroup))
// displayedGroups.add(newGroup);
// groupsViewer.setInput(displayedGroups.toArray());
// }
//
// private boolean contain(List<Node> list, Node node) {
// try {
// String path = node.getPath();
// for (Node currNode : list) {
// if (currNode.getPath().equals(path))
// return true;
// }
// return false;
// } catch (RepositoryException e) {
// throw new ArgeoException("Unable to check inclusion", e);
// }
// }
//
// /** Select the columns by editing the checkbox in the first column */
// class GroupEditingSupport extends EditingSupport {
// private static final long serialVersionUID = 1L;
//
// private final TableViewer viewer;
// private final AbstractFormPart formPart;
//
// public GroupEditingSupport(TableViewer viewer,
// AbstractFormPart formPart) {
// super(viewer);
// this.viewer = viewer;
// this.formPart = formPart;
// }
//
// @Override
// protected CellEditor getCellEditor(Object element) {
// return new CheckboxCellEditor(null, SWT.CHECK | SWT.READ_ONLY);
//
// }
//
// @Override
// protected boolean canEdit(Object element) {
// return true;
// }
//
// @Override
// protected Object getValue(Object element) {
// return selectedGroups.contains((Node) element);
// }
//
// @Override
// protected void setValue(Object element, Object value) {
// Boolean inGroup = (Boolean) value;
// Node group = (Node) element;
// if (inGroup && !selectedGroups.contains(group)) {
// selectedGroups.add(group);
// formPart.markDirty();
// } else if (!inGroup && selectedGroups.contains(group)) {
// selectedGroups.remove(group);
// formPart.markDirty();
// }
// viewer.refresh();
// }
// }
//
// private class BasicContentProvider implements
// IStructuredContentProvider {
// private static final long serialVersionUID = 1L;
//
// public Object[] getElements(Object inputElement) {
// return (Object[]) inputElement;
// }
//
// public void dispose() {
// }
//
// public void inputChanged(Viewer viewer, Object oldInput,
// Object newInput) {
// }
// }
// }
//
// @Override
// public void dispose() {
// JcrUtils.logoutQuietly(session);
// super.dispose();
// }
//
// /* DEPENDENCY INJECTION */
// public void setUserAdminService(UserAdminService userAdminService) {
// this.userAdminService = userAdminService;
// }
//
// public void setRepository(Repository repository) {
// this.session = CommonsJcrUtils.login(repository);
// }
//
// public void setPeopleService(PeopleService peopleService) {
// userManagementService = peopleService.getUserManagementService();
// }
// }