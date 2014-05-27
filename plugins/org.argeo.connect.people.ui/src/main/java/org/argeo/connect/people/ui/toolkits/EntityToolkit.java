package org.argeo.connect.people.ui.toolkits;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.argeo.ArgeoException;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.ui.PeopleImages;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.PeopleUiService;
import org.argeo.connect.people.ui.commands.OpenEntityEditor;
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
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Centralizes creation of common controls (typically Text and composite widget)
 * for entity, to be used in various forms.
 */
public class EntityToolkit {
	// private final static Log log = LogFactory.getLog(EntityToolkit.class);

	private PeopleService peopleService;
	private PeopleUiService peopleUiService;
	private final FormToolkit toolkit;
	private final IManagedForm form;

	public EntityToolkit(FormToolkit toolkit, IManagedForm form,
			PeopleService peopleService, PeopleUiService peopleUiService) {
		this.toolkit = toolkit;
		this.form = form;
		this.peopleUiService = peopleUiService;
		this.peopleService = peopleService;
	}

	// ////////////////
	// Various panels
	public void populateTagPanel(final Composite parent, final Node entity,
			final String tagPropName, final String newTagMsg) {
		parent.setLayout(PeopleUiUtils.gridLayoutNoBorder(2));

		GridData gd;

		final Composite nlCmp = toolkit.createComposite(parent);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		nlCmp.setLayoutData(gd);
		RowLayout rl = new RowLayout(SWT.HORIZONTAL);
		rl.wrap = true;
		rl.marginHeight = 0;
		rl.marginLeft = 5;
		rl.marginRight = 0;
		nlCmp.setLayout(rl);

		final Text tagTxt = toolkit.createText(parent, "", SWT.BORDER);
		tagTxt.setMessage(newTagMsg);
		gd = new GridData(SWT.CENTER, SWT.TOP, false, false);
		gd.minimumWidth = 120;
		gd.widthHint = 130;
		tagTxt.setLayoutData(gd);

		tagTxt.addTraverseListener(new TraverseListener() {
			private static final long serialVersionUID = 1L;

			public void keyTraversed(TraverseEvent e) {
				if (e.keyCode == SWT.CR) {
					String newTag = tagTxt.getText();
					addTag(entity, tagPropName, newTag);
					e.doit = false;
					tagTxt.setText("");
				}
			}
		});

		AbstractFormPart editPart = new AbstractFormPart() {
			public void refresh() {
				super.refresh();
				// We redraw the full control at each refresh, might be a more
				// efficient way to do
				Control[] oldChildren = nlCmp.getChildren();
				for (Control child : oldChildren)
					child.dispose();

				try {
					if (entity.hasProperty(tagPropName)) {
						Value[] values = entity.getProperty(tagPropName)
								.getValues();
						for (final Value value : values) {
							final String tagValue = value.getString();

							Composite tagCmp = toolkit.createComposite(nlCmp,
									SWT.NO_FOCUS);
							tagCmp.setLayout(PeopleUiUtils
									.gridLayoutNoBorder(2));
							Link link = new Link(tagCmp, SWT.NONE);
							link.setData(PeopleUiConstants.CUSTOM_VARIANT,
									"tag");
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

							if (CommonsJcrUtils.isNodeCheckedOutByMe(entity)) {
								addDeleteButton(tagCmp, entity, value,
										tagPropName);
							}
						}

						// nlCmp.pack();
						nlCmp.layout(false);
						// parent.getParent().pack();
						parent.getParent().layout();
					}
					tagTxt.setVisible(CommonsJcrUtils
							.isNodeCheckedOutByMe(entity));
				} catch (RepositoryException re) {
					throw new PeopleException(
							"Error while refreshing mailing list appartenance",
							re);
				}
			}
		};

		editPart.initialize(form);
		form.addPart(editPart);
	}

	private void addDeleteButton(Composite parent, final Node entity,
			final Value value, final String tagPropName) {
		final Button deleteBtn = new Button(parent, SWT.FLAT);
		deleteBtn.setData(PeopleUiConstants.CUSTOM_VARIANT,
				PeopleUiConstants.CSS_FLAT_IMG_BUTTON);
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

					boolean wasCheckedOut = CommonsJcrUtils
							.isNodeCheckedOutByMe(entity);
					if (!wasCheckedOut)
						CommonsJcrUtils.checkout(entity);
					entity.setProperty(tagPropName,
							tags.toArray(new String[tags.size()]));
					if (wasCheckedOut)
						form.dirtyStateChanged();
					else
						CommonsJcrUtils.saveAndCheckin(entity);
				} catch (RepositoryException e) {
					throw new PeopleException("unable to initialise deletion",
							e);
				}
				for (IFormPart part : form.getParts()) {
					((AbstractFormPart) part).markStale();
					part.refresh();
				}
			}
		});
	}

	private void addTag(Node tagable, String tagPropName, String newTag) {
		try {
			Value[] values;
			String[] valuesStr;
			String errMsg = null;
			if (tagable.hasProperty(tagPropName)) {
				values = tagable.getProperty(tagPropName).getValues();

				// Check dupplicate
				for (Value tag : values) {
					String curTagUpperCase = tag.getString().toUpperCase()
							.trim();
					if (newTag.toUpperCase().trim().equals(curTagUpperCase)) {
						errMsg = "This tag  \"" + newTag
								+ "\" already exists as \"" + tag.getString()
								+ "\" and thus could not be added.";
						MessageDialog.openError(PeopleUiPlugin.getDefault()
								.getWorkbench().getActiveWorkbenchWindow()
								.getShell(), "Dupplicates", errMsg);
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

			boolean wasCheckedout = CommonsJcrUtils.isNodeCheckedOut(tagable);
			if (!wasCheckedout)
				CommonsJcrUtils.checkout(tagable);

			tagable.setProperty(tagPropName, valuesStr);

			// register tag if needed.
			String tagsParentPath = peopleService
					.getResourcesBasePath(PeopleNames.PEOPLE_TAGS);
			Node tagsParent = tagable.getSession().getNode(tagsParentPath);
			peopleService.registerTag(tagsParent, newTag);

			if (!wasCheckedout)
				CommonsJcrUtils.saveAndCheckin(tagable);
			else
				form.dirtyStateChanged();
			for (IFormPart part : form.getParts()) {
				((AbstractFormPart) part).markStale();
				part.refresh();
			}
		} catch (RepositoryException re) {
			throw new ArgeoException("Unable to set tags", re);
		}

	}
}