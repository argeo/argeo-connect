package org.argeo.connect.people.rap.editors.parts;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.VersionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.cms.ui.workbench.util.CommandUtils;
import org.argeo.cms.util.CmsUtils;
import org.argeo.cms.util.useradmin.UserAdminUtils;
import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.ResourceService;
import org.argeo.connect.people.UserAdminService;
import org.argeo.connect.people.rap.PeopleRapImages;
import org.argeo.connect.people.rap.PeopleStyles;
import org.argeo.connect.people.rap.PeopleWorkbenchService;
import org.argeo.connect.people.rap.commands.OpenEntityEditor;
import org.argeo.connect.people.rap.composites.dropdowns.TagLikeDropDown;
import org.argeo.connect.people.rap.editors.util.AbstractPeopleEditor;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.util.JcrUiUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.rap.rwt.RWT;
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
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Wraps an Abstract form part that enable management of a tag like list in a
 * form editor.
 */
public class TagLikeListSmallPart extends Composite {
	private static final long serialVersionUID = -312141685147619814L;
	private final static Log log = LogFactory.getLog(TagLikeListSmallPart.class);

	// UI Context
	private final AbstractPeopleEditor editor;
	private final FormToolkit toolkit;
	private final String newTagMsg;

	// Context
	private final PeopleService peopleService;
	private final PeopleWorkbenchService peopleWorkbenchService;
	private final Node taggable;
	private final Node tagParent;
	private final String tagId;
	private String tagCodePropName = null;
	private String cssStyle = PeopleStyles.PEOPLE_CLASS_ENTITY_HEADER;
	private final String taggablePropName;

	// Deduced from the context, shortcut for this class
	private final ResourceService resourceService;
	private final UserAdminService userService;
	private final Session session;

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
	public TagLikeListSmallPart(AbstractPeopleEditor editor, Composite parent, int style, PeopleService peopleService,
			PeopleWorkbenchService peopleWorkbenchService, String tagId, Node taggable, String taggablePropName,
			String newTagMsg) {
		super(parent, style);
		this.editor = editor;
		this.toolkit = editor.getFormToolkit();
		this.peopleService = peopleService;
		this.peopleWorkbenchService = peopleWorkbenchService;
		this.tagId = tagId;
		this.taggable = taggable;
		this.taggablePropName = taggablePropName;
		this.newTagMsg = newTagMsg;

		// Cache some context object to ease implementation
		this.resourceService = peopleService.getResourceService();
		this.userService = peopleService.getUserAdminService();
		session = JcrUiUtils.getSession(taggable);
		tagParent = resourceService.getTagLikeResourceParent(session, tagId);

		try {
			if (tagParent.hasProperty(PeopleNames.PEOPLE_TAG_CODE_PROP_NAME))
				tagCodePropName = tagParent.getProperty(PeopleNames.PEOPLE_TAG_CODE_PROP_NAME).getString();
		} catch (RepositoryException e) {
			throw new PeopleException("unable to get tag prop name for " + tagParent, e);
		}
		RowLayout rl = new RowLayout(SWT.HORIZONTAL);
		rl.wrap = true;
		rl.marginLeft = rl.marginTop = rl.marginBottom = 0;
		rl.marginRight = 8;
		this.setLayout(rl);

		AbstractFormPart tagFormPart = new TagFormPart(this);
		// must be refreshed on first pass.
		tagFormPart.refresh();
		tagFormPart.initialize(editor.getManagedForm());
		editor.getManagedForm().addPart(tagFormPart);
	}

	private class TagFormPart extends AbstractFormPart {
		private Composite parentCmp;

		// Cache to trace newly created versionable tag like objects.
		private List<String> createdTagPath = new ArrayList<String>();

		// caches current value to check if the composite must be redrawn
		// private Value[] currValues;

		public TagFormPart(Composite parent) {
			this.parentCmp = parent;
		}

