package org.argeo.connect.people.rap.editors.parts;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.ResourceService;
import org.argeo.connect.people.rap.PeopleRapConstants;
import org.argeo.connect.people.rap.PeopleRapImages;
import org.argeo.connect.people.rap.dialogs.PickUpFromListDialog;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.widgets.FormToolkit;

// Add refresh ability to a basic composite
public class TemplateCatalogueWithPickUpPart extends Composite {

	private static final long serialVersionUID = 1L;
	private AbstractFormPart part;
	private FormToolkit toolkit;

	private ResourceService resourceService;
	private Node node;
	private String templateId;
	private String propertyName;

	public TemplateCatalogueWithPickUpPart(FormToolkit toolkit,
			AbstractFormPart part, Composite parent, int style,
			ResourceService resourceService, String templateId, Node node,
			String propertyName) {
		super(parent, style);
		this.toolkit = toolkit;
		this.part = part;
		this.templateId = templateId;
		this.resourceService = resourceService;
		this.node = node;
		this.propertyName = propertyName;

		RowLayout rl = new RowLayout(SWT.HORIZONTAL);
		rl.wrap = true;
		rl.marginLeft = 5;
		rl.marginRight = 0;

		this.setLayout(rl);
		refresh();
	}

	public void refresh() {
		Composite parent = this;
		// TODO Add a check to see if the property has changed
		Control[] oldChildren = parent.getChildren();
		for (Control child : oldChildren)
			child.dispose();

		try {
			if (node.hasProperty(propertyName)) {
				Value[] values = node.getProperty(propertyName).getValues();
				for (final Value value : values) {
					toolkit.createLabel(parent, value.getString());
					Button deleteBtn = createDeleteButton(parent);
					deleteBtn.addSelectionListener(getDeleteBtnListener(value
							.getString()));
				}
			}
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to recreate tag like composite "
					+ "content for prop " + propertyName + " on " + node, e);
		}
		Link addRelatedLk = createAddValueLk(parent);
		addRelatedLk.addSelectionListener(createAddValueListener(parent
				.getShell()));
		parent.layout();

	}

	private Button createDeleteButton(Composite parent) {
		Button button = new Button(parent, SWT.FLAT);
		button.setData(RWT.CUSTOM_VARIANT,
				PeopleRapConstants.PEOPLE_CLASS_FLAT_BTN);
		button.setImage(PeopleRapImages.DELETE_BTN);
		RowData rd = new RowData();
		rd.height = 16;
		rd.width = 16;
		button.setLayoutData(rd);
		return button;
	}

	private Link createAddValueLk(Composite parent) {
		Link link = new Link(parent, SWT.CENTER);
		toolkit.adapt(link, false, false);
		link.setText("<a>Add</a>");
		return link;
	}

	private SelectionListener getDeleteBtnListener(final String value) {
		return new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				CommonsJcrUtils.removeMultiPropertyValue(node, propertyName,
						value);
				part.refresh();
				part.markDirty();
			}
		};
	}

	private SelectionListener createAddValueListener(final Shell shell) {
		return new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				List<String> values = resourceService.getTemplateCatalogue(
						CommonsJcrUtils.getSession(node), templateId,
						propertyName, null);
				PickUpFromListDialog diag = new PickUpFromListDialog(shell,
						"Choose an entity", values.toArray(new String[values
								.size()]));
				int result = diag.open();
				if (result != Window.OK)
					return;
				String value = diag.getSelected();
				String errMsg = CommonsJcrUtils.addStringToMultiValuedProp(
						node, propertyName, value);
				if (errMsg != null)
					MessageDialog.openError(
							TemplateCatalogueWithPickUpPart.this.getShell(),
							"Dupplicates", errMsg);
				else {
					part.refresh();
					part.markDirty();
				}
			}
		};
	}
}
