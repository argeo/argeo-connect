package org.argeo.connect.people.ui.toolkits;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.argeo.connect.film.FilmNames;
import org.argeo.connect.film.core.FilmJcrUtils;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.ui.PeopleUiService;
import org.argeo.connect.people.ui.composites.FilmMainInfoPanelComposite;
import org.argeo.connect.people.ui.composites.FilmPrintComposite;
import org.argeo.connect.people.ui.utils.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.ResourcesJcrUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Centralize the creation of the different editors panels for films.
 */
public class FilmToolkit extends EntityToolkit implements FilmNames {
	// private final static Log log = LogFactory.getLog(FilmToolkit.class);

	private final FormToolkit toolkit;
	private final IManagedForm form;
	private final Node film;
	private final PeopleService peopleService;

	public FilmToolkit(FormToolkit toolkit, IManagedForm form, Node film,
			PeopleService peopleService, PeopleUiService peopleUiService) {
		super(toolkit, form, peopleUiService);
		this.toolkit = toolkit;
		this.form = form;
		this.film = film;
		this.peopleService = peopleService;
	}

	/** Populate a panel with a list synopsis. */
	public void populateSynopsisPanel(final Composite parent,
			final List<String> langIsos) {

		parent.setLayout(PeopleUiUtils.gridLayoutNoBorder());

		// Add a scrolled container
		ScrolledComposite container = new ScrolledComposite(parent,
				SWT.NO_FOCUS | SWT.V_SCROLL); // SWT.H_SCROLL |
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		final Composite panel = new Composite(container, SWT.NO_FOCUS);
		panel.setLayout(new GridLayout());

		container.setExpandHorizontal(true);
		container.setExpandVertical(false);
		container.setContent(panel);

		final AbstractFormPart editPart = new AbstractFormPart() {
			public void refresh() {
				super.refresh();

				// Fix me. only try to add panel the first time it is legal.
				Control[] oldChildren = panel.getChildren();
				if (oldChildren.length == 0) {
					try {
						for (String iso : langIsos) {
							Node currNode = FilmJcrUtils.getSynopsisNode(film,
									iso);
							// force creation to avoid npe and ease form life
							// cycle
							if (currNode == null
									&& CommonsJcrUtils
											.isNodeCheckedOutByMe(film))
								currNode = FilmJcrUtils
										.addOrUpdateSynopsisNode(film, null,
												null, iso);
							if (currNode != null) {
								Composite composite = toolkit.createComposite(
										panel, SWT.NO_FOCUS);
								composite.setLayoutData(new GridData(SWT.FILL,
										SWT.FILL, true, true));
								AbstractFormPart part = populateSingleSynopsisCmp(
										composite, currNode,
										ResourcesJcrUtils
												.getLangEnLabelFromIso(
														film.getSession(), iso));
								// FIXME
								// children form part is usually added before
								// current, and thus refreshed before current.
								// force refresh to be sure.
								part.refresh();

							}
						}
						panel.pack(true);
						panel.getParent().pack(true);
						panel.getParent().layout();
						parent.layout();
					} catch (RepositoryException e) {
						throw new PeopleException(
								"Unable to populate synopsis panel", e);
					}
				}
			}
		};

		editPart.initialize(form);
		form.addPart(editPart);
	}

