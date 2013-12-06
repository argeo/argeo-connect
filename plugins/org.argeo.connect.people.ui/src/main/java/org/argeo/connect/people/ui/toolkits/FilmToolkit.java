package org.argeo.connect.people.ui.toolkits;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.argeo.ArgeoException;
import org.argeo.connect.film.FilmNames;
import org.argeo.connect.film.core.FilmJcrUtils;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.ui.PeopleImages;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.dialogs.PickUpCountryDialog;
import org.argeo.connect.people.ui.dialogs.PickUpLangDialog;
import org.argeo.connect.people.ui.utils.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.ResourcesJcrUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Centralize the creation of the different editors panels for films.
 */
public class FilmToolkit extends EntityToolkit implements FilmNames {
	// private final static Log log = LogFactory.getLog(FilmToolkit.class);

	// private final static String LANG_KEY = "lang";
	// private final static String DEFAULT_LANG_KEY = "default";

	private final FormToolkit toolkit;
	private final IManagedForm form;

	public FilmToolkit(FormToolkit toolkit, IManagedForm form) {
		super(toolkit, form);
		// formToolkit
		// managedForm
		this.toolkit = toolkit;
		this.form = form;
	}

	/** Populate a panel with a list synopsis. */
	public void populateSynopsisPanel(Composite panel, final Node entity,
			List<String> langIsos) {
		panel.setLayout(PeopleUiUtils.gridLayoutNoBorder());
		try {
			for (String iso : langIsos) {
				Node currNode = FilmJcrUtils.getSynopsisNode(entity, iso);
				// force creation to avoid npe and ease form life cycle
				if (currNode == null)
					currNode = FilmJcrUtils.addOrUpdateSynopsisNode(entity,
							null, null, iso);
				Composite composite = toolkit.createComposite(panel);
				composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
						true));
				populateSingleSynopsisCmp(
						composite,
						currNode,
						ResourcesJcrUtils.getLangEnLabelFromIso(
								entity.getSession(), iso));
			}
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to create " + "synopsis panel", e);
		}
	}

	/** Populate a synopsis composite. */
	private void populateSingleSynopsisCmp(Composite panel,
			final Node synopsisNode, String titleLabel) {
		panel.setLayout(new GridLayout());

		Group group = new Group(panel, 0);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		group.setText(titleLabel);
		group.setLayout(new GridLayout());

		// Short synopsis
		final Text shortSynTxt = toolkit.createText(group, "", SWT.BORDER
				| SWT.MULTI | SWT.WRAP);
		GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false);
		gd.heightHint = 50;
		shortSynTxt.setLayoutData(gd);
		shortSynTxt
				.setToolTipText("Enter a short " + titleLabel + " synopsis.");

		// Long synopsis
		final Text synopsisTxt = toolkit.createText(group, "", SWT.BORDER
				| SWT.MULTI | SWT.WRAP);
		synopsisTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		synopsisTxt.setToolTipText("Enter the full length " + titleLabel
				+ " synopsis.");

		final AbstractFormPart editPart = new AbstractFormPart() {
			public void refresh() {
				super.refresh();
				PeopleUiUtils.refreshFormTextWidget(shortSynTxt, synopsisNode,
						SYNOPSIS_CONTENT_SHORT, "Enter a short synopsis");
				PeopleUiUtils.refreshFormTextWidget(synopsisTxt, synopsisNode,
						SYNOPSIS_CONTENT, "Enter the full synopsis");
			}
		};

		PeopleUiUtils.addTxtModifyListener(editPart, shortSynTxt, synopsisNode,
				SYNOPSIS_CONTENT_SHORT, PropertyType.STRING);
		PeopleUiUtils.addTxtModifyListener(editPart, synopsisTxt, synopsisNode,
				SYNOPSIS_CONTENT, PropertyType.STRING);

		editPart.initialize(form);
		form.addPart(editPart);
	}

	public void populateFilmDetailsPanel(Composite parent, final Node entity) {
		parent.setLayout(new GridLayout());

		Composite origLanCmp = toolkit.createComposite(parent);
		origLanCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		origLanCmp.setLayout(new GridLayout());
		populateLangPanel(origLanCmp, entity, FILM_ORIGINAL_LANGUAGE, "Add...");

		Composite prodCountryCmp = toolkit.createComposite(parent);
		prodCountryCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true,
				false));
		prodCountryCmp.setLayout(new GridLayout());
		populateProdCountryPanel(prodCountryCmp, entity, FILM_PROD_COUNTRY,
				"Add...");

		Composite adminInfoCmp = toolkit.createComposite(parent);
		adminInfoCmp
				.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		populateMainInfoCmp(adminInfoCmp, entity);

		Composite payAccCmp = toolkit.createComposite(parent);
		payAccCmp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		populateAltTitleGroup(payAccCmp);

	}

	private void populateMainInfoCmp(Composite parent, final Node film) {
		GridLayout layout = PeopleUiUtils.gridLayoutNoBorder(2);
		layout.horizontalSpacing = layout.verticalSpacing = 5;
		parent.setLayout(layout);

		// 1st line
		final Text prodYearTxt = createLT(parent, "Production year:");
		final Text directorTxt = createLT(parent, "Director:");

		// 2nd Line
		final Text lengthTxt = createLT(parent, "Exact length (hh:mm:ss):");
		final Text lengthInMinTxt = createLT(parent, "Length in minutes:");

		// 3rd Line
		final Text genreTxt = createLT(parent, "Genre:");
		toolkit.createLabel(parent, "", SWT.NONE);

		// 4th Line
		final Text categoryTxt = createLT(parent, "Category:");
		final Text animTechTxt = createLT(parent, "Animation technique:");
		// // Category
		// toolkit.createLabel(parent, );
		// final Combo catCmb = new Combo(parent, SWT.READ_ONLY);
		// toolkit.adapt(catCmb, false, false);
		// catCmb.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		// catCmb.setEnabled(false);
		// Animation Technique
		// toolkit.createLabel(parent, "Animation technique");
		// final Combo animTechCmb = new Combo(parent, SWT.READ_ONLY);
		// toolkit.adapt(animTechCmb, false, false);
		// animTechCmb.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true,
		// false));
		// animTechCmb.setEnabled(false);

		// 5th Line : Flags
		Composite flagCmp = toolkit.createComposite(parent);
		flagCmp.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false, 2, 1));
		flagCmp.setLayout(PeopleUiUtils.gridLayoutNoBorder(4));

		final Button isFeatureBtn = toolkit.createButton(flagCmp,
				"Feature film", SWT.CHECK | SWT.LEFT);
		isFeatureBtn
				.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		final Button isPremiereBtn = toolkit.createButton(flagCmp, "Premiere",
				SWT.CHECK);
		isPremiereBtn.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false,
				false));
		final Button isStudentProjBtn = toolkit.createButton(flagCmp,
				"Student Project", SWT.CHECK);
		isStudentProjBtn.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false,
				false));
		final Button isDebutFilmBtn = toolkit.createButton(flagCmp,
				"Debut film", SWT.CHECK);
		isDebutFilmBtn.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false,
				false));

		// 6th line
		Composite websiteCmp = toolkit.createComposite(parent);
		websiteCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false,
				2, 1));
		websiteCmp.setLayout(PeopleUiUtils.gridLayoutNoBorder(2));

		toolkit.createLabel(websiteCmp, "Official homepage:");
		final Text websiteTxt = toolkit.createText(websiteCmp, "", SWT.BORDER);
		websiteTxt.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		final AbstractFormPart notePart = new AbstractFormPart() {
			public void refresh() {
				super.refresh();
				PeopleUiUtils.refreshFormTextWidget(prodYearTxt, film,
						FILM_PROD_YEAR);
				PeopleUiUtils.refreshFormTextWidget(directorTxt, film,
						FILM_DIRECTOR);
				PeopleUiUtils.refreshFormTextWidget(lengthTxt, film,
						FILM_LENGTH);
				PeopleUiUtils.refreshFormTextWidget(lengthInMinTxt, film,
						FILM_LENGTH_IN_MIN);
				PeopleUiUtils.refreshFormTextWidget(genreTxt, film, FILM_GENRE);
				PeopleUiUtils.refreshFormTextWidget(categoryTxt, film,
						FILM_CATEGORY);
				PeopleUiUtils.refreshFormTextWidget(animTechTxt, film,
						FILM_ANIMATION_TECHNIQUE);

				PeopleUiUtils.refreshFormTextWidget(websiteTxt, film,
						FILM_WEBSITE);

				PeopleUiUtils.refreshCheckBoxWidget(isFeatureBtn, film,
						FILM_IS_FEATURE);
				PeopleUiUtils.refreshCheckBoxWidget(isPremiereBtn, film,
						FILM_IS_PREMIERE);
				PeopleUiUtils.refreshCheckBoxWidget(isStudentProjBtn, film,
						FILM_IS_STUDENT_PROJECT);
				PeopleUiUtils.refreshCheckBoxWidget(isDebutFilmBtn, film,
						FILM_IS_DEBUT_FILM);
			}
		};

		// TODO manage correctly date as long
		PeopleUiUtils.addModifyListener(prodYearTxt, film, FILM_PROD_YEAR,
				notePart);
		PeopleUiUtils.addModifyListener(directorTxt, film, FILM_DIRECTOR,
				notePart);
		PeopleUiUtils.addModifyListener(lengthTxt, film, FILM_LENGTH, notePart);
		PeopleUiUtils.addModifyListener(lengthInMinTxt, film,
				FILM_LENGTH_IN_MIN, notePart);
		PeopleUiUtils.addModifyListener(genreTxt, film, FILM_GENRE, notePart);
		PeopleUiUtils.addModifyListener(categoryTxt, film, FILM_CATEGORY,
				notePart);
		PeopleUiUtils.addModifyListener(animTechTxt, film,
				FILM_ANIMATION_TECHNIQUE, notePart);

		PeopleUiUtils.addModifyListener(websiteTxt, film, FILM_WEBSITE,
				notePart);

		PeopleUiUtils.addCheckBoxListener(isFeatureBtn, film, FILM_IS_FEATURE,
				notePart);
		PeopleUiUtils.addCheckBoxListener(isPremiereBtn, film,
				FILM_IS_PREMIERE, notePart);
		PeopleUiUtils.addCheckBoxListener(isStudentProjBtn, film,
				FILM_IS_STUDENT_PROJECT, notePart);
		PeopleUiUtils.addCheckBoxListener(isDebutFilmBtn, film,
				FILM_IS_DEBUT_FILM, notePart);

		parent.layout();

		notePart.initialize(form);
		form.addPart(notePart);
	}

	private Text createLT(Composite parent, String label) {
		Composite cmp = toolkit.createComposite(parent);
		cmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		GridLayout gl = new GridLayout(2, false);
		gl.marginWidth = gl.verticalSpacing = gl.marginHeight = 0;
		gl.horizontalSpacing = 5;
		cmp.setLayout(gl);
		toolkit.createLabel(cmp, label);
		Text txt = toolkit.createText(cmp, "", SWT.BORDER);
		txt.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		return txt;
	}

	private void populateAltTitleGroup(Composite parent) {
		parent.setLayout(PeopleUiUtils.gridLayoutNoBorder());
		final Group group = new Group(parent, 0);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		group.setText("Alternative Titles");
		group.setLayout(PeopleUiUtils.gridLayoutNoBorder());

		AbstractFormPart formPart = new AbstractFormPart() {
			public void refresh() {
				// TODO add "create account button"
				super.refresh();
				// try {
				// if (!entity.hasNode(PeopleNames.PEOPLE_PAYMENT_ACCOUNTS)
				// && CommonsJcrUtils.isNodeCheckedOutByMe(entity)) {
				// PeopleJcrUtils.createPaymentAccount(entity,
				// PeopleTypes.PEOPLE_BANK_ACCOUNT, "new");
				// entity.getSession().save();
				// }
				// } catch (RepositoryException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// }

				// Control[] children = group.getChildren();
				// for (Control child : children) {
				// child.dispose();
				// }
				//
				// NodeIterator ni = PeopleJcrUtils.getPaymentAccounts(entity);
				// while (ni != null && ni.hasNext()) {
				// Composite cmp = new BankAccountComposite(group, 0, toolkit,
				// form, ni.nextNode());
				// cmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true,
				// false));
				// }
				group.layout();
			}
		};
		// notePart.refresh();
		parent.layout();
		formPart.initialize(form);
		form.addPart(formPart);
	}

	/**
	 * Populate a parent composite with controls to manage tag like languages
	 * 
	 * @param parent
	 * 
	 * @param property
	 *            the multi-valued property we want to update
	 */
	public void populateLangPanel(final Composite parent, final Node entity,
			final String tagPropName, final String newTagMsg) {
		parent.setLayout(PeopleUiUtils.gridLayoutNoBorder(3));
		toolkit.createLabel(parent, "Original language(s):");

		// The tag composite
		final Composite valuesCmp = new Composite(parent, SWT.NO_FOCUS);
		GridData gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		valuesCmp.setLayoutData(gd);
		RowLayout rl = new RowLayout(SWT.HORIZONTAL);
		rl.wrap = true;
		rl.marginLeft = 5;
		rl.marginRight = 0;
		valuesCmp.setLayout(rl);

		// The add button
		final Link chooseLangLk = new Link(parent, SWT.BOTTOM);
		toolkit.adapt(chooseLangLk, false, false);
		chooseLangLk.setText("   <a>" + newTagMsg + "</a>");

		chooseLangLk.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = -7118320199160680131L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				try {
					PickUpLangDialog diag = new PickUpLangDialog(chooseLangLk
							.getShell(), newTagMsg, entity.getSession(), entity);
					diag.open();
					String lang = diag.getSelected();
					if (CommonsJcrUtils.checkNotEmptyString(lang))
						addMultiPropertyValue(entity, tagPropName, lang);
				} catch (RepositoryException e) {
					throw new PeopleException("Unable to add language", e);
				}

			}
		});

		final AbstractFormPart editPart = new AbstractFormPart() {
			public void refresh() {
				super.refresh();

				boolean isCO = CommonsJcrUtils.isNodeCheckedOutByMe(entity);
				// show add button only in edit mode
				chooseLangLk.setVisible(isCO);

				// We redraw the full control at each refresh, might be a more
				// efficient way to do
				Control[] oldChildren = valuesCmp.getChildren();
				for (Control child : oldChildren)
					child.dispose();

				try {
					if (entity.hasProperty(tagPropName)) {
						Value[] values = entity.getProperty(tagPropName)
								.getValues();
						for (final Value value : values) {
							final String valueStr = value.getString();
							String labelStr = ResourcesJcrUtils
									.getLangEnLabelFromIso(entity.getSession(),
											valueStr);
							toolkit.createLabel(valuesCmp, labelStr, SWT.BOTTOM);
							Button deleteBtn = new Button(valuesCmp, SWT.FLAT);
							deleteBtn.setData(PeopleUiConstants.CUSTOM_VARIANT,
									PeopleUiConstants.CSS_FLAT_IMG_BUTTON);
							deleteBtn.setImage(PeopleImages.DELETE_BTN);
							RowData rd = new RowData();
							rd.height = 16;
							rd.width = 16;
							deleteBtn.setLayoutData(rd);

							deleteBtn
									.addSelectionListener(new SelectionAdapter() {
										private static final long serialVersionUID = 1L;

										@Override
										public void widgetSelected(
												final SelectionEvent event) {
											removeMultiPropertyValue(entity,
													tagPropName, valueStr);
											for (IFormPart part : form
													.getParts()) {
												((AbstractFormPart) part)
														.markStale();
												part.refresh();
											}
										}
									});
							deleteBtn.setVisible(isCO);
						}

						// nlCmp.pack();
						valuesCmp.layout(false);
						// parent.getParent().pack();
						parent.getParent().layout();
					}
				} catch (RepositoryException re) {
					throw new PeopleException("Language list", re);
				}
			}
		};

		editPart.initialize(form);
		form.addPart(editPart);
	}

	/**
	 * Populate a parent composite with controls to manage tag like countries
	 */
	public void populateProdCountryPanel(final Composite parent,
			final Node entity, final String tagPropName, final String newTagMsg) {
		parent.setLayout(PeopleUiUtils.gridLayoutNoBorder(3));
		toolkit.createLabel(parent, "Production country(ies):");
		// The tag composite
		final Composite nlCmp = new Composite(parent, SWT.NO_FOCUS);
		GridData gd = new GridData(SWT.LEFT, SWT.LEFT, false, false);
		nlCmp.setLayoutData(gd);
		RowLayout rl = new RowLayout(SWT.HORIZONTAL);
		rl.wrap = true;
		rl.marginLeft = 5;
		rl.marginRight = 0;
		nlCmp.setLayout(rl);

		// The add button
		final Link chooseLangLk = new Link(parent, SWT.BOTTOM);
		toolkit.adapt(chooseLangLk, false, false);
		chooseLangLk.setText("<a>" + newTagMsg + "</a>");

		chooseLangLk.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = -7118320199160680131L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				try {
					PickUpCountryDialog diag = new PickUpCountryDialog(
							chooseLangLk.getShell(), newTagMsg, entity
									.getSession(), entity);
					diag.open();
					String lang = diag.getSelected();
					if (CommonsJcrUtils.checkNotEmptyString(lang))
						addMultiPropertyValue(entity, tagPropName, lang);
				} catch (RepositoryException e) {
					throw new PeopleException("Unable to add language", e);
				}

			}
		});

		final AbstractFormPart editPart = new AbstractFormPart() {
			public void refresh() {
				super.refresh();
				boolean isCO = CommonsJcrUtils.isNodeCheckedOutByMe(entity);
				// show add button only in edit mode
				chooseLangLk.setVisible(isCO);

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
							final String valueStr = value.getString();
							String labelStr = ResourcesJcrUtils
									.getCountryEnLabelFromIso(
											entity.getSession(), valueStr);
							toolkit.createLabel(nlCmp, labelStr, SWT.BOTTOM);

							Button deleteBtn = new Button(nlCmp, SWT.FLAT);
							deleteBtn.setData(PeopleUiConstants.CUSTOM_VARIANT,
									PeopleUiConstants.CSS_FLAT_IMG_BUTTON);
							deleteBtn.setImage(PeopleImages.DELETE_BTN);
							RowData rd = new RowData();
							rd.height = 16;
							rd.width = 16;
							deleteBtn.setLayoutData(rd);

							deleteBtn
									.addSelectionListener(new SelectionAdapter() {
										private static final long serialVersionUID = 1L;

										@Override
										public void widgetSelected(
												final SelectionEvent event) {
											removeMultiPropertyValue(entity,
													tagPropName, valueStr);
											for (IFormPart part : form
													.getParts()) {
												((AbstractFormPart) part)
														.markStale();
												part.refresh();
											}
										}
									});
							deleteBtn.setVisible(isCO);
						}
						// nlCmp.pack();
						nlCmp.layout(false);
						// parent.getParent().pack();
						parent.getParent().layout();
					}
				} catch (RepositoryException re) {
					throw new PeopleException("Language list", re);
				}
			}
		};

		editPart.initialize(form);
		form.addPart(editPart);
	}

	private void removeMultiPropertyValue(Node entity, String propName,
			String tagToRemove) {
		try {
			List<String> tags = new ArrayList<String>();
			Value[] values = entity.getProperty(propName).getValues();
			for (int i = 0; i < values.length; i++) {
				String curr = values[i].getString();
				if (!tagToRemove.equals(curr))
					tags.add(curr);
			}

			boolean wasCheckedOut = CommonsJcrUtils
					.isNodeCheckedOutByMe(entity);
			if (!wasCheckedOut)
				CommonsJcrUtils.checkout(entity);
			entity.setProperty(propName, tags.toArray(new String[tags.size()]));
			if (wasCheckedOut)
				form.dirtyStateChanged();
			else
				CommonsJcrUtils.saveAndCheckin(entity);
		} catch (RepositoryException e) {
			throw new PeopleException("unable to initialise deletion", e);
		}
	}

	private void addMultiPropertyValue(Node node, String propName, String value) {
		try {
			Value[] values;
			String[] valuesStr;
			String errMsg = null;
			if (node.hasProperty(propName)) {
				values = node.getProperty(propName).getValues();

				// Check dupplicate
				for (Value tag : values) {
					String curTagUpperCase = tag.getString().toUpperCase()
							.trim();
					if (value.toUpperCase().trim().equals(curTagUpperCase)) {
						errMsg = value
								+ " is already in the list and thus could not be added.";
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
				valuesStr[i] = value;
			} else {
				valuesStr = new String[1];
				valuesStr[0] = value;
			}

			boolean wasCheckedout = CommonsJcrUtils.isNodeCheckedOut(node);
			if (!wasCheckedout)
				CommonsJcrUtils.checkout(node);
			node.setProperty(propName, valuesStr);
			if (!wasCheckedout)
				CommonsJcrUtils.saveAndCheckin(node);
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