package org.argeo.connect.people.rap.editors;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.ArgeoException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.rap.PeopleUiConstants;
import org.argeo.connect.people.rap.PeopleUiPlugin;
import org.argeo.jcr.ArgeoNames;
import org.argeo.jcr.JcrUtils;
import org.argeo.security.ui.admin.UserTableComposite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

public class UserGroupEditor extends GroupEditor {

	public final static String ID = PeopleUiPlugin.PLUGIN_ID
			+ ".userGroupEditor";

	private UserTableComposite userTableCmp;

	// Main business Objects
	private Node group;

	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		group = getNode();
	}

	@Override
	protected void createToolkits() {
	}

	@Override
	protected void populateTabFolder(CTabFolder folder) {
		// The member list
		String tooltip = "Members of group "
				+ JcrUtils.get(group, Property.JCR_TITLE);
		Composite innerPannel = addTabToFolder(folder, CTAB_COMP_STYLE,
				"Members", PeopleUiConstants.PANEL_MEMBERS, tooltip);
		createBottomPart(innerPannel);
	}

	protected void createBottomPart(Composite parent) {
		parent.setLayout(new FillLayout());

		// Create the composite that displays the list and a filter
		userTableCmp = new MyUserTableComposite(parent, SWT.NO_FOCUS,
				getSession());
		userTableCmp.populate(true, false);

		// Configure
		getSite().setSelectionProvider(userTableCmp.getTableViewer());
	}

	private class MyUserTableComposite extends UserTableComposite {
		private static final long serialVersionUID = 1L;

		public MyUserTableComposite(Composite parent, int style, Session session) {
			super(parent, style, session);
		}

		protected void refreshFilteredList() {
			List<Node> nodes = new ArrayList<Node>();
			try {
				PropertyIterator pit = group
						.getReferences(PeopleNames.PEOPLE_USER_GROUPS);
				while (pit.hasNext()) {
					Property prop = pit.nextProperty();
					Node parent = prop.getParent().getParent();
					log.debug("Parent Name " + parent.getName());
					nodes.add(parent.getNode(ArgeoNames.ARGEO_PROFILE));
				}
				getTableViewer().setInput(nodes.toArray());
			} catch (RepositoryException e) {
				throw new ArgeoException("Unable to list users", e);
			}
		}

	}

}
