package org.argeo.connect.ui.parts;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.VersionManager;

import org.argeo.cms.ui.dialogs.CmsMessageDialog;
import org.argeo.cms.ui.eclipse.forms.AbstractFormPart;
import org.argeo.cms.ui.eclipse.forms.FormToolkit;
import org.argeo.cms.ui.util.CmsUiUtils;
import org.argeo.connect.ConnectException;
import org.argeo.connect.resources.ResourcesNames;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.ui.ConnectEditor;
import org.argeo.connect.ui.ConnectImages;
import org.argeo.connect.ui.ConnectUiStyles;
import org.argeo.connect.ui.ConnectUiUtils;
import org.argeo.connect.ui.SystemWorkbenchService;
import org.argeo.connect.ui.widgets.TagLikeDropDown;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;
import org.argeo.people.ui.PeopleMsg;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
//import org.eclipse.ui.forms.AbstractFormPart;
//import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Wraps an Abstract form part that enable management of a tag like list in a
 * form editor.
 */
public class TagLikeListPart extends Composite {
	private static final long serialVersionUID = -312141685147619814L;
	// private final static Log log = LogFactory.getLog(TagLikeListPart.class);

	// UI Context
	private final ConnectEditor editor;
	private final FormToolkit toolkit;
	private final String newTagMsg;

	// Context
	private final ResourcesService resourcesService;
	private final SystemWorkbenchService systemWorkbenchService;
	private final Node taggable;
	private final Node tagParent;
	private final String tagId;
	private final String taggablePropName;

	// Deduced from the context, shortcut for this class
	private final Session session;

	// Cache to trace newly created versionable tag like objects.
	// private final List<String> createdTagPath = new ArrayList<String>();

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
	public TagLikeListPart(ConnectEditor editor, Composite parent, int style, ResourcesService resourcesService,
			SystemWorkbenchService systemWorkbenchService, String tagId, Node taggable, String taggablePropName,
			String newTagMsg) {
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

		RowLayout rl = new RowLayout(SWT.HORIZONTAL);
		rl.wrap = true;
		rl.marginLeft = rl.marginTop = rl.marginBottom = 0;
		rl.marginRight = 8;
		this.setLayout(rl);

		AbstractFormPart tagFormPart = new TagFormPart(this);
		tagFormPart.initialize(editor.getManagedForm());
		editor.getManagedForm().addPart(tagFormPart);
	}

	private class TagFormPart extends AbstractFormPart {
		private Composite parentCmp;

		public TagFormPart(Composite parent) {
			this.parentCmp = parent;
		}

		@Override
		public void commit(boolean onSave) {

			// boolean isEmpty = createdTagPath.isEmpty();
			// if (onSave && !isEmpty) {
			// try {
			// Session session = taggable.getSession();
			// if (session.hasPendingChanges()) {
			// log.warn("Session have been saved before commit of newly created
			// tags when saving node "
			// + taggable);
			// session.save();
			// }
			// VersionManager manager =
			// session.getWorkspace().getVersionManager();
			// for (String path : createdTagPath) {
			// Node newTag = session.getNode(path);
			// if (newTag.isCheckedOut()) {
			// manager.checkin(path);
			// }
			// }
			// createdTagPath.clear();
			// } catch (RepositoryException re) {
			// throw new ConnectException("Error while committing tagrefreshing
			// tag like list for " + taggable,
			// re);
			// }
			// }
			super.commit(onSave);
		}

