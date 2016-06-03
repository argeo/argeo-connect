package org.argeo.connect.people.web.parts;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.util.JcrUiUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

/**
 * Part that display a list of contacts for a people:contactable node and the
 * jcr:description of this contactable node
 */
public class ContactsWithNotePart extends ContactsPart {

	@Override
	public Control createUi(Composite parent, Node context)
			throws RepositoryException {
		parent.setLayout(PeopleUiUtils.noSpaceGridLayout(2));
		Composite left = new Composite(parent, SWT.NO_FOCUS);
		left.setLayoutData(EclipseUiUtils.fillWidth());
		createContactPanel(left, context);

		Composite right = new Composite(parent, SWT.NO_FOCUS);
		right.setLayoutData(EclipseUiUtils.fillAll());
		createNotePanel(right, context);
		return parent;
	}

	private void createNotePanel(Composite parent, Node context)
			throws RepositoryException {
		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());

		// FIXME add description in the demo data and change the below
		if (context.hasProperty(Property.JCR_TITLE)) {
			Group group = new Group(parent, SWT.NO_FOCUS);
			group.setLayout(EclipseUiUtils.noSpaceGridLayout());
			group.setLayoutData(EclipseUiUtils.fillAll());

			group.setText("Note");
			Label label = new Label(group, SWT.WRAP);
			label.setText(JcrUiUtils.get(context, Property.JCR_TITLE));
		}
	}
}