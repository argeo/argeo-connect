package org.argeo.connect.people.rap.composites;

import java.util.ArrayList;
import java.util.List;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

import org.argeo.ArgeoException;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.UserAdminService;
import org.argeo.connect.people.rap.PeopleRapImages;
import org.argeo.connect.people.ui.PeopleColumnDefinition;
import org.argeo.connect.people.utils.UsersUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.eclipse.ui.utils.ViewerUtils;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.osgi.service.useradmin.Group;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;

/**
 * Generic composite that display a filter and a table viewer to display users
 * (can also be groups)
 * 
 * Warning: this class does not extends <code>TableViewer</code>. Use the
 * getTableViewer to acces it.
 * 
 */
public class GroupsTableViewer extends Composite {
	private static final long serialVersionUID = -7385959046279360420L;

	// Context
	private UserAdminService userAdminService;

	// Configuration
	private List<PeopleColumnDefinition> columnDefs = new ArrayList<PeopleColumnDefinition>();
	private boolean hasFilter;
	private boolean preventTableLayout = false;
	private boolean hasSelectionColumn;
	private int tableStyle;

	// Local UI Objects
	private TableViewer usersViewer;
	private Text filterTxt;

	/* EXPOSED METHODS */

	/**
	 * @param parent
	 * @param style
	 * @param userAdmin
	 */
	public GroupsTableViewer(Composite parent, int style,
			UserAdminService userAdminService) {
		super(parent, SWT.NO_FOCUS);
		this.tableStyle = style;
		this.userAdminService = userAdminService;

		columnDefs.add(new PeopleColumnDefinition("", new RoleIconLP(), 26));
		columnDefs.add(new PeopleColumnDefinition("Common Name",
				new CommonNameLP(), 150));
		columnDefs.add(new PeopleColumnDefinition("Domain", new DomainNameLP(),
				120));
		// columnDefs.add(new PeopleColumnDefinition("Distinguished Name",
		// new UserNameLP(), 300));

	}

	// // TODO workaround the bug of the table layout in the Form
	// public GroupsTableViewer(Composite parent, int style,
	// PeopleService peopleService, boolean preventTableLayout) {
	// super(parent, SWT.NO_FOCUS);
	// this.tableStyle = style;
	// userAdminService = peopleService.getUserAdminService();
	// this.preventTableLayout = preventTableLayout;
	// }
	//
	// /** This must be called before the call to populate method */
	// public void setColumnDefinitions(
	// List<PeopleColumnDefinition> columnDefinitions) {
	// this.columnDefs = columnDefinitions;
	// }

	/**
	 * 
	 * @param addFilter
	 *            choose to add a field to filter results or not
	 * @param addSelection
	 *            choose to add a column to select some of the displayed results
	 *            or not
	 */
	public void populate(boolean addFilter, boolean addSelection) {
		// initialization
		Composite parent = this;
		hasFilter = addFilter;
		hasSelectionColumn = addSelection;

		// Main Layout
		GridLayout layout = EclipseUiUtils.noSpaceGridLayout();
		layout.verticalSpacing = 5;
		this.setLayout(layout);
		if (hasFilter)
			createFilterPart(parent);

		Composite tableComp = new Composite(parent, SWT.NO_FOCUS);
		tableComp.setLayoutData(EclipseUiUtils.fillAll());
		usersViewer = createTableViewer(tableComp);

		usersViewer.setContentProvider(new UsersContentProvider());
	}

	/** Enable access to the selected users or groups */
	public List<User> getSelectedUsers() {
		if (hasSelectionColumn) {
			Object[] elements = ((CheckboxTableViewer) usersViewer)
					.getCheckedElements();

			List<User> result = new ArrayList<User>();
			for (Object obj : elements) {
				result.add((User) obj);
			}
			return result;
		} else
			throw new ArgeoException("Unvalid request: no selection column "
					+ "has been created for the current table");
	}

