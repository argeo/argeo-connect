package org.argeo.connect.people.ui.toolkits;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.argeo.connect.film.FilmNames;
import org.argeo.connect.film.FilmTypes;
import org.argeo.connect.film.core.FilmJcrUtils;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.ui.PeopleImages;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.dialogs.NewTitleDialog;
import org.argeo.connect.people.ui.dialogs.PickUpCountryDialog;
import org.argeo.connect.people.ui.dialogs.PickUpLangDialog;
import org.argeo.connect.people.ui.utils.JcrUiUtils;
import org.argeo.connect.people.ui.utils.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.PeopleJcrUtils;
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
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Table;
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

	// private final static String LANG_KEY = "lang";
	// private final static String DEFAULT_LANG_KEY = "default";

	private final FormToolkit toolkit;
	private final IManagedForm form;
	private final Node film;

	public FilmToolkit(FormToolkit toolkit, IManagedForm form, Node film) {
		super(toolkit, form);
		// formToolkit
		// managedForm
		this.toolkit = toolkit;
		this.form = form;
		this.film = film;
	}

	/** Populate a panel with a list synopsis. */
	public void populateSynopsisPanel(Composite panel, List<String> langIsos) {
		panel.setLayout(PeopleUiUtils.gridLayoutNoBorder());
		try {
			for (String iso : langIsos) {
				Node currNode = FilmJcrUtils.getSynopsisNode(film, iso);
				// force creation to avoid npe and ease form life cycle
				if (currNode == null
						&& CommonsJcrUtils.isNodeCheckedOutByMe(film))
					currNode = FilmJcrUtils.addOrUpdateSynopsisNode(film, null,
							null, iso);
				if (currNode != null) {
					Composite composite = toolkit.createComposite(panel);
					composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
							true, true));
					populateSingleSynopsisCmp(
							composite,
							currNode,
							ResourcesJcrUtils.getLangEnLabelFromIso(
									film.getSession(), iso));
				}
			}
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to create synopsis panel", e);
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

	public void populateFilmDetailsPanel(Composite parent) {
		parent.setLayout(new GridLayout());

		Composite origLanCmp = toolkit.createComposite(parent);
		origLanCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		origLanCmp.setLayout(new GridLayout());
		populateLangPanel(origLanCmp, FILM_ORIGINAL_LANGUAGE, "Add...");

		Composite prodCountryCmp = toolkit.createComposite(parent);
		prodCountryCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true,
				false));
		prodCountryCmp.setLayout(new GridLayout());
		populateProdCountryPanel(prodCountryCmp, FILM_PROD_COUNTRY, "Add...");

		Composite mainInfoCmp = toolkit.createComposite(parent);
		mainInfoCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		populateMainInfoCmp(mainInfoCmp);

		Composite altTitleCmp = toolkit.createComposite(parent);
		altTitleCmp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		populateAltTitleGroup(altTitleCmp);

	}

	private void populateMainInfoCmp(Composite parent) {
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

		//
		final Button addBtn = toolkit
				.createButton(group, "Add title", SWT.PUSH);

		final TableViewer viewer = createAltTitleViewer(group);

		AbstractFormPart formPart = new AbstractFormPart() {
			public void refresh() {
				super.refresh();
				boolean checkedOut = CommonsJcrUtils.isNodeCheckedOutByMe(film);
				addBtn.setVisible(checkedOut);
				try {
					if (film.hasNode(FilmNames.FILM_TITLES)) {
						List<Node> titles = JcrUtils.nodeIteratorToList(film
								.getNode(FilmNames.FILM_TITLES).getNodes());
						viewer.setInput(titles.toArray(new Node[titles.size()]));
					}
				} catch (RepositoryException e) {
					throw new PeopleException(
							"unexpected error whil gettin film titles ", e);
				}
				group.layout();
			}
		};

		List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();
		columns.add(new ColumnDefinition(FilmTypes.FILM_TITLE,
				FilmNames.FILM_TITLE, PropertyType.STRING, "Title", 150));
		columns.add(new ColumnDefinition(FilmTypes.FILM_TITLE,
				FilmNames.FILM_TITLE_ARTICLE, PropertyType.STRING, "Article",
				60));
		columns.add(new ColumnDefinition(FilmTypes.FILM_TITLE,
				FilmNames.FILM_TITLE_LATIN_PRONUNCIATION, PropertyType.STRING,
				"Latin pronun.", 150));
		configureAltTitleViewer(viewer, formPart, columns);

		viewer.setContentProvider(new MyTableContentProvider());

		configureAddTitleButton(formPart, addBtn, film,
				"Add a new title for current film");

		parent.layout();
		formPart.initialize(form);
		form.addPart(formPart);
	}

	private class MyTableContentProvider implements IStructuredContentProvider {
		private static final long serialVersionUID = 7164029504991808317L;

		public Object[] getElements(Object inputElement) {
			return (Object[]) inputElement;
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	private void configureAddTitleButton(final AbstractFormPart part,
			final Button button, final Node film, String tooltip) {
		button.setToolTipText(tooltip);
		button.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));

		button.addSelectionListener(new SelectionListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				NewTitleDialog dialog = new NewTitleDialog(button.getShell(),
						"Add a new title", film);
				int res = dialog.open();

				if (res == org.eclipse.jface.dialogs.Dialog.OK)
					part.markDirty();

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

	}

	private TableViewer createAltTitleViewer(Composite parent) {
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

	private void configureAltTitleViewer(TableViewer itemsViewer,
			AbstractFormPart part, List<ColumnDefinition> columns) {

		NodeViewerComparator comparator = new NodeViewerComparator();

		itemsViewer.getTable().setData(PeopleUiConstants.MARKUP_ENABLED,
				Boolean.TRUE);

		// The columns
		TableViewerColumn col;
		EditingSupport editingSupport;

		// original title
		col = ViewerUtils.createTableViewerColumn(itemsViewer, "", SWT.CENTER,
				25);
		editingSupport = new BooleanEditingSupport(itemsViewer, part,
				FilmNames.FILM_TITLE_IS_ORIG);
		col.setEditingSupport(editingSupport);
		col.setLabelProvider(new BooleanFlagLabelProvider(
				FilmNames.FILM_TITLE_IS_ORIG, PeopleImages.ORIGINAL_BTN, null));

		// primary item
		col = ViewerUtils.createTableViewerColumn(itemsViewer, "", SWT.CENTER,
				25);
		editingSupport = new BooleanEditingSupport(itemsViewer, part,
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
		col.getColumn().addSelectionListener(new LangListRwtAdapter());

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

	private class LangListRwtAdapter extends SelectionAdapter {
		private static final long serialVersionUID = -3867410418907732579L;

		public void widgetSelected(SelectionEvent event) {
			if (event.detail == RWT.HYPERLINK) {
				String string = event.text;
				log.warn("Got an ID: " + string);
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

	private class BooleanEditingSupport extends EditingSupport {
		private static final long serialVersionUID = 1L;
		private final TableViewer viewer;
		private final String propertyName;
		private final AbstractFormPart part;

		public BooleanEditingSupport(TableViewer viewer, AbstractFormPart part,
				String propertyName) {
			super(viewer);
			this.viewer = viewer;
			this.propertyName = propertyName;
			this.part = part;
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

		@Override
		protected void setValue(Object element, Object value) {
			Node currNode = (Node) element;
			PeopleJcrUtils.markAsPrimary(currNode, (Boolean) value);
			viewer.update(element, null);
			part.markDirty();
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

	/**
	 * Populate a parent composite with controls to manage tag like languages
	 * 
	 * @param parent
	 * 
	 * @param property
	 *            the multi-valued property we want to update
	 */
	public void populateLangPanel(final Composite parent,
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
							.getShell(), newTagMsg, film.getSession());
					diag.open();
					String lang = diag.getSelected();
					if (CommonsJcrUtils.checkNotEmptyString(lang))
						addMultiPropertyValue(film, tagPropName, lang);
				} catch (RepositoryException e) {
					throw new PeopleException("Unable to add language", e);
				}

			}
		});

		final AbstractFormPart editPart = new AbstractFormPart() {
			public void refresh() {
				super.refresh();

				boolean isCO = CommonsJcrUtils.isNodeCheckedOutByMe(film);
				// show add button only in edit mode
				chooseLangLk.setVisible(isCO);

				// We redraw the full control at each refresh, might be a more
				// efficient way to do
				Control[] oldChildren = valuesCmp.getChildren();
				for (Control child : oldChildren)
					child.dispose();

				try {
					if (film.hasProperty(tagPropName)) {
						Value[] values = film.getProperty(tagPropName)
								.getValues();
						for (final Value value : values) {
							final String valueStr = value.getString();
							String labelStr = ResourcesJcrUtils
									.getLangEnLabelFromIso(film.getSession(),
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
											removeMultiPropertyValue(film,
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
			final String tagPropName, final String newTagMsg) {
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
							chooseLangLk.getShell(), newTagMsg, film
									.getSession(), film);
					diag.open();
					String lang = diag.getSelected();
					if (CommonsJcrUtils.checkNotEmptyString(lang))
						addMultiPropertyValue(film, tagPropName, lang);
				} catch (RepositoryException e) {
					throw new PeopleException("Unable to add language", e);
				}

			}
		});

		final AbstractFormPart editPart = new AbstractFormPart() {
			public void refresh() {
				super.refresh();
				boolean isCO = CommonsJcrUtils.isNodeCheckedOutByMe(film);
				// show add button only in edit mode
				chooseLangLk.setVisible(isCO);

				// We redraw the full control at each refresh, might be a more
				// efficient way to do
				Control[] oldChildren = nlCmp.getChildren();
				for (Control child : oldChildren)
					child.dispose();

				try {
					if (film.hasProperty(tagPropName)) {
						Value[] values = film.getProperty(tagPropName)
								.getValues();
						for (final Value value : values) {
							final String valueStr = value.getString();
							String labelStr = ResourcesJcrUtils
									.getCountryEnLabelFromIso(
											film.getSession(), valueStr);
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
											removeMultiPropertyValue(film,
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