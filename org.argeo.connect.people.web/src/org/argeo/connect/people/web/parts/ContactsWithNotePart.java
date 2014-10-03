package org.argeo.connect.people.web.parts;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.web.PeopleWebUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

/**
 * Part that display a list of contacts for a people:contactable node and the
 * Jcr:Description of this contactable node
 */
public class ContactsWithNotePart extends ContactsPart {

	@Override
	public Control createUi(Composite parent, Node context)
			throws RepositoryException {
		parent.setLayout(new GridLayout(2, false));
		Composite left = new Composite(parent, SWT.NO_FOCUS);
		left.setLayoutData(PeopleWebUtils.horizontalFillData());
		createContactPanel(left, context);

		Composite right = new Composite(parent, SWT.NO_FOCUS);
		right.setLayoutData(PeopleWebUtils.fillGridData());
		createNotePanel(right, context);
		return parent;
	}

	private void createNotePanel(Composite parent, Node context)
			throws RepositoryException {
		parent.setLayout(new GridLayout());

		// FIXME add description in the demo data and change the below
		if (context.hasProperty(Property.JCR_TITLE)) {
			Group group = new Group(parent, SWT.NO_FOCUS);
			group.setLayout(new GridLayout());
			group.setLayoutData(PeopleWebUtils.fillGridData());

			group.setText("Note");
			Label label = new Label(group, SWT.WRAP);
			label.setText(CommonsJcrUtils.get(context, Property.JCR_TITLE));
		}
	}
}