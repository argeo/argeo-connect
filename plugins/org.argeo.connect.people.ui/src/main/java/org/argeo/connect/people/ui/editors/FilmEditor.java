package org.argeo.connect.people.ui.editors;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.film.FilmNames;
import org.argeo.connect.film.core.FilmJcrUtils;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.ui.JcrUiUtils;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.ui.providers.FilmOverviewLabelProvider;
import org.argeo.connect.people.ui.toolkits.FilmToolkit;
import org.argeo.connect.people.ui.toolkits.ListPanelToolkit;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

/**
 * Editor page that display a film with corresponding details
 */
public class FilmEditor extends AbstractEntityEditor {
	final static Log log = LogFactory.getLog(FilmEditor.class);

	public final static String ID = PeopleUiPlugin.PLUGIN_ID + ".filmEditor";

	// Main business Objects
	private Node film;

	private FilmToolkit filmPanelToolkit;
	private ListPanelToolkit listPanelToolkit;

	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		film = getNode();

		String shortName = FilmJcrUtils.getTitleForFilm(film);
		if (shortName != null) {
			if (shortName.length() > SHORT_NAME_LENGHT)
				shortName = shortName.substring(0, SHORT_NAME_LENGHT - 1)
						+ "...";
			setPartName(shortName);
		}
	}

	@Override
	protected void createToolkits() {
		filmPanelToolkit = new FilmToolkit(toolkit, getManagedForm());
		listPanelToolkit = new ListPanelToolkit(toolkit, getManagedForm(),
				getPeopleServices(), getPeopleUiServices());

	}

	protected void populateTabFolder(CTabFolder folder) {
		// Synopses
		String tooltip = "The synopses for film "
				+ JcrUtils.get(film, FilmNames.FILM_ID);
		Composite innerPannel = addTabToFolder(folder, SWT.NO_FOCUS,
				"Synopsis", "msm:synopses", tooltip);
		filmPanelToolkit.populateSynopsisPanel(innerPannel, film);

		// Crew
		tooltip = "Staff related to " + JcrUtils.get(film, FilmNames.FILM_ID);
		innerPannel = addTabToFolder(folder, SWT.NO_FOCUS, "Crew",
				PeopleUiConstants.PANEL_MEMBERS, tooltip);
		listPanelToolkit.populateMembersPanel(innerPannel, film);

	}

	protected void populateMainInfoComposite(final Composite parent) {
		try {
			parent.setLayout(new FormLayout());

			// READ ONLY PANEL
			final Composite roPanelCmp = toolkit.createComposite(parent,
					SWT.NO_FOCUS);
			PeopleUiUtils.setSwitchingFormData(roPanelCmp);
			roPanelCmp.setData(RWT.CUSTOM_VARIANT,
					PeopleUiConstants.PEOPLE_CSS_GENERALINFO_COMPOSITE);

			roPanelCmp.setLayout(new GridLayout());

			// Add a label with info provided by the FilmOverviewLabelProvider
			final Label filmInfoROLbl = toolkit.createLabel(roPanelCmp, "",
					SWT.WRAP);
			filmInfoROLbl.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
			final ColumnLabelProvider filmExtractLP = new FilmOverviewLabelProvider(
					false, getPeopleServices());

			// EDIT PANEL
			final Composite editPanelCmp = toolkit.createComposite(parent,
					SWT.NONE);
			PeopleUiUtils.setSwitchingFormData(editPanelCmp);
			editPanelCmp.setData(RWT.CUSTOM_VARIANT,
					PeopleUiConstants.PEOPLE_CSS_GENERALINFO_COMPOSITE);
			editPanelCmp.setLayout(new GridLayout(4, false));

			// Film ID
			Label lbl = toolkit.createLabel(editPanelCmp, "ID", SWT.NONE);
			lbl.setLayoutData(new GridData());
			final Text idTxt = toolkit.createText(editPanelCmp, "", SWT.BORDER
					| SWT.SINGLE | SWT.LEFT);
			idTxt.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
					| GridData.FILL_HORIZONTAL));

			// Original Title
			lbl = toolkit.createLabel(editPanelCmp, "Original Title", SWT.NONE);
			final Text origTitleTxt = toolkit.createText(editPanelCmp, "",
					SWT.BORDER | SWT.SINGLE | SWT.LEFT);
			origTitleTxt.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
					| GridData.FILL_HORIZONTAL));

			// Original Title Article
			lbl = toolkit.createLabel(editPanelCmp, "Orig. Title Article",
					SWT.NONE);
			final Text origTitleArticleTxt = toolkit.createText(editPanelCmp,
					"", SWT.BORDER | SWT.SINGLE | SWT.LEFT);
			origTitleArticleTxt.setLayoutData(new GridData(
					GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));

			// latin Title
			lbl = toolkit.createLabel(editPanelCmp, "Latin pronounciation",
					SWT.NONE);
			final Text latinTitleTxt = toolkit.createText(editPanelCmp, "",
					SWT.BORDER | SWT.SINGLE | SWT.LEFT);
			latinTitleTxt.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
					| GridData.FILL_HORIZONTAL));

			final EntityAbstractFormPart editPart = new EntityAbstractFormPart() {
				// Update values on refresh
				public void refresh() {
					super.refresh();

					// EDIT PART
					idTxt.setText(JcrUtils.get(film, FilmNames.FILM_ID));

					String latinTitle = JcrUtils.get(film,
							FilmNames.FILM_ORIG_LATIN_TITLE);
					if (latinTitle != null)
						latinTitleTxt.setText(latinTitle);

					String origTitle = JcrUtils.get(film,
							FilmNames.FILM_ORIGINAL_TITLE);
					if (origTitle != null)
						origTitleTxt.setText(origTitle);

					String origTitleArt = JcrUtils.get(film,
							FilmNames.FILM_ORIG_TITLE_ARTICLE);
					if (origTitleArt != null)
						origTitleArticleTxt.setText(origTitleArt);

					// READ ONLY PART
					String roText = filmExtractLP.getText(film);
					filmInfoROLbl.setText(roText);

					try {
						if (film.isCheckedOut())
							editPanelCmp.moveAbove(roPanelCmp);
						else
							editPanelCmp.moveBelow(roPanelCmp);
					} catch (RepositoryException e) {
						throw new PeopleException(
								"Unable to get checked out status", e);
					}
					editPanelCmp.getParent().layout();
					roPanelCmp.getParent().layout();
				}
			};

			// Listeners
			idTxt.addModifyListener(new ModifyListener() {
				private static final long serialVersionUID = 1L;

				@Override
				public void modifyText(ModifyEvent event) {
					if (JcrUiUtils.setJcrProperty(film, FilmNames.FILM_ID,
							PropertyType.STRING, idTxt.getText()))
						editPart.markDirty();
				}
			});

			latinTitleTxt.addModifyListener(new ModifyListener() {
				private static final long serialVersionUID = 1L;

				@Override
				public void modifyText(ModifyEvent event) {
					if (JcrUiUtils.setJcrProperty(film,
							FilmNames.FILM_ORIG_LATIN_TITLE,
							PropertyType.STRING, latinTitleTxt.getText()))
						editPart.markDirty();
				}
			});

			origTitleTxt.addModifyListener(new ModifyListener() {
				private static final long serialVersionUID = 1L;

				@Override
				public void modifyText(ModifyEvent event) {
					if (JcrUiUtils.setJcrProperty(film,
							FilmNames.FILM_ORIGINAL_TITLE, PropertyType.STRING,
							origTitleTxt.getText()))
						editPart.markDirty();
				}
			});

			origTitleArticleTxt.addModifyListener(new ModifyListener() {
				private static final long serialVersionUID = 1L;

				@Override
				public void modifyText(ModifyEvent event) {
					if (JcrUiUtils.setJcrProperty(film,
							FilmNames.FILM_ORIG_TITLE_ARTICLE,
							PropertyType.STRING, origTitleArticleTxt.getText()))
						editPart.markDirty();
				}
			});

			getManagedForm().addPart(editPart);

		} catch (Exception e) {
			// } catch (RepositoryException e) {
			throw new PeopleException("Cannot create main info section", e);
		}
	}

}