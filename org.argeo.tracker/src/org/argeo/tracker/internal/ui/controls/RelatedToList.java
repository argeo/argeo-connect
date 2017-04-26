package org.argeo.tracker.internal.ui.controls;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.ConnectException;
import org.argeo.connect.ui.ConnectUiStyles;
import org.argeo.connect.ui.ConnectUiUtils;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.workbench.AppWorkbenchService;
import org.argeo.connect.workbench.parts.PickUpRelatedDialog;
import org.argeo.tracker.TrackerException;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;

/** Wrap a composite to display and edit a list of related to entities */
public class RelatedToList extends Composite {
	private static final long serialVersionUID = -5339937283129519708L;

	// Context
	private AppWorkbenchService appWorkbenchService;
	private final Node relatable;
	private List<String> chosenValues;

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
	public RelatedToList(Composite parent, int style, Node relatable, String propName,
			AppWorkbenchService appWorkbenchService) {
		super(parent, style);
		this.appWorkbenchService = appWorkbenchService;
		this.relatable = relatable;
		chosenValues = new ArrayList<>();
		try {
			if (relatable.hasProperty(propName)) {
				Value[] vals = relatable.getProperty(propName).getValues();
				for (Value val : vals)
					chosenValues.add(val.getString());
			}
		} catch (RepositoryException e) {
			throw new TrackerException("Cannot retrieve related to property on " + relatable, e);
		}
		populate(this);
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
	public RelatedToList(Composite parent, int style, Node relatable, List<String> initialValues,
			AppWorkbenchService appWorkbenchService) {
		super(parent, style);
		this.relatable = relatable;
		this.appWorkbenchService = appWorkbenchService;
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
			addValueCmp(parent, ConnectJcrUtils.getNodeByIdentifier(relatable, value));
		}

		Link addRelatedLk = new Link(parent, SWT.NONE);
		addRelatedLk.setText("<a>Add...</a>");
		addNewRelatedSelList(addRelatedLk);
	}

	/**
	 * Configure the action launched when the user click the add link in the
	 * "relatedTo" composite
	 */
	private void addNewRelatedSelList(final Link link) {
		link.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = -7118320199160680131L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				try {
					PickUpRelatedDialog diag = new PickUpRelatedDialog(link.getShell(), "Choose a related item",
							relatable.getSession(), appWorkbenchService, relatable);
					int result = diag.open();
					if (Window.OK == result)
						addValue(link, diag.getSelected());
				} catch (RepositoryException e) {
					throw new ConnectException("Unable to link chosen node to current entity " + relatable, e);
				}
			}
		});
	}

	private void addValue(Link link, Node node) {
		String id = ConnectJcrUtils.getIdentifier(node);
		if (chosenValues.contains(id))
			return;

		chosenValues.add(id);
		Composite parent = link.getParent();
		Composite valueCmp = addValueCmp(parent, node);
		valueCmp.moveAbove(link);
		parent.layout(true, true);
	}

	private Composite addValueCmp(Composite parent, Node node) {
		Composite valueCmp = new Composite(parent, SWT.NO_FOCUS);
		GridLayout layout = ConnectUiUtils.noSpaceGridLayout(2);
		layout.horizontalSpacing = 3;
		valueCmp.setLayout(layout);
		Label valueLbl = new Label(valueCmp, SWT.NONE);
		valueLbl.setText(" " + ConnectJcrUtils.get(node, Property.JCR_TITLE));
		addDeleteButton(valueCmp, ConnectJcrUtils.getIdentifier(node));
		return valueCmp;
	}

	private void addDeleteButton(Composite parent, String value) {
		final Button deleteBtn = new Button(parent, SWT.FLAT);
		CmsUtils.style(deleteBtn, ConnectUiStyles.SMALL_DELETE_BTN);
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

	/**
	 * Overwrite to call the relevant open editor command, does nothing by
	 * default
	 */
	protected void callOpenEditor(String tagKey) {
	}
}
