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
package org.argeo.connect.people.ui.editors;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.UserManagementService;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.jcr.ArgeoNames;
import org.argeo.jcr.JcrUtils;
import org.argeo.jcr.UserJcrUtils;
import org.argeo.security.UserAdminService;
import org.argeo.security.jcr.JcrUserDetails;
import org.argeo.security.ui.admin.editors.ArgeoUserEditorInput;
import org.argeo.security.ui.admin.editors.DefaultUserMainPage;
import org.argeo.security.ui.admin.editors.UserRolesPage;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.userdetails.UserDetails;

/** People specific user Editor. Adds among other group management. */
public class UserEditor extends FormEditor {
	// private final static Log log = LogFactory.getLog(UserEditor.class);
	private static final long serialVersionUID = 270486756895365730L;

	public final static String ID = PeopleUiPlugin.PLUGIN_ID + ".userEditor";

	private JcrUserDetails userDetails;
	private Node userProfile;
	private UserAdminService userAdminService;
	private UserManagementService userManagementService;
	private Session session;

	// Pages
	private DefaultUserMainPage defaultUserMainPage;
	private UserGroupsPage userGroupsPage;
	private UserRolesPage userRolesPage;

	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		String username = "";

		username = ((ArgeoUserEditorInput) getEditorInput()).getUsername();
		userProfile = UserJcrUtils.getUserProfile(session, username);

		if (userAdminService.userExists(username)) {
			userDetails = (JcrUserDetails) userAdminService
					.loadUserByUsername(username);
		} else {
			GrantedAuthority[] authorities = {};
			try {
				userDetails = new JcrUserDetails(session, username, null,
						authorities);
			} catch (RepositoryException e) {
				throw new ArgeoException("Cannot retrieve disabled JCR profile");
			}
		}

