package org.argeo.connect.people.ui.composites;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.argeo.ArgeoException;
import org.argeo.connect.film.FilmNames;
import org.argeo.connect.film.FilmTypes;
import org.argeo.connect.film.core.FilmJcrUtils;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.ui.PeopleImages;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.dialogs.NewTimeStampDialog;
import org.argeo.connect.people.ui.dialogs.NewTitleDialog;
import org.argeo.connect.people.ui.dialogs.PickUpCountryDialog;
import org.argeo.connect.people.ui.dialogs.PickUpDateDialog;
import org.argeo.connect.people.ui.dialogs.PickUpFromListDialog;
import org.argeo.connect.people.ui.dialogs.PickUpLangDialog;
import org.argeo.connect.people.ui.utils.JcrUiUtils;
import org.argeo.connect.people.ui.utils.PeopleHtmlUtils;
import org.argeo.connect.people.ui.utils.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.ResourcesJcrUtils;
import org.argeo.eclipse.ui.jcr.lists.ColumnDefinition;
import org.argeo.eclipse.ui.jcr.lists.NodeViewerComparator;
import org.argeo.eclipse.ui.jcr.lists.SimpleJcrNodeLabelProvider;
import org.argeo.eclipse.ui.utils.ViewerUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * A people specific that display film details with RO/edit ability in a form.
 */
public class FilmMainInfoPanelComposite extends Composite implements FilmNames {
	private static final long serialVersionUID = 58381532068661087L;

	private final FormToolkit toolkit;
	private final IManagedForm form;

	// private final PeopleService peopleService;
	private final Node film;

	// local cache to simplify code
	// checkedOut state MUST NOT change during life cycle of this composite
	final boolean isCheckedOut;

	// do not forget to dispose on dispose() and to check if disposed on
	// refresh()
	private List<AbstractFormPart> localParts = new ArrayList<AbstractFormPart>();

	// The various parts that are shared in this instance
	private AbstractFormPart genrePart, countryPart, oLangPart, parentEditPart;

	private Composite innerCmp;
	private TableViewer titlesViewer;
	private TableViewer awardsViewer;

	public FilmMainInfoPanelComposite(Composite parent, int style,
			FormToolkit toolkit, IManagedForm form, Node entityNode,
			PeopleService peopleService) {
		super(parent, style);
		this.toolkit = toolkit;
		this.form = form;
		this.film = entityNode;
		isCheckedOut = CommonsJcrUtils.isNodeCheckedOutByMe(film);
		// this.peopleService = peopleService;
		populate();
	}

	private void populate() {
		Composite parent = this;
		parent.setLayout(PeopleUiUtils.gridLayoutNoBorder());

		// Add a scrolled container
		ScrolledComposite container = new ScrolledComposite(parent,
				SWT.NO_FOCUS | SWT.V_SCROLL); // SWT.H_SCROLL |
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		innerCmp = new Composite(container, SWT.NO_FOCUS);

		if (CommonsJcrUtils.isNodeCheckedOut(film))
			createEditComposite();
		else
			createROComposite();

		container.setExpandHorizontal(true);
		container.setExpandVertical(false);
		innerCmp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		container.setContent(innerCmp);
	}