		@Override
		public void commit(boolean onSave) {

			boolean isEmpty = createdTagPath.isEmpty();
			if (onSave && !isEmpty) {
				try {
					Session session = taggable.getSession();
					if (session.hasPendingChanges()) {
						log.warn("Session have been saved before commit " + "of newly created tags when saving node "
								+ taggable);
						session.save();
					}
					VersionManager manager = session.getWorkspace().getVersionManager();
					for (String path : createdTagPath) {
						Node newTag = session.getNode(path);
						if (newTag.isCheckedOut()) {
							manager.checkin(path);
						}
					}
					createdTagPath.clear();
				} catch (RepositoryException re) {
					throw new PeopleException("Error while committing tagrefreshing tag like list for " + taggable, re);
				}
			}
			super.commit(onSave);
		}

		public void refresh() {
			super.refresh();
			if (parentCmp.isDisposed())
				return;

			// We redraw the full control at each refresh, might be a more
			// efficient way to do
			CmsUtils.clear(parentCmp);
			boolean isCO = editor.isEditing();

			try {
				if (taggable.hasProperty(taggablePropName)) {
					Value[] values = taggable.getProperty(taggablePropName).getValues();
					for (final Value value : values) {
						final String tagKey = value.getString();

						String tagValue;
						if (tagCodePropName != null)
							tagValue = resourceService.getEncodedTagValue(session, tagId, tagKey);
						else
							tagValue = tagKey;

						Composite tagCmp = toolkit.createComposite(parentCmp, SWT.NO_FOCUS);
						tagCmp.setLayout(PeopleUiUtils.noSpaceGridLayout(2));
						Link link = new Link(tagCmp, SWT.NONE);
						link.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);

						if (taggablePropName.equals(PeopleNames.PEOPLE_TAGS)) {
							link.setText(" #<a>" + tagValue + "</a>");
						} else if (taggablePropName.equals(PeopleNames.PEOPLE_MAILING_LISTS)) {
							link.setText(" @<a>" + tagValue + "</a>");
						} else
							link.setText(" <a>" + tagValue + "</a>");

						CmsUtils.style(link, cssStyle);

						link.addSelectionListener(new SelectionAdapter() {
							private static final long serialVersionUID = 1L;

							@Override
							public void widgetSelected(final SelectionEvent event) {
								Node tag = peopleService.getResourceService().getRegisteredTag(tagParent, tagKey);

								try {
									if (createdTagPath.contains(tag.getPath())) {
										String msg = "This category is still in a draft state.\n"
												+ "Please save first.";
										MessageDialog.openInformation(parentCmp.getShell(), "Forbidden action", msg);
									} else
										CommandUtils.callCommand(peopleWorkbenchService.getOpenEntityEditorCmdId(),
												OpenEntityEditor.PARAM_JCR_ID, JcrUiUtils.getIdentifier(tag));
								} catch (RepositoryException e) {
									throw new PeopleException("unable to get path for resource tag node " + tag
											+ " while editing " + taggable, e);
								}
							}
						});

						if (isCO) {
							addDeleteButton(TagFormPart.this, tagCmp, value);
						}
					}
				}
				if (isCO) {
					final Text tagTxt = toolkit.createText(parentCmp, "", SWT.BORDER);
					tagTxt.setMessage(newTagMsg);
					RowData rd = new RowData(80, SWT.DEFAULT);
					tagTxt.setLayoutData(rd);

					final TagLikeDropDown tagDD = new TagLikeDropDown(session, resourceService, tagId, tagTxt);

					tagTxt.addTraverseListener(new TraverseListener() {
						private static final long serialVersionUID = 1L;

						public void keyTraversed(TraverseEvent e) {
							if (e.keyCode == SWT.CR) {
								String newTag = tagDD.getText();
								addTag(tagTxt.getShell(), TagFormPart.this, newTag);
								e.doit = false;
								// if (!tagTxt.isDisposed())
								// tagDD.reset("");
								// tagTxt.setText("");
							}
						}
					});
					// we must call this so that the row data can copute the OK
					// button size.
					tagTxt.getParent().layout();

					Button okBtn = toolkit.createButton(parentCmp, "OK", SWT.BORDER | SWT.PUSH | SWT.BOTTOM);
					// Button okBtn = new Button(parentCmp, SWT.BORDER |
					// SWT.PUSH
					// | SWT.BOTTOM);
					// okBtn.setText("OK");
					rd = new RowData(SWT.DEFAULT, tagTxt.getSize().y - 2);
					okBtn.setLayoutData(rd);

					okBtn.addSelectionListener(new SelectionAdapter() {
						private static final long serialVersionUID = 2780819012423622369L;

						@Override
						public void widgetSelected(SelectionEvent e) {
							String newTag = tagDD.getText();
							if (EclipseUiUtils.isEmpty(newTag))
								return;
							else
								addTag(parentCmp.getShell(), TagFormPart.this, newTag);
						}
					});
				}
				parentCmp.layout();
				parentCmp.getParent().getParent().layout();
			} catch (RepositoryException re) {
				throw new PeopleException("Error while refreshing tag like list for " + taggable, re);
			}
		}

