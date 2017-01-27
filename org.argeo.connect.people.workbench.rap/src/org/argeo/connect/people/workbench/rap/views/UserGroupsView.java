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
package org.argeo.connect.people.workbench.rap.views;

public class UserGroupsView {
}

//
/// ** Display a filtered table with user groups. */
// public class UserGroupsView extends ViewPart implements Refreshable {
// public final static String ID = PeopleRapPlugin.PLUGIN_ID
// + ".userGroupsView";
//
// /* DEPENDENCY INJECTION */
// private Repository repository;
//
// private Session session;
// private PeopleWorkbenchService peopleWorkbenchService;
//
// private JcrUserListener userStructureListener;
// private JcrUserListener userPropertiesListener;
// private UserGroupTableComposite userTableCmp;
//
// @Override
// public void createPartControl(Composite parent) {
// this.session = ConnectJcrUtils.login(repository);
//
// parent.setLayout(new FillLayout());
//
// // Create the composite that displays the list and a filter
// userTableCmp = new UserGroupTableComposite(parent, SWT.NO_FOCUS,
// session);
// userTableCmp.populate(true, false);
//
// // Configure
// userTableCmp.getTableViewer().addDoubleClickListener(
// new PeopleJcrViewerDClickListener(peopleWorkbenchService));
// getViewSite().setSelectionProvider(userTableCmp.getTableViewer());
//
// // Add listener to refresh the list when something changes
// userStructureListener = new JcrUserListener(getSite().getShell()
// .getDisplay());
// JcrUtils.addListener(session, userStructureListener, Event.NODE_ADDED
// | Event.NODE_REMOVED, getGroupsBasePath(), null);
//
// userPropertiesListener = new JcrUserListener(getSite().getShell()
// .getDisplay());
// JcrUtils.addListener(session, userStructureListener,
// Event.PROPERTY_CHANGED | Event.PROPERTY_ADDED
// | Event.PROPERTY_REMOVED, getGroupsBasePath(),
// PeopleTypes.PEOPLE_USER_GROUP);
// }
//
// @Override
// public void setFocus() {
// userTableCmp.setFocus();
// }
//
// @Override
// public void dispose() {
// JcrUtils.removeListenerQuietly(session, userStructureListener);
// JcrUtils.removeListenerQuietly(session, userPropertiesListener);
// JcrUtils.logoutQuietly(session);
// super.dispose();
// }
//
// @Override
// public void forceRefresh(Object object) {
// refresh();
// }
//
// public void refresh() {
// this.getSite().getShell().getDisplay().asyncExec(new Runnable() {
// @Override
// public void run() {
// userTableCmp.refresh();
// }
// });
// }
//
// private class JcrUserListener implements EventListener {
// private final Display display;
//
// public JcrUserListener(Display display) {
// super();
// this.display = display;
// }
//
// @Override
// public void onEvent(EventIterator events) {
// display.asyncExec(new Runnable() {
// @Override
// public void run() {
// userTableCmp.refresh();
// }
// });
// }
// }
//
// @Deprecated
// private final static String PEOPLE_BASE_PATH = "/argeo:system/argeo:people";
// /**
// * Overwrite to provide an application specific base path for the user
// * groups
// *
// * @return
// */
// @Deprecated
// protected String getGroupsBasePath() {
// return PEOPLE_BASE_PATH;
// }
//
// /* DEPENDENCY INJECTION */
// public void setRepository(Repository repository) {
// this.repository = repository;
// }
//
// public void setPeopleWorkbenchService(
// PeopleWorkbenchService peopleWorkbenchService) {
// this.peopleWorkbenchService = peopleWorkbenchService;
// }
//
// }