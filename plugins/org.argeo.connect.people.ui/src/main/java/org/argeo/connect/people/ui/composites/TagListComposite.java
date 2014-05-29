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
import org.argeo.connect.people.utils.ResourcesJcrUtils;
import org.argeo.eclipse.ui.utils.CommandUtils;
import org.eclipse.jface.dialogs.MessageDialog;
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
	private final Node entity;
	private final String tagPropName;
	private final String newTagMsg;

	public TagListComposite(Composite parent, int style, FormToolkit toolkit,
			IManagedForm form, PeopleService peopleService,
			PeopleUiService peopleUiService, Node entity, String tagPropName,
			String newTagMsg) {
		super(parent, style);
		this.toolkit = toolkit;
		this.form = form;
		this.peopleUiService = peopleUiService;
		this.peopleService = peopleService;
		this.entity = entity;
		this.tagPropName = tagPropName;
		this.newTagMsg = newTagMsg;

		populate();
	}

	// ////////////////
	// The Tag panel
	private void populate() {
		Composite parent = this;
		GridLayout gl = PeopleUiUtils.gridLayoutNoBorder();
		gl.marginLeft = 3;
		parent.setLayout(gl);

		Composite nlCmp = toolkit.createComposite(parent);
		nlCmp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		RowLayout rl = new RowLayout(SWT.HORIZONTAL);
		rl.wrap = true;
		rl.marginHeight = rl.marginLeft = 0;
		rl.marginRight = 8;
		nlCmp.setLayout(rl);

		AbstractFormPart tagFormPart = new TagFormPart(nlCmp, entity,
				tagPropName, newTagMsg);
		tagFormPart.initialize(form);
		form.addPart(tagFormPart);
	}

	private class TagFormPart extends AbstractFormPart {
		private Composite nlCmp;
		private Node entity;
		private String tagPropName;
		private String newTagMsg;

		public TagFormPart(Composite parent, Node entity, String tagPropName,
				String newTagMsg) {
			this.nlCmp = parent;
			this.entity = entity;
			this.tagPropName = tagPropName;
			this.newTagMsg = newTagMsg;
		}

		public void refresh() {
			super.refresh();
			// We redraw the full control at each refresh, might be a more
			// efficient way to do
			Control[] oldChildren = nlCmp.getChildren();
			for (Control child : oldChildren)
				child.dispose();

			boolean isCO = CommonsJcrUtils.isNodeCheckedOutByMe(entity);

			try {
				if (entity.hasProperty(tagPropName)) {
					Value[] values = entity.getProperty(tagPropName)
							.getValues();
					for (final Value value : values) {
						final String tagValue = value.getString();

						Composite tagCmp = toolkit.createComposite(nlCmp,
								SWT.NO_FOCUS);
						tagCmp.setLayout(PeopleUiUtils.gridLayoutNoBorder(2));
						Link link = new Link(tagCmp, SWT.NONE);
						link.setData(PeopleUiConstants.CUSTOM_VARIANT, "tag");
						link.setText(" #<a>" + tagValue + "</a>");
						link.setData(PeopleUiConstants.MARKUP_ENABLED,
								Boolean.TRUE);

						link.addSelectionListener(new SelectionAdapter() {
							private static final long serialVersionUID = 1L;

							@Override
							public void widgetSelected(
									final SelectionEvent event) {
								Node cachedTag = ResourcesJcrUtils.getTagNodeFromValue(
										CommonsJcrUtils.getSession(entity),
										peopleService
												.getResourcesBasePath(PeopleNames.PEOPLE_TAGS),
										tagValue);
								CommandUtils.callCommand(peopleUiService
										.getOpenEntityEditorCmdId(),
										OpenEntityEditor.PARAM_JCR_ID,
										CommonsJcrUtils
												.getIdentifier(cachedTag));
							}
						});

						if (isCO) {
							addDeleteButton(TagFormPart.this, tagCmp, entity,
									value, tagPropName);
						}
					}
				}
				if (isCO) {
					final Text tagTxt = toolkit.createText(nlCmp, "",
							SWT.BORDER);
					tagTxt.setMessage(newTagMsg);
					RowData rd = new RowData(120, SWT.DEFAULT);
					tagTxt.setLayoutData(rd);

					String tagBP = peopleService
							.getResourcesBasePath(PeopleNames.PEOPLE_TAGS);
					final SimpleResourceDropDown tagDD = new SimpleResourceDropDown(
							peopleUiService, entity.getSession(), tagBP, tagTxt);

					tagTxt.addTraverseListener(new TraverseListener() {
						private static final long serialVersionUID = 1L;

						public void keyTraversed(TraverseEvent e) {
							if (e.keyCode == SWT.CR) {
								String newTag = tagDD.getText();
								addTag(tagTxt.getShell(), TagFormPart.this,
										entity, tagPropName, newTag);
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
										entity, tagPropName, newTag);
						}
					});

				}
				nlCmp.layout(false);
				nlCmp.getParent().getParent().layout();

			} catch (RepositoryException re) {
				throw new PeopleException(
						"Error while refreshing mailing list appartenance", re);
			}
		}
	}

	private void addDeleteButton(final AbstractFormPart part, Composite parent,
			final Node entity, final Value value, final String tagPropName) {
		final Button deleteBtn = new Button(parent, SWT.FLAT);
		deleteBtn.setData(PeopleUiConstants.CUSTOM_VARIANT,
				PeopleUiConstants.CSS_FLAT_IMG_BUTTON);
		deleteBtn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		deleteBtn.setImage(PeopleImages.DELETE_BTN_LEFT);
		deleteBtn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				try {
					String tagToRemove = value.getString();
					List<String> tags = new ArrayList<String>();
					Value[] values = entity.getProperty(tagPropName)
							.getValues();
					for (int i = 0; i < values.length; i++) {
						String curr = values[i].getString();
						if (!tagToRemove.equals(curr))
							tags.add(curr);
					}
					entity.setProperty(tagPropName,
							tags.toArray(new String[tags.size()]));
					part.markDirty();
					part.refresh();
					// form.dirtyStateChanged();
				} catch (RepositoryException e) {
					throw new PeopleException("unable to initialise deletion",
							e);
				}
				// for (IFormPart part : form.getParts()) {
				// ((AbstractFormPart) part).markStale();
				// part.refresh();
				// }
			}
		});
	}

	private void addTag(Shell shell, final AbstractFormPart part, Node tagable,
			String tagPropName, String newTag) {
		String msg = null;

		try {
			String tagsParentPath = peopleService
					.getResourcesBasePath(PeopleNames.PEOPLE_TAGS);

			Session session = tagable.getSession();
			// Check if such a tag is already registered
			Node registered = peopleService.getRegisteredTag(session,
					tagsParentPath, newTag);

			if (registered == null) {
				// Ask end user if we create a new tag
				msg = "This tag  \""
						+ newTag
						+ "\" is not yet registered.\n Are you sure you want to create it?";
				if (MessageDialog.openConfirm(shell, "Confirm creation", msg)) {
					registered = peopleService.registerTag(session,
							tagsParentPath, newTag);
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
						msg = "Current entity \""
								+ CommonsJcrUtils.get(tagable,
										Property.JCR_TITLE)
								+ "\" is already tagged as \""
								+ tag.getString()
								+ "\". Nothing has been done.";
						MessageDialog.openError(shell, "Already existing tag",
								msg);
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
			throw new ArgeoException("Unable to set tags", re);
		}
	}
}