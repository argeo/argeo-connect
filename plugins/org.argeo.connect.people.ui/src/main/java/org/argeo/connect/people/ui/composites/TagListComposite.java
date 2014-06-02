package org.argeo.connect.people.ui.composites;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.argeo.ArgeoException;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.ui.PeopleImages;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiService;
import org.argeo.connect.people.ui.commands.OpenEntityEditor;
import org.argeo.connect.people.ui.composites.dropdowns.SimpleResourceDropDown;
import org.argeo.connect.people.ui.utils.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.eclipse.ui.utils.CommandUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Wraps an Abstract form part that enable management of a tag like list in a
 * form editor.
 */
public class TagListComposite extends Composite {
	private static final long serialVersionUID = -312141685147619814L;
	// public class ContactComposite {
	// private static final long serialVersionUID = -789885142022513273L;

	private PeopleService peopleService;
	private PeopleUiService peopleUiService;
	private final FormToolkit toolkit;
	private final IManagedForm form;
	private final Node tagable;
	private final String tagPropName;
	private final String tagsParentPath;
	private final String resourceType;
	private final String newTagMsg;

	// TODO document this
	public TagListComposite(Composite parent, int style, FormToolkit toolkit,
			IManagedForm form, PeopleService peopleService,
			PeopleUiService peopleUiService, Node tagable, String tagPropName,
			String tagsParentPath, String resourceType, String newTagMsg) {
		super(parent, style);
		this.toolkit = toolkit;
		this.form = form;
		this.peopleUiService = peopleUiService;
		this.peopleService = peopleService;
		this.tagable = tagable;
		this.tagPropName = tagPropName;
		this.tagsParentPath = tagsParentPath;
		this.resourceType = resourceType;
		this.newTagMsg = newTagMsg;

		populate();
	}

	/* Main layout and form part creation */
	private void populate() {
		Composite parent = this;
		RowLayout rl = new RowLayout(SWT.HORIZONTAL);
		rl.wrap = true;
		rl.marginHeight = rl.marginLeft = 0;
		rl.marginRight = 8;
		parent.setLayout(rl);

		AbstractFormPart tagFormPart = new TagFormPart(parent);
		tagFormPart.initialize(form);
		form.addPart(tagFormPart);
	}

	private class TagFormPart extends AbstractFormPart {
		private Composite nlCmp;

		public TagFormPart(Composite parent) {
			this.nlCmp = parent;
		}

		public void refresh() {
			super.refresh();
			// We redraw the full control at each refresh, might be a more
			// efficient way to do
			Control[] oldChildren = nlCmp.getChildren();
			for (Control child : oldChildren)
				child.dispose();

			boolean isCO = CommonsJcrUtils.isNodeCheckedOutByMe(tagable);

			try {
				if (tagable.hasProperty(tagPropName)) {
					Value[] values = tagable.getProperty(tagPropName)
							.getValues();
					for (final Value value : values) {
						final String tagValue = value.getString();

						Composite tagCmp = toolkit.createComposite(nlCmp,
								SWT.NO_FOCUS);
						tagCmp.setLayout(PeopleUiUtils.gridLayoutNoBorder(2));
						Link link = new Link(tagCmp, SWT.NONE);
						link.setText(" #<a>" + tagValue + "</a>");
						// Specific style for tags.
						if (tagPropName.equals(PeopleNames.PEOPLE_TAGS))
							link.setData(PeopleUiConstants.CUSTOM_VARIANT,
									PeopleUiConstants.PEOPLE_CSS_TAG_STYLE);
						link.setData(PeopleUiConstants.MARKUP_ENABLED,
								Boolean.TRUE);

						link.addSelectionListener(new SelectionAdapter() {
							private static final long serialVersionUID = 1L;

							@Override
							public void widgetSelected(
									final SelectionEvent event) {
								Node tag = peopleService.getRegisteredTag(
										CommonsJcrUtils.getSession(tagable),
										tagsParentPath, tagValue);
								// ResourcesJcrUtils.getTagNodeFromValue(
								// CommonsJcrUtils.getSession(entity),
								// peopleService
								// .getResourcesBasePath(PeopleNames.PEOPLE_TAGS),
								// tagValue);
								CommandUtils.callCommand(peopleUiService
										.getOpenEntityEditorCmdId(),
										OpenEntityEditor.PARAM_JCR_ID,
										CommonsJcrUtils.getIdentifier(tag));
							}
						});

						if (isCO) {
							addDeleteButton(TagFormPart.this, tagCmp, value);
						}
					}
				}
				if (isCO) {
					final Text tagTxt = toolkit.createText(nlCmp, "",
							SWT.BORDER);
					tagTxt.setMessage(newTagMsg);
					RowData rd = new RowData(120, SWT.DEFAULT);
					tagTxt.setLayoutData(rd);

					final SimpleResourceDropDown tagDD = new SimpleResourceDropDown(
							peopleUiService, tagable.getSession(),
							tagsParentPath, tagTxt);

					tagTxt.addTraverseListener(new TraverseListener() {
						private static final long serialVersionUID = 1L;

						public void keyTraversed(TraverseEvent e) {
							if (e.keyCode == SWT.CR) {
								String newTag = tagDD.getText();
								addTag(tagTxt.getShell(), TagFormPart.this,
										newTag);
								e.doit = false;
								// if (!tagTxt.isDisposed())
								// tagDD.reset("");
								// tagTxt.setText("");
							}
						}
					});

					tagTxt.getParent().layout();

					Button okBtn = toolkit.createButton(nlCmp, "OK", SWT.BORDER
							| SWT.PUSH | SWT.BOTTOM);
					rd = new RowData(SWT.DEFAULT, tagTxt.getSize().y - 2);
					okBtn.setLayoutData(rd);

					okBtn.addSelectionListener(new SelectionAdapter() {
						private static final long serialVersionUID = 2780819012423622369L;

						@Override
						public void widgetSelected(SelectionEvent e) {
							String newTag = tagDD.getText();
							if (CommonsJcrUtils.isEmptyString(newTag))
								return;
							else
								addTag(tagTxt.getShell(), TagFormPart.this,
										newTag);
						}
					});

				}
				nlCmp.layout(false);
				nlCmp.getParent().getParent().layout();

			} catch (RepositoryException re) {
				throw new PeopleException(
						"Error while refreshing tag like list for " + tagable,
						re);
			}
		}
	}

