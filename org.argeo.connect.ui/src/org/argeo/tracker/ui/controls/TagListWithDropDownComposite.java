package org.argeo.tracker.ui.controls;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;

import org.argeo.cms.ui.util.CmsUiUtils;
import org.argeo.connect.ui.ConnectUiStyles;
import org.argeo.connect.ui.ConnectUiUtils;
import org.argeo.connect.ui.widgets.ConnectAbstractDropDown;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Wraps an Abstract form part that enable management of a tag like list in a
 * form editor.
 */
public abstract class TagListWithDropDownComposite extends Composite {

	private static final long serialVersionUID = 5439358000985800234L;

	// Context
	private List<String> chosenValues;
	private final String newTagMsg = "Add...";

	public List<String> getChosenValues() {
		return chosenValues;
	}

	/**
	 * 
	 * @param parent
	 * @param style
	 * @param toolkit
	 * @param form
	 * @param peopleService
	 * @param peopleWorkbenchService
	 * @param taggable
	 * @param tagId
	 * @param newTagMsg
	 */
	public TagListWithDropDownComposite(Composite parent, int style, List<String> initialValues) {
		super(parent, style);
		if (initialValues == null)
			chosenValues = new ArrayList<>();
		else
			chosenValues = initialValues;

		populate(this);
	}

	public void populate(Composite parent) {

		RowLayout rl = new RowLayout(SWT.HORIZONTAL);
		rl.wrap = true;
		rl.marginLeft = rl.marginBottom = 0;
		rl.marginTop = 4;
		rl.marginRight = 8;
		parent.setLayout(rl);

		for (String value : chosenValues) {
			addValueCmp(parent, value);
		}

		final Text addValueTxt = new Text(parent, SWT.BORDER);
		addValueTxt.setMessage(newTagMsg);
		RowData rd = new RowData(80, SWT.DEFAULT);
		addValueTxt.setLayoutData(rd);
		final TagLikeDropDown tagDD = new TagLikeDropDown(addValueTxt);

		// we must call this so that the row data can compute the OK
		// button size.
		addValueTxt.getParent().layout();
		Button okBtn = new Button(parent, SWT.BORDER | SWT.PUSH | SWT.BOTTOM);
		okBtn.setText("OK");
		rd = new RowData(SWT.DEFAULT, addValueTxt.getSize().y - 2);
		okBtn.setLayoutData(rd);

		addValueTxt.addTraverseListener(new TraverseListener() {
			private static final long serialVersionUID = 1L;

			public void keyTraversed(TraverseEvent e) {
				if (e.keyCode == SWT.CR) {
					String newTag = tagDD.getText();
					addValue(addValueTxt, newTag);
					tagDD.reset("");
					e.doit = false;
				}
			}
		});

		okBtn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 2780819012423622369L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				String newTag = tagDD.getText();
				if (EclipseUiUtils.isEmpty(newTag))
					return;
				else
					addValue(addValueTxt, newTag);
				tagDD.reset("");
			}
		});
	}

	private void addValue(Control dropdownTxt, String newTag) {
		if (!tagExists(newTag))
			// TODO manage creation
			return;

		if (chosenValues.contains(newTag))
			return;

		chosenValues.add(newTag);
		Composite parent = dropdownTxt.getParent();
		Composite valueCmp = addValueCmp(parent, newTag);
		valueCmp.moveAbove(dropdownTxt);
		parent.layout(true, true);
	}

	private Composite addValueCmp(Composite parent, String value) {
		Composite valueCmp = new Composite(parent, SWT.NO_FOCUS);
		GridLayout layout = ConnectUiUtils.noSpaceGridLayout(2);
		layout.horizontalSpacing = 3;
		valueCmp.setLayout(layout);
		Label valueLbl = new Label(valueCmp, SWT.NONE);
		valueLbl.setText(" " + value);
		addDeleteButton(valueCmp, value);
		return valueCmp;
	}

	private void addDeleteButton(Composite parent, String value) {
		final Button deleteBtn = new Button(parent, SWT.FLAT);
		CmsUiUtils.style(deleteBtn, ConnectUiStyles.SMALL_DELETE_BTN);
		deleteBtn.setLayoutData(new GridData(8, 8));
		deleteBtn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				chosenValues.remove(value);
				Composite lineCmp = parent.getParent();
				parent.dispose();
				lineCmp.layout(true, true);
			}
		});
	}

	private class TagLikeDropDown extends ConnectAbstractDropDown {

		public TagLikeDropDown(Text text) {
			super(text);
			init();
		}

		@Override
		protected List<String> getFilteredValues(String filter) {
			return TagListWithDropDownComposite.this.getFilteredValues(filter);
		}
	}

	/**
	 * Overwrite to provide a filtered list of relevant possible new values
	 */
	abstract protected List<String> getFilteredValues(String filter);

	/**
	 * Overwrite to perform a check prior to effectively creating the new value
	 */
	protected boolean tagExists(String tagKey) {
		List<String> existings = getFilteredValues(null);
		return existings.contains(tagKey);
	}

	/** Overwrite to store the tag key in another property */
	protected String getTagKey(Node tagDefinition) {
		return ConnectJcrUtils.get(tagDefinition, Property.JCR_TITLE);
	}

	/** Overwrite to display a label rather than the stored value */
	protected String getEncodedTagValue(String tagKey) {
		return tagKey;
	}

	/**
	 * Overwrite to get the stored value from the displayed label, we expect a
	 * bijection between the 2 of them
	 */
	protected String getDecodedTagValue(String tagValue) {
		return tagValue;
	}

	/**
	 * Overwrite to call the relevant open editor command, does nothing by
	 * default
	 */
	protected void callOpenEditor(String tagKey) {
	}
}