		private void addTag(Shell shell, final AbstractFormPart part, String newTag) {
			String msg = null;
			try {
				Session session = taggable.getSession();
				// Retrieve code from value
				if (tagCodePropName != null)
					newTag = resourceService.getEncodedTagCodeFromValue(session, tagId, newTag);

				// Check if a tag with such a key is already registered
				Node registered = peopleService.getResourceService().getRegisteredTag(tagParent, newTag);

				if (registered == null) {
					boolean canAdd = !"true"
							.equals(peopleService.getConfigProperty(PeopleConstants.PEOPLE_PROP_PREVENT_TAG_ADDITION))
							|| UserAdminUtils.isUserInRole(PeopleConstants.ROLE_BUSINESS_ADMIN);
					if (canAdd) {
						// Ask end user if we create a new tag
						msg = "\"" + newTag + "\" is not yet registered.\n Are you sure you want to create it?";
						if (MessageDialog.openConfirm(shell, "Confirm creation", msg)) {
							registered = peopleService.getResourceService().registerTag(session, tagId, newTag);
							if (registered.isNodeType(NodeType.MIX_VERSIONABLE))
								createdTagPath.add(registered.getPath());
						} else
							return;
					} else {
						msg = "\"" + newTag + "\" is not yet registered\n"
								+ "and you don't have sufficient rights to create it.\n"
								+ "Please contact a Business Admin and ask him "
								+ "to register it for you if it is valid.";
						MessageDialog.openError(shell, "Unvalid choice", msg);
						return;
					}
				}

				Value[] values;
				String[] valuesStr;
				if (taggable.hasProperty(taggablePropName)) {
					values = taggable.getProperty(taggablePropName).getValues();

					// Check duplicates
					for (Value tag : values) {
						String curTagUpperCase = tag.getString().toUpperCase().trim();
						if (newTag.toUpperCase().trim().equals(curTagUpperCase)) {
							msg = "\"" + JcrUiUtils.get(taggable, Property.JCR_TITLE) + "\" is already linked with \""
									+ tag.getString() + "\". Nothing has been done.";
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
				taggable.setProperty(taggablePropName, valuesStr);
				part.markDirty();
				part.refresh();
			} catch (RepositoryException re) {
				throw new PeopleException("Unable to set " + taggablePropName + " on " + taggable, re);
			}
		}
	}

	private void addDeleteButton(final AbstractFormPart part, Composite parent, final Value value) {
		final Button deleteBtn = new Button(parent, SWT.FLAT);
		CmsUtils.style(deleteBtn, PeopleStyles.FLAT_BTN);
		deleteBtn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		deleteBtn.setImage(PeopleRapImages.DELETE_BTN_LEFT);
		deleteBtn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				try {
					String tagToRemove = value.getString();
					List<String> tags = new ArrayList<String>();
					Value[] values = taggable.getProperty(taggablePropName).getValues();
					for (int i = 0; i < values.length; i++) {
						String curr = values[i].getString();
						if (!tagToRemove.equals(curr))
							tags.add(curr);
					}
					taggable.setProperty(taggablePropName, tags.toArray(new String[tags.size()]));
					part.markDirty();
					part.refresh();
				} catch (RepositoryException e) {
					throw new PeopleException("unable to initialise deletion", e);
				}
			}
		});
	}
}