	/** Returns the User table viewer, typically to add doubleclick listener */
	public TableViewer getTableViewer() {
		return usersViewer;
	}

	/**
	 * Force the refresh of the underlying table using the current filter string
	 * if relevant
	 */
	public void refresh() {
		String filter = hasFilter ? filterTxt.getText() : null;
		if ("".equals(filter.trim()))
			filter = null;
		refreshFilteredList(filter);
	}

	/**
	 * Build repository request : caller might overwrite in order to display a
	 * subset of all users
	 */
	protected List<Group> listFilteredElements(String filter) {
		return userAdminService.listGroups(filter);
	}

	/* GENERIC COMPOSITE METHODS */
	@Override
	public boolean setFocus() {
		if (hasFilter)
			return filterTxt.setFocus();
		else
			return usersViewer.getTable().setFocus();
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	/* LOCAL CLASSES AND METHODS */
	// Will be usefull to rather use a virtual table viewer
	private void refreshFilteredList(String filter) {
		List<Group> users = listFilteredElements(filter);
		usersViewer.setInput(users.toArray());
	}

	private class UsersContentProvider implements IStructuredContentProvider {
		private static final long serialVersionUID = 1L;

		public Object[] getElements(Object inputElement) {
			return (Object[]) inputElement;
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	/* MANAGE FILTER */
	private void createFilterPart(Composite parent) {
		// Text Area for the filter
		filterTxt = new Text(parent, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH
				| SWT.ICON_CANCEL);
		filterTxt.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_FILL));
		filterTxt.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 1L;

			public void modifyText(ModifyEvent event) {
				refreshFilteredList(filterTxt.getText());
			}
		});
	}

	private TableViewer createTableViewer(final Composite parent) {

		int style = tableStyle | SWT.H_SCROLL | SWT.V_SCROLL;
		if (hasSelectionColumn)
			style = style | SWT.CHECK;
		Table table = new Table(parent, style);
		TableColumnLayout layout = new TableColumnLayout();

		// TODO the table layout does not works with the scrolled form

		if (preventTableLayout) {
			parent.setLayout(EclipseUiUtils.noSpaceGridLayout());
			table.setLayoutData(EclipseUiUtils.fillAll());
		} else
			parent.setLayout(layout);

		TableViewer viewer;
		if (hasSelectionColumn)
			viewer = new CheckboxTableViewer(table);
		else
			viewer = new TableViewer(table);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		TableViewerColumn column;
		int offset = 0;
		if (hasSelectionColumn) {
			offset = 1;
			column = ViewerUtils.createTableViewerColumn(viewer, "", SWT.NONE,
					25);
			column.setLabelProvider(new ColumnLabelProvider() {
				private static final long serialVersionUID = 1L;

				@Override
				public String getText(Object element) {
					return null;
				}
			});
			SelectionAdapter selectionAdapter = new SelectionAdapter() {
				private static final long serialVersionUID = 1L;

				boolean allSelected = false;

				@Override
				public void widgetSelected(SelectionEvent e) {
					allSelected = !allSelected;
					((CheckboxTableViewer) usersViewer)
							.setAllChecked(allSelected);
				}
			};
			column.getColumn().addSelectionListener(selectionAdapter);
		}

		// NodeViewerComparator comparator = new NodeViewerComparator();
		// TODO enable the sort by click on the header
		int i = offset;
		for (PeopleColumnDefinition colDef : columnDefs)
			createTableColumn(viewer, layout, colDef);

		// column = ViewerUtils.createTableViewerColumn(viewer,
		// colDef.getHeaderLabel(), SWT.NONE, colDef.getColumnSize());
		// column.setLabelProvider(new CLProvider(colDef.getPropertyName()));
		// column.getColumn().addSelectionListener(
		// JcrUiUtils.getNodeSelectionAdapter(i,
		// colDef.getPropertyType(), colDef.getPropertyName(),
		// comparator, viewer));
		// i++;
		// }

		// IMPORTANT: initialize comparator before setting it
		// JcrColumnDefinition firstCol = colDefs.get(0);
		// comparator.setColumn(firstCol.getPropertyType(),
		// firstCol.getPropertyName());
		// viewer.setComparator(comparator);

		return viewer;
	}

	/** Default creation of a column for a user table */
	private TableViewerColumn createTableColumn(TableViewer tableViewer,
			TableColumnLayout layout, PeopleColumnDefinition columnDef) {

		boolean resizable = true;
		TableViewerColumn tvc = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn column = tvc.getColumn();

		column.setText(columnDef.getHeaderLabel());
		column.setWidth(columnDef.getColumnSize());
		column.setResizable(resizable);

		ColumnLabelProvider lp = columnDef.getColumnLabelProvider();
		// add a reference to the display to enable font management
		if (lp instanceof UserAdminAbstractLP)
			((UserAdminAbstractLP) lp).setDisplay(tableViewer.getTable()
					.getDisplay());
		tvc.setLabelProvider(lp);

		layout.setColumnData(
				column,
				new ColumnWeightData(columnDef.getColumnSize(), columnDef
						.getColumnSize(), resizable));

		return tvc;
	}

	private String systemDomainSuffix = "ou=roles,ou=node";
	private String KEY_DN = "dn";

	private abstract class UserAdminAbstractLP extends ColumnLabelProvider {
		private static final long serialVersionUID = 137336765024922368L;

		// private Font italic;
		private Font bold;

		@Override
		public Font getFont(Object element) {
			// Self as bold
			try {
				LdapName selfUserName = new LdapName(
						userAdminService.getMyUserName());
				String userName = UsersUtils
						.getProperty((User) element, KEY_DN);
				LdapName userLdapName = new LdapName(userName);
				if (userLdapName.equals(selfUserName))
					return bold;
			} catch (InvalidNameException e) {
				throw new PeopleException("unable to define font for "
						+ element, e);
			}
			return null;
		}

		@Override
		public String getText(Object element) {
			User user = (User) element;
			return getText(user);
		}

		public void setDisplay(Display display) {
			// italic = JFaceResources.getFontRegistry().defaultFontDescriptor()
			// .setStyle(SWT.ITALIC).createFont(display);
			bold = JFaceResources.getFontRegistry().defaultFontDescriptor()
					.setStyle(SWT.BOLD).createFont(display);
		}

		public abstract String getText(User user);

	}

	private class RoleIconLP extends UserAdminAbstractLP {
		private static final long serialVersionUID = 6550449442061090388L;

		@Override
		public String getText(User user) {
			return "";
		}

		@Override
		public Image getImage(Object element) {
			User user = (User) element;
			String dn = (String) user.getProperties().get(KEY_DN);
			if (dn.endsWith(systemDomainSuffix))
				return PeopleRapImages.ICON_ROLE;
			else if (user.getType() == Role.GROUP)
				return PeopleRapImages.ICON_GROUP;
			else
				return PeopleRapImages.ICON_USER;
		}
	}

	private class DomainNameLP extends UserAdminAbstractLP {
		private static final long serialVersionUID = 5256703081044911941L;

		@Override
		public String getText(User user) {
			String dn = (String) user.getProperties().get(KEY_DN);
			if (dn.endsWith(systemDomainSuffix))
				return "System roles";
			try {
				LdapName name;
				name = new LdapName(dn);
				List<Rdn> rdns = name.getRdns();
				return (String) rdns.get(1).getValue() + '.'
						+ (String) rdns.get(0).getValue();
			} catch (InvalidNameException e) {
				throw new ArgeoException("Unable to get domain name for " + dn,
						e);
			}
		}
	}

	private class CommonNameLP extends UserAdminAbstractLP {
		private static final long serialVersionUID = 5256703081044911941L;

		@Override
		public String getText(User user) {
			Object obj = user.getProperties().get(
					org.argeo.osgi.useradmin.LdifName.cn.name());
			if (obj != null)
				return (String) obj;
			else
				return "";
		}
	}
}