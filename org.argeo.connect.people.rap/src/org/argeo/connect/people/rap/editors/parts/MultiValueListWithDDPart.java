package org.argeo.connect.people.rap.editors.parts;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.rap.PeopleRapConstants;
import org.argeo.connect.people.rap.PeopleRapImages;
import org.argeo.connect.people.rap.composites.dropdowns.PeopleAbstractDropDown;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Provides a composite that display values of a multivalue property with
 * edition abilities. Implement the <code>getFilteredValues</code> method to
 * provide the possible legal values that might be added
 */

public abstract class MultiValueListWithDDPart extends Composite {

	private static final long serialVersionUID = 1L;
	private AbstractFormPart part;
	private FormToolkit toolkit;

	private Node node;
	private String propertyName;

	public MultiValueListWithDDPart(FormToolkit toolkit, AbstractFormPart part,
			Composite parent, int style, Node node, String propertyName) {
		super(parent, style);
		this.toolkit = toolkit;
		this.part = part;
		this.node = node;
		this.propertyName = propertyName;

		RowLayout rl = new RowLayout(SWT.HORIZONTAL);
		rl.wrap = true;
		rl.marginLeft = 5;
		rl.marginRight = 0;

		this.setLayout(rl);
		refresh();
	}

	protected abstract List<String> getFilteredValues(String filter);

	/** Performs the real addition, overwrite to make further sanity checks */
	protected void addValue(String value) {
		String errMsg = null;

		if (CommonsJcrUtils.isEmptyString(value))
			return;
		if (!getFilteredValues(null).contains(value))
			errMsg = value + " is not a legal choice, "
					+ "please correct and try again";
		else
			errMsg = CommonsJcrUtils.addStringToMultiValuedProp(node,
					propertyName, value);
		if (errMsg != null)
			MessageDialog.openError(MultiValueListWithDDPart.this.getShell(),
					"Addition not allowed", errMsg);
		else {
			part.refresh();
			part.markDirty();
		}
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

			final Text tagTxt = toolkit.createText(parent, "", SWT.BORDER);
			tagTxt.setMessage("Add...");
			RowData rd = new RowData(80, SWT.DEFAULT);
			tagTxt.setLayoutData(rd);

			final PeopleAbstractDropDown addValueDD = new AddValueDD(tagTxt,
					SWT.READ_ONLY);

			tagTxt.addTraverseListener(new TraverseListener() {
				private static final long serialVersionUID = 1L;

				public void keyTraversed(TraverseEvent e) {
					if (e.keyCode == SWT.CR) {
						addValue(addValueDD.getText());
						e.doit = false;
					}
				}
			});
			// we must call this so that the row data can compute the OK
			// button size.
			tagTxt.getParent().layout();

			Button okBtn = toolkit.createButton(parent, "OK", SWT.BORDER
					| SWT.PUSH | SWT.BOTTOM);
			rd = new RowData(SWT.DEFAULT, tagTxt.getSize().y - 2);
			okBtn.setLayoutData(rd);

			okBtn.addSelectionListener(new SelectionAdapter() {
				private static final long serialVersionUID = 2780819012423622369L;

				@Override
				public void widgetSelected(SelectionEvent e) {
					addValue(addValueDD.getText());
				}
			});

		} catch (RepositoryException e) {
			throw new PeopleException(
					"Unable to recreate multi value list composite "
							+ "content for prop " + propertyName + " on "
							+ node, e);
		}
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

	private class AddValueDD extends PeopleAbstractDropDown {

		public AddValueDD(Text text, int style) {
			super(text, style);
		}

		@Override
		protected List<String> getFilteredValues(String filter) {
			return MultiValueListWithDDPart.this.getFilteredValues(filter);
		}
	}
}