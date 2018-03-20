package org.argeo.connect.workbench;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.argeo.cms.ui.eclipse.forms.AbstractFormPart;
import org.argeo.cms.ui.workbench.util.CommandUtils;
import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.ConnectException;
import org.argeo.connect.ui.ConnectEditor;
import org.argeo.connect.ui.ConnectUiStyles;
import org.argeo.connect.ui.SystemWorkbenchService;
import org.argeo.connect.ui.parts.PickUpRelatedDialog;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
//import org.eclipse.ui.forms.AbstractFormPart;

/** Provides basic information about a task in a form context */
public class LinkListPart extends Composite {
	private static final long serialVersionUID = -5813631462166187272L;

	// Context
	private final SystemWorkbenchService systemWorkbenchService;
	private final Node entity;
	private final String propName;
	private final List<String> hiddenItemIds = new ArrayList<String>();

	// UI Context
	private final ConnectEditor editor;
	private final AbstractFormPart formPart;

	// COMPOSITES
	private static final String VALUE_KEY = "valueKey";

	protected Node getTargetWithValue(String string) {
		try {
			return entity.getSession().getNodeByIdentifier(string);
		} catch (RepositoryException e) {
			throw new ConnectException("Unable to retrieve target node of related entity for " + entity, e);
		}
	}

	public LinkListPart(ConnectEditor editor, AbstractFormPart formPart, Composite parent, int style,
			SystemWorkbenchService systemWorkbenchService, Node entity, String propName) {
		this(editor, formPart, parent, style, systemWorkbenchService, entity, propName, null);
	}

	/**
	 * Will prevent display of linked entities whose id is passed in the list
	 * 
	 * @param toolkit
	 * @param formPart
	 * @param parent
	 * @param style
	 * @param systemWorkbenchService
	 * @param entity
	 * @param propName
	 * @param hiddenItemIds
	 */
	public LinkListPart(ConnectEditor editor, AbstractFormPart formPart, Composite parent, int style,
			SystemWorkbenchService systemWorkbenchService, Node entity, String propName, List<String> hiddenItemIds) {
		super(parent, style);
		this.formPart = formPart;
		this.editor = editor;
		this.systemWorkbenchService = systemWorkbenchService;
		this.entity = entity;
		this.propName = propName;

		// Initialise the form
		RowLayout rl = new RowLayout(SWT.HORIZONTAL);
		rl.wrap = true;
		rl.marginBottom = rl.marginTop = rl.marginLeft = rl.marginRight = 0;
		rl.spacing = 8;
		this.setLayout(rl);
		if (hiddenItemIds != null)
			this.hiddenItemIds.addAll(hiddenItemIds);
		recreateRelated(this);
	}

	public void refresh() {
		if (relatedHasChanged()) {
			recreateRelated(this);
		}
		this.layout();
	}

	// HELPERS
	private void recreateRelated(Composite relatedCmp) {
		try {
			// Dispose existing
			EclipseUiUtils.clear(relatedCmp);

			if (entity.hasProperty(propName)) {
				Value[] values = entity.getProperty(propName).getValues();
				for (Value value : values) {
					String valueStr = value.getString();
					if (!hiddenItemIds.contains(valueStr)) {
						Node targetNode = getTargetWithValue(valueStr);
						String labelStr = ConnectJcrUtils.get(targetNode, Property.JCR_TITLE);
						createDeletableClickable(relatedCmp, valueStr, labelStr, editor.isEditing());
					}
				}
			} else if (!editor.isEditing())
				// Add an empty label to force a correct layout
				new Label(relatedCmp, SWT.NONE);

			if (editor.isEditing()) {
				// The add button
				Link newRelatedLk = new Link(relatedCmp, SWT.CENTER);
				newRelatedLk.setText("<a>Add</a>");
				addNewRelatedSelList(newRelatedLk);
			}
		} catch (RepositoryException e) {
			throw new ConnectException("Unable to create related to composite for " + entity, e);
		}
	}

	private Composite createDeletableClickable(Composite parent, final String value, String label, boolean isEditing) {
		Composite part = new Composite(parent, SWT.NO_FOCUS);
		part.setLayout(EclipseUiUtils.noSpaceGridLayout(new GridLayout(2, false)));
		part.setData(VALUE_KEY, value);

		Link relatedLk = new Link(part, SWT.LEFT);
		relatedLk.setText("<a>" + label + "</a>");
		CmsUtils.style(relatedLk, ConnectUiStyles.ENTITY_HEADER);
		relatedLk.addSelectionListener(new OpenEditorAdapter(value));

		if (isEditing) {
			// Display delete button only in edit mode.
			Button deleteBtn = new Button(part, SWT.FLAT);
			CmsUtils.style(deleteBtn, ConnectUiStyles.SMALL_DELETE_BTN);
			GridData gd = new GridData(8, 8);
			gd.horizontalIndent = 2;
			deleteBtn.setLayoutData(gd);
			deleteBtn.addSelectionListener(new SelectionAdapter() {
				private static final long serialVersionUID = 1L;

				@Override
				public void widgetSelected(final SelectionEvent event) {
					ConnectJcrUtils.removeRefFromMultiValuedProp(entity, propName, value);
					formPart.refresh();
					formPart.markDirty();
				}
			});
		}
		part.layout();
		return part;
	}

	private boolean relatedHasChanged() {
		try {
			Value[] values = null;
			Control[] oldChildren;

			if (entity.hasProperty(propName))
				values = entity.getProperty(propName).getValues();
			oldChildren = this.getChildren();

			if (values == null)
				return oldChildren.length > 0;

			int valueIndex = 0;
			loop: for (int i = 0; i < oldChildren.length; i++) {
				String currUiValue = (String) oldChildren[i].getData(VALUE_KEY);
				if (currUiValue == null)
					// skip non business controls
					continue loop;
				if (valueIndex >= values.length || !currUiValue.equals(values[valueIndex].getString()))
					return true;
				valueIndex++;
			}
			// initialisation or added value
			if (valueIndex < values.length)
				return true;
			return false;
		} catch (RepositoryException e) {
			throw new ConnectException("Unable to determine stale state for prop " + propName + " of " + entity, e);
		}
	}

	// Local classes
	private class OpenEditorAdapter extends SelectionAdapter {
		private static final long serialVersionUID = 1L;

		private final String jcrId;

		public OpenEditorAdapter(String jcrId) {
			this.jcrId = jcrId;
		}

		@Override
		public void widgetSelected(final SelectionEvent event) {
			CommandUtils.callCommand(systemWorkbenchService.getOpenEntityEditorCmdId(), ConnectEditor.PARAM_JCR_ID,
					jcrId);
		}
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
							entity.getSession(), systemWorkbenchService, entity);

					int result = diag.open();
					if (Window.OK == result) {
						Node node = diag.getSelected();
						String errMsg = ConnectJcrUtils.addRefToMultiValuedProp(entity, propName, node);
						if (errMsg != null)
							MessageDialog.openError(link.getShell(), "Duplicates", errMsg);
						else {
							formPart.refresh();
							formPart.markDirty();
						}
					}
				} catch (RepositoryException e) {
					throw new ConnectException("Unable to link chosen node " + "to current activity " + entity, e);
				}
			}
		});
	}
}