	private void createROComposite() {
		innerCmp.setLayout(new GridLayout(4, false));
		// GridLayout layout = PeopleUiUtils.gridLayoutNoBorder();

		// Category
		PeopleUiUtils.createBoldLabel(toolkit, innerCmp, "Category");
		final Label catLbl = toolkit.createLabel(innerCmp, "");
		catLbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3,
				1));

		// Optional animation technique label
		boolean isAnimation = isValueContained(film, FILM_CATEGORIES,
				"Animation");
		if (isAnimation)
			PeopleUiUtils.createBoldLabel(toolkit, innerCmp,
					"Animation technique");
		final Label animTechLbl = isAnimation ? toolkit.createLabel(innerCmp,
				"") : null;
		if (animTechLbl != null) {
			animTechLbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
					false, 3, 1));
		}

		// Genre
		PeopleUiUtils.createBoldLabel(toolkit, innerCmp, "Genre");
		final Label genreLbl = toolkit.createLabel(innerCmp, "");
		genreLbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				3, 1));

		// Prod country & original language
		PeopleUiUtils.createBoldLabel(toolkit, innerCmp,
				"Country of Production");
		final Label prodCountryLbl = toolkit.createLabel(innerCmp, "");
		prodCountryLbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));

		PeopleUiUtils.createBoldLabel(toolkit, innerCmp, "Original Language");
		final Label origLangLbl = toolkit.createLabel(innerCmp, "");
		origLangLbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));

		// Prod year & home page
		PeopleUiUtils.createBoldLabel(toolkit, innerCmp, "Production Year");
		final Label prodYearLbl = toolkit.createLabel(innerCmp, "");
		prodYearLbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));

		PeopleUiUtils.createBoldLabel(toolkit, innerCmp, "Homepage");
		final Label homePageLbl = toolkit.createLabel(innerCmp, "");
		homePageLbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));
		homePageLbl.setData(PeopleUiConstants.MARKUP_ENABLED, Boolean.TRUE);

		// Length & Type
		PeopleUiUtils.createBoldLabel(toolkit, innerCmp, "Length (hh:mm:ss)");
		final Label lengthLbl = toolkit.createLabel(innerCmp, "");
		lengthLbl
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		PeopleUiUtils.createBoldLabel(toolkit, innerCmp, "Type");
		final Label typeLbl = toolkit.createLabel(innerCmp, "");
		typeLbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		// Title and Awards
		addROTableViewer();

		final AbstractFormPart readOnlyPart = new AbstractFormPart() {
			public void refresh() {
				super.refresh();

				if (FilmMainInfoPanelComposite.this.isDisposed())
					return;

				catLbl.setText(CommonsJcrUtils.getMultiAsString(film,
						FILM_CATEGORIES, ", "));
				if (animTechLbl != null)
					animTechLbl.setText(CommonsJcrUtils.get(film,
							FILM_ANIMATION_TECHNIQUE));
				genreLbl.setText(CommonsJcrUtils.getMultiAsString(film,
						FILM_GENRES, ", "));

				prodCountryLbl.setText(getMultiCountriesAsString(film,
						FILM_PROD_COUNTRY));
				origLangLbl.setText(getMultiLangsAsString(film,
						FILM_ORIGINAL_LANGUAGE));
				prodYearLbl.setText(CommonsJcrUtils.get(film, FILM_PROD_YEAR));

				String webSite = CommonsJcrUtils.get(film, FILM_WEBSITE);
				if (CommonsJcrUtils.checkNotEmptyString(webSite))
					homePageLbl.setText(PeopleHtmlUtils
							.getWebsiteSnippet(webSite));

				Long lengthInSec = CommonsJcrUtils.getLongValue(film,
						FILM_LENGTH);
				if (lengthInSec != null) {
					String tmpStr = PeopleUiUtils
							.getLengthFormattedAsString(lengthInSec.longValue())
							+ " ("
							+ TimeUnit.SECONDS.toMinutes(lengthInSec
									.longValue()) + " min)";
					lengthLbl.setText(tmpStr);
				}

				typeLbl.setText(CommonsJcrUtils.get(film, FILM_TYPE));

				refreshTitlesViewer();

				innerCmp.pack(true);
				innerCmp.getParent().pack(true);
				innerCmp.getParent().layout();

			}
		};

		readOnlyPart.refresh();
		readOnlyPart.initialize(form);
		form.addPart(readOnlyPart);
		localParts.add(readOnlyPart);
	}

	private String getMultiCountriesAsString(Node node, String propertyName) {
		try {
			String separator = ", ";
			if (!node.hasProperty(propertyName))
				return "";
			else {
				Value[] langs = node.getProperty(propertyName).getValues();
				StringBuilder builder = new StringBuilder();
				for (Value val : langs) {
					String currStr = val.getString();
					if (CommonsJcrUtils.checkNotEmptyString(currStr))
						builder.append(
								ResourcesJcrUtils.getCountryEnLabelFromIso(
										film.getSession(), currStr)).append(
								separator);
				}
				if (builder.lastIndexOf(separator) >= 0)
					return builder.substring(0, builder.length() - 2);
				else
					return builder.toString();
			}
		} catch (RepositoryException e) {
			throw new ArgeoException("Cannot get multi valued property "
					+ propertyName + " of " + node, e);
		}
	}

	private String getMultiLangsAsString(Node node, String propertyName) {
		try {
			String separator = ", ";
			if (!node.hasProperty(propertyName))
				return "";
			else {
				Value[] langs = node.getProperty(propertyName).getValues();
				StringBuilder builder = new StringBuilder();
				for (Value val : langs) {
					String currStr = val.getString();
					if (CommonsJcrUtils.checkNotEmptyString(currStr))
						builder.append(
								ResourcesJcrUtils.getLangEnLabelFromIso(
										film.getSession(), currStr)).append(
								separator);
				}
				if (builder.lastIndexOf(separator) >= 0)
					return builder.substring(0, builder.length() - 2);
				else
					return builder.toString();
			}
		} catch (RepositoryException e) {
			throw new ArgeoException("Cannot get multi valued property "
					+ propertyName + " of " + node, e);
		}
	}

	private void createEditComposite() {
		GridLayout layout = PeopleUiUtils.gridLayoutNoBorder(2);
		layout.horizontalSpacing = 5;
		innerCmp.setLayout(layout);

		Composite mainInfoCmp = toolkit.createComposite(innerCmp);
		mainInfoCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false,
				2, 1));
		populateMainInfoCmp(mainInfoCmp);

		// Title and Awards
		addEditTableViewer();

		innerCmp.pack(true);
		innerCmp.getParent().pack(true);
		innerCmp.getParent().layout();
	}

	private void populateMainInfoCmp(Composite parent) {
		GridLayout layout = PeopleUiUtils.gridLayoutNoBorder(4);
		layout.horizontalSpacing = layout.verticalSpacing = 5;
		parent.setLayout(layout);

		// Category
		PeopleUiUtils.createBoldLabel(toolkit, parent, "Category");
		Composite categoriesCmp = toolkit.createComposite(parent);
		categoriesCmp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 3, 1));
		final List<Button> categoryBtns = createCategoryChk(categoriesCmp);
		categoriesCmp.layout();

		// Animation tech
		PeopleUiUtils.createBoldLabel(toolkit, parent, "Animation technique");
		final Text animTechTxt = toolkit.createText(parent, "", SWT.BORDER);
		animTechTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));

		Label dummyLbl = toolkit.createLabel(parent, "");
		dummyLbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				2, 1));

		// genre
		PeopleUiUtils.createBoldLabel(toolkit, parent, "Genre");
		final Composite genresCmp = toolkit.createComposite(parent);
		genresCmp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				3, 1));
		createGenrePart(genresCmp);
		genresCmp.layout();

		// Contry of prod & original language
		PeopleUiUtils.createBoldLabel(toolkit, parent, "Country of Production");
		final Composite prodCountriesCmp = toolkit.createComposite(parent);
		prodCountriesCmp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));
		createProdCountryPart(prodCountriesCmp, FILM_PROD_COUNTRY);
		// prodCountriesCmp.pack();
		prodCountriesCmp.layout();

		PeopleUiUtils.createBoldLabel(toolkit, parent, "Original Language");
		final Composite origLangCmp = toolkit.createComposite(parent);
		origLangCmp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));
		createOriginalLangPart(origLangCmp, FILM_ORIGINAL_LANGUAGE);
		origLangCmp.layout();

		// prod year & home page
		PeopleUiUtils.createBoldLabel(toolkit, parent, "Production year");
		final Text prodYearTxt = toolkit.createText(parent, "", SWT.BORDER);
		prodYearTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));

		PeopleUiUtils.createBoldLabel(toolkit, parent, "Homepage");
		final Text websiteTxt = toolkit.createText(parent, "", SWT.BORDER);
		websiteTxt
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		// final Text directorTxt = createLT(parent, "Director:");

		// Length
		PeopleUiUtils.createBoldLabel(toolkit, parent, "Length (hh:mm:ss)");
		Composite dateParentCmp = toolkit.createComposite(parent, SWT.NO_FOCUS);

		layout = PeopleUiUtils.gridLayoutNoBorder(2);
		layout.horizontalSpacing = 10;
		dateParentCmp.setLayout(layout);

		final DateTime lengthDt = new DateTime(dateParentCmp, SWT.RIGHT
				| SWT.TIME | SWT.LONG | SWT.DROP_DOWN);
		final Label lengthInMinLbl = toolkit.createLabel(dateParentCmp, "");
		lengthInMinLbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));

		// Film Type : must be moved to the header
		PeopleUiUtils.createBoldLabel(toolkit, parent, "Type");
		Composite flagCmp = toolkit.createComposite(parent, SWT.NO_FOCUS);
		flagCmp.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		layout = new GridLayout(3, false);
		layout.marginHeight = layout.marginTop = layout.marginBottom = layout.verticalSpacing = 0;
		flagCmp.setLayout(layout);
		final Button featureBtn = new Button(flagCmp, SWT.RADIO);
		featureBtn.setText("Feature");
		final Button midLengthBtn = new Button(flagCmp, SWT.RADIO);
		midLengthBtn.setText("Mid-Length");
		final Button shortBtn = new Button(flagCmp, SWT.RADIO);
		shortBtn.setText("Short");

		parentEditPart = new AbstractFormPart() {
			public void refresh() {
				super.refresh();

				if (FilmMainInfoPanelComposite.this.isDisposed())
					return;

				refreshCategoryChk(categoryBtns);
				PeopleUiUtils.refreshFormTextWidget(animTechTxt, film,
						FILM_ANIMATION_TECHNIQUE);
				animTechTxt.setEnabled(isValueContained(film, FILM_CATEGORIES,
						"Animation"));

				PeopleUiUtils.refreshFormTextWidget(prodYearTxt, film,
						FILM_PROD_YEAR);

				PeopleUiUtils.refreshFormTextWidget(websiteTxt, film,
						FILM_WEBSITE);

				Long lengthInSec = CommonsJcrUtils.getLongValue(film,
						FILM_LENGTH);

				if (lengthInSec == null) {
					lengthDt.setHours(0);
					lengthDt.setMinutes(0);
					lengthDt.setSeconds(0);
					lengthInMinLbl.setText("0min");
				} else {
					lengthDt.setHours((int) PeopleUiUtils
							.getHoursFromLength(lengthInSec.longValue()));
					lengthDt.setMinutes((int) PeopleUiUtils
							.getMinutesFromLength(lengthInSec.longValue()));
					lengthDt.setSeconds((int) PeopleUiUtils
							.getSecondsFromLength(lengthInSec.longValue()));
					lengthInMinLbl.setText(TimeUnit.SECONDS
							.toMinutes(lengthInSec.longValue()) + " min");
				}

				PeopleUiUtils.refreshRadioWidget(featureBtn, film, FILM_TYPE);
				PeopleUiUtils.refreshRadioWidget(midLengthBtn, film, FILM_TYPE);
				PeopleUiUtils.refreshRadioWidget(shortBtn, film, FILM_TYPE);
			}
		};

		parentEditPart.refresh();

		PeopleUiUtils.addModifyListener(animTechTxt, film,
				FILM_ANIMATION_TECHNIQUE, parentEditPart);
		PeopleUiUtils.addModifyListener(prodYearTxt, film, FILM_PROD_YEAR,
				parentEditPart);
		PeopleUiUtils.addModifyListener(websiteTxt, film, FILM_WEBSITE,
				parentEditPart);

		addCategoryCheckListeners(categoryBtns);

		Listener listener = new Listener() {
			private static final long serialVersionUID = 1L;

			public void handleEvent(Event event) {
				if (JcrUiUtils.setJcrProperty(film, FILM_TYPE,
						PropertyType.STRING, ((Button) event.widget).getText()))
					parentEditPart.markDirty();
			}
		};
		featureBtn.addListener(SWT.Selection, listener);
		midLengthBtn.addListener(SWT.Selection, listener);
		shortBtn.addListener(SWT.Selection, listener);

		parent.layout();

		parentEditPart.initialize(form);
		form.addPart(parentEditPart);
		localParts.add(parentEditPart);
	}

	private List<Button> createCategoryChk(Composite parent) {
		List<Button> buttons = new ArrayList<Button>();
		// TODO clean this
		final String[] elements = { "Fiction", "Animation", "Documentary",
				"Experimental", "Art", "Performance", "Music", "Video",
				"Mockumentary" };
		parent.setLayout(createBasicRL());
		for (String category : elements) {
			buttons.add(toolkit.createButton(parent, category, SWT.CHECK));
		}
		return buttons;
	}

	private void refreshCategoryChk(List<Button> buttons) {
		for (Button button : buttons) {
			button.setSelection(isValueContained(film, FILM_CATEGORIES,
					button.getText()));
		}
	}

	private void addCategoryCheckListeners(List<Button> buttons) {
		for (final Button button : buttons) {

			button.addSelectionListener(new SelectionAdapter() {
				private static final long serialVersionUID = 1L;

				@Override
				public void widgetSelected(SelectionEvent e) {
					boolean newValue = button.getSelection();
					boolean oldValue = isValueContained(film, FILM_CATEGORIES,
							button.getText());
					if (newValue != oldValue) {
						if (newValue)
							CommonsJcrUtils.addStringToMultiValuedProp(film,
									FILM_CATEGORIES, button.getText());
						else
							CommonsJcrUtils.removeStringFromMultiValuedProp(
									film, FILM_CATEGORIES, button.getText());
						parentEditPart.refresh();
						parentEditPart.markDirty();
					}
				}
			});
		}
	}

	private void createGenrePart(final Composite parent) {
		parent.setLayout(createBasicRL());
		genrePart = new AbstractFormPart() {
			public void refresh() {
				try {
					super.refresh();
					if (FilmMainInfoPanelComposite.this.isDisposed())
						return;

					Control[] oldChildren = parent.getChildren();
					for (Control child : oldChildren)
						child.dispose();

					if (film.hasProperty(FILM_GENRES)) {
						Value[] values = film.getProperty(FILM_GENRES)
								.getValues();
						for (final Value value : values) {
							toolkit.createLabel(parent, value.getString());
							Button deleteBtn = createDeleteButton(parent);
							deleteBtn
									.addSelectionListener(getDeleteBtnListener(
											genrePart, film, FILM_GENRES,
											value.getString()));
						}
						// relatedCmp.pack();
					}
					Link addRelatedLk = createAddValueLk(parent);
					addRelatedLk
							.addSelectionListener(getAddGenreListener(parent
									.getShell()));

					parent.layout(false);
					parent.getParent().getParent().layout();

				} catch (RepositoryException re) {
					throw new PeopleException(
							"Unable to refresh genre form part for film "
									+ film, re);
				}
				parent.layout();
			}
		};

		genrePart.refresh();
		genrePart.initialize(form);
		form.addPart(genrePart);
		localParts.add(genrePart);
	}

	/**
	 * Populate a parent composite with controls to manage tag like countries
	 */
	private void createProdCountryPart(final Composite parent,
			final String tagPropName) {

		parent.setLayout(createBasicRL());
		// toolkit.createLabel(parent, "test", SWT.NO_FOCUS);

		countryPart = new AbstractFormPart() {
			public void refresh() {
				super.refresh();
				if (FilmMainInfoPanelComposite.this.isDisposed())
					return;
				cleanChildren(parent);

				try {
					if (film.hasProperty(tagPropName)) {
						Value[] values = film.getProperty(tagPropName)
								.getValues();
						for (final Value value : values) {
							final String valueStr = value.getString();
							String labelStr = ResourcesJcrUtils
									.getCountryEnLabelFromIso(
											film.getSession(), valueStr);
							toolkit.createLabel(parent, labelStr, SWT.BOTTOM);

							Button deleteBtn = createDeleteButton(parent);
							deleteBtn
									.addSelectionListener(new SelectionAdapter() {
										private static final long serialVersionUID = 1L;

										@Override
										public void widgetSelected(
												final SelectionEvent event) {
											CommonsJcrUtils
													.removeMultiPropertyValue(
															film, tagPropName,
															valueStr);
											countryPart.refresh();
											countryPart.markDirty();
										}
									});
						}
					}
					Link addRelatedLk = createAddValueLk(parent);
					addRelatedLk
							.addSelectionListener(getAddCountryListener(parent
									.getShell()));

					parent.layout(false);
					parent.getParent().layout();
				} catch (RepositoryException re) {
					throw new PeopleException("Language list", re);
				}
			}
		};
		countryPart.refresh();

		countryPart.initialize(form);
		form.addPart(countryPart);
		localParts.add(countryPart);
	}

	/**
	 * Populate a parent composite with controls to manage tag like languages
	 * 
	 * @param parent
	 * 
	 * @param property
	 *            the multi-valued property we want to update
	 */
	private void createOriginalLangPart(final Composite parent,
			final String langPropName) {
		parent.setLayout(createBasicRL());

		oLangPart = new AbstractFormPart() {
			public void refresh() {
				super.refresh();

				if (FilmMainInfoPanelComposite.this.isDisposed())
					return;
				cleanChildren(parent);

				try {
					if (film.hasProperty(langPropName)) {
						Value[] values = film.getProperty(langPropName)
								.getValues();
						for (final Value value : values) {
							final String valueStr = value.getString();
							String labelStr = ResourcesJcrUtils
									.getLangEnLabelFromIso(film.getSession(),
											valueStr);
							toolkit.createLabel(parent, labelStr, SWT.BOTTOM);
							Button deleteBtn = createDeleteButton(parent);
							deleteBtn
									.addSelectionListener(new SelectionAdapter() {
										private static final long serialVersionUID = 1L;

										@Override
										public void widgetSelected(
												final SelectionEvent event) {
											CommonsJcrUtils
													.removeStringFromMultiValuedProp(
															film, langPropName,
															valueStr);
											oLangPart.refresh();
											oLangPart.markDirty();

										}
									});
						}
						Link addLangLk = createAddValueLk(parent);
						addLangLk
								.addSelectionListener(getAddLangListener(parent
										.getShell()));

						parent.layout(false);
						parent.getParent().layout();
					}
				} catch (RepositoryException re) {
					throw new PeopleException("Language list", re);
				}
			}
		};
		oLangPart.refresh();

		oLangPart.initialize(form);
		form.addPart(oLangPart);
		localParts.add(oLangPart);
	}

	private void cleanChildren(Composite parent) {
		// We redraw the full control at each refresh, might be a more
		// efficient way to do
		Control[] oldChildren = parent.getChildren();
		for (Control child : oldChildren)
			child.dispose();
	}

	private RowLayout createBasicRL() {
		RowLayout rl = new RowLayout(SWT.HORIZONTAL);
		rl.wrap = true;
		rl.marginLeft = 5;
		rl.marginRight = 0;
		return rl;
	}

	private Link createAddValueLk(Composite parent) {
		Link link = new Link(parent, SWT.CENTER);
		toolkit.adapt(link, false, false);
		link.setText("<a>Add</a>");
		return link;
	}

	private Button createDeleteButton(Composite parent) {
		Button button = new Button(parent, SWT.FLAT);
		button.setData(PeopleUiConstants.CUSTOM_VARIANT,
				PeopleUiConstants.CSS_FLAT_IMG_BUTTON);
		button.setImage(PeopleImages.DELETE_BTN);
		RowData rd = new RowData();
		rd.height = 16;
		rd.width = 16;
		button.setLayoutData(rd);
		return button;
	}

	private SelectionListener getDeleteBtnListener(final AbstractFormPart part,
			final Node node, final String propName, final String value) {
		return new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				CommonsJcrUtils.removeRefFromMultiValuedProp(node, propName,
						value);
				part.refresh();
				part.markDirty();
			}
		};
	}

	// Configure the action launched when the user click the add link in the
	// Related to composite
	private SelectionListener getAddGenreListener(final Shell shell) {
		return new SelectionAdapter() {
			// TODO clean this
			final String[] elements = { "Roadmovie", "Thriller", "Drama",
					"Romantic Comedy", "Erotic Movie", "Horror" };

			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				PickUpFromListDialog diag = new PickUpFromListDialog(shell,
						"Choose an entity", elements);
				int result = diag.open();
				if (result != Window.OK)
					return;
				String value = diag.getSelected();
				String errMsg = CommonsJcrUtils.addStringToMultiValuedProp(
						film, FILM_GENRES, value);
				if (errMsg != null)
					MessageDialog.openError(PeopleUiPlugin.getDefault()
							.getWorkbench().getActiveWorkbenchWindow()
							.getShell(), "Dupplicates", errMsg);
				else {
					genrePart.refresh();
					genrePart.markDirty();
				}
			}
		};
	}

	private SelectionListener getAddCountryListener(final Shell shell) {
		return new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				try {
					PickUpCountryDialog diag = new PickUpCountryDialog(shell,
							"Add", film.getSession());
					diag.open();
					String lang = diag.getSelected();
					if (CommonsJcrUtils.checkNotEmptyString(lang)) {
						CommonsJcrUtils.addStringToMultiValuedProp(film,
								FILM_PROD_COUNTRY, lang);
						countryPart.refresh();
						countryPart.markDirty();
					}
				} catch (RepositoryException e) {
					throw new PeopleException("Unable to add language", e);
				}

			}
		};
	}

	private SelectionListener getAddLangListener(final Shell shell) {
		return new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				try {
					PickUpLangDialog diag = new PickUpLangDialog(shell, "Add",
							film.getSession());
					diag.open();
					String lang = diag.getSelected();
					if (CommonsJcrUtils.checkNotEmptyString(lang))
						CommonsJcrUtils.addStringToMultiValuedProp(film,
								FILM_ORIGINAL_LANGUAGE, lang);
					oLangPart.refresh();
					oLangPart.markDirty();

				} catch (RepositoryException e) {
					throw new PeopleException("Unable to add language", e);
				}

			}
		};
	}

	protected void disposePart(AbstractFormPart part) {
		if (part != null) {
			form.removePart(part);
			part.dispose();
		}
	}

	@Override
	public void dispose() {
		for (AbstractFormPart part : localParts)
			disposePart(part);
		super.dispose();
	}

	private void populateAltTitleGroup(Composite parent) {
		parent.setLayout(PeopleUiUtils.gridLayoutNoBorder());
		final Group group = new Group(parent, 0);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		group.setText("Titles");
		group.setLayout(PeopleUiUtils.gridLayoutNoBorder());

		final Link addLk = isCheckedOut ? new Link(group, SWT.BOTTOM) : null;
		if (addLk != null)
			addLk.setText("<a>Add a new title</a>");

		titlesViewer = createItemViewer(group);

		AbstractFormPart formPart = new AbstractFormPart() {
			public void refresh() {
				super.refresh();
				if (FilmMainInfoPanelComposite.this.isDisposed())
					return;
				refreshTitlesViewer();
				group.layout();
			}
		};

		configureAltTitleViewer(titlesViewer, formPart);
		titlesViewer.setContentProvider(new MyTableContentProvider());
		if (addLk != null)
			configureAddTitleLink(formPart, addLk, film,
					"Add a new title for current film");
		formPart.refresh();

		parent.layout();
		formPart.initialize(form);
		form.addPart(formPart);
		localParts.add(formPart);
	}

	private void populateTimeStampsGroup(Composite parent) {
		parent.setLayout(PeopleUiUtils.gridLayoutNoBorder());
		final Group group = new Group(parent, 0);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		group.setText("Awards and Festivals");
		group.setLayout(PeopleUiUtils.gridLayoutNoBorder());

		final Link addLk = isCheckedOut ? new Link(group, SWT.CENTER) : null;
		if (addLk != null)
			addLk.setText("<a>Add a new noteworthy date</a>");

		awardsViewer = createItemViewer(group);

		AbstractFormPart formPart = new AbstractFormPart() {
			public void refresh() {
				super.refresh();
				if (FilmMainInfoPanelComposite.this.isDisposed())
					return;
				refreshTimeStampsViewer();
				group.layout();
			}
		};

		configureTimestampsViewer(awardsViewer, formPart);
		awardsViewer.setContentProvider(new MyTableContentProvider());

		if (addLk != null)
			configureAddTimestampLink(formPart, addLk, film,
					"Add a new title for current film");

		formPart.refresh();
		parent.layout();
		formPart.initialize(form);
		form.addPart(formPart);
		localParts.add(formPart);
	}

	private void configureTimestampsViewer(TableViewer itemsViewer,
			AbstractFormPart part) {
		List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();

		columns.add(new ColumnDefinition(null, Property.JCR_TITLE,
				PropertyType.STRING, "Title", 120));
		columns.add(new ColumnDefinition(null, Property.JCR_DESCRIPTION,
				PropertyType.STRING, "Description", 180));

		NodeViewerComparator comparator = new NodeViewerComparator();

		// RAP SPECIFIC, enable adding link
		itemsViewer.getTable().setData(PeopleUiConstants.MARKUP_ENABLED,
				Boolean.TRUE);
		itemsViewer.getTable().addSelectionListener(
				new TimestampRwtAdapter(part));

		// The columns
		TableViewerColumn col;

		// specific columns
		col = ViewerUtils.createTableViewerColumn(itemsViewer, "Date",
				SWT.NONE, 80);
		col.setLabelProvider(new DateLabelProvider());
		col.getColumn().addSelectionListener(
				PeopleUiUtils.getSelectionAdapter(0, PropertyType.DATE,
						FILM_TIMESTAMP_VALUE, comparator, itemsViewer));

		col = ViewerUtils.createTableViewerColumn(itemsViewer, "Country",
				SWT.NONE, 80);
		col.setLabelProvider(new CountryLabelProvider());
		col.getColumn().addSelectionListener(
				PeopleUiUtils.getSelectionAdapter(1, PropertyType.STRING,
						FILM_AWARD_COUNTRY_ISO, comparator, itemsViewer));

		int i = 2;
		for (ColumnDefinition colDef : columns) {
			col = ViewerUtils.createTableViewerColumn(itemsViewer,
					colDef.getHeaderLabel(), SWT.NONE, colDef.getColumnSize());
			col.setLabelProvider(new SimpleJcrNodeLabelProvider(colDef
					.getPropertyName()));
			col.setEditingSupport(new TextEditingSupport(itemsViewer, part,
					colDef.getPropertyName()));

			col.getColumn().addSelectionListener(
					PeopleUiUtils.getSelectionAdapter(i,
							colDef.getPropertyType(), colDef.getPropertyName(),
							comparator, itemsViewer));
			i++;
		}

		ColumnDefinition firstCol = columns.get(0);
		// IMPORTANT: initialize comparator before setting it
		comparator.setColumn(firstCol.getPropertyType(),
				firstCol.getPropertyName());
		itemsViewer.setComparator(comparator);
	}

	private void configureAddTimestampLink(final AbstractFormPart part,
			final Link link, final Node film, String tooltip) {
		link.setToolTipText(tooltip);
		link.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		link.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				NewTimeStampDialog dialog = new NewTimeStampDialog(link
						.getShell(), "Add a new timestamp", film);
				int res = dialog.open();
				if (res == org.eclipse.jface.dialogs.Dialog.OK) {
					part.refresh();
					part.markDirty();
				}
			}
		});
	}

	private void configureAddTitleLink(final AbstractFormPart part,
			final Link button, final Node film, String tooltip) {
		button.setToolTipText(tooltip);
		button.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, false, false));
		button.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				NewTitleDialog dialog = new NewTitleDialog(button.getShell(),
						"Add a new title", film);
				int res = dialog.open();
				if (res == org.eclipse.jface.dialogs.Dialog.OK) {
					part.refresh();
					part.markDirty();
				}
			}
		});
	}

	private void configureAltTitleViewer(TableViewer itemsViewer,
			AbstractFormPart part) {
		List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();
		columns.add(new ColumnDefinition(FilmTypes.FILM_TITLE,
				FilmNames.FILM_TITLE_VALUE, PropertyType.STRING, "Title", 150));
		columns.add(new ColumnDefinition(FilmTypes.FILM_TITLE,
				FilmNames.FILM_TITLE_ARTICLE, PropertyType.STRING, "Article",
				60));
		columns.add(new ColumnDefinition(FilmTypes.FILM_TITLE,
				FilmNames.FILM_TITLE_LATIN_PRONUNCIATION, PropertyType.STRING,
				"Latin pronun.", 150));

		NodeViewerComparator comparator = new NodeViewerComparator();

		// RAP SPECIFIC, enable adding link
		itemsViewer.getTable().setData(PeopleUiConstants.MARKUP_ENABLED,
				Boolean.TRUE);
		itemsViewer.getTable().addSelectionListener(
				new LangListRwtAdapter(part));

		// The columns
		TableViewerColumn col;
		EditingSupport editingSupport;

		// original title
		col = ViewerUtils.createTableViewerColumn(itemsViewer, "", SWT.CENTER,
				25);
		editingSupport = new OriginalEditingSupport(itemsViewer, part,
				FilmNames.FILM_TITLE_IS_ORIG);
		col.setEditingSupport(editingSupport);
		col.setLabelProvider(new BooleanFlagLabelProvider(
				FilmNames.FILM_TITLE_IS_ORIG, PeopleImages.ORIGINAL_BTN, null));

		// primary item
		col = ViewerUtils.createTableViewerColumn(itemsViewer, "", SWT.CENTER,
				25);
		editingSupport = new PrimaryEditingSupport(itemsViewer, part,
				PeopleNames.PEOPLE_IS_PRIMARY);
		col.setEditingSupport(editingSupport);
		col.setLabelProvider(new BooleanFlagLabelProvider(
				PeopleNames.PEOPLE_IS_PRIMARY, PeopleImages.PRIMARY_BTN,
				PeopleImages.PRIMARY_NOT_BTN));

		// the language
		col = ViewerUtils.createTableViewerColumn(itemsViewer, "lang.",
				SWT.NONE, 40);
		col.setLabelProvider(new LangLabelProvider());

		col.getColumn().addSelectionListener(
				PeopleUiUtils.getSelectionAdapter(2, PropertyType.STRING,
						PeopleNames.PEOPLE_LANG, comparator, itemsViewer));

		int i = 3;
		for (ColumnDefinition colDef : columns) {
			col = ViewerUtils.createTableViewerColumn(itemsViewer,
					colDef.getHeaderLabel(), SWT.NONE, colDef.getColumnSize());
			col.setLabelProvider(new SimpleJcrNodeLabelProvider(colDef
					.getPropertyName()));
			col.setEditingSupport(new TextEditingSupport(itemsViewer, part,
					colDef.getPropertyName()));

			col.getColumn().addSelectionListener(
					PeopleUiUtils.getSelectionAdapter(i,
							colDef.getPropertyType(), colDef.getPropertyName(),
							comparator, itemsViewer));
			i++;
		}

		ColumnDefinition firstCol = columns.get(0);
		// IMPORTANT: initialize comparator before setting it
		comparator.setColumn(firstCol.getPropertyType(),
				firstCol.getPropertyName());
		itemsViewer.setComparator(comparator);
	}

	// //////////////////////////////////
	// Table viewers helpers
	private void refreshTitlesViewer() {
		try {
			if (film.hasNode(FilmNames.FILM_TITLES)) {
				List<Node> titles = JcrUtils.nodeIteratorToList(film.getNode(
						FilmNames.FILM_TITLES).getNodes());
				titlesViewer.setInput(titles.toArray(new Node[titles.size()]));
				titlesViewer.refresh();
			}
		} catch (RepositoryException e) {
			throw new PeopleException(
					"unexpected error while getting film titles for node "
							+ film, e);
		}
	}

	private void refreshTimeStampsViewer() {
		try {
			if (film.hasNode(FilmNames.FILM_TIMESTAMPS)) {
				List<Node> stamps = JcrUtils.nodeIteratorToList(film.getNode(
						FilmNames.FILM_TIMESTAMPS).getNodes());
				awardsViewer.setInput(stamps.toArray(new Node[stamps.size()]));
			}
		} catch (RepositoryException e) {
			throw new PeopleException(
					"unexpected error while getting film timestamps for node "
							+ film, e);
		}
	}

	private TableViewer createItemViewer(Composite parent) {
		// Table control creation
		int style = SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL;
		Table table = new Table(parent, style);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		table.setLayoutData(gd);
		TableViewer itemsViewer = new TableViewer(table);
		itemsViewer.setContentProvider(new MyTableContentProvider());
		return itemsViewer;
	}

	private class MyTableContentProvider implements IStructuredContentProvider {
		private static final long serialVersionUID = 7164029504991808317L;

		public Object[] getElements(Object inputElement) {
			return (Object[]) inputElement;
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			viewer.refresh();
		}
	}

	private final String DATE_TYPE = "date";
	private final String COUNTRY_TYPE = "country";

	class TimestampRwtAdapter extends SelectionAdapter {
		private static final long serialVersionUID = -2031967921306265218L;

		private final AbstractFormPart part;

		public TimestampRwtAdapter(AbstractFormPart part) {
			super();
			this.part = part;
		}

		public void widgetSelected(SelectionEvent event) {
			String type = "";
			try {
				if (event.detail == RWT.HYPERLINK) {
					String[] command = event.text.split("/");
					type = command[0];
					String uid = command[1];
					Session session = film.getSession();

					if (DATE_TYPE.equals(type)) {
						PickUpDateDialog diag = new PickUpDateDialog(
								event.display.getActiveShell(), "Choose a date");
						diag.open();
						Calendar cal = diag.getSelected();
						Node node = session.getNodeByIdentifier(uid);
						if (JcrUiUtils.setJcrProperty(node,
								FilmNames.FILM_TIMESTAMP_VALUE,
								PropertyType.DATE, cal)) {
							part.refresh();
							part.markDirty();
						}
					} else if (COUNTRY_TYPE.equals(type)) {
						PickUpCountryDialog diag = new PickUpCountryDialog(
								event.display.getActiveShell(),
								"Choose a country", session);
						diag.open();
						String countryIso = diag.getSelected();
						if (CommonsJcrUtils.checkNotEmptyString(countryIso)) {
							Node node = session.getNodeByIdentifier(uid);
							if (JcrUiUtils.setJcrProperty(node,
									FilmNames.FILM_AWARD_COUNTRY_ISO,
									PropertyType.STRING, countryIso)) {
								part.refresh();
								part.markDirty();
							}
						}
					}
				}
			} catch (RepositoryException re) {
				throw new PeopleException("Unable to update timestamp " + type
						+ " value", re);
			}
		}
	}

	private class DateLabelProvider extends ColumnLabelProvider {
		private static final long serialVersionUID = -3585657671575195850L;

		private DateFormat dateFormat = new SimpleDateFormat(
				PeopleUiConstants.DEFAULT_DATE_FORMAT);

		@Override
		public String getText(Object element) {
			try {
				Node currNode = (Node) element;
				if (currNode.hasProperty(FILM_TIMESTAMP_VALUE)) {
					String formattedDate = dateFormat.format(currNode
							.getProperty(FILM_TIMESTAMP_VALUE).getDate()
							.getTime());

					if (!CommonsJcrUtils.isNodeCheckedOutByMe(film)) {
						return formattedDate;
					} else {
						String uri = DATE_TYPE + "/" + currNode.getIdentifier();
						return "<a " + PeopleUiConstants.PEOPLE_CSS_URL_STYLE
								+ " href=\"" + uri + "\" target=\"_rwt\">"
								+ formattedDate + "</a>";
					}
				}
				return "";
			} catch (RepositoryException re) {
				throw new ArgeoException("Unable to get text from row", re);
			}
		}
	}

	private class CountryLabelProvider extends ColumnLabelProvider {
		private static final long serialVersionUID = 5901879216975700264L;

		@Override
		public String getText(Object element) {
			try {
				Node node = (Node) element;
				String countryIso = CommonsJcrUtils.get(node,
						FILM_AWARD_COUNTRY_ISO);
				String countryLbl = ResourcesJcrUtils.getCountryEnLabelFromIso(
						film.getSession(), countryIso);

				if (!CommonsJcrUtils.isNodeCheckedOutByMe(film)) {
					return countryLbl;
				} else {
					String uri = COUNTRY_TYPE + "/" + node.getIdentifier();
					return "<a " + PeopleUiConstants.PEOPLE_CSS_URL_STYLE
							+ " href=\"" + uri + "\" target=\"_rwt\">"
							+ countryLbl + "</a>";
				}
			} catch (RepositoryException e) {
				throw new PeopleException(
						"Error while preparing lang edition link for node "
								+ element, e);
			}

		}
	}

	private class LangListRwtAdapter extends SelectionAdapter {

		private static final long serialVersionUID = -3867410418907732579L;
		private final AbstractFormPart part;

		public LangListRwtAdapter(AbstractFormPart part) {
			super();
			this.part = part;
		}

		public void widgetSelected(SelectionEvent event) {
			try {
				if (event.detail == RWT.HYPERLINK) {
					String uid = event.text;
					Session session = film.getSession();

					PickUpLangDialog diag = new PickUpLangDialog(
							event.display.getActiveShell(),
							"Choose a language", session);
					diag.open();
					String lang = diag.getSelected();
					if (CommonsJcrUtils.checkNotEmptyString(lang)) {
						Node node = session.getNodeByIdentifier(uid);
						if (JcrUiUtils.setJcrProperty(node,
								PeopleNames.PEOPLE_LANG, PropertyType.STRING,
								lang)) {
							part.refresh();
							part.markDirty();
						}
					}
				}
			} catch (RepositoryException re) {
				throw new PeopleException("Unable to set new lang for title",
						re);
			}
		}
	}

	private class LangLabelProvider extends ColumnLabelProvider {
		private static final long serialVersionUID = -4185990064274214740L;

		@Override
		public String getText(Object element) {
			Node node = (Node) element;
			String currValue = CommonsJcrUtils.get(node,
					PeopleNames.PEOPLE_LANG);
			if (!CommonsJcrUtils.isNodeCheckedOutByMe(film)) {
				return currValue;
			} else {
				try {
					String uri = node.getIdentifier();
					return "<a " + PeopleUiConstants.PEOPLE_CSS_URL_STYLE
							+ " href=\"" + uri + "\" target=\"_rwt\">"
							+ currValue + "</a>";
				} catch (RepositoryException e) {
					throw new PeopleException(
							"Error while preparing lang edition link for node "
									+ element, e);
				}
			}
		}
	}

	private class BooleanFlagLabelProvider extends ColumnLabelProvider {
		private static final long serialVersionUID = 1L;

		private final String propertyName; // = PeopleNames.PEOPLE_IS_PRIMARY;
		private final Image imgTrue; // = PeopleImages.PRIMARY_BTN;
		private final Image imgFalse; // = PeopleImages.PRIMARY_NOT_BTN;

		public BooleanFlagLabelProvider(String propertyName, Image imgTrue,
				Image imgFalse) {
			this.propertyName = propertyName;
			this.imgTrue = imgTrue;
			this.imgFalse = imgFalse;
		}

		@Override
		public String getText(Object element) {
			return null;
		}

		@Override
		public Image getImage(Object element) {
			boolean isPrimary = false;
			try {
				Node currNode = ((Node) element);
				if (currNode.hasProperty(propertyName)
						&& currNode.getProperty(propertyName).getValue()
								.getType() == PropertyType.BOOLEAN)
					isPrimary = currNode.getProperty(propertyName).getBoolean();
			} catch (RepositoryException e) {
				throw new PeopleException("Unable to get " + propertyName
						+ " value for node " + element, e);
			}

			if (isPrimary) {
				return imgTrue;
			} else {
				return imgFalse;
			}
		}
	}

	private class PrimaryEditingSupport extends BooleanEditingSupport {
		private static final long serialVersionUID = 7142181193364269102L;
		private final AbstractFormPart part;

		public PrimaryEditingSupport(TableViewer viewer, AbstractFormPart part,
				String propertyName) {
			super(viewer, propertyName);
			this.part = part;
		}

		@Override
		protected void setValue(Object element, Object value) {
			Node currNode = (Node) element;
			if (((Boolean) value).booleanValue()
					&& FilmJcrUtils.updatePrimaryTitle(film, currNode)) {
//				part.refresh();
				// we refresh all parts to also refresh the header
				for (IFormPart part : form.getParts()) {
					part.refresh();
				}
				part.markDirty();
				
			}
		}
	}

	private class OriginalEditingSupport extends BooleanEditingSupport {
		private static final long serialVersionUID = 8386010388202750285L;
		private final AbstractFormPart part;

		public OriginalEditingSupport(TableViewer viewer,
				AbstractFormPart part, String propertyName) {
			super(viewer, propertyName);
			this.part = part;
		}

		@Override
		protected void setValue(Object element, Object value) {
			Node currNode = (Node) element;
			if (FilmJcrUtils.markAsOriginalTitle(film, currNode,
					(Boolean) value)) {
				//part.refresh();
				// we refresh all parts to also refresh the header
				for (IFormPart part : form.getParts()) {
					part.refresh();
				}
				part.markDirty();
				
			}
		}
	}

	private abstract class BooleanEditingSupport extends EditingSupport {
		private static final long serialVersionUID = 1L;
		private final TableViewer viewer;
		private final String propertyName;

		public BooleanEditingSupport(TableViewer viewer, String propertyName) {
			super(viewer);
			this.viewer = viewer;
			this.propertyName = propertyName;
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return new CheckboxCellEditor(viewer.getTable());
		}

		@Override
		protected boolean canEdit(Object element) {
			return CommonsJcrUtils.isNodeCheckedOutByMe(film);
		}

		@Override
		protected Object getValue(Object element) {
			// check if current row display a primary title
			try {
				Node currNode = (Node) element;
				if (currNode.hasProperty(propertyName)
						&& currNode.getProperty(propertyName).getValue()
								.getType() == PropertyType.BOOLEAN)
					return currNode.getProperty(propertyName).getBoolean();
			} catch (RepositoryException e) {
				throw new PeopleException("Unable to get " + propertyName
						+ " value for node " + element, e);
			}
			return false;
		}
	}

	private class TextEditingSupport extends EditingSupport {
		private static final long serialVersionUID = 1L;
		private final TableViewer viewer;
		private final String propertyName;
		private final AbstractFormPart part;

		public TextEditingSupport(TableViewer viewer, AbstractFormPart part,
				String propertyName) {
			super(viewer);
			this.viewer = viewer;
			this.propertyName = propertyName;
			this.part = part;
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return new TextCellEditor(viewer.getTable());
		}

		@Override
		protected boolean canEdit(Object element) {
			return CommonsJcrUtils.isNodeCheckedOutByMe(film);
		}

		@Override
		protected Object getValue(Object element) {
			// check if current row display a primary title
			try {
				Node currNode = (Node) element;
				if (currNode.hasProperty(propertyName)
						&& currNode.getProperty(propertyName).getValue()
								.getType() == PropertyType.STRING)
					return currNode.getProperty(propertyName).getString();
			} catch (RepositoryException e) {
				throw new PeopleException("Unable to get " + propertyName
						+ " value for node " + element, e);
			}
			return "";
		}

		@Override
		protected void setValue(Object element, Object value) {
			Node currNode = (Node) element;
			if (JcrUiUtils.setJcrProperty(currNode, propertyName,
					PropertyType.STRING, (String) value)) {
				part.markDirty();
				viewer.update(element, null);
				part.markDirty();
			}
		}
	}

	private void addROTableViewer() {
		Composite altTitleCmp = toolkit.createComposite(innerCmp);
		GridData gd = new GridData(SWT.CENTER, SWT.FILL, true, true, 2, 1);
		gd.heightHint = 150;
		altTitleCmp.setLayoutData(gd);
		populateAltTitleGroup(altTitleCmp);

		Composite timeStampsCmp = toolkit.createComposite(innerCmp);
		timeStampsCmp.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true,
				true, 2, 1));
		populateTimeStampsGroup(timeStampsCmp);
	}

	private void addEditTableViewer() {
		Composite altTitleCmp = toolkit.createComposite(innerCmp);
		GridData gd = new GridData(SWT.CENTER, SWT.FILL, true, true);
		gd.heightHint = 150;
		altTitleCmp.setLayoutData(gd);
		populateAltTitleGroup(altTitleCmp);

		Composite timeStampsCmp = toolkit.createComposite(innerCmp);
		timeStampsCmp.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true,
				true));
		populateTimeStampsGroup(timeStampsCmp);
	}

	private boolean isValueContained(Node node, String propName, String value) {
		try {
			if (!node.hasProperty(propName) || value == null)
				return false;
			Value[] values = node.getProperty(propName).getValues();

			for (Value val : values) {
				if (value.equals(val.getString()))
					return true;
			}
			return false;
		} catch (RepositoryException re) {
			throw new PeopleException(
					"Unable to check if multi valued property " + propName
							+ " of node " + node + " already has this value "
							+ value, re);
		}
	}

	// private Text createLT(Composite parent, String label) {
	// Composite cmp = toolkit.createComposite(parent, SWT.NO_FOCUS);
	// cmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
	// GridLayout gl = new GridLayout(2, false);
	// gl.marginWidth = gl.verticalSpacing = gl.marginHeight = 0;
	// gl.horizontalSpacing = 5;
	// cmp.setLayout(gl);
	// toolkit.createLabel(cmp, label);
	// Text txt = toolkit.createText(cmp, "", SWT.BORDER);
	// txt.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
	// return txt;
	// }

	// private void removeMultiPropertyValue(Node entity, String propName,
	// String tagToRemove) {
	// try {
	// List<String> tags = new ArrayList<String>();
	// Value[] values = entity.getProperty(propName).getValues();
	// for (int i = 0; i < values.length; i++) {
	// String curr = values[i].getString();
	// if (!tagToRemove.equals(curr))
	// tags.add(curr);
	// }
	//
	// boolean wasCheckedOut = CommonsJcrUtils
	// .isNodeCheckedOutByMe(entity);
	// if (!wasCheckedOut)
	// CommonsJcrUtils.checkout(entity);
	// entity.setProperty(propName, tags.toArray(new String[tags.size()]));
	// if (wasCheckedOut)
	// form.dirtyStateChanged();
	// else
	// CommonsJcrUtils.saveAndCheckin(entity);
	// } catch (RepositoryException e) {
	// throw new PeopleException("unable to initialise deletion", e);
	// }
	// }

	// private void addMultiPropertyValue(Node node, String propName) {
	// try {
	// Value[] values;
	// String[] valuesStr;
	// String errMsg = null;
	// if (node.hasProperty(propName)) {
	// values = node.getProperty(propName).getValues();
	//
	// // Check dupplicate
	// for (Value tag : values) {
	// String curTagUpperCase = tag.getString().toUpperCase()
	// .trim();
	// if (value.toUpperCase().trim().equals(curTagUpperCase)) {
	// errMsg = value
	// + " is already in the list and thus could not be added.";
	// MessageDialog.openError(PeopleUiPlugin.getDefault()
	// .getWorkbench().getActiveWorkbenchWindow()
	// .getShell(), "Dupplicates", errMsg);
	// return;
	// }
	// }
	//
	// valuesStr = new String[values.length + 1];
	// int i;
	// for (i = 0; i < values.length; i++) {
	// valuesStr[i] = values[i].getString();
	// }
	// valuesStr[i] = value;
	// } else {
	// valuesStr = new String[1];
	// valuesStr[0] = value;
	// }
	//
	// boolean wasCheckedout = CommonsJcrUtils.isNodeCheckedOut(node);
	// if (!wasCheckedout)
	// CommonsJcrUtils.checkout(node);
	// node.setProperty(propName, valuesStr);
	// if (!wasCheckedout)
	// CommonsJcrUtils.saveAndCheckin(node);
	// else
	// form.dirtyStateChanged();
	//
	// for (IFormPart part : form.getParts()) {
	// ((AbstractFormPart) part).markStale();
	// part.refresh();
	// }
	// } catch (RepositoryException re) {
	// throw new ArgeoException("Unable to set tags", re);
	// }
	//
	// }
}