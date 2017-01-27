package org.argeo.connect.people.rap.editors.parts;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.rap.PeopleRapImages;
import org.argeo.connect.people.rap.PeopleStyles;
import org.argeo.connect.people.rap.composites.dropdowns.PeopleAbstractDropDown;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.util.JcrUiUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Provides a composite that display values of a multivalue property with
 * edition abilities. Implement the <code>getFilteredValues</code> method to
 * provide the possible legal values that might be added
 */

public abstract class MultiValueListWithDDPart extends Composite {
	private static final long serialVersionUID = 7439016082872333306L;
	private AbstractFormPart part;
	private FormToolkit toolkit;

	private Node node;
	private String propertyName;
	private String addMsg;

	public MultiValueListWithDDPart(FormToolkit toolkit, AbstractFormPart part, Composite parent, int style, Node node,
			String propertyName, String addMsg) {
		super(parent, style);
		this.toolkit = toolkit;
		this.part = part;
		this.node = node;
		this.propertyName = propertyName;
		this.addMsg = addMsg;
		refresh();
	}

	protected abstract List<String> getFilteredValues(String filter);

	/** Performs the real addition, overwrite to make further sanity checks */
	protected void addValue(String value) {
		String errMsg = null;

		if (EclipseUiUtils.isEmpty(value))
			return;
		if (!getFilteredValues(null).contains(value))
			errMsg = value + " is not a legal choice, " + "please correct and try again";
		else
			errMsg = JcrUiUtils.addStringToMultiValuedProp(node, propertyName, value);
		if (errMsg != null)
			MessageDialog.openError(MultiValueListWithDDPart.this.getShell(), "Addition not allowed", errMsg);
		else {
			part.refresh();
			part.markDirty();
		}
	}

	public void refresh() {
		Composite parent = this;

		RowLayout rl = new RowLayout(SWT.HORIZONTAL);
		rl.wrap = true;
		rl.marginLeft = rl.marginBottom = rl.marginTop = 0;
		rl.marginRight = 8;

		this.setLayout(rl);

		// TODO Add a check to see if the property has changed
		EclipseUiUtils.clear(parent);

		try {
			if (node.hasProperty(propertyName)) {
				Value[] values = node.getProperty(propertyName).getValues();
				for (final Value value : values) {
					// Workaround the fact that row layout elements are top
					// aligned
					Composite valCmp = toolkit.createComposite(parent, SWT.NO_FOCUS);
					GridLayout gl = PeopleUiUtils.noSpaceGridLayout(2);
					gl.marginTop = 2;
					valCmp.setLayout(gl);

					// Label and delete button
					Label label = new Label(valCmp, SWT.BOTTOM);
					label.setText(value.getString());
					CmsUtils.style(label, PeopleStyles.PEOPLE_CLASS_ENTITY_HEADER);
					CmsUtils.markup(label);
					Button deleteBtn = createDeleteButton(valCmp);
					deleteBtn.addSelectionListener(getDeleteBtnListener(value.getString()));
				}
			}

			final Text tagTxt = toolkit.createText(parent, "", SWT.BORDER);
			tagTxt.setMessage(addMsg);
			RowData rd = new RowData(80, SWT.DEFAULT);
			tagTxt.setLayoutData(rd);
			// This does not work
			// CmsUtils.style(tagTxt, "add_value");

			final PeopleAbstractDropDown addValueDD = new AddValueDD(tagTxt, SWT.READ_ONLY, true);

			tagTxt.addTraverseListener(new TraverseListener() {
				private static final long serialVersionUID = 1L;

				public void keyTraversed(TraverseEvent e) {
					if (e.keyCode == SWT.CR) {
						addValue(addValueDD.getText());
						e.doit = false;
					}
				}
			});
			// we must call this so that the row data can compute the height of
			// other controls.
			tagTxt.getParent().layout();
			int height = tagTxt.getSize().y;

			Button okBtn = toolkit.createButton(parent, "OK", SWT.BORDER | SWT.PUSH | SWT.BOTTOM);
			rd = new RowData(SWT.DEFAULT, height - 2);
			okBtn.setLayoutData(rd);

			okBtn.addSelectionListener(new SelectionAdapter() {
				private static final long serialVersionUID = 2780819012423622369L;

				@Override
				public void widgetSelected(SelectionEvent e) {
					addValue(addValueDD.getText());
				}
			});

		} catch (RepositoryException e) {
			throw new PeopleException("Unable to recreate multi value list composite " + "content for prop "
					+ propertyName + " on " + node, e);
		}
		parent.layout();
	}

	private Button createDeleteButton(Composite parent) {
		Button button = new Button(parent, SWT.FLAT);
		CmsUtils.style(button, PeopleStyles.FLAT_BTN);
		button.setImage(PeopleRapImages.DELETE_BTN);
		GridData gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		button.setLayoutData(gd);
		return button;
	}

	private SelectionListener getDeleteBtnListener(final String value) {
		return new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				JcrUiUtils.removeMultiPropertyValue(node, propertyName, value);
				part.refresh();
				part.markDirty();
			}
		};
	}

	private class AddValueDD extends PeopleAbstractDropDown {

		public AddValueDD(Text text, int style, boolean refreshOnFocus) {
			super(text, style, refreshOnFocus);
		}

		@Override
		protected List<String> getFilteredValues(String filter) {
			return MultiValueListWithDDPart.this.getFilteredValues(filter);
		}
	}
}