	/** Populate a synopsis composite. */
	private AbstractFormPart populateSingleSynopsisCmp(Composite panel,
			final Node synopsisNode, final String titleLabel) {
		panel.setLayout(new GridLayout());

		GridData gd;

		Group group = new Group(panel, 0);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		group.setText(titleLabel);
		group.setLayout(new GridLayout(2, false));

		// Logline
		PeopleUiUtils.createBoldLabel(toolkit, group, "Logline");
		final Text loglineTxt = toolkit.createText(group, "", SWT.BORDER
				| SWT.SINGLE);
		gd = new GridData(SWT.FILL, SWT.TOP, true, false);
		loglineTxt.setLayoutData(gd);

		// Short synopsis
		PeopleUiUtils.createBoldLabel(group, "Extract", SWT.TOP);
		final Text shortSynTxt = toolkit.createText(group, "", SWT.BORDER
				| SWT.MULTI | SWT.WRAP);
		gd = new GridData(SWT.FILL, SWT.TOP, true, false);
		gd.heightHint = 50;
		shortSynTxt.setLayoutData(gd);

		// Long synopsis
		PeopleUiUtils.createBoldLabel(group, "Synopsis", SWT.TOP);
		final Text synopsisTxt = toolkit.createText(group, "", SWT.BORDER
				| SWT.MULTI | SWT.WRAP);
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.heightHint = 100;
		synopsisTxt.setLayoutData(gd);

		final AbstractFormPart editPart = new AbstractFormPart() {
			public void refresh() {
				super.refresh();
				if (!shortSynTxt.isDisposed()) {
					PeopleUiUtils.refreshFormTextWidget(loglineTxt,
							synopsisNode, FILM_LOG_LINE, titleLabel
									+ " log line");
					PeopleUiUtils.refreshFormTextWidget(shortSynTxt,
							synopsisNode, SYNOPSIS_CONTENT_SHORT,
							"Enter a short " + titleLabel + " synopsis");
					PeopleUiUtils.refreshFormTextWidget(synopsisTxt,
							synopsisNode, SYNOPSIS_CONTENT, "Enter the full "
									+ titleLabel + "  synopsis");
				}
			}
		};

		PeopleUiUtils.addTxtModifyListener(editPart, loglineTxt, synopsisNode,
				FILM_LOG_LINE, PropertyType.STRING);
		PeopleUiUtils.addTxtModifyListener(editPart, shortSynTxt, synopsisNode,
				SYNOPSIS_CONTENT_SHORT, PropertyType.STRING);
		PeopleUiUtils.addTxtModifyListener(editPart, synopsisTxt, synopsisNode,
				SYNOPSIS_CONTENT, PropertyType.STRING);

		editPart.initialize(form);
		form.addPart(editPart);
		return editPart;
	}

