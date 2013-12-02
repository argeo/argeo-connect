package org.argeo.connect.people.ui.toolkits;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.argeo.connect.film.FilmNames;
import org.argeo.connect.film.core.FilmJcrUtils;
import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.ui.PeopleImages;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.dialogs.PickUpCountryDialog;
import org.argeo.connect.people.ui.dialogs.PickUpLangDialog;
import org.argeo.connect.people.ui.utils.JcrUiUtils;
import org.argeo.connect.people.ui.utils.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.ResourcesJcrUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
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
	private final static Log log = LogFactory.getLog(FilmToolkit.class);

	private final static String LANG_KEY = "lang";
	private final static String DEFAULT_LANG_KEY = "default";

	private final FormToolkit toolkit;
	private final IManagedForm form;

	public FilmToolkit(FormToolkit toolkit, IManagedForm form) {
		super(toolkit, form);
		// formToolkit
		// managedForm
		this.toolkit = toolkit;
		this.form = form;
	}

	/**
	 * 
	 * @param panel
	 * @param entity
	 * @param descLabel
	 *            typically synopsis pour a film or Description for a
	 *            multilingual description
	 * @param langs
	 *            an ordered list of the various languages that are to be
	 *            displayed
	 */

	public void populateMultiLangDescPanel(Composite panel, final Node entity,
			String descLabel, List<String> langs) {
		final List<Text> texts = new ArrayList<Text>();

		int style = GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL
				| GridData.GRAB_VERTICAL;
		panel.setLayout(new GridLayout());
		// Original description
		toolkit.createLabel(panel, descLabel, SWT.NONE);
		Text descTxt = toolkit.createText(panel, "", SWT.BORDER | SWT.MULTI
				| SWT.WRAP);
		GridData gd = new GridData(style);
		descTxt.setLayoutData(gd);
		descTxt.setData(LANG_KEY, DEFAULT_LANG_KEY);
		texts.add(descTxt);

		// Alt description

		for (String lang : langs) {
			toolkit.createLabel(panel, descLabel + " " + lang.toUpperCase(),
					SWT.NONE);
			Text altDescTxt = toolkit.createText(panel, "", SWT.BORDER
					| SWT.MULTI | SWT.WRAP);
			altDescTxt.setLayoutData(new GridData(style));
			altDescTxt.setData(LANG_KEY, lang);
			texts.add(altDescTxt);
		}

		final AbstractFormPart editPart = new AbstractFormPart() {
			public void refresh() {
				super.refresh();
				for (Text text : texts) {
					String key = (String) text.getData(LANG_KEY);
					if (DEFAULT_LANG_KEY.equals(key))
						PeopleUiUtils.refreshTextWidgetValue(text, entity,
								Property.JCR_DESCRIPTION);
					else {
						Node altDescNode = CommonsJcrUtils.getAltPropertyNode(
								entity, PeopleNames.PEOPLE_ALT_LANGS, key);
						if (altDescNode != null)
							PeopleUiUtils.refreshTextWidgetValue(text,
									altDescNode, Property.JCR_DESCRIPTION);
					}
					text.setEnabled(CommonsJcrUtils
							.isNodeCheckedOutByMe(entity));
				}
			}
		};

		for (final Text text : texts) {
			final String currLang = (String) text.getData(LANG_KEY);
			if (DEFAULT_LANG_KEY.equals(currLang))
				PeopleUiUtils.addTxtModifyListener(editPart, text, entity,
						Property.JCR_DESCRIPTION, PropertyType.STRING);
			else {
				text.addModifyListener(new ModifyListener() {
					private static final long serialVersionUID = 1L;

					@Override
					public void modifyText(ModifyEvent event) {
						String altDesc = text.getText();
						if (CommonsJcrUtils.getAltPropertyNode(entity,
								PeopleNames.PEOPLE_ALT_LANGS, currLang) == null)
							if (CommonsJcrUtils.checkNotEmptyString(altDesc))
								CommonsJcrUtils.getOrCreateAltLanguageNode(
										entity, currLang);// create node
							else
								return;
						Node altTitle = CommonsJcrUtils.getAltPropertyNode(
								entity, PeopleNames.PEOPLE_ALT_LANGS, currLang);
						if (JcrUiUtils.setJcrProperty(altTitle,
								Property.JCR_DESCRIPTION, PropertyType.STRING,
								altDesc))
							editPart.markDirty();
					}
				});
			}
		}
		editPart.initialize(form);
		form.addPart(editPart);
	}

	/** Populate a panel with a german and an english synopsis. */
	// TODO clean this
	public void populateSynopsisPanel(Composite panel, final Node entity) {
		try {
			panel.setLayout(new GridLayout());
			// Original synopsis
			toolkit.createLabel(panel, "German synopsis: ", SWT.NONE);
			final Text synopsisTxt = toolkit.createText(panel, "", SWT.BORDER
					| SWT.MULTI | SWT.WRAP);
			GridData gd = new GridData(GridData.FILL_BOTH);
			gd.widthHint = 200;
			gd.heightHint = 120;
			synopsisTxt.setLayoutData(gd);
			Node origSynopsisNode = FilmJcrUtils.getSynopsisNode(entity,
					PeopleConstants.LANG_DE);
			// force creation to avoid npe and ease form life cycle
			if (origSynopsisNode == null)
				origSynopsisNode = FilmJcrUtils.addOrUpdateSynopsisNode(entity,
						null, null, PeopleConstants.LANG_DE);
			synopsisTxt.setData("LinkedNode", origSynopsisNode.getPath());

			// EN synopsis
			toolkit.createLabel(panel, "English synopsis: ", SWT.NONE);
			final Text enSynopsisTxt = toolkit.createText(panel, "", SWT.BORDER
					| SWT.MULTI | SWT.WRAP);
			gd = new GridData(GridData.FILL_BOTH);
			gd.widthHint = 200;
			gd.heightHint = 120;
			enSynopsisTxt.setLayoutData(gd);
			Node enSynopsisNode = FilmJcrUtils.getSynopsisNode(entity, "en");
			// force creation to avoid npe and ease form life cycle
			if (enSynopsisNode == null)
				enSynopsisNode = FilmJcrUtils.addOrUpdateSynopsisNode(entity,
						null, null, PeopleConstants.LANG_EN);
			enSynopsisTxt.setData("LinkedNode", enSynopsisNode.getPath());

			final AbstractFormPart editPart = new AbstractFormPart() {
				public void refresh() {
					super.refresh();
					try {
						String path = (String) enSynopsisTxt
								.getData("LinkedNode");
						Session session = entity.getSession();
						if (session.nodeExists(path)) {
							Node enSynNode = session.getNode(path);
							String syn = JcrUtils.get(enSynNode,
									SYNOPSIS_CONTENT);
							if (!CommonsJcrUtils.isEmptyString(syn))
								enSynopsisTxt.setText(syn);
						}

						path = (String) synopsisTxt.getData("LinkedNode");
						if (session.nodeExists(path)) {
							Node origSynNode = entity.getSession()
									.getNode(path);
							String syn = JcrUtils.get(origSynNode,
									SYNOPSIS_CONTENT);
							if (!CommonsJcrUtils.isEmptyString(syn))
								synopsisTxt.setText(syn);
						} else
							log.debug("no node");

						boolean isCheckouted = entity.isCheckedOut();
						synopsisTxt.setEnabled(isCheckouted);
						enSynopsisTxt.setEnabled(isCheckouted);
					} catch (RepositoryException e) {
						throw new PeopleException(
								"Cannot get synopsis node from widget", e);
					}
				}
			};

			synopsisTxt.addModifyListener(new ModifyListener() {
				private static final long serialVersionUID = 1L;

				@Override
				public void modifyText(ModifyEvent event) {
					try {
						String path = (String) synopsisTxt
								.getData("LinkedNode");

						Node synopsisNode = entity.getSession().getNode(path);
						if (synopsisNode != null) {
							if (JcrUiUtils.setJcrProperty(synopsisNode,
									SYNOPSIS_CONTENT, PropertyType.STRING,
									synopsisTxt.getText()))
								editPart.markDirty();
						}
					} catch (RepositoryException e) {
						throw new PeopleException(
								"Cannot get synopsis node from widget", e);
					}

				}
			});

			enSynopsisTxt.addModifyListener(new ModifyListener() {
				private static final long serialVersionUID = 1L;

				@Override
				public void modifyText(ModifyEvent event) {
					try {
						String path = (String) enSynopsisTxt
								.getData("LinkedNode");
						Node enSynopsisNode = entity.getSession().getNode(path);
						if (enSynopsisNode != null) {
							if (JcrUiUtils.setJcrProperty(enSynopsisNode,
									SYNOPSIS_CONTENT, PropertyType.STRING,
									enSynopsisTxt.getText()))
								editPart.markDirty();
						}
					} catch (RepositoryException e) {
						throw new PeopleException(
								"Cannot get synopsis node from widget", e);
					}
				}
			});

			editPart.initialize(form);
			form.addPart(editPart);
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to create " + "synopsis panel", e);
		}
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
		GridLayout gl = PeopleUiUtils.gridLayoutNoBorder(4);
		gl.horizontalSpacing = 10;
		gl.verticalSpacing = 5;
		parent.setLayout(gl);

		// Production year
		toolkit.createLabel(parent, "Production year");
		final Text prodYearTxt = toolkit.createText(parent, "", SWT.BORDER);
		prodYearTxt.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		// Director
		toolkit.createLabel(parent, "Director");
		final Text directorTxt = toolkit.createText(parent, "", SWT.BORDER);
		directorTxt.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		// Exact length
		toolkit.createLabel(parent, "Exact length (hh:mm:ss)");
		final Text lengthTxt = toolkit.createText(parent, "", SWT.BORDER);
		lengthTxt.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		// Length in minutes
		toolkit.createLabel(parent, "Length in minutes");
		final Text lengthInMinTxt = toolkit.createText(parent, "", SWT.BORDER);
		lengthInMinTxt.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true,
				false));

		// Category
		toolkit.createLabel(parent, "Category");
		final Combo catCmb = new Combo(parent, SWT.READ_ONLY);
		toolkit.adapt(catCmb, false, false);
		catCmb.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		catCmb.setEnabled(false);

		// Animation Technique
		toolkit.createLabel(parent, "Animation technique");
		final Combo animTechCmb = new Combo(parent, SWT.READ_ONLY);
		toolkit.adapt(animTechCmb, false, false);
		animTechCmb.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		animTechCmb.setEnabled(false);

		// Genre
		toolkit.createLabel(parent, "Genre");
		final Text genreTxt = toolkit.createText(parent, "", SWT.BORDER);
		genreTxt.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3,
				1));

		// Website
		toolkit.createLabel(parent, "Website");
		final Text websiteTxt = toolkit.createText(parent, "", SWT.BORDER);
		websiteTxt.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false,
				3, 1));

		// Flags
		final Button isFeatureBtn = toolkit.createButton(parent,
				"Feature film", SWT.CHECK | SWT.LEFT);
		isFeatureBtn
				.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		final Button isPremiereBtn = toolkit.createButton(parent, "Premiere",
				SWT.CHECK);
		isPremiereBtn.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false,
				false));
		final Button isStudentProjBtn = toolkit.createButton(parent,
				"Student Project", SWT.CHECK);
		isStudentProjBtn.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false,
				false));
		final Button isDebutFilmBtn = toolkit.createButton(parent,
				"Debut film", SWT.CHECK);
		isDebutFilmBtn.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false,
				false));

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
							deleteBtn.setData(RWT.CUSTOM_VARIANT,
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
							deleteBtn.setData(RWT.CUSTOM_VARIANT,
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