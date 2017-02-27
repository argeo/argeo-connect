package org.argeo.tracker.internal.ui.controls;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.ConnectException;
import org.argeo.connect.ConnectNames;
import org.argeo.connect.ui.ConnectUiStyles;
import org.argeo.connect.ui.ConnectUiUtils;
import org.argeo.connect.ui.widgets.ConnectAbstractDropDown;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.tracker.TrackerException;
import org.argeo.tracker.internal.ui.AbstractTrackerEditor;
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
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IManagedForm;

/**
 * Wraps an Abstract form part that enable management of a tag like list in a
 * form editor.
 */
public abstract class TagListFormPart extends Composite {

	private static final long serialVersionUID = 5439358000985800234L;

	// UI Context
	private final AbstractTrackerEditor editor;

	// Context
	private final Node taggable;
	private final String taggablePropName;

	private final String newTagMsg = "Add...";

	// Cache to trace newly created versionable tag like objects.
	private List<String> createdTagPath = new ArrayList<String>();

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
	public TagListFormPart(AbstractTrackerEditor editor, IManagedForm form, Composite parent, int style, Node taggable,
			String taggablePropName) {
		super(parent, style);
		this.editor = editor;
		this.taggable = taggable;
		this.taggablePropName = taggablePropName;

		RowLayout rl = new RowLayout(SWT.HORIZONTAL);
		rl.wrap = true;
		rl.marginLeft = rl.marginTop = rl.marginBottom = 0;
		rl.marginRight = 8;
		this.setLayout(rl);

		AbstractFormPart tagFormPart = new TagFormPart(this);
		// must be refreshed on first pass.
		// tagFormPart.refresh();
		tagFormPart.initialize(form);
		form.addPart(tagFormPart);
	}

	private class TagFormPart extends AbstractFormPart {
		private Composite parentCmp;

		// caches current value to check if the composite must be redrawn
		// private Value[] currValues;

		public TagFormPart(Composite parent) {
			super();
			this.parentCmp = parent;
		}

		@Override
		public void commit(boolean onSave) {
			boolean isEmpty = createdTagPath.isEmpty();
			if (onSave && !isEmpty) {
				// Cannot perform aa check point here: the session has not yet
				// been saved
				// try {
				// Session session = taggable.getSession();
				// VersionManager vm =
				// session.getWorkspace().getVersionManager();
				// for (String currAbsPath : createdTagPath) {
				// if (session.nodeExists(currAbsPath)) {
				// Node currTagNode = session.getNode(currAbsPath);
				// if (currTagNode.isNodeType(NodeType.MIX_VERSIONABLE)) {
				// JcrUtils.updateLastModified(currTagNode);
				// session.save();
				// vm.checkpoint(currAbsPath);
				// }
				// }
				// }
				// } catch (RepositoryException e) {
				// throw new AoException("Unable to commit newly created tags
				// for " + taggable, e);
				// }
				createdTagPath.clear();
			}
			super.commit(onSave);
		}

		public void refresh() {
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
						String tagValue = getEncodedTagValue(tagKey);
						Composite tagCmp = new Composite(parentCmp, SWT.NO_FOCUS);
						tagCmp.setLayout(ConnectUiUtils.noSpaceGridLayout(2));
						Link link = new Link(tagCmp, SWT.NONE);
						CmsUtils.markup(link);
						if (taggablePropName.equals(ConnectNames.CONNECT_TAGS))
							link.setText(" #<a>" + tagValue + "</a>");
						else
							link.setText(" <a>" + tagValue + "</a>");

						link.addSelectionListener(new SelectionAdapter() {
							private static final long serialVersionUID = 1L;

							@Override
							public void widgetSelected(final SelectionEvent event) {
								if (isNewlyCreatedTag(tagKey)) {
									String msg = "This category is still in a draft state.\n" + "Please save first.";
									MessageDialog.openInformation(parentCmp.getShell(), "Forbidden action", msg);
								} else
									callOpenEditor(tagKey);
							}
						});

						if (isCO) {
							addDeleteButton(TagFormPart.this, tagCmp, value);
						}
					}
				}
				if (isCO) {
					final Text tagTxt = new Text(parentCmp, SWT.BORDER);
					tagTxt.setMessage(newTagMsg);
					RowData rd = new RowData(80, SWT.DEFAULT);
					tagTxt.setLayoutData(rd);

					final TagLikeDropDown tagDD = new TagLikeDropDown(tagTxt);

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
					// we must call this so that the row data can compute the OK
					// button size.
					tagTxt.getParent().layout();
					Button okBtn = new Button(parentCmp, SWT.BORDER | SWT.PUSH | SWT.BOTTOM);
					okBtn.setText("OK");
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
				parentCmp.getParent().getParent().layout(true, true);
			} catch (RepositoryException re) {
				throw new ConnectException("Error while refreshing tag like list for " + taggable, re);
			}
			super.refresh();
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
					part.refresh();
					part.markDirty();
				} catch (RepositoryException e) {
					throw new ConnectException("unable to initialise deletion", e);
				}
			}
		});
	}

	private void addTag(Shell shell, final AbstractFormPart part, String newTag) {
		String msg = null;
		try {
			if (!tagExists(newTag)) {
				if (canCreateTag(newTag)) {
					// Ask end user if we create a new tag
					msg = "\"" + newTag + "\" is not yet registered.\n Are you sure you want to create it?";
					if (MessageDialog.openConfirm(shell, "Confirm creation", msg)) {
						Node newTagNode = createTag(newTag);
						createdTagPath.add(newTagNode.getPath());
					} else
						return;
				} else {
					msg = "\"" + newTag + "\" is not yet registered\n"
							+ "and you don't have sufficient rights to create it.\n"
							+ "Please contact a Business Admin and ask him " + "to register it for you if it is valid.";
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
			throw new TrackerException("Unable to set " + taggablePropName + " on " + taggable, re);
		}
	}

	private class TagLikeDropDown extends ConnectAbstractDropDown {

		public TagLikeDropDown(Text text) {
			super(text);
			init();
		}

		@Override
		protected List<String> getFilteredValues(String filter) {
			return TagListFormPart.this.getFilteredValues(filter);
		}
	}

	private boolean isNewlyCreatedTag(String tagKey) {
		try {
			Session session = taggable.getSession();
			for (String absPath : createdTagPath) {
				if (session.nodeExists(absPath)) {
					String currTagKey = getTagKey(session.getNode(absPath));
					if (tagKey.equals(currTagKey))
						return true;
				}
			}
			return false;
		} catch (RepositoryException re) {
			throw new TrackerException("Unable to set " + taggablePropName + " on " + taggable, re);
		}

	}

	/**
	 * Overwrite to provide a filtered list of relevant possible new values
	 */
	abstract protected List<String> getFilteredValues(String filter);

	/**
	 * Creates a new value in the relevant catalog, we don't expect the session
	 * to be saved
	 */
	abstract protected Node createTag(String tagKey) throws RepositoryException;

	/**
	 * Overwrite to perform a check prior to effectively creating the new value
	 */
	protected boolean canCreateTag(String tagKey) {
		return true;
	}

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