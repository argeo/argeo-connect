package org.argeo.connect.workbench;

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
import org.argeo.cms.ui.eclipse.forms.AbstractFormPart;
import org.argeo.cms.ui.eclipse.forms.FormToolkit;
import org.argeo.cms.ui.workbench.util.CommandUtils;
import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.ConnectException;
import org.argeo.connect.resources.ResourcesNames;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.ui.ConnectUiStyles;
import org.argeo.connect.ui.ConnectUiUtils;
import org.argeo.connect.ui.widgets.TagLikeDropDown;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.workbench.commands.OpenEntityEditor;
import org.argeo.connect.workbench.parts.AbstractConnectEditor;
import org.argeo.eclipse.ui.EclipseUiUtils;
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
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
//import org.eclipse.ui.forms.AbstractFormPart;
//import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Wraps an Abstract form part that enable management of a tag like list in a
 * form editor.
 */
public class TagLikeListSmallPart extends Composite {
	private static final long serialVersionUID = -312141685147619814L;
	private final static Log log = LogFactory.getLog(TagLikeListSmallPart.class);

	// UI Context
	private final AbstractConnectEditor editor;
	private final FormToolkit toolkit;
	private final String newTagMsg;

	// Context
	private final ResourcesService resourcesService;
	private final SystemWorkbenchService systemWorkbenchService;
	private final Node taggable;
	private final Node tagParent;
	private final String tagId;
	private String tagCodePropName = null;
	private String cssStyle = ConnectUiStyles.ENTITY_HEADER;
	private final String taggablePropName;

	// Deduced from the context, shortcut for this class
	private final Session session;

	/**
	 * 
	 * @param parent
	 * @param style
	 * @param toolkit
	 * @param form
	 * @param resourcesService
	 * @param systemWorkbenchService
	 * @param taggable
	 * @param tagId
	 * @param newTagMsg
	 */
	public TagLikeListSmallPart(AbstractConnectEditor editor, Composite parent, int style,
			ResourcesService resourcesService, SystemWorkbenchService systemWorkbenchService, String tagId, Node taggable,
			String taggablePropName, String newTagMsg) {
		super(parent, style);
		this.editor = editor;
		this.toolkit = editor.getFormToolkit();
		this.resourcesService = resourcesService;
		this.systemWorkbenchService = systemWorkbenchService;
		this.tagId = tagId;
		this.taggable = taggable;
		this.taggablePropName = taggablePropName;
		this.newTagMsg = newTagMsg;

		// Cache some context object to ease implementation
		session = ConnectJcrUtils.getSession(taggable);
		tagParent = resourcesService.getTagLikeResourceParent(session, tagId);

		try {
			if (tagParent.hasProperty(ResourcesNames.RESOURCES_TAG_CODE_PROP_NAME))
				tagCodePropName = tagParent.getProperty(ResourcesNames.RESOURCES_TAG_CODE_PROP_NAME).getString();
		} catch (RepositoryException e) {
			throw new ConnectException("unable to get tag prop name for " + tagParent, e);
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
					throw new ConnectException("Error while committing tagrefreshing tag like list for " + taggable,
							re);
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
							tagValue = resourcesService.getEncodedTagValue(session, tagId, tagKey);
						else
							tagValue = tagKey;

						Composite tagCmp = toolkit.createComposite(parentCmp, SWT.NO_FOCUS);
						tagCmp.setLayout(ConnectUiUtils.noSpaceGridLayout(2));
						Link link = new Link(tagCmp, SWT.NONE);
						CmsUtils.markup(link);

						if (taggablePropName.equals(ResourcesNames.CONNECT_TAGS)) {
							link.setText(" #<a>" + tagValue + "</a>");
							// } else if
							// (taggablePropName.equals(PeopleNames.PEOPLE_MAILING_LISTS))
							// {
							// link.setText(" @<a>" + tagValue + "</a>");
						} else
							link.setText(" <a>" + tagValue + "</a>");

						CmsUtils.style(link, cssStyle);

						link.addSelectionListener(new SelectionAdapter() {
							private static final long serialVersionUID = 1L;

							@Override
							public void widgetSelected(final SelectionEvent event) {
								Node tag = resourcesService.getRegisteredTag(tagParent, tagKey);

								try {
									if (createdTagPath.contains(tag.getPath())) {
										String msg = "This category is still in a draft state.\n"
												+ "Please save first.";
										MessageDialog.openInformation(parentCmp.getShell(), "Forbidden action", msg);
									} else
										CommandUtils.callCommand(systemWorkbenchService.getOpenEntityEditorCmdId(),
												OpenEntityEditor.PARAM_JCR_ID, ConnectJcrUtils.getIdentifier(tag));
								} catch (RepositoryException e) {
									throw new ConnectException("unable to get path for resource tag node " + tag
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

					final TagLikeDropDown tagDD = new TagLikeDropDown(session, resourcesService, tagId, tagTxt);

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
				throw new ConnectException("Error while refreshing tag like list for " + taggable, re);
			}
		}

		private void addTag(Shell shell, final AbstractFormPart part, String newTag) {
			String msg = null;
			try {
				Session session = taggable.getSession();
				// Retrieve code from value
				if (tagCodePropName != null)
					newTag = resourcesService.getEncodedTagCodeFromValue(session, tagId, newTag);

				// Check if a tag with such a key is already registered
				Node registered = resourcesService.getRegisteredTag(tagParent, newTag);

				if (registered == null) {
					if (resourcesService.canCreateTag(session)) {

						// Ask end user if we create a new tag
						msg = "\"" + newTag + "\" is not yet registered.\n Are you sure you want to create it?";
						if (MessageDialog.openConfirm(shell, "Confirm creation", msg)) {
							registered = resourcesService.registerTag(session, tagId, newTag);
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
							msg = "\"" + ConnectJcrUtils.get(taggable, Property.JCR_TITLE)
									+ "\" is already linked with \"" + tag.getString() + "\". Nothing has been done.";
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
				throw new ConnectException("Unable to set " + taggablePropName + " on " + taggable, re);
			}
		}
	}

	private void addDeleteButton(final AbstractFormPart part, Composite parent, final Value value) {
		final Button deleteBtn = new Button(parent, SWT.FLAT);
		CmsUtils.style(deleteBtn, ConnectUiStyles.SMALL_DELETE_BTN);
		deleteBtn.setLayoutData(new GridData(8, 8));
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
					throw new ConnectException("unable to initialise deletion", e);
				}
			}
		});
	}
}