		public void refresh() {
			super.refresh();
			EclipseUiUtils.clear(parentCmp);

			boolean isCO = editor.isEditing();

			try {
				if (taggable.hasProperty(taggablePropName)) {
					Value[] values = taggable.getProperty(taggablePropName).getValues();
					for (final Value value : values) {
						final String tagValue = value.getString();

						Composite tagCmp = toolkit.createComposite(parentCmp, SWT.NO_FOCUS);
						tagCmp.setLayout(ConnectUiUtils.noSpaceGridLayout(2));
						// Label hashTagLbl = new Label(tagCmp, SWT.NONE);
						// hashTagLbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
						// hashTagLbl.setImage(ConnectImages.TAG);
						Link link = new Link(tagCmp, SWT.NONE);
						// link.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

						CmsUiUtils.markup(link);
						CmsUiUtils.style(link, ConnectUiStyles.ENTITY_HEADER);
						link.setText(getLinkText(taggablePropName, tagValue));
						link.addSelectionListener(new SelectionAdapter() {
							private static final long serialVersionUID = 1L;

							@Override
							public void widgetSelected(final SelectionEvent event) {
								Node tag = resourcesService.getRegisteredTag(tagParent, tagValue);
								// try {
								// if (createdTagPath.contains(tag.getPath())) {
								// String msg = "This category is still in a
								// draft state.\n"
								// + "Please save first.";
								// MessageDialog.openInformation(parentCmp.getShell(),
								// "Forbidden action", msg);
								// } else

								// CommandUtils.callCommand(systemWorkbenchService.getOpenEntityEditorCmdId(),
								// ConnectEditor.PARAM_JCR_ID, ConnectJcrUtils.getIdentifier(tag));
								systemWorkbenchService.openEntityEditor(tag);

								// } catch (RepositoryException e) {
								// throw new ConnectException("unable to get
								// path for resource tag node " + tag
								// + " while editing " + taggable, e);
								// }
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
					RowData rd = new RowData(120, SWT.DEFAULT);
					tagTxt.setLayoutData(rd);

					final TagLikeDropDown tagDD = new TagLikeDropDown(taggable.getSession(), resourcesService, tagId,
							tagTxt);

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

					tagTxt.getParent().layout();

					Button okBtn = toolkit.createButton(parentCmp, "OK", SWT.BORDER | SWT.PUSH | SWT.BOTTOM);
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
				parentCmp.layout(false);
				parentCmp.getParent().getParent().layout();

			} catch (RepositoryException re) {
				throw new ConnectException("Error while refreshing tag like list for " + taggable, re);
			}
		}
	}

	protected String getLinkText(String taggablePropName, String value) {
		if (taggablePropName.equals(ResourcesNames.CONNECT_TAGS))
			return " <a>#" + value + "</a>";
		else
			return " <a>" + value + "</a>";
	}

	private void addDeleteButton(final AbstractFormPart part, Composite parent, final Value value) {
		final Button deleteBtn = new Button(parent, SWT.FLAT);
		deleteBtn.setImage(ConnectImages.DELETE_SMALL);
		// deleteBtn.setText("-");
		// CmsUiUtils.style(deleteBtn, ConnectUiStyles.SMALL_DELETE_BTN);
		// deleteBtn.setLayoutData(new GridData(24, 24));
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
					part.refresh();
					part.markDirty();
				} catch (RepositoryException e) {
					throw new ConnectException("unable to initialise deletion", e);
				}
			}
		});
	}

	private Node registerNewTag(String newTag) {
		// We must create the tag in a distinct session, they are stored in a
		// distinct subtree of the workspace as the business object.
		// thus session.save will fail for user that does not have read
		// privileges on the root node of the workspace (~ all users except
		// sysadmin...)
		Session tagSession = null;
		try {
			Session session = taggable.getSession();
			tagSession = session.getRepository().login();
			Node registered = resourcesService.registerTag(tagSession, tagId, newTag);
			tagSession.save();
			if (registered.isNodeType(NodeType.MIX_VERSIONABLE)) {
				VersionManager manager = session.getWorkspace().getVersionManager();
				manager.checkpoint(registered.getPath());
			}
			session.refresh(true);
			Node newTagNode = session.getNode(registered.getPath());
			return newTagNode;
		} catch (RepositoryException re) {
			throw new ConnectException("Unable to set " + taggablePropName + " on " + taggable, re);
		} finally {
			JcrUtils.logoutQuietly(tagSession);
		}
	}

	private void addTag(Shell shell, final AbstractFormPart part, String newTag) {
		String msg = null;

		try {
			Session session = taggable.getSession();
			// Check if such a tag is already registered
			Node registered = resourcesService.getRegisteredTag(tagParent, newTag);

			if (registered == null) {
				if (resourcesService.canCreateTag(session)) {
					// Ask end user if we create a new tag
					// msg = "\"" + newTag + "\" is not yet registered.\n Are you sure you want to
					// create it?";
					msg = PeopleMsg.confirmNewTag.format(new String[] { newTag });
					// if (MessageDialog.openConfirm(shell, "Confirm creation", msg)) {
					if (CmsMessageDialog.openConfirm(msg)) {
						registered = registerNewTag(newTag);
					} else
						return;
				} else {
					// msg = "\"" + newTag + "\" is not yet registered "
					// + "and you don't have sufficient rights to create it.\n"
					// + "Please contact a Business Admin and ask him " + "to register it for you if
					// it is valid.";
					msg = PeopleMsg.cannotCreateTag.format(new String[] { newTag });
					CmsMessageDialog.openWarning(msg);
					// MessageDialog.openError(shell, "Unvalid choice", msg);
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
						msg = "\"" + ConnectJcrUtils.get(taggable, Property.JCR_TITLE) + "\" is already linked with \""
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
			part.refresh();
			part.markDirty();
		} catch (RepositoryException re) {
			throw new ConnectException("Unable to set " + taggablePropName + " on " + taggable, re);
		}
	}
}