		this.setPartProperty("name", username != null ? username : "<new user>");
		setPartName(username != null ? username : "<new user>");
	}

	protected void addPages() {
		try {
			defaultUserMainPage = new DefaultUserMainPage(this, userProfile);
			addPage(defaultUserMainPage);

			userGroupsPage = new UserGroupsPage(this, userDetails,
					userManagementService);
			addPage(userGroupsPage);

			userRolesPage = new UserRolesPage(this, userDetails,
					userAdminService);
			addPage(userRolesPage);

		} catch (Exception e) {
			throw new ArgeoException("Cannot add pages", e);
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		// list pages

		if (defaultUserMainPage.isDirty()) {
			defaultUserMainPage.doSave(monitor);
			String newPassword = defaultUserMainPage.getNewPassword();
			defaultUserMainPage.resetNewPassword();
			if (newPassword != null)
				userDetails = userDetails.cloneWithNewPassword(newPassword);
		}

		if (userRolesPage.isDirty()) {
			userRolesPage.doSave(monitor);
			userDetails = userDetails.cloneWithNewRoles(userRolesPage
					.getRoles());
		}

		userAdminService.updateUser(userDetails);
		firePropertyChange(PROP_DIRTY);

		userRolesPage.setUserDetails(userDetails);
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	public void refresh() {
		userRolesPage.refresh();
	}

	@Override
	public void dispose() {
		JcrUtils.logoutQuietly(session);
		super.dispose();
	}

	/* Local classes */
	private class UserGroupsPage extends FormPage implements ArgeoNames {
		private final Log log = LogFactory.getLog(UserGroupsPage.class);

		private TableViewer rolesViewer;
		private UserManagementService userManagementService;
		private List<String> roles;

		public UserGroupsPage(FormEditor editor, UserDetails userDetails,
				UserManagementService userManagementService) {
			super(editor, ID, "Roles");
			this.userManagementService = userManagementService;
		}

		public void setUserDetails(UserDetails userDetails) {
			this.roles = new ArrayList<String>();
			for (GrantedAuthority ga : userDetails.getAuthorities())
				roles.add(ga.getAuthority());
			if (rolesViewer != null)
				rolesViewer.refresh();
		}

		protected void createFormContent(final IManagedForm mf) {
			ScrolledForm form = mf.getForm();
			form.setText("Roles");
			FillLayout mainLayout = new FillLayout();
			// ColumnLayout mainLayout = new ColumnLayout();
			// mainLayout.minNumColumns = 1;
			// mainLayout.maxNumColumns = 4;
			// mainLayout.topMargin = 0;
			// mainLayout.bottomMargin = 5;
			// mainLayout.leftMargin = mainLayout.rightMargin =
			// mainLayout.horizontalSpacing = mainLayout.verticalSpacing = 10;
			form.getBody().setLayout(mainLayout);
			createRolesPart(form.getBody());
		}

		/** Creates the role section */
		protected void createRolesPart(Composite parent) {
			Table table = new Table(parent, SWT.MULTI | SWT.H_SCROLL
					| SWT.V_SCROLL);

			AbstractFormPart part = new AbstractFormPart() {
				public void commit(boolean onSave) {
					// roles have already been modified in editing
					super.commit(onSave);
					if (log.isTraceEnabled())
						log.trace("Role part committed");
				}
			};
			getManagedForm().addPart(part);

			// GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
			// gridData.verticalSpan = 20;
			// table.setLayoutData(gridData);
			table.setLinesVisible(true);
			table.setHeaderVisible(false);
			rolesViewer = new TableViewer(table);

			// check column
			TableViewerColumn column = createTableViewerColumn(rolesViewer,
					"checked", 20);
			column.setLabelProvider(new ColumnLabelProvider() {
				public String getText(Object element) {
					return null;
				}

				public Image getImage(Object element) {
					String role = element.toString();
					return null;

				}
			});
			column.setEditingSupport(new RoleEditingSupport(rolesViewer, part));

			// role column
			column = createTableViewerColumn(rolesViewer, "Role", 200);
			column.setLabelProvider(new ColumnLabelProvider() {
				public String getText(Object element) {
					return element.toString();
				}

				public Image getImage(Object element) {
					return null;
				}
			});
			rolesViewer.setContentProvider(new RolesContentProvider());
			rolesViewer.setInput(getEditorSite());
		}

		protected TableViewerColumn createTableViewerColumn(TableViewer viewer,
				String title, int bound) {
			final TableViewerColumn viewerColumn = new TableViewerColumn(
					viewer, SWT.NONE);
			final TableColumn column = viewerColumn.getColumn();
			column.setText(title);
			column.setWidth(bound);
			column.setResizable(true);
			column.setMoveable(true);
			return viewerColumn;

		}

		public List<String> getRoles() {
			return roles;
		}

		public void refresh() {
			rolesViewer.refresh();
		}

		private class RolesContentProvider implements
				IStructuredContentProvider {
			public Object[] getElements(Object inputElement) {
				return userAdminService.listEditableRoles().toArray();
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
			}
		}

		/** Select the columns by editing the checkbox in the first column */
		class RoleEditingSupport extends EditingSupport {

			private final TableViewer viewer;
			private final AbstractFormPart formPart;

			public RoleEditingSupport(TableViewer viewer,
					AbstractFormPart formPart) {
				super(viewer);
				this.viewer = viewer;
				this.formPart = formPart;
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return new CheckboxCellEditor(null, SWT.CHECK | SWT.READ_ONLY);

			}

			@Override
			protected boolean canEdit(Object element) {
				return true;
			}

			@Override
			protected Object getValue(Object element) {
				String role = element.toString();
				return roles.contains(role);

			}

			@Override
			protected void setValue(Object element, Object value) {
				Boolean inRole = (Boolean) value;
				String role = element.toString();
				if (inRole && !roles.contains(role)) {
					roles.add(role);
					formPart.markDirty();
				} else if (!inRole && roles.contains(role)) {
					roles.remove(role);
					formPart.markDirty();
				}
				viewer.refresh();
			}
		}
	}

	/* DEPENDENCY INJECTION */
	public void setUserAdminService(UserAdminService userAdminService) {
		this.userAdminService = userAdminService;
	}

	public void setRepository(Repository repository) {
		this.session = CommonsJcrUtils.login(repository);
	}

	public void setPeopleService(PeopleService peopleService) {
		userManagementService = peopleService.getUserManagementService();
	}

}