	/**
	 * Populate a composite that display the list of the film prints for a given
	 * film.
	 */
	public void populateFilmPrintListCmp(Composite parent) {
		parent.setLayout(new GridLayout());

		// Create new button
		final Button addBtn = toolkit.createButton(parent, "Add film print",
				SWT.PUSH);

		// A scrollable composite with the list
		ScrolledComposite filmPrintScrollableCmp = new ScrolledComposite(
				parent, SWT.H_SCROLL | SWT.V_SCROLL);
		filmPrintScrollableCmp.setLayout(new GridLayout(1, false));
		filmPrintScrollableCmp.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
				true, true));
		filmPrintScrollableCmp.setExpandHorizontal(true);
		filmPrintScrollableCmp.setExpandVertical(false);
		final Composite body = toolkit.createComposite(filmPrintScrollableCmp,
				SWT.NONE);
		body.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, false, true));
		body.setLayout(PeopleUiUtils.gridLayoutNoBorder());
		filmPrintScrollableCmp.setContent(body);

		// Add life cycle management
		AbstractFormPart sPart = new AbstractFormPart() {
			public void refresh() {
				super.refresh();
				try {
					addBtn.setVisible(CommonsJcrUtils
							.isNodeCheckedOutByMe(film));
					// We redraw the full control at each refresh, might be a
					// more
					// efficient way to do
					Control[] oldChildren = body.getChildren();
					for (Control child : oldChildren)
						child.dispose();

					if (!film.hasNode(FILM_PRINTS)) // No film print to display
						return;

					NodeIterator ni = film.getNode(FILM_PRINTS).getNodes();
					while (ni.hasNext()) {
						Node currNode = ni.nextNode();
						Composite currCmp = new FilmPrintComposite(body,
								SWT.NO_FOCUS, toolkit, form, currNode);
						currCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP,
								true, false));
					}
				} catch (RepositoryException re) {
					throw new PeopleException(
							"Cannot refresh film prints list", re);
				}
				// IMPORTANT otherwise the scrolled composite is blank
				body.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				body.pack();
				body.getParent().layout(true, true);
			}
		};

		sPart.initialize(form);
		form.addPart(sPart);
		configureAddFilmPrintButton(sPart, addBtn, "Register a new film print");
	}

	private void configureAddFilmPrintButton(final AbstractFormPart part,
			final Button button, String tooltip) {
		button.setToolTipText(tooltip);
		button.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
		button.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				FilmJcrUtils.createFilmPrint(film);
				part.refresh();
				part.markDirty();
			}
		});
	}

	public void populateFilmMainInfoPanel(Composite parent) {
		parent.setLayout(PeopleUiUtils.gridLayoutNoBorder());
		MainInfoFormPart mainInfoPart = new MainInfoFormPart(parent);
		mainInfoPart.initialize(form);
		form.addPart(mainInfoPart);

	}

	private class MainInfoFormPart extends AbstractFormPart {
		private boolean isCurrentlyCheckedOut;
		private Composite parent;

		public MainInfoFormPart(Composite parent) {
			this.parent = parent;
			// will force creation on first pass
			isCurrentlyCheckedOut = !CommonsJcrUtils.isNodeCheckedOutByMe(film);
		}

		@Override
		public void refresh() {
			super.refresh();
			// ||
			if (isCurrentlyCheckedOut != CommonsJcrUtils
					.isNodeCheckedOutByMe(film)) {
				isCurrentlyCheckedOut = CommonsJcrUtils
						.isNodeCheckedOutByMe(film);

				for (Control control : parent.getChildren()) {
					control.dispose();
				}

				FilmMainInfoPanelComposite mainInfoCmp = new FilmMainInfoPanelComposite(
						parent, SWT.NO_FOCUS, toolkit, form, film,
						peopleService);
				mainInfoCmp.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
						true, true));
				parent.layout();
			}
		}
	}

	public void populateFilmAdditionalInfoPanel(Composite parent) {
		GridLayout layout = PeopleUiUtils.gridLayoutNoBorder(1);
		layout.horizontalSpacing = 5;
		parent.setLayout(layout);

		final Text directorTxt = createLT(parent, "Director:");

		// Flags
		Composite flagCmp = toolkit.createComposite(parent, SWT.NO_FOCUS);
		flagCmp.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false, 1, 1));
		flagCmp.setLayout(PeopleUiUtils.gridLayoutNoBorder(3));

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

		final Button hasTrailerBtn = toolkit.createButton(flagCmp,
				"Film Has Trailer", SWT.CHECK | SWT.LEFT);
		hasTrailerBtn.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false,
				false));

		final Button extractsOnTvBtn = toolkit.createButton(flagCmp,
				"Allows Extracts on TV", SWT.CHECK | SWT.LEFT);
		extractsOnTvBtn.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false,
				false, 2, 1));

		final AbstractFormPart additionalInfoPart = new AbstractFormPart() {
			public void refresh() {
				super.refresh();
				PeopleUiUtils.refreshFormTextWidget(directorTxt, film,
						FILM_DIRECTOR);

				PeopleUiUtils.refreshCheckBoxWidget(isPremiereBtn, film,
						FILM_IS_PREMIERE);
				PeopleUiUtils.refreshCheckBoxWidget(isStudentProjBtn, film,
						FILM_IS_STUDENT_PROJECT);
				PeopleUiUtils.refreshCheckBoxWidget(isDebutFilmBtn, film,
						FILM_IS_DEBUT_FILM);

				PeopleUiUtils.refreshCheckBoxWidget(hasTrailerBtn, film,
						FILM_HAS_TRAILER);
				PeopleUiUtils.refreshCheckBoxWidget(extractsOnTvBtn, film,
						FILM_EXTRACTS_ON_TV_ALLOWED);
			}
		};

		additionalInfoPart.refresh();

		PeopleUiUtils.addModifyListener(directorTxt, film, FILM_DIRECTOR,
				additionalInfoPart);
		PeopleUiUtils.addCheckBoxListener(isPremiereBtn, film,
				FILM_IS_PREMIERE, additionalInfoPart);
		PeopleUiUtils.addCheckBoxListener(isStudentProjBtn, film,
				FILM_IS_STUDENT_PROJECT, additionalInfoPart);
		PeopleUiUtils.addCheckBoxListener(isDebutFilmBtn, film,
				FILM_IS_DEBUT_FILM, additionalInfoPart);

		PeopleUiUtils.addCheckBoxListener(hasTrailerBtn, film,
				FILM_HAS_TRAILER, additionalInfoPart);
		PeopleUiUtils.addCheckBoxListener(extractsOnTvBtn, film,
				FILM_EXTRACTS_ON_TV_ALLOWED, additionalInfoPart);

		additionalInfoPart.initialize(form);
		form.addPart(additionalInfoPart);
	}

	private Text createLT(Composite parent, String label) {
		Composite cmp = toolkit.createComposite(parent, SWT.NO_FOCUS);
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

}