	private void addDeleteButton(final AbstractFormPart part, Composite parent,
			final Value value) {
		final Button deleteBtn = new Button(parent, SWT.FLAT);
		deleteBtn.setData(PeopleUiConstants.CUSTOM_VARIANT,
				PeopleUiConstants.CSS_FLAT_IMG_BUTTON);
		deleteBtn
				.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		deleteBtn.setImage(PeopleImages.DELETE_BTN_LEFT);
		deleteBtn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				try {
					String tagToRemove = value.getString();
					List<String> tags = new ArrayList<String>();
					Value[] values = tagable.getProperty(tagPropName)
							.getValues();
					for (int i = 0; i < values.length; i++) {
						String curr = values[i].getString();
						if (!tagToRemove.equals(curr))
							tags.add(curr);
					}
					tagable.setProperty(tagPropName,
							tags.toArray(new String[tags.size()]));
					part.markDirty();
					part.refresh();
				} catch (RepositoryException e) {
					throw new PeopleException("unable to initialise deletion",
							e);
				}
			}
		});
	}

	private void addTag(Shell shell, final AbstractFormPart part, String newTag) {
		String msg = null;

		try {
			Session session = tagable.getSession();
			// Check if such a tag is already registered
			Node registered = peopleService.getRegisteredTag(session,
					tagsParentPath, newTag);

			if (registered == null) {
				// Ask end user if we create a new tag
				msg = "\""
						+ newTag
						+ "\" is not yet registered.\n Are you sure you want to create it?";
				if (MessageDialog.openConfirm(shell, "Confirm creation", msg)) {
					registered = peopleService.registerTag(session,
							resourceType, tagsParentPath, newTag);
				} else
					return;
			}

			Value[] values;
			String[] valuesStr;
			if (tagable.hasProperty(tagPropName)) {
				values = tagable.getProperty(tagPropName).getValues();

				// Check duplicates
				for (Value tag : values) {
					String curTagUpperCase = tag.getString().toUpperCase()
							.trim();
					if (newTag.toUpperCase().trim().equals(curTagUpperCase)) {
						msg = "\""
								+ CommonsJcrUtils.get(tagable,
										Property.JCR_TITLE)
								+ "\" is already linked with \""
								+ tag.getString()
								+ "\". Nothing has been done.";
						MessageDialog.openError(shell, "Duplicate link", msg);
						return;
					}
				}

				valuesStr = new String[values.length + 1];
				int i;
				for (i = 0; i < values.length; i++) {
					valuesStr[i] = values[i].getString();
				}
				valuesStr[i] = newTag;
			} else {
				valuesStr = new String[1];
				valuesStr[0] = newTag;
			}
			tagable.setProperty(tagPropName, valuesStr);
			part.markDirty();
			part.refresh();
		} catch (RepositoryException re) {
			throw new ArgeoException("Unable to set " + tagPropName + " on "
					+ tagable, re);
		}
	}
}